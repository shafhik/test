package id.co.dycode.dokuchatvideolibrary.utilities;

import org.json.JSONArray;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoItem {

    private String id = "";
    private String title = "";
    private String description = "";
    private String url = "";
    private String youtube_id = "";
    private String like_count = "";
    private JSONArray like_list;

    public VideoItem() {
    }

    public VideoItem(String id, String title, String description, String url, String youtube_id, String like_count, JSONArray like_list) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.url = url;
        this.youtube_id = youtube_id;
        this.like_count = like_count;
        this.like_list = like_list;
    }


    public void SetId(String id) {
        this.id = id;
    }

    public void SetTittle(String title) {
        this.title = title;
    }

    public void SetDescription(String description) {
        this.description = description;
    }

    public void SetUrl(String url) {
        this.url = url;
    }

    public void SetYoutubeId(String youtube_id) {
        this.youtube_id = youtube_id;
    }

    public void SetLikeCount(String like_count) {
        this.like_count = like_count;
    }

    public void SetLikeList(JSONArray like_list) {
        this.like_list = like_list;
    }

    public String GetId() {
        return id;
    }

    public String GetTitle() {
        return title;
    }

    public String GetDescription() {
        return description;
    }

    public String GetUrl() {
        return url;
    }

    public String GetYoutubeId() {
        return youtube_id;
    }

    public String GetLikeCount() {
        return like_count;
    }

    public JSONArray GetLikeList() {
        return like_list;
    }


}