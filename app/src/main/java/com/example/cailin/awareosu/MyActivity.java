package com.example.cailin.awareosu;

import java.util.*;
import java.lang.*;
import java.text.*;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Typeface;
import android.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;

public class MyActivity extends AppCompatActivity{
    public final static String EXTRA_MESSAGE = "com.example.cailin.MESSAGE";
    public String[] offCampusCrimes;
    public String[] onCampusCrimes;
    public String[] offCampusCrimeLinks;
    public String date;
    private Geocoder coder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coder = new Geocoder(getBaseContext());

        Intent i = getIntent();
        offCampusCrimes = i.getStringArrayExtra("off");
        onCampusCrimes = i.getStringArrayExtra("on");
        offCampusCrimeLinks = i.getStringArrayExtra("offLinks");
        // Get web scraped info from splash screen

        Button onCampusButton = (Button) findViewById(R.id.onCampus_button);

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        offCampus(offCampusCrimes, offCampusCrimeLinks, dateFormat.format(cal.getTime()));
        onCampus(onCampusCrimes, dateFormat.format(cal.getTime()));
    }

    public void offCampus(String[] crimes, String[] links, String dateFromSearch) {
        System.arraycopy(crimes, 0, offCampusCrimes, 0, crimes.length);
        // Updated offCampusCrimes array

        TableLayout offCampusTable = (TableLayout) findViewById(R.id.off_campus);
        offCampusTable.removeAllViews();
        TextView offMessage = (TextView) findViewById(R.id.off_message);
        offMessage.setText("");
        TableLayout offCampusHeaderTable = (TableLayout) findViewById(R.id.offHeader_table);
        offCampusHeaderTable.removeAllViews();
        Button offCampusButton = (Button) findViewById(R.id.offCampus_button);
        String info = "";
        String crimeLink = "<a href='placeholder'>Crime Report</a>";
        // Access off-campus table and create variables

        int j = 1;
        for (int i = 0; i < crimes.length; i += 5) {
            if (crimes[i] == null)
            {
                i = crimes.length;
            }
            else if (crimes[0].equals("down"))
            {
                offCampusButton.setText("Website down");
                offMessage.setClickable(true);
                offMessage.setMovementMethod(LinkMovementMethod.getInstance());
                offMessage.setText(Html.fromHtml("The Columbus Police Department's website is " +
                        "currently down.<br><br>Please be sure to check the " +
                        "<a href='http://www.columbuspolice.org/reports/SearchLocation?loc=zon4'>CPD " +
                        "web portal</a> later today or tomorrow for any updates."));
                i = crimes.length;
            }
            else if (crimes[0].equals("empty"))
            {
                offCampusButton.setText("No Off-Campus Crimes reported for " + dateFromSearch);
                offMessage.setClickable(true);
                offMessage.setMovementMethod(LinkMovementMethod.getInstance());
                offMessage.setSingleLine(false);
                offMessage.setText(Html.fromHtml("This is due to either:<br> &#8226; The Columbus Police Department forgetting " +
                        "to upload crime information<br> &#8226; Attempting to retrieve crime information before " +
                        "it has been uploaded<br> &#8226; No crimes have occurred on this day<br><br> Please be sure to " +
                        "check the <a href='http://www.columbuspolice.org/reports/SearchLocation?loc=zon4'>CPD " +
                        "web portal</a> later today or tomorrow for any updates."));
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
                description.setClickable(true);
                description.setMovementMethod(LinkMovementMethod.getInstance());
                info = links[j];
                j++;

                description.setText(Html.fromHtml(crimeLink.replaceAll("placeholder", "http://www.columbuspolice.org/reports/PublicReport?caseID=" + info)));
                row.addView(description);
                // Add location to row

                offCampusTable.addView(row);
            }
        }
    }

    public void onCampus(String[] crimes, String dateFromSearch) {
        System.arraycopy(crimes, 0, onCampusCrimes, 0, crimes.length);
        // Updated onCampusCrimes array

        TableLayout onCampusTable = (TableLayout) findViewById(R.id.on_campus);
        onCampusTable.removeAllViews();
        TableLayout onCampusHeaderTable = (TableLayout) findViewById(R.id.onHeader_table);
        onCampusHeaderTable.removeAllViews();
        TextView onMessage = (TextView) findViewById(R.id.on_message);
        onMessage.setText("");
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
                onMessage.setClickable(true);
                onMessage.setMovementMethod(LinkMovementMethod.getInstance());
                onMessage.setText(Html.fromHtml("The OSU Police Department's website is currently down.\n" +
                        "Please be sure to check the <a href='http://www.ps.ohio-state.edu/police/daily_log/'>OSU web portal</a>" +
                        " later today or tomorrow for any updates."));
                i = crimes.length;
            }
            else if (crimes[0].equals("empty"))
            {
                onCampusButton.setText("No On-Campus Crimes reported for " + dateFromSearch);
                onMessage.setClickable(true);
                onMessage.setMovementMethod(LinkMovementMethod.getInstance());
                onMessage.setText(Html.fromHtml("This is due to either:<br> &#8226; The Ohio State Police Department forgetting " +
                        "to upload crime information<br> &#8226; Attempting to retrieve crime information before " +
                        "it has been uploaded<br> &#8226; No crimes have occurred on this day<br><br> Please be sure to " +
                        "check the <a href='http://www.ps.ohio-state.edu/police/daily_log/'>OSU " +
                        "web portal</a> later today or tomorrow for any updates."));
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

            String[] crimeLocations = getLocations(offCampusCrimes, onCampusCrimes);
            Double[] latLong = getLocationFromAddress(crimeLocations);
            // Get array containing lat + long of crimes, needed for map

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
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    public void onBackPressed() {
        //finish();
        onCreate(null);
    }

    /*
        Retrieves locations of crimes currently displayed in MyActivity
     */
    public String[] getLocations(String[] offCampusCrimeInfo, String[] onCampusCrimeInfo)
    {
        String[] locations = new String[offCampusCrimeInfo.length + onCampusCrimeInfo.length];
        String[] result;
        int counter = 0;
        // Declare variables

        for (int i = 0; i < offCampusCrimeInfo.length; i += 5)
        {
            if ((i + 4) <= offCampusCrimeInfo.length) {
                String location = offCampusCrimeInfo[i + 4];
                // Get location

                if (location != null) {
                    // If valid location, add to result array
                    locations[counter] = location.replaceAll("null", "");
                    counter++;
                }
            }
        }
        // Off campus

        for (int i = 0; i < onCampusCrimeInfo.length; i += 8)
        {
            if ((i + 6) <= onCampusCrimeInfo.length) {
                String location = onCampusCrimeInfo[i + 6];
                // Get location

                if (location != null) {
                    // If valid location, add to result array
                    locations[counter] = location.replaceAll("null", "");
                    counter++;
                }
            }
        }
        // On campus

        result = new String[counter];
        // Initialize result array

        for (int i = 0; i < result.length; i++)
        {
            result[i] = locations[i];
        }
        // Copy location to new array with correct size

        return result;
    }


    public Double[] getLocationFromAddress(String[] addresses) {
        Double[] result = new Double[addresses.length];
        List<Address> address;
        LatLng p1 = null;

        int j = 0;
        for (int i = 0; i < addresses.length; i++) {
            try {
                address = coder.getFromLocationName(addresses[i], 5);
                if (address != null) {
                    Address location = address.get(0);

                    result[j] = location.getLatitude();
                    result[j + 1] = location.getLongitude();
                    // Add lat & long to result array
                    j += 2;
                }

            } catch (Exception ex) {
                // Do nothing
            }
        }
        return result;
    }
}

