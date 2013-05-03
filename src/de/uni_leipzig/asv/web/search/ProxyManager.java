package de.uni_leipzig.asv.web.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import de.uni_leipzig.asv.WebSearchLauncher;
import de.uni_leipzig.asv.web.ProxySwitcher;
import de.uni_leipzig.asv.web.proxy.HideMyAssProxyRequest;
import de.uni_leipzig.asv.web.proxy.Proxy;
import de.uni_leipzig.asv.web.proxy.Proxy.Type;
import de.uni_leipzig.asv.web.search.EToolsSearchRequest.EToolsSearchResponse;
import de.uni_leipzig.asv.web.search.GoogleSearchRequest.GoogleSearchResponse;

public final class ProxyManager {
	private static ProxyManager instance = null;

	// location of proxy list file
	private static boolean useLocalProxyFile = false;
	private static String proxyFile = null;

	public static void setLocalProxyFile(String file) {
		proxyFile = file;
		useLocalProxyFile = true;
	}

	public static final ProxyManager getInstance(String file) {
		if (ProxyManager.instance == null)
			ProxyManager.instance = new ProxyManager();
		setLocalProxyFile(file);
		return ProxyManager.instance;
	}
	
	public static final ProxyManager getInstance() {
		if (ProxyManager.instance == null)
			ProxyManager.instance = new ProxyManager();
		return ProxyManager.instance;
	}

	private ProxySwitcher<GoogleSearchResponse> googleProxySwitcher = new ProxySwitcherImpl<GoogleSearchResponse>();
	private ProxySwitcher<EToolsSearchResponse> etoolsProxySwitcher = new ProxySwitcherImpl<EToolsSearchResponse>();

	private ProxyManager() {
	}

	/**
	 * Sendet eine Anfrage an die Suchmaschine Google und wandelt die
	 * gelieferten Suchergebnisse in die standardm��ige Darstellung um.
	 * 
	 * @param query
	 *            die Anfrage an die Suchmaschine
	 * @param resultCount
	 *            die angeforderte Anzahl der Suchergebnisse
	 * @return die geparsten Sucherbnisse
	 * @throws IOException
	 *             falls die Anfrage nicht versendet werden konnte oder eine
	 *             Fehler beim Parsen der Suchergebnisse auftrat
	 */
	public GoogleSearchResponse requestGoogle(String query, int resultCount)
			throws IOException {
		GoogleSearchRequest request = new GoogleSearchRequest(query,
				resultCount);
		request.setProxySwitcher(this.googleProxySwitcher);
		return request.submit();
	}

	/**
	 * Sendet eine Anfrage an die Suchmaschine ETools und wandelt die
	 * gelieferten Suchergebnisse in die standardm��ige Darstellung um.
	 * 
	 * @param query
	 *            die Anfrage an die Suchmaschine
	 * @param resultCount
	 *            die angeforderte Anzahl der Suchergebnisse
	 * @return die geparsten Sucherbnisse
	 * @throws IOException
	 *             falls die Anfrage nicht versendet werden konnte oder eine
	 *             Fehler beim Parsen der Suchergebnisse auftrat
	 */
	public EToolsSearchResponse requestETools(String query, int resultCount)
			throws IOException {
		EToolsSearchRequest request = new EToolsSearchRequest(query,
				resultCount);
		request.setProxySwitcher(this.etoolsProxySwitcher);
		return request.submit();
	}

	private static final class ProxySwitcherImpl<T> extends ProxySwitcher<T> {
		private final List<Proxy> proxies = new ArrayList<Proxy>();

		public ProxySwitcherImpl() {
			super();
		}

		boolean first = true;

		protected Proxy getNextProxy() {
			if (this.proxies.isEmpty()) {
				System.out.println(">>> GET NEW PROXIES: ");
				try {
					if (useLocalProxyFile) { // Local Proxy List
						if (proxyFile == null)
							return null;
						File in = new File(proxyFile);
						if (!in.isFile())
							throw new IllegalArgumentException("input must be a file");
						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new InputStreamReader(
									new FileInputStream(in), "UTF-8"));

						// Parse proxies
						String line = null;
						String host = null;
						int port;
						Type type = null;
						while ((line = reader.readLine()) != null) {
							host = line.substring(0, line.indexOf(':') - 1);
							port = Integer.parseInt(line.substring(line
									.indexOf(':') + 1));
							type = Type.HTTP;
							Proxy proxy = new Proxy(host, port, type);
							System.out.println("host: "+host+" port: "+port);
							proxies.add(proxy);
						}
						proxyFile = null;

						} finally {
							if (reader != null)
								reader.close();
						}
					} else { // Get Proxies from HideMyAss
						HideMyAssProxyRequest request = new HideMyAssProxyRequest(
								this.first ? 1000
										: HideMyAssProxyRequest.PAGE_PROXY_LIMIT);
						this.first = false;
						this.proxies.addAll(request.submit());
					}
				} catch (IOException e) {
					System.out.println(">>> EXCEPTION : " + e.getMessage());
				}
			}
			if (this.proxies.isEmpty())
				return null;
			Proxy proxy = this.proxies.remove(0);
			System.out.println(">>> USE PROXY : " + proxy);
			return proxy;
		}

		protected void onRequestSucceed(Proxy proxy, int counter) {
			System.out.println(">>> PROXY SUCCEED : " + counter);
		}

		protected void onRequestFailed(Proxy proxy, int counter, String message) {
			System.out.println(">>> PROXY FAILED : " + message);
		}
	}
}
