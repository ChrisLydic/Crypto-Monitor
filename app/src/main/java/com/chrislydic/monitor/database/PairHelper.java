package com.chrislydic.monitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chrislydic.monitor.Pair;
import com.chrislydic.monitor.network.Coin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chris on 1/15/2018.
 */

public class PairHelper {
	private static PairHelper pairHelper;

	private Context mContext;
	private SQLiteDatabase mDatabase;

	public static PairHelper get( Context context ) {
		if ( pairHelper == null) {
			pairHelper = new PairHelper(context);
		}

		return pairHelper;
	}

	private PairHelper( Context context ) {
		mContext = context.getApplicationContext();
		mDatabase = new MonitorBaseHelper( mContext ).getWritableDatabase();
	}

	public List<Pair> getPairs() {
		List<Pair> pairs = new ArrayList<>();

		PairCursorWrapper cursor = queryItems( null, null );

		try {
			cursor.moveToFirst();
			while ( !cursor.isAfterLast() ) {
				pairs.add( cursor.getPair() );
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}

		Collections.sort( pairs, new Comparator<Pair>() {
			@Override
			public int compare( Pair pairA, Pair pairB ) {
				return pairA.getOrder() - pairB.getOrder();
			}
		} );

		return pairs;
	}

	public Pair getPair( long id ) {
		PairCursorWrapper cursor = queryItems( PairDbSchema.Table.Cols.ID + " = ?",
												new String[] { Long.toString( id ) } );
		try {
			if ( cursor.getCount() == 0 ) {
				return null;
			}

			cursor.moveToFirst();
			return cursor.getPair();
		} finally {
			cursor.close();
		}
	}

	public void addPair( String fromSymbol, String fromName, String toSymbol ) {
		ContentValues values = new ContentValues();
		values.put( PairDbSchema.Table.Cols.FSYM, fromSymbol );
		values.put( PairDbSchema.Table.Cols.FNAME, fromName );
		values.put( PairDbSchema.Table.Cols.TSYM, toSymbol );
		values.put( PairDbSchema.Table.Cols.ORDER, 0 );

		// find the max order value so the new pair is at the end
		Cursor cursor = mDatabase.rawQuery(
				"SELECT MAX(" + PairDbSchema.Table.Cols.ORDER +
				") as " + PairDbSchema.Table.Cols.ORDER +
				" FROM " + PairDbSchema.Table.NAME, null );

		try {
			if ( cursor.getCount() > 0 ) {
				cursor.moveToFirst();
				values.put( PairDbSchema.Table.Cols.ORDER,
						cursor.getInt( cursor.getColumnIndex( PairDbSchema.Table.Cols.ORDER ) ) + 1 );
			}
		} finally {
			cursor.close();
		}


		mDatabase.insert( PairDbSchema.Table.NAME, null, values );
	}

	public void swapPair( Pair pairA, Pair pairB ) {
		int aOrder = pairA.getOrder();
		pairA.setOrder( pairB.getOrder() );
		pairB.setOrder( aOrder );

		ContentValues valuesA = new ContentValues();
		valuesA.put( PairDbSchema.Table.Cols.ORDER, pairA.getOrder() );
		ContentValues valuesB = new ContentValues();
		valuesB.put( PairDbSchema.Table.Cols.ORDER, pairB.getOrder() );

		mDatabase.update( PairDbSchema.Table.NAME, valuesA, PairDbSchema.Table.Cols.ID + " = ?", new String[] { Long.toString( pairA.getId() ) } );
		mDatabase.update( PairDbSchema.Table.NAME, valuesB, PairDbSchema.Table.Cols.ID + " = ?", new String[] { Long.toString( pairB.getId() ) } );
	}

	public void deletePair( Pair pair ) {
		mDatabase.delete( PairDbSchema.Table.NAME,
				PairDbSchema.Table.Cols.ID + " = ?",
				new String[]{ Long.toString( pair.getId() ) } );
	}

	private PairCursorWrapper queryItems( String whereClause, String[] whereArgs ) {
		Cursor cursor = mDatabase.query(
				PairDbSchema.Table.NAME,
				null, // columns -- null selects all columns
				whereClause,
				whereArgs,
				null, // groupby
				null, // having
				null  // orderby
		);

		return new PairCursorWrapper( cursor );
	}
}
