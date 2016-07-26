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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class ChannelFragment extends Fragment implements ContactListListener {

    private FragmentActivity myContext;


    TextView screen_tittle;
    EditText inpSearchContact;
    Toolbar toolbar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    HttpMethodController http_controller;
    ProgressDialog progress_dialog;

    RecyclerView mRecyclerView;
    ContactRecycleViewAdapter mAdapter;
    protected Handler handler;
    String http_result = "";
    String room_id;

    ArrayList<ContactItem> contact_all = new ArrayList<ContactItem>();
    ContactItem contact_item;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = (FragmentActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.contact_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contact_recycler_view);

        pref = getActivity().getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();


        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        screen_tittle = (TextView) rootView.findViewById(R.id.screen_tittle);
        inpSearchContact = (EditText) rootView.findViewById(R.id.inpSearchContact);

        inpSearchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 0){
                    final List<ContactItem> filteredContactList = filter(contact_all, s.toString());
                    mAdapter.addData(filteredContactList);
                }else{
                    mAdapter.addData(contact_all);
                }

            }
        });
        screen_tittle.setText("Saluran");

        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));

        String url_contact_list = getString(R.string.API_URL_MOBILE) + "rooms_only";

        HashMap<String, String> params = new HashMap<>();
        params.put("token", (pref.getString("user_token", null)));
        params.put("company_id", (pref.getString("co_brand_id", null)));
        params.put("public", "true");

        try {
            http_result = new GetListContactTask().execute(url_contact_list, params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        GetlistContact(http_result);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(myContext));
        mAdapter = new ContactRecycleViewAdapter(mRecyclerView, this);
        mAdapter.addData(contact_all);
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
    public void onSelectContact(final int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        alertDialog.setTitle("Saluran");
        alertDialog.setMessage("Apakah anda ingin mengikuti saluran " + contact_all.get(position).GetName() + "?");
        alertDialog.setPositiveButton("YA", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String url_join_channel = getString(R.string.API_URL_MOBILE) + "join_public_room";

                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("token", (pref.getString("user_token", null)));

                HashMap<String, String> form_bpdy = new HashMap<>();
                form_bpdy.put("room_id", contact_all.get(position).GetRefiId());

                new TaskJoinChannel().execute(url_join_channel, parameters, form_bpdy);
            }
        });

        alertDialog.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();

    }

    class GetListContactTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];

            http_controller = new HttpMethodController();
            String[] result = http_controller.GetMethod(url, parameter);

            return result[0];
        }
    }

    private void GetlistContact(String result){
        JSONObject jsonObject;

        if (result == null) {
            contact_item = new ContactItem();
            contact_item.SetName("Empty");
            contact_item.SetRefId("Empty");
            contact_item.SetAvatar("Empty");
            contact_all.add(contact_item);
            Toast.makeText(getActivity(), "Koneksi server gagal", Toast.LENGTH_SHORT).show();
        } else {
            try {
                jsonObject = new JSONObject(result);
                JSONArray results = jsonObject.getJSONArray("results");

                String[] name = new String[results.length()];
                String[] email = new String[results.length()];
                String[] avatar = new String[results.length()];
                Boolean[] isJoined = new Boolean[results.length()];
                JSONObject result_detail;


                for (int i = 0; i < results.length(); i++) {
                    result_detail = results.getJSONObject(i);
                    isJoined[i] = result_detail.getBoolean("is_joined");
                    if (isJoined[i] == false) {
                        name[i] = result_detail.getString("name");
                        email[i] = result_detail.getString("id");
                        avatar[i] = result_detail.getString("room_avatar");

                        contact_item = new ContactItem();
                        contact_item.SetName(name[i]);
                        contact_item.SetRefId(email[i]);
                        contact_item.SetAvatar(avatar[i]);
                        contact_all.add(contact_item);
                    }
                    Collections.sort(contact_all, new Comparator<ContactItem>() {
                        @Override
                        public int compare(ContactItem lhs, ContactItem rhs) {
                            return lhs.GetName().compareToIgnoreCase(rhs.GetName());
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
        }
    }



    class TaskJoinChannel extends AsyncTask<Object, String, String> {

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
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    JSONObject room_detail = jsonObject.getJSONObject("room");
                    room_id = String.valueOf(room_detail.getInt("id"));
                    progress_dialog.dismiss();

                    Intent intent = new Intent(getActivity(), MessageActivity.class);
                    intent.putExtra("room_id", room_id);

                    Intent intent_self = getActivity().getIntent();
                    getActivity().finish();
                    startActivity(intent_self);

                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}