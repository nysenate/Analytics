package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.Utils;
import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;
import gov.nysenate.analytics.connectors.OpenLegislationConnect;
import gov.nysenate.analytics.models.Source;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ini4j.Profile.Section;

import com.google.gdata.data.analytics.DataEntry;

public class BillsReport extends CSVReport
{

    public static boolean generateCSV(GoogleAnalyticsConnect gac, Section params)
    {

        try {
            ArrayList<String> topBills = new ArrayList<String>();
            Pattern p = Pattern.compile("[a-zA-Z][.-]?\\d{3,5}[.-]?[a-zA-Z]?\\-\\d{1,4}");

            BufferedWriter bw = getOutputWriter(params);
            bw.write(params.get("column_headers"));
            bw.newLine();

            for (DataEntry entry : gac.getDataFeed(params).getEntries()) {
                String billUri = entry.stringValueOf("ga:pagePath");
                Matcher m = p.matcher(billUri);
                if (m.find()) {
                    String bill = billUri.substring(m.start(), m.end());
                    if (!(params.get("amended") == "true") && bill.matches("^[a-zA-Z][.-]?\\d{3,5}[.-]?[a-zA-Z]$")) {
                        bill = billUri.substring(m.start(), m.end() - 1);
                    }

                    String title = OpenLegislationConnect.get(bill);

                    params.put("filters", "ga:pagePath=@" + bill);
                    List<Source> lst = Utils.combineDataFeedBySource(gac.getDataFeed(params).getEntries(), "/bill/");
                    Collections.sort(lst);
                    for (Source source : Utils.groupOthers(lst, Integer.parseInt(params.get("count")))) {
                        bw.write(params.get("end_date") + ","
                                + bill + ",\""
                                + title + "\","
                                + source.source + ","
                                + source.pageviews + ","
                                + source.bounces + ","
                                + source.time);
                        bw.newLine();
                    }

                    if (!topBills.contains(bill)) {
                        topBills.add(bill);
                        if (topBills.size() == Integer.parseInt(params.get("count")))
                            break;
                    }
                }
            }
            bw.close();
            return true;

        }
        catch (IOException e) {
            System.out.println("[bill]:output_file could not be opened for writing" + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("[bill] Error retrieving bill from OpenLeg" + e.getMessage());
        }
        return false;
    }
}
