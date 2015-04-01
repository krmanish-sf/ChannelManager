package salesmachine.oim.stores.modal.shop.order.status;

public enum OrderStatus {
	Item_backordered(101), Item_returned(103), Item_shipped(106), Item_out_of_stock(
			107), Item_removed_from_invoice(118), Shipping_or_handling_charges_changed(
			200), Cancel_Invoice(306), Cancel_invoice_before_shipment(307), Invoice_shipped(
			400), Order_received_by_seller(700);
	private int status;

	OrderStatus(int status) {
		this.status = status;
	}

	public String getValue() {
		return String.valueOf(status);
	}
}
