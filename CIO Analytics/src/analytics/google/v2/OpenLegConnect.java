package analytics.google.v2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// for bills

public class OpenLegConnect {
	
	static final String URL = "http://open.nysenate.gov";
	
	static final String BILL_URL = "http://open.nysenate.gov/legislation/api/1.0/html/bill/";
	
	public static String get(String value) throws Exception {
		if(value.contains("/transcript/")) {
			Pattern p = Pattern.compile("<h2>Transcript: \\w{3} \\d{1,2}, \\d{4}( \\d{1,2}:\\d{2} (AM|PM))?</h2>");

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
			return ret.replaceAll("\"", "");
		}
		else { //it's a bill
			Pattern p = Pattern.compile("<h2>[A-Z]\\d{2,5}\\w?\\-\\d{1,4}:.*?</h2>");

			String title = getTitle(BILL_URL + value,p);

			title = title.replaceAll("^\\s*<h2>","");
			title = title.replaceAll("</h2>$","");

			return title.replaceAll("\"", "");
		}
	}
	
	public static String getTitle(String url, Pattern p) throws Exception {
		System.out.println(url);
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		}
		catch (Exception e) {
			return "";
		}
		
		String in = null;
		
		while((in = br.readLine()) != null) {
			Matcher m = p.matcher(in);
			
			if(m.find()) {
				return in;
				
			}
		}
		br.close();
		
		return "";
	}
	
	public static String getTitle(String url) throws Exception {
		System.out.println(url);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url.replaceAll(" ", "%20")).openStream()));
			
			String in = null;
			
			while((in = br.readLine()) != null) {
				if(in.contains("<title>")) {
					return in;
					
				}
			}
			br.close();
		}
		catch(Exception e) {
			return "";
		}
		
		return null;
	}
	
	
	
//	
//	
//	public static void getBOTitle(Collection<BillObject> lst) throws Exception {		
//		Pattern p = Pattern.compile("<h2>[A-Z]\\d{2,5}:.*?</h2>");
//			
//		for(BillObject b:lst) {
//			String title = getTitle(BILL_URL + b.getBillNo(),p);
//			
//			title = title.replaceAll("^\\s*<h2>","");
//			title = title.replaceAll("</h2>$","");
//			
//			System.out.println(title);
//			
//		}
//	}
//	
//	public static void GetSOTitle(Collection<SourceObject> lst) throws Exception {
//		SourceObject so = lst.iterator().next();
//		
//		if(so.getSource().contains("/transcript/")) {
//			Pattern p = Pattern.compile("<h2>Transcript: \\w{3} \\d{1,2}, \\d{4} \\d{1,2}:\\d{2} (AM|PM)</h2>");
//			
//			for(SourceObject s:lst) {
//				String title = getTitle(URL + s.getSource(),p);
//				
//				title = title.replaceAll("^<h2>","");
//				title = title.replaceAll("</h2>$","");
//				
//				System.out.println(title);
//			}
//		}
//		else if(so.getSource().contains("/meeting/")) {
//			
//			for(SourceObject s:lst) {
//				String title = getTitle(URL + s.getSource());
//				
//				title = title.replaceAll("^<title>Committee Meeting: ", "");
//				title = title.replaceAll(" - New York State Senate</title>$", "");
//				
//				System.out.println(title);
//			}
//		}
//		else if(so.getSource().contains("/calendar/")) {
//			Pattern p = Pattern.compile("<h2>Calendar no. \\d{1,5} \\((floor|active)\\) / Year: \\d{4} / Session: .*?</h2>");
//			
//			for(SourceObject s:lst) {
//				String title = getTitle(URL + s.getSource(),p);
//				
//				title = title.replaceAll("^<h2>","");
//				title = title.replaceAll("</h2>$","");
//				
//				System.out.println(title);
//			}
//		}
//	}
	
}
