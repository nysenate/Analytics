package gov.nysenate.analytics.models;

public class Source implements Comparable<Source>
{
    public int pageviews;
    public int bounces;
    public double timeOnPage;
    public String source;

    public Source(String source, int pageviews, int bounces, double time)
    {
        this.source = source;
        this.pageviews = pageviews;
        this.bounces = bounces;
        this.timeOnPage = time;
    }

    public int compareTo(Source that)
    {
        return that.pageviews - this.pageviews;
    }
}
