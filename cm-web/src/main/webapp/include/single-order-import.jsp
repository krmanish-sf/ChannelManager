<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>
<div class="modal fade" id="singleordermodal" tabindex="-1"
	role="dialog" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="modalTitle">Single Order Entry</h4>
			</div>
			<div class="modal-body ">
				<div class="row">
					<div class="container">
						<form id="singleOrderForm" class="form-horizontal" role="form"
							novalidate="novalidate">
							<div>
								<h4 class="lighter">Order Details</h4>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Order
										Date</label>
									<div class="col-sm-7">
										<input type="date" id="order_date" name="order_date"
											required="required" placeholder="mm/dd/yyyy"
											class="datepicker" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Store
										Order Id</label>
									<div class="col-sm-7">
										<input type="text" id="store_order_id" required="required"
											name="store_order_id" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Shipping
										Details</label>
									<div class="col-sm-7">
										<input type="text" id="shipping_details"
											name="shipping_details" required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Pay
										Method</label>
									<div class="col-sm-7">
										<input type="text" id="PAY_METHOD" name="PAY_METHOD"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Order
										Comment</label>
									<div class="col-sm-7">
										<textarea id="ORDER_COMMENT" name="ORDER_COMMENT"></textarea>
									</div>
								</div>
							</div>
							<div id="CUSTOMER">
								<h4 class="lighter">Customer Details</h4>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Name</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_NAME" name="CUSTOMER_NAME"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Street
										Address</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_STREET_ADDRESS"
											name="CUSTOMER_STREET_ADDRESS" required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Suburb</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_SUBURB" name="CUSTOMER_SUBURB"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">City</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_CITY" name="CUSTOMER_CITY"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">State/Province</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_STATE" name="CUSTOMER_STATE"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Country</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_COUNTRY"
											name="CUSTOMER_COUNTRY" required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Zip</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_ZIP" name="CUSTOMER_ZIP" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Company</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_COMPANY"
											name="CUSTOMER_COMPANY" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Phone</label>
									<div class="col-sm-7">
										<input type="text" id="CUSTOMER_PHONE" name="CUSTOMER_PHONE"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Email</label>
									<div class="col-sm-7">
										<input type="email" id="CUSTOMER_EMAIL" name="CUSTOMER_EMAIL"
											required="required" />
									</div>
								</div>
							</div>
							<div id="SHIPPING">
								<h4 class="lighter">Shipping Details</h4>
								<input type="checkbox" id="customer_shipping"><label
									for="customer_shipping">same as customer</label>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Name</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_NAME" name="DELIVERY_NAME"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Street
										Address</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_STREET_ADDRESS"
											name="DELIVERY_STREET_ADDRESS" required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Suburb</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_SUBURB" name="DELIVERY_SUBURB"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">City</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_CITY" name="DELIVERY_CITY"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">State</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_STATE" name="DELIVERY_STATE"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Country</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_COUNTRY"
											name="DELIVERY_COUNTRY" required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Zip</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_ZIP" name="DELIVERY_ZIP"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Company</label>
									<div class="col-sm-7">
										<input type="text" id="DELIVERY_COMPANY"
											name="DELIVERY_COMPANY" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Phone</label>
									<div class="col-sm-7">
										<input type="tel" id="DELIVERY_PHONE" name="DELIVERY_PHONE"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Email</label>
									<div class="col-sm-7">
										<input type="email" id="DELIVERY_EMAIL" name="DELIVERY_EMAIL"
											required="required" />
									</div>
								</div>
							</div>
							<div id="BILLING">
								<h4 class="lighter">Billing Details</h4>
								<input type="checkbox" id="customer_billing" /><label
									for="customer_billing">same as customer</label> <input
									type="checkbox" id="shipping_billing" /><label
									for="shipping_billing">same as shipping</label>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Name</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_NAME" name="BILLING_NAME"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Street
										Address</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_STREET_ADDRESS"
											name="BILLING_STREET_ADDRESS" required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Suburb</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_SUBURB" name="BILLING_SUBURB"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">City</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_CITY" name="BILLING_CITY"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">State</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_STATE" name="BILLING_STATE"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Country</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_COUNTRY" name="BILLING_COUNTRY"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Zip</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_ZIP" name="BILLING_ZIP"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Company</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_COMPANY" name="BILLING_COMPANY" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Phone</label>
									<div class="col-sm-7">
										<input type="text" id="BILLING_PHONE" name="BILLING_PHONE"
											required="required" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-5 control-label no-padding-right">Email</label>
									<div class="col-sm-7">
										<input type="email" id="BILLING_EMAIL" name="BILLING_EMAIL"
											required="required" />
									</div>
								</div>
							</div>
							<div>
								<h4 class="lighter">Products Detail</h4>
								<table id="prod_table"
									class="table table-bordered table-striped dataTable">
									<tbody>
										<tr id="product_row">
											<td class="form-horizontal">
												<div class="form-group">
													<label class="col-sm-5 control-label no-padding-right">SKU
														*</label>
													<div class="col-sm-7">
														<input type="text" value="" name="sku" class="width-70"
															required="required">
													</div>
												</div>
												<div class="form-group">
													<label class="col-sm-5 control-label no-padding-right">Name</label>
													<div class="col-sm-7">
														<input type="text" value="" name="name" class="width-70">
													</div>
												</div>
												<div class="form-group">
													<label class="col-sm-5 control-label no-padding-right">Quantity
														*</label>
													<div class="col-sm-7">
														<input type="text" value="" name="quantity"
															class="width-70" required="required">
													</div>
												</div>
												<div class="form-group">
													<label class="col-sm-5 control-label no-padding-right">Sale-Price
														*</label>
													<div class="col-sm-7">
														<input type="text" value="" name="saleprice"
															class="width-70" required="required">
													</div>
												</div>
												<div class="form-group">
													<label class="col-sm-5 control-label no-padding-right">Cost-Price</label>
													<div class="col-sm-7">
														<input type="text" value="" name="costprice"
															class="width-70">
													</div>
												</div>
												<div class="form-group">
													<label class="col-sm-5 control-label no-padding-right">Supplier</label>
													<div class="col-sm-7">
														<!-- <input type="text" value="" name="supplier"
															class="width-70"> -->
														<select name="supplier" class="width-70"></select>
													</div>
												</div>
												<div class="form-group">
													<label class="col-sm-5 control-label no-padding-right">Status</label>
													<div class="col-sm-7">
														<select name="status">
															<option value="0">Unprocessed</option>
															<option value="2">Processed</option>
															<option value="3">Failed</option>
															<option value="100">Unconfirmed</option>
															<option value="5">Manually Processed</option>
															<option value="6">Cancelled</option>
														</select>
														<button class="btn btn-warning btn-sm btn-delete-product"
															type="button" style="display: none;">
															<i class="icon-trash "></i>Delete Product
														</button>
													</div>
												</div>
											</td>
										</tr>
									</tbody>
								</table>
							</div>

							<div class="form-group center">
								<small> *&nbsp; required </small>
								<button class="btn btn-info btn-sm" type="button"
									id="add_product_row">
									<i class="icon-ok "></i>Add Product
								</button>
								<button class="btn btn-info btn-sm" type="button"
									id="btnSaveOrder">
									<i class="icon-save "></i>Save
								</button>
							</div>
						</form>
					</div>
				</div>
			</div>

			<!-- PAGE CONTENT BEGINS -->

		</div>
	</div>
</div>
