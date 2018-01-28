package com.chrislydic.monitor.network;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by chris on 1/12/2018.
 */

public class History {
	@SerializedName("USD")
	private String usd;

	@SerializedName("Response")
	private String response;

	@SerializedName("Type")
	private int type;

	@SerializedName("Aggregated")
	private boolean aggregated;

	@SerializedName("TimeTo")
	private long timeTo;

	@SerializedName("TimeFrom")
	private long timeFrom;

	@SerializedName("FirstValueInArray")
	private boolean firstValueInArray;

	//there's also a conversiontype object

	private ArrayList<Time> times;
	private ArrayList<Entry> entries;

	private class Time {
		private long time;
		private double close;
		private double high;
		private double low;
		private double open;
		private double volumefrom;
		private double volumeto;

		public Time(long time, double close, double high, double low,
		                 double open, double volumefrom, double volumeto) {
			this.time = time;
			this.close = close;
			this.high = high;
			this.low = low;
			this.open = open;
			this.volumefrom = volumefrom;
			this.volumeto = volumeto;
		}
	}

	public void addTime(long time, double close, double high, double low,
	                    double open, double volumefrom, double volumeto) {
		if (times == null) {
			times = new ArrayList<>();
		}
		if (entries == null) {
			entries = new ArrayList<>();
		}
		times.add( new Time( time, close, high, low, open, volumefrom, volumeto ) );
		entries.add( new Entry( time, (float)close ) );
	}

	public ArrayList<Entry> getEntries() {
		return entries;
	}
}