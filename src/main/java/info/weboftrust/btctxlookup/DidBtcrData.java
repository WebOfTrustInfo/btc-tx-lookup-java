package info.weboftrust.btctxlookup;

import java.net.URI;

public class DidBtcrData {

	private final ChainAndTxid spentInChainAndTxid;
	private final String inputScriptPubKey;
	private final URI continuationUri;

	public DidBtcrData(ChainAndTxid spentInChainAndTxid, String inputScriptPubKey, URI continuationUri) { 

		this.spentInChainAndTxid = spentInChainAndTxid;
		this.inputScriptPubKey = inputScriptPubKey;
		this.continuationUri = continuationUri;
	}

	public ChainAndTxid getSpentInChainAndTxid() {

		return this.spentInChainAndTxid;
	}

	public String getInputScriptPubKey() {

		return this.inputScriptPubKey;
	}

	public URI getContinuationUri() {

		return this.continuationUri;
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {
		return "BtcrData [spentInChainAndTxid=" + spentInChainAndTxid + ", inputScriptPubKey=" + inputScriptPubKey
				+ ", continuationUri=" + continuationUri + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((continuationUri == null) ? 0 : continuationUri.hashCode());
		result = prime * result + ((inputScriptPubKey == null) ? 0 : inputScriptPubKey.hashCode());
		result = prime * result + ((spentInChainAndTxid == null) ? 0 : spentInChainAndTxid.hashCode());
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
		DidBtcrData other = (DidBtcrData) obj;
		if (continuationUri == null) {
			if (other.continuationUri != null)
				return false;
		} else if (!continuationUri.equals(other.continuationUri))
			return false;
		if (inputScriptPubKey == null) {
			if (other.inputScriptPubKey != null)
				return false;
		} else if (!inputScriptPubKey.equals(other.inputScriptPubKey))
			return false;
		if (spentInChainAndTxid == null) {
			if (other.spentInChainAndTxid != null)
				return false;
		} else if (!spentInChainAndTxid.equals(other.spentInChainAndTxid))
			return false;
		return true;
	}
}
