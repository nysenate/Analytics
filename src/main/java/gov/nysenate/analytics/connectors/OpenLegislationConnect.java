package gov.nysenate.analytics.connectors;

import gov.nysenate.analytics.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class OpenLegislationConnect {
	
	static final String URL = "http://open.nysenate.gov";
	static final String BILL_URL = "http://open.nysenate.gov/legislation/2.0/bill/";
	
	public static String get(String value) throws Exception {
		if(value.contains("/transcript/")) {
			Pattern p = Pattern.compile("Transcript: \\w{3} \\d{1,2}, \\d{4}( \\d{1,2}:\\d{2} (AM|PM))?");

			String title = getTitle(URL + value,p);
						
			title = title.replaceAll("^\\s*<h2>","");
			title = title.replaceAll("</h2>$","");
			
			return title.replaceAll("\"", "");
		}
		else if(value.contains("/meeting/")) {
			String title = getTitle(URL + value);
			
			title = title.replaceAll("^\\s+<title>Committee Meeting: ", "");
			title = title.replaceAll(" - New York State Senate</title>$", "");
			
			return title.replaceAll("\"", "");
		}
		else if(value.contains("/calendar/")) {
			Pattern p = Pattern.compile("<h2>Calendar no. \\d{1,5} \\((floor|active)\\) / Year: \\d{4} / Session: .*?</h2>");
			
			String title = getTitle(URL + value,p);
			title = title.replaceAll("^\\s*<h2>","");
			title = title.replaceAll("</h2>$","");
				
			return title.replaceAll("\"", "");
		}
		else if(value.contains("/search")) {
			String ret = "";
			Matcher type1 = Pattern.compile("/search/([^?]+)\\?(.*)$").matcher(value);
			Matcher type2 = Pattern.compile("/search/\\?([^?]+)(.*)$").matcher(value);
			
			if(type1.find()) {
				ret = type1.group(1);
			} else if (type2.find()) {
				ret = type2.group(1);
			}
			System.out.println("  "+ret);
			/*
			Pattern pattern = Pattern.compile("(\\?|&)\\w*?=[\\w\\W&&[^&]]*");
			Matcher m = pattern.matcher(value);
			String ret = "";
			
			while(m.find()) {
				String term = value.substring(m.start(),m.end());
				try {
					String termValue = term.split("(\\?|&)\\w*?=")[1];
					String termStripped = term.replaceAll("(&|\\?|=[\\w\\W]*)", "");
					
					ret += (!ret.equals("") ? "; " : "") + termStripped + ": " + termValue;
				}
				catch (Exception e) {
					
				}
				
			}
			*/
			return ret.replaceAll("\"", "'").replaceAll("\\+", " ");
		}
		else { //it's a bill
			//Pattern p = Pattern.compile("<h2>[A-Z]\\d{2,5}\\w?\\-\\d{1,4}:.*?</h2>");

			InputStream content = new URL(BILL_URL + value + ".json").openStream();
			JSONObject data = new JSONObject(new Scanner(content).useDelimiter("\\A").next());
			
			String title = data.getJSONObject("response").getJSONArray("results").getJSONObject(0).getJSONObject("data").getJSONObject("bill").getString("title");
			return title.replaceAll("\"", "");
		}
	}
	
	public static String getTitle(String url, Pattern p) throws Exception {
		System.out.println("  "+url);
		try {
			BufferedReader br = Utils.getReader(url);
			String in = null;
			
			while((in = br.readLine()) != null) {
				Matcher m = p.matcher(in);
				
				if(m.find()) {
					return in;
					
				}
			}
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static String getTitle(String url) throws Exception {
		System.out.println("  "+url);
		try {
			BufferedReader br = Utils.getReader(url);
			String in = null;
			
			while((in = br.readLine()) != null) {
				if(in.contains("<title>")) {
					return in;
					
				}
			}
			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}	
}
