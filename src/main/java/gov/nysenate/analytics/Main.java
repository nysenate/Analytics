package gov.nysenate.analytics;

import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;
import gov.nysenate.analytics.models.NYSenate;
import gov.nysenate.analytics.reports.BillsReport;
import gov.nysenate.analytics.reports.ExcelReport;
import gov.nysenate.analytics.reports.FacebookReport;
import gov.nysenate.analytics.reports.LivestreamReport;
import gov.nysenate.analytics.reports.SenatorsReport;
import gov.nysenate.analytics.reports.SimpleReport;
import gov.nysenate.analytics.reports.SocialMediaReport;
import gov.nysenate.analytics.reports.TwitterReport;
import gov.nysenate.analytics.reports.YoutubeReport;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;


public class Main
{
  /**
   * @author GraylinKim
   * @author Ken Zalewski
   *
   * @param args
   *      A list of strings read in as Posix command line arguments
   *
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    try {
      String usage = "Usage: "+args[0]+" --help {-s|--start_date} YYYY-MM-DD {-e|--end_date} YYYY-MM-DD {-f|--ini-file} config_file";
      Options options = new Options();
      options.addOption("s", "start-date", true, "Start date of the report.");
      options.addOption("e", "end-date", true, "End date of the report.");
      options.addOption("f", "ini-file", true, "Path to configuration file.");
      options.addOption("h", "help", false, "Print this message");
      CommandLine opts = new PosixParser().parse(options, args);

      if (opts.hasOption("-h")) {
        System.out.println(usage);
        System.exit(0);
      }
      else if (!opts.hasOption('s') || !opts.hasOption('e') || !opts.hasOption('f')) {
        System.err.println("--start-date, --end-date, and --ini-file are required.");
        System.err.println(usage);
        System.exit(1);
      }

      File iniFile = new File(opts.getOptionValue('f'));
      String startDate = opts.getOptionValue('s');
      String endDate = opts.getOptionValue('e');
      String dateFormat = "^\\d{4}-\\d{2}-\\d{2}$";

      if (!startDate.matches(dateFormat)) {
        System.err.println("Invalid start date '"+startDate+"'; YYYY-MM-DD required");
        System.exit(1);
      }
      else if (!endDate.matches(dateFormat)) {
        System.err.println("Invalid end date '"+endDate+"'; YYYY-MM-DD required");
        System.exit(1);
      }
      else if (!iniFile.isFile()) {
        System.err.println("Invalid config file '"+iniFile.getAbsoluteFile()+"'");
        System.exit(1);
      }

      run(new Ini(iniFile), startDate, endDate);
    }
    catch (ParseException e) {
      System.err.println("Unable to parse command line arguments." + e);
      e.printStackTrace(System.err);
      System.exit(1);
    }
  } // main()


  /**
   * @author GraylinKim
   * 
   * @param config
   *      Active configuration object. See analytics.ini.example.
   * 
   * @param startDate
   *      The start date to be injected into report configurations that do
   *      not specify a start date.
   * 
   * @param endDate
   *      The end date to be injected into report configurations that do
   *      not specify an end date.
   * 
   * @throws Exception
   */
  public static void run(Ini config, String startDate, String endDate) throws Exception
  {
    Section analyticsConfig = config.get("service:analytics");
    GoogleAnalyticsConnect googleConnector = new GoogleAnalyticsConnect(
            analyticsConfig.get("service_account_id"),
            new File(analyticsConfig.get("key_file")),
            analyticsConfig.get("app_name")
    );

    List<NYSenate> senatorData = Utils.getSenateData();

    for (Map.Entry<String, Section> entry : config.entrySet()) {
      // Skip non-report blocks
      if (!entry.getKey().startsWith("report:")) {
        continue;
      }

      // Inject missing start/end dates into the report configuration
      Section reportConfig = entry.getValue();
      if (!reportConfig.containsKey("start_date")) {
        reportConfig.add("start_date", startDate);
      }
      if (!reportConfig.containsKey("end_date")) {
        reportConfig.add("end_date", endDate);
      }

      String reportType = reportConfig.get("report_type");

      System.out.println("Processing: "+reportType+" "+entry.getKey());
      switch (reportType) {
      case "senators":
        SenatorsReport.generateCSV(googleConnector, senatorData, reportConfig);
        break;
      case "bills":
        BillsReport.generateCSV(googleConnector, reportConfig);
        break;
      case "simple_analytics":
        SimpleReport.generateCSV(googleConnector, reportConfig);
        break;
      case "livestream":
        LivestreamReport.generateCSV(reportConfig);
        break;
      case "twitter":
        TwitterReport.generateCSV(senatorData, reportConfig);
        break;
      case "facebook":
        FacebookReport.generateCSV(senatorData, reportConfig);
        break;
      case "youtube":
        YoutubeReport.generateCSV(senatorData, reportConfig);
        break;
      case "socialmedia":
        SocialMediaReport.generateCSV(senatorData, reportConfig);
        break;
      default:
        System.err.println("Unknown report type: "+reportType+"; skipping.");
        break;
      }
    }

    config.put("", "start_date", startDate);
    config.put("", "end_date", endDate);
    ExcelReport.generateExcel(config);
    Utils.emailExcel(config);
    System.out.println("Done");
  } // run()
}
