package com.chrislydic.monitor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class CoinPagerAdapter extends FragmentStatePagerAdapter {
	List<Pair> tabItems;

	public CoinPagerAdapter( FragmentManager fm, List<Pair> items) {
		super(fm);
		this.tabItems = items;
	}

	@Override
	public Fragment getItem( int position ) {
		return CoinFragment.newInstance( tabItems.get( position ) );
	}

	@Override
	public int getItemPosition( Object object ) {
		// when notifyDataSetChanged is called, this method will tell the adapter
		// to reload the fragments - needed because otherwise user can change pair
		// positions in settings and then any existing fragments in the main
		// activity won't be recreated and may display the wrong pair's data.
		return POSITION_NONE;
	}

	@Override
	public CharSequence getPageTitle( int position ) {
		if ( position >= tabItems.size() ) {
			return null;
		}

		return tabItems.get( position ).toString();
	}

	@Override
	public int getCount() {
		return tabItems.size();
	}

	public void updateTabs(List<Pair> items) {
		tabItems = items;
		notifyDataSetChanged();

	}

}
