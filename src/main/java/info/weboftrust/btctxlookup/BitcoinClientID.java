package info.weboftrust.btctxlookup;

import com.google.common.base.Preconditions;

public enum BitcoinClientID {
	BITCOIND, BTCD, BITCOINJ, BLOCKCYPHERAPI;

	public static BitcoinClientID fromString(String client) {
		Preconditions.checkNotNull(client, "Chain string is null!");
		switch (client.toUpperCase()) {
		case "BITCOIND":
			return BITCOIND;
		case "BTCD":
			return BTCD;
		case "BITCOINJ":
			return BITCOINJ;
		case "BLOCKCYPHERAPI":
			return BLOCKCYPHERAPI;
		default:
			throw new IllegalArgumentException("Unknown Bitcoin client!");
		}
	}
}
