package info.weboftrust.btctxlookup;

import info.weboftrust.btctxlookup.Chain;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import junit.framework.TestCase;

public class TxrefConverterTest extends TestCase {

	@Override
	protected void setUp() throws Exception {

	}

	@Override
	protected void tearDown() throws Exception {

	}

	/*
	 * txref encode / decode
	 */

	private static Object[][] tests1 = new Object[][] {

		// mainnet (short form)

		new Object[] { Chain.MAINNET, "tx1:rqqq-qqqq-qygr-lgl", (int) 0x0, (int) 0x0 },
		new Object[] { Chain.MAINNET, "tx1:rqqq-qqll-lceg-dfk", (int) 0x0, (int) 0x7FFF },
		new Object[] { Chain.MAINNET, "tx1:r7ll-llqq-qhgl-lue", (int) 0xFFFFFF, (int) 0x0 },
		new Object[] { Chain.MAINNET, "tx1:r7ll-llll-lte5-das", (int) 0xFFFFFF, (int) 0x7FFF },

		new Object[] { Chain.MAINNET, "tx1:rzqq-qqqq-qhlr-5ct", (int) 1, (int) 0 },
		new Object[] { Chain.MAINNET, "tx1:r7ll-lrgl-ql0m-ykh", (int) 2097151, (int) 1000 },
		new Object[] { Chain.MAINNET, "tx1:r7ll-lrll-8e38-mdl", (int) 2097151, (int) 8191 },
		new Object[] { Chain.MAINNET, "tx1:rk63-uqnf-z08h-t4q", (int) 467883, (int) 2355 },
		new Object[] { Chain.MAINNET, "tx1:r7ll-lrqq-qw4q-a8c", (int) 2097151, (int) 0 },
		new Object[] { Chain.MAINNET, "tx1:rqqq-qqll-8nvy-ezc", (int) 0, (int) 8191 },
		new Object[] { Chain.MAINNET, "tx1:rjk0-uqay-z0u3-gl8", (int) 0x71F69, (int) 0x89D },

		// testnet (short form)

		new Object[] { Chain.TESTNET, "txtest1:xqqq-qqqq-qfqz-92p", (int) 0, (int) 0 },
		new Object[] { Chain.TESTNET, "txtest1:xqqq-qqll-l43f-htg", (int) 0, (int) 0x7FFF },
		new Object[] { Chain.TESTNET, "txtest1:x7ll-llqq-q6q7-978", (int) 0xFFFFFF, (int) 0 },
		new Object[] { Chain.TESTNET, "txtest1:x7ll-llll-lx34-hlw", (int) 0xFFFFFF, (int) 0x7FFF },

		new Object[] { Chain.TESTNET, "txtest1:xk63-uqnf-zz0k-3h7", (int) 467883, (int) 2355 },
		new Object[] { Chain.TESTNET, "txtest1:xyv2-xzpq-q63z-7p4", (int) 1152194, (int) 1 },
		new Object[] { Chain.TESTNET, "txtest1:xz35-jznz-q9yu-ply", (int) 1354001, (int) 83 },
		new Object[] { Chain.TESTNET, "txtest1:xkyt-fzzq-q23l-k4n", (int) 1201739, (int) 2 },
		new Object[] { Chain.TESTNET, "txtest1:xksa-czpq-qxr3-l8k", (int) 1456907, (int) 1 },

		new Object[] { Chain.TESTNET, "txtest1:xjk0-uqay-zz5s-jae", (int) 466793, (int) 2205 },

		// mainnet (extended form)

		new Object[] { Chain.MAINNET, "tx1:yqqq-qqqq-qqqq-f0ng-4y", (int) 0x0, (int) 0x0, (int) 0x0 },
		new Object[] { Chain.MAINNET, "tx1:yqqq-qqll-lqqq-nsg4-4g", (int) 0x0, (int) 0x7FFF, (int) 0x0 },
		new Object[] { Chain.MAINNET, "tx1:y7ll-llqq-qqqq-ztam-5x", (int) 0xFFFFFF, (int) 0x0, (int) 0x0 },
		new Object[] { Chain.MAINNET, "tx1:y7ll-llll-lqqq-c5xx-52", (int) 0xFFFFFF, (int) 0x7FFF, (int) 0x0 },
		new Object[] { Chain.MAINNET, "tx1:yqqq-qqqq-qpqq-td6l-vu", (int) 0x0, (int) 0x0, (int) 0x1 },
		new Object[] { Chain.MAINNET, "tx1:yqqq-qqll-lpqq-3jpz-vs", (int) 0x0, (int) 0x7FFF, (int) 0x1 },
		new Object[] { Chain.MAINNET, "tx1:y7ll-llqq-qpqq-qf5v-d7", (int) 0xFFFFFF, (int) 0, (int) 0x1 },
		new Object[] { Chain.MAINNET, "tx1:y7ll-llll-lpqq-6k03-dj", (int) 0xFFFFFF, (int) 0x7FFF, (int) 0x1 },
		new Object[] { Chain.MAINNET, "tx1:yjk0-uqay-zrfq-h48h-5e", (int) 0x71F69, (int) 0x89D, (int) 0x123 },
		new Object[] { Chain.MAINNET, "tx1:yjk0-uqay-zu4x-vf9r-7x", (int) 0x71F69, (int) 0x89D, (int) 0x1ABC },

		// testnet (extended form)

		new Object[] { Chain.TESTNET, "txtest1:8qqq-qqqq-qqqq-8hur-kr", (int) 0x0, (int) 0x0, (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:8qqq-qqll-lqqq-ag87-k0", (int) 0x0, (int) 0x7FFF, (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:87ll-llqq-qqqq-vnjs-hp", (int) 0xFFFFFF, (int) 0x0, (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:87ll-llll-lqqq-kvfd-hd", (int) 0xFFFFFF, (int) 0x7FFF, (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:8qqq-qqqq-qpqq-9445-0m", (int) 0x0, (int) 0x0, (int) 0x1 },
		new Object[] { Chain.TESTNET, "txtest1:8qqq-qqll-lpqq-l2wf-0h", (int) 0x0, (int) 0x7FFF, (int) 0x1 },
		new Object[] { Chain.TESTNET, "txtest1:87ll-llqq-qpqq-w3m8-we", (int) 0xFFFFFF, (int) 0x0, (int) 0x1 },
		new Object[] { Chain.TESTNET, "txtest1:87ll-llll-lpqq-5wq6-w4", (int) 0xFFFFFF, (int) 0x7FFF, (int) 0x1 },
		new Object[] { Chain.TESTNET, "txtest1:8jk0-uqay-zrfq-edgu-h7", (int) 0x71F69, (int) 0x89D, (int) 0x123 },
		new Object[] { Chain.TESTNET, "txtest1:8jk0-uqay-zu4x-z32g-ap", (int) 0x71F69, (int) 0x89D, (int) 0x1ABC },

		new Object[] { Chain.TESTNET, "txtest1:8z35-jznz-qqqq-e05n-vx", (int) 1354001, (int) 83, (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:8kyt-fzzq-qqqq-z0xs-je", (int) 1201739, (int) 2, (int) 0x0 },
		new Object[] { Chain.TESTNET, "txtest1:8ksa-czpq-qqqq-fctg-6q", (int) 1456907, (int) 1, (int) 0x0 }
	};

	public void testTxrefEncode() throws Exception {

		for (Object[] test : tests1) {

			if (test.length > 4) {

				String result = ChainAndLocationData.txrefEncode((Chain) test[0], (int) test[2], (int) test[3], (int) test[4], true);
				assertEquals((String) test[1], result);
			} else {

				String result1 = ChainAndLocationData.txrefEncode((Chain) test[0], (int) test[2], (int) test[3], 0);
				assertEquals((String) test[1], result1);

				String result2 = ChainAndLocationData.txrefEncode((Chain) test[0], (int) test[2], (int) test[3]);
				assertEquals((String) test[1], result2);
			}

		}
	}

	public void testTxrefDecode() throws Exception {

		for (Object[] test : tests1) {

			String txref = (String) test[1];

			ChainAndLocationData result = ChainAndLocationData.txrefDecode(txref);
			assertEquals((Chain) test[0], result.getChain());
			assertEquals(test[2], result.getLocationData().getBlockHeight());
			assertEquals(test[3], result.getLocationData().getTransactionPosition());

			if (test.length > 4) {

				assertTrue(result.isExtended());

				assertEquals(test[4], result.getLocationData().getTxoIndex());
			} else {

				assertFalse(result.isExtended());
			}
		}
	}


	private static Object[][] tests2 = new Object[][] {

		new Object[] { "tx1:yqqq-qqqq-qqqq-f0ng-4y", "tx1:rqqq-qqqq-qygr-lgl" },
		new Object[] { "tx1:yqqq-qqll-lqqq-nsg4-4g", "tx1:rqqq-qqll-lceg-dfk" },
		new Object[] { "tx1:y7ll-llqq-qqqq-ztam-5x", "tx1:r7ll-llqq-qhgl-lue" },
		new Object[] { "tx1:y7ll-llll-lqqq-c5xx-52", "tx1:r7ll-llll-lte5-das" },
		new Object[] { "tx1:yqqq-qqqq-qpqq-td6l-vu", null },
		new Object[] { "tx1:yqqq-qqll-lpqq-3jpz-vs", null },
		new Object[] { "tx1:y7ll-llqq-qpqq-qf5v-d7", null },
		new Object[] { "tx1:y7ll-llll-lpqq-6k03-dj", null },
		new Object[] { "tx1:yjk0-uqay-zrfq-h48h-5e", null },
		new Object[] { "tx1:yjk0-uqay-zu4x-vf9r-7x", null },

		new Object[] { "txtest1:8qqq-qqqq-qqqq-8hur-kr", "txtest1:xqqq-qqqq-qfqz-92p" },
		new Object[] { "txtest1:8qqq-qqll-lqqq-ag87-k0", "txtest1:xqqq-qqll-l43f-htg" },
		new Object[] { "txtest1:87ll-llqq-qqqq-vnjs-hp", "txtest1:x7ll-llqq-q6q7-978" },
		new Object[] { "txtest1:87ll-llll-lqqq-kvfd-hd", "txtest1:x7ll-llll-lx34-hlw" },
		new Object[] { "txtest1:8qqq-qqqq-qpqq-9445-0m", null },
		new Object[] { "txtest1:8qqq-qqll-lpqq-l2wf-0h", null },
		new Object[] { "txtest1:87ll-llqq-qpqq-w3m8-we", null },
		new Object[] { "txtest1:87ll-llll-lpqq-5wq6-w4", null },
		new Object[] { "txtest1:8jk0-uqay-zrfq-edgu-h7", null },
		new Object[] { "txtest1:8jk0-uqay-zu4x-z32g-ap", null },

		new Object[] { "txtest1:8z35-jznz-qqqq-e05n-vx", "txtest1:xz35-jznz-q9yu-ply" },
		new Object[] { "txtest1:8kyt-fzzq-qqqq-z0xs-je", "txtest1:xkyt-fzzq-q23l-k4n" },
		new Object[] { "txtest1:8ksa-czpq-qqqq-fctg-6q", "txtest1:xksa-czpq-qxr3-l8k" }
	};

	public void testShortExtended() throws Exception {

		for (Object[] test : tests2) {

			String ext = (String) test[0];
			String shrt = (String) test[1];

			ChainAndLocationData extChainAndLocationData = ChainAndLocationData.txrefDecode(ext);
			String txref = ChainAndLocationData.txrefEncode(extChainAndLocationData);

			if (shrt == null) {

				assertTrue(extChainAndLocationData.getLocationData().getTxoIndex() > 0);
				assertEquals(txref, ext);
			} else {

				assertEquals(extChainAndLocationData.getLocationData().getTxoIndex(), 0);
				assertEquals(txref, shrt);
			}
		}
	}
}
