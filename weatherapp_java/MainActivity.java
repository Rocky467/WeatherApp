package com.example.rakesh.weatherapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
// text to voice
    Button button;
    private TextToSpeech textToSpeech;
// weather
    EditText city;
    TextView result,date,temperature;
// progress bar
    ProgressBar progressBar;
// change background
    ConstraintLayout layout;
// degree celsius
    ImageView imageView;
// voice to text
    private final int REQ_CODE_VOICE_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//      Alert for No Internet
        if (!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();

        button = findViewById(R.id.button);

        city = findViewById(R.id.getcity);
        temperature = findViewById(R.id.temp);
        date = findViewById(R.id.date);
        result = findViewById(R.id.result);

        progressBar = findViewById(R.id.progressBar);

        layout = findViewById(R.id.layout);

        imageView = findViewById(R.id.imageView2);
        imageView.animate().alpha(0).setDuration(0);

//      To show date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = simpleDateFormat.format(calendar.getTime());
        date.setText(formattedDate);

//      To speak a weather
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "Language not supported!", Toast.LENGTH_SHORT).show();
                    } else {
                        button.setEnabled(true);
                        voiceOutput();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Initilization failed!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

//      check weather button method
        public void check(View view) {
//      Input conditions
        String name = city.getText().toString().trim();
        if (!name.equals("")) {
            Toast.makeText(MainActivity.this, "Loading Please wait...", Toast.LENGTH_SHORT).show();
        }
        if (name.equals("")){
            Toast.makeText(MainActivity.this, "Please enter city", Toast.LENGTH_SHORT).show();
            return;
        }

//      Progress bar
        Thread thread = new Thread(){
                    @Override
                    public void run(){
                        super.run();
                        for(int i = 0;i<=100;){
                            try{
                                sleep(500);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                            progressBar.setProgress(i);
                            i = i+10;
                        }
                    }
        };
        thread.start();

//      parsing weather data online by JSON
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city.getText().toString() + "&appid=54b0ff6a15e410011d01ad275021ba6f&units=Imperial";

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener <JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
//                              Weather
                                JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                                String description = weather.getString("description");
                                result.setText(description);

//                              Temperature
                                JSONObject main = response.getJSONObject("main");
                                String temp = String.valueOf(main.getDouble("temp"));
                                double degreeCelsius = (Double.parseDouble(temp) - 32) / 1.8;
                                int i = (int) Math.round(degreeCelsius);
                                temperature.setText(String.valueOf(i));
                                imageView.animate().alpha(1).setDuration(0);

                                 voiceOutput();

//                          change wallpaper
                            if (result.getText().toString().contains("clear")){
                                layout.setBackgroundResource(R.drawable.clear);
                            }
                            if (result.getText().toString().contains("clouds")){
                                layout.setBackgroundResource(R.drawable.cloudy);
                            }
                            if (result.getText().toString().contains("rain")||result.getText().toString().contains("drizzle")){
                                layout.setBackgroundResource(R.drawable.rain);
                            }
                            if (result.getText().toString().contains("mist")||result.getText().toString().contains("haze")){
                                layout.setBackgroundResource(R.drawable.haze);
                            }
                            if (result.getText().toString().contains("smoke")){
                                layout.setBackgroundResource(R.drawable.smoke);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
                }

                );
                 RequestQueue queue = Volley.newRequestQueue(this);
                 queue.add(jsonObjectRequest);

    }

//  Speak weather method
    private void voiceOutput() {

        if (!isConnected(MainActivity.this)) {
            CharSequence warning = "sorry, i detect there is no internet connection right now";
            textToSpeech.speak(warning, TextToSpeech.QUEUE_FLUSH, null, "id1");
        }

        if (isConnected(MainActivity.this)) {
            CharSequence intro = "hi, please enter the name of your city, and i will tell you hows a weather out there";
            textToSpeech.speak(intro, TextToSpeech.QUEUE_FLUSH, null, "id1");
        }

        if (!city.getText().toString().isEmpty() &&!temperature.getText().toString().isEmpty()) {
            CharSequence text = city.getText() + " city is having " + result.getText() + " with temperature" + temperature.getText() + " degree celsius";
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1");
        }
    }

//  shutdown TextToSpeech
    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

//  voice input method
    public void say(View view) {
        getVoiceInput();
    }

    private void getVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak name of your city !");
        try {
            startActivityForResult(intent, REQ_CODE_VOICE_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_VOICE_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    city.setText(result.get(0));
                }
                break;
            }

        }
    }

//  code to check Internet connectivity
  public boolean isConnected(Context context) {

     ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

     if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
        android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
        else return false;
     } else
        return false;
 }

    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please connect to the Internet");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder;
    }

//  Alert to exit on backButton pressed
    public void onBackPressed(){
        buildDialogExit(MainActivity.this).show();
    }

    public AlertDialog.Builder buildDialogExit(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Do you really want to exit?");
        builder.setMessage("");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        return builder;
    }

}
