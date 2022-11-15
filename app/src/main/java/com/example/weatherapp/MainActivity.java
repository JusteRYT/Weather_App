package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCity;
    private TextView textViewWeather;
    private TextView Time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextCity = findViewById(R.id.editTextCity);
        textViewWeather = findViewById(R.id.textViewShow);
        Time = findViewById(R.id.textView2);
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
    }

    public void SetTime (){
        runOnUiThread(() -> {
            try {
                Date date = new Date();
                Time.setText(DateFormat.getTimeInstance().format(date));
            } catch (Exception e){}
        });
    }
    class CountDownRunner implements Runnable{

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    SetTime();
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }catch (Exception e){}
            }
        }
    }
    public void ShowWeather(View view) {
        String city = editTextCity.getText().toString().trim();
        if (!city.isEmpty()){
            DownloadWeatherTask task = new DownloadWeatherTask();
            String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=8bcfde1214fc4399ce708d369fc9ae3a&lang=ru&units=metric";
            String url = String.format(weatherUrl,city);
            task.execute(url);
        }
    }

    private class DownloadWeatherTask extends AsyncTask <String, Void , String>{
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while (line != null){
                        result.append(line);
                        line = reader.readLine();
                    }
                    return result.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String city = jsonObject.getString("name");
                String temp;
                temp = jsonObject.getJSONObject("main").getString("temp");
                String temp1 = temp.substring(0, temp.indexOf("."));
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                String weather = String.format("Город: %s\nТемпература: %s\nНа улице: %s", city, temp1, description);
                textViewWeather.setText(weather);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}