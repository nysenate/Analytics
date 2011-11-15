package analytics.google.v2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.Lists;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;

public class Process {	
	
	private static int NUM_BILLS = 5;
	public static final String PROP = "analytics.";
	
	public static List<String> getBills(DataFeed df) {
		List<String> top = new ArrayList<String>();		
		
		for(DataEntry de:df.getEntries()) {
			String billUri = de.stringValueOf("ga:pagePath");
			String bill;
			if((bill = billFromUri(billUri,false)) != null) {
								
				if(!top.contains(bill)) {
					top.add(bill);
				}				
				
				if(top.size() == NUM_BILLS) {
					break;
				}
			}			
		}
		
		return top;
	}
	
	private static String billFromUri(String uri, boolean amended) {
		Pattern p = Pattern.compile("[a-zA-Z][.-]?\\d{3,5}[.-]?[a-zA-Z]?\\-\\d{1,4}");
		Matcher m = p.matcher(uri);
		
		if(m.find()) {
			String ret = uri.substring(m.start(),m.end());			
			if(ret.matches("^[a-zA-Z][.-]?\\d{3,5}[.-]?[a-zA-Z]$") && !amended) {
				return uri.substring(m.start(),m.end()-1);
			}
			return ret;
		}
		return null;		
	}
	
	public static Collection<SourceObject> combineDataFeedBySource(List<DataEntry> entries, String pathMatch) {
		Map<String,SourceObject> map = new HashMap<String,SourceObject>();
		for(DataEntry de:entries) {
			String path = de.stringValueOf("ga:pagePath");			
			
			if(pathMatch == null || path.contains(pathMatch)) { 
				
				String s = ((pathMatch == null) ? path : de.stringValueOf("ga:source"));
				int view = new Integer(de.stringValueOf("ga:pageviews"));
				int b = new Integer(de.stringValueOf("ga:bounces"));
				double t = new Double(de.stringValueOf("ga:timeOnPage"));			
				
				if(map.containsKey(s)) {
					SourceObject so = map.get(s);
					so.setPageviews(so.getPageviews() + view);
					so.setBounces(so.getBounces() + b);
					so.setTime(so.getTime() + t);
					map.remove(s);
					map.put(s, so);
				}
				else {
					SourceObject so = new SourceObject(s, view, b, t);				
					map.put(so.getSource(), so);
				}				
			}
			else {
				
			}					
		}
		
		return map.values();
		
	}
	
	
	public static Collection<SenatorObject> senators(GAConnect gac, Properties props, String name) {		
		String prop = PROP + name;
		
		Collection<SenatorObject> ret = new ArrayList<SenatorObject>();
		
		String fil_s = "ga:pagePath=~^";
		String fil_e = ".*";
		
		List<SenatorObject> senators = null;

		try {
			senators = NYSenateConnect.SenatorData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(SenatorObject ao:senators) {
			
			System.out.println(ao.getUrlPath());
			
			DataQuery dq = gac.queryBuilderFilterRegex(props,prop,fil_s + ao.getUrlPath() + fil_e);
			
			DataFeed df = gac.getDataFeed(props, dq);
			
			Collection<SourceObject> lst = combineDataFeedBySource(df.getEntries(), ao.getUrlPath());
			
			lst = orderList(lst, props, prop, true);
			
			ao.setSource(lst);
			
			ret.add(ao);		
		}
		
		return ret;
	}
	
	public static Collection<SourceObject> simpleSearch(GAConnect gac, Properties props, String name) {
		String prop = PROP + name;
		
		Collection<SourceObject> lst = null;
		
		DataFeed df = gac.getDataFeed(props, gac.queryBuilder(props, prop));
		
		lst = combineDataFeedBySource(df.getEntries(), null);
		
		lst = orderList(lst, props, prop, false);
		
		return lst;
	}	
	
	public static Collection<SourceObject> oldSearch(GAConnect gac, Properties props, String name) throws IOException {
		String prop = PROP + name;
		
		Collection<SourceObject> lst = null;
		
		DataFeed df = gac.getDataFeed(props, gac.queryBuilder(props, prop));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("nysenate1.csv")));
		bw.write(props.getProperty(name + ".columns"));
		bw.newLine();
		
