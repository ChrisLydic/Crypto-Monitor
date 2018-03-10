package com.chrislydic.monitor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.chrislydic.monitor.database.AlertHelper;
import com.chrislydic.monitor.database.PairHelper;
import com.chrislydic.monitor.network.CryptoCompareAPI;
import com.chrislydic.monitor.network.SimplePrice;
import com.chrislydic.monitor.network.SimplePriceDeserializer;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 */
public class PriceAlertService extends JobService {
	public static final String ALERT_ARG = "alert";
	private static final String TAG = PriceAlertService.class.getSimpleName();
	private static final int NOTIFICATION_ID = 1;
//	private JobParameters jobParams;
	private Call<SimplePrice> priceCall;
//	private Alert alert;
//	private Pair pair;

	@Override
	public boolean onStartJob( final JobParameters jobParams ) {
		//JobParameters jobParams = job;
		long alertId = jobParams.getExtras().getLong( ALERT_ARG );
		final Alert alert = AlertHelper.get( getApplicationContext() ).getAlert( alertId );
		final Pair pair = PairHelper.get( getApplicationContext() ).getPair( alert.getPairId() );

		Gson gson =
				new GsonBuilder()
						.registerTypeAdapter( SimplePrice.class, new SimplePriceDeserializer() )
						.create();

		Retrofit retrofitCryptoCompare = new Retrofit.Builder()
				.baseUrl( CryptoCompareAPI.URL )
				.addConverterFactory( GsonConverterFactory.create( gson ) )
				.build();

		CryptoCompareAPI priceService = retrofitCryptoCompare.create( CryptoCompareAPI.class );

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		String exchangePref = sharedPref.getString( "pref_exchange", getString( R.string.exchange_default ) );

		if (priceCall != null) {
			priceCall.cancel();
		}
		priceCall = priceService.fetchPrice( pair.getFromSymbol(), pair.getToSymbol(), exchangePref );
		priceCall.enqueue( new Callback<SimplePrice>() {
			@Override
			public void onResponse( Call<SimplePrice> call, Response<SimplePrice> response ) {
				double price = response.body().getValue();
				double percentChange = ( ( price - alert.getPrevious() ) / alert.getPrevious() ) * 100.0;
				String priceDisplay = String.format( Locale.getDefault(), "%.2f", price );
				String percentChangeDisplay = String.format( Locale.getDefault(), "%.2f", percentChange );
				String description = null;

				String[] frequencies = getResources().getStringArray( R.array.frequenciesNotification );
				String time = "hour";

				for ( int i = 0; i < Alert.FREQ_VALUES.length; i++ ) {
					if ( Alert.FREQ_VALUES[i] == alert.getFrequency() ) {
						time = frequencies[i];
						break;
					}
				}

				if ( alert.getPrevious() != -1d ) {
					if ( alert.getType() == Alert.PRICE_VALUE ) {
						if ( alert.getAction() != Alert.FALL_TO ) {
							if ( price >= alert.getAmount() && alert.getPrevious() < alert.getAmount() ) {
								description = getString(
										R.string.price_alert_notification_description,
										pair.getFromName(),
										getString( R.string.price_alert_notification_increased_to ),
										priceDisplay,
										pair.getToSymbol(),
										time
								);
							}
						}

						if ( alert.getAction() != Alert.RISE_TO ) {
							if ( price <= alert.getAmount() && alert.getPrevious() > alert.getAmount() ) {
								description = getString(
										R.string.price_alert_notification_description,
										pair.getFromName(),
										getString( R.string.price_alert_notification_decreased_to ),
										priceDisplay,
										pair.getToSymbol(),
										time
								);
							}
						}
					} else {
						if ( alert.getAction() != Alert.FALL_TO && percentChange >= alert.getAmount() ) {
							description = getString(
									R.string.price_alert_notification_description_percent,
									pair.getFromName(),
									getString( R.string.price_alert_notification_increased_by ),
									percentChangeDisplay,
									priceDisplay,
									pair.getToSymbol(),
									time
							);
						}

						if ( alert.getAction() != Alert.RISE_TO && ( -1 * percentChange ) >= alert.getAmount() ) {
							description = getString(
									R.string.price_alert_notification_description_percent,
									pair.getFromName(),
									getString( R.string.price_alert_notification_decreased_by ),
									percentChangeDisplay,
									priceDisplay,
									pair.getToSymbol(),
									time
							);
						}
					}
				}

				AlertHelper.get( getApplicationContext() ).updatePrevious( alert.getId(), price );

				if ( description != null ) {
					String title = getString(
							R.string.price_alert_notification_title,
							pair,
							String.format( Locale.getDefault(), "%.2f", price ),
							percentChange > 0.0 ? "+" : "",
							String.format( Locale.getDefault(), "%.2f", percentChange )
					);
					//TODO remove? String now = new SimpleDateFormat("ddHHmmss",  Locale.US).format(new Date());

					// create a notification that will open the main activity when tapped
					NotificationCompat.Builder builder =
							new NotificationCompat.Builder( getApplicationContext() )
									.setVisibility( NotificationCompat.VISIBILITY_PUBLIC )
									.setVibrate( new long[]{0, 500} )
									.setSound( Settings.System.DEFAULT_NOTIFICATION_URI )
									.setAutoCancel( true )
									.setSmallIcon( R.mipmap.ic_launcher )
									.setContentTitle( title )
									.setStyle( new NotificationCompat.BigTextStyle()
											.bigText( description ) );

					Intent notificationIntent = new Intent( getApplicationContext(), MainActivity.class );

					TaskStackBuilder stackBuilder = TaskStackBuilder.create( getApplicationContext() );
					stackBuilder.addParentStack( MainActivity.class );
					stackBuilder.addNextIntent( notificationIntent );
					PendingIntent resultPendingIntent =
							stackBuilder.getPendingIntent(
									0,
									PendingIntent.FLAG_UPDATE_CURRENT
							);
					builder.setContentIntent( resultPendingIntent );

					NotificationManager notificationManager =
							(NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );

					notificationManager.notify(
							"com.chrislydic.monitor." + alert.getId(),
							NOTIFICATION_ID,
							builder.build()
					);
				} else {
					//jobFinished( jobParams, false ); end notification
				}

				jobFinished( jobParams, true );
			}

			@Override
			public void onFailure( Call<SimplePrice> call, Throwable t ) {
				Log.e( TAG, t.getMessage() );
				jobFinished( jobParams, true );
			}
		} );

		return true;
	}

