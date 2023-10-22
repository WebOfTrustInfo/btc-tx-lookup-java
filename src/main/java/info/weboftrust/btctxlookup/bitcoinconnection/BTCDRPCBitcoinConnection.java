package info.weboftrust.btctxlookup.bitcoinconnection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import info.weboftrust.btctxlookup.BitcoinClientID;
import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.DidBtcrData;
import info.weboftrust.btctxlookup.dto.AddressRelatedTx;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.In;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;

public class BTCDRPCBitcoinConnection extends BitcoindRPCBitcoinConnection {

	private final static ObjectMapper mapper;
	private static final Logger log = LoggerFactory.getLogger(BTCDRPCBitcoinConnection.class);

	private static final int DEFAULT_COUNT = 100;
	private static final int DEFAULT_SKIP = 0;
	private static final boolean DEFAULT_REVERSE = false;

	private static final BitcoinClientID CLIENT_ID = BitcoinClientID.BTCD;

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Deprecated
	public BTCDRPCBitcoinConnection(String rpcUrlMainnet, String rpcUrlTestnet) throws MalformedURLException {
		super(rpcUrlMainnet, rpcUrlTestnet);
	}

	public BTCDRPCBitcoinConnection(String rpcUrl, Chain chain) throws MalformedURLException {
		super(rpcUrl, chain);
	}

	public BTCDRPCBitcoinConnection(URL rpcUrl, Chain chain) {
		super(rpcUrl, chain);
	}

	@Deprecated
	public BTCDRPCBitcoinConnection(URL rpcUrlMainnet, URL rpcUrlTestnet) {
		super(rpcUrlMainnet, rpcUrlTestnet);
	}

	@Deprecated
	public BTCDRPCBitcoinConnection(BitcoinJSONRPCClient bitcoindRpcClientMainnet,
			BitcoinJSONRPCClient bitcoindRpcClientTestnet) {
		super(bitcoindRpcClientMainnet, bitcoindRpcClientTestnet);
	}

	@Deprecated
	public BTCDRPCBitcoinConnection() {
		super();
	}

