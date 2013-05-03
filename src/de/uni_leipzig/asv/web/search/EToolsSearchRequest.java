package de.uni_leipzig.asv.web.search;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.text.MessageFormat;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.uni_leipzig.asv.web.MultiRequest;
import de.uni_leipzig.asv.web.search.SearchResponse.Result;
/**
 * Eine Anfrage an die Suchmaschine ETools.
 * 
 * @author Sergej Sintschilin
 * @author Quan Nguyen
 */
public final class EToolsSearchRequest extends MultiRequest<EToolsSearchRequest.EToolsSearchResponse> {
	/**
	 * Die Antwort der Suchmaschine ETools.
	 * 
	 * @author Sergej Sintschilin
	 * @author Quan Nguyen
	 */
	public static class EToolsSearchResponse extends SearchResponse {
		protected EToolsSearchResponse() {
			super();
			this.requestedWebSearchEngine = "Etools";
		}
	}
	public static final int PAGE_RESULT_LIMIT = 40;
	public static final int GENERAL_RESULT_LIMIT = 400;
	private String query;
	private int resultCount;
	/**
	 * Bereitet eine Anfrage für die Suchmaschine ETools vor.
	 * 
	 * @param query
	 *            die Anfrage an die Suchmaschine
	 * @param resultCount
	 *            die angeforderte Anzahl der Suchergebnisse
	 */
	public EToolsSearchRequest(String query, int resultCount) {
		if (query == null || query.isEmpty())
			throw new IllegalArgumentException();
		this.query = query;
		if (resultCount < 1 || resultCount > EToolsSearchRequest.GENERAL_RESULT_LIMIT)
			throw new IllegalArgumentException();
		this.resultCount = resultCount;
	}
	protected void start(Context context) throws IOException {
		HttpGet request = new HttpGet();
		int page = 1;
		int count = 0;
		while (true) {
			if (context.isCompleted())
				break;
			if (count >= EToolsSearchRequest.GENERAL_RESULT_LIMIT)
				break;
			if (page == 1)
				request.setURI(EToolsSearchRequest.buildURI(this.query, "web", "all", 40, 40));
			else
				request.setURI(EToolsSearchRequest.buildURI(context.getParamAsString("jsessionid", ""), page));
			context.submit(request);
			page++;
			count += EToolsSearchRequest.PAGE_RESULT_LIMIT;
		}
	}
	private static final URI buildURI(String query, String country, String language, int dataSourceResults, int pageResults) throws IOException {
		final String uri = "http://www.etools.ch/searchAdvancedSubmit.do?query={0}&country={1}&language={2}&dataSourceResults={3}&pageResults={4}";
		try {
			return new URI(MessageFormat.format(uri, URLEncoder.encode(query, "UTF-8"), country, language, String.valueOf(dataSourceResults), String.valueOf(pageResults)));
		} catch (Exception e) {
			throw new IOException();
		}
	}
	private static final URI buildURI(String jsessionid, int page) throws IOException {
		final String uri = "http://www.etools.ch/searchAdvanced.do;jsessionid={0}?page={1}";
		try {
			return new URI(MessageFormat.format(uri, jsessionid, String.valueOf(page)));
		} catch (Exception e) {
			throw new IOException();
		}
	}
	protected EToolsSearchResponse handleResponse(HttpResponse response, EToolsSearchResponse value, Context context) throws IOException {
		if (response.getStatusLine().getStatusCode() != 200)
			throw new IOException("status is not OK");
		Document doc;
		{
			InputStream in = response.getEntity().getContent();
			doc = Jsoup.parse(response.getEntity().getContent(), null, "");
			in.close();
		}
		if (doc.title().equalsIgnoreCase("Access Banned"))
			throw new IOException("access banned");
		if (doc.title().equalsIgnoreCase("Access Denied"))
			throw new IOException("access denied");
		if (value == null) {
			value = new EToolsSearchResponse();
			value.requestedQuery = this.query;
			value.requestedResultCount = this.resultCount;
			try {
				context.setParamAsString("jsessionid", MultiRequest.extractCookies(response.getFirstHeader("Set-Cookie").getValue()).get("JSESSIONID"));
			} catch (Exception e) {}
		}
		for (Element element : doc.getElementsByClass("record")) {
			try {
				Result result = new Result();
				result.title = element.children().get(0).text();
				result.link = element.children().get(0).attr("href");
				result.snippet = element.children().get(1).text();
				value.results.add(result);
			} catch (Exception e) {}
			if (value.results.size() >= this.resultCount) {
				context.complete();
				break;
			}
		}
		return value;
	}
}
