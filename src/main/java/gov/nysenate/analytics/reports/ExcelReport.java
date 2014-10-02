package gov.nysenate.analytics.reports;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVReader;

public class ExcelReport
{
  public static void generateExcel(Ini config) throws IOException
  {
    BufferedReader br = null;
    Workbook wb = new HSSFWorkbook();
    CreationHelper helper = wb.getCreationHelper();

    for (Map.Entry<String, Section> entry : config.entrySet()) {
      try {
        // Skip non-report related blocks
        if (!entry.getKey().startsWith("report:")) {
          continue;
        }

        // Get the report type and log the start of processing
        String report_type = entry.getKey().split(":")[1];
        String fileName = entry.getValue().get("output_file");
        System.out.println("Adding sheet for: " + report_type);

        Sheet sheet = wb.createSheet(report_type);
        CSVReader reader = new CSVReader(new FileReader(fileName));
        List<String[]> csvRows = reader.readAll();
        for (int r = 0; r < csvRows.size(); r++) {
          Row xlsRow = sheet.createRow(r);
          String[] csvRow = csvRows.get(r);
          for (int c = 0; c < csvRow.length; c++) {
            xlsRow.createCell(c).setCellValue(helper.createRichTextString(csvRow[c]));
          }
        }
        reader.close();
        for (int column = 0; column < csvRows.get(0).length; column++) {
          sheet.autoSizeColumn(column);
        }
      }
      catch (IOException e) {
        System.out.println("Exception while adding sheet " + entry.getKey());
        System.out.println(e);
        continue;
      }
      finally {
        if (br != null)
          br.close();
      }
    }

    createReferenceSheet(wb);
    orderSheets(wb);

    // Write the output to a file
    FileOutputStream fileOut = new FileOutputStream(getFileName(config));
    System.out.println("Writing to "+getFileName(config));
    wb.write(fileOut);
    fileOut.close();
  } // generateExcel()


  private static void createReferenceSheet(Workbook wb)
  {
    System.out.println("Adding Sheet for: About the Statistics");
    Sheet refSheet = wb.createSheet("About the Statistics");
    List<String[]> refInfo = new ArrayList<String[]>();
    refInfo.add(new String[] { "Worksheet", "Date Range", "Description" });
    refInfo.add(new String[] { "NYSenate Traffic", "Launch of NYSenate.gov to present", "Provides daily web analytics of the NY Senate's homepage - NYSenate.gov" });
    refInfo.add(new String[] { "NYSenate Home Traffic", "Launch of NYSenate.gov to present", "Provides daily web analytics for the all pages in the NY Senate's website - Nysenate.gov" });
    refInfo.add(new String[] { "Senator Traffic", "Current month", "Provides web analytics for each of the Senator's websites" });
    refInfo.add(new String[] { "OpenLeg Traffic", "Launch of Open Legislation website to present",
        "Provides daily web analytics for the Open Legislation website - open.nysenate.gov/legislation" });
    refInfo.add(new String[] { "OpenLeg Top 5 Pages", "Current month",
        "Provides web statistics for five Open Legislation pages viewed for current month on the NY Senate's Open Legislation Service's website - open.nysenate.gov/legislation" });
    refInfo.add(new String[] { "OpenLeg Top 5 Bills", "Current month",
        "Provides web statistics for five bills viewed for current month on the NY Senate's Open Legislation Service's website - open.nysenate.gov/legislation" });
    refInfo.add(new String[] { "OpenLeg Top 5 Keywords", "Current month",
        "Presents top five keyword searches based on page views done on the NY Senate's Open Legislation Service's website - open.nysenate.gov/legislation" });
    refInfo.add(new String[] { "OpenLeg Top 5 Transcripts", "Current month",
        "Presents top five transcripts based on page views on the NY Senate's Open Legislation Service's website - open.nysenate.gov/legislation" });
    refInfo.add(new String[] { "OpenLeg Top 5 Calendars", "Current month",
        "Presents top five legislative calendars based on page views on the NY Senate's Open Legislation Service's website - open.nysenate.gov/legislation" });
    refInfo.add(new String[] { "OpenLeg Top 5 Meetings", "Current month",
        "Presents top five committee meetings based on page views on the NY Senate's Open Legislation Service's website - open.nysenate.gov/legislation" });
    refInfo.add(new String[] { "Social Media Links", "Snapshot when statistics are generated",
        "Provides the url's of the following social media sites used by Senators: Twitter, Youtube, Facebook, and Flickr" });
    refInfo.add(new String[] { "Facebook Stats", "Snapshot when statistics are generated", "Shows number of fans for the Facebook sites linked to from the Senators' NYSenate.gov websites" });
    refInfo.add(new String[] { "Twitter Stats", "Snapshot when statistics are generated", "Provides statistics for the Twitter account linked to from the Senators' NYSenate.gov websites" });
    refInfo.add(new String[] { "Livestream Stats", "Current month / All Time", "Shows current month and all time viewing minutes for the Senate's channels on Livestream.com" });
    // refInfo.add(new String[] { "YouTube Stats", "Snapshot / All Time Shows",
    // "Shows statistics for the Senate's two YouTube channels: NYSenate and NYSenateUnCut" });

    CreationHelper helper = wb.getCreationHelper();
    for (int r = 0; r < refInfo.size(); r++) {
      Row xlsRow = refSheet.createRow(r);
      String[] csvRow = refInfo.get(r);
      for (int c = 0; c < csvRow.length; c++) {
        xlsRow.createCell(c).setCellValue(helper.createRichTextString(csvRow[c]));
      }
    }
    for (int column = 0; column < refInfo.get(0).length; column++) {
      refSheet.autoSizeColumn(column);
    }
  } // createReferenceSheet()


  private static void orderSheets(Workbook wb)
  {
    wb.setSheetOrder("About the Statistics", 0);
    wb.setSheetOrder("NYSenate Traffic", 1);
    wb.setSheetOrder("NYSenate Home Traffic", 2);
    wb.setSheetOrder("Senator Traffic", 3);
    wb.setSheetOrder("OpenLeg Traffic", 4);
    wb.setSheetOrder("OpenLeg Top 5 Pages", 5);
    wb.setSheetOrder("OpenLeg Top 5 Bills", 6);
    wb.setSheetOrder("OpenLeg Top 5 Keywords", 7);
    wb.setSheetOrder("OpenLeg Top 5 Transcripts", 8);
    wb.setSheetOrder("OpenLeg Top 5 Calendars", 9);
    wb.setSheetOrder("OpenLeg Top 5 Meetings", 10);
    wb.setSheetOrder("Social Media Links", 11);
    wb.setSheetOrder("Facebook Stats", 12);
    wb.setSheetOrder("Twitter Stats", 13);
    wb.setSheetOrder("Livestream Stats", 14);
    // wb.setSheetOrder("Youtube Stats", 15);
  } // orderSheets()


  public static String getFileName(Ini config)
  {
    String startDate = config.get("", "start_date");
    String endDate = config.get("", "end_date");
    return "scratch/NYSenate_Web_Analytics_"+startDate+"_to_"+endDate+".xls";
  } // getFileName()
}
