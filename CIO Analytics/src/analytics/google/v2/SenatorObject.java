package analytics.google.v2;
import java.lang.reflect.Method;
import java.util.*;

public class SenatorObject {
	
	private String fName;
	private String lName;
	private String urlPath;
	private Collection<SourceObject> source;
	
	public SenatorObject() {
		source = new ArrayList<SourceObject>();
	}
	
	public SenatorObject(String fName, String lName, String urlPath) {
		source = new ArrayList<SourceObject>();
		this.fName = fName;
		this.lName = lName;
		this.urlPath = urlPath;
	}
	
	public SenatorObject(String fName, String lName, String urlPath, Collection<SourceObject> source) {
		this.fName = fName;
		this.lName = lName;
		this.urlPath = urlPath;
		this.source = source;
	}
	
	public String getFName() {
		return fName;
	}
	public String getLName() {
		return lName;
	}
	public String getUrlPath() {
		return urlPath;
	}
	public Collection<SourceObject> getSource() {
		return source;
	}
	
	public void setFName(String fName) {
		this.fName = fName;
	}
	public void setLName(String lName) {
		this.lName = lName;
	}
	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}
	public void setSource(Collection<SourceObject> source) {
		this.source = source;
	}
	
	public void addSource(SourceObject s) {
		source.add(s);
	}
	
	@SuppressWarnings("unused")
	private int getTotal(String method) {		
		Method m = null;
		try {
			m = (SourceObject.class).getDeclaredMethod(method);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		}
		
		int count = 0;
		
		if(m != null) {
			for(SourceObject so:source) {
				
				try {
					count += (Integer)m.invoke(so);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}
		
		return count;
	}
	
	public int getTotalBounces() {
		int i = 0;
		for(SourceObject so:source) {
			i+=so.getBounces();
		}
		return i;
	}
	
	public int getTotalViews() {
		int i = 0;
		for(SourceObject so:source) {
			i+=so.getPageviews();
		}
		return i;
	}
	
	public double getTotalTime() {
		int i = 0;
		for(SourceObject so:source) {
			i+=so.getTime();
		}
		return i;
	}
	
}
