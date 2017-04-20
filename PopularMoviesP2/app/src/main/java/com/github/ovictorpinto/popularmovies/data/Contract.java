package com.github.ovictorpinto.popularmovies.data;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.github.ovictorpinto.popularmovies.model.Movie;

public final class Contract {
    
    static final String AUTHORITY = "com.github.ovictorpinto.popularmovies";
    static final String PATH_FAVORITE = "favorite";
    static final String PATH_FAVORITE_BY_ID_MOVIE = "favorite/*";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    
    private Contract() {
    }
    
    @SuppressWarnings("unused")
    public static final class Favorite implements BaseColumns {
        
        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_FAVORITE).build();
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final int POSITION_ID = 0;
        public static final int POSITION_POSTER = 1;
        public static final int POSITION_MOVIE_ID = 2;
        public static final String[] FAVORITE_COLUMNS = {_ID, COLUMN_POSTER, COLUMN_MOVIE_ID};
        
        static final String TABLE_NAME = "favorites";
        
        public static Uri makeUriForMovie(int idMovie) {
            return URI.buildUpon().appendPath(String.valueOf(idMovie)).build();
        }
        
        static int getIdMovieFromUri(Uri queryUri) {
            return Integer.parseInt(queryUri.getLastPathSegment());
        }
        
        public static ContentValues contentValuesFromMovie(Movie movie) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_POSTER, movie.getPosterPath());
            cv.put(COLUMN_MOVIE_ID, movie.getId());
            
            return cv;
        }
        
    }
    
}
