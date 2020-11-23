/*
PAGERADAPTER: to manage the fragments of tablayout
 */
package com.ewlab.a_cube;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.tab_action.TabAction;
import com.ewlab.a_cube.tab_games.TabGames;


public class PagerAdapter extends FragmentPagerAdapter {

    private static final int TABS = 2;
    Context context;


    public PagerAdapter(FragmentManager fm, Context context) {

        super(fm);
        this.context = context;

    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                TabAction tAzioni = new TabAction();
                return tAzioni;
            case 1:
                TabGames tTest = new TabGames();
                return tTest;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return TABS;
    }

    public CharSequence getPageTitle(int position) {

        switch(position) {

            case 0:
                return context.getResources().getString(R.string.actions);

            case 1:
                return context.getResources().getString(R.string.games);

            default:
                return null;
        }
    }

}
