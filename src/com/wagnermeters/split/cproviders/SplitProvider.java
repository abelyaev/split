package com.wagnermeters.split.cproviders;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class SplitProvider extends ContentProvider {
	
	static private SQLiteDatabase db;
	
	private static class SplitDbHelper extends SQLiteOpenHelper {
		
		public static final String DB_NAME = "split.db";
		
		public static final int DB_VERSION = 1;
		
		public static final String EMC2TEMP_TABLE = "EMC2TEMP";
		
		public static final String CATEGORIES_TABLE = "CATEGORIES";
		
		public static final String ARTICLES_TABLE = "ARTICLES";
		
		public static final String EMC2TEMP_TABLE_CREATE = "CREATE TABLE " + EMC2TEMP_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, h INTEGER, T INTEGER, M INTEGER)";
		
		public static final String CATEGORIES_TABLE_CREATE = "CREATE TABLE " + CATEGORIES_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, backend_id INTEGER, title TEXT, deleted INT DEFAULT 0)";
		
		public static final String ARTICLES_TABLE_CREATE = "CREATE TABLE " + ARTICLES_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, backend_id INTEGER, category_id INTEGER, section TEXT, title TEXT, teaser TEXT, link TEXT, deleted INT DEFAULT 0)";

		public SplitDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(EMC2TEMP_TABLE_CREATE);
			db.execSQL(CATEGORIES_TABLE_CREATE);
			db.execSQL(ARTICLES_TABLE_CREATE);
		}
		
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
		
	}
	
	public static final Uri EMC2TEMP_URI = Uri.parse("content://com.wagnermeters.split.splitprovider/emc2temp");
	
	public static final Uri CATEGORIES_URI = Uri.parse("content://com.wagnermeters.split.splitprovider/categories");
	
	public static final Uri ARTICLES_URI = Uri.parse("content://com.wagnermeters.split.splitprovider/articles");

	private static final int EMC2TEMP = 1;
	
	private static final int CATEGORIES = 2;
	
	private static final int ARTICLES = 3;

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sURIMatcher.addURI("com.wagnermeters.split.splitprovider", "emc2temp", EMC2TEMP);
		sURIMatcher.addURI("com.wagnermeters.split.splitprovider", "categories", CATEGORIES);
		sURIMatcher.addURI("com.wagnermeters.split.splitprovider", "articles", ARTICLES);
	}
	
	public boolean onCreate() {
		db = new SplitDbHelper(getContext()).getWritableDatabase();
		
		return false;
	}

	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	public String getType(Uri uri) {
		return null;
	}

	public Uri insert(Uri uri, ContentValues values) {
		int match = sURIMatcher.match(uri);

        switch(match) {
        	case EMC2TEMP:
        		db.insert(SplitDbHelper.EMC2TEMP_TABLE, null, values);
        		
        		break;
        	case CATEGORIES:
        		db.insert(SplitDbHelper.CATEGORIES_TABLE, null, values);
        		
        		break;
        	case ARTICLES:
        		db.insert(SplitDbHelper.ARTICLES_TABLE, null, values);
        		getContext().getContentResolver().notifyChange(ARTICLES_URI, null);
        		getContext().getContentResolver().notifyChange(CATEGORIES_URI, null);
        		
        		break;
        }
        		
        return uri;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		int match = sURIMatcher.match(uri);
		
		switch(match) {
    		case EMC2TEMP:
    			result = db.query(SplitDbHelper.EMC2TEMP_TABLE, projection, selection, selectionArgs, null, null, sortOrder);

    			break;
    		case CATEGORIES:
    			result = db.query(SplitDbHelper.CATEGORIES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    			result.setNotificationUri(getContext().getContentResolver(), CATEGORIES_URI);

    			break;
    		case ARTICLES:
    			result = db.query(SplitDbHelper.ARTICLES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    			result.setNotificationUri(getContext().getContentResolver(), ARTICLES_URI);

    			break;
		}
		
		return result;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int match = sURIMatcher.match(uri);
		int result = 0;

        switch(match) {
        	case CATEGORIES:
        		result = db.update(SplitDbHelper.CATEGORIES_TABLE, values, selection, selectionArgs);
        		
        		break;
        	case ARTICLES:
        		result = db.update(SplitDbHelper.ARTICLES_TABLE, values, selection, selectionArgs);
        		getContext().getContentResolver().notifyChange(ARTICLES_URI, null);
        		getContext().getContentResolver().notifyChange(CATEGORIES_URI, null);
        		
        		break;
        }
        		
        return result;
	}
	
	public static void importDB(Context context, InputStream in) {
		db.close();

		OutputStream out;
		try {
			out = new FileOutputStream(context.getDatabasePath(SplitDbHelper.DB_NAME));

			byte[] buf = new byte[1024];
			int len;
			while((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			
			out.close();
		}
		catch (FileNotFoundException e) {}
		catch (IOException e) {}

		db = new SplitDbHelper(context).getWritableDatabase();
	}
	
}