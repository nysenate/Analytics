package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.Utils;
import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;
import gov.nysenate.analytics.structures.NYSenate;
import gov.nysenate.analytics.structures.Source;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.ini4j.Profile.Section;

import com.google.gdata.data.analytics.DataFeed;

public class SenatorsReport
{
    public static boolean generateCSV(GoogleAnalyticsConnect gac, List<NYSenate> senators, Section params)
    {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(params.get("output_file"))));
            for (NYSenate senator : senators) {
                if (senator.fName.equals("NYSenate"))
                    continue;

                double totalTime = 0;
                int totalBounces = 0, totalPageviews = 0;
                System.out.println("  " + senator.nysenateURL);

                // Print out the CSV header
                bw.write(senator.fName + " " + senator.lName);
                bw.newLine();
                bw.write(params.get("column_headers"));
                bw.newLine();

                // Add a filter and retrieve the senator's stats by source
                params.put("filters", "ga:pagePath=~^" + senator.nysenateURL + ".*");
                DataFeed df = gac.getDataFeed(params);
                List<Source> lst = Utils.combineDataFeedBySource(df.getEntries(), senator.nysenateURL);
                Collections.sort(lst);
                // Sort the sources and truncate to the top "count" for printing
                for (Source so : Utils.groupOthers(lst, Integer.parseInt(params.get("count")))) {
                    bw.write(so.source + "," + so.pageviews + "," + so.bounces + "," + so.time);
                    bw.newLine();

                    totalBounces += so.bounces;
                    totalPageviews += so.pageviews;
                    totalTime += so.time;
                }

                bw.write("Total" + "," + totalPageviews + "," + totalBounces + "," + totalTime);
                bw.newLine();
                bw.newLine();
            }
            bw.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
