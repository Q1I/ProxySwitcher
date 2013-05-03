package de.uni_leipzig.asv.web.search;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.uni_leipzig.asv.web.MultiRequest;
import de.uni_leipzig.asv.web.search.SearchResponse.Result;
/**
 * Eine Anfrage an die Suchmaschine Google.
 * 
 * @author Sergej Sintschilin
 * @author Quan Nguyen
 */
public final class GoogleSearchRequest extends MultiRequest<GoogleSearchRequest.GoogleSearchResponse> {
	public static final int PAGE_RESULT_LIMIT = 100;
	public static final int GENERAL_RESULT_LIMIT = 1000;
	/**
	 * Die Antwort der Suchmaschine Google. Erweitert die standardmäßige Antwort
	 * um die Statistik der von Google gelieferten Ergebnisse.
	 * 
	 * @author Sergej Sintschilin
	 * @author Quan Nguyen
	 */
	public static class GoogleSearchResponse extends SearchResponse {
		protected Integer totalResultCount = null;
		protected GoogleSearchResponse() {
			super();
			this.requestedWebSearchEngine = "Google";
		}
		/**
		 * Gibt die ungefähre Anzahl der gefundenen Ergebnisse, die von Google
		 * bei einer Suche geliefert wird.
		 * 
		 * @return die ungefähre Anzahl der gefundenen Ergebnisse
		 */
		public Integer getTotalResultCount() {
			return this.totalResultCount;
		}
	}
	private String query;
	private int resultCount;
	/**
	 * Bereitet eine Anfrage für die Suchmaschine Google vor.
	 * 
	 * @param query
	 *            die Anfrage an die Suchmaschine
	 * @param resultCount
	 *            die angeforderte Anzahl der Suchergebnisse
	 */
	public GoogleSearchRequest(String query, int resultCount) {
		if (query == null || query.isEmpty())
			throw new IllegalArgumentException();
		this.query = query;
		if (resultCount < 1 || resultCount > GoogleSearchRequest.GENERAL_RESULT_LIMIT)
			throw new IllegalArgumentException();
		this.resultCount = resultCount;
	}
	protected void start(Context context) throws IOException {
		HttpGet request = new HttpGet();
		// request.setHeader("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
		int start = 0;
		int num = GoogleSearchRequest.PAGE_RESULT_LIMIT;
		while (true) {
			if (context.isCompleted())
				break;
			if (start >= GoogleSearchRequest.GENERAL_RESULT_LIMIT)
				break;
			// int num = Math.min(this.resultCount - start, GoogleSearchRequest.PAGE_RESULT_LIMIT);
			request.setURI(GoogleSearchRequest.buildURI(this.query, start, num));
			context.submit(request);
			start += num;
		}
	}
	private static final URI buildURI(String query, int start, int num) throws IOException {
		final String uri = "https://www.google.com/search?q={0}&start={1}&num={2}";
		try {
			return new URI(MessageFormat.format(uri, URLEncoder.encode(query, "UTF-8"), String.valueOf(start), String.valueOf(num)));
		} catch (Exception e) {
			throw new IOException();
		}
	}
	protected GoogleSearchResponse handleResponse(HttpResponse response, GoogleSearchResponse value, Context context) throws IOException {
		if (response.getStatusLine().getStatusCode() != 200)
			throw new IOException("status is not OK");
		Document doc;
		{
			InputStream in = response.getEntity().getContent();
			doc = Jsoup.parse(response.getEntity().getContent(), "CP1252", "");
			in.close();
		}
		if (value == null) {
			value = new GoogleSearchResponse();
			value.requestedQuery = this.query;
			value.requestedResultCount = this.resultCount;
			try {
				value.totalResultCount = Integer.valueOf(doc.select("div#resultStats").first().text().replaceAll("[^\\d]", ""));
			} catch (Exception e) {}
		}
		for (Element element : doc.select("li.g")) {
			try {
				Result result = new Result();
				result.title = element.select("h3.r").first().text();
				Matcher matcher = Pattern.compile("q=([^&]*)").matcher(element.select("a[href^=/url]").first().attr("href"));
				matcher.find();
				result.link = URLDecoder.decode(matcher.group(1), "UTF-8");
				result.snippet = element.select("span.st").first().text();
				value.results.add(result);
			} catch (Exception e) {}
			if (value.results.size() >= this.resultCount) {
				context.complete();
				break;
			}
		}
		if (!doc.select("p#ofr").isEmpty())
			context.complete();
		else {
			Elements select = doc.select("table#nav td:not(.b)");
			if (select.isEmpty())
				context.complete();
			else if (select.last().select("a").isEmpty())
				context.complete();
		}
		return value;
	}
}
