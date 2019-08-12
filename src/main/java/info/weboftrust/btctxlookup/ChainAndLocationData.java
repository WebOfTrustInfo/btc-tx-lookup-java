package info.weboftrust.btctxlookup;

import design.contract.txref.LocationData;
import design.contract.txref.Txref;

public class ChainAndLocationData {

	private Chain chain;
	private LocationData locationData;

	public ChainAndLocationData(Chain chain, LocationData locationData) {

		this.chain = chain;
		this.locationData = locationData;
	}

	public ChainAndLocationData(Chain chain, int blockHeight, int transactionPosition, int txoIndex) {

		this.chain = chain;
		this.locationData = new LocationData(null, null, blockHeight, transactionPosition, txoIndex, -1);
	}

	public ChainAndLocationData(Chain chain, int blockHeight, int transactionPosition) {

		this.chain = chain;
		this.locationData = new LocationData(null, null, blockHeight, transactionPosition, -1, -1);
	}

	/*
	 * Getters
	 */

	public Chain getChain() { 

		return this.chain;
	}

	public LocationData getLocationData() { 

		return this.locationData;
	}

	/*
	 * Helper methods
	 */

	public static String txrefEncode(Chain chain, int blockHeight, int transactionPosition, int txoIndex, boolean forceExtended) {

		if (Chain.MAINNET == chain)
			return Txref.encode(blockHeight, transactionPosition, txoIndex, forceExtended);
		else if (Chain.TESTNET == chain)
			return Txref.encodeTestnet(blockHeight, transactionPosition, txoIndex, forceExtended);
		else
			throw new IllegalArgumentException("Unknown chain: " + chain);
	}

	public static String txrefEncode(Chain chain, int blockHeight, int transactionPosition, int txoIndex) {

		if (Chain.MAINNET == chain)
			return Txref.encode(blockHeight, transactionPosition, txoIndex);
		else if (Chain.TESTNET == chain)
			return Txref.encodeTestnet(blockHeight, transactionPosition, txoIndex);
		else
			throw new IllegalArgumentException("Unknown chain: " + chain);
	}

	public static String txrefEncode(Chain chain, int blockHeight, int transactionPosition) {

		if (Chain.MAINNET == chain)
			return Txref.encode(blockHeight, transactionPosition);
		else if (Chain.TESTNET == chain)
			return Txref.encodeTestnet(blockHeight, transactionPosition);
		else
			throw new IllegalArgumentException("Unknown chain: " + chain);
	}

	public static String txrefEncode(ChainAndLocationData chainAndLocationData) {

		if (chainAndLocationData.getLocationData().getTxoIndex() == -1)
			return txrefEncode(chainAndLocationData.getChain(), chainAndLocationData.getLocationData().getBlockHeight(), chainAndLocationData.getLocationData().getTransactionPosition());
		else
			return txrefEncode(chainAndLocationData.getChain(), chainAndLocationData.getLocationData().getBlockHeight(), chainAndLocationData.getLocationData().getTransactionPosition(), chainAndLocationData.getLocationData().getTxoIndex());
	}

	public static ChainAndLocationData txrefDecode(String txref) {

		LocationData locationData = Txref.decode(txref);
		Chain chain;

		if (Txref.MAGIC_BTC_MAIN == locationData.getMagicCode() || Txref.MAGIC_BTC_MAIN_EXTENDED == locationData.getMagicCode()) chain = Chain.MAINNET;
		else if (Txref.MAGIC_BTC_TEST == locationData.getMagicCode() || Txref.MAGIC_BTC_TEST_EXTENDED == locationData.getMagicCode()) chain = Chain.TESTNET;
		else throw new IllegalStateException("Unknown magic code: " + locationData.getMagicCode());

		return new ChainAndLocationData(chain, locationData);
	}

	public boolean isExtended() {

		if (Txref.MAGIC_BTC_MAIN_EXTENDED == this.getLocationData().getMagicCode() || Txref.MAGIC_BTC_TEST_EXTENDED == this.getLocationData().getMagicCode()) return true;
		if (Txref.MAGIC_BTC_MAIN == this.getLocationData().getMagicCode() || Txref.MAGIC_BTC_TEST == this.getLocationData().getMagicCode()) return false;

		throw new IllegalStateException("Unknown magic code: " + this.getLocationData().getMagicCode());
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {
		return "ChainAndLocationData [chain=" + chain + ", locationData=" + locationData + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chain == null) ? 0 : chain.hashCode());
		result = prime * result + ((locationData == null) ? 0 : locationData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChainAndLocationData other = (ChainAndLocationData) obj;
		if (chain != other.chain)
			return false;
		if (locationData == null) {
			if (other.locationData != null)
				return false;
		} else if (!locationData.equals(other.locationData))
			return false;
		return true;
	}
}