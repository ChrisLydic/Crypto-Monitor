package com.chrislydic.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.chrislydic.monitor.database.PairHelper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
	static final int CREATE_PAIR_REQUEST = 1;

	@BindView(R.id.tab_layout) protected TabLayout tabLayout;
	private CoinPagerAdapter coinAdapter;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		ButterKnife.bind(this);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		List<Pair> tabitems = PairHelper.get( this ).getPairs();
		if (tabitems.isEmpty()) {
			PairHelper.get( this ).addPair( "BTC", "USD" );
			tabitems = PairHelper.get( this ).getPairs();
		}

		tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

		final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		coinAdapter = new CoinPagerAdapter
				(getSupportFragmentManager(), tabitems);
		viewPager.setAdapter(coinAdapter);

		tabLayout.setupWithViewPager( viewPager );

		viewPager.addOnPageChangeListener(new
				TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		int id = item.getItemId();

		if ( id == R.id.action_settings ) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, CREATE_PAIR_REQUEST);
			return true;
		} else if ( id == R.id.action_add ) {
			Intent intent = new Intent(this, PairActivity.class);
			startActivityForResult( intent, CREATE_PAIR_REQUEST );
			return true;
		}

		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		super.onActivityResult(requestCode, resultCode, data);
		coinAdapter.updateTabs( PairHelper.get( this ).getPairs() );
	}
}
