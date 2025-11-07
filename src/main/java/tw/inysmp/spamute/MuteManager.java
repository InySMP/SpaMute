package tw.inysmp.spamute;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MuteManager {

    private final SpaMute plugin;
    // 儲存禁言狀態: UUID -> 解除禁言的時間戳 (毫秒)
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private final File muteFile;
    private FileConfiguration muteConfig;
    private BukkitTask checkTask;

    // 定義日期格式，用於給玩家看
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public MuteManager(SpaMute plugin) {
        this.plugin = plugin;
        
        // --- 設定 log 資料夾路徑 ---
        File logFolder = new File(plugin.getDataFolder(), "log");
        
        // 如果 log 資料夾不存在，則創建它
        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                plugin.getLogger().info("已創建 /log/ 資料夾用於儲存禁言記錄。");
            } else {
                plugin.getLogger().severe("無法創建 /log/ 資料夾！請檢查權限。");
            }
        }
        
        // 將 muteFile 指向 log 資料夾內的 mutes.yml
        this.muteFile = new File(logFolder, "mutes.yml");
        // --- 設定結束 ---

        loadMuteData();
    }

    /**
     * 載入禁言數據文件 (mutes.yml)。
     */
    private void loadMuteData() {
        if (!muteFile.exists()) {
            try {
                muteFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("無法創建 mutes.yml 文件於 log 資料夾內！ " + e.getMessage());
            }
        }
        muteConfig = YamlConfiguration.loadConfiguration(muteFile);

        // 從文件中讀取數據到內存
        for (String key : muteConfig.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                long unmuteTime = muteConfig.getLong(key);
                
                // 只載入尚未到期的禁言
                if (unmuteTime > System.currentTimeMillis()) {
                    mutedPlayers.put(playerId, unmuteTime);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("mutes.yml 中發現無效的 UUID: " + key);
            }
        }
    }

    /**
     * 將當前的禁言數據儲存到文件。
     */
    public void saveMuteData() {
        muteConfig = new YamlConfiguration(); 
        for (Map.Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
            muteConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            plugin.getLogger().severe("無法儲存 mutes.yml 文件！ " + e.getMessage());
        }
    }

    /**
     * 執行禁言操作。
     * @param playerId 玩家的 UUID
     * @param durationSeconds 禁言時長 (秒)
     */
    public void mutePlayer(UUID playerId, long durationSeconds) {
        long unmuteTime = System.currentTimeMillis() + durationSeconds * 1000L;
        mutedPlayers.put(playerId, unmuteTime);
        saveMuteData(); 
    }

    /**
     * 檢查玩家是否在禁言中。
     * @param playerId 玩家的 UUID
     * @return 如果禁言中則返回 true。
     */
    public boolean isMuted(UUID playerId) {
        if (!mutedPlayers.containsKey(playerId)) {
            return false;
        }

        long unmuteTime = mutedPlayers.get(playerId);
        if (unmuteTime <= System.currentTimeMillis()) {
            // 已過期，自動解除
            unmutePlayer(playerId);
            return false;
        }
        return true;
    }
    
    // --- 新增: 供 /unmute 指令使用的 Public 方法 ---

    /**
     * 手動強制解除禁言。由指令處理器 (如 /unmute) 呼叫。
     * @param playerId 玩家的 UUID
     */
    public void forceUnmutePlayer(UUID playerId) {
        if (mutedPlayers.containsKey(playerId)) {
            mutedPlayers.remove(playerId);
            
            // 發送解除禁言通知給玩家
            if (Bukkit.getPlayer(playerId) != null && Bukkit.getPlayer(playerId).isOnline()) {
                 Bukkit.getPlayer(playerId).sendMessage(plugin.getMessage("unmute-success-target"));
            }
            
            saveMuteData(); 
        }
    }
    
    // --- Private: 供後臺自動解除使用的私有方法 ---

    /**
     * 自動解除禁言 (由檢查任務呼叫)。
     * @param playerId 玩家的 UUID
     */
    private void unmutePlayer(UUID playerId) {
        if (mutedPlayers.containsKey(playerId)) {
            mutedPlayers.remove(playerId);
            
            // 發送解除禁言通知給玩家
            if (Bukkit.getPlayer(playerId) != null && Bukkit.getPlayer(playerId).isOnline()) {
                 Bukkit.getPlayer(playerId).sendMessage(plugin.getMessage("player-unmuted-auto"));
            }
            
            // 通知管理員 
            plugin.getServer().broadcast(plugin.getPluginPrefix() + ChatColor.GREEN + 
                Bukkit.getOfflinePlayer(playerId).getName() + " 的禁言已自動解除。", "spamute.admin");
            
            saveMuteData(); 
        }
    }
    
    // --- 時間格式化與 Getter ---
    
    /**
     * 獲取解除禁言的時間 (格式化)。
     */
    public String getUnmuteTimeFormatted(UUID playerId) {
        if (!mutedPlayers.containsKey(playerId)) return "N/A";
        
        long unmuteTime = mutedPlayers.get(playerId);
        Instant instant = Instant.ofEpochMilli(unmuteTime);
        return DATE_FORMATTER.format(instant);
    }
    
    /**
     * 獲取玩家剩餘禁言時間 (格式化為易讀文本)。
     */
    public String getRemainingMuteTimeFormatted(UUID playerId) {
        if (!mutedPlayers.containsKey(playerId)) return "0秒";
        
        long remainingMillis = mutedPlayers.get(playerId) - System.currentTimeMillis();
        
        // 調用輔助方法
        return getRemainingTimeFormatted(remainingMillis);
    }
    
    /**
     * 將秒數轉換為易讀的時長格式 (例如 86400 -> 1天)。
     * @param durationSeconds 總時長（秒）
     * @return 格式化的時長字符串
     */
    public String formatDuration(long durationSeconds) {
        // 計算總毫秒數
        long totalMillis = durationSeconds * 1000L;
        
        // 直接調用輔助方法
        return getRemainingTimeFormatted(totalMillis);
    }

    /**
     * 獲取剩餘毫秒數 (格式化為易讀文本)。
     * 這是主要的格式化邏輯，用於避免代碼冗餘。
     */
    private String getRemainingTimeFormatted(long remainingMillis) {
        if (remainingMillis <= 0) return "0秒";

        long days = TimeUnit.MILLISECONDS.toDays(remainingMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小時");
        // 只有在沒有天數時，才顯示分鐘（避免太長）
        if (minutes > 0 && days == 0) sb.append(minutes).append("分"); 
        
        // 如果前面都沒有，或者只顯示了小時，則顯示秒
        if (sb.length() == 0 || (days == 0 && hours > 0 && minutes == 0)) {
             sb.append(seconds).append("秒"); 
        } else if (sb.length() == 0) {
             sb.append(seconds).append("秒");
        }


        return sb.toString();
    }


    /**
     * 啟動異步任務，定期檢查並自動解除禁言。
     */
    public void startMuteCheckTask() {
        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel();
        }
        
        // 每 20 秒檢查一次（異步任務不會卡住主線程）
        checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            mutedPlayers.entrySet().removeIf(entry -> {
                long unmuteTime = entry.getValue();
                
                if (unmuteTime <= System.currentTimeMillis()) {
                    // 已過期，解除禁言並發送消息（需要切換到主線程執行 Bukkit API）
                    UUID playerId = entry.getKey();
                    Bukkit.getScheduler().runTask(plugin, () -> unmutePlayer(playerId));
                    return true; // 從 Map 中移除
                }
                return false;
            });
        }, 20 * 20L, 20 * 20L); // 延遲 20 秒啟動，每 20 秒重複一次
    }
}