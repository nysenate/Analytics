package gov.nysenate.analytics;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;

import gov.nysenate.analytics.reports.BillsReport;
import gov.nysenate.analytics.reports.ExcelReport;
import gov.nysenate.analytics.reports.ExcelReportEmail;
import gov.nysenate.analytics.reports.FacebookReport;
import gov.nysenate.analytics.reports.LivestreamReport;
import gov.nysenate.analytics.reports.SenatorsReport;
import gov.nysenate.analytics.reports.SimpleReport;
import gov.nysenate.analytics.reports.SocialMediaReport;
import gov.nysenate.analytics.reports.TwitterReport;
import gov.nysenate.analytics.reports.YoutubeReport;
import gov.nysenate.analytics.structures.NYSenate;
import gov.nysenate.analytics.Utils;

public class Main {

	public static void main(String[] args) throws Exception {


		String[] required = null;
        CommandLine opts = null;



        try {
        	Options options = new Options()
            .addOption("s", "start_date", true, "Start Date of the Report.")
            .addOption("e", "end_date", true, "End Date of the Report.")
            .addOption("h", "help", false, "Print this message");
        opts = new PosixParser().parse(options, args);
        required = opts.getArgs();
        System.out.println(" req length >>" + opts.getOptionValue('e')+"<<");

        	System.out.println(" req  " + opts.getOptionValue('s'));
        if(opts.hasOption("-h")) {
            System.err.println("USAGE: Main --start_date yyyy-mm-dd --end_date yyyy-mm-dd ");
            System.exit(0);
        }
        if (!opts.hasOption('s') && !opts.hasOption('e')) {
            System.err.println("--start_date  and --end_date is required..");
            System.err.println("USAGE:Main --start_date yyyy-mm-dd --end_date yyyy-mm-dd ");
            System.exit(1);
        }
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }

		  String startDate = null, endDate = null;

		  Ini config = new Ini(new File("analytics.ini"));
		  Section serviceConfig = config.get("service:analytics");
		  if(opts.hasOption("-start_date")) {
		   serviceConfig.add("start_date", opts.getOptionValue('s'));
		  }
		  if(opts.hasOption("-end_date")) {
		   serviceConfig.add("end_date", opts.getOptionValue('e'));
		  }

		//Ini config = new Ini(new File("analytics.ini"));
		GoogleAnalyticsConnect googleConnector = new GoogleAnalyticsConnect(config.get("service:analytics"));

		//Set the senator data from the .gov website. Add the NYSenate Accounts
		List<NYSenate> senatorData = Utils.SenatorData();
		NYSenate tObj = new NYSenate();
		tObj.fName = "NYSenate";
		tObj.twitterURL  = "http://www.twitter.com/nysenate";
		tObj.facebookURL = "http://www.facebook.com/NYsenate";
		tObj.youtubeURL = "http://www.youtube.com/user/NYSenate";
		senatorData.add(tObj);

		NYSenate uncutObj = new NYSenate();
		uncutObj.fName = "NYSenateUncut";
		uncutObj.youtubeURL = "http://www.youtube.com/user/nysenateuncut";
		senatorData.add(uncutObj);

		for(Map.Entry<String,Section> entry : config.entrySet()) {
			//Skip non-report related blocks
			if(!entry.getKey().startsWith("report:")) continue;

			//if(!entry.getValue().get("report_type").equals("simple_analytics")) continue;

			//Get the report type and log the start of processing
			String report_type = entry.getValue().get("report_type");
			System.out.println("Processing: "+entry.getKey()+", Report Type: "+report_type);

		if(report_type.equals("senators"))
				SenatorsReport.generateCSV(googleConnector, senatorData, entry.getValue());

			else if(report_type.equals("bills"))
				BillsReport.generateCSV(googleConnector,  entry.getValue());

			else if(report_type.equals("simple_analytics"))
				SimpleReport.generateCSV(googleConnector, entry.getValue());

			else if(report_type.equals("livestream")){
				Section params = entry.getValue();
				params.put("end_date", config.get("service:analytics", "end_date"));
				LivestreamReport.generateCSV(params);
			}

			else if(report_type.equals("twitter"))
				TwitterReport.generateCSV(senatorData, entry.getValue());

			else if(report_type.equals("facebook")){
				Section params = entry.getValue();
				params.put("end_date", config.get("service:analytics", "end_date"));
				FacebookReport.generateCSV(senatorData, params);
			}

			else if(report_type.equals("youtube")){
				Section params = entry.getValue();
				params.put("end_date", config.get("service:analytics", "end_date"));
				YoutubeReport.generateCSV(senatorData, params);
			}

			else if(report_type.equals("socialmedia")){
				Section params = entry.getValue();
				params.put("end_date", config.get("service:analytics", "end_date"));
				SocialMediaReport.generateCSV(senatorData, params);
			}
		}
		ExcelReport.generateExcel(config);
		ExcelReportEmail.emailExcel(config);


		System.out.println("Done");


	}

}
