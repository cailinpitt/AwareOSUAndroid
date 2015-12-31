package com.example.cailin.awareosu;

/**
 * Created by Cailin on 12/31/2015.
 */
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.DatePicker;
import android.app.Dialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public String[] offCampusCrimes;
    public String[] onCampusCrimes;
    public String date;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Use the current date as the default date in the date picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        //Create a new DatePickerDialog instance and return it
        /*
            DatePickerDialog Public Constructors - Here we uses first one
            public DatePickerDialog (Context context, DatePickerDialog.OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth)
            public DatePickerDialog (Context context, int theme, DatePickerDialog.OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth)
         */
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //Do something with the date chosen by the user
        //((MyActivity) getActivity()).onCampus();
        date = "" + (month + 1) + "/" + day + "/" + year;
        // Update date that was searched

        try {
            String[] info = new RetrieveCrimes().execute().get();
            // Make main thread wait until crime info is retrieved
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // We now have crime information

        ((MyActivity) getActivity()).offCampus(offCampusCrimes, date);
        ((MyActivity) getActivity()).onCampus(onCampusCrimes, date);
        // Load layout tables with information
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
                offCampusCrimes[0] = "The Columbus Police Department's website is currently down. Please be sure to " +
                        "check the CPD web portal (http://www.columbuspolice.org/reports/SearchLocation?" +
                        "loc=zon4\\) later today or tomorrow for any updates.";

                websiteDown = true;
                // Website is down, handle case
            }

            if (!websiteDown) {
                Element crimeTable = doc.getElementById("MainContent_gvResults");
                // HTML table holding yesterday's crimes

                Elements crimeInfo = crimeTable.getElementsByTag("td");
                // Get individual crime information

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
                onCampusCrimes[0] = "The OSU Police Department's website is currently down. Please be sure to " +
                        "check the OSU web portal (http://www.ps.ohio-state.edu/police/daily_log/) " +
                        "later today or tomorrow for any updates.";

                websiteDown = true;
                // Website is down, handle case
            }

            if (!websiteDown) {
                Elements crimeTable = doc.select("td.log");
                // HTML table holding yesterday's crimes

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