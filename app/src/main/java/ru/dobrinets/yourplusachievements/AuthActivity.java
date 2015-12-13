package ru.dobrinets.yourplusachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private EditText login;
    private EditText password;

    static final String COOKIE = "cookie";
    static final String WEB_SERVICE = "web_service";
    static final String APP_PREFERENCES = "app_settings";
    SharedPreferences preferences;


    private class loginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return logInProc();
            } catch (IOException e) {
                Log.e("Авторизация", e.toString());
                return "e";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("n")) {
                Toast.makeText(getBaseContext(), "Неверный логин или пароль!", Toast.LENGTH_SHORT).show();
            } else if(result.equals("e")) {
                Toast.makeText(getBaseContext(), "Ошибка :( Попробуйте еще раз", Toast.LENGTH_SHORT).show();
            } else if(!result.equals("")) {

                preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(COOKIE, result);
                editor.commit();

                goToList();
            }
        }
    }

    private String logInProc() throws IOException {
        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String web = preferences.getString(WEB_SERVICE, "");

        String url = web + "/login";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestMethod("POST");

        JSONObject auth = new JSONObject();
        try {
            auth.put("userLogin", login.getText());
            auth.put("userPass", password.getText());
        } catch (JSONException je) {
            je.printStackTrace();
        }

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(auth.toString());
        wr.flush();

        if (200 == conn.getResponseCode()) {
            final String COOKIES_HEADER = "Set-Cookie";
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            String q = "";
            if(cookiesHeader != null)
            {
                for (String cookie : cookiesHeader)
                {
                    q = HttpCookie.parse(cookie).get(0).toString();
                }
            }
            conn.disconnect();
            return q;
        } else if (400 == conn.getResponseCode()) {
            return "n";
        }
        else {
            throw new IOException(conn.getResponseMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        login = (EditText) findViewById(R.id.loginInput);
        password = (EditText) findViewById(R.id.passInput);
    }

    public void onBtnClick(View v) {
        new loginTask().execute();
    }

    private void goToList(){
        Intent intent = new Intent(this, AchievesActivity.class);
        startActivity(intent);
        this.finish();
    }
}
