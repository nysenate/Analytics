package main;

public class Main {
	public static void main(String[] args) throws Exception {
//		new analytics.google.v2.GAConnect();
		System.out.println("LiveStream");
		new analytics.livestream.parser.LiveStreamParser();
		System.out.println("NYSenate stuff");
		new analytics.social.NYSenateAnalytics();
	}
}
