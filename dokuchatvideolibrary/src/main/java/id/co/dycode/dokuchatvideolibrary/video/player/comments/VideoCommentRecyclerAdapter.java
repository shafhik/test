package id.co.dycode.dokuchatvideolibrary.video.player.comments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import id.co.dycode.dokuchatvideolibrary.HttpMethodController;
import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.utilities.VideoCommentItem;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoCommentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    HttpMethodController httpController;


    RecyclerView.ViewHolder selectHolder;

    private ArrayList<VideoCommentItem> mDataset;
    Integer current_user_id;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public VideoCommentRecyclerAdapter(ArrayList<VideoCommentItem> myDataSet, RecyclerView recyclerView) {
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
                    .inflate(R.layout.video_comment_thumbnail, parent, false);

            vh = new VideoCommentViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_loading_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        selectHolder = holder;
        pref = context.getSharedPreferences("doku_user_prefence", 0);
        editor = pref.edit();
        current_user_id = Integer.valueOf(pref.getString("user_id", null));
        if (holder instanceof VideoCommentViewHolder) {

            final VideoCommentViewHolder vHolder = (VideoCommentViewHolder) holder;
            final Integer[] report_counter = {mDataset.get(position).getFlag()};
            JSONArray flag_list = mDataset.get(position).getFlagList();
            vHolder.btnInappropriate.setTextColor(context.getResources().getColor(R.color.Grey));
            vHolder.btnInappropriate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_attention_off, 0);
            for (int i = 0; i < flag_list.length(); i++) {
                try {
                    if (current_user_id == Integer.valueOf(flag_list.getString(i))) {
                        vHolder.btnInappropriate.setTextColor(context.getResources().getColor(R.color.dokuColor));
                        vHolder.btnInappropriate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_attention_on, 0);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            vHolder.textCommentWriter.setText(mDataset.get(position).getUserName());
            vHolder.textVideoComment.setText(mDataset.get(position).getComments());
            vHolder.textVideoCommentTime.setText(mDataset.get(position).getTime());
            Glide.with(context).load(mDataset.get(position).getAvatar()).asBitmap().centerCrop().into(new BitmapImageViewTarget(vHolder.imgCommentWriterAvatar) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    vHolder.imgCommentWriterAvatar.setImageDrawable(circularBitmapDrawable);
                }
            });

            if (report_counter[0] >= 5) {
                vHolder.hide_comment.setVisibility(View.VISIBLE);
            } else {
                vHolder.hide_comment.setVisibility(View.GONE);
            }
            vHolder.btnInappropriate.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String uri_comment = mDataset.get(position).getUri();
                    String[] uri_parts = uri_comment.split("/");
                    String comment_id = uri_parts[1];

                    String url_report = context.getResources().getString(R.string.API_URL_VIDEO) + "comments/" +
                            comment_id + "/flag";
                    String parameters = null;

                    HashMap<String, String> form_data = new HashMap<>();
                    form_data.put("user_id", pref.getString("user_id", null).toString());

                    String status = null;
                    try {
                        status = new TaskReportComment().execute(url_report, form_data).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (status != null) {
                        if (status.equals("201")) {
                            vHolder.btnInappropriate.setTextColor(context.getResources().getColor(R.color.dokuColor));
                            vHolder.btnInappropriate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_attention_on, 0);
                            report_counter[0] = report_counter[0] + 1;
                            if (report_counter[0] >= 5) {
                                vHolder.hide_comment.setVisibility(View.VISIBLE);
                            } else {
                                vHolder.hide_comment.setVisibility(View.GONE);
                            }
                        } else if (status.equals("204")) {
                            vHolder.btnInappropriate.setTextColor(context.getResources().getColor(R.color.Grey));
                            vHolder.btnInappropriate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_attention_off, 0);
                        }
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.internet_connection_alert_tittle))
                                .setMessage(context.getString(R.string.internet_connection_alert_message))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();

                        alertDialog.show();
                        return;
                    }
                    //Toast.makeText(context, status.toString(), Toast.LENGTH_LONG).show();
                }
            });

            vHolder.hide_comment.setOnClickListener(new LinearLayout.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Peringatan");
                    alertDialog.setMessage("Komentar ini mengandung unsur yang tidak pantas. Apakah tetap ingin ditampilkan?");
                    alertDialog.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            vHolder.hide_comment.setVisibility(View.GONE);
                        }
                    });

                    // Setting Negative "NO" Button
                    alertDialog.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to invoke NO event
                            dialog.cancel();
                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();
                }
            });

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        selectHolder.itemView.clearAnimation();
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

    public static class VideoCommentViewHolder extends RecyclerView.ViewHolder {
        public TextView textVideoComment, textCommentWriter, textVideoCommentTime;
        public ImageView imgCommentWriterAvatar;
        public Button btnInappropriate;
        public LinearLayout hide_comment;

        public VideoCommentViewHolder(View v) {
            super(v);
            textVideoComment = (TextView) v.findViewById(R.id.text_video_comment);
            textCommentWriter = (TextView) v.findViewById(R.id.text_comment_writer);
            textVideoCommentTime = (TextView) v.findViewById(R.id.text_video_comment_time);
            imgCommentWriterAvatar = (ImageView) v.findViewById(R.id.img_comment_writer_avatar);

            textVideoCommentTime = (TextView) v.findViewById(R.id.text_video_comment_time);
            imgCommentWriterAvatar = (ImageView) v.findViewById(R.id.img_comment_writer_avatar);
            btnInappropriate = (Button) v.findViewById(R.id.btn_inappropriate);

            hide_comment = (LinearLayout) v.findViewById(R.id.hide_comment);


        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar_video);
        }
    }

    class TaskReportComment extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... post_request) {

            String url = String.valueOf(post_request[0]);
            HashMap<String, String> parameter = new HashMap<>();
            HashMap<String, String> form_body = (HashMap<String, String>) post_request[1];

            httpController = new HttpMethodController();
            String[] result = httpController.PostMethod(url, parameter, form_body);

            return result[1];
        }
    }
}