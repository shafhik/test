package id.co.dycode.dokuchatvideolibrary.chat.channel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.ListRecycleView.DividerItemDecoration;
import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.conversation.MessageActivity;
import id.co.dycode.dokuchatvideolibrary.utilities.ContactItem;

/**
 * Created by 1 on 7/5/2016.
 */
public class DirectFragment extends Fragment implements ContactListListener {

    private static final String CONTACT_MODE = "id.co.dycode.dokuchatvideolibrary.chat.channel.DirectFragment";
    public static final int NORMAL_MODE = 0;
    public static final int SEARCH_MODE = 1;


    private Context context;

    SharedPreferences pref;
    HttpMethodController httpController;

    String httpResult = "";

    List<ContactItem> listContact = new ArrayList<ContactItem>();
    List<ContactItem> filteredContactList = new ArrayList<>();
    ContactItem contactItem;
    ContactRecycleViewAdapter mAdapter;

    //Views
    TextView textTitle;
    EditText inpSearchContact;
    Toolbar toolbar;
    RecyclerView rvContact;
    RelativeLayout containerEmpty;

    String friendMail, friendName;

    int mode;

    public static DirectFragment newInstance(int mode) {
        DirectFragment directFragment = new DirectFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CONTACT_MODE, mode);
        directFragment.setArguments(bundle);
        return directFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.contact_list, container, false);

        rvContact = (RecyclerView) rootView.findViewById(R.id.contact_recycler_view);
        pref = getActivity().getSharedPreferences("doku_user_prefence", 0);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        textTitle = (TextView) rootView.findViewById(R.id.screen_tittle);
        inpSearchContact = (EditText) rootView.findViewById(R.id.inpSearchContact);
        containerEmpty = (RelativeLayout) rootView.findViewById(R.id.container_empty);
        inpSearchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    filteredContactList = filter(listContact, s.toString());
                    mAdapter.addData(filteredContactList);
                } else {
                    filteredContactList.clear();
                    filteredContactList.addAll(listContact);
                    mAdapter.addData(filteredContactList);
                }

            }
        });
        textTitle.setText("Kontak");

        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));


        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupMode(getArguments());
        rvContact.setHasFixedSize(true);
        rvContact.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new ContactRecycleViewAdapter(rvContact, this);
        rvContact.addItemDecoration(new DividerItemDecoration());
        rvContact.setAdapter(mAdapter);
        if (getTag().equals("direct")) {
            getListContact("");
        } else {
            containerEmpty.setVisibility(View.VISIBLE);
        }
        mAdapter.setOnLoadMoreListener(new ContactRecycleViewAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
            }
        });
    }

    private void setupMode(Bundle arguments) {
        if (arguments == null)
            return;

        mode = arguments.getInt(CONTACT_MODE, NORMAL_MODE);
        if (mode == SEARCH_MODE) {
            toolbar.setVisibility(View.GONE);
            inpSearchContact.setVisibility(View.GONE);
        } else {
            containerEmpty.setVisibility(View.GONE);
        }

    }

    private List<ContactItem> filter(List<ContactItem> contactItems, String query) {
        query = query.toLowerCase();

        final List<ContactItem> filteredModelList = new ArrayList<>();
        for (ContactItem contactItem : contactItems) {
            final String text = contactItem.GetName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(contactItem);
            }
        }
        return filteredModelList;
    }

    @Override
    public void onSelectContact(final int position) {
        friendMail = filteredContactList.get(position).GetRefiId();
        friendName = filteredContactList.get(position).GetName();

        if (friendMail.equals("Empty")) {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Kontak");

        // Setting Dialog Message
        alertDialog.setMessage("Apakah anda ingin membuat percakapan pribadi dengan " + filteredContactList.get(position).GetName() + "?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YA", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                JSONArray contacts = new JSONArray();
                contacts.put(friendMail);


                JSONObject post_json = new JSONObject();
                try {
                    post_json.put("token", pref.getString("user_token", null).toString());
                    post_json.put("name", friendName);
                    post_json.put("room_type", 1);
                    post_json.put("company_id", Integer.valueOf(pref.getString("co_brand_id", null).toString()));
                    post_json.put("emails", contacts);
                    //post_json.put("company_id", "5/5/1993");

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String url_create_grup_room = getString(R.string.API_URL_MOBILE) + "room_create_with_participant";
                new TaskCreateRoomWithParticipant().execute(url_create_grup_room, post_json);

            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }

    public void getListContact(String filter) {
        String url_contact_list = getActivity().getString(R.string.API_DOKU) + "apprequest/chatfavaccount";

        MessageDigest md = null;
        byte[] digest = new byte[0];
        StringBuffer sb = null;
        String text_words = getActivity().getString(R.string.DOKU_PARTNER_KEY) + (pref.getString("user_email", null)) + getActivity().getString(R.string.DOKU_API_VERSION);


        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(text_words.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        HashMap<String, String> params = new HashMap<>();
        params.put("refId", (pref.getString("user_email", null)));
        params.put("words", sb.toString());
        params.put("version", getActivity().getString(R.string.DOKU_API_VERSION));
        params.put("firstResult", "0");
        params.put("maxResult", "999999");

        if (!TextUtils.isEmpty(filter)) {
            params.put("name", filter);
        }

        try {
            httpResult = new GetListContactTask().execute(url_contact_list, params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            httpResult = e.toString();
        } catch (ExecutionException e) {
            e.printStackTrace();
            httpResult = e.toString();
        }

        parseListContact(httpResult);
    }


    class GetListContactTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();
            HashMap<String, String> form_body = (HashMap<String, String>) post_request[1];

            httpController = new HttpMethodController();
            String[] result = httpController.PostMethod(url, parameter, form_body);

            return result[0];
        }
    }

    private void parseListContact(String result) {
        listContact.clear();
        JSONObject jsonObject;

        if (result == null) {
            contactItem = new ContactItem();
            contactItem.SetName("Empty");
            contactItem.SetRefId("Empty");
            contactItem.SetAvatar("Empty");
            listContact.add(contactItem);
            Toast.makeText(getActivity(), "Koneksi server gagal", Toast.LENGTH_SHORT).show();
        } else {
            try {
                jsonObject = new JSONObject(result);
                JSONArray results = jsonObject.getJSONArray("friends");

                String[] name = new String[results.length()];
                String[] email = new String[results.length()];
                String[] avatar = new String[results.length()];
                JSONObject result_detail;


                for (int i = 0; i < results.length(); i++) {
                    result_detail = results.getJSONObject(i);
                    name[i] = result_detail.getString("name");
                    email[i] = result_detail.getString("refId");
                    avatar[i] = result_detail.getString("avatar");

                    contactItem = new ContactItem();
                    contactItem.SetName(name[i]);
                    contactItem.SetRefId(email[i]);
                    contactItem.SetAvatar(avatar[i]);
                    listContact.add(contactItem);

                    Collections.sort(listContact, new Comparator<ContactItem>() {
                        @Override
                        public int compare(ContactItem lhs, ContactItem rhs) {
                            String lName = lhs.GetName();
                            String rName = rhs.GetName();

                            return lName.compareToIgnoreCase(rName);
                        }
                    });
                }


            } catch (JSONException e) {
                e.printStackTrace();
                if (mode == NORMAL_MODE) {
                    contactItem = new ContactItem();
                    contactItem.SetName("Empty");
                    contactItem.SetRefId("Empty");
                    contactItem.SetAvatar("Empty");
                    listContact.add(contactItem);
                }
            }
        }

        filteredContactList.clear();
        filteredContactList.addAll(listContact);
        mAdapter.addData(filteredContactList);
        if (mode == SEARCH_MODE) {
            if (filteredContactList.size() == 0) {
                containerEmpty.setVisibility(View.VISIBLE);
            } else {
                containerEmpty.setVisibility(View.GONE);
            }
        }
    }


    class TaskCreateRoomWithParticipant extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            //replace parameter and form body here
            HashMap<String, String> parameter = new HashMap<>();
            JSONObject json_body = (JSONObject) post_request[1];


            httpController = new HttpMethodController();
            String[] result = httpController.PostJsonMethod(url, parameter, json_body);


            if (result[0] != null) {
                return result[0].toString();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.internet_connection_alert_tittle))
                        .setMessage(getActivity().getString(R.string.internet_connection_alert_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();

                alertDialog.show();
                return;
            } else {
                JSONObject jsonObject = null;
                String roomId = "", lastChatId = "", lastTopicId = "", codeEn = "", roomName = "", roomAvatar = "";

                try {
                    jsonObject = new JSONObject(result);
                    JSONObject room_creation = jsonObject.getJSONObject("results");
                    JSONObject room_detail = room_creation.getJSONObject("rooms");
                    roomId = String.valueOf(room_detail.getInt("id"));
                    lastChatId = String.valueOf(room_detail.getInt("last_comment_id"));
                    lastTopicId = String.valueOf(room_detail.getInt("last_topic_id"));
                    codeEn = room_detail.getString("code_en");
                    roomName = room_detail.getString("name");
                    roomAvatar = room_detail.getString("room_avatar");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("last_chat_id", lastChatId);
                intent.putExtra("last_topic_id", lastTopicId);
                intent.putExtra("room_id", roomId);
                intent.putExtra("code_en", codeEn);
                intent.putExtra("room_name", roomName);
                intent.putExtra("room_avatar", roomAvatar);

                Intent intent_self = getActivity().getIntent();

                getActivity().finish();
                startActivity(intent_self);

                startActivity(intent);

            }
        }
    }


}