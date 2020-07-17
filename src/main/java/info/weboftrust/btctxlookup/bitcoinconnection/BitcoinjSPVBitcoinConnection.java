package info.weboftrust.btctxlookup.bitcoinconnection;

import java.io.IOException;

import org.bitcoinj.core.BlockChain;

import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.DidBtcrData;

public class BitcoinjSPVBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final BitcoinjSPVBitcoinConnection instance = new BitcoinjSPVBitcoinConnection();

	protected BlockChain blockChain;

	public BitcoinjSPVBitcoinConnection() {
		throw new RuntimeException("Not implemented.");
	}

	public BitcoinjSPVBitcoinConnection(BlockChain blockChain) {
		throw new RuntimeException("Not implemented.");
	}

	public static BitcoinjSPVBitcoinConnection get() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public ChainAndTxid lookupChainAndTxid(ChainAndLocationData chainAndLocationData) throws IOException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public ChainAndLocationData lookupChainAndLocationData(ChainAndTxid chainAndTxid) throws IOException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public DidBtcrData getDidBtcrData(ChainAndTxid chainAndTxid) {

		throw new RuntimeException("Not implemented.");
	}

	/*
	 * Getters and setters
	 */

	public BlockChain getBlockChain() {

		return this.blockChain;
	}

	public void setBlockChain(BlockChain blockChain) {

		this.blockChain = blockChain;
	}
}