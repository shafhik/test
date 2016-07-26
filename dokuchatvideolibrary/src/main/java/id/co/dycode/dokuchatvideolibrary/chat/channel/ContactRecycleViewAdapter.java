package id.co.dycode.dokuchatvideolibrary.chat.channel;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.conversation.MessageActivity;
import id.co.dycode.dokuchatvideolibrary.utilities.ContactItem;

/**
 * Created by 1 on 7/5/2016.
 */
public class ContactRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    String targetEmail;

    RecyclerView.ViewHolder select_holder;
    HttpMethodController http_controller;
    SharedPreferences pref;

    private ArrayList<ContactItem> mDataset = new ArrayList<>();

    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private ContactListListener contactListListener;

    ProgressDialog progress_dialog;

    public ContactRecycleViewAdapter(RecyclerView recyclerView, ContactListListener contactListListener) {
        this.contactListListener = contactListListener;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }


    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        context = parent.getContext();
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contact_list_thumbnail, parent, false);

            vh = new TextViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_loading_item, parent, false);

            vh = new ProgressViewHolder(v);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        select_holder = holder;
        if (holder instanceof TextViewHolder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contactListListener.onSelectContact(position);
                }
            });

            ((TextViewHolder) holder).imgSelected.setVisibility(mDataset.get(position).isSelected() ? View.VISIBLE : View.GONE);
            ((TextViewHolder) holder).text_name.setText(mDataset.get(position).GetName());
            if (position != 0 && mDataset.get(position).GetName().substring(0, 1).toUpperCase().equals(mDataset.get(position - 1).GetName().substring(0, 1).toUpperCase())) {
                ((TextViewHolder) holder).text_first_letter.setText("");
            } else {
                ((TextViewHolder) holder).text_first_letter.setText(mDataset.get(position).GetName().substring(0, 1).toUpperCase());
            }
            Glide.with(context).load(mDataset.get(position).GetAvatar()).asBitmap().centerCrop().into(new BitmapImageViewTarget(((TextViewHolder) holder).avatar) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    ((TextViewHolder) holder).avatar.setImageDrawable(circularBitmapDrawable);
                }
            });
            targetEmail = mDataset.get(position).GetRefiId();
            //Picasso.with(context).load(thumbnail_url).into(((VideoCommentViewHolder) holder).img_video);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        select_holder.itemView.clearAnimation();
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView text_name, text_first_letter;
        public ImageView avatar, imgSelected;

        public TextViewHolder(View v) {
            super(v);
            text_name = (TextView) v.findViewById(R.id.contact_name);
            text_first_letter = (TextView) v.findViewById(R.id.contact_first_letter);
            avatar = (ImageView) v.findViewById(R.id.contact_avatar);
            imgSelected = (ImageView) v.findViewById(R.id.img_contact_selected);

            try {
                if (((MessageActivity) context).GetFragemntTypeId() == 0) {
                    itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            pref = context.getSharedPreferences("doku_user_prefence", 0);
                            if (!mDataset.get(getPosition()).GetRefiId().equals(pref.getString("user_email", null))) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());

                                // Setting Dialog Title
                                alertDialog.setTitle("Kontak");

                                // Setting Dialog Message
                                alertDialog.setMessage("Apakah anda ingin menghapus " + mDataset.get(getPosition()).GetName() + " dari grup ini?");

                                alertDialog.setPositiveButton("YA", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        HashMap<String, String> parameter = new HashMap<>();
                                        parameter.put("token", pref.getString("user_token", null));
                                        parameter.put("room_id", ((MessageActivity) context).room_id);
                                        parameter.put("target_email", mDataset.get(getPosition()).GetRefiId());

                                        String url_create_private_room = context.getString(R.string.API_URL_MOBILE) + "kick_participant";

                                        new TaskKickParticipant().execute(url_create_private_room, parameter);

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
                            return false;
                        }
                    });
                }
            }catch (Exception e){

            }
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar_video);
        }
    }

    public void addData(List<ContactItem> contactItemList) {
        mDataset.clear();
        mDataset.addAll(contactItemList);
        notifyDataSetChanged();
    }

    class TaskKickParticipant extends AsyncTask<Object, String, String> {

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

            HashMap<String, String> parameter = (HashMap<String, String>) post_request[1];
            HashMap<String, String> form_body = new HashMap<>();


            http_controller = new HttpMethodController();
            String[] result = http_controller.PostMethod(url, parameter, form_body);


            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progress_dialog.dismiss();
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("room_id", ((MessageActivity) context).room_id);

            ((MessageActivity)context).finish();

            context.startActivity(intent);


        }
    }
}