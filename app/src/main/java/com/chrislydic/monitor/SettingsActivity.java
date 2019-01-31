package com.chrislydic.monitor;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chrislydic.monitor.database.PairHelper;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {
	@BindView( R.id.pair_recycler_view ) RecyclerView recyclerView;
	private PairAdapter adapter;
	private List<Pair> pairsList;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_settings );
		ButterKnife.bind(this);

		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getString( R.string.action_settings ));

		pairsList = PairHelper.get( this ).getPairs();
		adapter = new PairAdapter( pairsList );
		recyclerView.setLayoutManager( new LinearLayoutManager( this ) );
		recyclerView.setAdapter( adapter );

		getFragmentManager().beginTransaction()
				.replace(R.id.settings_container, new SettingsFragment())
				.commit();
	}

	@Override
	public boolean onSupportNavigateUp(){
		finish();
		return true;
	}

	private void updateUI() {
		adapter.notifyDataSetChanged();
	}

	static class PairHolder extends RecyclerView.ViewHolder {
		@BindView( R.id.pair_name ) public TextView value;
		@BindView( R.id.move_pair_up ) public ImageButton moveUp;
		@BindView( R.id.move_pair_down ) public ImageButton moveDown;
		@BindView( R.id.delete_pair ) public ImageButton delete;

		private PairHolder( View view ) {
			super( view );
			ButterKnife.bind(this, view);
		}
	}

	private class PairAdapter extends RecyclerView.Adapter<PairHolder> {
		private List<Pair> pairs;

		public PairAdapter( List<Pair> pairs ) {
			this.pairs = pairs;
		}

		@Override
		public PairHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
			View pairView = LayoutInflater.from( parent.getContext() )
					.inflate( R.layout.pair_settings, parent, false );

			return new PairHolder( pairView );
		}

		@Override
		public void onBindViewHolder( final PairHolder holder, int position ) {
			holder.value.setText( pairs.get( position ).toString() );

			holder.moveUp.setVisibility( View.VISIBLE );
			holder.moveDown.setVisibility( View.VISIBLE );

			if ( position == 0 ) {
				holder.moveUp.setVisibility( View.INVISIBLE );
			}
			if (position == pairs.size() - 1) {
				holder.moveDown.setVisibility( View.INVISIBLE );
			}

			holder.moveUp.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					PairHelper.get( getApplicationContext() ).swapPair( pairs.get( holder.getAdapterPosition() ), pairs.get( holder.getAdapterPosition() - 1 ) );
					Collections.swap( pairsList, holder.getAdapterPosition(), holder.getAdapterPosition() - 1 );
					updateUI();
				}
			} );

			holder.moveDown.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					PairHelper.get( getApplicationContext() ).swapPair( pairs.get( holder.getAdapterPosition() ), pairs.get( holder.getAdapterPosition() + 1 ) );
					Collections.swap( pairsList, holder.getAdapterPosition(), holder.getAdapterPosition() + 1 );
					updateUI();
				}
			} );

			holder.delete.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					AlertDialog.Builder alertDialogBuilder =
							new AlertDialog.Builder( SettingsActivity.this, R.style.AlertDialogTheme );
					alertDialogBuilder.setTitle( getResources().getString( R.string.delete_pair_title ) )
							.setMessage( getResources().getString( R.string.delete_pait_message, pairs.get( holder.getAdapterPosition() ) ) )
							.setPositiveButton(
									"OK",
									new DialogInterface.OnClickListener() {
										public void onClick( DialogInterface dialog, int id ) {
											PairHelper.get( getApplicationContext() ).deletePair( pairs.get( holder.getAdapterPosition() ) );
											pairsList.remove( holder.getAdapterPosition() );
											updateUI();
										}
									} )
							.setNegativeButton( "Cancel", null )
							.show();
				}
			} );
		}

		@Override
		public int getItemCount() {
			return pairs.size();
		}
	}
}