		for(DataEntry de:df.getEntries()) {
			String s = de.stringValueOf("ga:date");
			int view = new Integer(de.stringValueOf("ga:pageviews"));
			int b = new Integer(de.stringValueOf("ga:bounces"));
			double t = new Double(de.stringValueOf("ga:timeOnPage"));
			
			int visits = new Integer(de.stringValueOf("ga:visits"));
			
			String y = s.substring(0, 4);
			String m = s.substring(4,6);
			String d = s.substring(6,8);
			
			bw.write((m + "/" + d + "/" + y +  ", " + view + ", " + visits + ", " + b + ", " + t));
			bw.newLine();
		}
		
		bw.close();
		
		return lst;
	}
	
	public static void dayRangeViewsSearch(GAConnect gac, Properties props, String name) throws IOException {
		String prop = PROP + name;
		
		DataFeed df = gac.getDataFeed(props, gac.queryBuilder(props, prop));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(name + ".csv")));
		bw.write(props.getProperty(name + ".columns"));
		bw.newLine();
		
		for(DataEntry de:df.getEntries()) {
			String s = de.stringValueOf("ga:date");
			int view = new Integer(de.stringValueOf("ga:pageviews"));
			
			String y = s.substring(0, 4);
			String m = s.substring(4,6);
			String d = s.substring(6,8);
			
			bw.write((m + "/" + d + "/" + y +  "," + view));
			bw.newLine();
		}
		
		bw.close();
	}
	
	public static Collection<BillObject> bill(GAConnect gac, Properties props, String name) {		
		String prop = PROP + name;
		
		Collection<BillObject> ret = new ArrayList<BillObject>();
				
		DataFeed df = gac.getDataFeed(props, gac.queryBuilder(props, prop));	
		
		List<String> bills = getBills(df);
		
		df = null;		
		
		for(String bill:bills) {			
			DataQuery dq = gac.queryBuilderFilterRegex(props,prop, "ga:pagePath=@" + bill);
			
			df = gac.getDataFeed(props, dq);
						
			Collection<SourceObject> lst = Process.combineDataFeedBySource(df.getEntries(), "/bill/");
			
			lst = orderList(lst, props, prop, true);
			
			ret.add(new BillObject(bill,lst));
		}
		
		return ret;
	}
	
	public static Collection<SourceObject> truncateList(Collection<SourceObject> lst, int count, boolean oTog) {
		Collection<SourceObject> ret = new ArrayList<SourceObject>();
		
		int  i = 0;
		
		SourceObject other = null;;
		
		int bounces = 0, pageviews = 0;
		double time = 0;
		
		for(SourceObject so:lst) {
			if(i < count) {
				ret.add(so);				
				i++;
			}
			else {
				bounces += so.getBounces();
				pageviews += so.getPageviews();
				time += so.getTime();
			}			
		}
		
		if(oTog) {
			other = new SourceObject("other",pageviews,bounces,time);
			ret.add(other);
		}
		
		
		
		return ret;
	}
	
	public static Collection<SourceObject> orderList(Collection<SourceObject> lst, Properties props, String prop, boolean oTog) {
		SourceObject[] obs = new SourceObject[lst.size()];
		lst.toArray(obs);
		
		SourceObject[] sos = sortByViews((SourceObject[])obs, 0, obs.length-1);

		lst = truncateList(Lists.newArrayList(sos), new Integer(props.getProperty(prop+".count")),oTog);		
		
		return truncateList(Lists.newArrayList(sos), new Integer(props.getProperty(prop+".count")),oTog);	
	}
	
	
	public static SourceObject[] sortByViews(SourceObject[] sos, int low, int high) {		
		if(high > low) {
			int partitionPivot = (int)(Math.random()*(high-low) + low);
			int newPivot = partition(sos, low, high, partitionPivot);
			sortByViews(sos, low, newPivot-1);
			sortByViews(sos, newPivot+1,high);			
		}		
		return sos;
	}
	
	private static int partition(SourceObject[] sos, int low, int high, int pivot) {
		SourceObject so = sos[pivot];
		
		swap(sos, pivot, high);
		
		int index = low;
		
		for(int i = low; i < high; i++) {
			if((sos[i]).getPageviews() > (so.getPageviews())) {
				swap(sos, i, index);
				index++;
			}
		}
		
		swap(sos, high, index);
		
		return index;
	}
	
	private static void swap(SourceObject[] sos, int i, int j) {
		SourceObject temp = sos[i];
		sos[i] = sos[j];
		sos[j] = temp;
	}	
}
