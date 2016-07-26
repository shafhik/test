package id.co.dycode.dokuchatvideolibrary.video.player;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.video.player.comments.VideoCommentActivity;

/**
 * Created by 1 on 7/5/2016.
 */
public class VideoPlayerFragment extends Fragment {

    String API_KEY;
    private FragmentActivity myContext;


    private String VIDEO_ID;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext=(FragmentActivity) activity;
        VIDEO_ID = ((VideoCommentActivity)getActivity()).GetYoutubeID();
        API_KEY = getActivity().getString(R.string.YOUTUBE_API_KEY);
    }

    @Override
    public View  onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.you_tube_api, container, false);


        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();


        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_layout, youTubePlayerFragment).commit();


        youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {


            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    player.loadVideo(VIDEO_ID);
                    player.play();
                }
            }


            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                // YouTube error
                String errorMessage = error.toString();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                Log.d("errorMessage:", errorMessage);
            }
        });

        return rootView;
    }
}