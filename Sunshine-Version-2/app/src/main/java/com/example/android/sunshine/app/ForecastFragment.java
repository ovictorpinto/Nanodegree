package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String POSITION_CLICKED = "POSITION_CLICKED";
    private ListView listView;

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    private ForecastAdapter adapter;
    private int positionClicked;
    private boolean todaySpecial;

    final int LOADER_ID = 100;
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID, WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry
            .COLUMN_MIN_TEMP, WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT, WeatherContract.LocationEntry.COLUMN_COORD_LONG};

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    public void setTodaySpecial(boolean todaySpecial) {
        this.todaySpecial = todaySpecial;
        if (adapter != null) {
            adapter.setTodayIsEspecial(todaySpecial);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            updateWeather();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String loc = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        //        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
        //        fetchWeatherTask.execute(loc);

        //        Intent intent = new Intent(getActivity(), SunshineService.class);
        //        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
        //        getActivity().startService(intent);

        //quando usa alarmes para atualizar
        //        Intent intentAlarm = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        //        intentAlarm.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
        //
        //        Log.d(ForecastFragment.class.getSimpleName(), "Refreshing...");
        //        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intentAlarm, PendingIntent.FLAG_ONE_SHOT);
        //        AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        //        alarmMgr.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 2 * 1000, pendingIntent);

        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView) rootView.findViewById(R.id.list_forecast);

        adapter = new ForecastAdapter(getActivity(), null, 0);
        adapter.setTodayIsEspecial(todaySpecial);
        listView.setAdapter(adapter);
        //        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //            @Override
        //            public void onItemClick(AdapterView<?> parent, View view, int POSITION_CLICKED, long id) {
        //                String forecast = null;//adapter.getItem(POSITION_CLICKED);
        //                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
        //                Intent intent = new Intent(getActivity(), DetailActivity.class);
        //                intent.putExtra(Intent.EXTRA_TEXT, forecast);
        //                startActivity(intent);
        //            }
        //        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct POSITION_CLICKED for getItem(), or null
                // if it cannot seek to that POSITION_CLICKED.
                positionClicked = position;
                //antes já abria direto a activity
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    //                    Intent intent = new Intent(getActivity(), DetailActivity.class).setData(WeatherContract
                    // .WeatherEntry
                    //                            .buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                    //                    startActivity(intent);

                    final Uri uri = WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE));
                    ((Callback) getActivity()).onItemSelected(uri);
                }

            }
        });

        if (savedInstanceState != null) {
            positionClicked = savedInstanceState.getInt(POSITION_CLICKED, 0);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry
                .buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        listView.smoothScrollToPosition(positionClicked);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION_CLICKED, positionClicked);
    }
}