package com.addsensor.CameraMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CameraAdapterDB {
	private static final String TAG = "CameraAdapterDB";
	
	public static final String KEY_ROWID = "_id";
    public static final String KEY_LOGIN = "login";
    public static final String KEY_PASS = "pass";
    private static final String DATABASE_NAME = "cameradb";
    private static final String DATABASE_TABLE = "credentials";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_CREATE = "create table if not exists credentials (_id integer primary key autoincrement, " + "login text, pass text);";
    
    private final Context context;
    private DatabaseHelper DBHelper;
    SQLiteDatabase db;
    
    public CameraAdapterDB( Context cntxt ) {
    	this.context = cntxt;
    	DBHelper = new DatabaseHelper( context );
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper( Context context ) {
			super( context, DATABASE_NAME, null, DATABASE_VERSION ); 
		}

		@Override
		public void onCreate( SQLiteDatabase db ) {
			Log.d ( CameraAdapterDB.TAG, "Creamos BD" );
			// TODO Auto-generated method stub
			db.execSQL( DATABASE_CREATE );
		}

		@Override
		public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
			// TODO Auto-generated method stub
			Log.w( TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data" );
            db.execSQL( "DROP TABLE IF EXISTS titles" );
            onCreate(db);
		}
    }
    
    // Open the database
    public CameraAdapterDB open() throws SQLException {
    	Log.d ( CameraAdapterDB.TAG, "Open BD" );
    	db = DBHelper.getWritableDatabase();
    	return this;
    }
    
    // Close the database
     public void close() {
    	 Log.d ( CameraAdapterDB.TAG, "Close BD" );
    	 DBHelper.close();
     }
     
     // Insert login and pass into database
     public long insert( String login, String pass ) {
    	 ContentValues values = new ContentValues();
    	 if ( !loginExists(login) ) {
    		 Log.d ( CameraAdapterDB.TAG, "Insertamos en BD" );
        	 values.put( KEY_LOGIN, login );
        	 values.put( KEY_PASS, pass );
        	 return db.insert( DATABASE_TABLE, null, values ); 
    	 }
    	 values.put( KEY_PASS, pass);
    	 return db.update( DATABASE_TABLE, values, KEY_LOGIN + "=" + login, null);
     }
     
     // Login already exists
     public boolean loginExists( String key ) {
    	 Log.d ( CameraAdapterDB.TAG, "El login en la BD" );
    	 Cursor auxCursor = null;
    	 try {
    		 //auxCursor = db.query( DATABASE_TABLE, new String[] { KEY_PASS }, KEY_LOGIN + "=" + key, null, null, null, null );
    		 auxCursor = db.rawQuery("SELECT pass FROM credentials WHERE login='" + key + "';", null);
    	 } catch ( Exception e ) {
    		 System.out.println(e);
    	 }
    	 if ( auxCursor.getCount() != 0 ) return true;
    	 return false;
     }
     
     // Retrieves a particular password
     public Cursor getPass( String key ) {
    	 Log.d ( CameraAdapterDB.TAG, "Cojemos el PASS" );
    	 Log.d ( CameraAdapterDB.TAG, "El login es: "  + key );
    	 Cursor auxCursor = null;
    	 try {
    		 //auxCursor = db.query( DATABASE_TABLE, new String[] { KEY_LOGIN, KEY_PASS }, KEY_LOGIN + "=" + key, null, null, null, null );
    		auxCursor = db.rawQuery("SELECT login, pass FROM credentials WHERE login='" + key + "';", null);
        	 
    	 } catch ( Exception e ) {
    		 System.out.println(e);
    	 }
    	 if ( auxCursor.getCount() != 0 ) auxCursor.moveToFirst();
    	 //Log.d ( CameraAdapterDB.TAG, "El cursor es: " + auxCursor);
    	 return auxCursor;
     }
}
