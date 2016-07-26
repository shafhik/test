package id.co.dycode.dokuchatvideolibrary.utilities;

/**
 * Created by 1 on 7/5/2016.
 */
public class MessageItem {
    private String messageId = "";
    private String message = "";
    private String usernameAs = "";
    private String usernameReal = "";
    private String avatar = "";
    private String time = "";
    private int dayOfYear;

    //message as day separator, default false
    private boolean isSeparator = false;


    public MessageItem() {
    }

    public MessageItem(String messageId, String message, String usernameAs, String usernameReal,
                       String avatar, String time) {
        this.messageId = messageId;
        this.message = message;
        this.usernameAs = usernameAs;
        this.usernameReal = usernameReal;
        this.avatar = avatar;
        this.time = time;
    }

    public void setMessageId(String message_id) {
        this.messageId = message_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUsernameAs(String username_as) {
        this.usernameAs = username_as;
    }

    public void setUsernameReal(String username_real) {
        this.usernameReal = username_real;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDayOfYear() {
        return dayOfYear;
    }

    public boolean isSeparator() {
        return isSeparator;
    }

    public void setSeparator(boolean separator) {
        isSeparator = separator;
    }

    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public String getUsernameAs() {
        return usernameAs;
    }

    public String GetUsernameReal() {
        return usernameReal;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getTime() {
        return time;
    }
}