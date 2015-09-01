package salesmachine.oim.suppliers;
public class PHIHVAData implements Comparable<PHIHVAData> {
	private String STORE_ORDER_ITEM_ID;
	private String DETAIL_ID;
	private String SUPPLIER_ORDER_NUMBER;
	private String STATUS_VALUE;
	private String insertion_tm;
	private String processing_tm;
	private String isConfirmed;
	private String isShipped;
	private String isTracked;
	private String location = "";

	public PHIHVAData(String sTORE_ORDER_ITEM_ID, String dETAIL_ID, String sUPPLIER_ORDER_NUMBER, String sTATUS_VALUE, String insertion_tm,
			String processing_tm, String isConfirmed, String isShipped, String isTracked) {
		super();
		STORE_ORDER_ITEM_ID = sTORE_ORDER_ITEM_ID;
		DETAIL_ID = dETAIL_ID;
		SUPPLIER_ORDER_NUMBER = sUPPLIER_ORDER_NUMBER;
		STATUS_VALUE = sTATUS_VALUE;
		this.insertion_tm = insertion_tm;
		this.processing_tm = processing_tm;
		this.isConfirmed = isConfirmed;
		this.isShipped = isShipped;
		this.isTracked = isTracked;
	}

	public PHIHVAData(String sTORE_ORDER_ITEM_ID, String dETAIL_ID, String sUPPLIER_ORDER_NUMBER, String sTATUS_VALUE, String insertion_tm,
			String processing_tm, String isConfirmed, String isShipped, String isTracked, String location) {
		super();
		STORE_ORDER_ITEM_ID = sTORE_ORDER_ITEM_ID;
		DETAIL_ID = dETAIL_ID;
		SUPPLIER_ORDER_NUMBER = sUPPLIER_ORDER_NUMBER;
		STATUS_VALUE = sTATUS_VALUE;
		this.insertion_tm = insertion_tm;
		this.processing_tm = processing_tm;
		this.isConfirmed = isConfirmed;
		this.isShipped = isShipped;
		this.isTracked = isTracked;
		this.location = location;
	}



	public String getSTORE_ORDER_ITEM_ID() {
		return STORE_ORDER_ITEM_ID;
	}

	public void setSTORE_ORDER_ITEM_ID(String sTORE_ORDER_ITEM_ID) {
		STORE_ORDER_ITEM_ID = sTORE_ORDER_ITEM_ID;
	}

	public String getDETAIL_ID() {
		return DETAIL_ID;
	}

	public void setDETAIL_ID(String dETAIL_ID) {
		DETAIL_ID = dETAIL_ID;
	}

	public String getSUPPLIER_ORDER_NUMBER() {
		return SUPPLIER_ORDER_NUMBER;
	}

	public void setSUPPLIER_ORDER_NUMBER(String sUPPLIER_ORDER_NUMBER) {
		SUPPLIER_ORDER_NUMBER = sUPPLIER_ORDER_NUMBER;
	}

	public String getSTATUS_VALUE() {
		return STATUS_VALUE;
	}

	public void setSTATUS_VALUE(String sTATUS_VALUE) {
		STATUS_VALUE = sTATUS_VALUE;
	}

	public String getInsertion_tm() {
		return insertion_tm;
	}

	public void setInsertion_tm(String insertion_tm) {
		this.insertion_tm = insertion_tm;
	}

	public String getProcessing_tm() {
		return processing_tm;
	}

	public void setProcessing_tm(String processing_tm) {
		this.processing_tm = processing_tm;
	}

	public String getIsConfirmed() {
		return isConfirmed;
	}

	public void setIsConfirmed(String isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	public String getIsShipped() {
		return isShipped;
	}

	public void setIsShipped(String isShipped) {
		this.isShipped = isShipped;
	}

	public String getIsTracked() {
		return isTracked;
	}

	public void setIsTracked(String isTracked) {
		this.isTracked = isTracked;
	}

	public String isLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return STORE_ORDER_ITEM_ID + "," + DETAIL_ID + "," + SUPPLIER_ORDER_NUMBER + "," + STATUS_VALUE + "," + insertion_tm + "," + processing_tm
				+ "," + isConfirmed + "," + isShipped + "," + isTracked + "," + location + "\n";
	}

	@Override
	public int compareTo(PHIHVAData o) {
		return o.location.compareTo(this.location);
	}

}