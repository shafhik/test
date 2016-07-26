package id.co.dycode.dokuchatvideolibrary.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.utilities.ChatItem;

/**
 * Created by 1 on 7/5/2016.
 */
public class ChatAdapter extends BaseAdapter {
    private static ArrayList<ChatItem> listContact;

    private LayoutInflater mInflater;
    Context context;

    public ChatAdapter(Context photosFragment, ArrayList<ChatItem> results){
        listContact = results;
        mInflater = LayoutInflater.from(photosFragment);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listContact.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return listContact.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        holder = new ViewHolder();

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.chat_list_thumbnail, null);
            holder.txtname = (TextView) convertView.findViewById(R.id.chat_room);
            holder.txtphone = (TextView) convertView.findViewById(R.id.last_chat);
            holder.avatar = (ImageView) convertView.findViewById(R.id.chat_room_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.txtname.setText(listContact.get(position).GetName());
        holder.txtphone.setText(listContact.get(position).getLastChat());
        final ViewHolder finalHolder = holder;
        Glide.with(mInflater.getContext()).load(listContact.get(position).GetAvatar()).asBitmap().centerCrop().into(new BitmapImageViewTarget(finalHolder.avatar) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mInflater.getContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                finalHolder.avatar.setImageDrawable(circularBitmapDrawable);
            }
        });

        return convertView;
    }

    static class ViewHolder{
        TextView txtname, txtphone, notif_count;
        ImageView avatar;
    }
}









        /*extends ArrayAdapter<String> {
    String[] chat_room, last_chat, image_url;
    Context context;

    public ChatList(Activity context, String[] chat_room,
                       String[] last_chat, String[] image_url) {
        super(context, R.layout.list_chat);
        // TODO Auto-generated constructor stub
        this.chat_room = chat_room;
        this.last_chat = last_chat;
        this.image_url = image_url;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View single_row = inflater.inflate(R.layout.list_chat, null, true);
        TextView text_room = (TextView) single_row.findViewById(R.id.chat_room);
        TextView text_last_chat = (TextView) single_row.findViewById(R.id.last_chat);
        ImageView imageView = (ImageView) single_row.findViewById(R.id.chat_room_image);

        text_room.setText(chat_room[position]);
        text_last_chat.setText(last_chat[position]);

		/*
		 * int res = getResources()
		 *
		 * boolean filefound = true; Drawable d = null;
		 *
		 * try { d = resource.getDrawable(position); } catch(NotFoundException
		 * e) { filefound = false; }
		 *//*
        imageView.setImageURI(Uri.parse(image_url[position]));
        return single_row;
    }
}
*/