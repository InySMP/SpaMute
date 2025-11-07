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
        // 核心檢查權限
        if (!sender.hasPermission("spamute.admin")) {
            sender.sendMessage(plugin.getMessage("command-no-permission"));
            return true;
        }

        // 檢查參數數量
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessage("unmute-usage"));
            return true;
        }

        String targetName = args[0];
        
        // 獲取目標玩家的 UUID
        @SuppressWarnings("deprecation")
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
        
        // 檢查玩家是否存在（如果Bukkit無法找到UUID，它可能會返回一個帶有00000...UUID的OfflinePlayer）
        if (targetPlayer == null || targetPlayer.getUniqueId() == null) {
            sender.sendMessage(plugin.getMessage("unmute-player-not-found").replace("{player}", targetName));
            return true;
        }
    
        UUID targetUUID = targetPlayer.getUniqueId();
        MuteManager muteManager = plugin.getMuteManager();
        
        // 檢查玩家是否在禁言列表中
        if (!muteManager.isMuted(targetUUID)) {
            sender.sendMessage(plugin.getMessage("unmute-not-muted").replace("{player}", targetName));
            return true;
        }

        // 執行強制解除禁言 (使用 MuteManager.forceUnmutePlayer)
        muteManager.forceUnmutePlayer(targetUUID);
        
        // 發送成功消息給執行者
        sender.sendMessage(plugin.getMessage("unmute-success-sender").replace("{player}", targetName));
        
        // 通知所有管理員
        plugin.getServer().broadcast(
            plugin.getMessage("unmute-notify-admin")
                .replace("{player}", targetName)
                .replace("{sender}", sender.getName()), 
            "spamute.admin"
        );
        
        return true;
    }
}