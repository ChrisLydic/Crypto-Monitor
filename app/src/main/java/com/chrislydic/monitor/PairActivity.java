package com.chrislydic.monitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class PairActivity extends AppCompatActivity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_pair );

		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		setTitle( getString( R.string.action_add ) );

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.pair_container, new PairFragment())
				.commit();
	}
}
