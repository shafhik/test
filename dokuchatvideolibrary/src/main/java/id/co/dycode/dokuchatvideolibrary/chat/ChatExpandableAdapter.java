package id.co.dycode.dokuchatvideolibrary.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.utilities.ChatItem;
import id.co.dycode.dokuchatvideolibrary.utilities.DateUtil;

/**
 * Created by 1 on 7/5/2016.
 */
public class ChatExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> _listDataHeader; // header titles
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    // child data in format of header title, child title
    private HashMap<String, ArrayList<ChatItem>> _listDataChild;
    private ArrayList<ChatItem> listContact;
    private LayoutInflater mInflater;
    ExpandableListView eLV;

    public ChatExpandableAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, ArrayList<ChatItem>> listChildData) {
        this.context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        ChatItem array_room = (ChatItem) getChild(groupPosition, childPosition);

        pref = context.getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();
        final ChildViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.chat_list_thumbnail, null);

            viewHolder = new ChildViewHolder();

            viewHolder.txt_name_room = (TextView) convertView.findViewById(R.id.chat_room);
            viewHolder.txt_last_chat = (TextView) convertView.findViewById(R.id.last_chat);
            viewHolder.txt_last_time = (TextView) convertView.findViewById(R.id.time_chat_list);
            viewHolder.avatar_image = (ImageView) convertView.findViewById(R.id.chat_room_image);
            viewHolder.icon_chat = (ImageView) convertView.findViewById(R.id.room_icon);
            viewHolder.img_last_chat = (ImageView) convertView.findViewById(R.id.img_last_chat);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) convertView.getTag();
        }



        viewHolder.txt_name_room.setText(array_room.GetName());

        //handle lastchat image
        if (TextUtils.isEmpty(array_room.getLastChat())) {
            viewHolder.txt_last_chat.setText(R.string.tap_to_start);
            viewHolder.img_last_chat.setVisibility(View.GONE);
        } else if (array_room.getLastChat().startsWith("[file]") && array_room.getLastChat().endsWith("[/file]")) {
            viewHolder.img_last_chat.setVisibility(View.VISIBLE);
            viewHolder.txt_last_chat.setText(R.string.image);
        } else {
            viewHolder.img_last_chat.setVisibility(View.GONE);
            viewHolder.txt_last_chat.setText(array_room.getLastChat());
        }


        viewHolder.txt_last_time.setText(DateUtil.getStringLastChatDate(array_room.GetLastTime()));
        if (!array_room.GetNotifCount().equals("0")) {
            viewHolder.txt_last_chat.setTextColor(Color.parseColor(pref.getString("user_color", null).toString()));
        }else {
            viewHolder.txt_last_chat.setTextColor(ContextCompat.getColor(context,R.color.Black));
        }
        try {
            if (groupPosition == 0) {
                viewHolder.icon_chat.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_status_big_broadcast));
            } else if (groupPosition == 2) {
                viewHolder.icon_chat.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_status_big_group));
            } else{
                viewHolder.icon_chat.setImageDrawable(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Glide.with(mInflater.getContext()).load(array_room.GetAvatar()).asBitmap().centerCrop().into(new BitmapImageViewTarget(viewHolder.avatar_image) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mInflater.getContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                viewHolder.avatar_image.setImageDrawable(circularBitmapDrawable);
            }
        });

        return convertView;

    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        GroupViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.chat_list_grouping, null);
            viewHolder = new GroupViewHolder();
            viewHolder.txt_group_name = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            viewHolder.imgGroupIndicator = (ImageView) convertView
                    .findViewById(R.id.imgGroupIndicator);
            viewHolder.groupDropdown = (LinearLayout) convertView
                    .findViewById(R.id.groupDropdown);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder) convertView.getTag();
        }

        viewHolder.txt_group_name.setTypeface(null, Typeface.BOLD);
        viewHolder.txt_group_name.setText(headerTitle);

        eLV = (ExpandableListView) parent;
        if (getChildrenCount(groupPosition) == 0) {
            viewHolder.imgGroupIndicator.setVisibility(View.GONE);

        } else {
            viewHolder.imgGroupIndicator.setVisibility(View.VISIBLE);
            viewHolder.imgGroupIndicator.setImageResource(isExpanded ? R.drawable.ic_group_indicator_down : R.drawable.ic_group_indicator_up);
        }

        //eLV.expandGroup(groupPosition);
        //eLV.invalidateViews();
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    static class GroupViewHolder {
        TextView txt_group_name;
        ImageView imgGroupIndicator;
        LinearLayout groupDropdown;
    }

    static class ChildViewHolder {
        TextView txt_name_room, txt_last_chat, txt_last_time;
        ImageView avatar_image, icon_chat, img_last_chat;
    }
}
