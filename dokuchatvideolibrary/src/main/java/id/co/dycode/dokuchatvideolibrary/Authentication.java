package id.co.dycode.dokuchatvideolibrary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import id.co.dycode.dokuchatvideolibrary.chat.ChatActivity;
import id.co.dycode.dokuchatvideolibrary.video.VideoActivity;

/**
 * Created by 1 on 7/5/2016.
 */
public class Authentication {
    HttpMethodController http_controller;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressDialog progress_dialog;

    public void AcessVideo(Context context, String email, String username, String avatar , String color, String co_brand_id){

        //String url_sign_in = context.getString(R.string.API_URL_SIGNIN);
        //new TaskAcessVideo().execute(url_sign_in, user_name, password, color, context);
        pref = context.getSharedPreferences("doku_user_prefence", 0); // 0 - for private mode
        editor = pref.edit();

        editor.putString("user_email", email); // Storing string
        editor.putString("user_name", username); // Storing string
        editor.putString("user_avatar", avatar); // Storing string
        editor.putString("user_color", color); // Storing string
        editor.putString("co_brand_id", co_brand_id); // Storing string

        editor.commit(); // commit changes

        Intent intent = new Intent(context, VideoActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        (context).startActivity(intent);

    }

    public Boolean AcessChat(Context context, String user_name, String password, String color, String co_brand_id){
        Boolean result;
        String url_sign_in = context.getString(R.string.API_URL_SIGNIN);
        try {
            new TaskAcessChat(context).execute(url_sign_in, user_name, password, color, context, co_brand_id).get();
            result = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public Boolean AcessChatRoom(Context context, String room_id){
        Boolean result;

        pref = context.getSharedPreferences("doku_user_prefence", 0); // 0 - for private mode
        editor = pref.edit();
        if (!pref.getString("user_token", null).equals(null)){
            Intent intent = new Intent(context, VideoActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("room_id", room_id);
            context.startActivity(intent);
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public void LibraryLogout(Context context){

        pref = (context).getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();

        editor.clear();
        editor.commit();
    }




    class TaskAcessVideo extends AsyncTask<Object, String, Wrapper> {


        @Override
        protected Wrapper doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();

            HashMap<String, String> form_body = new HashMap<>();
            form_body.put("user[email]", String.valueOf(post_request[1]));
            form_body.put("user[password]", String.valueOf(post_request[2]));

            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);

            Wrapper wrap = new Wrapper();
            if (result != null) {
                wrap.result = result[0];
                wrap.email = String.valueOf(post_request[1]);
                wrap.color = String.valueOf(post_request[3]);
                wrap.co_brand_id = String.valueOf(post_request[4]);
                wrap.context = (Context) post_request[4];

            } else {
                wrap.context = (Context) post_request[4];
            }

            return wrap;

        }

        @Override
        protected void onPostExecute(Wrapper wrap) {
            super.onPostExecute(wrap);

            //Toast.makeText(wrap.context, wrap.result, Toast.LENGTH_SHORT).show();
            if (wrap.result == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(wrap.context)
                        .setTitle("Failed to authenticate user")
                        .setMessage("Please check your internet connection")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                alertDialog.show();
                return;
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(wrap.result);
                Boolean sucess = jsonObject.getBoolean("success");

                //   if (sucess == true) {
                pref = wrap.context.getSharedPreferences("doku_user_prefence", 0); // 0 - for private mode
                editor = pref.edit();

                editor.putString("user_token", jsonObject.getString("token")); // Storing string
                JSONArray id = jsonObject.getJSONArray("company_ids");
                //editor.putString("user_id", String.valueOf(id.getInt(0))); // Storing string
                editor.putString("user_email", wrap.email); // Storing string
                editor.putString("user_name", jsonObject.getString("username")); // Storing string
                editor.putString("user_avatar", jsonObject.getString("avatar")); // Storing string
                editor.putString("user_color", wrap.color); // Storing string
                editor.putString("co_brand_id", wrap.co_brand_id); // Storing string

                editor.commit(); // commit changes

                Intent intent = new Intent(wrap.context, VideoActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                (wrap.context).startActivity(intent);

                //Toast.makeText(wrap.context, String.valueOf(id.getInt(0)), Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class TaskAcessChat extends AsyncTask<Object, String, Wrapper> {
        Context digital_context;

        TaskAcessChat(Context context) {
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
        protected Wrapper doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();

            HashMap<String, String> form_body = new HashMap<>();
            form_body.put("user[email]", String.valueOf(post_request[1]));
            form_body.put("user[password]", String.valueOf(post_request[2]));
            form_body.put("user[company_id]", String.valueOf(post_request[5]));

            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);

            Wrapper wrap = new Wrapper();
            if (result != null) {
                wrap.result = result[0];
                wrap.email = String.valueOf(post_request[1]);
                wrap.color = String.valueOf(post_request[3]);
                wrap.co_brand_id = String.valueOf(post_request[5]);
                wrap.context = (Context) post_request[4];

            } else {
                wrap.context = (Context) post_request[4];
            }
            return wrap;

        }

        @Override
        protected void onPostExecute(Wrapper wrap) {
            super.onPostExecute(wrap);

            //Toast.makeText(wrap.context, wrap.result, Toast.LENGTH_SHORT).show();
            if (wrap.result == null) {
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
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(wrap.result);
                Boolean sucess = jsonObject.getBoolean("success");

                if (sucess == true) {
                    pref = wrap.context.getSharedPreferences("doku_user_prefence", 0); // 0 - for private mode
                    editor = pref.edit();

                    editor.putString("user_token", jsonObject.getString("token")); // Storing string
                    //JSONArray id = jsonObject.getJSONArray("company_ids");
                    //editor.putString("user_id", String.valueOf(id.getInt(0))); // Storing string
                    editor.putString("user_email", wrap.email); // Storing string
                    editor.putString("user_name", jsonObject.getString("username")); // Storing string
                    if (jsonObject.getString("avatar").equals("")) {
                        editor.putString("user_avatar", "https://qiscuss3.s3.amazonaws.com/uploads/a6c9d41a4552978c858070da257aef1a/user.png"); // Storing string
                    } else {
                        editor.putString("user_avatar", jsonObject.getString("avatar")); // Storing string
                    }
                    editor.putString("user_color", wrap.color); // Storing string
                    editor.putString("co_brand_id", wrap.co_brand_id); // Storing string

                    editor.commit(); // commit changes

                    Intent intent = new Intent(wrap.context, ChatActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    (wrap.context).startActivity(intent);

                    //Toast.makeText(wrap.context, String.valueOf(id.getInt(0)), Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(wrap.context, "Username Invalid", Toast.LENGTH_SHORT).show();
                }
                progress_dialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class Wrapper
    {
        public String result, email, color, co_brand_id;
        public Context context;
    }
}
