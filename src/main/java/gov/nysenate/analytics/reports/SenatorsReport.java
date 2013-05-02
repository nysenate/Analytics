package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.Utils;
import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;
import gov.nysenate.analytics.models.NYSenate;
import gov.nysenate.analytics.models.Source;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gdata.data.analytics.DataFeed;

/**
 * Handles reports with `report_type=senators` and writes them to the `output_file` as
 * a CSV file in the following format:
 * 
 * > Senator Name
 * > Source,Views,Bounces,Time
 * > ...
 * > ...
 * > Total, #views, #bounces, #time
 * >
 * > Senator Name
 * > ...
 * > ...
 * 
 * Use GoogleAnalytics to find the top 5 sources of traffic to the senators page on NYSenate.gov.
 * See GoogleAnalyticsConnect for information on cofiguring your queries.
 * 
 * @author GraylinKim
 * 
 */
public class SenatorsReport extends CSVReport
{
    public static boolean generateCSV(GoogleAnalyticsConnect gac, List<NYSenate> senators, Section params) throws IOException
    {
        CSVWriter writer = getCSVWriter(params);
        try {
            for (NYSenate senator : senators) {
                if (senator.fName.equals("NYSenate"))
                    continue;

                double totalTime = 0;
                int totalBounces = 0, totalPageviews = 0;
                System.out.println("  " + senator.nysenateURL);

                // Print out the CSV header
                writer.writeNext(new String[] { senator.fName + " " + senator.lName });
                writer.writeNext(params.get("column_headers").split(","));

                // Add a filter and retrieve the senator's stats by source
                params.put("filters", "ga:pagePath=~^" + senator.nysenateURL + ".*");
                DataFeed df = gac.getDataFeed(params);
                List<Source> lst = Utils.combineDataFeedBySource(df.getEntries(), senator.nysenateURL);
                Collections.sort(lst);
                // Sort the sources and truncate to the top "count" for printing
                for (Source so : Utils.groupOthers(lst, Integer.parseInt(params.get("count")))) {
                    writer.writeNext(new String[] { so.source, String.valueOf(so.pageviews), String.valueOf(so.bounces), String.valueOf(so.timeOnPage) });

                    totalBounces += so.bounces;
                    totalPageviews += so.pageviews;
                    totalTime += so.timeOnPage;
                }

                writer.writeNext(new String[] { "Total", String.valueOf(totalPageviews), String.valueOf(totalBounces), String.valueOf(totalTime) });
                writer.writeNext(new String[] {});
            }
        }
        finally {
            writer.close();
        }
        return true;
    }
}
