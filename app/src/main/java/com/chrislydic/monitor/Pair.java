package com.chrislydic.monitor;

import java.io.Serializable;

/**
 * Created by chris on 1/14/2018.
 */

public class Pair implements Serializable {
	private long id;
	private String fromSymbol;
	private String fromName;
	private String toSymbol;
	private int order;

	public Pair( long id, String fromSymbol, String fromName, String toSymbol, int order ) {
		this.id = id;
		this.fromSymbol = fromSymbol;
		this.fromName = fromName;
		this.toSymbol = toSymbol;
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public long getId() {
		return id;
	}

	public String getFromSymbol() {
		return fromSymbol;
	}

	public String getFromName() {
		return fromName;
	}

	public String getToSymbol() {
		return toSymbol;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public void setOrder( int order ) {
		this.order = order;
	}

	@Override
	public String toString() {
		return fromSymbol + "/" + toSymbol;
	}
}
