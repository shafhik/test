package id.co.dycode.dokuchatvideolibrary.video;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.utilities.VideoItem;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    int lastPosition = 1;

    RecyclerView.ViewHolder select_holder;

    private ArrayList<VideoItem> mDataset;

    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public VideoRecyclerViewAdapter(ArrayList<VideoItem> myDataSet, RecyclerView recyclerView) {
        mDataset = myDataSet;

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
                    .inflate(R.layout.video_list_thumbnail, parent, false);

            vh = new TextViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_loading_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        select_holder = holder;
        if (holder instanceof TextViewHolder) {
            Animation animation = AnimationUtils.loadAnimation(context,
                    (position > lastPosition) ? R.anim.bottom_up
                            : R.anim.up_botton);
            holder.itemView.startAnimation(animation);
            lastPosition = position;

            String thumbnail_url = "http://img.youtube.com/vi/" + mDataset.get(position).GetYoutubeId() + "/default.jpg";

            ((TextViewHolder) holder).text_video_tittle.setText(mDataset.get(position).GetTitle());
            //((VideoCommentViewHolder) holder).text_video_description.setText(mDataset.get(position).GetDescription());
            Glide.with(context).load(thumbnail_url).asBitmap().centerCrop().into(new BitmapImageViewTarget(((TextViewHolder) holder).img_video));
            //Picasso.with(context).load(thumbnail_url).into(((VideoCommentViewHolder) holder).img_video);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView){
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

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView text_video_tittle, text_video_description;
        public ImageView img_video;

        public TextViewHolder(View v) {
            super(v);
            text_video_tittle = (TextView) v.findViewById(R.id.video_tittle);
            //text_video_description = (TextView) v.findViewById(R.id.video_description);
            img_video = (ImageView) v.findViewById(R.id.video_thumbnail);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar_video);
        }
    }


}