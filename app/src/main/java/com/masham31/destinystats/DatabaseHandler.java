package com.masham31.destinystats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by admin on 11/04/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DESTINY_DB_NAME = "destinyManager";
    private static final String TABLE_MEMBERS = "members";
    private static final String TABLE_STATS = "stats";

    private static final String KEY_ID = "_id"; // cursor 0
    private static final String KEY_MEMBER_ID = "memberId"; // cursor 1
    private static final String KEY_PLATFORM = "platform"; // cursor 2
    private static final String KEY_CHAR_ID = "characterId"; // cursor 3
    private static final String KEY_KD_ACTUAL = "kdActual"; // cursor 4
    private static final String KEY_KD_DISPLAY = "kdDisplay"; // cursor 5
    private static final String KEY_COMBAT_ACTUAL = "combatActual"; // cursor 6
    private static final String KEY_COMBAT_DISPLAY = "combatDisplay"; // cursor 7

    private static final String KEY_DISPLAY_NAME = "displayName";

    public DatabaseHandler(Context context){
        super(context, DESTINY_DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEMBERS_TABLE = "CREATE TABLE " + TABLE_MEMBERS
                + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_MEMBER_ID + " TEXT, "
                + KEY_DISPLAY_NAME + " TEXT);";
        String CREATE_STATS_TABLE = "CREATE TABLE " + TABLE_STATS + "(" + KEY_ID + "INTEGER PRIMARY KEY, " + KEY_MEMBER_ID
                + " TEXT, " + KEY_PLATFORM + " INTEGER, " + KEY_CHAR_ID + " TEXT, " + KEY_KD_ACTUAL + " REAL, " + KEY_KD_DISPLAY
                + " TEXT, " + KEY_COMBAT_ACTUAL + " REAL, " + KEY_COMBAT_DISPLAY + " TEXT);";

        Log.d("mitch", " db onCreate()");

        db.execSQL(CREATE_STATS_TABLE);
        db.execSQL(CREATE_MEMBERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);


        onCreate(db);
    }

    public void addMember(String memId, String displayName){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MEMBER_ID, memId);
        values.put(KEY_DISPLAY_NAME, displayName);

        db.insert(TABLE_MEMBERS, null, values);
        db.close();
    }

    public String getMember(String memId, String displayName) {

        String result = "";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MEMBERS + " WHERE " + KEY_MEMBER_ID + " = '" + memId + "'", null);

        if (cursor.getCount() > 0) {
            if (cursor != null) {
                cursor.moveToFirst();
                try {
                    result = cursor.getString(2);
                } catch (CursorIndexOutOfBoundsException e) {
                    Log.d("CursorError", e.toString());
                    result = "";
                }
            } else {
                result = "";

            }
        }
        return result;
    }

    public void addStats(String memId, String charId, int platform, float kdActual, float combatActual, String strKd, String strCombat){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MEMBER_ID, memId);
        values.put(KEY_PLATFORM, platform);
        values.put(KEY_CHAR_ID, charId);
        values.put(KEY_KD_ACTUAL, kdActual);
        values.put(KEY_KD_DISPLAY, strKd);
        values.put(KEY_COMBAT_ACTUAL, combatActual);
        values.put(KEY_COMBAT_DISPLAY, strCombat);

        db.insert(TABLE_STATS, null, values);
        db.close();
    }

    public StatsClass getStats(String memId, String charId, int platform) {
        SQLiteDatabase db = this.getReadableDatabase();
        StatsClass thisChar = new StatsClass();

        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_STATS + " WHERE " + KEY_MEMBER_ID + " = '" + memId + "' AND "
                + KEY_PLATFORM + " = " + platform + " AND " + KEY_CHAR_ID + " = '" + charId + "'", null);

        if(c.getCount() > 0){
            if(c != null){
                c.moveToFirst();
                try{

                    thisChar.setKdActual(c.getFloat(4));
                    thisChar.setKdDisplay(c.getString(5));
                    thisChar.setCombatActual(c.getFloat(6));
                    thisChar.setCombatDisplay(c.getString(7));


                } catch (CursorIndexOutOfBoundsException e){
                    Log.d("CursorError", e.toString());
                }
            }
        }

        return thisChar;
    }

    public void updateStats(StatsClass updateValues, String memId, String charId, int platform){
        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("UPDATE " + TABLE_STATS + " SET " + KEY_COMBAT_DISPLAY + " = " +
                updateValues.getCombatDisplay() + ", " + KEY_COMBAT_ACTUAL + " = " +
                updateValues.getCombatActual() + ", " + KEY_KD_DISPLAY + " = " +
                updateValues.getKdDisplay() + ", " + KEY_KD_ACTUAL + " = " +
                updateValues.getKdActual() + " WHERE " + KEY_MEMBER_ID + " = '" + memId + "' AND "
                + KEY_PLATFORM + " = " + platform + " AND " + KEY_CHAR_ID + " = '" + charId + "'", null);

    }
}
