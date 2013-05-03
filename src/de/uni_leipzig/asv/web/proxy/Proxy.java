package de.uni_leipzig.asv.web.proxy;
import org.apache.http.HttpHost;
/**
 * Container der Parameter für die Erstellung eines Proxy-Hosts.
 * 
 * @author Sergej Sintschilin
 * @author Quan Nguyen
 */
public final class Proxy {
	/**
	 * Standartmäßige Host-Adresse ist <code>HTTP</code>.
	 */
	public static final Type DEFAULT_TYPE = Type.HTTP;
	/**
	 * Standartmäßige Port-Nummer ist <code>-1</code>.
	 */
	public static final int DEFAULT_PORT = -1;
	public static enum Type {
		HTTP("http"), HTTPS("https"), SOCKS("socks");
		private final String s;
		private Type(String s) {
			this.s = s;
		}
		public String toString() {
			return this.s;
		}
		public static Type getByString(String s) {
			for (Type type : Type.values())
				if (s.equals(type.s))
					return type;
			return null;
		}
	}
	private final String host;
	private final int port;
	private final Type type;
	/**
	 * Erstellt eine Proxy mit den übergebenen Host-Adresse, Port-Nummer und
	 * Typ.
	 * 
	 * @param host
	 *            der Host der Proxy
	 * @param port
	 *            der Port der Proxy
	 * @param type
	 *            der Typ der Proxy
	 */
	public Proxy(String host, int port, Type type) {
		if (host == null)
			throw new IllegalArgumentException("host name may not be null");
		this.host = host;
		this.port = port;
		if (type == null)
			throw new IllegalArgumentException("type name may not be null");
		this.type = type;
	}
	/**
	 * Erstellt eine Proxy mit den übergebenen Host-Adresse und Port-Nummer und
	 * dem standartmäßigen Typ.
	 * 
	 * @param host
	 *            der Host der Proxy
	 * @param port
	 *            der Port der Proxy
	 */
	public Proxy(String host, int port) {
		this(host, port, Proxy.DEFAULT_TYPE);
	}
	/**
	 * Erstellt eine Proxy mit der übergebenen Host-Adresse und
	 * den standartmäßigen Port-Nummer und Typ.
	 * 
	 * @param host
	 *            der Host der Proxy
	 */
	public Proxy(String host) {
		this(host, Proxy.DEFAULT_PORT);
	}
	/**
	 * Gibt die Host-Adresse der Proxy zurück.
	 * 
	 * @return der Host der Proxy
	 */
	public final String getHost() {
		return this.host;
	}
	/**
	 * Gibt die Port-Nummer der Proxy zurück.
	 * 
	 * @return der Port der Proxy
	 */
	public final int getPort() {
		return this.port;
	}
	/**
	 * Gibt den Typ der Proxy zurück.
	 * 
	 * @return der Typ der Proxy
	 */
	public final Type getType() {
		return this.type;
	}
	/**
	 * Konvertiert die Proxy zu einem HttpHost, der für die Anfragen benutzt
	 * werden kann.
	 * 
	 * @return HttpHost
	 */
	public final HttpHost toHttpHost() {
		return new HttpHost(this.host, this.port, this.type.toString());
	}
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(this.type.toString());
		buffer.append("://");
		buffer.append(this.host);
		if (this.port != -1) {
			buffer.append(':');
			buffer.append(String.valueOf(this.port));
		}
		return buffer.toString();
	}
}
