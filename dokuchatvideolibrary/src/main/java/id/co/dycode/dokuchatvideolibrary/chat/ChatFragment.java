package id.co.dycode.dokuchatvideolibrary.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.conversation.MessageActivity;
import id.co.dycode.dokuchatvideolibrary.utilities.ChatItem;
import id.co.dycode.dokuchatvideolibrary.utilities.NetworkUtil;

/**
 * Created by fahmi on 15/07/2016.
 */
public class ChatFragment extends Fragment {

    private static final int RQ_MESSAGE = 12;
    ChatExpandableAdapter chatAdapter;
    List<String> listDataHeader;
    HashMap<String, ArrayList<ChatItem>> listDataChild;

    ArrayList<ChatItem> listBroadcast = new ArrayList<ChatItem>();
    ArrayList<ChatItem> listChannel = new ArrayList<ChatItem>();
    ArrayList<ChatItem> listGroups = new ArrayList<ChatItem>();
    ArrayList<ChatItem> listContacts = new ArrayList<ChatItem>();

    ExpandableListView lvChat;
    RelativeLayout containerEmpty;
    FrameLayout frameContainer;

    SharedPreferences pref;
    String userToken, coBrandId;

    Pusher pusher;
    Channel channel;

    Snackbar snackbar;
    Toolbar mainToolbar;
    float fabY;


    private NetworkChangeReceiver networkChangeReceiver;
    private IntentFilter filter;


    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        lvChat = (ExpandableListView) view.findViewById(R.id.list_chat);
        containerEmpty = (RelativeLayout) view.findViewById(R.id.container_empty);
        frameContainer = (FrameLayout) view.findViewById(R.id.frame_container);


        if (getTag().equals("search")) {
            containerEmpty.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pref = getContext().getSharedPreferences("doku_user_prefence", 0);
        userToken = pref.getString("user_token", null);
        coBrandId = pref.getString("co_brand_id", null);

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, ArrayList<ChatItem>>();

        if (getTag().equals("chat")) {
            updateChatList(userToken, coBrandId, "");

            chatAdapter = new ChatExpandableAdapter(getContext(), listDataHeader, listDataChild);

            // setting list adapter
            lvChat.setAdapter(chatAdapter);
            // Listview on child click listener
            lvChat.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {


                    String last_chat_id = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetLastCommentId();
                    String last_topic_id = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetLastTopicId();
                    String room_id = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetRoomId();
                    String code_en = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetCodeEn();
                    String room_name = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetName();
                    String room_avatar = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetAvatar();
                    String room_type = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetAvatar();

                    if (snackbar != null && snackbar.isShown()) {
                        Toast.makeText(getActivity(), "Koneksi terputus", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getContext(), MessageActivity.class);
                        intent.putExtra("last_chat_id", last_chat_id);
                        intent.putExtra("last_topic_id", last_topic_id);
                        intent.putExtra("room_id", room_id);
                        intent.putExtra("code_en", code_en);
                        intent.putExtra("room_name", room_name);
                        intent.putExtra("room_avatar", room_avatar);
                        intent.putExtra("room_type", room_avatar);

                        startActivityForResult(intent, RQ_MESSAGE);
                    }
                    return false;
                }
            });

