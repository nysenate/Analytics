package analytics.livestream.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import Constant.Constants;
import analytics.livestream.generated.Channel;
import analytics.livestream.generated.Rss;





public class LiveStreamParser implements Constant.Constants {
	
	public static final String apiUrlStart = "http://x";
	public static final String apiUrlEnd = "x.channel-api.livestream-api.com/2.0/info";

	
	public LiveStreamParser() {
		List<LiveStreamStat> lssList;
		try {
			lssList = getUpdates();
			writeUpdates(lssList);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public List<LiveStreamStat> getUpdates() throws MalformedURLException, IOException, Exception {
		File f = new File("lschannels");	
		
		List<LiveStreamStat> lssList = new ArrayList<LiveStreamStat>();
		
	
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String key = null;
		
		while((key = br.readLine()) != null) {
			LiveStreamStat lss = doParsing(new URL(apiUrlStart + key + apiUrlEnd));
			lss.setKey(key);
			
			lssList.add(lss);
		}		
		
		br.close();
		
		return lssList;
	}
	
	public void writeUpdates(List<LiveStreamStat> lssList) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("scratch/livestream.csv")));
		
		bw.write("Date,Channel,Weekly Minutes,Total Minutes");
		bw.newLine();
		
		for(LiveStreamStat lss:lssList) {
			bw.write(Constants.DATE + ",");
			bw.write(lss.getTitle().replaceAll(",", " ") + "," + lss.getWeekly() + "," + lss.getTotal());
			bw.newLine();		
		}
		bw.write("\n,Total,");		
		bw.write(getTotal(lssList, LiveStreamStat.class, "getWeekly") + ",");
		bw.write(Integer.toString(getTotal(lssList, LiveStreamStat.class, "getTotal")));
		
		bw.close();
	}
	
	public int getTotal(List<LiveStreamStat> lssList, Class<?> c, String method) {
				
		Method[] methods = c.getDeclaredMethods();
		
		Method m = null;
		
		int count = 0;
		
	
		for(Method tm:methods) {
			if(tm.getName().equals(method)) {
				m = tm;
			}
		}
		
		if(m != null) {
			for(LiveStreamStat lss:lssList) {
				try {
					count += (Integer)m.invoke(lss);
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
			
		
		
		return count;
	}
	
	
	public LiveStreamStat doParsing(URL url) throws Exception {
		Rss rss = parseStream(url);
		
		Channel channel = rss.getChannel();
		
		String title = channel.getTitle();
		
		String link = channel.getLink();
		
		int weekMinutes = channel.getViewerMinutesThisWeek();
		
		int totalMinutes = channel.getTotalViewerMinutes();
		
		
		
		return new LiveStreamStat(title, link, weekMinutes,totalMinutes);
	}
	
	
	
	public Rss parseStream(URL url) throws Exception {
		
		String packageName = "analytics.livestream.generated";
	    JAXBContext jc = JAXBContext.newInstance( packageName );
	    Unmarshaller u = jc.createUnmarshaller();
	    Rss sd = (Rss)u.unmarshal( url );
	   
	    return sd;
		
	}
}




