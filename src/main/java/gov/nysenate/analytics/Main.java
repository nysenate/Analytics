package gov.nysenate.analytics;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import gov.nysenate.analytics.connectors.GoogleAnalyticsConnect;

import gov.nysenate.analytics.reports.BillsReport;
import gov.nysenate.analytics.reports.FacebookReport;
import gov.nysenate.analytics.reports.LivestreamReport;
import gov.nysenate.analytics.reports.SenatorsReport;
import gov.nysenate.analytics.reports.SimpleReport;
import gov.nysenate.analytics.reports.SocialMediaReport;
import gov.nysenate.analytics.reports.TwitterReport;
import gov.nysenate.analytics.structures.NYSenate;
import gov.nysenate.analytics.Utils;

public class Main {
	
	public static void main(String[] args) throws Exception {
		Ini config = new Ini(new File("analytics.ini"));
		GoogleAnalyticsConnect googleConnector = new GoogleAnalyticsConnect(config.get("service:analytics"));
		
		//Set the senator data from the .gov website. Add the NYSenate Accounts
		List<NYSenate> senatorData = Utils.SenatorData();
		NYSenate tObj = new NYSenate();
		tObj.fName = "NYSenate";
		tObj.twitterURL  = "http://www.twitter.com/nysenate";
		tObj.facebookURL = "http://www.facebook.com/NYsenate";
		senatorData.add(tObj);

		for(Map.Entry<String,Section> entry : config.entrySet()) {
			//Skip non-report related blocks
			if(!entry.getKey().startsWith("report:")) continue;
			
			//Get the report type and log the start of processing
			String report_type = entry.getValue().get("report_type");
			System.out.println("Processing: "+entry.getKey()+", Report Type: "+report_type);
			
			if(report_type.equals("senators"))
				SenatorsReport.generateCSV(googleConnector, senatorData, entry.getValue());
			
			else if(report_type.equals("bills"))
				BillsReport.generateCSV(googleConnector,  entry.getValue());
			
			else if(report_type.equals("simple_analytics")) 
				SimpleReport.generateCSV(googleConnector, entry.getValue());
			
			else if(report_type.equals("livestream"))
				LivestreamReport.generateCSV(entry.getValue());
			
			else if(report_type.equals("twitter"))
				TwitterReport.generateCSV(senatorData, entry.getValue());
			
			else if(report_type.equals("facebook"))
				FacebookReport.generateCSV(senatorData, entry.getValue());
			
			else if(report_type.equals("socialmedia"))
				SocialMediaReport.generateCSV(senatorData, entry.getValue());
		}
		System.out.println("Done");
	}
}
