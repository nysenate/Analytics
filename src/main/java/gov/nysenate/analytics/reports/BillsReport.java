package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.Utils;
import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;
import gov.nysenate.analytics.connectors.OpenLegislationConnect;
import gov.nysenate.analytics.models.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gdata.data.analytics.DataEntry;

/**
 * Handles reports with `report_type=bills` and writes them to the `output_file` as
 * a CSV file in the following format:
 * 
 * > Date,Bill,Title,Views,Bounces Time
 * > ...
 * > ...
 * 
 * Use GoogleAnalytics to find the top 5 most popular bills broken down by source of traffic.
 * 
 * See GoogleAnalyticsConnect for information on configuring your queries.
 * 
 * @author GraylinKim
 * 
 */
public class BillsReport extends CSVReport
{
    private static Pattern billPattern = Pattern.compile("([A-Z][.-]?\\d{3,5}[.-]?[A-Z]?\\-\\d{1,4})", Pattern.CASE_INSENSITIVE);

    public static void generateCSV(GoogleAnalyticsConnect gac, Section params) throws IOException
    {
        CSVWriter writer = getCSVWriter(params);
        writer.writeNext(params.get("column_headers").split(","));
        try {
            ArrayList<String> topBills = new ArrayList<String>();

            for (DataEntry entry : gac.getDataFeed(params).getEntries()) {
                String billUri = entry.stringValueOf("ga:pagePath");
                Matcher m = billPattern.matcher(billUri);
                if (m.find()) {
                    String bill = m.group(1).toUpperCase();
                    if (!(params.get("amended") == "true") && bill.matches("^[a-zA-Z][.-]?\\d{3,5}[.-]?[a-zA-Z]$")) {
                        bill = bill.substring(0, bill.length() - 1);
                    }

                    if (!topBills.contains(bill)) {
                        String title = OpenLegislationConnect.get(bill);
                        params.put("filters", "ga:pagePath=@" + bill);
                        List<Source> lst = Utils.combineDataFeedBySource(gac.getDataFeed(params).getEntries(), bill);
                        Collections.sort(lst);
                        for (Source source : Utils.groupOthers(lst, Integer.parseInt(params.get("count")))) {
                            writer.writeNext(new String[] {
                                    params.get("end_date"),
                                    bill,
                                    title,
                                    source.source,
                                    String.valueOf(source.pageviews),
                                    String.valueOf(source.bounces),
                                    String.valueOf(source.timeOnPage)
                            });
                        }
                        writer.writeNext(new String[] {});
                        System.out.println("Adding top bill: " + bill);
                        topBills.add(bill);
                        if (topBills.size() == Integer.parseInt(params.get("count")))
                            break;
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println("[bill] Error retrieving bill from OpenLeg" + e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            writer.close();
        }
    }
}
