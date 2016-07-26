package id.co.dycode.dokuchatvideolibrary.utilities;

import org.json.JSONArray;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoCommentItem {

    private String userName = "";
    private String comments = "";
    private String uri = "";
    private String avatar = "";
    private String time = "";
    private Integer flag;
    private JSONArray flagList;

    public VideoCommentItem() {
    }

    public VideoCommentItem(String userName, String comments, String uri, String avatar, String time, Integer flag, JSONArray flagList) {
        this.userName = userName;
        this.comments = comments;
        this.uri = uri;
        this.avatar = avatar;
        this.time = time;
        this.flag = flag;
        this.flagList = flagList;
    }


    public void setUserName(String user_name) {
        this.userName = user_name;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public void setFlagList(JSONArray flag_list) {
        this.flagList = flag_list;
    }


    public String getUserName() {
        return userName;
    }

    public String getComments() {
        return comments;
    }

    public String getUri() {
        return uri;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getTime() {
        return time;
    }

    public Integer getFlag() {
        return flag;
    }

    public JSONArray getFlagList() {
        return flagList;
    }
}