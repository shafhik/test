package id.co.dycode.dokuchatvideolibrary.chat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.channel.ChannelFragment;
import id.co.dycode.dokuchatvideolibrary.chat.channel.ContactFragment;
import id.co.dycode.dokuchatvideolibrary.chat.channel.DirectFragment;
import id.co.dycode.dokuchatvideolibrary.chat.search.SearchFragment;
import id.co.dycode.dokuchatvideolibrary.utilities.CommonUtils;

/**
 * Created by 1 on 7/5/2016.
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton fabOpen, fabPrivate, fabGroup, fabChannel;
    private Boolean isFabOpen = false;
    private Animation animFabOpen, animeFabClose, animRotateForward, animRotateBackward;

    SharedPreferences pref;
    TextView textTitle;
    EditText inpSearch;
    ImageButton btnSearch;
    Toolbar toolbar;

    ContactFragment fragmentContact;
    ChannelFragment fragmentChannel;
    DirectFragment fragmentDirect;
    ChatFragment fragmentChat;
    SearchFragment fragmentSearch;

    public static ChatActivity chat_act;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.Black));
        }
        setContentView(R.layout.activity_chat);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fabOpen = (FloatingActionButton) findViewById(R.id.fab);
        fabPrivate = (FloatingActionButton) findViewById(R.id.fab_private);
        fabGroup = (FloatingActionButton) findViewById(R.id.fab_group);
        fabChannel = (FloatingActionButton) findViewById(R.id.fab_channel);
        inpSearch = (EditText) findViewById(R.id.search_textbox);
        btnSearch = (ImageButton) findViewById(R.id.search);

        animFabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        animeFabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        animRotateForward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        animRotateBackward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        fabOpen.setOnClickListener(this);
        fabPrivate.setOnClickListener(this);
        fabGroup.setOnClickListener(this);
        fabChannel.setOnClickListener(this);
        btnSearch.setVisibility(View.VISIBLE);
        textTitle = (TextView) findViewById(R.id.screen_tittle);
        textTitle.setText(getString(R.string.title_chat));
        chat_act = this;


        pref = getApplicationContext().getSharedPreferences("doku_user_prefence", 0);
        toolbar.setBackgroundColor(Color.parseColor(pref.getString("user_color", null)));

        inpSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchRoom();
                    handled = true;
                }
                return handled;
            }
        });

        btnSearch.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentChat.snackbar != null && fragmentChat.snackbar.isShown()) {
                    Toast.makeText(getApplicationContext(), "Koneksi terputus", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (inpSearch.getVisibility() != View.VISIBLE) {
                        inpSearch.setVisibility(View.VISIBLE);
                        textTitle.setVisibility(View.GONE);
                        btnSearch.setImageResource(R.drawable.ic_clear_white_24dp);
                        CommonUtils.showKeyboard(inpSearch);
                        setUpSearch();
                    } else {
                        inpSearch.setVisibility(View.INVISIBLE);
                        textTitle.setVisibility(View.VISIBLE);
                        btnSearch.setImageResource(R.drawable.ic_navbar_search);
                        inpSearch.setText("");
                        CommonUtils.hideKeyboard(inpSearch);
                        onBackPressed();
                    }
                }
            }
        });

        setupContent();

    }

    @Override
    public void onBackPressed() {
        if (fragmentSearch != null && fragmentSearch.isVisible()) {
            inpSearch.setVisibility(View.INVISIBLE);
            textTitle.setVisibility(View.VISIBLE);
            btnSearch.setImageResource(R.drawable.ic_navbar_search);
            inpSearch.setText("");
            CommonUtils.hideKeyboard(inpSearch);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.hide(fragmentSearch);
            fragmentTransaction.commitAllowingStateLoss();
            return;
        }

        if (fragmentContact != null && fragmentContact.isVisible()) {
            getSupportFragmentManager().popBackStackImmediate();
            return;
        }

        if (fragmentDirect != null && fragmentDirect.isVisible()) {
            getSupportFragmentManager().popBackStackImmediate();
            return;
        }

        if (fragmentChannel != null && fragmentChannel.isVisible()) {
            getSupportFragmentManager().popBackStackImmediate();
            return;
        }

        finish();
    }

    private void setupContent() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragmentChat == null)
            fragmentChat = ChatFragment.newInstance();
        fragmentTransaction.add(R.id.frame_container, fragmentChat, "chat").addToBackStack("chat");
        fragmentTransaction.commitAllowingStateLoss();
    }


    private void setUpSearch() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in, R.anim.animation_slide_out);
        if (fragmentSearch == null) {
            fragmentSearch = SearchFragment.newInstance();
            fragmentTransaction.add(R.id.frame_container, fragmentSearch, "search").addToBackStack("search");
        } else {
            fragmentTransaction.show(fragmentSearch);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }


    private void searchRoom() {
        if (fragmentChat.snackbar != null && fragmentChat.snackbar.isShown()) {
            Toast.makeText(getApplicationContext(), "Koneksi terputus", Toast.LENGTH_SHORT).show();
            return;
        } else {
            CommonUtils.hideKeyboard(inpSearch);
            String filter = inpSearch.getText().toString();
            fragmentSearch.searchContactAndRoom(pref, filter);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fab) {
            animateFAB();
        } else if (id == R.id.fab_private) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in, R.anim.animation_slide_out);
            fragmentDirect = DirectFragment.newInstance(DirectFragment.NORMAL_MODE);
            fragmentTransaction.add(R.id.contact_group, fragmentDirect, "direct").addToBackStack("channel");
            fragmentTransaction.commitAllowingStateLoss();
            animateFAB();

        } else if (id == R.id.fab_group) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in, R.anim.animation_slide_out);
            fragmentContact = new ContactFragment();
            fragmentTransaction.add(R.id.contact_group, fragmentContact).addToBackStack("contact");
            fragmentTransaction.commitAllowingStateLoss();
            animateFAB();

        } else if (id == R.id.fab_channel) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in, R.anim.animation_slide_out);
            fragmentChannel = new ChannelFragment();
            fragmentTransaction.add(R.id.contact_group, fragmentChannel).addToBackStack("channel");
            fragmentTransaction.commitAllowingStateLoss();
            animateFAB();
        }
    }

    public void animateFAB() {
        if (isFabOpen) {
            fabOpen.startAnimation(animRotateBackward);
            fabPrivate.startAnimation(animeFabClose);
            fabGroup.startAnimation(animeFabClose);
            fabChannel.startAnimation(animeFabClose);
            fabPrivate.setClickable(false);
            fabGroup.setClickable(false);
            fabChannel.setClickable(false);
            fabOpen.setImageResource(R.drawable.plus);
            isFabOpen = false;

        } else {
            fabOpen.startAnimation(animRotateForward);
            fabPrivate.startAnimation(animFabOpen);
            fabGroup.startAnimation(animFabOpen);
            fabChannel.startAnimation(animFabOpen);
            fabPrivate.setClickable(true);
            fabGroup.setClickable(true);
            fabChannel.setClickable(true);
            fabOpen.setImageResource(R.drawable.plus_x);
            isFabOpen = true;

        }
    }


}