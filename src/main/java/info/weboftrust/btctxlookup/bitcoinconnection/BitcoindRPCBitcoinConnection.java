package info.weboftrust.btctxlookup.bitcoinconnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;

import info.weboftrust.btctxlookup.*;
import info.weboftrust.btctxlookup.dto.UTXOSet;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.In;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out;
import wf.bitcoin.javabitcoindrpcclient.GenericRpcException;

public class BitcoindRPCBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	protected static final Pattern patternAsmInputScriptPubKey = Pattern.compile("^[^\\s]+ ([0-9a-fA-F]+)$");
	protected static final Pattern patternAsmContinuationUri = Pattern.compile("^OP_RETURN ([0-9a-fA-F]+)$");
	private final static ObjectMapper mapper;
	private static final Logger log = LogManager.getLogger(BitcoindRPCBitcoinConnection.class);

	private static final BitcoinClientID CLIENT_ID = BitcoinClientID.BITCOIND;

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	protected boolean legacy = false;
	protected Chain chain;
	protected BitcoinJSONRPCClient bitcoindRpcClientMainnet = null;
	protected BitcoinJSONRPCClient bitcoindRpcClientTestnet = null;
	protected BitcoinJSONRPCClient bitcoindRpcClient = null;

	@Deprecated
	public BitcoindRPCBitcoinConnection(String rpcUrlMainnet, String rpcUrlTestnet) throws MalformedURLException {
		this(new URL(rpcUrlMainnet), new URL(rpcUrlTestnet));
	}

	@Deprecated
	public BitcoindRPCBitcoinConnection(URL rpcUrlMainnet, URL rpcUrlTestnet) {
		this(new BitcoinJSONRPCClient(rpcUrlMainnet), new BitcoinJSONRPCClient(rpcUrlTestnet));
	}

	@Deprecated
	public BitcoindRPCBitcoinConnection(BitcoinJSONRPCClient bitcoindRpcClientMainnet,
			BitcoinJSONRPCClient bitcoindRpcClientTestnet) {

		this.bitcoindRpcClientMainnet = bitcoindRpcClientMainnet;
		this.bitcoindRpcClientTestnet = bitcoindRpcClientTestnet;
		this.legacy = true;
	}

	public BitcoindRPCBitcoinConnection(String rpcUrl, Chain chain) throws MalformedURLException {
		this(new URL(rpcUrl), chain);
	}

	public BitcoindRPCBitcoinConnection(URL rpcUrl, Chain chain) {
		this(new BitcoinJSONRPCClient(rpcUrl), chain);
	}

	public BitcoindRPCBitcoinConnection(BitcoinJSONRPCClient bitcoindRpcClient, Chain chain) {
		this.chain = chain;
		this.bitcoindRpcClient = bitcoindRpcClient;
	}

	@Deprecated
	public BitcoindRPCBitcoinConnection() {
		this(BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL);
	}

	public static BitcoinClientID getClientId() {
		return CLIENT_ID;
	}

	public Chain getChain() {
		return chain;
	}

	public BitcoinJSONRPCClient getBitcoindRpcClient() {
		return bitcoindRpcClient;
	}

	@Override
	public ChainAndTxid lookupChainAndTxid(ChainAndLocationData chainAndLocationData) {
		BitcoindRpcClient client = getBitcoinRpcClient(chainAndLocationData.getChain());

		Block block = client.getBlock(chainAndLocationData.getLocationData().getBlockHeight());
		if (block == null)
			return null;
		if (block.tx().size() <= chainAndLocationData.getLocationData().getTransactionPosition())
			return null;

		String txid = block.tx().get(chainAndLocationData.getLocationData().getTransactionPosition());

		return new ChainAndTxid(chainAndLocationData.getChain(), txid,
				chainAndLocationData.getLocationData().getTxoIndex());
	}

	protected BitcoinJSONRPCClient getBitcoinRpcClient(Chain chain) {
		if (legacy) {
			switch (chain) {
			case MAINNET:
				return bitcoindRpcClientMainnet;
			case TESTNET:
				return bitcoindRpcClientTestnet;
			default:
				throw new IllegalArgumentException();
			}
		} else {
			return bitcoindRpcClient;
		}
	}

	@Nullable
	@Override
	public ChainAndLocationData lookupChainAndLocationData(ChainAndTxid chainAndTxid) {
		log.info("Getting chain and location data for txid: {} on chain: {}", chainAndTxid::getTxid,
				chainAndTxid::getChain);

		BitcoindRpcClient client = getBitcoinRpcClient(chainAndTxid.getChain());

		RawTransaction rawTransaction = client.getRawTransaction(chainAndTxid.getTxid());
		if (rawTransaction == null) {
			return null;
		}

		Block block = client.getBlock(rawTransaction.blockHash());
		if (block == null) {
			return null;
		}

		int blockHeight = block.height();
		int transactionPosition;
		for (transactionPosition = 0; transactionPosition < block.size(); transactionPosition++) {
			if (block.tx().get(transactionPosition).equals(chainAndTxid.getTxid()))
				break;
		}
		if (transactionPosition == block.size())
			return null;

		final ChainAndLocationData result = new ChainAndLocationData(chainAndTxid.getChain(), blockHeight,
				transactionPosition, chainAndTxid.getTxoIndex());

		log.debug("Resolved chain and location data is: \nBlock Height: {}, TX Position: {}, txoIndex: {}",
				() -> result.getLocationData().getBlockHeight(),
				() -> result.getLocationData().getTransactionPosition(), () -> result.getLocationData().getTxoIndex());

		return result;
	}

	@Override
	public DidBtcrData getDidBtcrData(ChainAndTxid chainAndTxid) throws IOException {

		// retrieve transaction data

		BitcoindRpcClient client = getBitcoinRpcClient(chainAndTxid.getChain());

		RawTransaction rawTransaction = client.getRawTransaction(chainAndTxid.getTxid());
		if (rawTransaction == null)
			return null;

		// find input script pub key

		String inputScriptPubKey = null;

		List<In> vIn = rawTransaction.vIn();
		if (vIn == null || vIn.size() < 1)
			return null;

		for (In in : vIn) {

			Map<String, Object> scriptSig = in.scriptSig();
			if (scriptSig == null)
				continue;

			String asm = (String) scriptSig.get("asm");

			Map<String, Object> mIn = mapper.convertValue(in, new TypeReference<Map<String, Object>>() {
			});
			List<String> txinwitness = (List<String>) ((Map) mIn.get("m")).get("txinwitness");

			if (asm != null && !asm.trim().isEmpty()) {

				Matcher matcher = patternAsmInputScriptPubKey.matcher(asm);

				if (log.isDebugEnabled())
					log.debug("IN: " + in.scriptSig().get("asm") + " (MATCHES: " + matcher.matches() + ")");

				if (matcher.matches() && matcher.groupCount() == 1) {

					if (log.isDebugEnabled())
						log.debug("inputScriptPubKey: " + matcher.group(1));

					inputScriptPubKey = matcher.group(1);
					break;
				}
			} else if (txinwitness != null && txinwitness.size() == 2) {

				// Get the second witness push -> pubKey
				inputScriptPubKey = txinwitness.get(1);
				break;
			} else {

				throw new IOException("Script type not supported.");
			}
		}

		if (inputScriptPubKey == null)
			return null;
		if (inputScriptPubKey.length() > 66)
			inputScriptPubKey = inputScriptPubKey.substring(inputScriptPubKey.length() - 66);

		// find DID DOCUMENT CONTINUATION URI

		URI continuationUri = null;

		List<Out> vOut = rawTransaction.vOut();
		if (vOut == null || vOut.size() < 1)
			return null;

		for (Out out : vOut) {

			if (out.scriptPubKey() != null && out.scriptPubKey().asm() != null) {

				Matcher matcher = patternAsmContinuationUri.matcher(out.scriptPubKey().asm());

				if (log.isDebugEnabled())
					log.debug("OUT: " + out.scriptPubKey().asm() + " (MATCHES: " + matcher.matches() + ")");

				if (matcher.matches() && matcher.groupCount() == 1) {

					if (log.isDebugEnabled())
						log.debug("continuationUri: " + matcher.group(1));

					try {

						continuationUri = URI.create(
								new String(Hex.decodeHex(matcher.group(1).toCharArray()), StandardCharsets.UTF_8));
						break;
					} catch (DecoderException ignored) {

					}
				}
			}
		}

		// find transaction lock time

		long transactionLockTime = rawTransaction.lockTime();

		// done

		return new DidBtcrData(null, inputScriptPubKey, continuationUri, transactionLockTime);
	}

	public String sendRawTransaction(String rawtxhex) throws BitcoinConnectionException {
		Preconditions.checkState(!legacy);
		try {
			return bitcoindRpcClient.sendRawTransaction(rawtxhex);
		} catch (GenericRpcException e) {
			throw new BitcoinConnectionException(e);
		}
	}

	public int getBlockCount() throws BitcoinConnectionException {
		Preconditions.checkState(!legacy);
		try {
			return bitcoindRpcClient.getBlockCount();
		} catch (GenericRpcException e) {
			throw new BitcoinConnectionException(e);
		}
	}

	public BigDecimal estimateFees(int targetConfirmInBlocks) {
		Preconditions.checkState(!legacy);
		return bitcoindRpcClient.estimateFee(targetConfirmInBlocks);
	}

	public Map<String, Long> findUnspents(String address) throws BitcoinConnectionException {
		Preconditions.checkState(!legacy);
		Preconditions.checkNotNull(address, "Given address is null!");
		final Object response = bitcoindRpcClient.query("scantxoutset", "start",
				Collections.singletonList("addr(" + address + ")"));

		final UTXOSet utxoSet = mapper.convertValue(response, UTXOSet.class);

		if (utxoSet.getUnspents().isEmpty()) {
			log.info("No UTXO found for the address: {}", address::toString);
			return null;
		}

		Map<String, Long> utxos = new LinkedHashMap<>();

		for (UTXOSet.Unspent unspent : utxoSet.getUnspents()) {
			String rawtx = getRawTransactionHex(unspent.getTxid());
			utxos.put(rawtx, unspent.getVout());
		}
		return utxos;
	}

	public String getRawTransactionHex(String txid) throws BitcoinConnectionException {
		Preconditions.checkState(!legacy);
		try {
			return bitcoindRpcClient.getRawTransaction(txid).hex();
		} catch (GenericRpcException e) {
			throw new BitcoinConnectionException(e);
		}
	}

	public BitcoindRpcClient.RawTransaction getRawTransaction(String txid) throws BitcoinConnectionException {
		Preconditions.checkState(!legacy);
		try {
			return bitcoindRpcClient.getRawTransaction(txid);
		} catch (GenericRpcException e) {
			throw new BitcoinConnectionException(e);
		}
	}

	public boolean isTxConfirmed(String txID, int requiredDepth) {
		Preconditions.checkState(!legacy);
		log.debug("Checking confirmations for tx id {} ", () -> txID);
		BitcoindRpcClient.RawTransaction raw;

		try {
			raw = bitcoindRpcClient.getRawTransaction(txID);
		} catch (GenericRpcException e) {
			log.error(e);
			return false;
		}

		Preconditions.checkNotNull(raw, "Cannot get the TX from bitcoin client");
		log.trace("RAW TX for txID {} is:\n{}", () -> txID, () -> raw);
		int confirms = 0;
		if (raw.confirmations() != null) {
			confirms = raw.confirmations();
		}

		log.debug("Transaction has {} confirmations.", confirms);

		return confirms >= requiredDepth;
	}

	/*
	 * Getters and setters
	 */

	@Deprecated
	public BitcoinJSONRPCClient getBitcoindRpcClientMainnet() {
		return this.bitcoindRpcClientMainnet;
	}

	@Deprecated
	public void setBitcoindRpcClientMainnet(BitcoinJSONRPCClient bitcoindRpcClientMainnet) {
		this.bitcoindRpcClientMainnet = bitcoindRpcClientMainnet;
	}

	@Deprecated
	public void setRpcUrlMainnet(String rpcUrlMainnet) throws MalformedURLException {
		this.setRpcUrlMainnet(new URL(rpcUrlMainnet));
	}

	@Deprecated
	public void setRpcUrlMainnet(URL rpcUrlMainnet) {
		this.bitcoindRpcClientMainnet = new BitcoinJSONRPCClient(rpcUrlMainnet);
	}

	@Deprecated
	public BitcoinJSONRPCClient getBitcoindRpcClientTestnet() {
		return this.bitcoindRpcClientTestnet;
	}

	@Deprecated
	public void setBitcoindRpcClientTestnet(BitcoinJSONRPCClient bitcoindRpcClientTestnet) {
		this.bitcoindRpcClientTestnet = bitcoindRpcClientTestnet;
	}

	@Deprecated
	public void setRpcUrlTestnet(String rpcUrlTestnet) throws MalformedURLException {
		this.setRpcUrlTestnet(new URL(rpcUrlTestnet));
	}

	@Deprecated
	public void setRpcUrlTestnet(URL rpcUrlTestnet) {
		this.bitcoindRpcClientTestnet = new BitcoinJSONRPCClient(rpcUrlTestnet);
	}
}
