package com.chrislydic.monitor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
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
	public static final String PAIR_ARG = "pair";
	private static final String TAG = PriceAlertService.class.getSimpleName();
	private static final int NOTIFICATION_ID = 1;
	private JobParameters jobParams;
	private Alert alert;
	private Pair pair;

	@Override
	public boolean onStartJob( JobParameters job ) {
		jobParams = job;
		long alertId = job.getExtras().getLong( ALERT_ARG );
		alert = AlertHelper.get( getApplicationContext() ).getAlert( alertId );
		pair = PairHelper.get( getApplicationContext() ).getPair( alert.getPairId() );

		Gson gson =
				new GsonBuilder()
						.registerTypeAdapter(SimplePrice.class, new SimplePriceDeserializer())
						.create();

		Retrofit retrofitCryptoCompare = new Retrofit.Builder()
				.baseUrl(CryptoCompareAPI.URL)
				.addConverterFactory( GsonConverterFactory.create(gson))
				.build();

		CryptoCompareAPI priceService = retrofitCryptoCompare.create( CryptoCompareAPI.class );

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		String exchangePref = sharedPref.getString( "pref_exchange", getString( R.string.exchange_default ) );

		priceService.fetchPrice( pair.getFromSymbol(), pair.getToSymbol(), exchangePref ).enqueue( new PriceCallback() );

		return true;
	}

	@Override
	public boolean onStopJob( JobParameters job ) {
		return true;
	}

	private class PriceCallback implements Callback<SimplePrice> {
		@Override
		public void onResponse( Call<SimplePrice> call, Response<SimplePrice> response ) {
			double price = response.body().getValue();
			String description = null;

			if (alert.getPrevious() != -1d) {
				if (alert.getType() == Alert.PRICE_VALUE) {
					if (alert.getAction() != Alert.FALL_TO) {
						if ( price >= alert.getAmount() && alert.getPrevious() < alert.getAmount() ) {
							description = getString(
									R.string.price_alert_notification_description,
									pair.getFromSymbol(),
									getString( R.string.price_alert_notification_increased_to ),
									price,
									pair.getToSymbol()
							);
						}
					}

					if (alert.getAction() != Alert.RISE_TO) {
						if ( price <= alert.getAmount() && alert.getPrevious() > alert.getAmount() ) {
							description = getString(
									R.string.price_alert_notification_description,
									pair.getFromSymbol(),
									getString( R.string.price_alert_notification_decreased_to ),
									price,
									pair.getToSymbol()
							);
						}
					}
				} else {
					double percentChange = ( ( price - alert.getPrevious() ) / alert.getPrevious() ) * 100.0;

					if (alert.getAction() != Alert.FALL_TO && percentChange >= alert.getAmount() ) {
						description = getString(
								R.string.price_alert_notification_description_percent,
								pair.getFromSymbol(),
								getString( R.string.price_alert_notification_increased_by ),
								percentChange,
								price,
								pair.getToSymbol()
						);
					}

					if (alert.getAction() != Alert.RISE_TO && (-1 * percentChange) >= alert.getAmount() ) {
						description = getString(
								R.string.price_alert_notification_description_percent,
								pair.getFromSymbol(),
								getString( R.string.price_alert_notification_decreased_by ),
								percentChange,
								price,
								pair.getToSymbol()
						);
					}
				}
			}

			if (description != null) {
				String title = getString( R.string.price_alert_notification_title );

				// create a notification that will open the main activity when tapped
				NotificationCompat.Builder mBuilder =
						new NotificationCompat.Builder( getApplicationContext() )
								.setSmallIcon( R.mipmap.ic_launcher )
								.setContentTitle( title )
								.setContentText( description );

				Intent notificationIntent = new Intent( getApplicationContext(), MainActivity.class );

				notificationIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP );

				PendingIntent intent = PendingIntent
						.getActivity( getApplicationContext(), 0, notificationIntent, 0 );

				mBuilder.setContentIntent( intent );

				NotificationManager mNotifyMgr = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );
				mNotifyMgr.notify( NOTIFICATION_ID, mBuilder.build() );
			}

			AlertHelper.get( getApplicationContext() ).updatePrevious( alert.getId(), price );

			jobFinished( jobParams, false );
		}

		@Override
		public void onFailure( Call<SimplePrice> call, Throwable t ) {
			Log.e( "", t.getMessage() );
			jobFinished( jobParams, true );
		}
	}
}
