package gov.nysenate.analytics.models;

public class NYSenate
{
    public String fName;
    public String lName;
    public String facebookURL;
    public String flickrURL;
    public String twitterURL;
    public String youtubeURL;
    public String nysenateURL;
    public String friends;

    public NYSenate()
    {
    }

    public String toString()
    {
        return ((fName != null) ? fName : "")
                + ((lName != null) ? "," + lName : ",")
                + ((twitterURL != null) ? "\n---->" + twitterURL : "")
                + ((youtubeURL != null) ? "\n---->" + youtubeURL : "")
                + ((facebookURL != null) ? "\n---->" + facebookURL + ",Fans " + ((friends != null) ? friends : "") : "")
                + ((flickrURL != null) ? "\n---->" + flickrURL : "");
    }

    public String toAnalyticsString()
    {
        return ((fName != null) ? fName : "")
                + ((lName != null) ? "," + lName : ",")
                + ((twitterURL != null) ? "," + twitterURL : ",")
                + ((youtubeURL != null) ? "," + youtubeURL : ",")
                + ((facebookURL != null) ? "," + facebookURL : ",")
                + ((flickrURL != null) ? "," + flickrURL : ",");
    }
}
