package id.co.dycode.dokuchatvideolibrary.chat.conversation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.utilities.DateUtil;
import id.co.dycode.dokuchatvideolibrary.utilities.MessageItem;

/**
 * Created by 1 on 7/5/2016.
 */
public class MessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;

    private final int VIEW_SEPARATOR = 2;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    int lastPosition = 1;

    RecyclerView.ViewHolder select_holder;

    SharedPreferences pref;
    String loggedinUser, sender;

    private ArrayList<MessageItem> mDataset;

    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    View convertView;

    public MessageRecyclerAdapter(Context context, ArrayList<MessageItem> myDataSet, RecyclerView recyclerView) {
        mDataset = myDataSet;
        this.context = context;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            linearLayoutManager.setStackFromEnd(true);
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
        return mDataset.get(position) != null ? mDataset.get(position).isSeparator() ? VIEW_SEPARATOR : VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        pref = context.getSharedPreferences("doku_user_prefence", 0);

        loggedinUser = pref.getString("user_name", null);

        if (viewType == VIEW_ITEM) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_message, parent, false);

            vh = new TextViewHolder(convertView);
        } else if (viewType == VIEW_SEPARATOR) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_separator_message, parent, false);
            vh = new SeparatorViewHolder(convertView);
        } else {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_loading_item, parent, false);

            vh = new ProgressViewHolder(convertView);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        select_holder = holder;
        String sender;
        if (holder instanceof TextViewHolder) {
            sender = mDataset.get(position).getUsernameAs();
            if (sender.equals(loggedinUser)) {

                ((TextViewHolder) holder).other_layout.setVisibility(View.GONE);
                ((TextViewHolder) holder).my_layout.setVisibility(View.VISIBLE);

                String message_inbox, image_checker1, image_checker2;
                message_inbox = mDataset.get(position).getMessage();
                if (message_inbox.length() > 8) {
                    image_checker1 = message_inbox.substring(0, 6);
                    image_checker2 = message_inbox.substring(message_inbox.length() - 7, message_inbox.length());
                    if (image_checker1.equals("[file]") && image_checker2.equals("[/file]")) {
                        ((TextViewHolder) holder).frame_messege_image.setVisibility(View.VISIBLE);
                        ((TextViewHolder) holder).my_message_image.setVisibility(View.VISIBLE);
                        ((TextViewHolder) holder).my_message.setVisibility(View.GONE);
                        LoadImage(message_inbox, holder);
                    } else {
                        ((TextViewHolder) holder).frame_messege_image.setVisibility(View.GONE);
                        ((TextViewHolder) holder).my_message.setVisibility(View.VISIBLE);
                    }
                } else {
                    ((TextViewHolder) holder).frame_messege_image.setVisibility(View.GONE);
                    ((TextViewHolder) holder).my_message.setVisibility(View.VISIBLE);
                }
                ((TextViewHolder) holder).my_message.setText(mDataset.get(position).getMessage());
                ((TextViewHolder) holder).my_time.setText(mDataset.get(position).getTime());
            } else {

                ((TextViewHolder) holder).other_layout.setVisibility(View.VISIBLE);
                ((TextViewHolder) holder).my_layout.setVisibility(View.GONE);

                String message_inbox, image_checker1, image_checker2;
                message_inbox = mDataset.get(position).getMessage();
                if (message_inbox.length() > 8) {
                    image_checker1 = message_inbox.substring(0, 6);
                    image_checker2 = message_inbox.substring(message_inbox.length() - 7, message_inbox.length());
                    if (image_checker1.equals("[file]") && image_checker2.equals("[/file]")) {
                        ((TextViewHolder) holder).message_image.setVisibility(View.VISIBLE);
                        ((TextViewHolder) holder).message.setVisibility(View.GONE);
                        Glide.with(context).load(message_inbox.substring(7, message_inbox.length() - 8)).asBitmap().centerCrop().into(new BitmapImageViewTarget(((TextViewHolder) holder).message_image));
                        ((TextViewHolder) holder).message_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //finalConvertView.setBackground(holder.message_image.getDrawable());
                                loadPhoto(((TextViewHolder) holder).message_image, convertView, 1000, 1000);
                            }
                        });
                    } else {
                        ((TextViewHolder) holder).message_image.setVisibility(View.GONE);
                        ((TextViewHolder) holder).message.setVisibility(View.VISIBLE);
                    }
                } else {
                    ((TextViewHolder) holder).message_image.setVisibility(View.GONE);
                    ((TextViewHolder) holder).message.setVisibility(View.VISIBLE);
                }
                ((TextViewHolder) holder).message.setText(mDataset.get(position).getMessage());
                ((TextViewHolder) holder).message_sender.setText(mDataset.get(position).getUsernameAs());
                ((TextViewHolder) holder).time.setText(mDataset.get(position).getTime());
                Glide.with(context).load(mDataset.get(position).getAvatar()).asBitmap().centerCrop().into(new BitmapImageViewTarget(((TextViewHolder) holder).avatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ((TextViewHolder) holder).avatar.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
        } else if (holder instanceof SeparatorViewHolder) {
            ((SeparatorViewHolder) holder).textSeparator.setText(DateUtil.getSeparatorDay(mDataset.get(position).getDayOfYear()));
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

    public void LoadImage(final String message_inbox, final RecyclerView.ViewHolder holder) {
        Glide.with(context).load(message_inbox.substring(7, message_inbox.length() - 8))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        ((TextViewHolder) holder).loading_failed.setVisibility(View.VISIBLE);
                        ((TextViewHolder) holder).frame_messege_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((TextViewHolder) holder).load_message_image.setVisibility(View.VISIBLE);
                                ((TextViewHolder) holder).loading_failed.setVisibility(View.GONE);
                                LoadImage(message_inbox, holder);
                            }
                        });
                        ((TextViewHolder) holder).load_message_image.setVisibility(View.GONE);
                        ((TextViewHolder) holder).my_message_image.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        ((TextViewHolder) holder).my_message_image.setVisibility(View.VISIBLE);
                        ((TextViewHolder) holder).load_message_image.setVisibility(View.GONE);
                        ((TextViewHolder) holder).my_message_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //finalConvertView.setBackground(holder.message_image.getDrawable());
                                loadPhoto(((TextViewHolder) holder).my_message_image, convertView, 1000, 1000);
                            }
                        });
                        return false;
                    }
                })
                .centerCrop()
                .into(new BitmapImageViewTarget(((TextViewHolder) holder).my_message_image));

    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView message, message_sender, time, my_message, my_time;
        ImageView avatar, message_image, my_message_image, loading_failed;
        LinearLayout other_layout, my_layout;
        FrameLayout frame_messege_image;
        ProgressBar load_message_image;

        public TextViewHolder(View v) {
            super(v);
            message = (TextView) v.findViewById(R.id.message);
            message_sender = (TextView) v.findViewById(R.id.message_sender);
            time = (TextView) v.findViewById(R.id.time_message);
            avatar = (ImageView) v.findViewById(R.id.avatar_sender);
            message_image = (ImageView) v.findViewById(R.id.message_image);
            my_message = (TextView) v.findViewById(R.id.my_message);
            my_time = (TextView) v.findViewById(R.id.my_time_message);
            my_message_image = (ImageView) v.findViewById(R.id.my_message_image);
            loading_failed = (ImageView) v.findViewById(R.id.img_loading_failed);
            frame_messege_image = (FrameLayout) v.findViewById(R.id.frame_messege_image);
            load_message_image = (ProgressBar) v.findViewById(R.id.progressBar);
            other_layout = (LinearLayout) v.findViewById(R.id.message_other);
            my_layout = (LinearLayout) v.findViewById(R.id.message_self);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar_video);
        }
    }


    private void loadPhoto(ImageView imageView, View convertView, int width, int height) {


        AlertDialog.Builder imageDialog = new AlertDialog.Builder(convertView.getRootView().getContext());
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.custom_fullimage_dialog,
                (ViewGroup) convertView.findViewById(R.id.layout_root));
        ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
        image.setImageDrawable(imageView.getDrawable());
        imageDialog.setView(layout);
        imageDialog.setPositiveButton("Tutup", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });


        imageDialog.create();
        imageDialog.show();

    }

    private class SeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView textSeparator;

        public SeparatorViewHolder(View convertView) {
            super(convertView);
            textSeparator = (TextView) convertView.findViewById(R.id.text_separator);
        }
    }
}