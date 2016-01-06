package com.example.cailin.awareosu;

/**
 * Created by Cailin on 12/31/2015.
 */

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.DatePicker;

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
    public String[] offCampusCrimeLinks;
    public String date;
    public int selectedMonth;
    public int selectedDay;
    public int selectedYear;
    int numberOfOnCrimes = 0;
    int numberOfOffCrimes = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

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
        selectedDay = day;
        selectedMonth = month + 1;
        selectedYear = year;
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

        ((MyActivity) getActivity()).offCampus(offCampusCrimes, offCampusCrimeLinks, date, numberOfOffCrimes);
        ((MyActivity) getActivity()).onCampus(onCampusCrimes, date, numberOfOnCrimes);
        // Load layout tables with information
    }

    /**
     * Class to handle web scraping.
     */
    public class RetrieveCrimes extends AsyncTask<Void, Integer, String[]> {


        @Override
        protected String[] doInBackground(Void... arg0) {
        /* Get yesterday's crimes */

            offCampusCrimes = new String[500];
            onCampusCrimes = new String[500];
            String columbusPD = "http://www.columbuspolice.org/reports/Results?from=placeholder&to=placeholder&loc=zon4&types=9";
            String OSUPD = "http://www.ps.ohio-state.edu/police/daily_log/view.php";
            Document doc = null;
            boolean websiteDown = false;
            // Create variables

            int retries = 0;
            while (retries < 3) {
                try {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                    }
                    // Sleep for one second before trying

                    String userAgent = System.getProperty("http.agent");
                    doc = Jsoup.connect(columbusPD.replaceAll("placeholder", date)).timeout(20 * 1000).userAgent(userAgent).get();
                    websiteDown = false;
                    // Try to visit Columbus PD's website
                } catch (java.io.IOException ex) {
                    offCampusCrimes[0] = "down";

                    websiteDown = true;
                    // Website is down, handle case
                }
                if (websiteDown)
                {
                    retries++;
                }
                else
                {
                    retries = 3;
                }
            }

            if (!websiteDown) {
                Element crimeTable = doc.getElementById("MainContent_gvResults");
                // HTML table holding yesterday's crimes

                if (crimeTable != null) {
                    String crimeNumSentence = doc.getElementById("MainContent_lblCount").text();
                    int space = crimeNumSentence.indexOf(" ");
                    numberOfOffCrimes = Integer.parseInt(crimeNumSentence.substring(0, space));
                    //Retrieve number of crimes for this day

                    Elements crimeInfo = crimeTable.getElementsByTag("td");
                    Elements trElements = crimeTable.getElementsByTag("tr");
                    // Get individual crime information

                    int counter = 0;
                    int limit;

                    if (numberOfOffCrimes > 29)
                    {
                        limit = 30;
                    }
                    else
                    {
                        limit = numberOfOffCrimes + 1;
                    }
                    offCampusCrimeLinks = new String[trElements.size()];
                    for (Element crime : trElements) {
                        if ((counter != 0) && (counter < limit)) {
                            int indexOfParenthesis = crime.attr("onclick").indexOf(")");
                            String reportNum = crime.attr("onclick").substring(11, indexOfParenthesis);
                            offCampusCrimeLinks[counter - 1] = reportNum;
                            // Get links to each individual crime report
                        }
                        counter++;
                    }

                    counter = 0;
                    for (Element info : crimeInfo) {
                        if (counter < (limit * 5) - 5) {
                            String linkText = info.text();
                            offCampusCrimes[counter] += linkText;
                            counter++;
                            // Retrieve individual crime info
                        }
                    }
                }
                else
                {
                    offCampusCrimes[0] = "empty";
                }
            }

            websiteDown = false;
            try {
                String userAgent = System.getProperty("http.agent");
                doc = Jsoup.connect(OSUPD)
                        .timeout(10 * 1000)
                        .userAgent(userAgent)
                        .data("phrase", "")
                        .data("report_number", "")
                        .data("from_month", "" + selectedMonth)
                        .data("from_day", "" + selectedDay)
                        .data("from_year", "" + selectedYear)
                        .data("to_month", "" + selectedMonth)
                        .data("to_day", "" + selectedDay)
                        .data("to_year", "" + selectedYear)
                        .data("view_col", "report_date")
                        .data("view_cending", "DESC")
                        .data("searching", "Search")
                        .post();
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
                numberOfOnCrimes = crimeTable.size() / 8;
                // Get number of on campus crimes

                if (crimeTable.size() == 0)
                {
                    onCampusCrimes[0] = "empty";
                }
                else {

                    int counter = 0;
                    for (Element info : crimeTable) {
                        String linkText = info.text();
                        onCampusCrimes[counter] += linkText;
                        counter++;
                        // Retrieve individual crime info
                    }
                }
            }

            return null;
        }

        protected String getYesterdaysDate(Void... arg0) {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return dateFormat.format(cal.getTime());
        }
    }
}
