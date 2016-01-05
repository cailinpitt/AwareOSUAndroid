package com.example.cailin.awareosu;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MyActivity extends AppCompatActivity{
    public final static String EXTRA_MESSAGE = "com.example.cailin.MESSAGE";
    public String[] offCampusCrimes;
    public String[] onCampusCrimes;
    public String[] offCampusCrimeLinks;
    public FragmentManager manager;
    public String date;
    private Geocoder coder;
    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coder = new Geocoder(getBaseContext());


        SharedPreferences settings = getSharedPreferences("crimeInfo", 0);
        String temp = settings.getString("off", null);

        SharedPreferences.Editor editor = settings.edit();

        if (temp != null) {
            offCampusCrimes = temp.split("#");
            editor.remove("off");

            temp = settings.getString("on", null);
            onCampusCrimes = temp.split("#");
            editor.remove("on");

            temp = settings.getString("links", null).replaceAll("null,", "");
            if (temp.length() == 0)
            {
                offCampusCrimeLinks = null;
            }
            else {
                offCampusCrimeLinks = temp.split("#");
                editor.remove("links");
            }

            date = settings.getString("date", null);
            editor.remove("date");

            editor.commit();

            // We just came from map fragment, load previous info and display
        }
        else
        {
            Intent i = getIntent();
            offCampusCrimes = i.getStringArrayExtra("off");
            onCampusCrimes = i.getStringArrayExtra("on");
            offCampusCrimeLinks = i.getStringArrayExtra("offLinks");
            // Get web scraped info from splash screen

            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            date = dateFormat.format(cal.getTime());
        }

        offCampus(offCampusCrimes, offCampusCrimeLinks, date);
        onCampus(onCampusCrimes, date);

        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if(getSupportFragmentManager().getBackStackEntryCount() == 0) finish();
            }
        });
    }

    public void offCampus(String[] crimes, String[] links, String dateFromSearch) {
        date = dateFromSearch;
        // Update date

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
        String crimeLink = "<a href='placeholder'>Report</a>";
        // Access off-campus table and create variables

        int j = 1;
        for (int i = 0; i < crimes.length; i += 5) {
            if (crimes[i] == null)
            {
                i = crimes.length;
            }
            else if (crimes[0].equals("down"))
            {
                offCampusButton.setText("Website down/not loading");
                offMessage.setClickable(true);
                offMessage.setMovementMethod(LinkMovementMethod.getInstance());
                offMessage.setText(Html.fromHtml("The Columbus Police Department's website is " +
                        "currently unreachable.<br><br>Please be sure to check the " +
                        "<a href='http://www.columbuspolice.org/reports/SearchLocation?loc=zon4'>CPD " +
                        "web portal</a> later today or tomorrow for any updates."));
                i = crimes.length;
            } else if (crimes[0].equals("empty"))
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
                    offMessage.setHeight(0);
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

                    offCampusTable.addView(row);
                }

                if ((i < crimes.length)
                        && ((i + 1) < crimes.length)
                        && ((i + 4) < crimes.length)
                        && j < links.length) {
                    TableRow row = new TableRow(this);
                    // Create new row

                    TextView reportNum = new TextView(this);
                    reportNum.setHeight(50);
                    reportNum.setMaxWidth(25);
                    reportNum.setSingleLine(false);
                    info = crimes[i];

                    if (info != null && info.contains("null")) {
                        info = info.replaceAll("null", "");
                    }
                    reportNum.setText(info);
                    row.addView(reportNum);
                    // Add Report Number to row

                    TextView incidentType = new TextView(this);
                    incidentType.setHeight(50);
                    incidentType.setMaxWidth(35);
                    incidentType.setSingleLine(false);
                    info = crimes[i + 1];

                    if (info != null && info.contains("null")) {
                        info = info.replaceAll("null", "");
                    }
                    incidentType.setText(info);
                    row.addView(incidentType);
                    // Add Incident Type to row

                    TextView location = new TextView(this);
                    location.setHeight(50);
                    location.setMaxWidth(35);
                    location.setSingleLine(false);
                    info = crimes[i + 4];

                    if (info != null && info.contains("null")) {
                        info = info.replaceAll("null", "");
                    }
                    location.setText(info);
                    row.addView(location);
                    // Add location to row

                    TextView description = new TextView(this);
                    description.setHeight(50);
                    description.setMaxWidth(35);
                    description.setSingleLine(false);
                    description.setClickable(true);
                    description.setMovementMethod(LinkMovementMethod.getInstance());
                    info = links[j];
                    j++;

                    description.setText(Html.fromHtml(crimeLink.replaceAll("placeholder", "http://www.columbuspolice.org/reports/PublicReport?caseID=" + info)));
                    row.addView(description);
                    // Add location to row
                    offCampusTable.addView(row);
                }
                else
                {
                    i = crimes.length;
                }
            }
        }
    }

    public void onCampus(String[] crimes, String dateFromSearch) {
        date = dateFromSearch;
        // Update date

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


        if (crimes[0].equals("empty"))
        {
            onCampusButton.setText("No On-Campus Crimes reported for " + dateFromSearch);
            onMessage.setClickable(true);
            onMessage.setMovementMethod(LinkMovementMethod.getInstance());
            onMessage.setText(Html.fromHtml("This is due to either:<br> &#8226; The Ohio State Police Department forgetting " +
                    "to upload crime information<br> &#8226; Attempting to retrieve crime information before " +
                    "it has been uploaded<br> &#8226; No crimes have occurred on this day<br><br> Please be sure to " +
                    "check the <a href='http://www.ps.ohio-state.edu/police/daily_log/'>OSU " +
                    "web portal</a> later today or tomorrow for any updates."));
        }
        else if (crimes[0].equals("down")) {
            onCampusButton.setText("Website down/not loading");
            onMessage.setClickable(true);
            onMessage.setMovementMethod(LinkMovementMethod.getInstance());
            onMessage.setText(Html.fromHtml("The OSU Police Department's website is currently unreachable.<br><br>" +
                    "Please be sure to check the <a href='http://www.ps.ohio-state.edu/police/daily_log/'>OSU web portal</a>" +
                    " later today or tomorrow for any updates."));
        }
        else {
            for (int i = 0; i < crimes.length; i += 8) {
                if ((crimes[i] == null) || (crimes[i].equals("null"))) {
                    i = crimes.length;
                }
                else {
                    if (i == 0) {
                        onMessage.setHeight(0);
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

                        onCampusTable.addView(row);
                    }


                    if ((i < crimes.length)
                            && ((i + 5) < crimes.length)
                            && ((i + 6) < crimes.length)
                            && ((i + 7) < crimes.length)) {
                        TableRow row = new TableRow(this);
                        // Create new row

                        TextView reportNum = new TextView(this);
                        reportNum.setHeight(50);
                        reportNum.setMaxWidth(45);
                        reportNum.setSingleLine(false);
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
                        incidentType.setSingleLine(false);
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
                        location.setSingleLine(false);
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
                        description.setSingleLine(false);
                        info = crimes[i + 7];

                        if (info != null && info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        description.setText(info);
                        row.addView(description);
                        // Add location to row

                        onCampusTable.addView(row);
                    } else {
                        i = crimes.length;
                    }
                }
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
            String crimeInfo[] = getCrimeInfo(offCampusCrimes, onCampusCrimes);
            double[] latLong = getLocationFromAddress(crimeLocations);
            // Get array containing lat + long of crimes, needed for map

            setContentView(R.layout.activity_map_fragment);
            addMapFragment(latLong, crimeInfo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addMapFragment(double[] locations, String[] crimeInfo) {

        SharedPreferences settings = getSharedPreferences("crimeInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        StringBuilder data = new StringBuilder();
        for (int i = 0; i < offCampusCrimes.length; i++) {
            data.append(offCampusCrimes[i]).append("#");
        }
        editor.putString("off", data.toString());

        data = new StringBuilder();
        for (int i = 0; i < onCampusCrimes.length; i++) {
            data.append(onCampusCrimes[i]).append("#");
        }
        editor.putString("on", data.toString());

        data = new StringBuilder();
        if (offCampusCrimeLinks != null) {
            for (int i = 0; i < offCampusCrimeLinks.length; i++) {
                data.append(offCampusCrimeLinks[i]).append("#");
            }
            editor.putString("links", data.toString());
        }
        else
        {
            editor.putString("links", "");
        }

        editor.putString("date", date);


        editor.apply();

        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        MapFragment fragment = new MapFragment();
        transaction.replace(R.id.mapView, fragment, "map");
        transaction.addToBackStack("map");
        Bundle args = new Bundle();
        args.putDoubleArray("locations", locations);
        args.putStringArray("info", crimeInfo);

        args.putStringArray("off", offCampusCrimes);
        args.putStringArray("on", onCampusCrimes);
        args.putString("date", date);
        args.putStringArray("links", offCampusCrimeLinks);

        fragment.setArguments(args);
        // Pass location array to map fragment
        transaction.commit();

        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onBackPressed() {
        Intent openMainActivity= new Intent(getApplicationContext(), MyActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(openMainActivity);
        // Restart main, pass information crime info to it
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
                    locations[counter] = (location + " Columbus, Ohio").replaceAll("null", "");
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
                    locations[counter] = (location + " The Ohio State University").replaceAll("null", "");
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

    public String[] getCrimeInfo(String[] offCampusCrimeInfo, String[] onCampusCrimeInfo)
    {
        String[] info = new String[offCampusCrimeInfo.length + onCampusCrimeInfo.length];
        String[] result;
        int counter = 0;
        // Declare variables

        for (int i = 0; i < offCampusCrimeInfo.length; i += 5) {
            if ((i + 1) < offCampusCrimeInfo.length) {
                String crimeInfo = offCampusCrimeInfo[i + 1];
                // Get incident type

                if (offCampusCrimeInfo[i + 1] != null) {
                    // If valid location, add to result array
                    info[counter] = crimeInfo.replaceAll("null", "");
                    counter++;
                }
            }
        }
        // Off campus

        for (int i = 0; i < onCampusCrimeInfo.length; i += 8)
        {
            if ((i + 5) < onCampusCrimeInfo.length) {
                String crimeInfo = onCampusCrimeInfo[i + 5];
                // Get incident type

                if (onCampusCrimeInfo[i + 5] != null) {
                    // If valid location, add to result array
                    info[counter] = crimeInfo.replaceAll("null", "");
                    counter++;
                }
            }

        }
        // On campus

        result = new String[counter];
        // Initialize result array

        for (int i = 0; i < result.length; i++)
        {
            result[i] = info[i];
        }
        // Copy location to new array with correct size

        return result;
    }

    public double[] getLocationFromAddress(String[] addresses) {
        double[] result = new double[addresses.length * 2];
        List<Address> address;
        LatLng p1 = null;
        int i;

        int j = 0;
        for (i = 0; i < addresses.length; i++) {
            try {
                String addressToSearch = addresses[i].replaceAll("&(?!amp;)", "and");
                // '&' throws address search off. Replace w/ 'and'
                address = coder.getFromLocationName(addressToSearch, 5);
                if (address != null) {
                    Address location = address.get(0);

                    String add = location.getAddressLine(1);

                    if (add.contains("Columbus, OH")) {
                        if (Double.compare(location.getLatitude(), 0.0) != 0) {
                            result[j] = location.getLatitude();
                            result[j + 1] = location.getLongitude();
                            // Add lat & long to result array
                        }
                        else
                        {
                            result[j] = -1;
                            result[j + 1] = -1;
                            // This means we couldn't find the lat & long from the search.
                            // We won't add that location to the map.
                        }
                    }
                    else
                    {
                        // Else Google is having trouble finding coordinates because it is an on campus address.
                        // Do some creative searching.
                        List<Address> search = coder.getFromLocationName(add + " Columbus, Ohio", 5);
                        location = search.get(0);

                        if (Double.compare(location.getLatitude(), 0.0) != 0) {
                            result[j] = location.getLatitude();
                            result[j + 1] = location.getLongitude();
                        }
                        else
                        {
                            result[j] = -1;
                            result[j + 1] = -1;
                            // This means we couldn't find the lat & long from the search.
                            // We won't add that location to the map.
                        }
                    }
                }

            } catch (Exception ex) {
                result[j] = -1;
                result[j + 1] = -1;
                // This means we couldn't find the lat & long from the search.
                // We won't add that location to the map.
            }

            j += 2;
        }
        return result;
    }

    private static Intent getIntent(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArray("off", offCampusCrimes);
        outState.putStringArray("on", onCampusCrimes);
        outState.putStringArray("links", offCampusCrimeLinks);
        outState.putString("date", date);


        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}

