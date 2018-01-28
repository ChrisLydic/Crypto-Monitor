package com.chrislydic.monitor.database;

/**
 * Created by chris on 1/14/2018.
 */

public class AlertDbSchema {
	public static final class Table {
		public static final String NAME = "alert_table";

		public static final class Cols {
			public static final String ID = "_id";
			public static final String TYPE = "type";
			public static final String AMOUNT = "amount";
			public static final String ACTION = "action";
			public static final String PAIR = "pair";
			public static final String PREVIOUS = "previous";
			public static final String FREQUENCY = "frequency";
		}
	}
}
