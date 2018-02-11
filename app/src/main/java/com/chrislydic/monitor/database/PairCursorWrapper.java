package com.chrislydic.monitor.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.chrislydic.monitor.Pair;

/**
 * Created by chris on 1/14/2018.
 */

public class PairCursorWrapper extends CursorWrapper {
	public PairCursorWrapper( Cursor cursor ) {
		super( cursor );
	}

	public Pair getPair() {
		long id = getLong( getColumnIndex( PairDbSchema.Table.Cols.ID ) );
		String fromSymbol = getString( getColumnIndex( PairDbSchema.Table.Cols.FSYM ) );
		String fromName = getString( getColumnIndex( PairDbSchema.Table.Cols.FNAME ) );
		String toSymbol = getString( getColumnIndex( PairDbSchema.Table.Cols.TSYM ) );
		int order = getInt( getColumnIndex( PairDbSchema.Table.Cols.ORDER ) );

		return new Pair( id, fromSymbol, fromName, toSymbol, order );
	}
}
