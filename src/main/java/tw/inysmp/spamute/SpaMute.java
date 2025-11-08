package tw.inysmp.spamute;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public class SpaMute extends JavaPlugin {

    private static SpaMute instance;
    private MuteManager muteManager;

    private File messageFile;
    private FileConfiguration messageConfig; 

    private String pluginPrefix; 

    @Override
    public void onEnable() {
        instance = this;
        
        sendConsole("&a----------------------------------------");
        sendConsole("&6[&b&lSpaMute&6] &a自動防刷屏插件 &ev" + this.getDescription().getVersion() + " &a正在啟動...");
        
        this.saveDefaultConfig();
        loadMessageFile();
        
        // 2. 初始化核心管理器
        this.muteManager = new MuteManager(this);
        
        // 3. 註冊事件監聽器
        getServer().getPluginManager().registerEvents(new SpamListener(this), this);
        

        getCommand("spamute").setExecutor(new SpaMuteCommand(this));
        getCommand("unmute").setExecutor(new UnMuteCommand(this));
        getCommand("help").setExecutor(new HelpCommand(this));
        
        muteManager.startMuteCheckTask(); 
        
        sendConsole("&6[&b&lSpaMute&6] &a啟動完成！伺服器已受到保護。");
        sendConsole("&a----------------------------------------");
    }

    @Override
    public void onDisable() {
        sendConsole("&c----------------------------------------");
        sendConsole("&6[&b&lSpaMute&6] &c正在安全關閉...");
        
        if (muteManager != null) {
            muteManager.saveMuteData();
        }
        
        sendConsole("&6[&b&lSpaMute&6] &c關閉完成。");
        sendConsole("&c----------------------------------------");
    }
    
    public void loadMessageFile() {
        if (messageFile == null) {
            messageFile = new File(getDataFolder(), "message.yml");
        }
        if (!messageFile.exists()) {
            this.saveResource("message.yml", false);
        }
        
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);

        String rawPrefix = messageConfig.getString("prefix", "&6[&b&lSpaMute&6] &r");
        this.pluginPrefix = ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    public String getMessage(String path) {
        String message = messageConfig.getString(path, ChatColor.RED + "[SpaMute Error] 消息路徑錯誤: " + path);
        
        if (messageConfig.isList(path)) {
            StringBuilder sb = new StringBuilder();
            for (String line : messageConfig.getStringList(path)) {
                sb.append(processColors(line)).append("\n");
            }
            return sb.toString().trim();
        }

        return processColors(message);
    }

    private String processColors(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = message.replace("{prefix}", this.pluginPrefix);
        return message;
    }

    private void sendConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    
    public static SpaMute getInstance() {
        return instance;
    }
    
    public MuteManager getMuteManager() {
        return muteManager;
    }
    
    public String getPluginPrefix() {
        return pluginPrefix;
    }
}