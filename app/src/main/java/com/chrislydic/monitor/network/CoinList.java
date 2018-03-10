package com.chrislydic.monitor.network;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by chris on 1/12/2018.
 */

public class CoinList {
	@SerializedName("Response")
	private String response;

	@SerializedName("Type")
	private int type;

	@SerializedName("BaseImageUrl")
	private String baseUrl;

	private ArrayList<Coin> coins;
	private ArrayList<Coin> coinsFull;

	public void addCoin( String imageUrl, String name, String symbol, int sortOrder ) {
		if (coins == null) {
			coins = new ArrayList<>();
			coinsFull = coins;
		}
		coins.add( new Coin( imageUrl, name, symbol, sortOrder ) );
	}

	public void sort() {
		Collections.sort( coins, new Comparator<Coin>() {
			@Override
			public int compare( Coin coinA, Coin coinB ) {
				return coinA.getSortOrder() - coinB.getSortOrder();
			}
		} );
	}

	public void filter(String terms) {
		if (terms.isEmpty()) {
			coins = coinsFull;
		} else {
			coins = new ArrayList<>();
			for (Coin coin : coinsFull) {
				if (coin.getName().toLowerCase().contains( terms.toLowerCase() ) ||
						coin.getSymbol().equals( terms.toUpperCase() )) {
					coins.add(coin);
				}
			}
		}
	}

	public ArrayList<Coin> getCoins() {
		return coins;
	}

	public String getBaseUrl() {
		return baseUrl;
	}
}