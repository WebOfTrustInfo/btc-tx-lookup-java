package info.weboftrust.btctxlookup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class AddressRelatedTx {

	private String hex;
	private String txid;
	private String hash;
	private String size;
	private String vsize;
	private String weight;
	private long version;
	private long locktime;
	private List<Vin> vin;
	private List<Vout> vout;
	private String blockhash;
	private long confirmations;
	private long time;
	private long blocktime;

	public AddressRelatedTx() {
	}

	public String getHex() {
		return hex;
	}

	public String getTxid() {
		return txid;
	}

	public String getHash() {
		return hash;
	}

	public String getSize() {
		return size;
	}

	public String getVsize() {
		return vsize;
	}

	public String getWeight() {
		return weight;
	}

	public long getVersion() {
		return version;
	}

	public long getLocktime() {
		return locktime;
	}

	public List<Vin> getVin() {
		return vin == null ? null : Collections.unmodifiableList(vin);
	}

	public List<Vout> getVout() {
		return vout == null ? null : Collections.unmodifiableList(vout);
	}

	public String getBlockhash() {
		return blockhash;
	}

	public long getConfirmations() {
		return confirmations;
	}

	public long getTime() {
		return time;
	}

	public long getBlocktime() {
		return blocktime;
	}

	public static class Vin {

		private String txid;
		private long vout;
		private ScriptSig scriptSig;
		private PrevOut prevOut;
		private long sequence;

		public Vin() {
		}

		public String getTxid() {
			return txid;
		}

		public long getVout() {
			return vout;
		}

		public ScriptSig getScriptSig() {
			return scriptSig;
		}

		public PrevOut getPrevOut() {
			return prevOut;
		}

		public long getSequence() {
			return sequence;
		}

	}

	public static class PrevOut {
		private List<String> addresses;
		private BigDecimal value;

		public PrevOut() {
		}

		public List<String> getAddresses() {
			return Collections.unmodifiableList(addresses);
		}

		public BigDecimal getValue() {
			return value;
		}
	}

	public static class ScriptSig {
		private String asm;
		private String hex;

		public ScriptSig() {
		}

		public String getASM() {
			return asm;
		}

		public String getHex() {
			return hex;
		}
	}

	public static class Vout {
		private BigDecimal value;
		@JsonProperty("n")
		private long outIndex;
		private ScriptPubKey scriptPubKey;

		public Vout() {
		}

		public BigDecimal getValue() {
			return value;
		}

		public long getOutIndex() {
			return outIndex;
		}

		public ScriptPubKey getScriptPubKey() {
			return scriptPubKey;
		}
	}

	public static class ScriptPubKey {
		private String asm;
		private String hex;
		private String type;
		private Long reqSigs;
		private List<String> addresses;

		public ScriptPubKey() {
		}

		public String getASM() {
			return asm;
		}

		public String getHex() {
			return hex;
		}

		public String getType() {
			return type;
		}

		public Long getReqSigs() {
			return reqSigs;
		}

		public List<String> getAddresses() {
			return addresses == null ? null : Collections.unmodifiableList(addresses);
		}

	}
}