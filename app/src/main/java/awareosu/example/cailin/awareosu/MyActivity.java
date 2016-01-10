package awareosu.example.cailin.awareosu;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cailin.awareosu.R;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MyActivity extends AppCompatActivity{
    public String[] offCampusCrimes;
    public String[] offCampusCrimeLinks;
    public int offCrimeNum;

    public String[] onCampusCrimes;
    public int onCrimeNum;

    public String date;

    public FragmentManager manager;
    private Geocoder coder;
    // Declare global variables

    public int mNotificationId = 001;
    // Set an ID for the daily notification

    /**
     * Create MyActivity.
     *
     * @param savedInstanceState Used to restore variables if onDestroy or onPause were called
     *                           (however this is never used).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences SP = getSharedPreferences("firstRun", 0);
        // Use this to figure out if app is being run for the first time

        if (SP.getBoolean("my_first_time", true)) {
            notifyUser();
            // App is being run for the first time, initially turn notifications on

            SP.edit().putBoolean("my_first_time", false).apply();
            // Record that app has been run for the first time
        }
        // We want user to always be notified of crimes until they selected "no notifications"
        // in settings page

        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coder = new Geocoder(getBaseContext());
        // Set up Geocoder

        SharedPreferences settings = getSharedPreferences("crimeInfo", 0);
        boolean isCriticalSection = settings.getBoolean("criticalSection", true);
        // Figure out if we're returning from the Map Fragment and we have to rebuild MyActivity

        if (!isCriticalSection) {
            // If we're not in the critical section, that means we're coming from Map Fragment
            // Retrieve stored info and delete

            SharedPreferences.Editor editor = settings.edit();
            editor.remove("criticalSection");

            String temp = settings.getString("off", null);
            offCampusCrimes = temp.split("~");
            editor.remove("off");

            temp = settings.getString("on", null);
            onCampusCrimes = temp.split("~");
            editor.remove("on");

            temp = settings.getString("links", null);
            if (temp == null)
            {
                offCampusCrimeLinks = null;
            }
            else {
                temp = temp.replaceAll("null~", "");
                offCampusCrimeLinks = temp.split("~");
            }
            editor.remove("links");

            date = settings.getString("date", null);
            editor.remove("date");

            onCrimeNum = settings.getInt("onNum", 0);
            editor.remove("onNum");

            offCrimeNum = settings.getInt("offNum", 0);
            editor.remove("offNum");

            editor.apply();
        }
        else
        {
            // Else app just started running, Splash Screen will be providing info

            Intent i = getIntent();
            offCampusCrimes = i.getStringArrayExtra("off");
            onCampusCrimes = i.getStringArrayExtra("on");
            offCampusCrimeLinks = i.getStringArrayExtra("offLinks");
            offCrimeNum = i.getIntExtra("offCrimeNum", 0);
            onCrimeNum = i.getIntExtra("onCrimeNum", 0);

            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            date = dateFormat.format(cal.getTime());
            // Retrieve yesterday's date
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                offCampus(offCampusCrimes, offCampusCrimeLinks, date, offCrimeNum);
            }
        });
        // Run on separate thread (with access to UI)

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onCampus(onCampusCrimes, date, onCrimeNum);
            }
        });
        // Run on separate thread (with access to UI)

        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) finish();
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        // Take care of UserSettingsActivity
    }

    /**
     * Using off campus crime information, update the off campus message box and TableLayout.
     *
     * @param crimes Array holding off campus crime information
     * @param links Array holding id number used to link to the specific crime's online information
     * @param dateFromSearch String holding the date corresponding to the crime information in crimes and links
     * @param crimeNum Number of crimes for this day
     */
    public void offCampus(String[] crimes, String[] links, String dateFromSearch, int crimeNum) {
        date = dateFromSearch;
        // Update date

        System.arraycopy(crimes, 0, offCampusCrimes, 0, crimes.length);
        // Updated offCampusCrimes array

        if (links == null)
        {
            offCampusCrimeLinks = null;
        }
        else
        {
            offCampusCrimeLinks = new String[links.length];
            System.arraycopy(links, 0, offCampusCrimeLinks, 0, links.length);
        }
        // Update offCampusCrimeLinks array

        offCrimeNum = crimeNum;
        // Update offCrimeNum

        TableLayout offCampusTable = (TableLayout) findViewById(R.id.off_campus);
        offCampusTable.removeAllViews();
        // Access off campus TableLayout and clear it

        TextView offMessage = (TextView) findViewById(R.id.off_message);
        // Access off campus message box

        TableLayout offCampusHeaderTable = (TableLayout) findViewById(R.id.offHeader_table);
        offCampusHeaderTable.removeAllViews();
        // Access off campus header table and clear it

        Button offCampusButton = (Button) findViewById(R.id.offCampus_button);
        // Access off campus button (used to display number of crimes for a specific day)

        String info = "";
        String crimeLink = "<a href='placeholder'>Report</a>";
        int length;
        int j = 1;
        // Declare variables

        if (crimeNum != 0)
        {
            length = crimeNum * 5;
        }
        else
        {
            length = crimes.length;
        }
        // Access off-campus table and create variables

        for (int i = 0; i < length; i += 5) {
            if (crimes[i] == null)
            {
                i = length;
                // This means we're done retrieving crime information. End loop
            }
            else if (crimes[0].equals("down"))
            {
                // We were unable to retrieve crime information from website

                offCampusButton.setText("Website down/not loading");

                offMessage.setClickable(true);
                offMessage.setMovementMethod(LinkMovementMethod.getInstance());
                offMessage.setSingleLine(false);
                offMessage.setText(Html.fromHtml("The Columbus Police Department's website is " +
                        "currently unreachable.<br><br>Please be sure to check the " +
                        "<a href='http://www.columbuspolice.org/reports/SearchLocation?loc=zon4'>CPD " +
                        "web portal</a> later today or tomorrow for any updates."));
                offMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                i = length;
            } else if (crimes[0].equals("empty"))
            {
                // We were able to access website but no crimes for this day were found

                offCampusButton.setText("No Off-Campus Crimes reported for " + dateFromSearch);

                offMessage.setClickable(true);
                offMessage.setMovementMethod(LinkMovementMethod.getInstance());
                offMessage.setSingleLine(false);
                offMessage.setText(Html.fromHtml("This is due to either:<br>&nbsp;&#8226; The Columbus Police Department forgetting " +
                        "to upload crime information<br>&nbsp;&#8226; Attempting to retrieve crime information before " +
                        "it has been uploaded<br>&nbsp;&#8226; No crimes have occurred on this day<br><br> Please be sure to " +
                        "check the <a href='http://www.columbuspolice.org/reports/SearchLocation?loc=zon4'>CPD " +
                        "web portal</a> later today or tomorrow for any updates."));
                offMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                i = length;
            }
            else {
                // Crime info available
                if (i == 0)
                {
                    // Set up headers

                    offMessage.setText("");
                    offMessage.setHeight(0);
                    offCampusButton.setText(crimeNum + " Off-Campus Crimes for " + dateFromSearch);
                    TableRow row = new TableRow(this);
                    // Create new row

                    TextView reportNumHeader = new TextView(this);
                    reportNumHeader.setText("Report Number");
                    reportNumHeader.setGravity(Gravity.CENTER);
                    reportNumHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(reportNumHeader);
                    // Add Report Number Header to header table

                    TextView IncidentTypeHeader = new TextView(this);
                    IncidentTypeHeader.setText("Incident Type");
                    IncidentTypeHeader.setGravity(Gravity.CENTER);
                    IncidentTypeHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(IncidentTypeHeader);
                    // Add Incident Type Header to header table

                    TextView locationHeader = new TextView(this);
                    locationHeader.setText("Location");
                    locationHeader.setGravity(Gravity.CENTER);
                    locationHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(locationHeader);
                    // Add Location Header to header table

                    TextView descriptionHeader = new TextView(this);
                    descriptionHeader.setText("Description");
                    descriptionHeader.setGravity(Gravity.CENTER);
                    descriptionHeader.setTypeface(null, Typeface.BOLD);
                    row.addView(descriptionHeader);
                    // Add Description Header to header table

                    offCampusTable.addView(row);

                    // Add header to TableLayout
                }

                else if ((i < length)
                        && ((i + 1) < length)
                        && ((i + 4) < length)
                        && (j < crimeNum)
                        && (crimes[i] != null)
                        && (crimes[i + 1] != null)
                        && (crimes[i + 4] != null)
                        && (!crimes[i].equals("null"))
                        && (!crimes[i + 1].equals("null"))
                        && (!crimes[i + 4].equals("null"))) {
                    // Checking to ensure we're accessing valid locations & information in array

                    info = crimes[i];

                    if (info != null) {
                        TableRow row = new TableRow(this);
                        // Create new row

                        TextView reportNum = new TextView(this);
                        reportNum.setHeight(50);
                        reportNum.setMaxWidth(25);
                        reportNum.setGravity(Gravity.CENTER);
                        reportNum.setSingleLine(false);
                        reportNum.setPaddingRelative(1, 1, 1, 1);
                        if (info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        reportNum.setText(info + "\n");
                        row.addView(reportNum);
                        // Add Report Number to row

                        TextView incidentType = new TextView(this);
                        incidentType.setHeight(60);
                        incidentType.setMaxWidth(35);
                        incidentType.setGravity(Gravity.CENTER);
                        incidentType.setSingleLine(false);
                        incidentType.setPaddingRelative(1, 1, 1, 1);
                        info = crimes[i + 1];
                        if (info != null && info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        incidentType.setText(info + "\n");
                        row.addView(incidentType);
                        // Add Incident Type to row

                        TextView location = new TextView(this);
                        location.setHeight(60);
                        location.setMaxWidth(35);
                        location.setGravity(Gravity.CENTER);
                        location.setSingleLine(false);
                        location.setPaddingRelative(1, 1, 1, 1);
                        info = crimes[i + 4];
                        if (info != null && info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        location.setText(info + "\n");
                        row.addView(location);
                        // Add location to row

                        TextView description = new TextView(this);
                        description.setHeight(50);
                        description.setMaxWidth(35);
                        description.setGravity(Gravity.CENTER);
                        description.setSingleLine(false);
                        description.setClickable(true);
                        description.setPaddingRelative(1, 1, 1, 1);
                        description.setMovementMethod(LinkMovementMethod.getInstance());
                        info = links[j];
                        j++;

                        description.setText(Html.fromHtml(crimeLink.replaceAll("placeholder", "http://www.columbuspolice.org/reports/PublicReport?caseID=" + info)));
                        row.addView(description);
                        // Add location to row

                        View line = new View(this);
                        line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20));
                        line.setBackgroundColor(Color.rgb(255, 255, 255));
                        line.setVisibility(View.INVISIBLE);
                        offCampusTable.addView(line);
                        // Add "padding"

                        line = new View(this);
                        line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                        line.setBackgroundColor(Color.rgb(51, 51, 51));
                        offCampusTable.addView(line);
                        // Add horizontal row

                        line = new View(this);
                        line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20));
                        line.setBackgroundColor(Color.rgb(255, 255, 255));
                        line.setVisibility(View.INVISIBLE);
                        offCampusTable.addView(line);
                        // Add "padding"

                        offCampusTable.addView(row);
                        // Add info to TableLayout
                    }
                }
                else
                {
                    i = length;
                }
            }
        }

        if (crimeNum > 29) {
            // Because of the limitations of HTML web scraping, we can only retrieve the first 29
            // crimes of a given day from the CPD website (they use JS pagination).
            // Tell user there are more than 29 crimes and provide link to view the rest.

            String columbusPD = "http://www.columbuspolice.org/reports/Results?from=placeholder&to=placeholder&loc=zon4&types=9";
            columbusPD = columbusPD.replaceAll("placeholder", date);
            // Prepare URL

            TableRow row = new TableRow(this);
            // Create new row

            TextView overflow = new TextView(this);

            overflow.setHeight(50);
            overflow.setMaxWidth(35);
            overflow.setClickable(true);
            overflow.setMovementMethod(LinkMovementMethod.getInstance());
            overflow.setText(Html.fromHtml("Only first 29 crimes are shown, " + (crimeNum - 29) +
                    " are not shown. Click <a href='" + columbusPD + "'>here</a> to view the rest."));

            row.addView(overflow);
            // Add Overflow information to row

            offCampusTable.addView(row);
            // Add to table
        }
    }

    /**
     * Using on campus crime information, update the on campus message box and TableLayout.
     *
     * @param crimes Array holding on campus crime information
     * @param dateFromSearch String holding the date corresponding to the crime information in crimes and links
     * @param crimeNum Number of crimes for this day
     */
    public void onCampus(String[] crimes, String dateFromSearch, int crimeNum) {
        date = dateFromSearch;
        // Update date

        System.arraycopy(crimes, 0, onCampusCrimes, 0, crimes.length);
        // Updated offCampusCrimes array

        onCrimeNum = crimeNum;
        // Update offCrimeNum

        TableLayout onCampusTable = (TableLayout) findViewById(R.id.on_campus);
        onCampusTable.removeAllViews();
        TableLayout onCampusHeaderTable = (TableLayout) findViewById(R.id.onHeader_table);
        onCampusHeaderTable.removeAllViews();
        TextView onMessage = (TextView) findViewById(R.id.on_message);
        Button onCampusButton = (Button) findViewById(R.id.onCampus_button);
        // Access XML elements

        String info = "";
        // Initialize variables

        if (crimes[0].equals("empty")) {
            // This means we were able to access website, but no crimes were available for this day

            onCampusButton.setText("No On-Campus Crimes reported for " + dateFromSearch);

            onMessage.setClickable(true);
            onMessage.setMovementMethod(LinkMovementMethod.getInstance());
            onMessage.setSingleLine(false);
            onMessage.setText(Html.fromHtml("This is due to either:<br>&nbsp;&#8226; The Ohio State Police Department forgetting " +
                    "to upload crime information<br>&nbsp;&#8226; Attempting to retrieve crime information before " +
                    "it has been uploaded<br>&nbsp;&#8226; No crimes have occurred on this day<br><br> Please be sure to " +
                    "check the <a href='http://www.ps.ohio-state.edu/police/daily_log/'>OSU " +
                    "web portal</a> later today or tomorrow for any updates."));
            onMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
        else if (crimes[0].equals("down")) {
            // This means we were not able to access website

            onCampusButton.setText("Website down/not loading");

            onMessage.setClickable(true);
            onMessage.setMovementMethod(LinkMovementMethod.getInstance());
            onMessage.setSingleLine(false);
            onMessage.setText(Html.fromHtml("The OSU Police Department's website is currently unreachable.<br><br>" +
                    "Please be sure to check the <a href='http://www.ps.ohio-state.edu/police/daily_log/'>OSU web portal</a>" +
                    " later today or tomorrow for any updates."));
            onMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
        else {
            // Else we have crime information

            for (int i = 0; i < crimes.length; i += 8) {
                if ((crimes[i] == null) || (crimes[i].equals("null"))) {
                    i = crimes.length;
                    // We've reached the end of available crime info, end loop
                }
                else {
                    if (i == 0) {
                        // Setup table header

                        onMessage.setText("");
                        onMessage.setHeight(0);
                        onCampusButton.setText(crimeNum + " On-Campus Crimes for " + dateFromSearch);
                        TableRow row = new TableRow(this);
                        // Create new row

                        TextView reportNumHeader = new TextView(this);
                        reportNumHeader.setText("Report Number");
                        reportNumHeader.setGravity(Gravity.CENTER);
                        reportNumHeader.setTypeface(null, Typeface.BOLD);
                        row.addView(reportNumHeader);
                        // Add Report Number Header to header table

                        TextView IncidentTypeHeader = new TextView(this);
                        IncidentTypeHeader.setText("Incident Type");
                        IncidentTypeHeader.setGravity(Gravity.CENTER);
                        IncidentTypeHeader.setTypeface(null, Typeface.BOLD);
                        row.addView(IncidentTypeHeader);
                        // Add Incident Type Header to header table

                        TextView locationHeader = new TextView(this);
                        locationHeader.setText("Location");
                        locationHeader.setGravity(Gravity.CENTER);
                        locationHeader.setTypeface(null, Typeface.BOLD);
                        row.addView(locationHeader);
                        // Add Location Header to header table

                        TextView descriptionHeader = new TextView(this);
                        descriptionHeader.setText("Description");
                        descriptionHeader.setGravity(Gravity.CENTER);
                        descriptionHeader.setTypeface(null, Typeface.BOLD);
                        row.addView(descriptionHeader);
                        // Add Description Header to header table

                        onCampusTable.addView(row);
                    }

                    if ((i < crimes.length)
                            && ((i + 5) < crimes.length)
                            && ((i + 6) < crimes.length)
                            && ((i + 7) < crimes.length)) {
                        // Ensure we're accessing valid locations & information in array

                        TableRow row = new TableRow(this);
                        // Create new row

                        TextView reportNum = new TextView(this);
                        reportNum.setHeight(50);
                        reportNum.setMaxWidth(45);
                        reportNum.setGravity(Gravity.CENTER);
                        reportNum.setSingleLine(false);
                        reportNum.setPaddingRelative(1, 1, 1, 1);
                        info = crimes[i];
                        if (info != null && info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        reportNum.setText(info + "\n");
                        row.addView(reportNum);
                        // Add Report Number to row

                        TextView incidentType = new TextView(this);
                        incidentType.setHeight(50);
                        incidentType.setMaxWidth(40);
                        incidentType.setGravity(Gravity.CENTER);
                        incidentType.setSingleLine(false);
                        info = crimes[i + 5];
                        if (info != null && info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        incidentType.setText(info + "\n");
                        row.addView(incidentType);
                        // Add Incident Type to row

                        TextView location = new TextView(this);
                        location.setHeight(50);
                        location.setMaxWidth(50);
                        location.setGravity(Gravity.CENTER);

                        location.setSingleLine(false);
                        info = crimes[i + 6];
                        if (info != null && info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        location.setText(info + "\n");
                        row.addView(location);
                        // Add location to row

                        TextView description = new TextView(this);
                        description.setHeight(50);
                        description.setMaxWidth(40);
                        description.setGravity(Gravity.CENTER);
                        description.setSingleLine(false);
                        description.setPaddingRelative(1, 1, 1, 1);
                        info = crimes[i + 7];
                        if (info != null && info.contains("null")) {
                            info = info.replaceAll("null", "");
                        }
                        description.setText(info + "\n");
                        row.addView(description);
                        // Add location to row

                        View line = new View(this);
                        line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20));
                        line.setBackgroundColor(Color.rgb(255, 255, 255));
                        line.setVisibility(View.INVISIBLE);
                        onCampusTable.addView(line);
                        // Add "padding"

                        line = new View(this);
                        line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                        line.setBackgroundColor(Color.rgb(51, 51, 51));
                        onCampusTable.addView(line);
                        // Add horizontal row

                        line = new View(this);
                        line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20));
                        line.setBackgroundColor(Color.rgb(255, 255, 255));
                        line.setVisibility(View.INVISIBLE);
                        onCampusTable.addView(line);
                        // Add "padding"

                        onCampusTable.addView(row);
                        // Add info to TableLayout
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

    /**
     * Handle Action bar click.
     *
     * @param item Item in Action bar that was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Figure out what in Action bar was clicked, then handle it
        if (id == R.id.action_settings) {
            // User selected settings

            Intent i = new Intent(this, UserSettingsActivity.class);
            startActivity(i);
            // Start activity

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String notificationPreference = SP.getString("getNotification", "1");
            // Retrieve settings preferences

            if (!notificationPreference.equals("1"))
            {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // mId allows you to update the notification later on.
                // mNotificationManager.notify(mId, mBuilder.build());

                // Cancels notification.
                mNotificationManager.cancel(mNotificationId);
            }
            else {
                notifyUser();
                // User has selected the option to be notified of crimes
            }
            // Update notification preference based on what the user chose

            return true;
        }
        else if (id == R.id.pick_date){
            // User wants to search crime information for a specific day

            Toast toast = Toast.makeText(this, "Search can take up to 40 seconds.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            // Inform user that date search takes time

            // I couldn't figure out how to do a process dialog on top of a dialogfragment, so this
            // is me cheating

            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(),"Date Picker");
            // Call & show DatePicker

            return true;
        }
        else if (id == R.id.show_map){
            // User wants to view crime information on map

            String[] crimeLocations = getLocations(offCampusCrimes, onCampusCrimes);
            String crimeInfo[] = getCrimeInfo(offCampusCrimes, onCampusCrimes);
            double[] latLong = getLocationFromAddress(crimeLocations);
            // Get array containing lat + long of crimes, needed for map

            setContentView(R.layout.activity_map_fragment);

            // Critical section begins
            addMapFragment(latLong, crimeInfo);

            // Critical section ends when back button is pressed

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Start Map Fragment.
     *
     * @param locations Array holding latitude & longitude corresponding to each crime
     * @param crimeInfo Array holding type of crime, corresponds to locations (ex. Assult, Robbery, etc.)
     */
    private void addMapFragment(double[] locations, String[] crimeInfo) {
        SharedPreferences settings = getSharedPreferences("crimeInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        // Save current crime info in SharedPreferences, we need this in order to restart MyActivity
        // once the user clicks the back button

        StringBuilder data = new StringBuilder();
        for (int i = 0; i < offCampusCrimes.length; i++) {
            data.append(offCampusCrimes[i]).append("~");
        }
        editor.remove("off");
        editor.putString("off", data.toString());

        data = new StringBuilder();
        for (int i = 0; i < onCampusCrimes.length; i++) {
            data.append(onCampusCrimes[i]).append("~");
        }
        editor.remove("on");
        editor.putString("on", data.toString());

        data = new StringBuilder();
        if (offCampusCrimeLinks != null) {
            for (int i = 0; i < offCampusCrimeLinks.length; i++) {
                data.append(offCampusCrimeLinks[i]).append("~");
            }
            editor.remove("links");
            editor.putString("links", data.toString());
        }
        else
        {
            editor.remove("links");
            editor.putString("links", null);
        }

        editor.remove("date");
        editor.putString("date", date);

        editor.remove("onNum");
        editor.putInt("onNum", onCrimeNum);

        editor.remove("offNum");
        editor.putInt("offNum", offCrimeNum);

        editor.remove("criticalSection");
        editor.putBoolean("criticalSection", true);

        editor.apply();
        // Crime information saved

        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        MapFragment fragment = new MapFragment();
        transaction.replace(R.id.mapView, fragment, "map");
        transaction.addToBackStack("map");
        Bundle args = new Bundle();
        args.putDoubleArray("locations", locations);
        args.putStringArray("info", crimeInfo);
        // Pass locations & crime info to MapFragment

        fragment.setArguments(args);
        // Pass location array to map fragment
        transaction.commit();

        getSupportFragmentManager().executePendingTransactions();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
    /**
     * Handle back button click when in MapFragment.
     */
    @Override
    public void onBackPressed() {
        SharedPreferences settings = getSharedPreferences("crimeInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("criticalSection");
        editor.putBoolean("criticalSection", false);
        editor.apply();
        // Critical section is over

        Intent openMainActivity = new Intent(getApplicationContext(), MyActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(openMainActivity);
        // Restart main, pass information crime info to it
    }

    /**
     * Extract crime locations.
     *
     * @param offCampusCrimeInfo Array holding off campus crime information
     * @param onCampusCrimeInfo Array holding on campus crime information
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
                    // Add City + State to address to help Geocoder find exact latitude + longitude
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
                    // Add University name to address to help Geocoder find exact latitude + longitude
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

    /**
     * Extract crime description (ex. Assult, Robbery, etc).
     *
     * @param offCampusCrimeInfo Array holding off campus crime information
     * @param onCampusCrimeInfo Array holding on campus crime information
     */
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

    /**
     * Convert street address info latitude + longitude.
     *
     * @param addresses Array holding the address corresponding to each crime
     */
    public double[] getLocationFromAddress(String[] addresses) {
        double[] result = new double[addresses.length * 2];
        List<Address> address;
        LatLng p1 = null;
        int i;
        int j = 0;
        // Declare variables

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
                // -1 means we won't add that location to the map.
            }

            j += 2;
        }
        return result;
    }

    /**
     * Setup daily notification.
     */
    public void notifyUser()
    {

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(this , AlarmReceiver.class);
        // Create new alarm & intent

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, mNotificationId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        // Create new pending intent

        Calendar calendar = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 00);
        // Set notification date, 10:15 AM

        if(calendar.before(now)){
            calendar.add(Calendar.DATE, 1);
        }
        // If 10:15 AM has passed already, set it for tomorrow

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        //Set alarm
    }
}