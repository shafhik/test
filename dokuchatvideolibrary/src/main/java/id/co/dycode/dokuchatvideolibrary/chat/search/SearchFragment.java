package id.co.dycode.dokuchatvideolibrary.chat.search;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id.co.dycode.dokuchatvideolibrary.R;
import id.co.dycode.dokuchatvideolibrary.chat.ChatFragment;
import id.co.dycode.dokuchatvideolibrary.chat.channel.DirectFragment;

/**
 * Created by fahmi on 15/07/2016.
 */
public class SearchFragment extends Fragment {

    TabLayout tabSearch;
    ViewPager pagerSearch;
    SearchPagerAdapter searchPagerAdapter;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, null);
        tabSearch = (TabLayout) view.findViewById(R.id.tab_layout);
        pagerSearch = (ViewPager) view.findViewById(R.id.view_pager);


        SharedPreferences pref = getContext().getSharedPreferences("doku_user_prefence", 0);
        int themeColor = Color.parseColor(pref.getString("user_color", null));

        tabSearch.setSelectedTabIndicatorColor(themeColor);
        tabSearch.setTabTextColors(ContextCompat.getColor(getContext(),R.color.Black), themeColor);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchPagerAdapter = new SearchPagerAdapter(getContext(), getFragmentManager());
        pagerSearch.setAdapter(searchPagerAdapter);
        tabSearch.setupWithViewPager(pagerSearch);
    }

    public void searchContactAndRoom(SharedPreferences pref, String filter){
        ((ChatFragment) searchPagerAdapter.getItem(0)).updateChatList(pref.getString("user_token", null), pref.getString("co_brand_id", null), filter);
        ((DirectFragment) searchPagerAdapter.getItem(1)).getListContact(filter);
    }


}
