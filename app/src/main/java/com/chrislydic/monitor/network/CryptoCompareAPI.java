package com.chrislydic.monitor.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by chris on 12/26/2017.
 */
public interface CryptoCompareAPI {
	String URL = "https://min-api.cryptocompare.com";
	int HALF_HOUR = 30;
	int HOUR = 60;
	int THREE_HOURS = 180;
	int SIX_HOURS = 360;
	int TWELVE_HOURS = 720;
	int DAY = 1440;
	int WEEK = 168;
	int MONTH = 720;
	int YEAR = 365;

	@GET("/data/all/coinlist")
	public Call<CoinList> fetchCoinList();

	//base and counter currencies
	//the api accepts lists but that feature is not used in this app
	@GET("/data/price")
	public Call<SimplePrice> fetchPrice( @Query( "fsym" ) String base, @Query( "tsyms" ) String counter );

	@GET("/data/price")
	public Call<SimplePrice> fetchPrice( @Query( "fsym" ) String base, @Query( "tsyms" ) String counter, @Query( "e" ) String exchange );

	@GET("/data/pricemultifull")
	public Call<Price> fetchPriceData( @Query( "fsyms" ) String base, @Query( "tsyms" ) String counter );

	@GET("/data/pricemultifull")
	public Call<Price> fetchPriceData( @Query( "fsyms" ) String base, @Query( "tsyms" ) String counter, @Query( "e" ) String exchange );

	@GET("/data/histominute")
	public Call<History> fetchHistoMinute( @Query( "fsym" ) String base, @Query( "tsym" ) String counter, @Query( "limit" ) int limit );

	@GET("/data/histominute")
	public Call<History> fetchHistoMinute( @Query( "fsym" ) String base, @Query( "tsym" ) String counter, @Query( "limit" ) int limit, @Query( "e" ) String exchange );

	@GET("/data/histohour")
	public Call<History> fetchHistoHour( @Query( "fsym" ) String base, @Query( "tsym" ) String counter, @Query( "limit" ) int limit );

	@GET("/data/histohour")
	public Call<History> fetchHistoHour( @Query( "fsym" ) String base, @Query( "tsym" ) String counter, @Query( "limit" ) int limit, @Query( "e" ) String exchange );

	@GET("/data/histoday")
	public Call<History> fetchHistoDay( @Query( "fsym" ) String base, @Query( "tsym" ) String counter, @Query( "limit" ) int limit );

	@GET("/data/histoday")
	public Call<History> fetchHistoDay( @Query( "fsym" ) String base, @Query( "tsym" ) String counter, @Query( "limit" ) int limit, @Query( "e" ) String exchange );

	@GET("/data/histoday?allData=true")
	public Call<History> fetchAllHistory( @Query( "fsym" ) String base, @Query( "tsym" ) String counter );

	@GET("/data/histoday?allData=true")
	public Call<History> fetchAllHistory( @Query( "fsym" ) String base, @Query( "tsym" ) String counter, @Query( "e" ) String exchange );
}
