package de.uni_leipzig.asv.web;
import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import de.uni_leipzig.asv.web.proxy.Proxy;
/**
 * Das Objekt für die Ausführung einer Anfrage unter der Verwendung einer Proxy.
 * 
 * @author Sergej Sintschilin
 * @author Quan Nguyen
 * 
 * @param <R>
 *            ein der durch die Antwort der Anfrage bestimmter Wert
 */
public abstract class ProxySwitcher<R> {
	private Proxy currProxy = null;
	private int currProxyCounter = 0;
	/**
	 * Standardmäßiger Konstruktor.
	 */
	public ProxySwitcher() {}
	/**
	 * Führt eine Anfrage unter der Verwendung einer Proxy aus, übergibt die
	 * final gelieferte Antwort dem Antwortbehandler und gibt den resultierten
	 * Wert zurück.
	 * 
	 * @param client
	 *            der Ausführer der Anfrage
	 * @param responseHandler
	 *            der Antwortbehandler
	 * @param request
	 *            die auszuführende Anfrage
	 * @return der Wert, der vom Antwortbehandler generiert wurde
	 */
	protected final R request(HttpClient client, ResponseHandler<R> responseHandler, HttpRequestBase request) throws IOException {
		while (true) {
			if (this.currProxy == null) {
				this.currProxy = this.getNextProxy();
				if (this.currProxy == null)
					throw new IOException("all proxies exhausted");
				this.currProxyCounter = 0;
			} else
				this.currProxyCounter++;
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, this.currProxy.toHttpHost());
			try {
				R response = client.execute(request, responseHandler);
				this.onRequestSucceed(this.currProxy, this.currProxyCounter);
				return response;
			} catch (Exception e) {
				this.onRequestFailed(this.currProxy, this.currProxyCounter, e.getMessage());
				this.currProxy = null;
			} finally {
				request.reset();
			}
		}
	}
	/**
	 * Wird bei einer erfolgreichen Ausführung einer Anfrage aufgerufen.
	 * Standartmäßig tut die Methode nichts. Der Benutzer kann diese Methode
	 * überschreiben, um zusätzliche Maßnahmen durchzuführen.
	 * 
	 * @param proxy
	 *            die Proxy, die bei der Anfrage verwendet wurde
	 * @param counter
	 *            der Zähler, wie oft die Proxy bereits erfolgreich verwendet
	 *            wurde
	 */
	protected void onRequestSucceed(Proxy proxy, int counter) {}
	/**
	 * Wird bei einer gescheiterten Ausführung einer Anfrage aufgerufen.
	 * Standartmäßig tut die Methode nichts. Der Benutzer kann diese Methode
	 * überschreiben, um zusätzliche Maßnahmen durchzuführen.
	 * 
	 * @param proxy
	 *            die Proxy, die bei der Anfrage verwendet wurde
	 * @param counter
	 *            der Zähler, wie oft die Proxy bereits erfolgreich verwendet
	 *            wurde
	 * @param message
	 *            Fehlernachricht
	 */
	protected void onRequestFailed(Proxy proxy, int counter, String message) {}
	/**
	 * Fordert eine neue Proxy an.
	 * 
	 * @return eine neue Proxy oder <code>null</code> falls keine Proxy
	 *         vorhanden ist
	 */
	protected abstract Proxy getNextProxy();
}
