package info.weboftrust.btctxlookup;

import com.google.common.base.Preconditions;

public enum Chain {

	MAINNET("MAINNET"), TESTNET("TESTNET"), REGTESTNET("REGTESTNET");

	private final String value;

	Chain(String value) {
		this.value = value;
	}

	public static Chain fromString(String chain) {
		Preconditions.checkNotNull(chain, "Chain cannot be null!");
		switch (chain.toUpperCase()) {
		case "MAINNET":
			return MAINNET;
		case "TESTNET":
			return TESTNET;
		case "REGTESTNET":
			return REGTESTNET;
		default:
			throw new IllegalArgumentException();
		}
	}

	public String toString() {
		return value;
	}
}