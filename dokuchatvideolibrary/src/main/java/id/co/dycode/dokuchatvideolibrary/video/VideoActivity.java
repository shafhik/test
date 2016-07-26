package id.co.dycode.dokuchatvideolibrary.video;


import android.app.ProgressDialog;
import android.content.Context;
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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.ListRecycleView.DividerItemDecoration;
import id.co.dycode.dokuchatvideolibrary.ListRecycleView.RecyclerItemClickListener;
import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.utilities.VideoItem;
import id.co.dycode.dokuchatvideolibrary.video.player.comments.VideoCommentActivity;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoActivity extends AppCompatActivity {

    TextView textTitle;
    View viewExtendToolbar;
    ProgressDialog progressDialog;
    RelativeLayout containerEmpty;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    VideoRecyclerViewAdapter mAdapter;
    protected Handler handler;
    HttpMethodController httpController;
    Integer limit = 15;
    Integer offset = limit;
    Integer offsetAdditional = 15;
    String httpRes2, httpResult = "";

    ArrayList<VideoItem> listVideo = new ArrayList<VideoItem>();
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.Black));
        }
        setContentView(R.layout.video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.video_recycler_view);

        pref = getApplicationContext().getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();

        textTitle = (TextView) findViewById(R.id.screen_tittle);
        viewExtendToolbar = (View) findViewById(R.id.extend_toolbar);
        containerEmpty = (RelativeLayout) findViewById(R.id.container_video_empty);
        textTitle.setText("Video");

        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));
        viewExtendToolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));


        String url_check_register = getString(R.string.API_URL_VIDEO) + "commenter";

        HashMap<String, String> form_body = new HashMap<>();
        form_body.put("email", pref.getString("user_email", null).toString());
        form_body.put("username", pref.getString("user_name", null).toString());
        form_body.put("avatar", pref.getString("user_avatar", null).toString());

        String url_video_list = getString(R.string.API_URL_VIDEO) + "video";

        HashMap<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("offset", "0");
        params.put("search", pref.getString("co_brand_id", null).toString());

        try {
            httpRes2 = new CheckAutoRegisterTask(VideoActivity.this).execute(url_check_register, form_body).get();
            httpResult = new GetListVideoTask(VideoActivity.this).execute(url_video_list, params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (httpResult != null && httpRes2 != null) {

            parseListVideo(httpResult);

            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new VideoRecyclerViewAdapter(listVideo, mRecyclerView);

            mRecyclerView.addItemDecoration(new DividerItemDecoration(2, 0xffeaeaea));

            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            String video_selected_id = listVideo.get(position).GetId();
                            String video_selected_tittle = listVideo.get(position).GetTitle();
                            String video_selected_like_count = listVideo.get(position).GetLikeCount();
                            JSONArray video_youtube_like_list = listVideo.get(position).GetLikeList();
                            String video_youtube_url = listVideo.get(position).GetUrl();

                            Intent intent = new Intent(getApplicationContext(), VideoCommentActivity.class);
                            intent.putExtra("video_selected_id", video_selected_id);
                            intent.putExtra("video_selected_tittle", video_selected_tittle);
                            intent.putExtra("video_selected_like_count", video_selected_like_count);
                            intent.putExtra("video_youtube_url", video_youtube_url);
                            intent.putExtra("video_selected_like_list", video_youtube_like_list.toString());

                            startActivity(intent);
                            overridePendingTransition(R.anim.animation_slide_in, R.anim.animation_slide_out);

                        }
                    })
            );


            handler = new Handler();

            mAdapter.setOnLoadMoreListener(new VideoRecyclerViewAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    //add progress item
                    listVideo.add(null);
                    mAdapter.notifyItemInserted(listVideo.size() - 1);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //remove progress item
                            listVideo.remove(listVideo.size() - 1);
                            mAdapter.notifyItemRemoved(listVideo.size());

                            String url_chat_room_list = getString(R.string.API_URL_VIDEO) + "video";

                            HashMap<String, String> params = new HashMap<>();
                            params.put("limit", String.valueOf(limit));
                            params.put("offset", String.valueOf(offset));
                            params.put("search", pref.getString("co_brand_id", null).toString());

                            try {
                                httpResult = new GetListVideoTask(VideoActivity.this).execute(url_chat_room_list, params).get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            parseListVideo(httpResult);
                            //add items one by one
                            mAdapter.notifyItemInserted(listVideo.size());

                            mAdapter.setLoaded();
                            //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                            offset = offset + offsetAdditional;
                        }
                    }, 2000);
                    System.out.println("load");
                }
            });

        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(VideoActivity.this)
                    .setTitle(VideoActivity.this.getString(R.string.internet_connection_alert_tittle))
                    .setMessage(VideoActivity.this.getString(R.string.internet_connection_alert_message))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();

            alertDialog.show();
            progressDialog.dismiss();
        }


    }

    class GetListVideoTask extends AsyncTask<Object, String, String> {

        Context digital_context;

        GetListVideoTask(Context context) {
            this.digital_context = context;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];

            httpController = new HttpMethodController();
            String[] result = httpController.GetMethod(url, parameter);


            return result[0];
        }

    }

    class CheckAutoRegisterTask extends AsyncTask<Object, String, String> {
        Context digital_context;

        CheckAutoRegisterTask(Context context) {
            this.digital_context = context;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(digital_context);
            progressDialog.setMessage(digital_context.getString(R.string.internet_connection_waiting));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();
            HashMap<String, String> form = (HashMap<String, String>) post_request[1];

            httpController = new HttpMethodController();
            String[] result = httpController.PostMethod(url, parameter, form);

            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jsonObject = null;
            Integer id = null;

            if (result != null) {
                try {
                    jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");
                    id = data.getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                editor.putString("user_id", String.valueOf(id));
                editor.commit();
            } else {

                AlertDialog alertDialog = new AlertDialog.Builder(VideoActivity.this)
                        .setTitle(VideoActivity.this.getString(R.string.internet_connection_alert_tittle))
                        .setMessage(VideoActivity.this.getString(R.string.internet_connection_alert_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create();

                alertDialog.show();
                progressDialog.dismiss();
            }
            return;
        }
    }


    private void parseListVideo(String result) {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(result);
            JSONArray results = jsonObject.getJSONArray("data");

            String[] video_id = new String[results.length()];
            String[] video_title = new String[results.length()];
            String[] video_description = new String[results.length()];
            String[] video_url = new String[results.length()];
            String[] youtube_id = new String[results.length()];
            String[] video_like_count = new String[results.length()];
            JSONArray[] video_like_list = new JSONArray[results.length()];
            JSONObject result_detail;


            for (int i = 0; i < results.length(); i++) {
                result_detail = results.getJSONObject(i);
                video_url[i] = result_detail.getString("url");
                String[] url_parts;
                if (video_url[i].toLowerCase().indexOf("youtu.be/") != -1){
                    url_parts = video_url[i].split(".be/");
                } else {
                    url_parts = video_url[i].split("v=");
                }
                url_parts[1].replace("/","");
                if (!url_parts[0].equals(video_url[i])) {
                    video_id[i] = result_detail.getString("id");
                    video_title[i] = result_detail.getString("title");
                    video_description[i] = result_detail.getString("description");
                    youtube_id[i] = url_parts[1];
                    video_like_count[i] = result_detail.getString("like_count");
                    video_like_list[i] = result_detail.getJSONArray("like_list");

                    VideoItem videoItem = new VideoItem();
                    videoItem.SetId(video_id[i]);
                    videoItem.SetTittle(video_title[i]);
                    videoItem.SetDescription(video_description[i]);
                    videoItem.SetUrl(video_url[i]);
                    videoItem.SetYoutubeId(youtube_id[i]);
                    videoItem.SetLikeCount(video_like_count[i]);
                    videoItem.SetLikeList(video_like_list[i]);
                    listVideo.add(videoItem);
                    if (listVideo.size() == 0) {
                        containerEmpty.setVisibility(View.VISIBLE);
                    } else {
                        containerEmpty.setVisibility(View.GONE);
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
            containerEmpty.setVisibility(View.VISIBLE);
        }
    }

}
