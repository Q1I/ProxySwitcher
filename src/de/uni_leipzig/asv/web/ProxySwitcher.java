package de.uni_leipzig.asv.web;
import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import de.uni_leipzig.asv.web.proxy.Proxy;
/**
 * Das Objekt f�r die Ausf�hrung einer Anfrage unter der Verwendung einer Proxy.
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
	 * Standardm��iger Konstruktor.
	 */
	public ProxySwitcher() {}
	/**
	 * F�hrt eine Anfrage unter der Verwendung einer Proxy aus, �bergibt die
	 * final gelieferte Antwort dem Antwortbehandler und gibt den resultierten
	 * Wert zur�ck.
	 * 
	 * @param client
	 *            der Ausf�hrer der Anfrage
	 * @param responseHandler
	 *            der Antwortbehandler
	 * @param request
	 *            die auszuf�hrende Anfrage
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
	 * Wird bei einer erfolgreichen Ausf�hrung einer Anfrage aufgerufen.
	 * Standartm��ig tut die Methode nichts. Der Benutzer kann diese Methode
	 * �berschreiben, um zus�tzliche Ma�nahmen durchzuf�hren.
	 * 
	 * @param proxy
	 *            die Proxy, die bei der Anfrage verwendet wurde
	 * @param counter
	 *            der Z�hler, wie oft die Proxy bereits erfolgreich verwendet
	 *            wurde
	 */
	protected void onRequestSucceed(Proxy proxy, int counter) {}
	/**
	 * Wird bei einer gescheiterten Ausf�hrung einer Anfrage aufgerufen.
	 * Standartm��ig tut die Methode nichts. Der Benutzer kann diese Methode
	 * �berschreiben, um zus�tzliche Ma�nahmen durchzuf�hren.
	 * 
	 * @param proxy
	 *            die Proxy, die bei der Anfrage verwendet wurde
	 * @param counter
	 *            der Z�hler, wie oft die Proxy bereits erfolgreich verwendet
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
