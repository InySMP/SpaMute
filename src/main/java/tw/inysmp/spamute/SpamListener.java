package tw.inysmp.spamute;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpamListener implements Listener {

    private final SpaMute plugin;
    private final MuteManager muteManager;
    private final FileConfiguration config;
    
    // 儲存玩家的刷屏偵測數據：UUID -> SpamData 物件
    // 這是您的 SpamData.java 類
    private final Map<UUID, SpamData> spamTracker = new HashMap<>();

    public SpamListener(SpaMute plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.muteManager = plugin.getMuteManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // 1. 檢查權限繞過
        if (player.hasPermission("spamute.bypass")) {
            return;
        }

        // 2. 檢查是否已被禁言
        if (muteManager.isMuted(playerId)) {
            event.setCancelled(true);
            String remainingTime = muteManager.getRemainingMuteTimeFormatted(playerId); 
            // 這裡使用 plugin.getMessage()
            player.sendMessage(plugin.getMessage("player-try-speak").replace("{remaining}", remainingTime));
            return;
        }

        // 3. 獲取或初始化玩家的刷屏數據
        // 如果不存在，則創建一個新的 SpamData 實例
        SpamData data = spamTracker.getOrDefault(playerId, new SpamData());
        
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - data.getLastMessageTime();
        
        // 取得配置值
        int timeWindowSeconds = config.getInt("spam-detection.time-window-seconds");
        int maxMessages = config.getInt("spam-detection.max-messages");
        int muteThreshold = config.getInt("spam-detection.mute-threshold");
        long muteDurationSeconds = config.getLong("mute.duration-seconds");
        
        // --- 核心刷屏邏輯 ---
        
        // A. 如果距離上次發言超過偵測窗口時間，則重置計數
        if (timeElapsed > (long) timeWindowSeconds * 1000) {
            data.resetMessageCount(currentTime); // 這次發言是新的第一條
        } else {
            // B. 在偵測窗口內，增加消息計數
            data.incrementMessageCount(currentTime);

            // 檢查是否超過最大消息數 (刷屏行為發生)
            if (data.getMessageCount() > maxMessages) {
                // 1. 刷屏行為發生，取消本次發言
                event.setCancelled(true);
                
                // 2. 增加刷屏警告計數
                data.incrementWarningCount(); 
                
                // 3. 發送警告訊息給玩家
                player.sendMessage(plugin.getMessage("player-warning")
                    .replace("{count}", String.valueOf(data.getSpamWarningCount()))
                    .replace("{threshold}", String.valueOf(muteThreshold)));

                // 4. 檢查是否達到禁言閾值 (4次刷屏觸發禁言)
                if (data.getSpamWarningCount() >= muteThreshold) {
                    
                    // 達到閾值，執行禁言
                    muteManager.mutePlayer(playerId, muteDurationSeconds);
                    
                    // 發送禁言通知
                    String durationFormatted = muteManager.formatDuration(muteDurationSeconds);
                    player.sendMessage(plugin.getMessage("player-muted").replace("{duration}", durationFormatted)
                        .replace("{unmute_time}", muteManager.getUnmuteTimeFormatted(playerId)));
                    plugin.getServer().broadcast(plugin.getMessage("admin-notify-mute").replace("{player}", player.getName())
                        .replace("{duration}", durationFormatted), "spamute.admin");
                    
                    // 根據配置重置警告計數
                    if (config.getBoolean("mute.reset-count-on-mute")) {
                         data.resetWarningCount();
                    }
                }
                
                // 5. 無論是否禁言，都需要將當前消息計數重置為 0 (避免連續刷屏懲罰)
                data.resetMessageCount(currentTime); 
            }
        }
        
        // 4. 儲存更新後的數據
        spamTracker.put(playerId, data);
    }
}