package salesmachine.oim.suppliers.modal;

public class OrderDetailResponse {

  private final String poNumber;
  private final String status;
  private final String wareHouseCode;

  @Deprecated
  public OrderDetailResponse() {
    this(null, null, null);
  }

  public OrderDetailResponse(String poNumber, String status, String wareHouseCode) {
    this.poNumber = poNumber;
    this.status = status;
    this.wareHouseCode = wareHouseCode;
  }

  public String getPoNumber() {
    return poNumber;
  }

  public String getStatus() {
    return status;
  }

  public String getWareHouseCode() {
    return wareHouseCode;
  }

}
