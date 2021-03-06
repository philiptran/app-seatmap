package cl.seatmap.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cl.seatmap.domain.ContactLocation;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * 
 * @author philiptrannp
 * 
 */
public class ContactLocationDAO extends SQLiteAssetHelper {
	// All Static variables
	// Database Version
	public static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "contacts.db";

	// Contacts table name
	private static final String TABLE_CONTACT_LOCATION = "contact_location";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_LOCATION = "location";
	private static final String KEY_LEVEL = "level";
	private static final String KEY_X = "x";
	private static final String KEY_Y = "y";
	private static final String KEY_NAME = "name";
	private static final String KEY_HX = "h_x";
	private static final String KEY_HY = "h_y";

	/**
	 * @param context
	 */
	public ContactLocationDAO(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// This database is read-only.
		setForcedUpgrade();
	}

	/**
	 * Forces the database to reload from the default asset file.
	 */
	public static void forceDatabaseReload(Context context) {
		ContactLocationDAO dbHelper = new ContactLocationDAO(context);
		dbHelper.setForcedUpgrade();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.setVersion(-1);
		db.close();
		db = dbHelper.getWritableDatabase();
	}

	// @Override
	// public void onCreate(SQLiteDatabase db) {
	// String createSQL = "CREATE TABLE " + TABLE_CONTACT_LOCATION + "("
	// + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LOCATION + " TEXT,"
	// + KEY_X + " INTEGER,"
	// + KEY_Y + " INTEGER)";
	// db.execSQL(createSQL);
	// }

	public void add(ContactLocation cl) {
		SQLiteDatabase db = this.getWritableDatabase();
		//
		ContentValues values = new ContentValues();
		values.put(KEY_LOCATION, cl.getLocation());
		values.put(KEY_LEVEL, cl.getLevel());
		values.put(KEY_X, cl.getX());
		values.put(KEY_Y, cl.getY());
		values.put(KEY_NAME, cl.getName());
		values.put(KEY_HX, cl.getHx());
		values.put(KEY_HY, cl.getHy());

		// Inserting Row
		try {
			db.insert(TABLE_CONTACT_LOCATION, null, values);
		} finally {
			db.close(); // Closing database connection
		}
	}

	public ContactLocation get(String location) {
		SQLiteDatabase db = this.getReadableDatabase();
		//
		Cursor cursor = db.query(TABLE_CONTACT_LOCATION,
				new String[] { KEY_ID, KEY_LOCATION, KEY_LEVEL, KEY_X, KEY_Y,
						KEY_NAME, KEY_HX, KEY_HY }, KEY_LOCATION + "=?",
				new String[] { location }, null, null, null, null);
		try {
			if (cursor == null || cursor.getCount() == 0) {
				return null;
			}
			cursor.moveToFirst();
			ContactLocation cl = new ContactLocation();
			cl.setId(cursor.getInt(0));
			cl.setLocation(cursor.getString(1));
			cl.setLevel(cursor.getInt(2));
			cl.setX(cursor.getInt(3));
			cl.setY(cursor.getInt(4));
			cl.setName(cursor.getString(5));
			cl.setHx(cursor.getInt(6));
			cl.setHy(cursor.getInt(7));
			//
			return cl;
		} finally {
			if (cursor != null)
				cursor.close();
			db.close();
		}
	}

	public int update(ContactLocation cl) {
		SQLiteDatabase db = this.getWritableDatabase();
		//
		ContentValues values = new ContentValues();
		values.put(KEY_LOCATION, cl.getLocation());
		values.put(KEY_LEVEL, cl.getLevel());
		values.put(KEY_X, cl.getX());
		values.put(KEY_Y, cl.getY());
		values.put(KEY_NAME, cl.getName());
		values.put(KEY_HX, cl.getHx());
		values.put(KEY_HY, cl.getHy());

		// updating row
		try {
			return db.update(TABLE_CONTACT_LOCATION, values, KEY_ID + " = ?",
					new String[] { String.valueOf(cl.getId()) });
		} finally {
			db.close();
		}
	}

	public void delete(ContactLocation cl) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			db.delete(TABLE_CONTACT_LOCATION, KEY_ID + " = ?",
					new String[] { String.valueOf(cl.getId()) });
		} finally {
			db.close();
		}
	}

	/**
	 * Exclude location with empty name
	 * 
	 * @param cl
	 * @param distance
	 * @return
	 */
	public List<ContactLocation> findNearby(ContactLocation cl, int distance) {
		List<ContactLocation> nearbyList = new ArrayList<ContactLocation>();
		String[] selectionArgs = new String[] { cl.getX() + "", cl.getY() + "" };
		//
		SQLiteDatabase db = this.getReadableDatabase();
		// Cursor cursor = db.rawQuery(
		// "SELECT *, ((x-?)*(x-?)+(y-?)*(y-?)) as D FROM contact_location WHERE "
		// + "(name is not null OR name != '') AND "
		// + "(D>0 AND D<" + distance * distance + ") ORDER BY D",
		// selectionArgs);
		// Use Manhattan distance
		Cursor cursor = db.rawQuery(
				"SELECT *, (abs(x-?)+abs(y-?)) as D FROM contact_location WHERE "
						+ "(name is not null OR name != '') AND "
						+ "(D>0 AND D<" + distance + ") ORDER BY D",
				selectionArgs);
		// ORDER BY y, x
		try {
			if (cursor == null || cursor.getCount() == 0) {
				return nearbyList;
			}
			//
			while (!cursor.isLast()) {
				cursor.moveToNext();
				//
				ContactLocation c = new ContactLocation();
				c.setId(cursor.getInt(0));
				c.setLocation(cursor.getString(1));
				c.setLevel(cursor.getInt(2));
				c.setX(cursor.getInt(3));
				c.setY(cursor.getInt(4));
				c.setName(cursor.getString(5));
				c.setHx(cursor.getInt(6));
				c.setHy(cursor.getInt(7));
				//
				nearbyList.add(c);
			}
		} finally {
			if (cursor != null)
				cursor.close();
			db.close();
		}
		return nearbyList;
	}

	public ContactLocation findContactAt(int x, int y) {
		String[] selectionArgs = new String[] { x + "", y + "" };
		int delta = 350;
		//
		SQLiteDatabase db = this.getReadableDatabase();
		// Cursor cursor = db.rawQuery(
		// "SELECT *, ((x-?)*(x-?)+(y-?)*(y-?)) as D FROM contact_location WHERE "
		// + "(name is not null OR name != '') AND "
		// + "(D>0 AND D<" + delta * delta + ") ORDER BY D",
		// selectionArgs);
		// Use Manhattan distance
		Cursor cursor = db
				.rawQuery(
						"SELECT *, (abs(x-?)+abs(y-?)) as D FROM contact_location WHERE "
								+ "(name is not null OR name != '') AND "
								+ "(D>0 AND D<" + delta + ") ORDER BY D",
						selectionArgs);
		//
		ContactLocation c = null;
		try {
			if (cursor == null || cursor.getCount() == 0) {
				return c;
			}
			// only get the first record (closet location)
			cursor.moveToNext();
			//
			c = new ContactLocation();
			c.setId(cursor.getInt(0));
			c.setLocation(cursor.getString(1));
			c.setLevel(cursor.getInt(2));
			c.setX(cursor.getInt(3));
			c.setY(cursor.getInt(4));
			c.setName(cursor.getString(5));
			c.setHx(cursor.getInt(6));
			c.setHy(cursor.getInt(7));
			//
			return c;
		} finally {
			if (cursor != null)
				cursor.close();
			db.close();
		}
	}
}
