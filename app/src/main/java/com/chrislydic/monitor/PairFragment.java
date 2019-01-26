package com.chrislydic.monitor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chrislydic.monitor.database.PairHelper;
import com.chrislydic.monitor.network.Coin;
import com.chrislydic.monitor.network.CoinDeserializer;
import com.chrislydic.monitor.network.CoinList;
import com.chrislydic.monitor.network.CryptoCompareAPI;
import com.chrislydic.monitor.network.SimplePrice;
import com.chrislydic.monitor.network.SimplePriceDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 *
 */
public class PairFragment extends Fragment {
	private CryptoCompareAPI priceService;
	private Call<CoinList> coinListCall;
	private Call<SimplePrice> priceCall;

	@BindView(R.id.search_coin) protected EditText searchCoin;
	@BindView(R.id.coin_loading) protected ProgressBar loadCoin;
	private TextWatcher searchWatcher;
	private CoinList coinList;
	private CoinAdapter adapter;

	private Unbinder unbinder;

	public PairFragment() {
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		Gson gson =
				new GsonBuilder()
						.registerTypeAdapter(CoinList.class, new CoinDeserializer())
						.registerTypeAdapter(SimplePrice.class, new SimplePriceDeserializer())
						.create();

		Retrofit retrofitCryptoCompare = new Retrofit.Builder()
				.baseUrl( CryptoCompareAPI.URL)
				.addConverterFactory( GsonConverterFactory.create(gson) )
				.build();

		priceService = retrofitCryptoCompare.create(CryptoCompareAPI.class);
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState ) {
		View view = inflater.inflate(R.layout.fragment_pair, container, false);
		unbinder = ButterKnife.bind(this, view);

		adapter = new CoinAdapter( new ArrayList<Coin>() );

		DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
		float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		int noOfColumns = (int) (dpWidth / 120);

		RecyclerView mOrderRecyclerView = (RecyclerView) view.findViewById( R.id.coin_recycler_view );
		mOrderRecyclerView.setLayoutManager( new GridLayoutManager( getContext(), noOfColumns ) );
		mOrderRecyclerView.setAdapter( adapter );

		coinListCall = priceService.fetchCoinList();
		coinListCall.enqueue( new Callback<CoinList>() {
			@Override
			public void onResponse( Call<CoinList> call, Response<CoinList> response ) {
				coinList = response.body();
				updateUI();
			}

			@Override
			public void onFailure( Call<CoinList> call, Throwable t ) {
				Log.e( "", t.getMessage() );
			}
		} );

		searchWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged( CharSequence charSequence, int i, int i1, int i2 ) {

			}

			@Override
			public void onTextChanged( CharSequence charSequence, int i, int i1, int i2 ) {
				if (coinList != null) {
					coinList.filter( charSequence.toString() );
					updateUI();
				}
			}

			@Override
			public void afterTextChanged( Editable editable ) {

			}
		};

		searchCoin.addTextChangedListener( searchWatcher );

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		searchCoin.removeTextChangedListener( searchWatcher );
		unbinder.unbind();

