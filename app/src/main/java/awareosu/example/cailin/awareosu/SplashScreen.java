package awareosu.example.cailin.awareosu;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.cailin.awareosu.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Cailin on 12/31/2015.
 */
public class SplashScreen extends Activity {

    public String[] offCampusCrimes;
    public String[] onCampusCrimes;
    public String[] offCampusCrimeLinks;
    public String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * Showing splashscreen while making network calls to download necessary
         * data before launching the app Will use AsyncTask to make http call
         */
        new RetrieveCrimes().execute();
    }

    /**
     * Class to handle web scraping.
     */
    public class RetrieveCrimes extends AsyncTask<Void, Void, String[]> {


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
            int numberOfOnCrimes = 0;
            int numberOfOffCrimes = 0;
            // Create variables

            int retries = 0;
            while (retries < 2) {
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
                    retries = 2;
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
                doc = Jsoup.connect(OSUPD).timeout(20*1000).userAgent(userAgent).get();
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

                if (crimeTable.size() > 0) {
                    int counter = 0;
                    for (Element info : crimeTable) {
                        String linkText = info.text();
                        onCampusCrimes[counter] += linkText;
                        counter++;
                        // Retrieve individual crime info
                    }
                }
                else
                {
                    onCampusCrimes[0] = "empty";
                }
            }

            Intent i = new Intent(SplashScreen.this, MyActivity.class);
            i.putExtra("off", offCampusCrimes);
            i.putExtra("on", onCampusCrimes);
            i.putExtra("offLinks", offCampusCrimeLinks);
            i.putExtra("offCrimeNum", numberOfOffCrimes);
            i.putExtra("onCrimeNum", numberOfOnCrimes);
            startActivity(i);
            // Pass information to MyActivity

            finish();
            // Close this activity
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