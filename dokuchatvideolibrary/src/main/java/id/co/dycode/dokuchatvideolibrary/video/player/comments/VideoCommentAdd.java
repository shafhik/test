package id.co.dycode.dokuchatvideolibrary.video.player.comments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.R;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoCommentAdd extends Activity {

    String comment;
    String video_id, video_name, video_url;

    TextView screen_tittle;
    ImageButton check_button;
    EditText edit_video_comment;
    Toolbar toolbar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    URL url;
    HttpMethodController http_controller;
    ProgressDialog progress_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.Black));
        }
        setContentView(R.layout.video_comment_add);


        Bundle extras = getIntent().getExtras();
        if (!extras.equals(null)) {
            video_id = extras.getString("video_selected_id");
            video_name = extras.getString("video_selected_tittle");
            video_url = extras.getString("video_youtube_url");
        }

        pref = getApplicationContext().getSharedPreferences("doku_user_prefence", 0); // 0 - for private mode
        editor = pref.edit();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
        screen_tittle = (TextView) findViewById(R.id.screen_tittle);
        check_button = (ImageButton) findViewById(R.id.post_comment_button);
        edit_video_comment = (EditText) findViewById(R.id.edit_video_comment);
        screen_tittle.setText("Komentar Video");

        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));
        check_button.setVisibility(View.VISIBLE);


        check_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {


                String comment = edit_video_comment.getText().toString();


                if (!comment.equals(null)) {

                    Bundle extras = getIntent().getExtras();

                    String url_post_comment = getString(R.string.API_URL_VIDEO) + "video/" + video_id + "/comments";

                    HashMap<String, String> form_body = new HashMap<>();
                    form_body.put("user_id", pref.getString("user_id", null).toString());
                    form_body.put("comments", comment);

                    try {
                        new TaskPostComment(VideoCommentAdd.this).execute(url_post_comment, form_body).get();
                        VideoCommentActivity.vid_com.finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }
            }

        });

    }


    class TaskPostComment extends AsyncTask<Object, String, String> {
        Context digital_context;

        TaskPostComment(Context context) {
            this.digital_context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress_dialog = new ProgressDialog(digital_context);
            progress_dialog.setMessage(digital_context.getString(R.string.internet_connection_waiting));
            progress_dialog.show();
        }

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

            //Toast.makeText(wrap.context, wrap.result, Toast.LENGTH_SHORT).show();
            if (result == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(digital_context)
                        .setTitle(digital_context.getString(R.string.internet_connection_alert_tittle))
                        .setMessage(digital_context.getString(R.string.internet_connection_alert_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();

                progress_dialog.dismiss();
                alertDialog.show();
                return;
            }

            Intent intent = new Intent(VideoCommentAdd.this, VideoCommentActivity.class);

            intent.putExtra("video_selected_id", video_id);
            intent.putExtra("video_selected_tittle", video_name);
            intent.putExtra("video_youtube_url", video_url);

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();

            progress_dialog.dismiss();
        }

    }
}