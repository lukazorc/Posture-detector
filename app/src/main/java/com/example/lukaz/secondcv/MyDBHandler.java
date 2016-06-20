package com.example.lukaz.secondcv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "measurements.db";
    public static final String TABLE_MEASUREMENTS = "measurements";
    public static final String COLUMN_CAS = "_cas";
    public static final String COLUMN_RESULTS = "result";
    public static final String COLUMN_KOT1 = "kot1";
    public static final String COLUMN_KOT2 = "kot2";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_MEASUREMENTS + "(" +
                COLUMN_CAS + " TEXT PRIMARY KEY ," +
                COLUMN_RESULTS + " INTEGER, " +
                COLUMN_KOT1 + " TEXT, " +
                COLUMN_KOT2 + " TEXT " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEASUREMENTS);
        onCreate(db);
    }

    //Add new row to database
    public void addMeasurement(Measurements measurement){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CAS, measurement.get_cas());
        values.put(COLUMN_RESULTS, measurement.get_result());
        values.put(COLUMN_KOT1, measurement.get_kot1());
        values.put(COLUMN_KOT2, measurement.get_kot2());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_MEASUREMENTS, null, values);
        db.close();
    }

    //Delete product from the database
    /*public void deleteProduct(String productName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MEASUREMENTS + " WHERE " + COLUMN_RESULTS + "=\"" + productName + "\";");
    }*/

    //Print out the database
    public String[] databaseToString(String date){
        String stOpozoril = "";
        String stSuccess = "";
        String odstotekString = "";
        String[] rezultat = {"",""};

        SQLiteDatabase db =  getWritableDatabase();
        String query = "SELECT COUNT(*) AS STEVILO FROM " + TABLE_MEASUREMENTS + " WHERE " + COLUMN_CAS + " >= '"+ date + "'" ;
        String query2 = "SELECT COUNT(*) AS TOTAL FROM " + TABLE_MEASUREMENTS + " WHERE " + COLUMN_CAS + " >= '"+ date + "' AND " + COLUMN_RESULTS + "== 0" ;
        //Cursor point to a location in your result
        Cursor c = db.rawQuery(query, null);
        //Move to first row in your result
        c.moveToFirst();

        //Position after the last row means the end of the results
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex("STEVILO")) != null) {
                stOpozoril += c.getString(c.getColumnIndex("STEVILO"));
            }
            c.moveToNext();
        }
        db.close();

        SQLiteDatabase db2 =  getWritableDatabase(); //TO SPLOH RABI?
        Cursor c2 = db2.rawQuery(query2, null);

        //Move to first row in your result
        c2.moveToFirst();

        //Position after the last row means the end of the results
        while (!c2.isAfterLast()) {
            if (c2.getString(c2.getColumnIndex("TOTAL")) != null) {
                stSuccess += c2.getString(c2.getColumnIndex("TOTAL"));
            }
            c2.moveToNext();
        }
        db2.close();

        DecimalFormat df = new DecimalFormat("##.#"); //format zapisa
        double odstotek = (Double.parseDouble(stSuccess)/Double.parseDouble(stOpozoril))*100;
        odstotekString = String.valueOf(df.format(odstotek));
        rezultat[0] = stOpozoril;
        if (stOpozoril.equals("0")) {
            rezultat[1] = "0";
        }
        else {
            rezultat[1] = odstotekString;
        }

        return rezultat;
    }

    //Print out the database for the graph
    public ArrayList<Integer> databaseToStringForGraph(String date){
        ArrayList<Integer> successes = new ArrayList<Integer>();

        String query = "SELECT " + COLUMN_RESULTS + " AS REZULTATI FROM " + TABLE_MEASUREMENTS + " WHERE " + COLUMN_CAS + " >= '"+ date + "'" ;
        //Cursor point to a location in your result

        SQLiteDatabase db =  getWritableDatabase(); //TO SPLOH RABI?
        Cursor c = db.rawQuery(query, null);

        //Move to first row in your result
        c.moveToFirst();

        //Position after the last row means the end of the results
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex("REZULTATI")) != null) {
                successes.add(c.getInt((c.getColumnIndex("REZULTATI"))));
            }
            c.moveToNext();
        }
        db.close();

        return successes;
    }
}
