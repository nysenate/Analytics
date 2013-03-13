package gov.nysenate.analytics.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class ExcelReport
{
    public static void generateExcel(Ini config) throws Exception
    {
        BufferedReader br = null;
        Workbook wb = new HSSFWorkbook();
        CreationHelper helper = wb.getCreationHelper();

        for (Map.Entry<String, Section> entry : config.entrySet()) {
            try {
                // Skip non-report related blocks
                if (!entry.getKey().startsWith("report:"))
                    continue;

                // Get the report type and log the start of processing
                String report_type = entry.getKey().split(":")[1];
                String fileName = entry.getValue().get("output_file");
                System.out.println("Addind sheet for: " + report_type);

                Sheet sheet = wb.createSheet(report_type);
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));

                String webLine = "";

                int r = 0;
                while ((webLine = br.readLine()) != null) {
                    Row row = sheet.createRow((short) r++);
                    // String[] line = webLine.split(",");
                    String[] line = null;
                    if (webLine.contains("\",\"")) {
                        line = webLine.split("\",\"");
                        line[line.length - 1] = line[line.length - 1].split("\"")[0];
                    }
                    else {
                        line = webLine.split(",");
                    }
                    for (int i = 0; i < line.length; i++)
                        row.createCell(i)
                                .setCellValue(helper.createRichTextString(line[i]));
                }
            }
            catch (Exception e) {
                System.out.println("Exception while adding sheet " + entry.getKey());
                System.out.println(e);
                continue;
            }
            finally {
                if (br != null)
                    br.close();
            }
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("scratch/socialReport.xls");
        wb.write(fileOut);
        fileOut.close();

    }
}
