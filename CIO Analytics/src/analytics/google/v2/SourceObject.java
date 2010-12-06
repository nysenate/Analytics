package analytics.google.v2;

public class SourceObject {
	private int pageviews;
	private int bounces;
	private double time;
	private String source;
	
	public SourceObject() {
		
	}
	
	public SourceObject(String source, int pageviews, int bounces, double time) {
		this.source = source;
		this.pageviews = pageviews;
		this.bounces = bounces;
		this.time = time;
	}
	
	public int getPageviews() {
		return pageviews;
	}
	public int getBounces() {
		return bounces;
	}
	public double getTime() {
		return time;
	}
	public String getSource() {
		return source;
	}
	
	public void setPageviews(int pageviews) {
		this.pageviews = pageviews;
	}
	public void setBounces(int bounces) {
		this.bounces = bounces;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public void setSource(String source) {
		this.source = source;
	}	
}
