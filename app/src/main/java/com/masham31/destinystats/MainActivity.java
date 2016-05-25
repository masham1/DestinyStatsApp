package com.masham31.destinystats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    protected int platform = 0;
    private final String apiKey = "8a5471095d0c44afbcb3e7bf6a9958d2";
    protected String userName = "";
    protected String ID = null;
    protected String displayName = "";
    protected EditText gamerTag;
    protected RadioGroup consoles;
    protected RadioButton xbone, ps;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xbone = (RadioButton)findViewById(R.id.xbox);
        ps = (RadioButton)findViewById(R.id.psn);

        consoles = (RadioGroup)findViewById(R.id.console);
        gamerTag = (EditText)findViewById(R.id.gamerTag);

        sharedPrefs = getSharedPreferences("saved", MODE_PRIVATE);

        if (sharedPrefs.contains("gamerTag")) {
            gamerTag.setText(sharedPrefs.getString("gamerTag", ""));
            platform = sharedPrefs.getInt("platform", 0);

            switch(platform) {
                case 0:
                    break;
                case 1:
                    xbone.setChecked(true);
                    break;
                case 2:
                    ps.setChecked(true);
                    break;
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sharedPrefs.contains("gamerTag")) {
            gamerTag.setText(sharedPrefs.getString("gamerTag", ""));
        }

        if (sharedPrefs.contains("platform")) {
            platform = sharedPrefs.getInt("platform", 0);

            switch(platform) {
                case 0:
                    break;
                case 1:
                    xbone.setChecked(true);
                    break;
                case 2:
                    ps.setChecked(true);
                    break;
            }
        }
    }

    public void processCharacter(View v) {
        switch (consoles.getCheckedRadioButtonId()) {
            case (R.id.psn):
                platform = 2;
                break;
            case (R.id.xbox):
                platform = 1;
                break;
            default:
                platform = 0;
                break;
        }

        userName = gamerTag.getText().toString().trim();
        if (isNetworkConnected()) {
            if (!(userName.isEmpty()) && userName != null) {
                if (platform != 0) {

                    APIRequest task = new APIRequest();
                    task.execute();
                } else {
                    Toast.makeText(this, "Please select a platform", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a gamer tag", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network connection required", Toast.LENGTH_SHORT).show();
        }
    }

    class APIRequest extends AsyncTask<Void, Void, Void> {
        private int result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Searching", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ID = getMembershipId();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            handleId(ID);
        }
    }

    public String getMembershipId() {
        URL url = null;
        String response = null;
        String memberID = null;

        try {
            url = new URL("http://www.bungie.net/Platform/Destiny/SearchDestinyPlayer/"+ platform +"/" + userName.trim() + "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("X-API-Key", apiKey);


        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();

            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            response = sb.toString();

            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jMembership = new JSONObject(response);
            JSONArray responseArray = jMembership.getJSONArray("Response");

            for (int i = 0; i < responseArray.length(); i++){
                try {
                    JSONObject oneObject = responseArray.getJSONObject(i);
                    memberID = oneObject.getString("membershipId");
                    displayName = oneObject.getString("displayName");
                } catch (JSONException e){

                }
            }

        } catch (JSONException e) {
            Log.d("JSONException", e.toString());
        }

        return memberID;
    }

    public void handleId(String accountId) {
        SharedPreferences.Editor editor = sharedPrefs.edit();


        if (accountId != null) {
            if (accountId.equalsIgnoreCase("0") || accountId.isEmpty()) {
                Toast.makeText(MainActivity.this, "Sorry! We can't find that profile", Toast.LENGTH_LONG).show();
                gamerTag.requestFocus();
            } else {

                editor.putString("gamerTag", userName);
                editor.putInt("platform", platform);
                editor.commit();

                Toast.makeText(MainActivity.this, "Loading characters for user: " + displayName, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, CharacterList.class);

                intent.putExtra("memberId", accountId);
                intent.putExtra("platform", platform);
                intent.putExtra("displayName", displayName);
                startActivity(intent);
            }
        } else {
            Toast.makeText(MainActivity.this, "Sorry! We can't find that profile", Toast.LENGTH_LONG).show();
            gamerTag.requestFocus();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
