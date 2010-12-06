package analytics.livestream.parser;

public class LiveStreamStat {
	private String key;
	
	private String title;
	
	private String link;
	
	private int weekly;
	
	private int total;
	
	public LiveStreamStat() {
		
	}
	
	public LiveStreamStat (String title, String link, int weekly, int total) {
		this.title = title;
		this.link = link;
		this.weekly = weekly;
		this.total = total;
	}
	
	public String getKey() {
		return key;
	}
	public String getTitle() {
		return title;
	}
	public String getLink() {
		return link;
	}
	public int getWeekly() {
		return weekly;
	}
	public int getTotal() {
		return total;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setWeekly(int weekly) {
		this.weekly = weekly;
	}
	public void setTotal(int total) {
		this.total = total;
	}
}
