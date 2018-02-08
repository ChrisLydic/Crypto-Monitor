package com.chrislydic.monitor;

import android.content.Context;

import java.io.Serializable;
import java.util.Locale;

/**
 * An entry in an order book.
 */
public class Alert implements Serializable {
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

	public Alert( long id, int type, double amount, double previous, int action, long pairId, int frequency ) {
		this.id = id;
		this.type = type;
		this.amount = amount;
		this.previous = previous;
		this.action = action;
		this.pairId = pairId;
		this.frequency = frequency;
	}

	public String getString( Context ctx ) {
		if (type == PRICE_VALUE) {
			if (action == RISE_TO) {
				return Long.toString( id)+ctx.getResources().getString( R.string.alert_rise_to,
						String.format( Locale.getDefault(), "%.2f", amount ) );
			} else if (action == FALL_TO) {
				return Long.toString( id)+ctx.getResources().getString( R.string.alert_fall_to,
						String.format( Locale.getDefault(), "%.2f", amount ) );
			} else {
				return Long.toString( id)+ctx.getResources().getString( R.string.alert_change_to,
						String.format( Locale.getDefault(), "%.2f", amount ) );
			}
		} else {
			if (action == RISE_TO) {
				return Long.toString( id)+ctx.getResources().getString( R.string.alert_rise_by,
						String.format( Locale.getDefault(), "%.2f", amount ) );
			} else if (action == FALL_TO) {
				return Long.toString( id)+ctx.getResources().getString( R.string.alert_fall_by,
						String.format( Locale.getDefault(), "%.2f", amount ) );
			} else {
				return Long.toString( id)+ctx.getResources().getString( R.string.alert_change_by,
						String.format( Locale.getDefault(), "%.2f", amount ) );
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

	public void setPrevious( double previous ) {
		this.previous = previous;
	}
}

