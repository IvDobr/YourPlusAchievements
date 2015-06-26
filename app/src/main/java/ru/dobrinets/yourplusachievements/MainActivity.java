package ru.dobrinets.yourplusachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    SharedPreferences preferences;
    static final String COOKIE = "cookie";
    static final String APP_PREFERENCES = "app_settings";

    private class checkLogin extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                return isLogin();
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Next(result);
        }
    }

    private boolean isLogin() throws IOException {
        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String q = preferences.getString(COOKIE, "");
        String url = "http://192.168.0.104:9000/api";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setAllowUserInteraction(false);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Cookie", q);
        conn.setRequestProperty("Connection", "close");
        conn.connect();
        return 200 == conn.getResponseCode();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new checkLogin().execute();
        } else {
            Toast.makeText(getBaseContext(), "Нет соединения с интернетом!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void Next (Boolean status) {
        if (status) {
            Toast.makeText(getBaseContext(), "Залогинен", Toast.LENGTH_SHORT).show(); //НАЧИНАЯ ОТСЮДА
        } else {
            Intent intentAuth = new Intent(this, AuthActivity.class);
            startActivity(intentAuth);
            this.finish();
        }
    }
}
