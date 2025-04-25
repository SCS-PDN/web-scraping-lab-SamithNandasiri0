import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class NewsItem{	
	String headline;
	String date;
	String Author;

public NewsItem(String headline, String date,String Author) {
	this.headline=headline;
	this.date=date;
	this.Author=Author;
	
	}
}

public class WebScraper{
	public static void main(String[] args) {
		String url="https://www.bbc.com";
		Document doc=Jsoup.connect(url).get();
		System.out.println("Title: "+ doc.title());
		for(int i=1;i<6;i++) {
			Elements headings = doc.select("h"+i);
			for(Element h:headings) {
				System.out.println("H"+i+": "+ h.text());
			}
		}
		
		Elements links= doc.select("a[href]");
		for(Element link : links) {
			System.out.println("Link: "+ link.attr("abs:href"));
		}
		
		List<NewsItem> newsList = new ArrayList<>();
		Elements articles=doc.select("div[data-entityid^='container-top-stories#'] a");
		for(Element article: articles) {
			String headline=article.text();
			String date="N/A";
			String author="N/A";
			newsList.add(new NewsItem(headline, date, author));
		}
		
		for (NewsItem news : newsList) {
            System.out.println("Headline: " + news.headline + ", Date: " + news.date + ", Author: " + news.author);
        }
	}
}