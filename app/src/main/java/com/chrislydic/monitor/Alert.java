package com.chrislydic.monitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.io.Serializable;
import java.util.Locale;

/**
 * An entry in an order book.
 */
public class Alert implements Serializable {
	public static final String PRICE_ALERT_JOB = "com.chrislydic.monitor.pricealert.";
	public static final int[] FREQ_VALUES = new int[]{3600, 10800, 21600, 43200, 86400};
	public static final int RISE_TO = 0;
	public static final int FALL_TO = 1;
	public static final int CHANGE_TO = 2;
	public static final int PRICE_VALUE = 0;
	public static final int PERCENT_VALUE = 1;
	public static final int DELAY = 60;
	private long id;
	private int type;
	private double amount;
	private int action;
	private long pairId;
	private int frequency;
	private boolean enabled;
	private boolean active;

	public Alert( long id, int type, double amount, int action, long pairId, int frequency, boolean enabled, boolean active ) {
		this.id = id;
		this.type = type;
		this.amount = amount;
		this.action = action;
		this.pairId = pairId;
		this.frequency = frequency;
		this.enabled = enabled;
		this.active = active;
	}

	public Alert( long id, int type, double amount, int action, long pairId, int frequency ) {
		this( id, type, amount, action, pairId, frequency, true, false );
	}

	public String getString( Context ctx ) {
		String[] frequencies = ctx.getResources().getStringArray( R.array.frequencies );
		String time = frequencies[0];

		for ( int i = 0; i < Alert.FREQ_VALUES.length; i++ ) {
			if ( Alert.FREQ_VALUES[i] == frequency ) {
				time = frequencies[i];
				break;
			}
		}

		if (type == PRICE_VALUE) {
			if (action == RISE_TO) {
				return ctx.getResources().getString(
						R.string.alert_rise_to,
						String.format( Locale.getDefault(), "%.2f", amount ),
						time
				);
			} else if (action == FALL_TO) {
				return ctx.getResources().getString(
						R.string.alert_fall_to,
						String.format( Locale.getDefault(), "%.2f", amount ),
						time
				);
			} else {
				return ctx.getResources().getString(
						R.string.alert_change_to,
						String.format( Locale.getDefault(), "%.2f", amount ),
						time
				);
			}
		} else {
			if (action == RISE_TO) {
				return ctx.getResources().getString(
						R.string.alert_rise_by,
						String.format( Locale.getDefault(), "%.2f", amount ),
						time
				);
			} else if (action == FALL_TO) {
				return ctx.getResources().getString(
						R.string.alert_fall_by,
						String.format( Locale.getDefault(), "%.2f", amount ),
						time
				);
			} else {
				return ctx.getResources().getString(
						R.string.alert_change_by,
						String.format( Locale.getDefault(), "%.2f", amount ),
						time
				);
			}
		}
	}

	public long getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public double getAmount() {
		return amount;
	}

	public int getAction() {
		return action;
	}

	public long getPairId() {
		return pairId;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public boolean isActive() {
		return active;
	}


	/**
	 * Create a new service that checks hourly if the price of bitcoin has
	 * fallen below priceFloor.
	 *
	 * @param context
	 */
	public void createPriceAlert( Context context ) {
		FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher( new GooglePlayDriver( context ) );

		dispatcher.cancel( PRICE_ALERT_JOB + String.valueOf( id ) );

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( context );
		boolean syncOnDataPref = sharedPref.getBoolean( "pref_use_data", false );

		Bundle alertInfo = new Bundle();
		alertInfo.putLong( PriceAlertService.ALERT_ARG, id );

		// create a price alert job that is recurring, lasts forever (until this app kills it),
		//   and runs every x minutes provided there is network access
		Job.Builder alertBuilder = dispatcher.newJobBuilder()
				.setService( PriceAlertService.class )
				.setTag( PRICE_ALERT_JOB + String.valueOf( id ) )
				.setRecurring( true )
				.setLifetime( Lifetime.FOREVER )
				.setExtras( alertInfo )
				.setTrigger(
						Trigger.executionWindow( frequency, frequency + DELAY ) )
				.setReplaceCurrent( true )
				.setRetryStrategy( RetryStrategy.DEFAULT_LINEAR )
				.setConstraints(
						Constraint.ON_ANY_NETWORK
				);

		if ( !syncOnDataPref ) {
			alertBuilder = alertBuilder.setConstraints(
					Constraint.ON_UNMETERED_NETWORK
			);
		}

		dispatcher.mustSchedule( alertBuilder.build() );
	}

	public void cancelPriceAlert( Context context ) {
		FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher( new GooglePlayDriver( context ) );
		dispatcher.cancel( PRICE_ALERT_JOB + String.valueOf( id ) );
	}
}

