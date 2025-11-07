package tw.inysmp.spamute;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.Arrays;

public class SpaMuteCommand implements CommandExecutor {

    private final SpaMute plugin;

    public SpaMuteCommand(SpaMute plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 檢查權限：只有擁有 spamute.admin 權限的玩家才能使用這個指令
        if (!sender.hasPermission("spamute.admin")) {
            // 使用 plugin.getMessage() 獲取並發送無權限消息
            sender.sendMessage(plugin.getMessage("command-no-permission"));
            return true;
        }

        // 檢查參數
        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            // 發送用法錯誤消息
            sender.sendMessage(plugin.getMessage("command-usage"));
            return true;
        }

        // --- 執行重載 ---
        
        // 1. 重載 config.yml
        plugin.reloadConfig();
        
        // 2. 重載 message.yml（需要調用您在 SpaMute 主類中創建的方法）
        plugin.loadMessageFile();
        
        // 3. 重新初始化 MuteManager (如果需要重新載入持久化數據，但通常重載只需要處理配置)
        // 由於 MuteManager 的核心數據是持久化的，這裡只需重載配置，MuteManager本身無需重新實例化
        // 如果 MuteManager 中有其他配置需要重載，則需要在 MuteManager 中添加一個 reload 方法。
        // 為簡潔起見，假設只需要重載 config.yml 和 message.yml。

        // 4. 發送成功消息
        sender.sendMessage(plugin.getMessage("command-reload-success"));
        
        // 記錄到控制台
        plugin.getLogger().info("SpaMute 配置已由 " + sender.getName() + " 重載。");

        return true;
    }
}