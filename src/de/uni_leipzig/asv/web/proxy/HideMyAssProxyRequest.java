package de.uni_leipzig.asv.web.proxy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.uni_leipzig.asv.web.MultiRequest;
public final class HideMyAssProxyRequest extends MultiRequest<List<Proxy>> {
	public static final int PAGE_PROXY_LIMIT = 50;
	private int proxyCount;
	public HideMyAssProxyRequest(int proxyCount) {
		if (proxyCount < 1)
			throw new IllegalArgumentException();
		this.proxyCount = proxyCount;
	}
	protected void start(Context context) throws IOException {
		HttpGet request = new HttpGet();
		int page = 1;
		while (true) {
			if (context.isCompleted())
				break;
			request.setURI(HideMyAssProxyRequest.buildURI(page));
			context.submit(request);
			page++;
		}
	}
	private static final URI buildURI(int page) throws IOException {
		final String uri = "http://www.hidemyass.com/proxy-list/{0}";
		try {
			return new URI(MessageFormat.format(uri, String.valueOf(page)));
		} catch (URISyntaxException e) {
			throw new IOException();
		}
	}
	protected List<Proxy> handleResponse(HttpResponse response, List<Proxy> value, Context context) throws IOException {
		if (response.getStatusLine().getStatusCode() != 200)
			throw new IOException();
		if (value == null) {
			value = new ArrayList<Proxy>();
		}
		Document doc;
		{
			InputStream in = response.getEntity().getContent();
			doc = Jsoup.parse(response.getEntity().getContent(), null, "");
			in.close();
		}
		for (Element element : doc.select("table#listtable > tbody > tr")) {
			try {
				String host = HideMyAssProxyRequest.extractHost(element.child(1));
				int port = HideMyAssProxyRequest.extractPort(element.child(2));
				Proxy.Type type = HideMyAssProxyRequest.extractType(element.child(6));
				value.add(new Proxy(host, port, type));
			} catch (Exception e) {}
			if (value.size() >= this.proxyCount) {
				context.complete();
				break;
			}
		}
		return value;
	}
	public static String extractHost(Element e) {
		Set<String> classNames = new HashSet<String>();
		{
			final String regex = "[.](-?[_a-zA-Z]+[_a-zA-Z0-9-]*)\\s*[{]display\\s*[:]\\s*none[}]";
			final String input = e.child(0).child(0).html();
			Matcher matcher = Pattern.compile(regex).matcher(input);
			while (matcher.find())
				classNames.add(matcher.group(1));
		}
		Set<Element> elements = new HashSet<Element>();
		for (Element element : e.children().get(0).children())
			if (element.attr("style").contains("none") || classNames.contains(element.className()))
				elements.add(element);
		for (Element element : elements)
			element.remove();
		return e.text();
	}
	public static int extractPort(Element e) {
		return Integer.parseInt(e.text());
	}
	public static Proxy.Type extractType(Element e) {
		return Proxy.Type.HTTP;
		/*
		 * String type = e.text();
		 * if (type.equalsIgnoreCase("http"))
		 * return Proxy.Type.HTTP;
		 * else if (type.equalsIgnoreCase("https"))
		 * return Proxy.Type.HTTPS;
		 * else if (type.equalsIgnoreCase("socks4/5"))
		 * return Proxy.Type.SOCKS;
		 * else
		 * throw new IllegalArgumentException();
		 */
	}
}
