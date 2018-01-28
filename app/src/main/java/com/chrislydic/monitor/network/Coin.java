package com.chrislydic.monitor.network;

/**
 * Created by chris on 1/20/2018.
 */

public class Coin {
	public static final String NO_URL = "NONE";

	private String imageUrl;
	private String name;
	private String symbol;
	private int sortOrder;

	public Coin( String imageUrl, String name, String symbol, int sortOrder ) {
		this.imageUrl = imageUrl;
		this.name = name;
		this.symbol = symbol;
		this.sortOrder = sortOrder;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
