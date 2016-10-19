package com.github.ovictorpinto.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    private GridView gridView;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie selected = (Movie) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class).putExtra(Movie.PARAM, selected);
                startActivity(intent);
            }
        });
        LoadMovies task = new LoadMovies();
        task.execute();
    }

    class LoadMovies extends AsyncTask<Void, Void, List<Movie>> {

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

                Uri builder = Uri.parse("https://api.themoviedb.org/3/movie/popular").buildUpon()
                                 .appendQueryParameter("api_key", "f15f848efb98371d2beee477f0efeb9d").build();

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
                Log.e("PlaceholderFragment", "Error ", e);
                e.printStackTrace();
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (movies != null) {
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

            //            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            //            String unit = preferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));

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
