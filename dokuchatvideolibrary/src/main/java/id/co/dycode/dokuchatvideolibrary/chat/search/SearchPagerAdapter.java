package id.co.dycode.dokuchatvideolibrary.chat.search;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.ChatFragment;
import id.co.dycode.dokuchatvideolibrary.chat.channel.ContactFragment;
import id.co.dycode.dokuchatvideolibrary.chat.channel.DirectFragment;

/**
 * Created by fahmi on 15/07/2016.
 */
public class SearchPagerAdapter extends FragmentPagerAdapter {

    Context context;
    ChatFragment chatFragment;
    DirectFragment directFragment;


    public SearchPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        chatFragment = ChatFragment.newInstance();
        directFragment = DirectFragment.newInstance(DirectFragment.SEARCH_MODE);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return chatFragment;

            case 1:
                return directFragment;

            default:
                throw new IllegalArgumentException("Invalid search pager adapter position");
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.title_chat);

            case 1:
                return context.getResources().getString(R.string.title_contact);

            default:
                throw new IllegalArgumentException("Invalid home pager adapter position");
        }
    }
}
