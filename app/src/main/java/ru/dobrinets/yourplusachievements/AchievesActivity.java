package ru.dobrinets.yourplusachievements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AchievesActivity extends AppCompatActivity {

    static final String COOKIE = "cookie";
    static final String WEB_SERVICE = "web_service";
    static final String APP_PREFERENCES = "app_settings";
    SharedPreferences preferences;

    ListView list;

    Integer stipId;

    private class loadList extends AsyncTask<String, Void, String> {
        BufferedReader reader = null;

        @Override
        protected String doInBackground(String... urls) {
            try {
                preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                String web = preferences.getString(WEB_SERVICE, "");
                String url = web + "/achievements?pageSize=" + 500;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                String q = preferences.getString(COOKIE, "");
                conn.setRequestProperty("Cookie", q);

                conn.connect();
                InputStream inputStream = conn.getInputStream();
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERR", e.toString());
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            JSONObject dataJsonObj = null;
            try {
                dataJsonObj = new JSONObject(strJson);
                JSONArray aches = dataJsonObj.getJSONArray("aches");
                createList(aches);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void createList(JSONArray aches) {
        try {
            final ArrayList<HashMap<String, String>> achesList = new ArrayList<>();

            for (int i = 0; i < aches.length(); i++) {
                HashMap<String, String> hm = new HashMap<String, String>();

                hm.put("achCat", aches.getJSONObject(i).getString("achCat"));
                hm.put("achComment", aches.getJSONObject(i).getString("achComment"));
                hm.put("achDate", aches.getJSONObject(i).getString("achDate"));
                hm.put("achDop", aches.getJSONObject(i).getString("achDop"));
                hm.put("achId", aches.getJSONObject(i).getString("achId"));
                hm.put("achPremStatus",
                        parseStatus( aches.getJSONObject(i).getInt("achPremStatus") )
                );
                hm.put("achStipStatus",
                        parseStatus( aches.getJSONObject(i).getInt("achStipStatus") )
                );
                hm.put("achSubCat", aches.getJSONObject(i).getString("achSubCat"));
                hm.put("achTitle", aches.getJSONObject(i).getString("achTitle"));

                String status = "Премия: " + hm.get("achPremStatus") + " / Стипендия: " + hm.get("achStipStatus") ;

                hm.put("status", status);
                achesList.add(hm);
            }

            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    achesList,
                    android.R.layout.simple_list_item_2,
                    new String[] {"achTitle", "status"},
                    new int[] {android.R.id.text1, android.R.id.text2}
            );

            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent intent = new Intent(parent.getContext(), SeeAchieveActivity.class);

                    HashMap<String, String> item = achesList.get((int)id);
                    for(Map.Entry<String, String> entry : item.entrySet()) {
                        intent.putExtra(entry.getKey(), entry.getValue());
                    }

                    startActivity(intent);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String parseStatus(int i) {
        switch (i){
            case -1:
                return "НЕ принято";
            case 1:
                return "ПРИНЯТО";
            default:
                return "Неизвестно";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieves);
        list = (ListView) findViewById(R.id.listView);
        new loadList().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_achieves, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new loadList().execute();
            return true;
        }

        if (id == R.id.action_add_achieve) {
            Intent intent = new Intent(this, AddAchieveActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_stip) {
            setStip();
            return true;
        }

        if (id == R.id.action_set_server) {
            setServerDialog();
            return true;
        }

        if (id == R.id.action_exit) {
            preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(COOKIE);
            editor.commit();
            this.finish();
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void setServerDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText newServer = (EditText) promptView.findViewById(R.id.edittext);
        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        newServer.getText().append(preferences.getString(WEB_SERVICE, ""));

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove(WEB_SERVICE);
                        editor.putString(WEB_SERVICE, newServer.getText().toString());
                        editor.commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

    private void setStip() {
        String[] stips = new String[] {
                "Отменить выбор",
                "Научная деятельность",
                "Спортивная деятельность",
                "Творческая деятельность",
                "Общественная деятельность",
                "Успехи в учебе"
        };

        ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, stips);
        listView.setAdapter(adapter);


        LayoutInflater layoutInflater = LayoutInflater.from(this);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(listView);

        ListView.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new setterStip().execute();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

    private class setterStip extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                String web = preferences.getString(WEB_SERVICE, "");
                String url = web + "/achievements?pageSize=" + 500;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestMethod("POST");

                JSONObject stip = new JSONObject();
                try {
                    stip.put("stip", stipId);
                } catch (JSONException je) {
                    je.printStackTrace();
                }

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(stip.toString());
                wr.flush();

                if (200 == conn.getResponseCode()) {
                    return "ok";
                } else if (400 == conn.getResponseCode()) {
                    return "n";
                }
                else {
                    throw new IOException(conn.getResponseMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERR", e.toString());
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("ok")) {
                Toast.makeText(getBaseContext(), "Стипендия сохранена!", Toast.LENGTH_SHORT).show();
            } else if (result.equals("n")) {
                Toast.makeText(getBaseContext(), "Ошибка сервера", Toast.LENGTH_SHORT).show();
            } else if (result.equals("e")) {
                Toast.makeText(getBaseContext(), "Ошибка :( Попробуйте еще раз", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        new loadList().execute();
        super.onResume();
    }
}
