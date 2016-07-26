package id.co.dycode.dokuchatvideolibrary.chat.conversation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.ListRecycleView.DividerItemDecoration;
import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.ChatActivity;
import id.co.dycode.dokuchatvideolibrary.chat.channel.ContactAddFragment;
import id.co.dycode.dokuchatvideolibrary.chat.channel.GroupMemberFragment;
import id.co.dycode.dokuchatvideolibrary.utilities.ContactItem;
import id.co.dycode.dokuchatvideolibrary.utilities.DateUtil;
import id.co.dycode.dokuchatvideolibrary.utilities.MessageItem;
import id.co.dycode.dokuchatvideolibrary.utilities.NetworkUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 1 on 7/5/2016.
 */
@TargetApi(Build.VERSION_CODES.M)
public class MessageActivity extends AppCompatActivity {

    TextView screen_tittle, message_box, message_name, message_participant;
    LinearLayout bottomBar;
    ImageButton send_button, attach_button;
    ImageView toolbar_chat_image, message_icon;
    LinearLayout message_tittle;

    private static final int SELECT_PICTURE = 1999;
    int hasLocationPermission;
    List<String> permissions = new ArrayList<String>();
    private String ImagePath;
    String extension;
    File image;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public SearchView search;
    MessageRecyclerAdapter mAdapter;
    protected Handler handler;
    HttpMethodController http_controller;
    String http_result, participant = "";
    RecyclerView mRecyclerView;
    ProgressDialog progress_dialog;
    ContactAddFragment fragment_add_contact;
    GroupMemberFragment fragment_view_contact;

    ArrayList<MessageItem> listMessageSeparatedDate = new ArrayList<>();
    MessageItem messageItem;

    String last_chat_id, last_topic_id, code_en, message_text, room_avatar, room_type;
    public static String room_id;
    public static Integer roomTypeId;
    String room_name = "doku_chat";
    Boolean isAvatarChange = false;

    public static ArrayList<ContactItem> contact_all = new ArrayList<ContactItem>();
    ContactItem contact_item;

    Pusher pusher;
    Channel channel;

    Snackbar snackbar;
    Toolbar toolbar;
    BottomSheetDialogFragment bottomSheetDialogFragment;

    private NetworkChangeReceiver networkChangeReceiver;
    private IntentFilter filter;

