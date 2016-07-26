package id.co.dycode.dokuchatvideolibrary.chat.channel;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import java.util.ArrayList;
import java.util.List;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.ListRecycleView.DividerItemDecoration;
import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.conversation.MessageActivity;
import id.co.dycode.dokuchatvideolibrary.utilities.ContactItem;

/**
 * Created by 1 on 7/15/2016.
 */
public class GroupMemberFragment extends Fragment implements ContactListListener {

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

    ArrayList<ContactItem> contact_all = ((MessageActivity) getActivity()).contact_all;

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
        imgCreateGroup.setImageResource(R.drawable.ic_clear_white_24dp);

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
                    final List<ContactItem> filteredContactList = filter(contact_all, s.toString());
                    mAdapter.addData(filteredContactList);
                } else {
                    mAdapter.addData(contact_all);
                }

            }
        });
        screen_tittle.setText("Anggota Percakapan");


        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null).toString()));
        ((MessageActivity)getActivity()).SetFragmentTypeId(0);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(myContext));
        mAdapter = new ContactRecycleViewAdapter(mRecyclerView, this);
        mAdapter.addData(contact_all);
        mRecyclerView.addItemDecoration(new DividerItemDecoration());
        mRecyclerView.setAdapter(mAdapter);

        handler = new Handler();

        mRecyclerView.setOnLongClickListener(new RecyclerView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), "Long Pressed", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
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
        if (contact_all.get(position).GetRefiId().equals("Empty"))
            return;

    }

}