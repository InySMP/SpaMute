package tw.inysmp.spamute;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

    private final SpaMute plugin;

    public HelpCommand(SpaMute plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spamute.admin")) {
            sender.sendMessage(plugin.getMessage("command-no-permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(plugin.getMessage("command-usage"));
            return true;
        } else {
            sender.sendMessage(plugin.getMessage("command-usage")); 
            return true;
        }
    }
}