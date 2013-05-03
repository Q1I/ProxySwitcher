package de.uni_leipzig.asv.web;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
/**
 * Eine gekapselte Menge von aufeinanderfolgenden Anfragen.
 * 
 * @author Sergej Sintschilin
 * @author Quan Nguyen
 * 
 * @param <R>
 *            ein der durch die Antwort der Anfrage bestimmter Rückgabewert
 */
public abstract class MultiRequest<R> {
	/**
	 * Standardmäßiger Konstruktor ohne ProxySwitcher. Alle Anfragen werden
	 * direkt an den HttpClient weitergeleitet.
	 */
	public MultiRequest() {}
	private ProxySwitcher<R> ps = null;
	/**
	 * Setzt einen ProxySwitcher fest, durch den alle Anfragen an den HttpClient
	 * weitergeleitet werden.
	 * 
	 * @param ps
	 *            der ProxySwitcher
	 */
	public final void setProxySwitcher(ProxySwitcher<R> ps) {
		this.ps = ps;
	}
	/**
	 * Sendet die gekapselte Menge von aufeinanderfolgenden Anfragen an einen
	 * beliebigen Client und wandelt die gelieferten Ergebnisse in eine
	 * standardmäßige Darstellung um.
	 * 
	 * @return die geparsten Ergebnisse
	 * @throws IOException
	 *             falls eine Anfrage nicht versendet werden konnte oder ein
	 *             Fehler beim Parsen der Suchergebnisse auftrat
	 */
	public final R submit() throws IOException {
		ContextImpl<R> context = new ContextImpl<R>(this);
		this.start(context);
		context.complete();
		return context.getResponse();
	}
	/**
	 * Startet das Versenden der Anfragen.
	 * 
	 * @param context
	 *            die Kontrolleinheit zwischen den Anfragen
	 * @throws IOException
	 *             falls eine Anfrage nicht versendet werden konnte oder ein
	 *             Fehler beim Parsen der Suchergebnisse auftrat
	 */
	protected abstract void start(Context context) throws IOException;
	/**
	 * Die Kontrolleinheit zwischen den Anfragen.
	 * 
	 * @author Sergej Sintschilin
	 * @author Quan Nguyen
	 */
	protected static interface Context {
		/**
		 * Sendet die vorbereitete Anfrage an den HttpClient.
		 * 
		 * @param request
		 *            vorbereitete Anfrage
		 * @throws IOException
		 *             falls die Anfrage nicht versendet werden konnte oder ein
		 *             Fehler beim Parsen der Suchergebnisse auftrat
		 */
		public void submit(HttpRequestBase request) throws IOException;
		/**
		 * Setzt die Arbeit als abgeschlossen.
		 */
		public void complete();
		/**
		 * Überprüft, ob die Arbeit abgeschlossen ist.
		 * 
		 * @return <code>true</code>, falls die Arbeit abgeschlossen ist, sonst
		 *         <code>false</code>
		 */
		public boolean isCompleted();
		/**
		 * Gibt den Wert eines Parametes als String zurück.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @return der Wert des Parameters
		 */
		public String getParamAsString(String key);
		/**
		 * Gibt den Wert eines Parametes als Integer zurück.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @return der Wert des Parameters
		 */
		public Integer getParamAsInteger(String key);
		/**
		 * Gibt den Wert eines Parametes als Boolean zurück.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @return der Wert des Parameters
		 */
		public Boolean getParamAsBoolean(String key);
		/**
		 * Gibt den Wert eines Parametes als String zurück. Falls der Parameter
		 * nicht existiert oder sein Wert kein String ist, wird der
		 * standardmäßige Wert zurückgegeben.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @param defaultValue
		 *            standartmäßiger Rückgabewert
		 * @return der Wert des Parameters
		 */
		public String getParamAsString(String key, String defaultValue);
		/**
		 * Gibt den Wert eines Parametes als Integer zurück. Falls der Parameter
		 * nicht existiert oder sein Wert kein Integer ist, wird der
		 * standardmäßige Wert zurückgegeben.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @param defaultValue
		 *            standartmäßiger Rückgabewert
		 * @return der Wert des Parameters
		 */
		public Integer getParamAsInteger(String key, Integer defaultValue);
		/**
		 * Gibt den Wert eines Parametes als Boolean zurück. Falls der Parameter
		 * nicht existiert oder sein Wert kein Boolean ist, wird der
		 * standardmäßige Wert zurückgegeben.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @param defaultValue
		 *            standartmäßiger Rückgabewert
		 * @return der Wert des Parameters
		 */
		public Boolean getParamAsBoolean(String key, Boolean defaultValue);
		/**
		 * Setzt den Wert eines Parameters als String.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @param value
		 */
		public void setParamAsString(String key, String value);
		/**
		 * Setzt den Wert eines Parameters als Integer.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @param value
		 *            der Wert des Parameters
		 */
		public void setParamAsInteger(String key, Integer value);
		/**
		 * Setzt den Wert eines Parameters als Boolean.
		 * 
		 * @param key
		 *            der Name des Parameters
		 * @param value
		 *            der Wert des Parameters
		 */
		public void setParamAsBoolean(String key, Boolean value);
		/**
		 * Löscht einen Parameter.
		 * 
		 * @param key
		 *            der Name des Parameters
		 */
		public void removeParam(String key);
	}
	private static final class ContextImpl<R> implements Context, ResponseHandler<R> {
		private MultiRequest<R> root;
		private HttpClient client;
		private R response = null;
		public ContextImpl(MultiRequest<R> root) {
			this.root = root;
			final HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
			this.client = new DefaultHttpClient(httpParams);
		}
		public void submit(HttpRequestBase request) throws IOException {
			if (this.client != null)
				this.response = this.root.request(this.client, this, request);
		}
		public void complete() {
			if (this.client != null) {
				this.client.getConnectionManager().shutdown();
				this.client = null;
			}
		}
		public boolean isCompleted() {
			return this.client == null;
		}
		public R getResponse() {
			return this.response;
		}
		public R handleResponse(HttpResponse response) throws IOException {
			return this.root.handleResponse(response, this.response, this);
		}
		private Map<String, Object> params = new HashMap<String, Object>();
		private Object getParamAsObject(String key) {
			Object value = this.params.get(key);
			if (value == null)
				throw new NoSuchElementException();
			return value;
		}
		public String getParamAsString(String key) {
			Object value = this.getParamAsObject(key);
			if (value instanceof String)
				return (String) value;
			else
				throw new NoSuchElementException();
		}
		public Integer getParamAsInteger(String key) {
			Object value = this.getParamAsObject(key);
			if (value instanceof Integer)
				return (Integer) value;
			else
				throw new NoSuchElementException();
		}
		public Boolean getParamAsBoolean(String key) {
			Object value = this.getParamAsObject(key);
			if (value instanceof Boolean)
				return (Boolean) value;
			else
				throw new NoSuchElementException();
		}
		public String getParamAsString(String key, String defaultValue) {
			try {
				return this.getParamAsString(key);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		public Integer getParamAsInteger(String key, Integer defaultValue) {
			try {
				return this.getParamAsInteger(key);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		public Boolean getParamAsBoolean(String key, Boolean defaultValue) {
			try {
				return this.getParamAsBoolean(key);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		private void setParamAsObject(String key, Object value) {
			if (value == null)
				throw new IllegalArgumentException();
			this.params.put(key, value);
		}
		public void setParamAsString(String key, String value) {
			this.setParamAsObject(key, value);
		}
		public void setParamAsInteger(String key, Integer value) {
			this.setParamAsObject(key, value);
		}
		public void setParamAsBoolean(String key, Boolean value) {
			this.setParamAsObject(key, value);
		}
		public void removeParam(String key) {
			this.params.remove(key);
		}
	}
	private final R request(HttpClient client, ResponseHandler<R> responseHandler, HttpRequestBase request) throws IOException {
		if (this.ps != null) {
			return this.ps.request(client, responseHandler, request);
		} else {
			try {
				return client.execute(request, responseHandler);
			} finally {
				request.reset();
			}
		}
	}
	/**
	 * Parst das Ergebnis, das direkt vom Client geliert wurde, und gibt das
	 * geparste Ergebnis zurück.
	 * 
	 * @param response
	 *            das ungeparste Ergebnis
	 * @param value
	 *            das vorher geparste Ergebnis
	 * @param context
	 *            die Kontrolleinheit zwischen den Anfragen
	 * @return das geparste Ergebnis
	 * @throws IOException
	 *             falls ein Fehler beim Parsen der Suchergebnisse auftrat
	 */
	protected abstract R handleResponse(HttpResponse response, R value, Context context) throws IOException;
	public static Map<String, String> extractCookies(String header) {
		Map<String, String> cookies = new HashMap<String, String>();
		for (String cookie : header.split(";")) {
			cookie = cookie.trim();
			String[] pairKeyValue = cookie.split("=");
			if (pairKeyValue.length >= 1) {
				String key = pairKeyValue[0];
				if (pairKeyValue.length >= 2)
					cookies.put(key, pairKeyValue[1]);
				else
					cookies.put(key, String.valueOf(true));
			}
		}
		return cookies;
	}
}