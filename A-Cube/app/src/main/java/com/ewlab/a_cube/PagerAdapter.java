/*
PAGERADAPTER: to manage the fragments of tablayout
 */
package com.ewlab.a_cube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.tab_action.TabAction;
import com.ewlab.a_cube.tab_games.TabGames;


public class PagerAdapter extends FragmentPagerAdapter {

    private static final int TABS = 2;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
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

    public CharSequence getPageTitle(int position){
        switch(position){
            case 0:
                return "Actions";
            case 1:
                String b = String.valueOf(R.string.games);
                return "Games";
            default:
                return null;
        }
    }

}
