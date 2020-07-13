package info.weboftrust.btctxlookup.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class UTXOSet {
	private boolean success;
	private long txouts;
	private long height;
	private String bestBlock;
	private List<Unspent> unspents;
	private BigDecimal totalAmount;

	public boolean getSuccess() {
		return success;
	}

	public long getTxouts() {
		return txouts;
	}

	public long getHeight() {
		return height;
	}

	public String getBestBlock() {
		return bestBlock;
	}

	public List<Unspent> getUnspents() {
		return unspents == null ? null : Collections.unmodifiableList(unspents);
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public static class Unspent {
		private String txid;
		private long vout;
		private String scriptPubKey;
		private String desc;
		private BigDecimal amount;
		private long height;

		public Unspent() {
		}

		public String getTxid() {
			return txid;
		}

		public long getVout() {
			return vout;
		}

		public String getScriptPubKey() {
			return scriptPubKey;
		}

		public String getDesc() {
			return desc;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public long getHeight() {
			return height;
		}
	}
}
