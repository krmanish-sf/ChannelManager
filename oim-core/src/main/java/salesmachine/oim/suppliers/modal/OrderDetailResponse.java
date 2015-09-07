package salesmachine.oim.suppliers.modal;

public class OrderDetailResponse {

	public String poNumber;
	public String status;
	public String wareHouseCode;
	

	public OrderDetailResponse() {super();}
	public OrderDetailResponse(String poNumber, String status,String wareHouseCode) {
		this.poNumber = poNumber;
		this.status = status;
		this.wareHouseCode = wareHouseCode;
	}
	
}
