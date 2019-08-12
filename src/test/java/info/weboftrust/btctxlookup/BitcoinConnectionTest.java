package info.weboftrust.btctxlookup;

import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoindRPCBitcoinConnection;
import junit.framework.TestCase;

public class BitcoinConnectionTest extends TestCase {

	private static BitcoinConnection bitcoinConnection;

	static {

		bitcoinConnection = new BitcoindRPCBitcoinConnection();
	}

	@Override
	protected void setUp() throws Exception {

	}

	@Override
	protected void tearDown() throws Exception {

	}

	/*
	 * BitcoinConnection
	 */

	private static Object[][] tests = new Object[][] {
		new Object[] { Chain.TESTNET, "txtest1:xz35-jznz-q6mr-7q6", "2f1838f481be7b4f4d37542a751aa3a27be7114f798feb24ff0fc764730973d0", (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:xkyt-fzzq-q4wq-f2d", "67c0ee676221d9e0e08b98a55a8bf8add9cba854f13dda393e38ffa1b982b833", (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:xksa-czpq-qeuw-qcg", "eac139503dddaeeed8d8a169b0ae2d893c355ee610bf95eb0317a1eb86757af3", (int) 0x0 },
		new Object[] { Chain.MAINNET, "tx1:rk63-uqnf-zscg-527", "016b71d9ec62709656504f1282bb81f7acf998df025e54bd68ea33129d8a425b", (int) 0x0 }
	};

	public void testToTxref() throws Exception {

		for (Object[] test : tests) {

			ChainAndTxid result;

			if (test.length > 3) 
				result = new ChainAndTxid((Chain) test[0], (String) test[2], (int) test[3]);
			else
				result = new ChainAndTxid((Chain) test[0], (String) test[2]);

			String txref = bitcoinConnection.toTxref(result);
			assertEquals((String) test[1], txref);
		}
	}

	public void testFromTxref() throws Exception {

		for (Object[] test : tests) {

			String txref = (String) test[1];

			ChainAndTxid result = bitcoinConnection.fromTxref(txref);
			assertEquals((Chain) test[0], result.getChain());
			assertEquals((String) test[2], result.getTxid());

			if (test.length > 3) assertEquals((int) test[3], result.getTxoIndex());
		}
	}
}
