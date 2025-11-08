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

        if (player.hasPermission("spamute.bypass")) {
            return;
        }

        if (muteManager.isMuted(playerId)) {
            event.setCancelled(true);
            String remainingTime = muteManager.getRemainingMuteTimeFormatted(playerId); 
            player.sendMessage(plugin.getMessage("player-try-speak").replace("{remaining}", remainingTime));
            return;
        }

        SpamData data = spamTracker.getOrDefault(playerId, new SpamData());
        
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - data.getLastMessageTime();

        int timeWindowSeconds = config.getInt("spam-detection.time-window-seconds");
        int maxMessages = config.getInt("spam-detection.max-messages");
        int muteThreshold = config.getInt("spam-detection.mute-threshold");
        long muteDurationSeconds = config.getLong("mute.duration-seconds");

        if (timeElapsed > (long) timeWindowSeconds * 1000) {
            data.resetMessageCount(currentTime);
        } else {
            data.incrementMessageCount(currentTime);

            if (data.getMessageCount() > maxMessages) {
                event.setCancelled(true);

                data.incrementWarningCount(); 

                player.sendMessage(plugin.getMessage("player-warning")
                    .replace("{count}", String.valueOf(data.getSpamWarningCount()))
                    .replace("{threshold}", String.valueOf(muteThreshold)));

                if (data.getSpamWarningCount() >= muteThreshold) {

                    muteManager.mutePlayer(playerId, muteDurationSeconds);

                    String durationFormatted = muteManager.formatDuration(muteDurationSeconds);
                    player.sendMessage(plugin.getMessage("player-muted").replace("{duration}", durationFormatted)
                        .replace("{unmute_time}", muteManager.getUnmuteTimeFormatted(playerId)));
                    plugin.getServer().broadcast(plugin.getMessage("admin-notify-mute").replace("{player}", player.getName())
                        .replace("{duration}", durationFormatted), "spamute.admin");

                    if (config.getBoolean("mute.reset-count-on-mute")) {
                         data.resetWarningCount();
                    }
                }

                data.resetMessageCount(currentTime); 
            }
        }

        spamTracker.put(playerId, data);
    }
}