	public static BitcoinClientID getClientId() {
		return CLIENT_ID;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public DidBtcrData getDidBtcrData(ChainAndTxid chainAndTxid) throws IOException {

		BitcoinJSONRPCClient btcdRpcClient = getBitcoinRpcClient(chainAndTxid.getChain());

		// retrieve transaction data

		BitcoindRpcClient.RawTransaction rawTransaction = btcdRpcClient.getRawTransaction(chainAndTxid.getTxid());
		Preconditions.checkNotNull(rawTransaction, "RPC Error: Cannot get the transaction!");

		// find input script pub key

		String inputScriptPubKey = null;

		List<In> vIn = rawTransaction.vIn();
		if (vIn == null || vIn.size() < 1) {
			return null;
		}

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

		// Find change address

		String addr = null;
		for (Out out : vOut) {
			if (!"nulldata".equals(out.scriptPubKey().type())) {
				addr = out.scriptPubKey().addresses().get(0);
			}
		}

		// find spent in tx

		Map.Entry<ChainAndTxid, Boolean> result = findSpentInChainAndTxidWithDeactivation(addr, chainAndTxid.getTxid());

		// find transaction lock time

		long transactionLockTime = rawTransaction.lockTime();

		// done

		return new DidBtcrData(result == null ? null : result.getKey(), inputScriptPubKey, continuationUri,
				transactionLockTime, result == null ? false : result.getValue());
	}

	@Nullable
	public Map.Entry<ChainAndTxid, Boolean> findSpentInChainAndTxidWithDeactivation(String address, String latestTx) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(address));
		List<AddressRelatedTx> addressRelatedTxList = searchRawTransactions(address, 0, 100, 1, false, null);
		boolean correctTX = false;
		boolean latestTX = false;
		for (AddressRelatedTx atx : addressRelatedTxList) {
			if (!correctTX) {
				latestTX = latestTx.equals(atx.getTxid());
			}
			if (!correctTX && !latestTX) {
				continue;
			}
			if (!correctTX) {
				correctTX = true;
				continue;
			}

			int txoindex = 0;
			for (AddressRelatedTx.Vin in : atx.getVin()) {
				for (String addr : in.getPrevOut().getAddresses()) {
					if (address.equals(addr)) {

						ChainAndTxid chainAndTxid = new ChainAndTxid(chain, atx.getTxid(), txoindex);
						boolean deactivated = checkDeactivation(atx);
						return new AbstractMap.SimpleEntry<>(chainAndTxid, deactivated);
					}
				}
				txoindex++;
			}
		}
		return null;
	}

	public List<AddressRelatedTx> searchRawTransactions(String address, int skip, int count, int vinextra,
			boolean reverse, String[] filteraddrs) {

		Preconditions.checkNotNull(address, "Given target address is null!");
		Preconditions.checkNotNull(bitcoindRpcClient, "Bitcoind client is null");
		Preconditions.checkArgument(skip >= 0, "Skip count must be >= 0, argument was %s", skip);
		Preconditions.checkArgument(count > 0, "TX count must be > 0, argument was %s", count);
		Preconditions.checkArgument(vinextra == 0 || vinextra == 1, "Vin extra must be 0 or 1, argument was %s",
				vinextra);

		log.info(
				"Request received for searchRawTransactions. \nAddress: {}, skip {}, count {}, vinextra {}, reverse {}, filteraddrs {}",
				address, skip, count, vinextra, reverse, Arrays.toString(filteraddrs));

		Object searchrawtransactions = bitcoindRpcClient.query("searchrawtransactions", address, 1, skip, count,
				vinextra, reverse, filteraddrs);

		return mapper.convertValue(searchrawtransactions, new TypeReference<List<AddressRelatedTx>>() {
		});
	}

	private static boolean checkDeactivation(AddressRelatedTx atx) {
		if (atx.getVout().size() > 2) {
			throw new IllegalArgumentException("Invalid BTCR-TX format");
		}
		return atx.getVout().size() == 1;
	}

	public Map<String, Long> findUnspents(String address) {
		return this.findUnspents(address, 0, 100, false);
	}

	public Map<String, Long> findUnspents(String address, int skip, int count, boolean reverse) {
		Preconditions.checkState(!legacy);
		log.info("Request received for finding UTXOs. \nAddress: {}, skip {}, count {}, reverse {}", address, skip,
				count, reverse);
		List<AddressRelatedTx> addRelTxs = searchRawTransactions(address, skip, count, 1, reverse, null);
		log.debug("{} TX found with for the address {}", addRelTxs.size(), address);
		List<String> txins = new ArrayList<>();
		Map<String, Long> txouts = new LinkedHashMap<>();

		for (AddressRelatedTx atx : addRelTxs) {
			for (AddressRelatedTx.Vout out : atx.getVout()) {
				if (out.getScriptPubKey() != null && out.getScriptPubKey().getAddresses() != null) {
					for (String addr : out.getScriptPubKey().getAddresses()) {
						if (address.equals(addr)) {
							txouts.put(atx.getTxid(), out.getOutIndex());
						}
					}
				}
			}
			for (AddressRelatedTx.Vin in : atx.getVin()) {
				for (String addr : in.getPrevOut().getAddresses()) {
					if (address.equals(addr)) {
						txins.add(in.getTxid());
					}
				}
			}
		}

		txins.forEach(txouts::remove);

		Map<String, Long> utxos = new LinkedHashMap<>();

		for (AddressRelatedTx atx : addRelTxs) {
			if (txouts.containsKey(atx.getTxid())) {
				utxos.put(atx.getHex(), txouts.get(atx.getTxid()));
			}
		}

		return utxos;
	}

	@Nullable
	public ChainAndTxid findSpentInChainAndTxid(String address, String latestTx) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(address));
		List<AddressRelatedTx> addressRelatedTxList = searchRawTransactions(address, 0, 100, 1, false, null);
		boolean correctTX = false;
		boolean latestTX = false;
		for (AddressRelatedTx atx : addressRelatedTxList) {
			if (!correctTX) {
				latestTX = latestTx.equals(atx.getTxid());
			}
			if (!correctTX && !latestTX) {
				continue;
			}
			if (!correctTX) {
				correctTX = true;
				continue;
			}

			int txoindex = 0;
			for (AddressRelatedTx.Vin in : atx.getVin()) {
				for (String addr : in.getPrevOut().getAddresses()) {
					if (address.equals(addr)) {

						return new ChainAndTxid(chain, atx.getTxid(), txoindex);
					}
				}
				txoindex++;
			}
		}
		return null;
	}
}