            networkChangeReceiver = new NetworkChangeReceiver();
            filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");


        } else {
            updateChatList(userToken, null, "*");

            chatAdapter = new ChatExpandableAdapter(getContext(), listDataHeader, listDataChild);

            // setting list adapter
            lvChat.setAdapter(chatAdapter);
            // Listview on child click listener
            lvChat.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {


                    String last_chat_id = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetLastCommentId();
                    String last_topic_id = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetLastTopicId();
                    String room_id = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetRoomId();
                    String code_en = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetCodeEn();
                    String room_name = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetName();
                    String room_avatar = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetAvatar();
                    String room_type = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).GetAvatar();

                    if (snackbar != null && snackbar.isShown()) {
                        Toast.makeText(getActivity(), "Koneksi terputus", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getContext(), MessageActivity.class);
                        intent.putExtra("last_chat_id", last_chat_id);
                        intent.putExtra("last_topic_id", last_topic_id);
                        intent.putExtra("room_id", room_id);
                        intent.putExtra("code_en", code_en);
                        intent.putExtra("room_name", room_name);
                        intent.putExtra("room_avatar", room_avatar);
                        intent.putExtra("room_type", room_avatar);

                        startActivityForResult(intent, RQ_MESSAGE);
                    }
                    return false;
                }
            });

            networkChangeReceiver = new NetworkChangeReceiver();
            filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            containerEmpty.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        connectAndSubscribe();
        if (networkChangeReceiver != null)
            getActivity().registerReceiver(networkChangeReceiver, filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (networkChangeReceiver != null)
            getActivity().unregisterReceiver(networkChangeReceiver);
    }

    private void updateChatRoom(String roomId, String sender, String message) {
        if (sender.equalsIgnoreCase("quin"))
            return;

        listDataChild.put(listDataHeader.get(0), listBroadcast);

        for (String header : listDataHeader) {
            for (int i = 0; i < listDataChild.get(header).size(); i++) {
                if (listDataChild.get(header).get(i).GetRoomId().equals(roomId)) {
                    //Refresh Item
                    listDataChild.get(header).get(i).SetLastChat(message);
                    String notifCount = sender.equals(pref.getString("user_name", null)) ? "0" : "1";
                    listDataChild.get(header).get(i).SetNotifCount(notifCount);
                    chatAdapter.notifyDataSetChanged();
                }
            }
        }

    }


    private void connectAndSubscribe() {
        if (pusher == null)
            pusher = new Pusher(getString(R.string.PUSHER_KEY));
        if (pusher.getConnection() != null && pusher.getConnection().getState() != ConnectionState.CONNECTED && pusher.getConnection().getState() != ConnectionState.CONNECTING) {

            pusher.connect(new ConnectionEventListener() {
                @Override
                public void onConnectionStateChange(final ConnectionStateChange connectionStateChange) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (connectionStateChange.getCurrentState() == ConnectionState.DISCONNECTED) {
                                    mainToolbar = ((ChatActivity) getActivity()).getToolbar();
                                    if (snackbar == null) {
                                        snackbar = Snackbar.make(frameContainer, R.string.not_connected, Snackbar.LENGTH_INDEFINITE);
                                        View view = snackbar.getView();
                                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                                        params.gravity = Gravity.TOP;
                                        view.setLayoutParams(params);
                                        snackbar.show();
                                        params.topMargin = mainToolbar.getHeight();
                                        view.setLayoutParams(params);
                                    }
                                    snackbar.show();
                                } else if (connectionStateChange.getCurrentState() == ConnectionState.CONNECTED) {
                                    if (snackbar != null && snackbar.isShown()) {
                                        snackbar.dismiss();
                                    }

                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(String s, String s1, Exception e) {

                }
            });

            if (channel == null)
                channel = pusher.subscribe(userToken);


            channel.bind("newmessage", new SubscriptionEventListener() {
                @Override
                public void onEvent(String channelName, String eventName, final String data) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject push_message;
                                try {
                                    push_message = new JSONObject(data);
                                    String roomId = push_message.getString("room_id");
                                    String message = push_message.getString("message");
                                    String sender = push_message.getString("sender");

                                    updateChatRoom(roomId, sender, message);
                                } catch (Exception e) {
                                    Log.e("UpdtROOM", e.getLocalizedMessage());
                                }
                            }
                        });
                    }
                }

            });
        }
    }

    public void updateChatList(String token, String co_brand_id, String filter) {
        String url_chat_room_list = getString(R.string.API_URL_MOBILE) + "load_rooms?token=" + token + "&company_id=" + co_brand_id + "&filter=" + filter;
        new TaskGetListRoom(getContext()).execute(url_chat_room_list);
    }

    private ArrayList<ChatItem> getListChat(String result) {
        ArrayList<ChatItem> contactlist = new ArrayList<ChatItem>();
        ChatItem contact;


        listDataHeader.clear();
        listDataChild.clear();
        // Adding child data
        listDataHeader.add(getString(R.string.grouping_chat_1));
        listDataHeader.add(getString(R.string.grouping_chat_2));
        listDataHeader.add(getString(R.string.grouping_chat_3));
        listDataHeader.add(getString(R.string.grouping_chat_4));

        listBroadcast.clear();
        listChannel.clear();
        listGroups.clear();
        listContacts.clear();

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(result);
            JSONArray results = jsonObject.getJSONArray("results");

            Integer[] chat_id = new Integer[results.length()];
            Integer[] room_type = new Integer[results.length()];
            String[] chat_room = new String[results.length()];
            String[] last_chat = new String[results.length()];
            String[] image_url = new String[results.length()];
            String[] last_comment_id = new String[results.length()];
            String[] last_topic_id = new String[results.length()];
            String[] last_person = new String[results.length()];
            String[] last_time = new String[results.length()];
            String[] room_id = new String[results.length()];
            String[] code_en = new String[results.length()];
            String[] notif_counter = new String[results.length()];
            JSONObject result_detail;
            for (int i = 0; i < results.length(); i++) {
                result_detail = results.getJSONObject(i);
                chat_id[i] = result_detail.getInt("id");
                chat_room[i] = result_detail.getString("name");
                room_type[i] = result_detail.getInt("room_type");
                last_person[i] = result_detail.getString("sender");
                if (!last_person[i].equals("quin")) {
                    last_chat[i] = result_detail.getString("last_comment_message");
                } else {
                    last_chat[i] = "";
                }
                image_url[i] = result_detail.getString("room_avatar");
                last_comment_id[i] = result_detail.getString("last_comment_id");
                last_topic_id[i] = result_detail.getString("last_comment_topic_id");
                last_time[i] = result_detail.getString("last_comment_message_time");
                room_id[i] = result_detail.getString("id");
                code_en[i] = result_detail.getString("code_en");
                notif_counter[i] = String.valueOf(result_detail.getInt("count_notif"));
                contact = new ChatItem();
                contact.SetName(chat_room[i]);
                contact.SetLastChat(last_chat[i]);
                contact.SetAvatar(image_url[i]);
                contact.SetLastCommentId(last_comment_id[i]);
                contact.SetLastTopicId(last_topic_id[i]);
                contact.SetLastPerson(last_person[i]);
                contact.SetLastTime(last_time[i]);
                contact.SetRoomId(room_id[i]);
                contact.SetCodeEn(code_en[i]);
                contact.SetNotifCount(notif_counter[i]);

                if (room_type[i] == 0) {
                    listGroups.add(contact);
                } else if (room_type[i] == 1) {
                    listContacts.add(contact);
                } else if (room_type[i] == 2) {
                    listChannel.add(contact);
                } else if (room_type[i] == 3) {
                    listBroadcast.add(contact);
                }
            }

            listDataChild.put(listDataHeader.get(0), listBroadcast);
            listDataChild.put(listDataHeader.get(1), listChannel);
            listDataChild.put(listDataHeader.get(2), listGroups);
            listDataChild.put(listDataHeader.get(3), listContacts);


            chatAdapter.notifyDataSetChanged();
            if (listBroadcast.size() == 0 && listChannel.size() == 0 && listGroups.size() == 0 && listContacts.size() == 0) {
                containerEmpty.setVisibility(View.VISIBLE);
            } else {
                containerEmpty.setVisibility(View.GONE);
            }
            lvChat.post(new Runnable() {
                @Override
                public void run() {
                    lvChat.expandGroup(0);
                    lvChat.expandGroup(1);
                    lvChat.expandGroup(2);
                    lvChat.expandGroup(3);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            containerEmpty.setVisibility(View.VISIBLE);
        }

        return contactlist;
    }


    class TaskGetListRoom extends AsyncTask<String, String, String> {
        Context digital_context;

        TaskGetListRoom(Context context) {
            this.digital_context = context;
        }

        @Override
        protected String doInBackground(String... post_request) {

            HttpURLConnection connection = null;

            try {
                //Create connection
                URL url = new URL(post_request[0]);
                connection = (HttpURLConnection) url.openConnection();
                //connection.connect();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Language", "en-US");

                //Get Response
                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();

                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
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

            getListChat(result);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_MESSAGE && resultCode == AppCompatActivity.RESULT_OK) {
            updateChatList(userToken, coBrandId, "");
        }
        super.onActivityResult(requestCode, resultCode, data);
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
