package id.co.dycode.dokuchatvideolibrary.chat.channel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
 * Created by 1 on 7/13/2016.
 */
public class ContactAddFragment extends Fragment implements ContactListListener {

    private FragmentActivity myContext;

    TextView screen_tittle;
    EditText edit_video_comment;
    EditText inpSearchContact;
    ImageButton imgCreateGroup;
    Toolbar toolbar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    HttpMethodController http_controller;

    ContactRecycleViewAdapter mAdapter;
    protected Handler handler;
    String http_result = "";
    String last_chat_id, last_topic_id, code_en, room_name, room_avatar;

    ArrayList<ContactItem> contact_all = new ArrayList<ContactItem>();
    ArrayList<ContactItem> addedContact = new ArrayList<ContactItem>();
    List<ContactItem> filteredContactList = new ArrayList<>();
    ContactItem contact_item;
    ProgressDialog progress_dialog;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = (FragmentActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.contact_list, container, false);

        final RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contact_recycler_view);

        pref = getActivity().getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();


        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        screen_tittle = (TextView) rootView.findViewById(R.id.screen_tittle);
        edit_video_comment = (EditText) rootView.findViewById(R.id.edit_video_comment);
        inpSearchContact = (EditText) rootView.findViewById(R.id.inpSearchContact);
        imgCreateGroup = (ImageButton) rootView.findViewById(R.id.post_comment_button);
        ((MessageActivity)getActivity()).SetFragmentTypeId(1);
        addedContact = ((MessageActivity) getActivity()).contact_all;

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
                    filteredContactList = filter(contact_all, s.toString());
                    mAdapter.addData(filteredContactList);
                } else {
                    filteredContactList.clear();
                    filteredContactList.addAll(contact_all);
                    mAdapter.addData(filteredContactList);
                }

            }
        });
        screen_tittle.setText("Tambah Anggota");

        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));

        imgCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap<String, String> parameter = new HashMap<>();
                parameter.put("token", pref.getString("user_token", null));
                HashMap<String, String> form_body = new HashMap<>();
                form_body.put("room_id", ((MessageActivity) getActivity()).room_id);

                String contacts = "";
                for (ContactItem contact : filteredContactList) {
                    if (contact.isSelected()) {
                        if (!TextUtils.isEmpty(contacts)) {
                            contacts += ",";
                        }
                        contacts += contact.GetRefiId();
                    }

                }
                form_body.put("username_visual", contacts);


                String url_create_private_room = getActivity().getString(R.string.API_URL_MOBILE) + "invite";

                new TaskInvitePerson().execute(url_create_private_room, parameter, form_body);
            }
        });


        String url_contact_list = getActivity().getString(R.string.API_DOKU) + "apprequest/chatfavaccount";

        MessageDigest md = null;
        StringBuffer sb = null;
        String text_words = getActivity().getString(R.string.DOKU_PARTNER_KEY) + (pref.getString("user_email", null)) + getActivity().getString(R.string.DOKU_API_VERSION);

        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(text_words.getBytes());

            byte byteData[] = md.digest();

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
            http_result = new GetListContactTask().execute(url_contact_list, params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            http_result = e.toString();
        } catch (ExecutionException e) {
            e.printStackTrace();
            http_result = e.toString();
        }

        GetlistContact(http_result);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(myContext));
        mAdapter = new ContactRecycleViewAdapter(mRecyclerView, this);
        mAdapter.addData(filteredContactList);
        mRecyclerView.addItemDecoration(new DividerItemDecoration());
        mRecyclerView.setAdapter(mAdapter);


        handler = new Handler();

        mAdapter.setOnLoadMoreListener(new ContactRecycleViewAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
            }
        });

        return rootView;
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
        mAdapter.notifyItemChanged(position);

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

            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);

            return result[0];
        }
    }

    private void GetlistContact(String result) {
        JSONObject jsonObject;

        if (result == null) {
            contact_item.SetName("Empty");
            contact_item.SetRefId("Empty");
            contact_item.SetAvatar("Empty");
            contact_all.add(contact_item);
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

                    contact_item = new ContactItem();
                    contact_item.SetName(name[i]);
                    contact_item.SetRefId(email[i]);
                    contact_item.SetAvatar(avatar[i]);
                    contact_all.add(contact_item);

                    contact_all.removeAll(addedContact);

                    Collections.sort(contact_all, new Comparator<ContactItem>() {
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
                contact_item = new ContactItem();
                contact_item.SetName("Empty");
                contact_item.SetRefId("Empty");
                contact_item.SetAvatar("Empty");
                contact_all.add(contact_item);
            }

            filteredContactList.clear();
            filteredContactList.addAll(contact_all);
        }
    }

    class TaskInvitePerson extends AsyncTask<Object, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress_dialog = new ProgressDialog(getActivity());
            progress_dialog.setMessage(getActivity().getString(R.string.internet_connection_waiting));
            progress_dialog.show();
        }

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);

            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];
            HashMap<String, String> form_body = (HashMap<String, String>) post_request[2];


            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);


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
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("room_id", ((MessageActivity) getActivity()).room_id);

                progress_dialog.dismiss();

                getActivity().finish();

                startActivity(intent);
            }

        }
    }

}