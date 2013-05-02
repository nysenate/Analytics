package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;
import gov.nysenate.analytics.connectors.OpenLegislationConnect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;

/**
 * Handles reports with `report_type=simple_analytics` and writes them to the `output_file` as
 * a CSV file in a simple header followed by data format. The data included is determined from
 * the results of the GoogleAnalytics query performed. The header is derived from the available
 * dimension and metric names.
 * 
 * See GoogleAnalyticsConnect for information on configuring your queries.
 * 
 * @author GraylinKim
 * 
 */
public class SimpleReport extends CSVReport
{
    public static void generateCSV(GoogleAnalyticsConnect gac, Section params) throws IOException
    {
        CSVWriter writer = getCSVWriter(params);
        try {

            List<DataEntry> entries = gac.getDataFeed(params).getEntries();

            // Get a list of all the feed data columns from the first data entry
            // This requires some ugly hacking because we've got some
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
            ArrayList<String> values = new ArrayList<String>();
            for (String dataName : feedDataNames) {
                String cleaned = dataName.replace("ga:", "");
                values.add(cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1));
            }
            writer.writeNext(values.toArray(new String[] {}));

            // This needs to account for the hacks we made above for everything to work out.

            for (DataEntry entry : entries) {
                values.clear();
                for (String dataName : feedDataNames) {
                    if (dataName == "date") // Different than ga:data
                        values.add(params.get("end_date"));
                    else if (dataName == header)
                        values.add(OpenLegislationConnect.get(entry.stringValueOf("ga:pagePath")));
                    else
                        values.add(entry.stringValueOf(dataName));
                }
                writer.writeNext(values.toArray(new String[] {}));
            }

        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            writer.close();
        }
    }
}