		if ( priceCall != null ) {
			priceCall.cancel();
		}
		if ( coinListCall != null ) {
			coinListCall.cancel();
		}
	}

	/**
	 * Update the ui with new data.
	 */
	private void updateUI() {
		loadCoin.setVisibility( View.GONE );
		adapter.setCoins( coinList.getCoins() );
		adapter.notifyDataSetChanged();
	}

	private void newPair( final String fromSymbol, final String fromName, final String toSymbol ) {
		priceCall = priceService.fetchPrice( fromSymbol, toSymbol );
		priceCall.enqueue( new Callback<SimplePrice>() {
			@Override
			public void onResponse( Call<SimplePrice> call, Response<SimplePrice> response ) {
				PairHelper.get( getContext() ).addPair( fromSymbol, fromName, toSymbol );

				LayoutInflater inflater = getActivity().getLayoutInflater();
				View layout = inflater.inflate(R.layout.toast_success,
						(ViewGroup) getActivity().findViewById(R.id.custom_toast_container));

				TextView text = (TextView) layout.findViewById(R.id.text);
				text.setText(
						getResources().getString(R.string.pair_creation_success, fromSymbol, toSymbol)
				);

				Toast toast = new Toast(getActivity().getApplicationContext());
				toast.setGravity( Gravity.BOTTOM | Gravity.CENTER, 0, 200);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setView(layout);
				toast.show();
			}

			@Override
			public void onFailure( Call<SimplePrice> call, Throwable t ) {
				Log.e( "", t.getMessage() );

				LayoutInflater inflater = getActivity().getLayoutInflater();
				View layout = inflater.inflate(R.layout.toast_error,
						(ViewGroup) getActivity().findViewById(R.id.custom_toast_container));

				TextView text = (TextView) layout.findViewById(R.id.text);
				text.setText(
						getResources().getString(R.string.pair_creation_failure, toSymbol, fromSymbol, toSymbol)
				);

				Toast toast = new Toast(getActivity().getApplicationContext());
				toast.setGravity( Gravity.BOTTOM | Gravity.CENTER, 0, 200);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setView(layout);
				toast.show();
			}
		} );
	}

	static class CoinHolder extends RecyclerView.ViewHolder {
		@BindView( R.id.coin_name ) public TextView value;
		@BindView( R.id.coin_image ) public ImageView image;

		private CoinHolder( View view ) {
			super( view );
			ButterKnife.bind(this, view);
		}
	}

	private class CoinAdapter extends RecyclerView.Adapter<CoinHolder> {
		private List<Coin> coins;

		public CoinAdapter( List<Coin> coins ) {
			this.coins = coins;
		}

		@Override
		public CoinHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
			View coinView = LayoutInflater.from( parent.getContext() )
					.inflate( R.layout.coin, parent, false );

			return new CoinHolder( coinView );
		}

		@Override
		public void onBindViewHolder( CoinHolder holder, int position ) {
			holder.value.setText( coins.get( position ).getName() );
			if ( !coins.get( position ).getImageUrl().equals( Coin.NO_URL ) ) {
				Picasso.with( getContext() ).load( coinList.getBaseUrl() + coins.get( position ).getImageUrl() ).into( holder.image );
			}

			final String symbol = coins.get( position ).getSymbol();
			final String name = coins.get( position ).getName();

			holder.itemView.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					if (priceCall != null) {
						priceCall.cancel();
					}

					LayoutInflater li = LayoutInflater.from( getContext() );
					View promptsView = li.inflate( R.layout.pair_prompt, null );

					AlertDialog.Builder alertDialogBuilder =
							new AlertDialog.Builder( getContext(), R.style.AlertDialogTheme );
					alertDialogBuilder.setView( promptsView );
					alertDialogBuilder.setCancelable( false );

					((TextView) promptsView.findViewById( R.id.from_symbol ))
							.setText( getResources().getString(R.string.pair_input_from_symbol, symbol) );

					final EditText toSymbolInput = (EditText) promptsView
							.findViewById( R.id.to_symbol_input );

					alertDialogBuilder.setPositiveButton( "OK", null );

					alertDialogBuilder.setNegativeButton(
							"Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick( DialogInterface dialog, int id ) {
									dialog.cancel();
								}
							} );

					final AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();

					// alert dialog hack to avoid being dismissed when input is invalid
					alertDialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener(
							new View.OnClickListener() {
								public void onClick(View onClick) {
									String input = toSymbolInput.getText().toString().trim();
									if ( input.contains(" ") ) {
										toSymbolInput.setError("No Spaces Allowed");
									} else if ( input.isEmpty() ) {
										toSymbolInput.setError("Enter a Symbol");
									} else {
										newPair( symbol, name, input );
										alertDialog.dismiss();
									}
								}
							}
					);
				}
			} );
		}

		@Override
		public int getItemCount() {
			return coins.size();
		}

		public void setCoins( List<Coin> coinList ) {
			this.coins = coinList;
		}
	}
}
