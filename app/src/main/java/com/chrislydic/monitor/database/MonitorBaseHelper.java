package com.chrislydic.monitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chris on 4/5/2017.
 */

public class MonitorBaseHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "monitor.db";

	public MonitorBaseHelper( Context context ) {
		super( context, DATABASE_NAME, null, VERSION );
	}

	@Override
	public void onCreate( SQLiteDatabase db ) {
		db.execSQL( "create table " + PairDbSchema.Table.NAME + "( " +
				PairDbSchema.Table.Cols.ID + " integer primary key autoincrement, " +
				PairDbSchema.Table.Cols.FSYM + " text, " +
				PairDbSchema.Table.Cols.TSYM + " text, " +
				PairDbSchema.Table.Cols.ORDER + " integer)" );

		db.execSQL( "create table " + AlertDbSchema.Table.NAME + "( " +
				AlertDbSchema.Table.Cols.ID + " integer primary key autoincrement, " +
				AlertDbSchema.Table.Cols.TYPE + " integer, " +
				AlertDbSchema.Table.Cols.AMOUNT + " real, " +
				AlertDbSchema.Table.Cols.ACTION + " integer, " +
				AlertDbSchema.Table.Cols.PREVIOUS + " real, " +
				AlertDbSchema.Table.Cols.FREQUENCY + " integer, " +
				AlertDbSchema.Table.Cols.PAIR + " integer, " +
				"FOREIGN KEY(" + AlertDbSchema.Table.Cols.PAIR + ") REFERENCES " + PairDbSchema.Table.NAME + ")" );
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int i, int i1 ) {

	}
}
