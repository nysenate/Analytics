package analytics.social;

public class NYSenateObject {
	private String fname;
	private String lname;
	private String facebookURL;
	private String flickrURL;
	private String twitterURL;
	private String youtubeURL;
	private String friends;
	public NYSenateObject() {
	}
	public void setFName(String fname) {
		this.fname = fname;
	}
	public void setLName(String lname) {
		this.lname = lname;
	}
	public void setFacebookURL(String facebookURL) {
		this.facebookURL = facebookURL;
	}
	public void setFlickrURL(String flickrURL) {
		this.flickrURL = flickrURL;
	}
	public void setTwitterURL(String twitterURL) {
		this.twitterURL = twitterURL;
	}
	public void setYoutubeURL(String youtubeURL) {
		this.youtubeURL = youtubeURL;
	}
	public void setFriends(String friends) {
		this.friends = friends;
	}
	public String getFName() {
		return fname;
	}
	public String getLName() {
		return lname;
	}
	public String getFacebookURL() {
		return facebookURL;
	}
	public String getFlickrURL() {
		return flickrURL;
	}
	public String getTwitterURL() {
		return twitterURL;
	}
	public String getYoutubeURL() {
		return youtubeURL;
	}
	public String getFriends() {
		return friends;
	}
	public String toString() {
		return ((fname != null) ? fname : "")
			+ ((lname != null) ? "," + lname : ",")
			+ ((twitterURL!=null) ? "\n---->" + twitterURL : "") 
			+ ((youtubeURL!=null) ? "\n---->" + youtubeURL: "")
			+ ((facebookURL!= null) ? "\n---->" + facebookURL + ",Fans " + ((friends != null) ? friends : "") : "")
			+ ((flickrURL!=null) ? "\n---->" + flickrURL : "");
	}
	public String toAnalyticsString() {
		return ((fname != null) ? fname : "")
			+ ((lname != null) ? "," + lname : ",")
			+ ((twitterURL!=null) ? "," + twitterURL : ",") 
			+ ((youtubeURL!=null) ? "," + youtubeURL: ",")
			+ ((facebookURL!= null) ? "," + facebookURL : ",")
			+ ((flickrURL!=null) ? "," + flickrURL : ",");
	}
	public String toFacebookString() {
		return ((fname != null) ? fname : "")
			+ ((lname != null) ? "," + lname : ",")
			+ ((facebookURL!= null) ? "," + facebookURL + "," + ((friends != null) ? friends : "null") : ",,");
			
	}
}

