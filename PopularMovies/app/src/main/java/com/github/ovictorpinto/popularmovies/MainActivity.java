package com.github.ovictorpinto.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

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

public class MainActivity extends AppCompatActivity {

    public static final String PARAM_API_KEY = "api_key";
    private GridView gridView;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SORT_POPULAR = 0;
    private static final int SORT_VOTED = 1;

    private SharedPreferences mSharedPreferences;
    private int mSortBy;
    private LoadMovies mLoadMovies;
    private View mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mSortBy = mSharedPreferences.getInt(getString(R.string.pref_sort), SORT_POPULAR);

        setContentView(R.layout.activity_main);

        mMainView = findViewById(R.id.activity_main);

        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie selected = (Movie) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class).putExtra(Movie.PARAM, selected);
                startActivity(intent);
            }
        });

        refresh();
    }

    private void refresh() {
        if (isOnline()) {
            if (mLoadMovies != null) {
                mLoadMovies.cancel(true);
            }
            mLoadMovies = new LoadMovies();
            mLoadMovies.execute();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_sort, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadMovies != null) {
            mLoadMovies.cancel(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
                gridView.setAdapter(new MovieAdapter(MainActivity.this, movies));
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

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(ATT_LIST);

            List<Movie> movies = new ArrayList<>();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (int i = 0; i < moviesArray.length(); i++) {

                JSONObject movieJson = moviesArray.getJSONObject(i);

                Movie movie = new Movie();
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

                movie.setVoteAverage((float) movieJson.getDouble(ATT_VOTE));
                movies.add(movie);
            }

            return movies;

        }
    }
}
