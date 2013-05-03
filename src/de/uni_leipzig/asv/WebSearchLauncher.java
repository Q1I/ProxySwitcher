package de.uni_leipzig.asv;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.uni_leipzig.asv.web.search.EToolsSearchRequest;
import de.uni_leipzig.asv.web.search.GoogleSearchRequest;
import de.uni_leipzig.asv.web.search.ProxyManager;
import de.uni_leipzig.asv.web.search.SearchResponse;
public final class WebSearchLauncher {
	public enum WebSearchEngine {
		GOOGLE, ETOOLS
	}
	public static void launch(WebSearchEngine engine, File in, File out, boolean writeQuery, boolean writeLink, boolean writeTitle, boolean writeSnippet, String proxy) throws Exception {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(in), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
			//writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output, String.format("%05d", i) + "." + "csv")), "UTF-8"));
			if(proxy == null)
				WebSearchLauncher.launch(engine, reader, writer, writeQuery, writeLink, writeTitle, writeSnippet);
			else
				WebSearchLauncher.launch(engine, reader, writer, writeQuery, writeLink, writeTitle, writeSnippet,proxy);
		} finally {
			if (reader != null)
				reader.close();
			if (writer != null)
				writer.close();
		}
	}
	public static void launch(WebSearchEngine engine, BufferedReader in, BufferedWriter out, boolean writeQuery, boolean writeLink, boolean writeTitle, boolean writeSnippet) throws Exception {
		String query;
		while ((query = in.readLine()) != null) {
			WebSearchLauncher.writeSearchResponse(WebSearchLauncher.request(engine, query), out, writeQuery, writeLink, writeTitle, writeSnippet);
		}
	}
	public static SearchResponse request(WebSearchEngine engine, String query) throws Exception {
		SearchResponse response;
		switch (engine) {
			case GOOGLE :
				response = ProxyManager.getInstance().requestGoogle(query, GoogleSearchRequest.GENERAL_RESULT_LIMIT);
				break;
			case ETOOLS :
				response = ProxyManager.getInstance().requestETools(query, EToolsSearchRequest.GENERAL_RESULT_LIMIT);
				break;
			default :
				throw new IllegalArgumentException();
		}
		return response;
	}
	
	// local proxy ----------------
	public static void launch(WebSearchEngine engine, BufferedReader in, BufferedWriter out, boolean writeQuery, boolean writeLink, boolean writeTitle, boolean writeSnippet, String proxy) throws Exception {
		System.out.println("Local Proxy");
		String query;
		while ((query = in.readLine()) != null) {
			WebSearchLauncher.writeSearchResponse(WebSearchLauncher.request(engine, query, proxy), out, writeQuery, writeLink, writeTitle, writeSnippet);
		}
	}
	
	/**
	 * Request with proxy
	 * @param engine
	 * @param query
	 * @param proxy
	 * @return response
	 * @throws Exception
	 */
	public static SearchResponse request(WebSearchEngine engine, String query, String proxy) throws Exception {
		SearchResponse response;
		switch (engine) {
			case GOOGLE :
				response = ProxyManager.getInstance(proxy).requestGoogle(query, GoogleSearchRequest.GENERAL_RESULT_LIMIT);
				break;
			case ETOOLS :
				response = ProxyManager.getInstance(proxy).requestETools(query, EToolsSearchRequest.GENERAL_RESULT_LIMIT);
				break;
			default :
				throw new IllegalArgumentException();
		}
		return response;
	}
	public static void writeSearchResponse(SearchResponse response, BufferedWriter out, boolean writeQuery, boolean writeLink, boolean writeTitle, boolean writeSnippet) throws Exception {
		List<SearchResponse.Result> results = response.getResults();
		System.out.println(">>>> WRITE " + results.size() + " RESULTS FOR \"" + response.getRequestedQuery() + "\" TO FILE");
		for (SearchResponse.Result result : results) {
			List<String> fields = new ArrayList<String>();
			if (writeQuery)
				fields.add("\"" + response.getRequestedQuery() + "\"");
			if (writeLink)
				fields.add(result.getLink());
			if (writeTitle)
				fields.add("\"" + result.getTitle() + "\"");
			if (writeSnippet)
				fields.add("\"" + result.getSnippet() + "\"");
			WebSearchLauncher.writeCSVLine(fields, out);
		}
	}
	public static void writeCSVLine(List<String> fields, BufferedWriter out) throws Exception {
		fields = new ArrayList<String>(fields);
		while (!fields.isEmpty()) {
			String field = fields.remove(0);
			out.write(field);
			if (!fields.isEmpty())
				out.write(";");
			else
				out.newLine();
		}
	}
	public static void main(String[] args) {
		try {
			System.out.println("size: "+args.length);
			WebSearchEngine engine;
			File in;
			File out;
			boolean writeQuery;
			boolean writeLink;
			boolean writeTitle;
			boolean writeSnippet;
			String proxy = null;
			{
				Map<String, WebSearchEngine> map = new HashMap<String, WebSearchEngine>();
				map.put("google", WebSearchEngine.GOOGLE);
				map.put("etools", WebSearchEngine.ETOOLS);
				engine = map.get(args[0].toLowerCase());
				if (engine == null)
					throw new IllegalArgumentException("web search engine must me \"google\" or \"etools\"");
			}
			{
				in = new File(args[1]);
				if (!in.isFile())
					throw new IllegalArgumentException("input must be a file");
			}
			{
				out = new File(args[2]);
			}
			{
				writeQuery = Boolean.parseBoolean(args[3]);
			}
			{
				writeLink = Boolean.parseBoolean(args[4]);
			}
			{
				writeTitle = Boolean.parseBoolean(args[5]);
			}
			{
				writeSnippet = Boolean.parseBoolean(args[6]);
			}
			if(args.length>7)
			{
				proxy = args[7];
			}
			WebSearchLauncher.launch(engine, in, out, writeQuery, writeLink, writeTitle, writeSnippet,proxy);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
