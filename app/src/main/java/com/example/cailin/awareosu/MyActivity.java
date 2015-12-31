package com.example.cailin.awareosu;

import java.util.*;
import java.lang.*;
import java.text.*;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Typeface;
import android.content.Intent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.view.View;
import android.app.DialogFragment;

public class MyActivity extends AppCompatActivity{
    public final static String EXTRA_MESSAGE = "com.example.cailin.MESSAGE";
    public String[] offCampusCrimes;
    public String[] onCampusCrimes;
    public String date;
    RetrieveCrimes asyncTask = new RetrieveCrimes();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Looking great today, Cailin!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
/*
        try {
            String[] info = new RetrieveCrimes().execute().get();
            // Make main thread wait until crime info is retrieved
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // We now have crime information
**/
        Intent i = getIntent();
        offCampusCrimes = i.getStringArrayExtra("off");
        onCampusCrimes = i.getStringArrayExtra("on");
        // Get web scraped info from splash screen

        Button onCampusButton = (Button) findViewById(R.id.onCampus_button);

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        offCampus(offCampusCrimes, dateFormat.format(cal.getTime()));
        onCampus(onCampusCrimes, dateFormat.format(cal.getTime()));
    }

    public void offCampus(String[] crimes, String dateFromSearch) {
        TableLayout offCampusTable = (TableLayout) findViewById(R.id.off_campus);
        offCampusTable.removeAllViews();
        TextView offMessage = (TextView) findViewById(R.id.off_message);
        TableLayout offCampusHeaderTable = (TableLayout) findViewById(R.id.offHeader_table);
        offCampusHeaderTable.removeAllViews();
        Button offCampusButton = (Button) findViewById(R.id.offCampus_button);
        String info = "";
        // Access off-campus table and create variables

        for (int i = 0; i < crimes.length; i += 5) {
            if (crimes[i] == null)
            {
                i = crimes.length;
            }
            else if (crimes[0].equals("down"))
            {
                offCampusButton.setText("Website down");
                offMessage.setText("The Columbus Police Department's website is currently down. Please be sure to " +
                    "check the CPD web portal (http://www.columbuspolice.org/reports/SearchLocation?" +
                    "loc=zon4\\) later today or tomorrow for any updates.");
                i = crimes.length;
            }
            else if (crimes[0].equals("empty"))
            {
                offCampusButton.setText("No Off-Campus Crimes reported for " + dateFromSearch);
                offMessage.setText("This is due to either the Columbus Police Department forgetting " +
                        "to upload crime information, or there being no crimes. Please be sure to " +
                        "check the CPD web portal (http://www.columbuspolice.org/reports/SearchLocation?" +
                        "loc=zon4\\) later today or tomorrow for any updates.");
                i = crimes.length;
            }
            else {
                if (i == 0)
                {
                    offCampusButton.setText("Off-Campus Crimes for " + dateFromSearch);
                    TableRow row = new TableRow(this);
                    // Create new row

                    TextView reportNumHeader = new TextView(this);
                    reportNumHeader.setText("Report Number");
                    reportNumHeader.setGravity(Gravity.LEFT);
                    reportNumHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(reportNumHeader);
                    // Add Report Number Header to header table

                    TextView IncidentTypeHeader = new TextView(this);
                    IncidentTypeHeader.setText("Incident Type");
                    IncidentTypeHeader.setGravity(Gravity.LEFT);
                    IncidentTypeHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(IncidentTypeHeader);
                    // Add Incident Type Header to header table

                    TextView locationHeader = new TextView(this);
                    locationHeader.setText("Location");
                    locationHeader.setGravity(Gravity.LEFT);
                    locationHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(locationHeader);
                    // Add Location Header to header table

                    TextView descriptionHeader = new TextView(this);
                    descriptionHeader.setText("Description");
                    descriptionHeader.setGravity(Gravity.LEFT);
                    descriptionHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(descriptionHeader);
                    // Add Description Header to header table

                    offCampusHeaderTable.addView(row);
                }
                TableRow row = new TableRow(this);
                // Create new row

                TextView reportNum = new TextView(this);
                reportNum.setHeight(50);
                reportNum.setMaxWidth(45);
                info = crimes[i];

                if (info != null && info.contains("null")) {
                    info = info.replaceAll("null", "");
                }
                reportNum.setText(info);
                row.addView(reportNum);
                // Add Report Number to row

                TextView incidentType = new TextView(this);
                incidentType.setHeight(50);
                incidentType.setMaxWidth(40);
                info = crimes[i + 1];

                if (info != null && info.contains("null")) {
                    info = info.replaceAll("null", "");
                }
                incidentType.setText(info);
                row.addView(incidentType);
                // Add Incident Type to row

                TextView location = new TextView(this);
                location.setHeight(50);
                location.setMaxWidth(40);
                info = crimes[i + 4];

                if (info != null && info.contains("null")) {
                    info = info.replaceAll("null", "");
                }
                location.setText(info);
                row.addView(location);
                // Add location to row

                TextView description = new TextView(this);
                description.setHeight(50);
                description.setMaxWidth(40);
                description.setText("-");
                row.addView(description);
                // Add location to row

                offCampusTable.addView(row);
            }
        }
    }

    public void onCampus(String[] crimes, String dateFromSearch) {
        TableLayout onCampusTable = (TableLayout) findViewById(R.id.on_campus);
        onCampusTable.removeAllViews();
        TableLayout onCampusHeaderTable = (TableLayout) findViewById(R.id.onHeader_table);
        onCampusHeaderTable.removeAllViews();
        TextView onMessage = (TextView) findViewById(R.id.on_message);
        Button onCampusButton = (Button) findViewById(R.id.onCampus_button);
        String info = "";
        // Access off-campus table and create variables

        for (int i = 0; i < crimes.length; i += 8) {
            if (crimes[i] == null)
            {
                i = crimes.length;
            }
            else if (crimes[0].equals("down"))
            {
                onCampusButton.setText("Website down");
                onMessage.setText("The OSU Police Department's website is currently down. " +
                        "Please be sure to check the OSU web portal " +
                        "(http://www.ps.ohio-state.edu/police/daily_log/) " +
                        "later today or tomorrow for any updates.");
                i = crimes.length;
            }
            else if (crimes[0].equals("empty"))
            {
                onCampusButton.setText("No On-Campus Crimes reported for " + dateFromSearch);
                onMessage.setText("This is due to either the OSU Police Department forgetting " +
                        "to upload crime information, or there being no crimes. Please be sure to " +
                        "check the OSU web portal (http://www.ps.ohio-state.edu/police/daily_log/) " +
                        "later today or tomorrow for any updates.");
                i = crimes.length;
            }
            else {
                if (i == 0)
                {
                    onCampusButton.setText("On-Campus Crimes for " + dateFromSearch);
                    TableRow row = new TableRow(this);
                    // Create new row

                    TextView reportNumHeader = new TextView(this);
                    reportNumHeader.setText("Report Number");
                    reportNumHeader.setGravity(Gravity.LEFT);
                    reportNumHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(reportNumHeader);
                    // Add Report Number Header to header table

                    TextView IncidentTypeHeader = new TextView(this);
                    IncidentTypeHeader.setText("Incident Type");
                    IncidentTypeHeader.setGravity(Gravity.LEFT);
                    IncidentTypeHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(IncidentTypeHeader);
                    // Add Incident Type Header to header table

                    TextView locationHeader = new TextView(this);
                    locationHeader.setText("Location");
                    locationHeader.setGravity(Gravity.LEFT);
                    locationHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(locationHeader);
                    // Add Location Header to header table

                    TextView descriptionHeader = new TextView(this);
                    descriptionHeader.setText("Description");
                    descriptionHeader.setGravity(Gravity.LEFT);
                    descriptionHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(descriptionHeader);
                    // Add Description Header to header table

                    onCampusHeaderTable.addView(row);
                }
                TableRow row = new TableRow(this);
                // Create new row

                TextView reportNum = new TextView(this);
                reportNum.setHeight(50);
                reportNum.setMaxWidth(45);
                info = crimes[i];

                if (info != null && info.contains("null")) {
                    info = info.replaceAll("null", "");
                }
                reportNum.setText(info);
                row.addView(reportNum);
                // Add Report Number to row

                TextView incidentType = new TextView(this);
                incidentType.setHeight(50);
                incidentType.setMaxWidth(40);
                info = crimes[i + 5];

                if (info != null && info.contains("null")) {
                    info = info.replaceAll("null", "");
                }
                incidentType.setText(info);
                row.addView(incidentType);
                // Add Incident Type to row

                TextView location = new TextView(this);
                location.setHeight(50);
                location.setMaxWidth(40);
                info = crimes[i + 6];

                if (info != null && info.contains("null")) {
                    info = info.replaceAll("null", "");
                }
                location.setText(info);
                row.addView(location);
                // Add location to row

                TextView description = new TextView(this);
                description.setHeight(50);
                description.setMaxWidth(40);
                info = crimes[i + 7];

                if (info != null && info.contains("null")) {
                    info = info.replaceAll("null", "");
                }
                description.setText(info);
                row.addView(description);
                // Add location to row

                onCampusTable.addView(row);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
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
        else if (id == R.id.pick_date){
            // Do date stuff
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(),"Date Picker");

            return true;
        }
        else if (id == R.id.show_map){
            // Do map fragment

            setContentView(R.layout.activity_map_fragment);
            addMapFragment();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void getCrimes(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        startActivity(intent);
    }

    private void addMapFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        MapFragment fragment = new MapFragment();
        transaction.add(R.id.mapView, fragment);
        transaction.commit();
    }
    /**
     * Class to handle web scraping.
     */
    public class RetrieveCrimes extends AsyncTask<Void, Void, String[]> {
        private final Context RetrieveCrimes = null;

        @Override
        protected void onProgressUpdate(Void... values) {
            ProgressDialog pd = new ProgressDialog(RetrieveCrimes);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("Working...");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
        }

        @Override
        protected String[] doInBackground(Void... arg0) {
        /* Get yesterday's crimes */

            offCampusCrimes = new String[500];
            onCampusCrimes = new String[500];
            date = getYesterdaysDate();
            String columbusPD = "http://www.columbuspolice.org/reports/Results?from=placeholder&to=placeholder&loc=zon4&types=9";
            String OSUPD = "http://www.ps.ohio-state.edu/police/daily_log/view.php?date=yesterday";
            Document doc = null;
            boolean websiteDown = false;
            // Create variables

            try {
                String userAgent = System.getProperty("http.agent");
                doc = Jsoup.connect(columbusPD.replaceAll("placeholder", date)).timeout(10*1000).userAgent(userAgent).get();
                // Try to visit Columbus PD's website
            }
            catch(java.io.IOException ex){
                offCampusCrimes[0] = "down";

                websiteDown = true;
                // Website is down, handle case
            }

            if (!websiteDown) {
                Element crimeTable = doc.getElementById("MainContent_gvResults");
                // HTML table holding yesterday's crimes

                Elements crimeInfo = crimeTable.getElementsByTag("td");
                // Get individual crime information

                if (crimeInfo.size() == 0)
                {
                    offCampusCrimes[0] = "empty";
                }
                int counter = 0;
                for (Element info : crimeInfo) {
                    String linkText = info.text();
                    offCampusCrimes[counter] += linkText;
                    counter++;
                    // Retrieve individual crime info
                }
            }

            try {
                String userAgent = System.getProperty("http.agent");
                doc = Jsoup.connect(OSUPD).timeout(10*1000).userAgent(userAgent).get();
                // Try to visit OSU PD's website
            }
            catch(java.io.IOException ex){
                onCampusCrimes[0] = "down";

                websiteDown = true;
                // Website is down, handle case
            }

            if (!websiteDown) {
                Elements crimeTable = doc.select("td.log");
                // HTML table holding yesterday's crimes

                if (crimeTable.size() == 0)
                {
                    onCampusCrimes[0] = "empty";
                }

                int counter = 0;
                for (Element info : crimeTable) {
                    String linkText = info.text();
                    onCampusCrimes[counter] += linkText;
                    counter++;
                    // Retrieve individual crime info
                }
            }
            return null;
        }

        protected void onPostExecute(Void... arg0) {
        }

        protected String getYesterdaysDate(Void... arg0) {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return dateFormat.format(cal.getTime());
        }
    }
}
