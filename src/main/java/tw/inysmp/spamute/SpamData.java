package tw.inysmp.spamute;

/**
 * 用於追蹤單個玩家的刷屏活動的數據模型。
 * 這個數據會儲存在 SpaMute 插件的內存中，並在伺服器關閉時清除。
 */
public class SpamData {

    // 玩家在偵測窗口內發送的消息計數
    private int messageCount;
    
    // 玩家上次發送消息的時間戳 (毫秒)
    private long lastMessageTime;
    
    // 玩家累積的刷屏警告次數。達到閾值將被禁言。
    private int spamWarningCount; 

    public SpamData() {
        this.messageCount = 0;
        this.lastMessageTime = System.currentTimeMillis();
        this.spamWarningCount = 0;
    }

    // --- Getters ---

    public int getMessageCount() {
        return messageCount;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public int getSpamWarningCount() {
        return spamWarningCount;
    }

    // --- Setters and Modifiers ---
    
    /**
     * 重置消息計數和時間戳，通常在超過偵測窗口時調用。
     */
    public void resetMessageCount(long currentTime) {
        this.messageCount = 1; // 設置為 1，因為當前這次發言就是新的第一條
        this.lastMessageTime = currentTime;
    }

    /**
     * 在偵測窗口內，增加消息計數。
     * @param currentTime 當前時間戳
     */
    public void incrementMessageCount(long currentTime) {
        this.messageCount++;
        this.lastMessageTime = currentTime; // 更新時間戳為最新
    }

    /**
     * 增加一次刷屏警告計數。
     */
    public void incrementWarningCount() {
        this.spamWarningCount++;
    }

    /**
     * 達到禁言閾值後，將警告計數清零 (取決於 config.yml 的設定)。
     */
    public void resetWarningCount() {
        this.spamWarningCount = 0;
    }
}