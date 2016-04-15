package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by Angad on 13/04/16.
 */
public class ForecastAsyncTask extends AsyncTask<String,Void,String[]> {
    ForecastAsyncTaskInterface listener;
    @Override
    protected String[] doInBackground(String... params) {
        String urlString = params[0];
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream data = urlConnection.getInputStream();
            Scanner s = new Scanner(data);
            StringBuffer output = new StringBuffer();
            while (s.hasNext()) {
                output.append(s.nextLine());
            }
            Log.i("output", output.toString());
            s.close();
            urlConnection.disconnect();
            return parseJson(output.toString());
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
    @Override
    protected void onPostExecute(String[] weather) {
        if (listener != null)
            listener.Forecast(weather);
    }

    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }


    private String[] parseJson(String jsonString) {
        try {
            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            JSONObject object = new JSONObject(jsonString);
            JSONArray forecastArray = object.getJSONArray("list");
            String[] output = new String[forecastArray.length()];
            for (int i = 0; i < forecastArray.length(); i++) {
                JSONObject dayForecast = forecastArray.getJSONObject(i);
                String s = new String();
                String day;
                String description;
                String highAndLow;
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);
                JSONObject weatherObject = dayForecast.getJSONArray("weather").getJSONObject(0);
                description = weatherObject.getString("description");
                JSONObject temperatureObject = dayForecast.getJSONObject("temp");
                double high = temperatureObject.getDouble("max");
                double low = temperatureObject.getDouble("min");
                highAndLow = formatHighLows(high, low);
                output[i] = day + " - " + description + " - " + highAndLow;

            }
            return output;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
