package info.weboftrust.btctxlookup.bitcoinconnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.DidBtcrData;

public class BlockcypherAPIBitcoinConnection extends AbstractBitcoinConnection implements BitcoinConnection {

	private static final BlockcypherAPIBitcoinConnection instance = new BlockcypherAPIBitcoinConnection();

	public BlockcypherAPIBitcoinConnection() {

	}

	public static BlockcypherAPIBitcoinConnection get() {

		return instance;
	}

	@Override
	public ChainAndTxid lookupChainAndTxid(ChainAndLocationData chainAndLocationData) throws IOException {

		URI uri;
		if (chainAndLocationData.getChain() == Chain.MAINNET) {
			uri = URI.create("https://api.blockcypher.com/v1/btc/main/blocks/" + chainAndLocationData.getLocationData().getBlockHeight() + "?txstart=" + chainAndLocationData.getLocationData().getTransactionPosition() + "&limit=1");
		} else {
			uri = URI.create("https://api.blockcypher.com/v1/btc/test3/blocks/" + chainAndLocationData.getLocationData().getBlockHeight() + "?txstart=" + chainAndLocationData.getLocationData().getTransactionPosition() + "&limit=1");
		}

		JsonObject txData = retrieveJson(uri);
		String txid = txData.get("txids").getAsJsonArray().get(0).getAsString();

		return new ChainAndTxid(chainAndLocationData.getChain(), txid, chainAndLocationData.getLocationData().getTxoIndex());
	}

	@Override
	public ChainAndLocationData lookupChainAndLocationData(ChainAndTxid chainAndTxid) throws IOException {

		URI uri;
		if (chainAndTxid.getChain() == Chain.MAINNET) {
			uri = URI.create("https://api.blockcypher.com/v1/btc/main/txs/" + chainAndTxid.getTxid() + "?limit=500");
		} else {
			uri = URI.create("https://api.blockcypher.com/v1/btc/test3/txs/" + chainAndTxid.getTxid() + "?limit=500");
		}

		JsonObject txData = retrieveJson(uri);
		int blockHeight = txData.get("block_height").getAsInt();
		int transactionPosition = txData.get("block_index").getAsInt();

		if (blockHeight == -1 || transactionPosition == -1) return null;
		return new ChainAndLocationData(chainAndTxid.getChain(), blockHeight, transactionPosition, chainAndTxid.getTxoIndex());
	}

	private static final SimpleDateFormat RFC_339_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	@Override
	public DidBtcrData getDidBtcrData(ChainAndTxid chainAndTxid) throws IOException {

		// retrieve transaction data

		URI uri;
		if (chainAndTxid.getChain() == Chain.MAINNET) {
			uri = URI.create("https://api.blockcypher.com/v1/btc/main/txs/" + chainAndTxid.getTxid() + "?limit=500");
		} else {
			uri = URI.create("https://api.blockcypher.com/v1/btc/test3/txs/" + chainAndTxid.getTxid() + "?limit=500");
		}

		JsonObject txData = retrieveJson(uri);

		// find input script pub key

		String inputScriptPubKey = null;

		for (Iterator<JsonElement> i = ((JsonArray) txData.get("inputs")).iterator(); i.hasNext(); ) {

			JsonObject input = i.next().getAsJsonObject();
			JsonElement scriptType = input.get("script_type");
			if (scriptType == null || ! scriptType.isJsonPrimitive()) continue;

			if ("pay-to-pubkey-hash".equals(scriptType.getAsString())) {

				JsonElement script = input.get("script");
				if (script == null || ! script.isJsonPrimitive()) continue;

				Script payToPubKeyHashScript;

				try {

					payToPubKeyHashScript = new Script(Hex.decodeHex(script.getAsString().toCharArray()));
				} catch (ScriptException | DecoderException ex) {

					throw new IOException("Cannot decode script " + script.getAsString() + ": " + ex.getMessage(), ex);
				}

				inputScriptPubKey = Hex.encodeHexString(payToPubKeyHashScript.getChunks().get(1).data);
				break;
			} else if ("pay-to-witness-pubkey-hash".equals(scriptType.getAsString())) {

				JsonElement witness = input.get("witness");
				if (witness == null || ! witness.isJsonArray()) continue;

				inputScriptPubKey = witness.getAsJsonArray().get(1).getAsString();
				break;
			} else {

				throw new IOException("Script type " + scriptType.getAsString() + " not supported.");
			}
		}

		if (inputScriptPubKey == null) return null;
		if (inputScriptPubKey.length() > 66) inputScriptPubKey = inputScriptPubKey.substring(inputScriptPubKey.length() - 66);

		// find DID DOCUMENT CONTINUATION URI

		URI continuationUri = null;

		for (Iterator<JsonElement> i = ((JsonArray) txData.get("outputs")).iterator(); i.hasNext(); ) {

			JsonObject output = i.next().getAsJsonObject();
			JsonElement script = output.get("script");
			JsonElement scriptType = output.get("script_type");
			if (script == null || ! script.isJsonPrimitive()) continue;
			if (scriptType == null || ! scriptType.isJsonPrimitive()) continue;

			if ("null-data".equals(scriptType.getAsString())) {

				Script nullDataScript;

				try {

					nullDataScript = new Script(Hex.decodeHex(script.getAsString().toCharArray()));
				} catch (ScriptException | DecoderException ex) {

					throw new IOException("Cannot decode script " + script.getAsString() + ": " + ex.getMessage(), ex);
				}

				ScriptChunk scriptChunk = nullDataScript.getChunks().size() == 2 ? nullDataScript.getChunks().get(1) : null;
				byte[] data = scriptChunk == null ? null : scriptChunk.data;

				if (data == null || data.length < 1) throw new IOException("Cannot find data in script " + script.getAsString());

				continuationUri = URI.create(new String(data, StandardCharsets.UTF_8));
				break;
			}
		}

		// find spent in tx

		int outTxid = -1;
		ChainAndTxid spentInChainAndTxid = null;

		for (Iterator<JsonElement> i = ((JsonArray) txData.get("outputs")).iterator(); i.hasNext(); ) {

			outTxid++;

			JsonObject output = i.next().getAsJsonObject();
			JsonElement spentBy = output.get("spent_by");
			if (spentBy == null || ! spentBy.isJsonPrimitive()) continue;

			spentInChainAndTxid = new ChainAndTxid(chainAndTxid.getChain(), spentBy.getAsString(), outTxid);
			break;
		}

		// find transaction time

		JsonPrimitive received = (JsonPrimitive) txData.get("received");
		if (received == null) return null;

		long transactionTime;

		try {

			transactionTime = RFC_339_DATE_FORMAT.parse(received.getAsString()).getTime() / 1000L;
		} catch (ParseException ex) {

			throw new IOException("Cannot parse receive date '" + received.getAsString() + "': " + ex.getMessage(), ex);
		}

		// done

		return new DidBtcrData(spentInChainAndTxid, inputScriptPubKey, continuationUri, transactionTime);
	}

	/*
	 * Helper methods
	 */

	protected static final Gson gson = new Gson();

	protected static JsonObject retrieveJson(URI uri) throws IOException {

		URLConnection con = uri.toURL().openConnection();
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;

		return gson.fromJson(IOUtils.toString(in, encoding), JsonObject.class);
	}
}
