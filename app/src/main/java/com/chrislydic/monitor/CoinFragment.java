package com.chrislydic.monitor;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chrislydic.monitor.database.AlertHelper;
import com.chrislydic.monitor.network.CryptoCompareAPI;
import com.chrislydic.monitor.network.History;
import com.chrislydic.monitor.network.HistoryDeserializer;
import com.chrislydic.monitor.network.Price;
import com.chrislydic.monitor.network.PriceDeserializer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
public class CoinFragment extends Fragment {
	private static final String TAG = CoinFragment.class.getSimpleName();
	private static final String SELECTED_HISTORY = "com.chrislydic.monitor.history";
	private static final String COIN_TYPE = "ctype";
	private static final int HISTORY_HOUR = 0;
	private static final int HISTORY_DAY = 1;
	private static final int HISTORY_WEEK = 2;
	private static final int HISTORY_MONTH = 3;
	private static final int HISTORY_YEAR = 4;
	private static final int HISTORY_ALL = 5;
	private CryptoCompareAPI priceService;
	@BindView( R.id.price )
	protected TextView priceText;
	@BindView( R.id.percent )
	protected TextView percentText;
	@BindView( R.id.toggleHour )
	protected Button toggleHour;
	@BindView( R.id.toggleDay )
	protected Button toggleDay;
	@BindView( R.id.toggleWeek )
	protected Button toggleWeek;
	@BindView( R.id.toggleMonth )
	protected Button toggleMonth;
	@BindView( R.id.toggleYear )
	protected Button toggleYear;
	@BindView( R.id.toggleAll )
	protected Button toggleAll;
	private LineDataSet historyData;
	@BindView( R.id.chart )
	protected LineChart priceChart;
	private Unbinder unbinder;
	private Pair coinType;
	private int selectedHistory;
	private AlertDialog dialog;

	private List<Alert> alerts;
	private AlertAdapter adapter;

	private Call<Price> priceCall;
	private Call<History> historyCall;

	public CoinFragment() {
	}

	public static CoinFragment newInstance( Pair pair ) {
		CoinFragment fragment = new CoinFragment();
		Bundle args = new Bundle();
		args.putSerializable( COIN_TYPE, pair );
		fragment.setArguments( args );
		return fragment;
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		if ( getArguments() != null ) {
			coinType = (Pair) getArguments().getSerializable( COIN_TYPE );
		}

		Gson gson =
				new GsonBuilder()
						.registerTypeAdapter( Price.class, new PriceDeserializer() )
						.registerTypeAdapter( History.class, new HistoryDeserializer() )
						.create();

		Retrofit retrofitCryptoCompare = new Retrofit.Builder()
				.baseUrl( CryptoCompareAPI.URL )
				.addConverterFactory( GsonConverterFactory.create( gson ) )
				.build();

		priceService = retrofitCryptoCompare.create( CryptoCompareAPI.class );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState ) {
		View view = inflater.inflate( R.layout.fragment_coin, container, false );
		unbinder = ButterKnife.bind( this, view );

		alerts = AlertHelper.get( getContext() ).getAlerts( coinType );
		adapter = new AlertAdapter( alerts );

		RecyclerView mOrderRecyclerView = (RecyclerView) view.findViewById( R.id.alert_recycler_view );
		mOrderRecyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );
		mOrderRecyclerView.setAdapter( adapter );

		selectedHistory = HISTORY_DAY;
		if ( savedInstanceState != null ) {
			selectedHistory = savedInstanceState.getInt( SELECTED_HISTORY, HISTORY_DAY );
		}
		switch ( selectedHistory ) {
			case HISTORY_HOUR:
				toggleHour.setEnabled( false );
				break;
			case HISTORY_DAY:
				toggleDay.setEnabled( false );
				break;
			case HISTORY_WEEK:
				toggleWeek.setEnabled( false );
				break;
			case HISTORY_MONTH:
				toggleMonth.setEnabled( false );
				break;
			case HISTORY_YEAR:
				toggleYear.setEnabled( false );
				break;
			case HISTORY_ALL:
				toggleAll.setEnabled( false );
				break;
		}

