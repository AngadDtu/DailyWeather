package com.example.android.sunshine.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.Format;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id==R.id.action_settings){
Intent i=new Intent();
            i.setClass(MainActivity.this,SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ForecastFragment extends Fragment implements ForecastAsyncTaskInterface {
        final String format = "json";
        final String units = "meteric";
        final int numDays = 7;
        final String pin = "DELHI";
        final String key = "9d1983fecc9c6fdfc980242980c57746";
        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "meteric";
        final String DAYS_PARAM = "cnt";
        final String API_KEY = "APPID";
        ProgressDialog progress;
        ArrayList<String> weekForecast;
        ArrayAdapter<String> adapter;
        String unitType;

        public ForecastFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.forecast_fragment_menu, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_refresh) {
                updateWeather();
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private void updateWeather() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = prefs.getString(getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));
            SharedPreferences sharedPrefs =
                                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                         unitType = sharedPrefs.getString(
                                        getString(R.string.pref_units_key),
                                        getString(R.string.pref_units_metric));

            String urlString = getURLString(location,unitType);
            ForecastAsyncTask task = new ForecastAsyncTask(unitType);
            task.listener = this;
            task.execute(urlString);
            progress = new ProgressDialog(getActivity());
            progress.setTitle("Getting Forecast Data");
            progress.setMessage("Wait!");
            progress.show();

        }

        @Override
        public void onStart() {
            super.onStart();
            updateWeather();
        }

        private String getURLString(String unit, String location) {
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, location)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM,unit )
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(API_KEY, key)
                    .build();
            URL Url = null;
            try {
                Url = new URL(builtUri.toString());
                Log.i("Built Uri", String.valueOf(Url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return String.valueOf(Url);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            weekForecast = new ArrayList<>();

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String msg= (String) adapterView.getItemAtPosition(i);
                    Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent();
                    intent.setClass(getActivity(),DetailActivity.class);
                    intent.putExtra("forecast", msg);
                    startActivity(intent);
                }
            });
            return rootView;
        }

        @Override
        public Void Forecast(String[] s) {
            weekForecast.clear();
            for (String a : s) {
                weekForecast.add(a);
            }
            adapter.notifyDataSetChanged();
            progress.dismiss();
            return null;
        }
    }
}