    //TextWatcher
    private TextWatcher text_watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            checkFieldsForEmptyValues();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.Black));
        }
        setContentView(R.layout.chat_room);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.message_recycler_view);
        //contact_all.clear();

        message_box = (EditText) findViewById(R.id.message_box);
        attach_button = (ImageButton) findViewById(R.id.attachment);
        toolbar_chat_image = (ImageView) findViewById(R.id.toolbar_chat_image);
        message_icon = (ImageView) findViewById(R.id.message_room_icon);
        bottomBar = (LinearLayout) findViewById(R.id.msg_box);


        networkChangeReceiver = new NetworkChangeReceiver();
        filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        Bundle extras = getIntent().getExtras();
        if (!extras.equals(null)) {
            room_id = extras.getString("room_id");
        }

        pref = getApplicationContext().getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();

        message_box.addTextChangedListener(text_watcher);
        checkFieldsForEmptyValues();

        hasLocationPermission = ContextCompat.checkSelfPermission(MessageActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);


        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));
        toolbar_chat_image.setVisibility(View.VISIBLE);

        String url_detail_room = getString(R.string.API_URL_MOBILE) + "room/" + room_id + "?token=" + pref.getString("user_token", null);
        try {
            new GetDetailRoomTask().execute(url_detail_room).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        screen_tittle = (TextView) findViewById(R.id.screen_tittle);
        message_tittle = (LinearLayout) findViewById(R.id.message_tittle);
        message_name = (TextView) findViewById(R.id.message_room_name);
        message_participant = (TextView) findViewById(R.id.message_room_participant);

        screen_tittle.setVisibility(View.GONE);
        message_tittle.setVisibility(View.VISIBLE);

        Glide.with(getApplicationContext()).load(room_avatar).asBitmap().centerCrop().into(new BitmapImageViewTarget(toolbar_chat_image) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                toolbar_chat_image.setImageDrawable(circularBitmapDrawable);
            }
        });

        //SharedPreferences txt = pref;
        //txt = pref;


        String url_message_list = getString(R.string.API_URL_MOBILE) + "load_comments";
        String url_participant = getString(R.string.API_URL_MOBILE) + "listPeople";


        HashMap<String, String> params = new HashMap<>();
        params.put("token", pref.getString("user_token", null));
        params.put("topic_id", last_topic_id);

        HashMap<String, String> params_participant = new HashMap<>();
        params_participant.put("token", pref.getString("user_token", null));
        params_participant.put("room_id", room_id);

        try {
            http_result = new GetListMessageTask().execute(url_message_list, params).get();
            participant = new GetListPaticipant().execute(url_participant, params_participant).get();

        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        }

        message_name.setText(room_name.toString());
        if (room_type.equals("2")) {
            message_participant.setText(participant.toString() + " Anggota");
        }
        if (room_type.equals("0")) {
            message_participant.setText(participant.toString() + " Anggota");
            message_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_small_group));
        }
        if (room_type.equals("3")) {
            message_participant.setText("Siaran");
            message_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_small_broadcast));
            bottomBar.setVisibility(View.GONE);
        }
        if (http_result != null) {
            GetListMessage(http_result);


            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new MessageRecyclerAdapter(this, listMessageSeparatedDate, mRecyclerView);

            mRecyclerView.addItemDecoration(new DividerItemDecoration());

            mRecyclerView.setAdapter(mAdapter);
        } else {

            AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this)
                    .setTitle(MessageActivity.this.getString(R.string.internet_connection_alert_tittle))
                    .setMessage(MessageActivity.this.getString(R.string.internet_connection_alert_message))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create();

            progress_dialog.dismiss();
            alertDialog.show();
            contact_all.clear();
            finish();
            return;
        }


        send_button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                message_text = message_box.getText().toString();

                if (!message_text.equals(null)) {
                    String url_post_chat = getString(R.string.API_URL_MOBILE) + "postcomment?token=" + (pref.getString("user_token", null)) +
                            "&topic_id=" + last_topic_id;


                    HashMap<String, String> form_body = new HashMap<>();
                    form_body.put("comment", message_text);

                    new TaskPostChat(MessageActivity.this).execute(url_post_chat, form_body);
                    message_box.setText("");
                } else {
                    message_box.setText("");
                }
            }
        });

        attach_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialogFragment = AttachImageFragment.newInstance(last_topic_id);
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        handler = new Handler();


    }

    @Override
    public void onBackPressed() {
        if (fragment_add_contact != null && fragment_add_contact.isVisible()) {
            getSupportFragmentManager().popBackStackImmediate();
            setResult(RESULT_OK);
            return;
        }

        if (fragment_view_contact != null && fragment_view_contact.isVisible()) {
            getSupportFragmentManager().popBackStackImmediate();
            setResult(RESULT_OK);
            return;
        }

        if (isAvatarChange) {
            setResult(RESULT_OK);
            finish();
        } else {
            finish();
        }
    }

    private void checkFieldsForEmptyValues() {
        send_button = (ImageButton) findViewById(R.id.send);

        String message_box_text = message_box.getText().toString();

        if (message_box_text.equals("")) {
            send_button.setVisibility(View.GONE);
        } else {
            send_button.setVisibility(View.VISIBLE);
        }
    }

    class GetDetailRoomTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();

            http_controller = new HttpMethodController();
            String[] result = http_controller.GetMethod(url, parameter);

            JSONObject jsonObject, jsonComment = null;
            try {
                jsonObject = new JSONObject(result[0]);
                jsonComment = jsonObject.getJSONObject("results");
                last_chat_id = String.valueOf(jsonComment.getInt("last_comment_id"));
                last_topic_id = String.valueOf(jsonComment.getInt("last_comment_topic_id"));
                code_en = jsonComment.getString("code_en");
                room_name = jsonComment.getString("name");
                room_type = String.valueOf(jsonComment.getInt("room_type"));
                room_avatar = jsonComment.getString("room_avatar");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result[0];
        }
    }

    class GetListMessageTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];

            http_controller = new HttpMethodController();
            String[] result = http_controller.GetMethod(url, parameter);

            return result[0];
        }
    }

    class GetListPaticipant extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            contact_all.clear();
            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];

            http_controller = new HttpMethodController();
            String[] result = http_controller.GetMethod(url, parameter);

            String total_participant;
            JSONObject jsonObject, jsonComment = null;
            JSONArray results = null;
            try {
                jsonObject = new JSONObject(result[0]);
                jsonComment = jsonObject.getJSONObject("results");
                results = jsonComment.getJSONArray("people");
                total_participant = String.valueOf(results.length());

                String[] name = new String[results.length()];
                String[] email = new String[results.length()];
                String[] avatar = new String[results.length()];
                JSONObject result_detail;

                for (int i = 0; i < results.length(); i++) {
                    result_detail = results.getJSONObject(i);
                    name[i] = result_detail.getString("username");
                    email[i] = result_detail.getString("email");
                    avatar[i] = result_detail.getString("avatar");

                    contact_item = new ContactItem();
                    contact_item.SetName(name[i]);
                    contact_item.SetRefId(email[i]);
                    contact_item.SetAvatar(avatar[i]);
                    contact_all.add(contact_item);
                    Collections.sort(contact_all, new Comparator<ContactItem>() {
                        @Override
                        public int compare(ContactItem lhs, ContactItem rhs) {
                            return lhs.GetName().compareToIgnoreCase(rhs.GetName());
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
                total_participant = "0";
                contact_item = new ContactItem();
                contact_item.SetName("Empty");
                contact_item.SetRefId("Empty");
                contact_item.SetAvatar("Empty");
                contact_all.add(contact_item);
            }
            return total_participant;
        }
    }


    private void GetListMessage(String result) {
        messageItem = new MessageItem();

        JSONObject jsonObject, jsonComment, json_avatar = null;
        try {
            jsonObject = new JSONObject(result);
            jsonComment = jsonObject.getJSONObject("results");
            JSONArray results = jsonComment.getJSONArray("comments");

            String[] message_id = new String[results.length()];
            String[] message = new String[results.length()];
            String[] username_as = new String[results.length()];
            //String[] username_real = new String[results.length()+1];
            String[] avatar = new String[results.length()];
            String[] time = new String[results.length()];
            JSONObject result_detail;


            for (int i = 0; i < results.length(); i++) {
                result_detail = results.getJSONObject(i);
                message_id[i] = result_detail.getString("id");
                message[i] = result_detail.getString("message");
                username_as[i] = result_detail.getString("username_as");
                //username_real[i] = result_detail.getString("username_real");
                json_avatar = result_detail.getJSONObject("user_avatar");
                json_avatar = json_avatar.getJSONObject("avatar");
                avatar[i] = json_avatar.getString("url");
                time[i] = result_detail.getString("created_at");
            }

            listMessageSeparatedDate.clear();
            for (int i = results.length() - 1; i >= 0; i--) {
                if (username_as[i].equals("quin") && !room_type.equals("3")) {
                } else {
                    messageItem = new MessageItem();
                    messageItem.setMessageId(message_id[i]);
                    messageItem.setMessage(message[i]);
                    messageItem.setUsernameAs(username_as[i]);
                    //messageItem.setUsernameReal(username_real[i]);
                    messageItem.setAvatar(avatar[i]);
                    messageItem.setTime(DateUtil.getChatTime(time[i]));
                    messageItem.setDayOfYear(DateUtil.getDayOfYear(time[i]));
                    addToMessageSeparatedDateList(messageItem);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void updateChatMessage() {

        String url_message_list = getString(R.string.API_URL_MOBILE) + "load_comments";

        HashMap<String, String> params = new HashMap<>();
        params.put("token", pref.getString("user_token", null));
        params.put("topic_id", last_topic_id);

        try {
            http_result = new GetListMessageTask().execute(url_message_list, params).get();

            //Toast.makeText(getApplicationContext(), "apdet", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {

            //Toast.makeText(getApplicationContext(), "nggk", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (ExecutionException e) {

            //Toast.makeText(getApplicationContext(), "gk apdet", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        GetListMessage(http_result);

        mAdapter.notifyDataSetChanged();


        mAdapter.setLoaded();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(listMessageSeparatedDate.size());
            }
        });
        //screen_tittle.setText("http_result");
        //Toast.makeText(getApplicationContext(), http_result, Toast.LENGTH_SHORT).show();
    }

    private void addToMessageSeparatedDateList(MessageItem messageItem) {
        if (listMessageSeparatedDate.size() > 0) {
            if (listMessageSeparatedDate.get(listMessageSeparatedDate.size() - 1).getDayOfYear() != messageItem.getDayOfYear()) {
                MessageItem separator = new MessageItem();
                separator.setSeparator(true);
                separator.setDayOfYear(messageItem.getDayOfYear());
                listMessageSeparatedDate.add(separator);
            }

        }
        listMessageSeparatedDate.add(messageItem);

    }

    class TaskPostChat extends AsyncTask<Object, String, String> {


        Context digital_context;

        TaskPostChat(Context context) {
            this.digital_context = context;
        }


        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();
            HashMap<String, String> form_body = (HashMap<String, String>) post_request[1];

            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);


            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

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

                alertDialog.show();
                return;
            }

            messageItem = new MessageItem();

            JSONObject jsonObject, jsonComment, json_avatar = null;
            try {
                jsonObject = new JSONObject(result);
                jsonComment = jsonObject.getJSONObject("message");
                JSONArray results = jsonComment.getJSONArray("comments");

                String message_id;
                String message;
                String username_as;
                //String[] username_real = new String[results.length()+1];
                String avatar;
                String time;

                message_id = jsonComment.getString("id");
                message = jsonComment.getString("message");
                username_as = jsonComment.getString("username_as");
                //username_real[i] = result_detail.getString("username_real");
                json_avatar = jsonComment.getJSONObject("user_avatar");
                json_avatar = json_avatar.getJSONObject("avatar");
                avatar = json_avatar.getString("url");
                time = jsonComment.getString("created_at");

                if (username_as.equals("quin") && !room_type.equals("3")) {
                } else {
                    messageItem = new MessageItem();
                    messageItem.setMessageId(message_id);
                    messageItem.setMessage(message);
                    messageItem.setUsernameAs(username_as);
                    //messageItem.setUsernameReal(username_real[i]);
                    messageItem.setAvatar(avatar);
                    messageItem.setTime(DateUtil.getChatTime(time));
                    messageItem.setDayOfYear(DateUtil.getDayOfYear(time));
                    addToMessageSeparatedDateList(messageItem);
                }

            } catch (Exception e) {

            }


        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (room_type.equals("0")) {
            getMenuInflater().inflate(R.menu.menu_group, menu);
            return true;
        } else if (room_type.equals("1") || room_type.equals("2")) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_leave_group || id == R.id.action_leave_room) {


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MessageActivity.this);

            // Setting Dialog Title
            alertDialog.setTitle("Peringatan");

            // Setting Dialog Message
            alertDialog.setMessage("Apakah anda yakin ingin meninggalkan percakapan ini?");

            // Setting Icon to Dialog
            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    String url_leave_group = getString(R.string.API_URL_MOBILE) + "roomdelete";

                    HashMap<String, String> parameter = new HashMap<>();
                    parameter.put("token", pref.getString("user_token", null).toString());
                    parameter.put("room_id", room_id);

                    try {
                        new TaskLeaveGroup(MessageActivity.this).execute(url_leave_group, parameter).get();
                        ChatActivity.chat_act.finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Setting Negative "NO" Button
            alertDialog.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to invoke NO event
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
            return true;
        }
        if (id == R.id.change_grup_avatar) {
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(MessageActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        SELECT_PICTURE);
                galleryIntent();
            }

            return true;
        }
        if (id == R.id.action_add_user) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in, R.anim.animation_slide_out);
            fragment_add_contact = new ContactAddFragment();
            fragmentTransaction.add(R.id.contact_group_message, fragment_add_contact).addToBackStack("contact");
            fragmentTransaction.commitAllowingStateLoss();

            return true;
        }
        if (id == R.id.action_view_user) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in, R.anim.animation_slide_out);
            fragment_view_contact = new GroupMemberFragment();
            fragmentTransaction.add(R.id.contact_group_message, fragment_view_contact).addToBackStack("contact");
            fragmentTransaction.commitAllowingStateLoss();

            return true;
        }
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectAndSubscribe() {
        if (pusher == null)
            pusher = new Pusher(getString(R.string.PUSHER_KEY));

        if (pusher.getConnection() != null && pusher.getConnection().getState() != ConnectionState.CONNECTED && pusher.getConnection().getState() != ConnectionState.CONNECTING) {
            pusher.connect(new ConnectionEventListener() {
                @Override
                public void onConnectionStateChange(final ConnectionStateChange connectionStateChange) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (connectionStateChange.getCurrentState() == ConnectionState.DISCONNECTED) {
                                if (snackbar == null) {
                                    snackbar = Snackbar.make(mRecyclerView, R.string.not_connected, Snackbar.LENGTH_INDEFINITE);
                                    View view = snackbar.getView();
                                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                                    params.gravity = Gravity.TOP;
                                    view.setLayoutParams(params);
                                    snackbar.show();
                                    params.topMargin = toolbar.getHeight();
                                    view.setLayoutParams(params);
                                }
                                snackbar.show();
                            } else if (connectionStateChange.getCurrentState() == ConnectionState.CONNECTED) {
                                updateChatMessage();
                                if (snackbar != null && snackbar.isShown()) {
                                    snackbar.dismiss();
                                }

                            }
                        }
                    });
                }

                @Override
                public void onError(String s, String s1, Exception e) {

                }
            });

            if (channel == null)
                channel = pusher.subscribe(pref.getString("user_token", null));

            channel.bind("newmessage", new SubscriptionEventListener() {
                @Override
                public void onEvent(String channelName, String eventName, final String data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject push_message = null;
                            try {
                                push_message = new JSONObject(data);
                                String is_room_id = push_message.getString("room_id");

                                if (room_id.equals(is_room_id)) {
                                    String sender = push_message.getString("sender");
                                    String message_id = push_message.getString("id");
                                    String message = push_message.getString("message");
                                    JSONObject json_avatar = push_message.getJSONObject("sender_avatar");
                                    json_avatar = json_avatar.getJSONObject("avatar");
                                    String avatar = json_avatar.getString("url");
                                    String time = push_message.getString("created_at");

                                    if (sender.equals("quin") && !room_type.equals("3")) {
                                    } else {
                                        messageItem = new MessageItem();
                                        messageItem.setMessageId(message_id);
                                        messageItem.setMessage(message);
                                        messageItem.setUsernameAs(sender);
                                        //messageItem.setUsernameReal(username_real[i]);
                                        messageItem.setAvatar(avatar);
                                        messageItem.setTime(DateUtil.getChatFromEventTime(time));
                                        messageItem.setDayOfYear(DateUtil.getDayOfYearFromEventTime(time));

                                        //prevent duplicate
                                        if (listMessageSeparatedDate.size() > 0 && messageItem.getMessageId().equals(listMessageSeparatedDate.get(listMessageSeparatedDate.size() - 1).getMessageId())) {
                                            return;
                                        }

                                        addToMessageSeparatedDateList(messageItem);

                                        mAdapter.notifyItemInserted(listMessageSeparatedDate.size());
                                        mAdapter.setLoaded();

                                        mRecyclerView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mRecyclerView.smoothScrollToPosition(listMessageSeparatedDate.size() - 1);
                                            }
                                        });
                                    }
                                }

                            } catch (Exception e) {

                            }
                        }
                    });
                }

            });
        }


    }


    @Override
    public void onResume() {
        super.onResume();

        connectAndSubscribe();
        if (networkChangeReceiver != null)
            registerReceiver(networkChangeReceiver, filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
    }

    private void galleryIntent() {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                ImagePath = getPath(selectedImageUri);

                image = new File(ImagePath);
                extension = MimeTypeMap.getFileExtensionFromUrl(ImagePath);

                String url_upload_image = getString(R.string.API_URL_UPLOAD) + "?topic_id=" + last_topic_id + "&hashing=ok&username=" + (pref.getString("user_email", null));

                new TaskPostImage(MessageActivity.this).execute(url_upload_image, ImagePath, image);

            } else if (requestCode == AttachImageFragment.SELECT_PICTURE || requestCode == AttachImageFragment.CAMERA_REQUEST) {
                bottomSheetDialogFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


    public String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    class TaskLeaveGroup extends AsyncTask<Object, String, String> {
        Context digital_context;

        TaskLeaveGroup(Context context) {
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
            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];
            HashMap<String, String> form_body = new HashMap<>();

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
                contact_all.clear();
                return;
            }

            Intent intent = new Intent(MessageActivity.this, ChatActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            contact_all.clear();
            finish();

            progress_dialog.dismiss();
        }

    }


    class TaskPostImage extends AsyncTask<Object, String, String> {

        OkHttpClient client = new OkHttpClient();
        OutputStream out = null;

        Context digital_context;

        TaskPostImage(Context context) {
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

            String result;
            try {
                String url = String.valueOf(post_request[0]);
                RequestBody formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", (String) post_request[1],
                                RequestBody.create(MediaType.parse("image/" + extension), (File) post_request[2]))
                        .build();
                Request request = new Request.Builder().url((String) post_request[0]).post(formBody).build();
                Response response = this.client.newCall(request).execute();
                result = response.body().string();
            } catch (Exception ex) {
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            JSONObject jsonObject = null;

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

            try {
                jsonObject = new JSONObject(result);
                String post_url = jsonObject.getString("url");
                room_avatar = post_url;

                Glide.with(getApplicationContext()).load(post_url).asBitmap().centerCrop().into(new BitmapImageViewTarget(toolbar_chat_image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        toolbar_chat_image.setImageDrawable(circularBitmapDrawable);
                    }
                });

                String url_change_avatar = getString(R.string.API_URL_MOBILE) + "room/change_room_avatar" + "?token=" + pref.getString("user_token", null) + "&room_id=" + room_id + "&room_avatar=" + post_url;

                HashMap<String, String> parameter = new HashMap<>();
                HashMap<String, String> formBody = new HashMap<>();

                new TaskChangeAvatar().execute(url_change_avatar, formBody, parameter);

            } catch (Exception e) {
                e.printStackTrace();
            }

            progress_dialog.dismiss();
        }

    }

    class TaskChangeAvatar extends AsyncTask<Object, String, String> {


        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();
            HashMap<String, String> form_body = (HashMap<String, String>) post_request[1];

            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);


            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            isAvatarChange = true;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case SELECT_PICTURE: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    public void SetFragmentTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }


    public Integer GetFragemntTypeId() {
        return roomTypeId;
    }

    class NetworkChangeReceiver extends BroadcastReceiver {

        Context mContext;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            mContext = context;
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.e("network reciever", "On Receive network reciever");
            if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                if ((status == NetworkUtil.NETWORK_STATUS_MOBILE || status == NetworkUtil.NETWORK_STAUS_WIFI) && pusher != null) {
                    Log.e("network receiever", "Connected");
                    connectAndSubscribe();
                }
            }

        }
    }

}
