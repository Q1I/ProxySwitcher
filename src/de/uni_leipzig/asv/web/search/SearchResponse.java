package de.uni_leipzig.asv.web.search;
import java.util.ArrayList;
import java.util.List;
/**
 * Die standardm��ige Antwort einer Suchmaschine in Form einer Kapselung der
 * Suchergebnisse.
 * 
 * @author Sergej Sintschilin
 * @author Quan Nguyen
 */
public class SearchResponse {
	/**
	 * Das standardm��ige Suchergebnis einer Suchmaschine.
	 * 
	 * @author Sergej Sintschilin
	 * @author Quan Nguyen
	 */
	public static class Result {
		protected String title = null;
		protected String link = null;
		protected String snippet = null;
		protected Result() {}
		/**
		 * Gibt den Titel der Webseite zur�ck.
		 * 
		 * @return der Titel der Webseite
		 */
		public String getTitle() {
			return this.title;
		}
		/**
		 * Gibt das Link zur Webseite zur�ck.
		 * 
		 * @return das Link zur Webseite
		 */
		public String getLink() {
			return this.link;
		}
		/**
		 * Gibt den Textausschnitt aus der Webseite zur�ck.
		 * 
		 * @return der Textausschnitt aus der Webseite
		 */
		public String getSnippet() {
			return this.snippet;
		}
	}
	protected String requestedWebSearchEngine = null;
	protected String requestedQuery = null;
	protected Integer requestedResultCount = null;
	protected List<Result> results = new ArrayList<Result>();
	protected SearchResponse() {}
	/**
	 * Gibt den Namen der Suchmaschine zur�ck, die bei der Anfrage benutzt
	 * wurde.
	 * 
	 * @return die Suchmaschine
	 */
	public String getRequestedWebSearchEngine() {
		return this.requestedWebSearchEngine;
	}
	/**
	 * Gibt die urspr�ngliche Anfrage zu�ck, die an die Suchmaschine gestellt
	 * wurde.
	 * 
	 * @return die Suchanfrage
	 */
	public String getRequestedQuery() {
		return this.requestedQuery;
	}
	/**
	 * Gibt die urspr�ngliche Anzahl der angeforderten Ergebnisse.
	 * 
	 * @return die Anzahl der angeforderten Ergebnisse
	 */
	public Integer getRequestedResultCount() {
		return this.requestedResultCount;
	}
	/**
	 * Gibt die von der Suchmaschine gelieferten Ergebnisse.
	 * 
	 * @return die Suchergebnisse
	 */
	public List<Result> getResults() {
		return new ArrayList<Result>(this.results);
	}
}