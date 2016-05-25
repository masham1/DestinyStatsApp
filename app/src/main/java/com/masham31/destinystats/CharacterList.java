package com.masham31.destinystats;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CharacterList extends AppCompatActivity {

    protected String memberId, stringResponse, displayName, clanName;
    protected ArrayList<DestinyCharacter> charArray;
    protected int platform;
    private final String HUNTER = "671679327";
    private final String TITAN = "3655393761";
    private final String WARLOCK = "2271682572";
    private final String apiKey = "8a5471095d0c44afbcb3e7bf6a9958d2";
    protected TextView tv, clanTag;
    protected ArrayList<Bitmap> backgroundImgs, emblems;
    protected LinearLayout main;
    protected ProgressDialog loading = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_list);

        loading = new ProgressDialog(this);
        loading.setCancelable(true);
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.setMessage("Retrieving characters");

        charArray = new ArrayList<>();
        emblems = new ArrayList<>();
        backgroundImgs = new ArrayList<>();
        clanName = "";

        main = (LinearLayout)findViewById(R.id.charListLayout);
        tv = (TextView)findViewById(R.id.response);
        clanTag = (TextView)findViewById(R.id.clanTag);

        Bundle data = getIntent().getExtras();

        if (data != null) {

            memberId = data.getString("memberId");
            platform = data.getInt("platform");
            displayName = data.getString("displayName");

            tv.setText(displayName);
        }

        findCharacters task = new findCharacters();
        task.execute();

    }

    class findCharacters extends AsyncTask<Void, Void, Void>{
        private int result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            getCharacters();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            handleCharacters();
        }

    }

    public void getCharacters() {
        URL url = null;
        String response = null;
        String className = "";

        try {
            url = new URL("https://www.bungie.net/platform/destiny/"+ platform + "/account/" + memberId +"/summary/");

            Log.d("RequestURL", url.toString());

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
            JSONObject jResponse = new JSONObject(response);
            JSONObject jData = jResponse.getJSONObject("Response").getJSONObject("data");
            JSONArray jCharacters = jData.getJSONArray("characters");

            for(int i = 0; i < jCharacters.length(); i++){
                DestinyCharacter oneChar = new DestinyCharacter();

               try{


                   // all characters
                   JSONObject jCharacter = jCharacters.getJSONObject(i);

                   // single character
                   JSONObject jCharBase = jCharacter.getJSONObject("characterBase");

                   // determine class based on classHash vs. Constants
                   switch(Long.toString(jCharBase.getLong("classHash"))) {
                       case HUNTER:
                           className = "Hunter";
                           break;
                       case TITAN:
                           className = "Titan";
                           break;
                       case WARLOCK:
                           className = "Warlock";
                           break;
                   }

                   // Set Character attributes here
                   oneChar.setCharacterId(jCharBase.getString("characterId"));
                   oneChar.setLightLevel(Integer.toString(jCharBase.getInt("powerLevel")));
                   oneChar.setClassType(className);
                   oneChar.setBackgroundPath(jCharacter.getString("backgroundPath"));
                   oneChar.setEmblemPath(jCharacter.getString("emblemPath"));

                }catch(JSONException e){
                    Log.d("JSON Exception", e.toString());
                }

                charArray.add(oneChar);
            }


            if (jData.has("clanName")) {
                clanName = jData.getString("clanName");
            }


        } catch (JSONException e){
            Log.d("JSON Exception", e.toString());
        }
        return;
    }

    class loadEmblems extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listGuardians();
            loading.hide();

        }

        @Override
        protected Void doInBackground(Void... params) {
            String baseURL = "http://bungie.net";
            URL backgroundURL = null, emblemURL = null;

            for (int i = 0; i < charArray.size(); i++) {

                try {
                    backgroundURL = new URL(baseURL + charArray.get(i).backgroundPath);
                    emblemURL = new URL(baseURL + charArray.get(i).emblemPath);

                    InputStream backgroundIs = new BufferedInputStream(backgroundURL.openStream());
                    InputStream emblemIs = new BufferedInputStream(emblemURL.openStream());

                    backgroundImgs.add(BitmapFactory.decodeStream(backgroundIs));
                    emblems.add(BitmapFactory.decodeStream(emblemIs));

                    backgroundIs.close();
                    emblemIs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("ImageLoad", e.toString());
                }
            }

            return null;
        }
    }

    public void handleCharacters() {
        if (charArray.isEmpty()) {
            tv.setText("No characters were found");
        }else {

            loadEmblems loadTask = new loadEmblems();

            loadTask.execute();
        }
    }

    public void listGuardians() {
        RelativeLayout guardian = new RelativeLayout(this);
        ImageView emblem, background;
        TextView lightLevel, guardianClass;


        emblem = new ImageView(this);
        background = new ImageView(this);

        for(int i = 0; i < charArray.size(); i++){

            switch(i){
                case 0:
                    guardian = (RelativeLayout)findViewById(R.id.guardian0);
                    emblem = (ImageView)findViewById(R.id.emblem0);
                    background = (ImageView)findViewById(R.id.background0);
                    break;
                case 1:
                    guardian = (RelativeLayout)findViewById(R.id.guardian1);
                    emblem = (ImageView)findViewById(R.id.emblem1);
                    background = (ImageView)findViewById(R.id.background1);
                    break;
                case 2:
                    guardian = (RelativeLayout)findViewById(R.id.guardian2);
                    emblem = (ImageView)findViewById(R.id.emblem2);
                    background = (ImageView)findViewById(R.id.background2);
                    break;
            }

            lightLevel = new TextView(this);
            guardianClass = new TextView(this);

            lightLevel.setText(charArray.get(i).lightLevel + " Light");
            lightLevel.setTextColor(Color.WHITE);
            lightLevel.setPadding(emblem.getWidth() + 10, emblem.getHeight()/2, 0, 0);

            guardianClass.setText(charArray.get(i).classType);
            guardianClass.setTextColor(Color.WHITE);
            guardianClass.setPadding(emblem.getWidth() + 10, 0, 0, 0);
            guardianClass.setTextSize(20);

            emblem.setImageBitmap(emblems.get(i));


            background.setImageBitmap(backgroundImgs.get(i));

            guardian.addView(lightLevel);
            guardian.addView(guardianClass);

            if (clanName.isEmpty()){
                clanTag.setText(R.string.noClan);
            } else {
                clanTag.setText(clanName);
            }

        }
    }

    public void startCharActivity(View v){
        Intent intent = new Intent(CharacterList.this, CharacterStats.class);
        String guardianId = "";
        String guardianEmblem = "";
        String guardianBg = "";
        String guardianClass = "";
        String guardianLight = "";

        switch (v.getId()) {
            case R.id.guardian0:
                guardianId = charArray.get(0).characterId;
                guardianEmblem = charArray.get(0).emblemPath;
                guardianBg = charArray.get(0).backgroundPath;
                guardianClass = charArray.get(0).classType;
                guardianLight = charArray.get(0).lightLevel;
                break;
            case R.id.guardian1:
                guardianId = charArray.get(1).characterId;
                guardianEmblem = charArray.get(1).emblemPath;
                guardianBg = charArray.get(1).backgroundPath;
                guardianClass = charArray.get(1).classType;
                guardianLight = charArray.get(1).lightLevel;
                break;
            case R.id.guardian2:
                guardianId = charArray.get(2).characterId;
                guardianEmblem = charArray.get(2).emblemPath;
                guardianBg = charArray.get(2).backgroundPath;
                guardianClass = charArray.get(2).classType;
                guardianLight = charArray.get(2).lightLevel;

                break;
        }

        intent.putExtra("characterId", guardianId);
        intent.putExtra("membershipId", memberId);
        intent.putExtra("platform", platform);
        intent.putExtra("emblemPath", guardianEmblem);
        intent.putExtra("backgroundPath", guardianBg);
        intent.putExtra("guardianClass", guardianClass);
        intent.putExtra("lightLevel", guardianLight);


        startActivity(intent);

    }
}
