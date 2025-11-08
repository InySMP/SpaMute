package tw.inysmp.spamute;

public class SpamData {

    private int messageCount;
    
    private long lastMessageTime;
    
    private int spamWarningCount; 

    public SpamData() {
        this.messageCount = 0;
        this.lastMessageTime = System.currentTimeMillis();
        this.spamWarningCount = 0;
    }


    public int getMessageCount() {
        return messageCount;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public int getSpamWarningCount() {
        return spamWarningCount;
    }


    public void resetMessageCount(long currentTime) {
        this.messageCount = 1;
        this.lastMessageTime = currentTime;
    }

    /**
     * @param currentTime
     */
    public void incrementMessageCount(long currentTime) {
        this.messageCount++;
        this.lastMessageTime = currentTime; // 更新時間戳為最新
    }


    public void incrementWarningCount() {
        this.spamWarningCount++;
    }

    public void resetWarningCount() {
        this.spamWarningCount = 0;
    }
}