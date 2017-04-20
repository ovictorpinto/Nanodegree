package com.github.ovictorpinto.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class FavoriteProvider extends ContentProvider {
    
    private static final String TAG = FavoriteProvider.class.getSimpleName();
    
    private static final int FAVORITES = 100;
    private static final int FAVORITE_BY_ID_MOVIE = 101;
    
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private DbHelper dbHelper;
    
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_FAVORITE, FAVORITES);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_FAVORITE_BY_ID_MOVIE, FAVORITE_BY_ID_MOVIE);
        return matcher;
    }
    
    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }
    
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                Log.d(TAG, "Buscando todos");
                returnCursor = db.query(Contract.Favorite.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            
            case FAVORITE_BY_ID_MOVIE:
                String tableName = Contract.Favorite.TABLE_NAME;
                String condition = Contract.Favorite.COLUMN_MOVIE_ID + " = ?";
                String[] args = {String.valueOf(Contract.Favorite.getIdMovieFromUri(uri))};
                returnCursor = db.query(tableName, projection, condition, args, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }
        
        Context context = getContext();
        if (context != null) {
            returnCursor.setNotificationUri(context.getContentResolver(), uri);
        }
        
        return returnCursor;
    }
    
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
    
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;
        
        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                db.insert(Contract.Favorite.TABLE_NAME, null, values);
                returnUri = Contract.Favorite.URI;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }
        
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        
        return returnUri;
    }
    
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        
        if (selection == null) {
            selection = "1";
        }
        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                rowsDeleted = db.delete(Contract.Favorite.TABLE_NAME, selection, selectionArgs);
                
                break;
            
            case FAVORITE_BY_ID_MOVIE:
                int idMovie = Contract.Favorite.getIdMovieFromUri(uri);
                String whereClause = Contract.Favorite.COLUMN_MOVIE_ID + "=" + idMovie;
                rowsDeleted = db.delete(Contract.Favorite.TABLE_NAME, whereClause, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }
        
        if (rowsDeleted != 0) {
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
        }
        
        return rowsDeleted;
    }
    
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
    
}
