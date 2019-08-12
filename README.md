Built during the [2019 BTCR Hackathon](https://weboftrustinfo.github.io/btcr-hackathon-2019/) August 5th - 9th 2019.

![RWoT Logo](https://github.com/WebOfTrustInfo/ld-signatures-java/blob/master/wot-logo.png?raw=true)

# BTC TX Lookup

This library supports looking up Bitcoin transactions by TXREF and TXID, as well as looking up information specific to the [BTCR DID method](https://w3c-ccg.github.io/didm-btcr/).

It can use the following sources for lookups:

 * Blockcypher API - see `BlockcypherAPIBitcoinConnection.java`
 * Bitcoind JSON-RPC - see `BitcoindRPCBitcoinConnection.java`
 * BTCD JSON-RPC - see `BTCDRPCBitcoinConnection.java`
 * Bitcoinj SPV - see `BitcoinjSPVBitcoinConnection.java` (not implemented)

This library uses [libbech32-java](https://github.com/dcdpr/libbech32-java) and [libtxref-java](https://github.com/dcdpr/libtxref-java) as dependencies for support of [Bech32 Encoded Tx Position References](https://github.com/bitcoin/bips/blob/master/bip-0136.mediawiki).

It supersedes the earlier [txref-conversion-java](https://github.com/WebOfTrustInfo/txref-conversion-java/) library.

It is used by the [BTCR DID driver](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/btcr/) of the [DIF Universal Resolver](https://github.com/decentralized-identity/universal-resolver/).

### Maven

Build:

	mvn clean install

Dependency:

	<dependency>
		<groupId>info.weboftrust</groupId>
		<artifactId>btc-tx-lookup-java</artifactId>
		<version>0.1-SNAPSHOT</version>
		<scope>compile</scope>
	</dependency>

### Examples

#### Lookup a TXREF by TXID

	import info.weboftrust.btctxlookup.Chain;
	import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinConnection;
	import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
	
	public class Test {
	
		public static void main(String[] args) throws Exception {
	
			BitcoinConnection bitcoinConnection = BlockcypherAPIBitcoinConnection.get();
			String txref = bitcoinConnection.toTxref(Chain.MAINNET, "016b71d9ec62709656504f1282bb81f7acf998df025e54bd68ea33129d8a425b");
			System.out.println(txref); // expect "tx1:rk63-uqnf-zscg-527"
		}
	}

#### Lookup a TXID by TXREF

	import info.weboftrust.btctxlookup.ChainAndTxid;
	import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinConnection;
	import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
	
	public class Test {
	
		public static void main(String[] args) throws Exception {
	
			BitcoinConnection bitcoinConnection = BlockcypherAPIBitcoinConnection.get();
			ChainAndTxid chainAndTxid = bitcoinConnection.fromTxref("txtest1:xyv2-xzpq-q9wa-p7t");
			System.out.println(chainAndTxid.getChain()); // expect "TESTNET"
			System.out.println(chainAndTxid.getTxid()); // expect "f8cdaff3ebd9e862ed5885f8975489090595abe1470397f79780ead1c7528107"
		}
	}

## About

Rebooting Web-of-Trust - http://www.weboftrust.info/

Markus Sabadello, Danube Tech -  https://danubetech.com/

<br clear="left" />

<img align="left" src="https://raw.githubusercontent.com/peacekeeper/universal-resolver/master/docs/logo-ngi0pet.png" width="115">

Supported by [NLnet](https://nlnet.nl/) and [NGI0 PET](https://nlnet.nl/PET/#NGI), which is made possible with financial support from the European Commission's [Next Generation Internet](https://ngi.eu/) programme.
