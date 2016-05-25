package com.masham31.destinystats;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CharacterStats extends AppCompatActivity {

    protected String memberId, characterId, emblemPath, backgroundPath, guardianClass, lightLevel, strKillDeath, strTotalKills, strPrecisionKills, strCombatRating, strSpree;
    protected float  kDActual, combatActual;
    protected int platform;
    private final String apiKey = "8a5471095d0c44afbcb3e7bf6a9958d2";
    private TextView killDeath, totalKills, precisionKills, combatRating, killSpree;
    private ImageView kdImg, totalKillsImg, precisionImg, combatImg, killSpreeImg, emblem, background;
    private Bitmap bmUp, bmDown;
    private AssetManager assetManager;
    private InputStream iStr;
    private DatabaseHandler dbHandler;
    protected StatsClass returnedChar;
    protected ProgressDialog loading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_stats);

        dbHandler = new DatabaseHandler(CharacterStats.this);

        loading = new ProgressDialog(this);
        loading.setCancelable(true);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.setMessage("Retrieving stats");

        //TextViews
        killDeath = (TextView)findViewById(R.id.killDeath);
        totalKills = (TextView)findViewById(R.id.totalKills);
        precisionKills = (TextView)findViewById(R.id.precisionKills);
        combatRating = (TextView)findViewById(R.id.combatRating);
        killSpree = (TextView)findViewById(R.id.killSpree);

        //ImageViews
        kdImg = (ImageView)findViewById(R.id.kdImg);
        totalKillsImg = (ImageView)findViewById(R.id.totalKillsImg);
        precisionImg = (ImageView)findViewById(R.id.precisionImg);
        combatImg = (ImageView)findViewById(R.id.combatImg);
        killSpreeImg = (ImageView)findViewById(R.id.spreeImg);

        //Get bundled data from intent
        Bundle data = getIntent().getExtras();

        if (data != null) {

            memberId = data.getString("membershipId");
            platform = data.getInt("platform");
            characterId = data.getString("characterId");
            emblemPath = data.getString("emblemPath");
            backgroundPath = data.getString("backgroundPath");
            guardianClass = data.getString("guardianClass");
            lightLevel = data.getString("lightLevel");
        }

        LoadStats task = new LoadStats();

        task.execute();
    }

    class LoadStats extends AsyncTask<Void, Void, Void> {
        private int result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            if (strKillDeath != null) {

                killDeath.setText(strKillDeath);
                totalKills.setText(strTotalKills);
                precisionKills.setText(strPrecisionKills);
                combatRating.setText(strCombatRating);
                killSpree.setText(strSpree);

                testImprovement();
            } else {
                finish();
                Toast.makeText(CharacterStats.this, "No stats for this character!", Toast.LENGTH_LONG).show();
            }

            loading.dismiss();

        }

        @Override
        protected Void doInBackground(Void... params) {

            URL url = null;
            String response = null;

            try {
                url = new URL("http://www.bungie.net/Platform/Destiny/Stats/" + platform + "/" + memberId + "/" + characterId + "/?modes=AllPvP");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
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
                Log.d("StatResponse", response.toString());

                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                JSONObject jResponseObject = new JSONObject(response);
                JSONObject jResponseArray = jResponseObject.getJSONObject("Response").getJSONObject("allPvP").getJSONObject("allTime");

                if (jResponseArray.has("killsDeathsRatio")) {
                    strKillDeath = jResponseArray.getJSONObject("killsDeathsRatio").getJSONObject("basic").getString("displayValue");
                    kDActual = BigDecimal.valueOf(jResponseArray.getJSONObject("killsDeathsRatio").getJSONObject("basic").getDouble("value")).floatValue();

                    strTotalKills = jResponseArray.getJSONObject("kills").getJSONObject("basic").getString("displayValue");
                    strPrecisionKills = jResponseArray.getJSONObject("precisionKills").getJSONObject("basic").getString("displayValue");

                    strCombatRating = jResponseArray.getJSONObject("combatRating").getJSONObject("basic").getString("displayValue");
                    combatActual = BigDecimal.valueOf(jResponseArray.getJSONObject("combatRating").getJSONObject("basic").getDouble("value")).floatValue();

                    strSpree = jResponseArray.getJSONObject("longestKillSpree").getJSONObject("basic").getString("displayValue");

                    dbHandler.addStats(memberId, characterId, platform, kDActual, combatActual, strKillDeath, strCombatRating);
                } else {
                    strKillDeath = null;
                }


            } catch (JSONException e) {
                Log.d("JSONException", e.toString());

            }
            return null;
        }
    }

    public void testImprovement() {
        returnedChar = dbHandler.getStats(memberId, characterId, platform);
        StatsClass updateChar = new StatsClass();

        try {
            //Load in images
            assetManager = getAssets();
            iStr = assetManager.open("down.png");
            bmDown = BitmapFactory.decodeStream(iStr);
            iStr = assetManager.open("up.png");
            bmUp = BitmapFactory.decodeStream(iStr);

            Log.d("RETURNS", "JSON KD: " + kDActual);
            Log.d("RETURNS", "DB KD: " + returnedChar.getKdActual());

            if (kDActual < returnedChar.getKdActual()) {
                kdImg.setImageBitmap(bmDown);
            } else if (kDActual > returnedChar.getKdActual()) {
                kdImg.setImageBitmap(bmUp);
            } else if (kDActual == returnedChar.getKdActual()) {
                kdImg.setImageBitmap(null);
                kdImg.setVisibility(View.INVISIBLE);
            }

            if (combatActual < returnedChar.getCombatActual()) {
                combatImg.setImageBitmap(bmDown);
            } else if (combatActual > returnedChar.getCombatActual()) {
                combatImg.setImageBitmap(bmUp);
            } else {
                combatImg.setVisibility(View.INVISIBLE);
            }

        } catch (IOException e) {
//            Log.d("IMGLoad", e.toString());
        }

        updateChar.setCombatDisplay(strCombatRating);
        updateChar.setCombatActual(combatActual);
        updateChar.setKdDisplay(strKillDeath);
        updateChar.setKdActual(kDActual);

//        Log.d("UPDATECHAROBJ", Integer.toString(returnedChar.getId()));
//        Log.d("UPDATECHAROBJ", updateChar.getCombatDisplay());
//        Log.d("UPDATECHAROBJ", updateChar.getKdDisplay());
//        Log.d("UPDATECHAROBJ", Float.toString(updateChar.getCombatActual()));

        dbHandler.updateStats(updateChar, memberId, characterId, platform);
    }
}
