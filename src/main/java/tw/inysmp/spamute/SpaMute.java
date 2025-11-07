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
    
    // 專門用於 message.yml 的配置
    private File messageFile;
    private FileConfiguration messageConfig; 
    
    // 儲存 message.yml 中的前綴 (已轉換顏色)
    private String pluginPrefix; 

    @Override
    public void onEnable() {
        instance = this;
        
        sendConsole("&a----------------------------------------");
        sendConsole("&6[&b&lSpaMute&6] &a自動防刷屏插件 &ev" + this.getDescription().getVersion() + " &a正在啟動...");
        
        // 1. 載入主配置與訊息文件
        this.saveDefaultConfig(); // config.yml
        loadMessageFile();       // message.yml
        
        // 2. 初始化核心管理器
        this.muteManager = new MuteManager(this); // MuteManager 會自行處理 mutes.yml
        
        // 3. 註冊事件監聽器
        getServer().getPluginManager().registerEvents(new SpamListener(this), this);
        
        // 4. 註冊指令執行器
        getCommand("spamute").setExecutor(new SpaMuteCommand(this));
        
        // 5. 啟動自動解除禁言的檢查任務
        muteManager.startMuteCheckTask(); 
        
        sendConsole("&6[&b&lSpaMute&6] &a啟動完成！伺服器已受到保護。");
        sendConsole("&a----------------------------------------");
    }

    @Override
    public void onDisable() {
        sendConsole("&c----------------------------------------");
        sendConsole("&6[&b&lSpaMute&6] &c正在安全關閉...");
        
        if (muteManager != null) {
            muteManager.saveMuteData(); // 儲存所有未到期的禁言
        }
        
        sendConsole("&6[&b&lSpaMute&6] &c關閉完成。");
        sendConsole("&c----------------------------------------");
    }
    
    // --- 消息和配置處理方法 ---

    /**
     * 載入/重載 message.yml 文件並設置前綴。
     */
    public void loadMessageFile() {
        if (messageFile == null) {
            messageFile = new File(getDataFolder(), "message.yml");
        }
        if (!messageFile.exists()) {
            this.saveResource("message.yml", false);
        }
        
        // 載入配置
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        
        // 設置插件前綴
        String rawPrefix = messageConfig.getString("prefix", "&6[&b&lSpaMute&6] &r");
        this.pluginPrefix = ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }
    
    /**
     * 從 message.yml 獲取消息，並處理顏色代碼和前綴替換。
     * 這是供 SpamListener 和 MuteManager 調用的核心方法。
     */
    public String getMessage(String path) {
        String message = messageConfig.getString(path, ChatColor.RED + "[SpaMute Error] 消息路徑錯誤: " + path);
        
        // 處理列表 (多行消息)
        if (messageConfig.isList(path)) {
            StringBuilder sb = new StringBuilder();
            for (String line : messageConfig.getStringList(path)) {
                sb.append(processColors(line)).append("\n");
            }
            return sb.toString().trim(); // 移除最後一個換行符
        }

        return processColors(message);
    }
    
    /**
     * 替換顏色代碼和前綴佔位符的輔助方法。
     */
    private String processColors(String message) {
        // 1. 替換顏色代碼 (& -> §)
        message = ChatColor.translateAlternateColorCodes('&', message);
        // 2. 替換 {prefix} 佔位符
        message = message.replace("{prefix}", this.pluginPrefix);
        return message;
    }
    
    /**
     * 讓控制台消息帶有顏色。
     */
    private void sendConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    // --- Getters ---
    
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