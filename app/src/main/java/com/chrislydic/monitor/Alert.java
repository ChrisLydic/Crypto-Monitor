package com.chrislydic.monitor;

import android.content.Context;

import java.io.Serializable;
import java.util.Locale;

/**
 * An entry in an order book.
 */
public class Alert implements Serializable {
	public static final int[] FREQ_VALUES = new int[]{300, 60, 900, 1800, 3600, 10800, 21600, 43200, 86400};
	public static final int RISE_TO = 0;
	public static final int FALL_TO = 1;
	public static final int CHANGE_TO = 2;
	public static final int PRICE_VALUE = 0;
	public static final int PERCENT_VALUE = 1;
	private long id;
	private int type;
	private double amount;
	private double previous;
	private int action;
	private long pairId;
	private int frequency;
	private boolean enabled;
	private boolean active;

	public Alert( long id, int type, double amount, double previous, int action, long pairId, int frequency, boolean enabled, boolean active ) {
		this.id = id;
		this.type = type;
		this.amount = amount;
		this.previous = previous;
		this.action = action;
		this.pairId = pairId;
		this.frequency = frequency;
		this.enabled = enabled;
		this.active = active;
	}

	public Alert( long id, int type, double amount, double previous, int action, long pairId, int frequency ) {
		this( id, type, amount, previous, action, pairId, frequency, true, false );
	}

	public String getString( Context ctx ) {
		String[] frequencies = ctx.getResources().getStringArray( R.array.frequencies );
		String time = "1 hour";

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

	public double getPrevious() {
		return previous;
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
}

