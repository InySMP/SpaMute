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
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private final File muteFile;
    private FileConfiguration muteConfig;
    private BukkitTask checkTask;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public MuteManager(SpaMute plugin) {
        this.plugin = plugin;

        File logFolder = new File(plugin.getDataFolder(), "log");

        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                plugin.getLogger().info("已創建 /log/ 資料夾用於儲存禁言記錄。");
            } else {
                plugin.getLogger().severe("無法創建 /log/ 資料夾！請檢查權限。");
            }
        }

        this.muteFile = new File(logFolder, "mutes.yml");

        loadMuteData();
    }

    private void loadMuteData() {
        if (!muteFile.exists()) {
            try {
                muteFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("無法創建 mutes.yml 文件於 log 資料夾內！ " + e.getMessage());
            }
        }
        muteConfig = YamlConfiguration.loadConfiguration(muteFile);

        for (String key : muteConfig.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                long unmuteTime = muteConfig.getLong(key);
                
                if (unmuteTime > System.currentTimeMillis()) {
                    mutedPlayers.put(playerId, unmuteTime);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("mutes.yml 中發現無效的 UUID: " + key);
            }
        }
    }

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
     * @param playerId
     * @param durationSeconds
     */
    public void mutePlayer(UUID playerId, long durationSeconds) {
        long unmuteTime = System.currentTimeMillis() + durationSeconds * 1000L;
        mutedPlayers.put(playerId, unmuteTime);
        saveMuteData(); 
    }

    /**
     * @param playerId 
     * @return 
     */
    public boolean isMuted(UUID playerId) {
        if (!mutedPlayers.containsKey(playerId)) {
            return false;
        }

        long unmuteTime = mutedPlayers.get(playerId);
        if (unmuteTime <= System.currentTimeMillis()) {
            unmutePlayer(playerId);
            return false;
        }
        return true;
    }

    /**
     * @param playerId
     */
    public void forceUnmutePlayer(UUID playerId) {
        if (mutedPlayers.containsKey(playerId)) {
            mutedPlayers.remove(playerId);

            if (Bukkit.getPlayer(playerId) != null && Bukkit.getPlayer(playerId).isOnline()) {
                 Bukkit.getPlayer(playerId).sendMessage(plugin.getMessage("unmute-success-target"));
            }
            
            saveMuteData(); 
        }
    }

    /**
     * @param playerId
     */
    private void unmutePlayer(UUID playerId) {
        if (mutedPlayers.containsKey(playerId)) {
            mutedPlayers.remove(playerId);

            if (Bukkit.getPlayer(playerId) != null && Bukkit.getPlayer(playerId).isOnline()) {
                 Bukkit.getPlayer(playerId).sendMessage(plugin.getMessage("player-unmuted-auto"));
            }

            plugin.getServer().broadcast(plugin.getPluginPrefix() + ChatColor.GREEN + 
                Bukkit.getOfflinePlayer(playerId).getName() + " 的禁言已自動解除。", "spamute.admin");
            
            saveMuteData(); 
        }
    }

    public String getUnmuteTimeFormatted(UUID playerId) {
        if (!mutedPlayers.containsKey(playerId)) return "N/A";
        
        long unmuteTime = mutedPlayers.get(playerId);
        Instant instant = Instant.ofEpochMilli(unmuteTime);
        return DATE_FORMATTER.format(instant);
    }

    public String getRemainingMuteTimeFormatted(UUID playerId) {
        if (!mutedPlayers.containsKey(playerId)) return "0秒";
        
        long remainingMillis = mutedPlayers.get(playerId) - System.currentTimeMillis();

        return getRemainingTimeFormatted(remainingMillis);
    }
    
    /**
     * @param durationSeconds
     * @return
     */
    public String formatDuration(long durationSeconds) {
        long totalMillis = durationSeconds * 1000L;
        
        return getRemainingTimeFormatted(totalMillis);
    }

    private String getRemainingTimeFormatted(long remainingMillis) {
        if (remainingMillis <= 0) return "0秒";

        long days = TimeUnit.MILLISECONDS.toDays(remainingMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小時");
        if (minutes > 0 && days == 0) sb.append(minutes).append("分"); 
        
        if (sb.length() == 0 || (days == 0 && hours > 0 && minutes == 0)) {
             sb.append(seconds).append("秒"); 
        } else if (sb.length() == 0) {
             sb.append(seconds).append("秒");
        }


        return sb.toString();
    }


    public void startMuteCheckTask() {
        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel();
        }
        
        checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            mutedPlayers.entrySet().removeIf(entry -> {
                long unmuteTime = entry.getValue();
                
                if (unmuteTime <= System.currentTimeMillis()) {
                    UUID playerId = entry.getKey();
                    Bukkit.getScheduler().runTask(plugin, () -> unmutePlayer(playerId));
                    return true;
                }
                return false;
            });
        }, 20 * 20L, 20 * 20L);
    }
}