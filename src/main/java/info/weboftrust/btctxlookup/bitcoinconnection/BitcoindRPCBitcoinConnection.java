package info.weboftrust.btctxlookup.bitcoinconnection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.DidBtcrData;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.In;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out;

public class BitcoindRPCBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final BitcoindRPCBitcoinConnection instance = new BitcoindRPCBitcoinConnection();

	private static Logger log = LoggerFactory.getLogger(BitcoindRPCBitcoinConnection.class);

	private static final Pattern patternAsmInputScriptPubKey = Pattern.compile("^[^\\s]+ ([0-9a-fA-F]+)$");
	private static final Pattern patternAsmContinuationUri = Pattern.compile("^OP_RETURN ([0-9a-fA-F]+)$");

	protected BitcoinJSONRPCClient bitcoindRpcClientMainnet;
	protected BitcoinJSONRPCClient bitcoindRpcClientTestnet;

	public BitcoindRPCBitcoinConnection(BitcoinJSONRPCClient bitcoindRpcClientMainnet, BitcoinJSONRPCClient bitcoindRpcClientTestnet) {

		this.bitcoindRpcClientMainnet = bitcoindRpcClientMainnet;
		this.bitcoindRpcClientTestnet = bitcoindRpcClientTestnet;
	}

	public BitcoindRPCBitcoinConnection(URL rpcUrlMainnet, URL rpcUrlTestnet) {

		this(new BitcoinJSONRPCClient(rpcUrlMainnet), new BitcoinJSONRPCClient(rpcUrlTestnet));
	}

	public BitcoindRPCBitcoinConnection(String rpcUrlMainnet, String rpcUrlTestnet) throws MalformedURLException {

		this(new URL(rpcUrlMainnet), new URL(rpcUrlTestnet));
	}

	public BitcoindRPCBitcoinConnection() {

		this(BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL);
	}

	public static BitcoindRPCBitcoinConnection get() {

		return instance;
	}

	@Override
	public ChainAndTxid lookupChainAndTxid(ChainAndLocationData chainAndLocationData) throws IOException {

		BitcoindRpcClient bitcoindRpcClient = chainAndLocationData.getChain() == Chain.MAINNET ? this.bitcoindRpcClientMainnet : this.bitcoindRpcClientTestnet;

		Block block = bitcoindRpcClient.getBlock(chainAndLocationData.getLocationData().getBlockHeight());
		if (block == null) return null;
		if (block.tx().size() <= chainAndLocationData.getLocationData().getTransactionPosition()) return null;

		String txid = block.tx().get(chainAndLocationData.getLocationData().getTransactionPosition());

		return new ChainAndTxid(chainAndLocationData.getChain(), txid, chainAndLocationData.getLocationData().getTxoIndex());
	}

	@Override
	public ChainAndLocationData lookupChainAndLocationData(ChainAndTxid chainAndTxid) throws IOException {

		BitcoindRpcClient bitcoindRpcClient = chainAndTxid.getChain() == Chain.MAINNET ? bitcoindRpcClientMainnet : bitcoindRpcClientTestnet;

		RawTransaction rawTransaction = bitcoindRpcClient.getRawTransaction(chainAndTxid.getTxid());
		if (rawTransaction == null) return null;

		Block block = bitcoindRpcClient.getBlock(rawTransaction.blockHash());
		if (block == null) return null;

		int blockHeight = block.height();
		int transactionPosition;
		for (transactionPosition=0; transactionPosition<block.size(); transactionPosition++) { if (block.tx().get(transactionPosition).equals(chainAndTxid.getTxid())) break; }
		if (transactionPosition == block.size()) return null;

		return new ChainAndLocationData(chainAndTxid.getChain(), blockHeight, transactionPosition, chainAndTxid.getTxoIndex());
	}

	@Override
	public DidBtcrData getDidBtcrData(ChainAndTxid chainAndTxid) throws IOException {

		// retrieve transaction data

		BitcoindRpcClient bitcoindRpcClient = chainAndTxid.getChain() == Chain.MAINNET ? this.bitcoindRpcClientMainnet : this.bitcoindRpcClientTestnet;

		RawTransaction rawTransaction = bitcoindRpcClient.getRawTransaction(chainAndTxid.getTxid());
		if (rawTransaction == null) return null;

		// find input script pub key

		String inputScriptPubKey = null;

		List<In> vIn = rawTransaction.vIn();
		if (vIn == null || vIn.size() < 1) return null;

		for (In in : vIn) {

			Map<String, Object> scriptSig = in.scriptSig();
			if (scriptSig == null) continue;

			String asm = (String) scriptSig.get("asm");
			List<String> txinwitness = null;	// TODO: How to get this with bitcoind ?

			if (asm != null && ! asm.trim().isEmpty()) {

				Matcher matcher = patternAsmInputScriptPubKey.matcher(asm);

				if (log.isDebugEnabled()) log.debug("IN: " + in.scriptSig().get("asm") + " (MATCHES: " + matcher.matches() + ")");

				if (matcher.matches() && matcher.groupCount() == 1) {

					if (log.isDebugEnabled()) log.debug("inputScriptPubKey: " + matcher.group(1));

					inputScriptPubKey = matcher.group(1);
					break;
				}
			} else if (txinwitness != null && txinwitness.size() > 0) {

				inputScriptPubKey = txinwitness.get(1);
				break;
			} else {

				throw new IOException("Script type not supported.");
			}
		}

		if (inputScriptPubKey == null) return null;
		if (inputScriptPubKey.length() > 66) inputScriptPubKey = inputScriptPubKey.substring(inputScriptPubKey.length() - 66);

		// find DID DOCUMENT CONTINUATION URI

		URI continuationUri = null;

		List<Out> vOut = rawTransaction.vOut();
		if (vOut == null || vOut.size() < 1) return null;

		for (Out out : vOut) {

			if (out.scriptPubKey() != null && out.scriptPubKey().asm() != null) {

				Matcher matcher = patternAsmContinuationUri.matcher(out.scriptPubKey().asm());

				if (log.isDebugEnabled()) log.debug("OUT: " + out.scriptPubKey().asm() + " (MATCHES: " + matcher.matches() + ")");

				if (matcher.matches() && matcher.groupCount() == 1) {

					if (log.isDebugEnabled()) log.debug("continuationUri: " + matcher.group(1));

					try {

						continuationUri = URI.create(new String(Hex.decodeHex(matcher.group(1).toCharArray()), "UTF-8"));
						break;
					} catch (UnsupportedEncodingException | DecoderException ex) {

						continue;
					}
				}
			}
		}

		// find transaction lock time

		long transactionLockTime = rawTransaction.lockTime();

		// done

		return new DidBtcrData(null, inputScriptPubKey, continuationUri, transactionLockTime);
	}

	/*
	 * Getters and setters
	 */

	public BitcoinJSONRPCClient getBitcoindRpcClientMainnet() {

		return this.bitcoindRpcClientMainnet;
	}

	public void setBitcoindRpcClientMainnet(BitcoinJSONRPCClient bitcoindRpcClientMainnet) {

		this.bitcoindRpcClientMainnet = bitcoindRpcClientMainnet;
	}

	public void setRpcUrlMainnet(URL rpcUrlMainnet) {

		this.setBitcoindRpcClientMainnet(new BitcoinJSONRPCClient(rpcUrlMainnet));
	}

	public void setRpcUrlMainnet(String rpcUrlMainnet) throws MalformedURLException {

		this.setRpcUrlMainnet(new URL(rpcUrlMainnet));
	}

	public BitcoinJSONRPCClient getBitcoindRpcClientTestnet() {

		return this.bitcoindRpcClientTestnet;
	}

	public void setBitcoindRpcClientTestnet(BitcoinJSONRPCClient bitcoindRpcClientTestnet) {

		this.bitcoindRpcClientTestnet = bitcoindRpcClientTestnet;
	}

	public void setRpcUrlTestnet(URL rpcUrlTestnet) {

		this.setBitcoindRpcClientTestnet(new BitcoinJSONRPCClient(rpcUrlTestnet));
	}

	public void setRpcUrlTestnet(String rpcUrlTestnet) throws MalformedURLException {

		this.setRpcUrlTestnet(new URL(rpcUrlTestnet));
	}
}
