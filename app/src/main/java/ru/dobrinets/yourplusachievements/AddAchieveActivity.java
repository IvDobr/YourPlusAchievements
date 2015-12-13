package ru.dobrinets.yourplusachievements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAchieveActivity extends AppCompatActivity {

    String temp_checkedSubCat;
    String checkedSubCat;
    TextView checkedSubCatText;

    EditText addTitle;
    EditText addDate;
    EditText editText;

    AlertDialog alertDialog;

    static final String COOKIE = "cookie";
    static final String WEB_SERVICE = "web_service";
    static final String APP_PREFERENCES = "app_settings";
    SharedPreferences preferences;

    private class sendAchieve extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return sender();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERR", e.toString());
                return "e";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("ok")) {
                Toast.makeText(getBaseContext(), "Достижение сохранено!", Toast.LENGTH_SHORT).show();
                comeBack();
            } else if(result.equals("n")) {
                Toast.makeText(getBaseContext(), "Ошибка сервера", Toast.LENGTH_SHORT).show();
            } else if(result.equals("e")) {
                Toast.makeText(getBaseContext(), "Ошибка :( Попробуйте еще раз", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void comeBack() {
        this.finish();
    }

    private String sender() throws IOException {
        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String web = preferences.getString(WEB_SERVICE, "");
        String url = web + "/achievements";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String q = preferences.getString(COOKIE, "");
        conn.setRequestProperty("Cookie", q);
        conn.setRequestProperty("Connection", "keep-alive");

        JSONObject achiev = new JSONObject();
        try {
            achiev.put("achTitle", addTitle.getText());
            achiev.put("achDate", addDate.getText());
            achiev.put("achSubCat", checkedSubCat);
            achiev.put("achDop", editText.getText());
        } catch (JSONException je) {
            je.printStackTrace();
        }

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(achiev.toString());
        Log.d("MY", achiev.toString());
        wr.flush();

        if (200 == conn.getResponseCode()) {
            return "ok";
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
        setContentView(R.layout.activity_add_achieve);
        checkedSubCatText = (TextView) findViewById(R.id.checkedSubCatText);

        addTitle = (EditText) findViewById(R.id.addTitle);
        addDate = (EditText) findViewById(R.id.addDate);
        editText = (EditText) findViewById(R.id.editText);
    }

    public void onClickCreateAchieve(View v) {
        new sendAchieve().execute();
    }

    public void onClickSetCat(View v) {
        dialog();
    }

    private void dialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        final String[] groups = new String[]{
                "Научная деятельность",
                "Спортивная деятельность",
                "Творческая деятельность",
                "Общественная деятельность",
                "Успехи в учебе"
        };

        final List<List<String>> alliases = Arrays.asList(
                Arrays.asList(
                        "SubCat_1_1",
                        "SubCat_1_2",
                        "SubCat_1_3",
                        "SubCat_1_4",
                        "SubCat_1_5",
                        "SubCat_1_6",
                        "SubCat_1_7",
                        "SubCat_1_8",
                        "SubCat_1_9",
                        "SubCat_1_10"),

                Arrays.asList(
                        "SubCat_2_1",
                        "SubCat_2_2",
                        "SubCat_2_3",
                        "SubCat_2_4"),

                Arrays.asList(
                        "SubCat_3_1",
                        "SubCat_3_2",
                        "SubCat_3_3",
                        "SubCat_3_4",
                        "SubCat_3_5"),

                Arrays.asList(
                        "SubCat_4_1",
                        "SubCat_4_2",
                        "SubCat_4_3",
                        "SubCat_4_4",
                        "SubCat_4_5",
                        "SubCat_4_6",
                        "SubCat_4_7"),

                Arrays.asList(
                        "SubCat_5_1",
                        "SubCat_5_2",
                        "SubCat_5_3",
                        "SubCat_5_4")
        );

        ExpandableListView mainList = new ExpandableListView(this);

        Map<String, String> m;

        ArrayList<Map<String, String>> groupData  = new ArrayList<>();
        for (String group : groups) {
            m = new HashMap<>();
            m.put("cat", group);
            groupData.add(m);
        }
        String groupFrom[] = new String[] {"cat"};
        int groupTo[] = new int[] {android.R.id.text1};

        ArrayList<Map<String, String>> childDataItem;

        ArrayList<ArrayList<Map<String, String>>> childData = new ArrayList<>();
        for(List<String> listItem : alliases) {
            childDataItem = new ArrayList<>();
            for(String stringItem : listItem) {
                m = new HashMap<>();
                m.put("subCat", "\n" + preferences.getString(stringItem, "") + "\n");
                childDataItem.add(m);
            }
            childData.add(childDataItem);
        }
        String childFrom[] = new String[] {"subCat"};
        int childTo[] = new int[] {android.R.id.text1};

        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                childData,
                android.R.layout.simple_list_item_1,
                childFrom,
                childTo);

        mainList.setAdapter(adapter);

        ExpandableListView.OnChildClickListener onChildClickListener = new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                try {
                    temp_checkedSubCat = alliases.get(groupPosition).get(childPosition);
                    checkedSubCat = temp_checkedSubCat;
                    checkedSubCatText.setText(preferences.getString(checkedSubCat, ""));
                    Toast.makeText(getBaseContext(), "Категория выбрана",
                        Toast.LENGTH_LONG).show();
                    checkCat();
                } catch (Exception e) {
                    Log.e("Error", e.toString());
                }
                return false;
            }
        };

        mainList.setOnChildClickListener(onChildClickListener);

        dialogBuilder.setTitle("Выбор категории");
        dialogBuilder.setView(mainList);
//        dialogBuilder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int arg1) {
//                checkedSubCat = temp_checkedSubCat;
//                checkedSubCatText.setText(preferences.getString(checkedSubCat, ""));
//                Toast.makeText(getBaseContext(), "Категория выбрана",
//                        Toast.LENGTH_LONG).show();
//            }
//        });
        dialogBuilder.setCancelable(true);
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(getBaseContext(), "Вы ничего не выбрали",
                        Toast.LENGTH_LONG).show();
            }
        });

        alertDialog = dialogBuilder.show();
    }

    private void checkCat(){
        alertDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_achieve, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
