package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;
import gov.nysenate.analytics.connectors.OpenLegislationConnect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ini4j.Profile.Section;

import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;

public class SimpleReport
{
    public static boolean generateCSV(GoogleAnalyticsConnect gac, Section params)
    {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(params.get("output_file"))));
            List<DataEntry> entries = gac.getDataFeed(params).getEntries();

            // Get a list of all the feed data columns from the first data entry
            // This requires some ugly hacking becaues we've got some
            // non-standard values and formats
            DataEntry firstEntry = entries.get(0);
            List<String> feedDataNames = new ArrayList<String>();
            List<String> dimensionNames = new ArrayList<String>();

            // header is a special string used to hack around some custom column
            // names from openleg
            String header = "";

            for (Dimension dimension : firstEntry.getDimensions()) {
                dimensionNames.add(dimension.getName());

                // if this output needs to have an openleg field, hack it in,
                // we'll retrieve the value later
                if (dimension.getName().matches("ga:pagePath") && params.containsKey("has_openleg")) {
                    header = params.get("has_openleg");
                    if (params.get("has_openleg").length() > 0 && !header.matches("false"))
                        dimensionNames.add(header); // A hack for consistency
                }
            }

            // If date wasn't a specified dimension hack that in also, we'll
            // provide a dummy value later
            if (!dimensionNames.contains("ga:date"))
                feedDataNames.add("date");

            // Add all the dimension and metric names to our list
            feedDataNames.addAll(dimensionNames);
            for (Metric metric : firstEntry.getMetrics())
                feedDataNames.add(metric.getName());

            // Write out the header columns with proper casing
            for (String dataName : feedDataNames) {
                String cleaned = dataName.replace("ga:", "");
                bw.write(cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1) + ",");
            }
            bw.newLine();

            // This needs to account for the hacks we made above for everything
            // to work out.
            for (DataEntry entry : entries) {
                for (String dataName : feedDataNames) {
                    if (dataName == "date") // Different than ga:data
                        bw.write(gac.getDateString("end_date", params) + ",");
                    else if (dataName == header)
                        bw.write("\"" + OpenLegislationConnect.get(entry.stringValueOf("ga:pagePath")) + "\",");
                    else
                        bw.write("\"" + entry.stringValueOf(dataName) + "\",");
                }
                bw.newLine();
            }
            bw.newLine();
            bw.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
