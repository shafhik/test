package id.co.dycode.dokuchatvideolibrary.utilities;

/**
 * Created by 1 on 7/5/2016.
 */
public class ChatItem {
    private String name = "";
    private String last_chat = "";
    private String avatar = "";
    private String last_comment_id = "";
    private String last_comment_topic_id = "";
    private String room_id = "";
    private String last_person = "";
    private String last_time = "";
    private String code_en = "";
    private String notif_count = "";

    public ChatItem() {
    }

    public ChatItem(String name, String last_chat, String avatar, String last_comment_id, String last_comment_topic_id,
                    String room_id, String last_person, String last_time, String code_en, String notif_count) {
        this.name = name;
        this.last_chat = last_chat;
        this.avatar = avatar;
        this.last_comment_id = last_comment_id;
        this.last_comment_topic_id = last_comment_topic_id;
        this.room_id = room_id;
        this.last_person = last_person;
        this.last_time = last_time;
        this.code_en = code_en;
        this.notif_count = notif_count;
    }

    public void SetLastChat(String last_chat) {
        this.last_chat = last_chat;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void SetLastCommentId(String last_comment_id) {
        this.last_comment_id = last_comment_id;
    }

    public void SetLastTopicId(String last_comment_topic_id) {
        this.last_comment_topic_id = last_comment_topic_id;
    }

    public void SetLastPerson(String last_person) {
        this.last_person = last_person;
    }

    public void SetLastTime(String last_time) {
        this.last_time = last_time;
    }

    public void SetRoomId(String room_id) {
        this.room_id = room_id;
    }

    public void SetCodeEn(String code_en) {
        this.code_en = code_en;
    }

    public void SetNotifCount(String notif_count) {
        this.notif_count = notif_count;
    }


    public String GetName() {
        return name;
    }

    public String getLastChat() {
        return last_chat;
    }

    public String GetAvatar() {
        return avatar;
    }

    public String GetLastCommentId() {
        return last_comment_id;
    }

    public String GetLastTopicId() {
        return last_comment_topic_id;
    }

    public String GetLastPerson() {
        return last_person;
    }

    public String GetLastTime() {
        return last_time;
    }

    public String GetRoomId() {
        return room_id;
    }

    public String GetCodeEn() {
        return code_en;
    }

    public String GetNotifCount() {
        return notif_count;
    }
}