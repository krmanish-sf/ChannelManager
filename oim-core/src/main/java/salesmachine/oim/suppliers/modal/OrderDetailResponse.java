package salesmachine.oim.suppliers.modal;

public class OrderDetailResponse {

	public String poNumber;
	public String status;
	

	public OrderDetailResponse() {super();}
	public OrderDetailResponse(String poNumber, String status) {
		this.poNumber = poNumber;
		this.status = status;
	}
	
}
