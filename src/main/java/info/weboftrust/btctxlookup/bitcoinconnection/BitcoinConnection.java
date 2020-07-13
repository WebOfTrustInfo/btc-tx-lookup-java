package info.weboftrust.btctxlookup.bitcoinconnection;

import java.io.IOException;

import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.DidBtcrData;

public interface BitcoinConnection {

	ChainAndTxid lookupChainAndTxid(ChainAndLocationData chainAndLocationData) throws IOException;

	ChainAndTxid lookupChainAndTxid(Chain chain, int blockHeight, int transactionPosition, int txoIndex)
			throws IOException;

	ChainAndTxid lookupChainAndTxid(Chain chain, int blockHeight, int transactionPosition) throws IOException;

	ChainAndLocationData lookupChainAndLocationData(ChainAndTxid chainAndTxid) throws IOException;

	ChainAndLocationData lookupChainAndLocationData(Chain chain, String txid, int txoIndex) throws IOException;

	ChainAndLocationData lookupChainAndLocationData(Chain chain, String txid) throws IOException;

	String toTxref(ChainAndTxid chainAndTxid) throws IOException;

	String toTxref(Chain chain, String txid) throws IOException;

	ChainAndTxid fromTxref(String txref) throws IOException;

	DidBtcrData getDidBtcrData(ChainAndTxid chainAndTxid) throws IOException;

}
