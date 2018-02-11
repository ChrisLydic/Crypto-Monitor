package com.chrislydic.monitor.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.chrislydic.monitor.Alert;

/**
 * Created by chris on 1/14/2018.
 */

public class AlertCursorWrapper extends CursorWrapper {
	public AlertCursorWrapper( Cursor cursor ) {
		super( cursor );
	}

	public Alert getAlert() {
		long id = getLong( getColumnIndex( AlertDbSchema.Table.Cols.ID ) );
		int type = getInt( getColumnIndex( AlertDbSchema.Table.Cols.TYPE ) );
		double amount = getDouble( getColumnIndex( AlertDbSchema.Table.Cols.AMOUNT ) );
		int action = getInt( getColumnIndex( AlertDbSchema.Table.Cols.ACTION ) );
		int pairId = getInt( getColumnIndex( AlertDbSchema.Table.Cols.PAIR ) );
		double previous = getDouble( getColumnIndex( AlertDbSchema.Table.Cols.PREVIOUS ) );
		int frequency = getInt( getColumnIndex( AlertDbSchema.Table.Cols.FREQUENCY ) );
		boolean enabled = getInt( getColumnIndex( AlertDbSchema.Table.Cols.ENABLED ) ) == 1;
		boolean active = getInt( getColumnIndex( AlertDbSchema.Table.Cols.ACTIVE ) ) == 1;

		return new Alert( id, type, amount, previous, action, pairId, frequency, enabled, active );
	}
}