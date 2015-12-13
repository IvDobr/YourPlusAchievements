package ru.dobrinets.yourplusachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeeAchieveActivity extends AppCompatActivity {

    ListView list;

    Intent intent;

    static final String COOKIE = "cookie";
    static final String WEB_SERVICE = "web_service";
    static final String APP_PREFERENCES = "app_settings";
    SharedPreferences preferences;

    private class deleteAchieve extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                String web = preferences.getString(WEB_SERVICE, "");
                String url = web + "/achievements/" + intent.getStringExtra("achId");
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("DELETE");

                preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                String q = preferences.getString(COOKIE, "");
                conn.setRequestProperty("Cookie", q);

                if (200 == conn.getResponseCode()) {
                    return "ok";
                } else if (400 == conn.getResponseCode()) {
                    return "n";
                }
                else {
                    throw new IOException(conn.getResponseMessage());
                }
            } catch (IOException e) {
                Log.e("Удаление неудачно: ", e.toString());
                return "e";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("ok")) {
                Toast.makeText(getBaseContext(), "Достижение удалено!", Toast.LENGTH_SHORT).show();
                back();
            } else if(result.equals("")) {
                Toast.makeText(getBaseContext(), "Ошибка сервера", Toast.LENGTH_SHORT).show();
            } else if(result.equals("e")) {
                Toast.makeText(getBaseContext(), "Ошибка :( Попробуйте еще раз", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_achieve);

        intent = getIntent();
        ArrayList<HashMap<String, String>> achieve = new ArrayList<>();

        achieve.add( mapper("Название", intent.getStringExtra("achTitle")) );
        achieve.add( mapper("Дата", intent.getStringExtra("achDate")) );
        achieve.add( mapper("Категория", intent.getStringExtra("achCat")) );

        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        achieve.add( mapper("Подкатегория", preferences.getString(intent.getStringExtra("achSubCat"), "")) );
        achieve.add( mapper("Доп. информация", intent.getStringExtra("achDop")) );
        //achieve.add( mapper("id", intent.getStringExtra("achId")) );
        achieve.add( mapper("На премию", intent.getStringExtra("achPremStatus")) );
        achieve.add( mapper("На стипендию", intent.getStringExtra("achStipStatus")) );
        achieve.add( mapper("Комментарий проверяющего", intent.getStringExtra("achComment")) );

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                achieve,
                android.R.layout.simple_list_item_2,
                new String[] {"name", "value"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        list = (ListView) findViewById(R.id.listView2);

        list.setAdapter(adapter);
    }

    private static HashMap<String, String> mapper(String key, String val) {
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put("name", key);
        temp.put("value", val);
        return temp;
    }

    private static boolean canDel(Intent intent) {
        boolean a = !intent.getStringExtra("achPremStatus").equals("ПРИНЯТО");
        boolean b = !intent.getStringExtra("achStipStatus").equals("ПРИНЯТО");
        return a & b;
    }

    private void back() {
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (canDel(intent)) {
            getMenuInflater().inflate(R.menu.menu_see_achieve, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_achieve) {
            new deleteAchieve().execute();
            return true;
        }

        if (id == R.id.action_edit_achieve) {
            Toast.makeText(this, "Действие пока что не реализовано :(", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
