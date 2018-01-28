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

import com.chrislydic.monitor.database.AlertHelper;
import com.chrislydic.monitor.network.CryptoCompareAPI;
import com.chrislydic.monitor.network.History;
import com.chrislydic.monitor.network.HistoryDeserializer;
import com.chrislydic.monitor.network.Price;
import com.chrislydic.monitor.network.PriceDeserializer;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
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
	private static final String PRICE_ALERT_JOB = "com.chrislydic.monitor.pricealert";
	private static final String SELECTED_HISTORY = "com.chrislydic.monitor.history";
	private static final String COIN_TYPE = "ctype";
	private static final int HISTORY_HOUR = 0;
	private static final int HISTORY_DAY = 1;
	private static final int HISTORY_WEEK = 2;
	private static final int HISTORY_MONTH = 3;
	private static final int HISTORY_YEAR = 4;
	private static final int HISTORY_ALL = 5;
	private CryptoCompareAPI priceService;
	@BindView(R.id.price) protected TextView priceText;
	@BindView(R.id.percent) protected TextView percentText;
	@BindView(R.id.toggleHour) protected Button toggleHour;
	@BindView(R.id.toggleDay) protected Button toggleDay;
	@BindView(R.id.toggleWeek) protected Button toggleWeek;
	@BindView(R.id.toggleMonth) protected Button toggleMonth;
	@BindView(R.id.toggleYear) protected Button toggleYear;
	@BindView(R.id.toggleAll) protected Button toggleAll;
	@BindView(R.id.create_alert) protected Button createAlert;
	private LineDataSet historyData;
	@BindView(R.id.chart) protected LineChart priceChart;
	private Unbinder unbinder;
	private Pair coinType;
	private int selectedHistory;

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
						.registerTypeAdapter(Price.class, new PriceDeserializer())
						.registerTypeAdapter(History.class, new HistoryDeserializer())
						.create();

		Retrofit retrofitCryptoCompare = new Retrofit.Builder()
				.baseUrl(CryptoCompareAPI.URL)
				.addConverterFactory( GsonConverterFactory.create(gson))
				.build();

		priceService = retrofitCryptoCompare.create(CryptoCompareAPI.class);
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState ) {
		View view = inflater.inflate(R.layout.fragment_coin, container, false);
		unbinder = ButterKnife.bind(this, view);

		alerts = AlertHelper.get( getContext() ).getAlerts( coinType );
		adapter = new AlertAdapter( alerts );

		RecyclerView mOrderRecyclerView = (RecyclerView) view.findViewById( R.id.alert_recycler_view );
		mOrderRecyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );
		mOrderRecyclerView.setAdapter( adapter );

		createAlert.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				LayoutInflater li = LayoutInflater.from( getContext() );
				View promptsView = li.inflate( R.layout.alert_prompt, null );

				AlertDialog.Builder alertDialogBuilder =
						new AlertDialog.Builder( getContext() );
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
								String input = alertValue.getText().toString().trim();

								if ( input.isEmpty() ) {
									alertValue.setError("Enter a value");
								} else {
									String frequency = frequencySpinner.getSelectedItem().toString();
									int frequencyValue = 60;
									int alertType = Alert.PRICE_VALUE;
									int alertDirection = Alert.RISE_TO;

									switch ( frequency ) {
										case "10 minutes":
											frequencyValue = 10;
											break;
										case "15 minutes":
											frequencyValue = 15;
											break;
										case "30 minutes":
											frequencyValue = 30;
											break;
										case "45 minutes":
											frequencyValue = 45;
											break;
									}

									if ( typeRadioGroup.getCheckedRadioButtonId() == R.id.percent_option ) {
										alertType = Alert.PERCENT_VALUE;
									}
									if ( actionRadioGroup.getCheckedRadioButtonId() == R.id.fall_option ) {
										alertDirection = Alert.FALL_TO;
									} else if ( actionRadioGroup.getCheckedRadioButtonId() == R.id.change_option ) {
										alertDirection = Alert.CHANGE_TO;
									}

									AlertHelper
											.get( getContext() )
											.addAlert( alertDirection, Double.parseDouble( input ), coinType.getId(), alertType, frequencyValue );

									alerts = AlertHelper.get( getContext() ).getAlerts( coinType );
									updateUI();

									alertDialog.dismiss();
								}
							}
						}
				);
			}
		} );

		selectedHistory = HISTORY_DAY;
		if (savedInstanceState != null) {
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
			public void onClick(View v) {
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
			public void onClick(View v) {
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
			public void onClick(View v) {
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
			public void onClick(View v) {
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
			public void onClick(View v) {
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
			public void onClick(View v) {
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

		updatePriceData();

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

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();
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

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		String exchangePref = sharedPref.getString("pref_exchange", getString( R.string.exchange_default ));

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
				if (response.body() == null) {return;}
				final double price = response.body().price;
				priceText.setText( NumberFormat.getNumberInstance( Locale.getDefault() ).format( price ) );

				historyCall.enqueue( new Callback<History>() {
					@Override
					public void onResponse( Call<History> call, Response<History> response ) {
						Log.e( "", call.getClass().toString() );
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
						priceChart.getAxisLeft().setAxisMinimum(historyData.getYMin());
						priceChart.getAxisLeft().setAxisMaximum(historyData.getYMax());

						if ( selectedHistory == HISTORY_ALL ) {
							percentText.setVisibility( View.GONE );
						} else {
							percentText.setVisibility( View.VISIBLE );

							double oldPrice = response.body().getEntries().get( 0 ).getY();
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
						Log.e( "", t.getMessage() );
					}
				} );
			}

			@Override
			public void onFailure( Call<Price> call, Throwable t ) {
				Log.e( "", t.getMessage() );
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

	/**
	 * Create a new service that checks hourly if the price of bitcoin has
	 * fallen below priceFloor.
	 *
	 * @param alert new alert object
	 */
	private void createPriceAlert( Alert alert ) {
		FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher( new GooglePlayDriver( getContext() ) );

		// cancel the price alert job if it is running
		dispatcher.cancel( PRICE_ALERT_JOB );

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		boolean syncOnDataPref = sharedPref.getBoolean("pref_use_data", false);

		Bundle alertInfo = new Bundle();
		alertInfo.putSerializable( PriceAlertService.ALERT_ARG, alert );
		alertInfo.putSerializable( PriceAlertService.PAIR_ARG, coinType );

		// create a price alert job that is recurring, lasts forever (until this app kills it),
		//   and runs every x minutes provided there is network access
		Job.Builder alertBuilder = dispatcher.newJobBuilder()
				.setExtras( alertInfo )
				.setService( PriceAlertService.class )
				.setTag( String.valueOf( alert.getId() ) )
				.setRecurring( true )
				.setLifetime( Lifetime.FOREVER )
				.setTrigger( Trigger.executionWindow( ( alert.getFrequency() * 60 ), alert.getFrequency() * 60 + 100 ) )
				.setReplaceCurrent( true )
				.setRetryStrategy( RetryStrategy.DEFAULT_LINEAR )
				.setConstraints(
					Constraint.ON_ANY_NETWORK
				);

		if (!syncOnDataPref) {
			alertBuilder = alertBuilder.setConstraints(
					Constraint.ON_UNMETERED_NETWORK
				);
		}

		dispatcher.mustSchedule( alertBuilder.build() );
	}

	static class AlertHolder extends RecyclerView.ViewHolder {
		@BindView( R.id.alert_value ) public TextView value;
		@BindView( R.id.alert_menu ) public ImageButton menuButton;

		private AlertHolder( View view ) {
			super( view );
			ButterKnife.bind(this, view);
		}
	}

	private class AlertAdapter extends RecyclerView.Adapter<AlertHolder> {
		private List<Alert> alertList;

		public AlertAdapter( List<Alert> alerts ) {
			alertList = alerts;
		}

		@Override
		public AlertHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
			View alertView = LayoutInflater.from( parent.getContext() )
					.inflate( R.layout.price_alert, parent, false );

			return new AlertHolder( alertView );
		}

		@Override
		public void onBindViewHolder( final AlertHolder holder, int position ) {
			Alert alert = alertList.get( position );

			holder.value.setText( alert.getString( getContext() ) );

			holder.menuButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					PopupMenu popup = new PopupMenu(getContext(), view);
					popup.inflate( R.menu.menu_alert );

					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId()) {
								case R.id.item_alert_delete:
									AlertHelper
											.get( getContext() )
											.deleteAlert( alertList.get( holder.getAdapterPosition() ) );
									alerts = AlertHelper.get( getContext() ).getAlerts( coinType );
									updateUI();
									break;
							}
							return false;
						}
					});

					popup.show();
				}
			});
		}

		@Override
		public int getItemCount() {
			return alertList.size();
		}

		public void setAlerts( List<Alert> alertList ) {
			this.alertList = alertList;
		}
	}
}
