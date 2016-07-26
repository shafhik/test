package id.co.dycode.dokuchatvideolibrary.chat.channel;

import android.app.ProgressDialog;
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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
public class ContactFragment extends Fragment implements ContactListListener {

    private Context context;


    String httpResult = "";
    SharedPreferences pref;
    HttpMethodController httpController;

    List<ContactItem> listContact = new ArrayList<ContactItem>();
    List<ContactItem> filteredContactList = new ArrayList<>();
    ContactItem contactItem;
    ContactRecycleViewAdapter contactAdapter;


    // Views
    TextView textTitle;
    EditText inpVideoComment;
    EditText inpSearchContact, inpGroupName;
    ImageButton imgCreateGroup;
    Toolbar toolbar;
    RecyclerView rvContact;
    ProgressDialog progress_dialog;

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.contact_list, container, false);

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
        inpVideoComment = (EditText) rootView.findViewById(R.id.edit_video_comment);
        inpSearchContact = (EditText) rootView.findViewById(R.id.inpSearchContact);
        inpGroupName = (EditText) rootView.findViewById(R.id.search_textbox);
        imgCreateGroup = (ImageButton) rootView.findViewById(R.id.post_comment_button);
        rvContact = (RecyclerView) rootView.findViewById(R.id.contact_recycler_view);

        inpGroupName.setHint("Nama Grup");
        inpGroupName.setVisibility(View.VISIBLE);
        textTitle.setVisibility(View.GONE);


        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));


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
                    contactAdapter.addData(filteredContactList);
                } else {
                    filteredContactList.clear();
                    filteredContactList.addAll(listContact);
                    contactAdapter.addData(filteredContactList);
                }

            }
        });


        imgCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inpGroupName.getText().toString().replace(" ", "").equals("")) {
                    Toast.makeText(getActivity(), "Silahkan masukkan nama grup terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else {
                    JSONArray contacts = new JSONArray();
                    for (ContactItem contact : filteredContactList) {
                        if (contact.isSelected()) {
                            contacts.put(contact.GetRefiId());
                        }
                    }

                    JSONObject post_json = new JSONObject();
                    try {
                        post_json.put("token", pref.getString("user_token", null).toString());
                        post_json.put("name", inpGroupName.getText().toString());
                        post_json.put("room_type", 0);
                        post_json.put("company_id", Integer.valueOf(pref.getString("co_brand_id", null).toString()));
                        post_json.put("emails", contacts);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    String url_create_grup_room = getString(R.string.API_URL_MOBILE) + "room_create_with_participant";
                    new TaskCreateRoomWithParticipant().execute(url_create_grup_room, post_json);
                }
            }
        });


        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

        rvContact.setHasFixedSize(true);
        rvContact.setLayoutManager(new LinearLayoutManager(context));
        contactAdapter = new ContactRecycleViewAdapter(rvContact, this);
        contactAdapter.addData(filteredContactList);
        rvContact.addItemDecoration(new DividerItemDecoration());
        rvContact.setAdapter(contactAdapter);
        contactAdapter.setOnLoadMoreListener(new ContactRecycleViewAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
            }
        });


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
    public void onSelectContact(int position) {
        if (filteredContactList.get(position).GetRefiId().equals("Empty"))
            return;

        boolean currentState = filteredContactList.get(position).isSelected();
        filteredContactList.get(position).setSelected(!currentState);
        contactAdapter.notifyItemChanged(position);

        int btnCreateGroupVisibility = View.GONE;
        for (ContactItem contactItem : filteredContactList) {
            if (contactItem.isSelected()) {
                btnCreateGroupVisibility = View.VISIBLE;
                break;
            }
        }
        imgCreateGroup.setVisibility(btnCreateGroupVisibility);
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
                }

                Collections.sort(listContact, new Comparator<ContactItem>() {
                    @Override
                    public int compare(ContactItem lhs, ContactItem rhs) {
                        String lName = lhs.GetName();
                        String rName = rhs.GetName();

                        return lName.compareToIgnoreCase(rName);
                    }
                });


                //Toast.makeText(getApplicationContext(), message[0], Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
                contactItem = new ContactItem();
                contactItem.SetName("Empty");
                contactItem.SetRefId("Empty");
                contactItem.SetAvatar("Empty");
                listContact.add(contactItem);
            }
            filteredContactList.clear();
            filteredContactList.addAll(listContact);

        }
    }


    class TaskCreateRoomWithParticipant extends AsyncTask<Object, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress_dialog = new ProgressDialog(context);
            progress_dialog.setMessage(context.getString(R.string.internet_connection_waiting));
            progress_dialog.show();
        }

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            //replace parameter and form body here
            HashMap<String, String> parameter = new HashMap<>();
            JSONObject json_body = (JSONObject) post_request[1];


            httpController = new HttpMethodController();
            String[] result = httpController.PostJsonMethod(url, parameter, json_body);


            //Toast.makeText(getApplicationContext(), message[0], Toast.LENGTH_SHORT).show();
            return result[0];
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
                progress_dialog.dismiss();
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


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("room_id", roomId);

                progress_dialog.dismiss();
                //Todo : Find better way to handle refresh backstack, clear top or something
                Intent intent_self = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent_self);
                startActivity(intent);
            }

        }
    }

}