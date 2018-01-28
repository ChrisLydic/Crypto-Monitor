package com.chrislydic.monitor.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by chris on 12/31/2017.
 */
public class Price {
	@SerializedName("MARKET")
	public String market;

	@SerializedName("FROMSYMBOL")
	public String fromSymbol;

	@SerializedName("TOSYMBOL")
	public String toSymbol;

	@SerializedName("PRICE")
	public double price;

	@SerializedName("LASTUPDATE")
	public int lastUpdate;

	@SerializedName("LASTVOLUME")
	public double lastVolume;

	@SerializedName("LASTVOLUMETO")
	public double lastVolumeTo;

	@SerializedName("LASTTRADEID")
	public double lastTradeId;

	@SerializedName("VOLUME24HOUR")
	public double volume24Hour;

	@SerializedName("VOLUME24HOURTO")
	public double volume24HourTo;

	@SerializedName("OPEN24HOUR")
	public double open24Hour;

	@SerializedName("HIGH24HOUR")
	public double high24Hour;

	@SerializedName("LOW24HOUR")
	public double low24Hour;

	@SerializedName("LASTMARKET")
	public String lastMarket;

	@SerializedName("CHANGE24HOUR")
	public double change24Hour;

	@SerializedName("CHANGEPCT24HOUR")
	public double changepct24Hour;
}