	@Override
	public boolean onStopJob( JobParameters job ) {
		if ( priceCall != null ) {
			priceCall.cancel();
		}
		return true;
	}
//
//	private class PriceCallback implements Callback<SimplePrice> {
//		@Override
//		public void onResponse( Call<SimplePrice> call, Response<SimplePrice> response ) {
//			double price = response.body().getValue();
//			double percentChange = ( ( price - alert.getPrevious() ) / alert.getPrevious() ) * 100.0;
//			String priceDisplay = String.format( Locale.getDefault(), "%.2f", price );
//			String percentChangeDisplay = String.format( Locale.getDefault(), "%.2f", percentChange );
//			String description = null;
//
//			String[] frequencies = getResources().getStringArray( R.array.frequenciesNotification );
//			String time = "hour";
//
//			for ( int i = 0; i < Alert.FREQ_VALUES.length; i++ ) {
//				if ( Alert.FREQ_VALUES[i] == alert.getFrequency() ) {
//					time = frequencies[i];
//					break;
//				}
//			}
//
//			if ( alert.getPrevious() != -1d ) {
//				if ( alert.getType() == Alert.PRICE_VALUE ) {
//					if ( alert.getAction() != Alert.FALL_TO ) {
//						if ( price >= alert.getAmount() && alert.getPrevious() < alert.getAmount() ) {
//							description = getString(
//									R.string.price_alert_notification_description,
//									pair.getFromName(),
//									getString( R.string.price_alert_notification_increased_to ),
//									priceDisplay,
//									pair.getToSymbol(),
//									time
//							);
//						}
//					}
//
//					if ( alert.getAction() != Alert.RISE_TO ) {
//						if ( price <= alert.getAmount() && alert.getPrevious() > alert.getAmount() ) {
//							description = getString(
//									R.string.price_alert_notification_description,
//									pair.getFromName(),
//									getString( R.string.price_alert_notification_decreased_to ),
//									priceDisplay,
//									pair.getToSymbol(),
//									time
//							);
//						}
//					}
//				} else {
//					if ( alert.getAction() != Alert.FALL_TO && percentChange >= alert.getAmount() ) {
//						description = getString(
//								R.string.price_alert_notification_description_percent,
//								pair.getFromName(),
//								getString( R.string.price_alert_notification_increased_by ),
//								percentChangeDisplay,
//								priceDisplay,
//								pair.getToSymbol(),
//								time
//						);
//					}
//
//					if ( alert.getAction() != Alert.RISE_TO && ( -1 * percentChange ) >= alert.getAmount() ) {
//						description = getString(
//								R.string.price_alert_notification_description_percent,
//								pair.getFromName(),
//								getString( R.string.price_alert_notification_decreased_by ),
//								percentChangeDisplay,
//								priceDisplay,
//								pair.getToSymbol(),
//								time
//						);
//					}
//				}
//			}
//
//			AlertHelper.get( getApplicationContext() ).updatePrevious( alert.getId(), price );
//
//			if ( description != null ) {
//				String title = getString(
//						R.string.price_alert_notification_title,
//						pair,
//						String.format( Locale.getDefault(), "%.2f", price ),
//						percentChange > 0.0 ? "+" : "",
//						String.format( Locale.getDefault(), "%.2f", percentChange )
//				);
//				//TODO remove? String now = new SimpleDateFormat("ddHHmmss",  Locale.US).format(new Date());
//
//				// create a notification that will open the main activity when tapped
//				NotificationCompat.Builder builder =
//						new NotificationCompat.Builder( getApplicationContext() )
//								.setVisibility( NotificationCompat.VISIBILITY_PUBLIC )
//								.setVibrate( new long[]{0, 500} )
//								.setSound( Settings.System.DEFAULT_NOTIFICATION_URI )
//								.setAutoCancel( true )
//								.setSmallIcon( R.mipmap.ic_launcher )
//								.setContentTitle( title )
//								.setStyle( new NotificationCompat.BigTextStyle()
//										.bigText( description ) );
//
//				Intent notificationIntent = new Intent( getApplicationContext(), MainActivity.class );
//
//				TaskStackBuilder stackBuilder = TaskStackBuilder.create( getApplicationContext() );
//				stackBuilder.addParentStack( MainActivity.class );
//				stackBuilder.addNextIntent( notificationIntent );
//				PendingIntent resultPendingIntent =
//						stackBuilder.getPendingIntent(
//								0,
//								PendingIntent.FLAG_UPDATE_CURRENT
//						);
//				builder.setContentIntent( resultPendingIntent );
//
//				NotificationManager notificationManager =
//						(NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
//
//				notificationManager.notify(
//						"com.chrislydic.monitor." + alert.getId(),
//						NOTIFICATION_ID,
//						builder.build()
//				);
//			} else {
//				//jobFinished( jobParams, false ); end notification
//			}
//
//			jobFinished( jobParams, true );
//		}
//
//		@Override
//		public void onFailure( Call<SimplePrice> call, Throwable t ) {
//			Log.e( TAG, t.getMessage() );
//			jobFinished( jobParams, true );
//		}
//	}
}
