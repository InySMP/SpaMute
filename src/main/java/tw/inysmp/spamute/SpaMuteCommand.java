package tw.inysmp.spamute;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SpaMuteCommand implements CommandExecutor {

    private final SpaMute plugin;

    public SpaMuteCommand(SpaMute plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 核心檢查權限
        if (!sender.hasPermission("spamute.admin")) {
            sender.sendMessage(plugin.getMessage("command-no-permission"));
            return true;
        }

        // --- 處理 /spamute 指令 ---
        // 此處只處理 /spamute reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadMessageFile();
            sender.sendMessage(plugin.getMessage("command-reload-success"));
            plugin.getLogger().info("SpaMute 配置已由 " + sender.getName() + " 重載。");
            return true;
        } else {
            // 顯示 /spamute 的用法
            sender.sendMessage(plugin.getMessage("command-usage")); 
            return true;
        }
    }
}