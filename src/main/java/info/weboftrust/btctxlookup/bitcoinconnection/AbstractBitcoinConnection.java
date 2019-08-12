package info.weboftrust.btctxlookup.bitcoinconnection;

import java.io.IOException;

import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.ChainAndTxid;

public abstract class AbstractBitcoinConnection implements BitcoinConnection {

	@Override
	public ChainAndTxid lookupChainAndTxid(Chain chain, int blockHeight, int transactionPosition, int txoIndex) throws IOException {

		return this.lookupChainAndTxid(new ChainAndLocationData(chain, blockHeight, transactionPosition, txoIndex));
	}

	@Override
	public ChainAndTxid lookupChainAndTxid(Chain chain, int blockHeight, int transactionPosition) throws IOException {

		return this.lookupChainAndTxid(new ChainAndLocationData(chain, blockHeight, transactionPosition));
	}

	@Override
	public ChainAndLocationData lookupChainAndLocationData(Chain chain, String txid, int txoIndex) throws IOException {

		return this.lookupChainAndLocationData(new ChainAndTxid(chain, txid, txoIndex));
	}

	@Override
	public ChainAndLocationData lookupChainAndLocationData(Chain chain, String txid) throws IOException {

		return this.lookupChainAndLocationData(new ChainAndTxid(chain, txid));
	}


	@Override
	public String toTxref(ChainAndTxid chainAndTxid) throws IOException {

		ChainAndLocationData chainAndLocationData = this.lookupChainAndLocationData(chainAndTxid);
		if (chainAndLocationData == null) return null;

		String txref = ChainAndLocationData.txrefEncode(chainAndLocationData);
		return txref;
	}

	@Override
	public String toTxref(Chain chain, String txid) throws IOException {

		return this.toTxref(new ChainAndTxid(chain, txid));
	}

	@Override
	public ChainAndTxid fromTxref(String txref) throws IOException {

		ChainAndLocationData chainAndLocationData = ChainAndLocationData.txrefDecode(txref);
		if (chainAndLocationData == null) throw new IllegalArgumentException("Could not decode txref " + txref);

		return this.lookupChainAndTxid(chainAndLocationData);
	}
}
