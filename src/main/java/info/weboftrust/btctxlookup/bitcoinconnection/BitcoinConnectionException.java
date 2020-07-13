package info.weboftrust.btctxlookup.bitcoinconnection;

public class BitcoinConnectionException extends Exception {
	public BitcoinConnectionException(String msg, Throwable err) {
		super(msg, err);
	}

	public BitcoinConnectionException(String msg) {
		super(msg);
	}

	public BitcoinConnectionException(Throwable err) {
		super(err);
	}
}