		toggleHour.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				selectedHistory = HISTORY_HOUR;
				updatePriceData();

				toggleHour.setEnabled( false );

				toggleDay.setEnabled( true );
				toggleWeek.setEnabled( true );
				toggleMonth.setEnabled( true );
				toggleYear.setEnabled( true );
				toggleAll.setEnabled( true );
			}
		} );
		toggleDay.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				selectedHistory = HISTORY_DAY;
				updatePriceData();

				toggleDay.setEnabled( false );

				toggleHour.setEnabled( true );
				toggleWeek.setEnabled( true );
				toggleMonth.setEnabled( true );
				toggleYear.setEnabled( true );
				toggleAll.setEnabled( true );
			}
		} );
		toggleWeek.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				selectedHistory = HISTORY_WEEK;
				updatePriceData();

				toggleWeek.setEnabled( false );

				toggleHour.setEnabled( true );
				toggleDay.setEnabled( true );
				toggleMonth.setEnabled( true );
				toggleYear.setEnabled( true );
				toggleAll.setEnabled( true );
			}
		} );
		toggleMonth.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				selectedHistory = HISTORY_MONTH;
				updatePriceData();

				toggleMonth.setEnabled( false );

				toggleHour.setEnabled( true );
				toggleDay.setEnabled( true );
				toggleWeek.setEnabled( true );
				toggleYear.setEnabled( true );
				toggleAll.setEnabled( true );
			}
		} );
		toggleYear.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				selectedHistory = HISTORY_YEAR;
				updatePriceData();

				toggleYear.setEnabled( false );

				toggleHour.setEnabled( true );
				toggleDay.setEnabled( true );
				toggleWeek.setEnabled( true );
				toggleMonth.setEnabled( true );
				toggleAll.setEnabled( true );
			}
		} );
		toggleAll.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				selectedHistory = HISTORY_ALL;
				updatePriceData();

				toggleAll.setEnabled( false );

				toggleHour.setEnabled( true );
				toggleDay.setEnabled( true );
				toggleWeek.setEnabled( true );
				toggleMonth.setEnabled( true );
				toggleYear.setEnabled( true );
			}
		} );

		priceChart.getAxisRight().setEnabled( false );
		priceChart.getAxisLeft().setTextSize( 12 );
		priceChart.getAxisLeft().setTextColor( ContextCompat.getColor( getContext(), R.color.colorBackground ) );
		priceChart.getAxisLeft().setAxisLineColor( ContextCompat.getColor( getContext(), R.color.colorBackground ) );
		priceChart.getAxisLeft().setDrawGridLines( false );
		priceChart.getAxisLeft().setLabelCount( 3, true );
		priceChart.getXAxis().setPosition( XAxis.XAxisPosition.BOTTOM );
		priceChart.getXAxis().setTextSize( 12 );
		priceChart.getXAxis().setTextColor( ContextCompat.getColor( getContext(), R.color.colorBackground ) );
		priceChart.getXAxis().setAxisLineColor( ContextCompat.getColor( getContext(), R.color.colorBackground ) );
		priceChart.getXAxis().setDrawGridLines( false );
		priceChart.getXAxis().setLabelCount( 3, true );
		priceChart.getXAxis().setYOffset( 8f ); // fix y being cutoff
		priceChart.getXAxis().setAvoidFirstLastClipping( true );
		priceChart.getXAxis().setGranularity( 1f );
		priceChart.getLegend().setEnabled( false );
		priceChart.getDescription().setEnabled( false );
		priceChart.setDragEnabled( false );
		priceChart.setPinchZoom( false );
		priceChart.setScaleEnabled( false );
		priceChart.setDoubleTapToZoomEnabled( false );

		if ( selectedHistory == HISTORY_HOUR || selectedHistory == HISTORY_DAY ) {
			priceChart.getXAxis().setValueFormatter( new DateFormatterHour() );
		} else if ( selectedHistory == HISTORY_ALL ) {
			priceChart.getXAxis().setValueFormatter( new DateFormatterYear() );
		} else {
			priceChart.getXAxis().setValueFormatter( new DateFormatterDay() );
		}

		updatePriceData();

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();

		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}

		if ( priceCall != null ) {
			priceCall.cancel();
		}
		if ( historyCall != null ) {
			historyCall.cancel();
		}
	}

	@Override
	public void onSaveInstanceState( Bundle outState ) {
		super.onSaveInstanceState( outState );
		outState.putInt( SELECTED_HISTORY, selectedHistory );
	}

	@Override
	public void setUserVisibleHint( boolean isVisibleToUser ) {
		super.setUserVisibleHint( isVisibleToUser );
	}

	private void updatePriceData() {
		if ( historyCall != null ) {
			historyCall.cancel();
		}
		if ( priceCall != null ) {
			priceCall.cancel();
		}

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( getContext() );
		String exchangePref = sharedPref.getString( "pref_exchange", getString( R.string.exchange_default ) );

		priceCall = priceService.fetchPriceData( coinType.getFromSymbol(), coinType.getToSymbol(), exchangePref );

		if ( selectedHistory == HISTORY_HOUR ) {
			historyCall = priceService.fetchHistoMinute( coinType.getFromSymbol(), coinType.getToSymbol(), CryptoCompareAPI.HOUR, exchangePref );
		} else if ( selectedHistory == HISTORY_DAY ) {
			historyCall = priceService.fetchHistoMinute( coinType.getFromSymbol(), coinType.getToSymbol(), CryptoCompareAPI.DAY, exchangePref );
		} else if ( selectedHistory == HISTORY_WEEK ) {
			historyCall = priceService.fetchHistoHour( coinType.getFromSymbol(), coinType.getToSymbol(), CryptoCompareAPI.WEEK, exchangePref );
		} else if ( selectedHistory == HISTORY_MONTH ) {
			historyCall = priceService.fetchHistoHour( coinType.getFromSymbol(), coinType.getToSymbol(), CryptoCompareAPI.MONTH, exchangePref );
		} else if ( selectedHistory == HISTORY_YEAR ) {
			historyCall = priceService.fetchHistoDay( coinType.getFromSymbol(), coinType.getToSymbol(), CryptoCompareAPI.YEAR, exchangePref );
		} else {
			historyCall = priceService.fetchAllHistory( coinType.getFromSymbol(), coinType.getToSymbol(), exchangePref );
		}

		priceCall.enqueue( new Callback<Price>() {
			@Override
			public void onResponse( Call<Price> call, Response<Price> response ) {
				if ( response.body() == null || !isAdded() ) {
					return;
				}
				adapter.setDisableFooter( false );

				final double price = response.body().price;
				priceText.setText( NumberFormat.getNumberInstance( Locale.getDefault() ).format( price ) );

				historyCall.enqueue( new Callback<History>() {
					@Override
					public void onResponse( Call<History> call, Response<History> response ) {
						if ( response.body() == null || !isAdded() ) {
							return;
						}

						historyData = new LineDataSet( response.body().getEntries(), "price" );
						historyData.setColor( ContextCompat.getColor( getContext(), R.color.positive ) );
						historyData.setFillColor( ContextCompat.getColor( getContext(), R.color.positive ) );
						historyData.setLineWidth( 2f );
						historyData.setDrawValues( false );
						historyData.setMode( LineDataSet.Mode.LINEAR );
						historyData.setDrawFilled( true );
						historyData.setDrawCircles( false );
						historyData.setDrawHorizontalHighlightIndicator( false );
						historyData.setHighLightColor( ContextCompat.getColor( getActivity(), R.color.colorBackground ) );
						priceChart.getAxisLeft().setAxisMinimum( historyData.getYMin() );
						priceChart.getAxisLeft().setAxisMaximum( historyData.getYMax() );

						double oldPrice = response.body().getEntries().get( 0 ).getY();

						if ( selectedHistory == HISTORY_ALL || oldPrice == 0 ) {
							percentText.setVisibility( View.GONE );
						} else {
							percentText.setVisibility( View.VISIBLE );
							double percentChange = ( ( price - oldPrice ) / oldPrice ) * 100.0;

							percentText.setText( String.format(
									Locale.getDefault(),
									" %s%.2f%%",
									percentChange > 0.0 ? "+" : "",
									percentChange
							) );

							if ( percentChange > 0.0 ) {
								percentText.setTextColor( ContextCompat.getColor( getContext(), R.color.positive ) );
							} else {
								percentText.setTextColor( ContextCompat.getColor( getContext(), R.color.negative ) );
							}
						}

						updateUI();
					}

					@Override
					public void onFailure( Call<History> call, Throwable t ) {
						Log.e( TAG, t.getMessage() );

						if (isAdded()) {
							Toast.makeText(
									getContext(),
									getResources().getString( R.string.data_load_error ),
									Toast.LENGTH_SHORT
							).show();
						}
					}
				} );
			}

			@Override
			public void onFailure( Call<Price> call, Throwable t ) {
				Log.e( TAG, t.getMessage() );

				if ( t.getMessage().equals( "Coin unavailable" ) ) {
					if (isAdded()) {
						Toast.makeText(
								getContext(),
								getResources().getString( R.string.coin_retrieve_error ),
								Toast.LENGTH_SHORT
						).show();

						priceText.setText( getResources().getString( R.string.price_empty ) );

						adapter.setDisableFooter( true );
					}
				} else {
					if (isAdded()) {
						Toast.makeText(
								getContext(),
								getResources().getString( R.string.data_load_error ),
								Toast.LENGTH_SHORT
						).show();

						priceText.setText( getResources().getString( R.string.price_empty ) );
					}
				}
			}
		} );
	}

	/**
	 * Update the ui with new data.
	 */
	private void updateUI() {
		LineData lineData = new LineData( historyData );
		priceChart.setData( lineData );
		priceChart.invalidate();

		if ( selectedHistory == HISTORY_HOUR || selectedHistory == HISTORY_DAY ) {
			priceChart.getXAxis().setValueFormatter( new DateFormatterHour() );
		} else if ( selectedHistory == HISTORY_ALL ) {
			priceChart.getXAxis().setValueFormatter( new DateFormatterYear() );
		} else {
			priceChart.getXAxis().setValueFormatter( new DateFormatterDay() );
		}

		adapter.setAlerts( alerts );
		adapter.notifyDataSetChanged();
	}

	/**
	 * Class for displaying the date time values on the x axis properly.
	 */
	private class DateFormatterDay implements IAxisValueFormatter {
		@Override
		public String getFormattedValue( float value, AxisBase axis ) {
			SimpleDateFormat dateFormat = new SimpleDateFormat( "MMM dd", Locale.getDefault() );
			Date date = new Date( (long) value * 1000 );
			String formattedDate = dateFormat.format( date );
			formattedDate = formattedDate.split( " " )[0] + " " +
					Integer.toString( Integer.parseInt( formattedDate.split( " " )[1] ) );
			return formattedDate;
		}
	}

	/**
	 * Class for displaying the date time values on the x axis properly.
	 */
	private class DateFormatterHour implements IAxisValueFormatter {
		@Override
		public String getFormattedValue( float value, AxisBase axis ) {
			SimpleDateFormat dateFormat = new SimpleDateFormat( "hh:mm a", Locale.getDefault() );
			Date date = new Date( (long) value * 1000 );
			String formattedDate = dateFormat.format( date );
			formattedDate = Integer.toString( Integer.parseInt( formattedDate.split( ":" )[0] ) )
					+ ":" + formattedDate.split( ":" )[1];
			return formattedDate;
		}
	}

	/**
	 * Class for displaying the date time values on the x axis properly.
	 */
	private class DateFormatterYear implements IAxisValueFormatter {
		@Override
		public String getFormattedValue( float value, AxisBase axis ) {
			SimpleDateFormat dateFormat = new SimpleDateFormat( "MMM YYYY", Locale.getDefault() );
			Date date = new Date( (long) value * 1000 );
			return dateFormat.format( date );
		}
	}

	static class AlertHolder extends RecyclerView.ViewHolder {
		@BindView( R.id.alert_value )
		public TextView value;
		@BindView( R.id.alert_menu )
		public ImageButton menuButton;

		private AlertHolder( View view ) {
			super( view );
			ButterKnife.bind( this, view );
		}
	}

	static class CreateHolder extends RecyclerView.ViewHolder {
		@BindView( R.id.create_alert )
		public Button createAlert;

		private CreateHolder( View view ) {
			super( view );
			ButterKnife.bind( this, view );
		}
	}

	private class AlertAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final int TYPE_ITEM = 0;
		private static final int TYPE_FOOTER = 1;

		private List<Alert> alertList;
		private boolean disableFooter;

		public AlertAdapter( List<Alert> alerts ) {
			alertList = alerts;
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
			if ( viewType == TYPE_ITEM ) {
				View alertView = LayoutInflater.from( parent.getContext() )
						.inflate( R.layout.price_alert, parent, false );

				return new AlertHolder( alertView );
			} else {
				View footerView = LayoutInflater.from( parent.getContext() )
						.inflate( R.layout.price_footer, parent, false );

				return new CreateHolder( footerView );
			}
		}

		@Override
		public void onBindViewHolder( final RecyclerView.ViewHolder genericHolder, int position ) {
			if ( position >= alertList.size() ) {
				CreateHolder holder = (CreateHolder) genericHolder;
				holder.createAlert.setOnClickListener( new AlertDialogListener() );

				if (disableFooter ) {
					holder.createAlert.setEnabled( false );
				}
			} else {
				Alert alert = alertList.get( position );
				AlertHolder holder = (AlertHolder) genericHolder;
				setupAlertHolder( holder, alert );
			}
		}

		@Override
		public int getItemCount() {
			// there is always a price_footer
			return alertList.size() + 1;
		}

		public void setAlerts( List<Alert> alertList ) {
			this.alertList = alertList;
		}

		@Override
		public int getItemViewType( int position ) {
			if ( position >= alertList.size() ) {
				return TYPE_FOOTER;
			}
			return TYPE_ITEM;
		}

		public void setDisableFooter( boolean disableFooter ) {
			this.disableFooter = disableFooter;
			notifyDataSetChanged();
		}

		private void setupAlertHolder( final AlertHolder holder, Alert alert ) {
			holder.value.setText( alert.getString( getContext() ) );

			if ( !alert.isEnabled() ) {
				holder.value.setTextColor(
						ContextCompat.getColor( getContext(), R.color.colorLightGray ) );
			} else {
				holder.value.setTextColor(
						ContextCompat.getColor( getContext(), R.color.colorBackground ) );
			}

			holder.menuButton.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					PopupMenu popup = new PopupMenu( getContext(), view );
					popup.inflate( R.menu.menu_alert );

					if ( alertList.get( holder.getAdapterPosition() ).isEnabled() ) {
						popup.getMenu().findItem( R.id.item_alert_enable ).setVisible( false );
					} else {
						popup.getMenu().findItem( R.id.item_alert_disable ).setVisible( false );
					}

					popup.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick( MenuItem item ) {
							switch ( item.getItemId() ) {
								case R.id.item_alert_enable:
									AlertHelper
											.get( getContext() )
											.updateEnabled( alertList.get( holder.getAdapterPosition() ).getId(), true );

									alerts.get( holder.getAdapterPosition() ).setEnabled( true );
									alerts.get( holder.getAdapterPosition() ).createPriceAlert( getContext() );

									updateUI();
									break;
								case R.id.item_alert_disable:
									AlertHelper
											.get( getContext() )
											.updateEnabled( alertList.get( holder.getAdapterPosition() ).getId(), false );

									alerts.get( holder.getAdapterPosition() ).setEnabled( false );
									alerts.get( holder.getAdapterPosition() ).cancelPriceAlert( getContext() );

									updateUI();
									break;
								case R.id.item_alert_delete:
									AlertHelper
											.get( getContext() )
											.deleteAlert( alertList.get( holder.getAdapterPosition() ), getContext() );

									alerts.remove( holder.getAdapterPosition() );
									updateUI();
									break;
							}
							return false;
						}
					} );

					popup.show();
				}
			} );
		}
	}

	private class AlertDialogListener implements View.OnClickListener {
		public void onClick( View v ) {
			LayoutInflater li = LayoutInflater.from( v.getContext() );
			View promptsView = li.inflate( R.layout.alert_prompt, null );

			AlertDialog.Builder alertDialogBuilder =
					new AlertDialog.Builder( v.getContext() );
			alertDialogBuilder.setView( promptsView );
			alertDialogBuilder.setCancelable( false );

			final TextView alertActionText = (TextView) promptsView
					.findViewById( R.id.alert_value_action );
			final TextView alertTypeText = (TextView) promptsView
					.findViewById( R.id.alert_value_type );
			final RadioGroup typeRadioGroup = (RadioGroup) promptsView
					.findViewById( R.id.type_buttons );
			final RadioGroup actionRadioGroup = (RadioGroup) promptsView
					.findViewById( R.id.action_buttons );
			final EditText alertValue = (EditText) promptsView
					.findViewById( R.id.alert_value_input );
			final Spinner frequencySpinner = (Spinner) promptsView
					.findViewById( R.id.alert_frequency );

			actionRadioGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged( RadioGroup radioGroup, @IdRes int i ) {
					if ( i == R.id.rise_option ) {
						alertActionText.setText( R.string.enter_value_rise_action );
					} else if ( i == R.id.fall_option ) {
						alertActionText.setText( R.string.enter_value_fall_action );
					} else {
						alertActionText.setText( R.string.enter_value_change_action );
					}
				}
			} );
			typeRadioGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged( RadioGroup radioGroup, @IdRes int i ) {
					if ( i == R.id.price_option ) {
						alertTypeText.setText( R.string.enter_value_price_type );
					} else {
						alertTypeText.setText( R.string.enter_value_percent_type );
					}
				}
			} );

			alertDialogBuilder.setPositiveButton( "OK", null );

			alertDialogBuilder.setNegativeButton(
					"Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick( DialogInterface dialogIn, int id ) {
							dialogIn.cancel();
							dialog = null;
						}
					} );

			if ( dialog != null ) {
				dialog.dismiss();
				dialog = null;
			}
			dialog = alertDialogBuilder.create();
			dialog.show();

			// alert dialog hack to avoid being dismissed when input is invalid
			dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener(
					new View.OnClickListener() {
						public void onClick( View onClick ) {
							String input = alertValue.getText().toString().trim();

							if ( input.isEmpty() ) {
								alertValue.setError( "Enter a value" );
							} else {
								String frequency = frequencySpinner.getSelectedItem().toString();
								int frequencyValue = 3600;
								int alertType = Alert.PRICE_VALUE;
								int alertDirection = Alert.RISE_TO;
								String[] frequencies = onClick.getResources().getStringArray( R.array.frequencies );

								for ( int i = 0; i < frequencies.length; i++ ) {
									if ( frequencies[i].equals( frequency ) ) {
										frequencyValue = Alert.FREQ_VALUES[i];
										break;
									}
								}

								if ( typeRadioGroup.getCheckedRadioButtonId() == R.id.percent_option ) {
									alertType = Alert.PERCENT_VALUE;
								}
								if ( actionRadioGroup.getCheckedRadioButtonId() == R.id.fall_option ) {
									alertDirection = Alert.FALL_TO;
								} else if ( actionRadioGroup.getCheckedRadioButtonId() == R.id.change_option ) {
									alertDirection = Alert.CHANGE_TO;
								}

								Alert alert = AlertHelper
										.get( onClick.getContext() )
										.addAlert(
												alertDirection,
												Double.parseDouble( input ),
												coinType.getId(),
												alertType,
												frequencyValue
										);
// TODO handle double parse error
								alert.createPriceAlert( onClick.getContext() );
								alerts.add( alert );
								updateUI();

								dialog.dismiss();
								dialog = null;
							}
						}
					}
			);
		}
	}
}