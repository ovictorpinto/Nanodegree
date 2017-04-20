package com.github.ovictorpinto.popularmovies;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.github.ovictorpinto.popularmovies.data.Contract;
import com.github.ovictorpinto.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by victorpinto on 19/04/17. 
 */

public class MainFragment extends Fragment {
    
    interface DetailListener {
        void onSelected(Movie movie);
    }
    
    public static final String PARAM_API_KEY = "api_key";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SORT_POPULAR = 0;
    
    private static final int SORT_VOTED = 1;
    private static final int SORT_FAVORITE = 2;
    private SharedPreferences mSharedPreferences;
    
    private int mSortBy;
    private LoadMovies mLoadMovies;
    private View mMainView;
    private View progress;
    private View empty;
    private GridView gridView;
    private DetailListener detailListener;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        mSortBy = mSharedPreferences.getInt(getString(R.string.pref_sort), SORT_POPULAR);
        
        View rootView = inflater.inflate(R.layout.fragment_main, null, false);
        
        mMainView = rootView.findViewById(R.id.activity_main);
        progress = rootView.findViewById(R.id.progress);
        empty = rootView.findViewById(android.R.id.empty);
        
        gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie selected = (Movie) parent.getAdapter().getItem(position);
                detailListener.onSelected(selected);
            }
        });
        
        refresh();
        setHasOptionsMenu(true);
        return rootView;
    }
    
    private void refresh() {
        if (isOnline()) {
            if (mSortBy == SORT_FAVORITE) {
                //busca do content provider
                findFavorites();
            } else {//busca da internet
                if (mLoadMovies != null) {
                    mLoadMovies.cancel(true);
                }
                mLoadMovies = new LoadMovies();
                mLoadMovies.execute();
            }
        } else {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refresh();
                }
            };
            Snackbar.make(mMainView, R.string.offline, Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry, listener).show();
        }
    }
    
    private void findFavorites() {
        showProgress();
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(Contract.Favorite.URI, Contract.Favorite.FAVORITE_COLUMNS, null, null, null);
        List<Movie> movieList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Movie movie = new Movie();
            movie.setId(cursor.getInt(Contract.Favorite.POSITION_MOVIE_ID));
            movie.setPosterPath(cursor.getString(Contract.Favorite.POSITION_POSTER));
            movieList.add(movie);
        }
        cursor.close();
        showList(movieList);
        
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_sort, menu);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoadMovies != null) {
            mLoadMovies.cancel(true);
        }
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DetailListener) {
            detailListener = (DetailListener) context;
        } else {
            throw new IllegalStateException("Activity must implement DetailListener");
        }
    }
    
    private void showProgress() {
        gridView.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }
    
    private void showList(List<Movie> list) {
        if (list == null || list.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            gridView.setAdapter(new MovieAdapter(getActivity(), list));
            empty.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
        progress.setVisibility(View.GONE);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which != mSortBy) {
                        mSortBy = which;
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putInt(getString(R.string.pref_sort), mSortBy);
                        editor.apply();
                        refresh();
                    }
                    dialog.dismiss();
                }
            };
            builder.setSingleChoiceItems(R.array.sort_movies, mSortBy, listener).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Verify is device has connection
     * @return true if has a networking connection
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    
    /**
     * Task load movies from api themoviedb.
     */
    class LoadMovies extends AsyncTask<Void, Void, List<Movie>> {
        
        static final String URL_MOVIEDB = "https://api.themoviedb.org/3/movie";
        static final String PATH_POPULAR = "popular";
        static final String PATH_TOP_RATED = "top_rated";
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }
        
        @Override
        protected List<Movie> doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                
                Uri.Builder builder = Uri.parse(URL_MOVIEDB).buildUpon();
                switch (mSortBy) {
                    default:
                        Log.w(TAG, "Invalid sort?");
                    case SORT_POPULAR:
                        builder.appendPath(PATH_POPULAR);
                        break;
                    case SORT_VOTED:
                        builder.appendPath(PATH_TOP_RATED);
                        break;
                }
                builder.appendQueryParameter(PARAM_API_KEY, BuildConfig.MOVIE_DB_API_KEY).build();
                
                String urlString = builder.toString();
                Log.d(TAG, urlString);
                URL url = new URL(urlString);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                
                String movieJsonStr = buffer.toString();
                Log.d(TAG, movieJsonStr);
                
                return parseMovies(movieJsonStr);
            } catch (Exception e) {
                Log.e(TAG, "Error ", e);
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }
        
        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (movies != null && !isCancelled()) {
                showList(movies);
            }
        }
        
        private List<Movie> parseMovies(String moviesJsonStr) throws JSONException {
            
            // These are the names of the JSON objects that need to be extracted.
            final String ATT_LIST = "results";
            final String ATT_PATH = "poster_path";
            final String ATT_OVERVIEW = "overview";
            final String ATT_TITLE = "title";
            final String ATT_VOTE = "vote_average";
            final String ATT_DATE = "release_date";
            final String ATT_ID = "id";
            
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(ATT_LIST);
            
            List<Movie> movies = new ArrayList<>();
            
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            for (int i = 0; i < moviesArray.length(); i++) {
                
                JSONObject movieJson = moviesArray.getJSONObject(i);
                
                Movie movie = new Movie();
                movie.setId(movieJson.getInt(ATT_ID));
                movie.setOverview(movieJson.getString(ATT_OVERVIEW));
                movie.setPosterPath(movieJson.getString(ATT_PATH));
                movie.setTitle(movieJson.getString(ATT_TITLE));
                
                if (!movieJson.isNull(ATT_DATE)) {
                    try {
                        movie.setRelease(simpleDateFormat.parse(movieJson.getString(ATT_DATE)));
                    } catch (ParseException e) {
                        Log.w(TAG, "Wrong release date?");
                    }
                }
                
                movie.setVote_average((float) movieJson.getDouble(ATT_VOTE));
                movies.add(movie);
            }
            
            return movies;
            
        }
    }
}
