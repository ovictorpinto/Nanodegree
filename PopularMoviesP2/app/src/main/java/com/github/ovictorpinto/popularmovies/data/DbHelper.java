package com.github.ovictorpinto.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DbHelper extends SQLiteOpenHelper {
    
    private static final String NAME = "PopularMovies.db";
    private static final int VERSION = 1;
    
    DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Contract.Favorite.TABLE_NAME + " (" + Contract.Favorite._ID + " INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + Contract.Favorite.COLUMN_POSTER + " TEXT NOT NULL, " + Contract.Favorite.COLUMN_MOVIE_ID + " TEXT NOT" +
                " NULL, " + "UNIQUE (" + Contract.Favorite.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";
        
        db.execSQL(builder);
        
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        db.execSQL(" DROP TABLE IF EXISTS " + Contract.Favorite.TABLE_NAME);
        
        onCreate(db);
    }
}
