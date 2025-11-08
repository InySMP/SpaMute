package tw.inysmp.spamute;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.UUID;

public class UnMuteCommand implements CommandExecutor {

    private final SpaMute plugin;

    public UnMuteCommand(SpaMute plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("spamute.admin")) {
            sender.sendMessage(plugin.getMessage("command-no-permission"));
            return true;
        }


        if (args.length < 1) {
            sender.sendMessage(plugin.getMessage("unmute-usage"));
            return true;
        }

        String targetName = args[0];
        
        @SuppressWarnings("deprecation")
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);

        if (targetPlayer == null || targetPlayer.getUniqueId() == null) {
            sender.sendMessage(plugin.getMessage("unmute-player-not-found").replace("{player}", targetName));
            return true;
        }
    
        UUID targetUUID = targetPlayer.getUniqueId();
        MuteManager muteManager = plugin.getMuteManager();

        if (!muteManager.isMuted(targetUUID)) {
            sender.sendMessage(plugin.getMessage("unmute-not-muted").replace("{player}", targetName));
            return true;
        }

        muteManager.forceUnmutePlayer(targetUUID);

        sender.sendMessage(plugin.getMessage("unmute-success-sender").replace("{player}", targetName));

        plugin.getServer().broadcast(
            plugin.getMessage("unmute-notify-admin")
                .replace("{player}", targetName)
                .replace("{sender}", sender.getName()), 
            "spamute.admin"
        );
        
        return true;
    }
}