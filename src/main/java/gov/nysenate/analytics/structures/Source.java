package gov.nysenate.analytics.structures;

public class Source implements Comparable<Source>{
	public int pageviews;
	public int bounces;
	public double time;
	public String source;

	public Source(String source, int pageviews, int bounces, double time) {
		this.source = source;
		this.pageviews = pageviews;
		this.bounces = bounces;
		this.time = time;
	}

	public int compareTo(Source that) {
		return that.pageviews - this.pageviews;
	}
}
