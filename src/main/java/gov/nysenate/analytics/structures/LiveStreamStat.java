package gov.nysenate.analytics.structures;

public class LiveStreamStat {
	public String key;
	public String title;
	public String link;
	public int weekly;
	public int total;

	public LiveStreamStat() {}

	public LiveStreamStat (String title, String link, int weekly, int total, String key) {
		this.title = title;
		this.link = link;
		this.weekly = weekly;
		this.total = total;
		this.key = key;
	}
}
