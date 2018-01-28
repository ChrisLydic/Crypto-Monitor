package com.chrislydic.monitor.database;

/**
 * Created by chris on 1/14/2018.
 */

public class PairDbSchema {
	public static final class Table {
		public static final String NAME = "pair_table";

		public static final class Cols {
			public static final String ID = "_id";
			public static final String FSYM = "fsym";
			public static final String TSYM = "tsym";
			public static final String ORDER = "order_val";
		}
	}
}
