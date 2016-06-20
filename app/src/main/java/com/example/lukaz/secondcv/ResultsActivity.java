package com.example.lukaz.secondcv;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ResultsActivity extends AppCompatActivity {

    private static final String TAG = "Rezultati";

    MyDBHandler dbHandler;
    TextView uspesnost;
    TextView steviloOpozoril;
    TextView trajanjeMer;
    TextView recordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        uspesnost = (TextView)findViewById(R.id.uspesnostSed);
        steviloOpozoril = (TextView)findViewById(R.id.steviloOpozoril);
        trajanjeMer = (TextView)findViewById(R.id.trajanjeMer);
        recordText = (TextView)findViewById(R.id.recordText);

        dbHandler = new MyDBHandler(this, null, null, 1);

        Date date;
        Date date2;
        String diffFinal;
        String stariRekord;
        String odstotekString ="0";// odstotek pravilnega sedenja
        ArrayList<Integer> odcitki;

        String zacetek = readDateFromFile(); //dobimo iz txt datoteke cas zacetka merjenja sedenja
        try {
            odstotekString = printDatabase(zacetek); //prikazemo podatke iz baze in dobimo odstotek pravlnega sedenja
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }

        double odstotek = Double.parseDouble(odstotekString);
        try{
            if(readRecord() != "") { //iz txt datoteke dobimo stari rekord sedenja, ce obstaja
                stariRekord = readRecord();
            }
            else stariRekord = "0";
        }
        catch ( Exception e ){
            stariRekord = "0";
        }
        double stariR = Double.parseDouble(stariRekord);

        if(odstotek > stariR) { //preverimo ce smo naredili nov rekord in posodobimo starega
            saveRecord(odstotekString);
            recordText.setText("Nov rekord! (Stari: " + stariRekord + "%)");
        }
        else {
            recordText.setText("Rekord je " + stariRekord + "%");
        }

        DateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        String zdaj = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date());
        // merjenje koliko casa merimo sedenje
        try{
            date = df.parse(zacetek); //zacetek
            date2 = df.parse(zdaj); //zdaj
            long differenceMS = date2.getTime() - date.getTime(); //milliseconds
            double differenceS = differenceMS / 1000; //seconds
            double differenceM = differenceS / 60; //minuts
            int hours = (int) (differenceM / 60); //hours
            double minutsD = differenceM % 60; //minuts < 60
            int minuts = (int) minutsD; // minuts in int
            int secondsS = (int) differenceS % 60; // seconds < 60
            diffFinal = "" + String.format("%02d", hours) + ":" + String.format("%02d", minuts) + ":" + secondsS; //string ki prikazuje trajanje pravilno
            trajanjeMer.setText(diffFinal);
        }
        catch ( Exception e ){
            Log.e(TAG, e.toString());
        }

        odcitki = dbHandler.databaseToStringForGraph(zacetek);
        int steviloOdcitkov = odcitki.size();
        int ena6 =0;
        int dva6 =0;
        int tri6 =0;
        int stiri6 =0;
        int a = (int)(steviloOdcitkov*(1.0/4.0));
        for(int i = 0; i< (int)(steviloOdcitkov*(1.0/4.0)); i++) {
            if (odcitki.get(i)==1) {
                ena6 = ena6+odcitki.get(i);
            }
        }

        for(int i = (int)(steviloOdcitkov*(1.0/4.0)); i< (int)(steviloOdcitkov*(2.0/4.0)); i++) {
            if (odcitki.get(i)==1) {
                dva6 = dva6+odcitki.get(i);
            }
        }
        for(int i = (int)(steviloOdcitkov*(2.0/4.0)); i< (int)(steviloOdcitkov*(3.0/4.0)); i++) {
            if (odcitki.get(i)==1) {
                tri6 = tri6+odcitki.get(i);
            }
        }
        for(int i = (int)(steviloOdcitkov*(3.0/4.0)); i< steviloOdcitkov; i++) {
            if (odcitki.get(i)==1) {
                stiri6 = stiri6+odcitki.get(i);
            }
        }
        /*for(int i = steviloOdcitkov*(4/6); i< (steviloOdcitkov*(5/6)); i++) {

        }
        for(int i = steviloOdcitkov*(5/6); i< steviloOdcitkov; i++) {

        }*/

        GraphView graph = (GraphView) findViewById(R.id.graph);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(1, ena6),
                new DataPoint(2, dva6),
                new DataPoint(3, tri6),
                new DataPoint(4, stiri6)
        });
        series.setSpacing(50);
        graph.addSeries(series);
        double xInterval=1.0;
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(series.getHighestValueY() + 1);
        if (series instanceof BarGraphSeries ) {
            // Shunt the viewport, per v3.1.3 to show the full width of the first and last bars.
            graph.getViewport().setMinX(series.getLowestValueX() - (xInterval / 2.0));
            graph.getViewport().setMaxX(series.getHighestValueX() + (xInterval/2.0));
        } else {
            graph.getViewport().setMinX(series.getLowestValueX() );
            graph.getViewport().setMaxX(series.getHighestValueX());
        }
    }

    //prikazemo podatke iz baze in dobimo odstotek pravlnega sedenja
    public String printDatabase(String date) {
        String[] dbString = dbHandler.databaseToString(date);
        steviloOpozoril.setText(dbString[0]);
        uspesnost.setText(dbString[1] +"%");
        return dbString[1];
    }

    //dobimo iz txt datoteke cas zacetka merjenja sedenja
    private String readDateFromFile() {
        String ret = "";
        try {
            InputStream inputStream = openFileInput("dateFile.txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }

    // preberemo rekord sedenja z datoteke txt
    private String readRecord() {
        String ret = "";
        try {
            InputStream inputStream = openFileInput("recordFile.txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }

    // shrani rekord sedenja v datoteke txt
    public void saveRecord(String rekord) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(openFileOutput("recordFile.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(rekord);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }
}
