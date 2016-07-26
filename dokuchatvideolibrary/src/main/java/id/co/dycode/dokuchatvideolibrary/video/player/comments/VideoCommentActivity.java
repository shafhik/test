package id.co.dycode.dokuchatvideolibrary.video.player.comments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.ListRecycleView.DividerItemDecoration;
import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.utilities.DateUtil;
import id.co.dycode.dokuchatvideolibrary.utilities.VideoCommentItem;
import id.co.dycode.dokuchatvideolibrary.video.player.VideoPlayerFragment;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoCommentActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;

    TextView screen_tittle, likers, video_title;
    ImageButton like_button, share_button, add_comment_button;
    ProgressDialog progress_dialog;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public SearchView search;
    VideoCommentRecyclerAdapter mAdapter;
    protected Handler handler;
    HttpMethodController http_controller;
    Integer limit = 15;
    Integer additional_offset = 15;
    Integer offset = limit;
    String http_result, http_result1 = "";
    public String video_id, video_name, video_like_count, current_like, like_list, video_url, youtube_id;
    public static VideoCommentActivity vid_com;

    ArrayList<VideoCommentItem> video_comment_all = new ArrayList<VideoCommentItem>();
    VideoCommentItem video_comment_item;
    VideoPlayerFragment fragment_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.Black));
        }
        setContentView(R.layout.video_comment_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.video_comment_recycler_view);

        vid_com = this;
        pref = getApplicationContext().getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();

        Bundle extras = getIntent().getExtras();
        if (!extras.equals(null)) {
            video_id = extras.getString("video_selected_id");
            video_name = extras.getString("video_selected_tittle");
            video_url = extras.getString("video_youtube_url");
            String[] url_parts = video_url.split("v=");
            if (video_url.toLowerCase().indexOf("youtu.be/") != -1){
                url_parts = video_url.split(".be/");
            } else {
                url_parts = video_url.split("v=");
            }
            url_parts[1].replace("/","");
            SetYoutubeID(url_parts[1]);
        }

        //Toast.makeText(getApplicationContext(), (pref.getString("user_id", null)), Toast.LENGTH_SHORT).show();

        screen_tittle = (TextView) findViewById(R.id.screen_tittle);
        video_title = (TextView) findViewById(R.id.tittle_Video);
        share_button = (ImageButton) findViewById(R.id.share_button);
        add_comment_button = (ImageButton) findViewById(R.id.add_comment_button);
        like_button = (ImageButton) findViewById(R.id.like_button);
        likers = (TextView) findViewById(R.id.likers);
        screen_tittle.setText("Video");

        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));
        share_button.setVisibility(View.VISIBLE);
        add_comment_button.setVisibility(View.VISIBLE);


        video_title.setText(video_name);
        likers.setText(video_like_count);


        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment_video = new VideoPlayerFragment();
        fragmentTransaction.replace(R.id.main_frag, fragment_video);
        fragmentTransaction.commit();


        String url_video = getString(R.string.API_URL_VIDEO) + "video/" + video_id;

        String url_chat_room_list = getString(R.string.API_URL_VIDEO) + "video/" + video_id + "/comments";

        HashMap<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("offset", "0");

        try {
            http_result1 = new GetVideoTask().execute(url_video).get();
            http_result = new GetListVideoCommentTask().execute(url_chat_room_list, params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (http_result != null && http_result1 != null) {

            GetlistCommentVideo(http_result);

            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new VideoCommentRecyclerAdapter(video_comment_all, mRecyclerView);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(1, 0xffa3a3a3));

            mRecyclerView.setAdapter(mAdapter);


            handler = new Handler();

            mAdapter.setOnLoadMoreListener(new VideoCommentRecyclerAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    //add progress item
                    video_comment_all.add(null);
                    mAdapter.notifyItemInserted(video_comment_all.size() - 1);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //remove progress item
                            video_comment_all.remove(video_comment_all.size() - 1);
                            mAdapter.notifyItemRemoved(video_comment_all.size());

                            String url_chat_room_list = getString(R.string.API_URL_VIDEO) + "video/" + video_id + "/comments";

                            HashMap<String, String> params = new HashMap<>();
                            params.put("limit", String.valueOf(limit));
                            params.put("offset", String.valueOf(offset));

                            try {
                                http_result = new GetListVideoCommentTask().execute(url_chat_room_list, params).get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            GetlistCommentVideo(http_result);
                            //add items one by one
                            mAdapter.notifyItemInserted(video_comment_all.size());

                            mAdapter.setLoaded();
                            //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                            offset = offset + additional_offset;
                        }
                    }, 2000);
                    System.out.println("load");
                }
            });
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(VideoCommentActivity.this)
                    .setTitle(VideoCommentActivity.this.getString(R.string.internet_connection_alert_tittle))
                    .setMessage(VideoCommentActivity.this.getString(R.string.internet_connection_alert_message))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();

            alertDialog.show();
        }

        like_button.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url_like = getString(R.string.API_URL_VIDEO) + "video/" + video_id + "/like";

                HashMap<String, String> form_body = new HashMap<>();
                form_body.put("user_id", pref.getString("user_id", null).toString());

                new TaskLikeVideo().execute(url_like, form_body);
            }
        });

        share_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
                i.putExtra(Intent.EXTRA_TEXT, video_url);
                startActivity(Intent.createChooser(i, "Share URL"));
            }
        });

        add_comment_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent post_comment = new Intent(getApplicationContext(), VideoCommentAdd.class);

                post_comment.putExtra("video_selected_id", video_id);
                post_comment.putExtra("video_selected_tittle", video_name);
                post_comment.putExtra("video_youtube_url", video_url);
                startActivity(post_comment);

                /*
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in, R.anim.animation_slide_out);
                fragment_comment = new VideoCommentAddFragment();
                fragmentTransaction.add(fragment_comment, "comment").addToBackStack("comment");
                fragmentTransaction.commitAllowingStateLoss();
                */
            }
        });
    }

    class GetVideoTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();

            http_controller = new HttpMethodController();
            String[] result = http_controller.GetMethod(url, parameter);

            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jsonObject = null;
            if (result != null) {

                try {
                    jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    video_like_count = String.valueOf(data.getInt("like_count"));
                    current_like = video_like_count;
                    likers.setText(current_like);

                    //checked if he likes he video or not
                    JSONArray likes = data.getJSONArray("like_list");
                    Integer logged_user_id = Integer.parseInt(pref.getString("user_id", null).toString());
                    for (int i = 0; i < likes.length(); i++) {
                        if (logged_user_id == likes.getInt(i)) {
                            like_button.setImageResource(R.drawable.ic_thumb_up_on);
                            likers.setTextColor(getResources().getColor(R.color.dokuColor));
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(VideoCommentActivity.this)
                        .setTitle(VideoCommentActivity.this.getString(R.string.internet_connection_alert_tittle))
                        .setMessage(VideoCommentActivity.this.getString(R.string.internet_connection_alert_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create();

                alertDialog.show();
            }
            return;
        }
    }

    class GetListVideoCommentTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];

            http_controller = new HttpMethodController();
            String[] result = http_controller.GetMethod(url, parameter);

            return result[0];
        }
    }


    class TaskLikeVideo extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();
            HashMap<String, String> form_body = (HashMap<String, String>) post_request[1];

            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);


            return result[1];

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            if (result != null) {
                Integer updated_like;
                if (result.equals("201")) {
                    like_button.setImageResource(R.drawable.ic_thumb_up_on);
                    likers.setTextColor(getResources().getColor(R.color.dokuColor));
                    updated_like = Integer.parseInt(current_like) + 1;
                    likers.setText(updated_like.toString());
                    current_like = updated_like.toString();
                } else if (result.equals("204")) {
                    like_button.setImageResource(R.drawable.ic_thumb_up_off);
                    likers.setTextColor(getResources().getColor(R.color.colorWhite));
                    updated_like = Integer.parseInt(current_like) - 1;
                    likers.setText(updated_like.toString());
                    current_like = updated_like.toString();
                }
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(VideoCommentActivity.this)
                        .setTitle(VideoCommentActivity.this.getString(R.string.internet_connection_alert_tittle))
                        .setMessage(VideoCommentActivity.this.getString(R.string.internet_connection_alert_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();

                alertDialog.show();
                return;
            }
        }
    }


    private void GetlistCommentVideo(String result){

        JSONObject jsonObject, jsonComment, jsonRoom, jsonRoom_detail = null;

        try {
            jsonObject = new JSONObject(result);
            JSONArray results =jsonObject.getJSONArray("data");

            String[] user_name = new String[results.length()];
            String[] comments = new String[results.length()];
            String[] uri = new String[results.length()];
            String[] avatar = new String[results.length()];
            String[] time = new String[results.length()];
            String[] timeZone = new String[result.length()];
            Integer[] flag = new Integer[results.length()];
            JSONArray[] flag_list = new JSONArray[results.length()];
            JSONObject result_detail;

            for (int i = 0; i < results.length(); i++){
                result_detail = results.getJSONObject(i);
                JSONObject user_object = result_detail.getJSONObject("user");
                JSONObject time_object = result_detail.getJSONObject("created_at");
                JSONObject user_data = user_object.getJSONObject("data");
                user_name[i] = user_data.getString("username");
                comments[i] = result_detail.getString("comments");
                uri[i] = result_detail.getString("uri");
                avatar[i] = user_data.getString("avatar");
                time[i] = time_object.getString("date");
                timeZone[i] = time_object.getString("timezone");

                flag[i] = result_detail.getInt("flag");
                flag_list[i] = result_detail.getJSONArray("flag_list");
                video_comment_item = new VideoCommentItem();
                video_comment_item.setUserName(user_name[i]);
                video_comment_item.setComments(comments[i]);
                video_comment_item.setUri(uri[i]);
                video_comment_item.setAvatar(avatar[i]);
                video_comment_item.setTime(DateUtil.getDifferentStringValue(DateUtil.getDifference(time[i], timeZone[i])));
                video_comment_item.setFlag(flag[i]);
                video_comment_item.setFlagList(flag_list[i]);
                video_comment_all.add(video_comment_item);
            }



            //Toast.makeText(getApplicationContext(), results.toString(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            video_comment_item = new VideoCommentItem();
            video_comment_item.setUserName("0");
            video_comment_item.setComments("0");
            video_comment_item.setUri("0");
            video_comment_item.setAvatar("0");
            video_comment_item.setTime("0");
            video_comment_item.setFlag(0);
            video_comment_item.setFlagList(null);
            video_comment_all.add(video_comment_item);

            //Toast.makeText(getApplicationContext(), "gagal", Toast.LENGTH_SHORT).show();
        }

    }

    public void SetYoutubeID(String youtube_id) {
        this.youtube_id = youtube_id;
    }

    public String GetYoutubeID() {
        return youtube_id;
    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*
        if(fragment_comment.isAdded()){
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment_comment);
        } else {
            this.overridePendingTransition(R.anim.animation_slide_up,
                    R.anim.animation_slide_down);
        }
        */
    }

}
