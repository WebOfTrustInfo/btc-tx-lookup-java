package info.weboftrust.btctxlookup;

public class ChainAndTxid {

	private final Chain chain;
	private final String txid;
	private final int txoIndex;

	public ChainAndTxid(Chain chain, String txid) {

		this(chain, txid, -1);
	}

	public ChainAndTxid(Chain chain, String txid, int txoIndex) {

		this.chain = chain;
		this.txid = txid;
		this.txoIndex = txoIndex;
	}

	/*
	 * Getters
	 */

	public Chain getChain() {

		return this.chain;
	}

	public String getTxid() {

		return this.txid;
	}

	public int getTxoIndex() {

		return this.txoIndex;
	}

	/*
	 * Object methods
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chain == null) ? 0 : chain.hashCode());
		result = prime * result + ((txid == null) ? 0 : txid.hashCode());
		result = prime * result + txoIndex;
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
		ChainAndTxid other = (ChainAndTxid) obj;
		if (chain != other.getChain())
			return false;
		if (txid == null) {
			if (other.getTxid() != null)
				return false;
		} else if (!txid.equals(other.getTxid()))
			return false;
		return txoIndex == other.getTxoIndex();
	}

	@Override
	public String toString() {
		return "ChainAndTxid [chain=" + chain + ", txid=" + txid + ", txoIndex=" + txoIndex + "]";
	}
}