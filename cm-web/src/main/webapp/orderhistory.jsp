<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
	<div class="main-content">
				<div id="breadcrumbs" class="breadcrumbs">
					<ul class="breadcrumb">
						<li><a href="index.jsp"><i class="icon-home home-icon"></i>Home</a>
						</li>
						<li class="active">Order History</li>
					</ul>
				</div>
		<div class="page-content">
		<div class="page-header">
				<h1>
							Order History<small> <i class="icon-double-angle-right"></i>
								overview &amp; stats
							</small>
						</h1>
					</div>
		<div class="row">
							<div class="col-xs-12">
<div class="widget-box transparent">
	<div class="widget-header">
                  <div class="widget-toolbar pull-left">
                    <ul id="recent-tab" class="nav nav-tabs">
                      <li class="active"> <a href="#tabs-1"
											class="red" data-toggle="tab">Basic Search</a> </li>
                      <li> <a href="#tabs-2" class="green"
											data-toggle="tab">Advanced Search</a> </li>
                    </ul>
                  </div>
                </div>
                <div class="widget-main no-padding">
   	<div class="tab-content padding-8 overflow-visible">
	<div id="tabs-1" class="tab-pane active">
		<table class="table-responsive">
			<tr>
				<td>Supplier</td>
				<td>
					<select multiple="multiple" size="4" name="supplier" id="suppliers">
					</select>
				</td>
				<td>Channel</td>
				<td>
					<select multiple="multiple" size="4" name="channel" id="channels">
					</select>
				</td>
				<td>Shipping</td>
				<td>
					<select multiple="multiple" size="4" name="shipping" id="shipping">
						<option value="usps">USPS</option>
						<option value="ups">UPS</option>
					</select>
				</td>
				
			</tr>
			<tr>
				<td>From</td>
				<td>
    <input type="text" id="datefrom" name="datefrom" size="16"
													class="span2 datepicker" placeholder="mm/dd/yyyy">
    														 
				</td>
				<td>To</td>
				<td>
    <input type="text" id="dateto" name="dateto" size="16"
													class="span2 datepicker" placeholder="mm/dd/yyyy">
    			</td>
			<td>Order Status</td>
				<td>
					<select multiple="multiple" style="width: 150px;" size="4"
													name="order_status" id="order_status">
					</select>
				</td>
			</tr>
		</table>
		<div id="date_edit_container" style="display: none">
    		<strong>Select Date:</strong><br>
    		<div id="datepicker"></div>
    	</div>		
	</div>
	<div id="tabs-2" class="tab-pane">
		<table class="table-responsive">
			<tr>
				<td>Customer Name</td>
				<td>
					<input type="text" name="customer_name" value="">
				</td>
				<td>Customer Email</td>
				<td>
					<input type="text" name="customer_email" value="">
				</td>
				<td>Customer Address</td>
				<td>
					<input type="text" name="customer_address" value="">
				</td>
			
			</tr>
			<tr>
				<td>Customer Phone</td>
				<td>
					<input type="text" name="customer_phone" value="">
				</td>
				<td>Order Total &gt;</td>
				<td>
					<input type="text" name="order_total_min" value="">
				</td>
				<td>Order Total &lt; </td>
				<td>
					<input type="text" name="order_total_max" value="">
				</td>
				
			</tr>
			<tr>
				<td>Order ID</td>
				<td>
					<input type="text" name="order_id" value="">
				</td>
											<td>Product SKU</td>
				<td>
					<input type="text" name="sku" value="">
				</td>
					<td>Customer Zip</td>
				<td>
					<input type="text" name="customer_zip" value="">
				</td>
			</tr>
		</table>		
	</div>
	</div>
	</div>
<div class="col-xs-12">
	<button type="button" id="searchBtn" class="btn btn-info">
	<i class="icon-ok bigger-110"></i>Search</button>
	<button type="reset" class="btn" id="clearBtn">
	<i class="icon-undo bigger-110"></i>Reset</button>
		</div>
	</div>
	</div>
	<div class="col-sm-12">
                <h4 class="lighter pull-left"> <i
								class="icon-time orange"></i> Your Order History</h4>
    </div>
    <!-- /widget-main -->
	<div class="col-xs-12">
								<div class="space-2"></div>
								<div class="container">
                  <div class="col-sm-2">
                    	<select id="processselect1"
									class=" width-100 pull-left">
						<option value="0">With selected</option>
                      	<option value="track">Recheck Orders</option>
                      	<option value="re-process">Resubmit Orders</option>
                    	</select>
                  	</div>
                  	<div class="col-sm-3">
										<a id="btnUpdateBulk" class="btn btn-info pull-left"
									href="javascript:;">Update Selected Orders</a>
					</div>
					<div class="col-sm-7">
		            	<div class="alert alert-block alert-warning">
		                	<i
										class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
		                	Please make sure to edit the <strong>PO number</strong> according to supplier requirements in-order to avoid re-processing errors.
		              	</div>
              		</div>
                </div>
                <div class="space-2"></div>
								<div class="widget-main no-padding">
								<table id="tableprocesschannel1"
								class="table table-bordered table-striped  table-responsive dataTable">
											<thead class="thin-border-bottom">
												<tr role="row">
												<th class="center sorting_disabled sorting_asc"
											role="columnheader" tabindex="0"
											aria-controls="tableprocesschannel1" rowspan="1" colspan="1"
											style="width: 18px;" aria-sort="ascending"
											aria-label=": activate to sort column descending"><label>
                                <input type="checkbox" class="ace">
                                <span class="lbl"></span>
															</label>
                            </th>
													<th><i
											class="icon-sort-by-order-alt icon-2x visible-xs"></i><span
											class="hidden-xs visible-sm">Order Id</span></th>
												<th>Order Status</th>
													<th class="hidden-xs" role="columnheader">Order Date</th>
													<th class="sorting" role="columnheader"><i
											class="icon-home icon-2x blue visible-xs"></i><span
											class="hidden-xs">Customer</span></th>
													<th class="hidden-sm hidden-xs">Channel
														Name</th>
												<th class="hidden-sm hidden-xs sorting"><span>Shipping</span></th>
												<th><i class="icon-usd icon-2x blue visible-xs"></i><span
											class="hidden-xs">Order Total</span></th>
												<th class="hidden-xs">Edit Order</th>
												</tr>
											</thead>
									</table>
									<div aria-hidden="true" role="dialog" tabindex="-1"
								id="myModalResolve" class="modal fade">
										<div id="myModalResolvecontent" class="modal-dialog">
											<div class="modal-content">
												<div class="modal-header">
													<button aria-hidden="true" data-dismiss="modal"
												class="close" type="button">&times;</button>
													<h4 class="modal-title">Detail</h4>
												</div>
												<div id="processmodel" class="modal-body ">
													<div class="row">
														<div class="col-xs-12">
															<!-- PAGE CONTENT BEGINS -->
															<table id="tableorderdetails"
														class="table table-striped table-bordered table-hover table-responsive">
																<thead>
																	<tr>
																		<th>Sku</th>
																		<th>Name</th>
																		<th>Quantity</th>
																		<th>Sale Price</th>
																		<th>Status</th>
																	</tr>
																</thead>
																<tbody>
																	
																</tbody>
															</table>
														</div>
														<!-- PAGE CONTENT ENDS -->
													</div>
													<!-- /.col -->
												</div>
											</div>
										</div>
										<!-- /.modal-content -->
									</div>
									<!-- / mobile.modal -->
									<!-- <div aria-hidden="true" role="dialog" tabindex="-1"
								id="myModalResolvemob" class="modal fade">
										<div class="modal-dialog">
											<div class="modal-content">
												<div class="modal-header">
													<button aria-hidden="true" data-dismiss="modal"
												class="close" type="button">&times;</button>
													<h4 class="modal-title">Detail</h4>
												</div>
												<div class="modal-body">
													<div class="row">
														<div id="processmodelM" class="col-xs-12 ohistory">
															<div class="container">
																<label class="col-sm-3 control-label no-padding-left">
																	Sku:</label> <br> <input type="text" value=" NV8083538"
															class="col-xs-10 col-sm-9" readonly="">
															</div>
															<div class="container">
																<label class="col-sm-3 control-label no-padding-left">Name:</label>
																<br> <input type="text" value="Ravish Test Product"
															readonly="" class="col-xs-10 col-sm-5">
															</div>
															<div class="container">
																<label class="col-sm-3 control-label no-padding-left">Quantity:</label>
																<br> <input type="text" class="col-xs-10 col-sm-5"
															readonly="" value="4">
															</div>
															<div class="container">
																<label class="col-sm-3 control-label no-padding-left">Default:</label>
																<br> <input type="text" value="Unprocessed"
															readonly="" class="col-xs-10 col-sm-5" name="Status">
																<br>
															</div>
															<div class="hr hr-20"></div>
														</div>
													</div>

													PAGE CONTENT ENDS
												</div>
												/.col
											</div>
										</div>
									</div> -->
									<!-- /.modal-content -->
									<div aria-hidden="true" role="dialog" tabindex="-1"
								id="myModaledit" class="modal fade">
                  <div class="modal-dialog" id="myModaleditdailog">
                    <div class="modal-content">
                      <div class="modal-header">
                        <button aria-hidden="true" data-dismiss="modal"
												class="close" type="button">&times;</button>
                        <h4 class="modal-title">Order Detail: Order Id# <span
													data-bind-order="storeOrderId"></span> </h4>
                      </div>
                      <div class="modal-body ">
                        <div class="row">
                          <div class="col-sm-12">
                            <form role="form" class="form-horizontal">
                              <div class="row">
                                <div class="col-sm-4">
                                  <label>Bill No</label>
                                  <input type="text"
																	data-bind-order="orderId" readonly="readonly"
																	class="pull-right">
                                </div>
                                <div class="col-sm-4">
                                  <label>Order Date</label>
                                  <input type="text" id="orderdate"
																	data-bind-order="orderTmString" readonly="readonly"
																	class="pull-right" name="orderdate">
                                </div> <div class="col-sm-4">
                                  <label>Total Amount</label>
                                  <input type="text" id="ordertotalamt"
																	data-bind-order="orderTotalAmount" class="pull-right"
																	maxlength="50" name="ordertotalamt">
                                </div>
                                
                              </div>
                               <div class="space-4"></div>
                              <div class="row">
                                <div class="col-sm-4">
                                  <label>Shipping Details</label>
                                  <input type="text"
																	id="shippingdetails" data-bind-order="shippingDetails"
																	class="pull-right" maxlength="500"
																	name="shippingdetails">
                                </div>
                                <div class="col-sm-4">
                                  <label>Payment Method</label>
                                  <input type="text" id="paymentmethod"
																	data-bind-order="payMethod" class="pull-right"
																	maxlength="500" name="paymentmethod">
                                </div>
                                <div class="col-sm-4">
                                  <label>Comments</label>
                                  <input type="text" id="ordercomment"
																	data-bind-order="orderComment" maxlength="1000"
																	class="pull-right width-56" name="ordercomment">
                                </div>
                              </div>
                              <div class="space-4"></div>
                              <fieldset>
                              <legend>Delivery</legend>
                           
                              <div class="row">
                              <div class="col-sm-4">
                                  <label>Address Line 1</label>
                                  <input type="text"
																		id="deliverystreetadd"
																		data-bind-order="deliveryStreetAddress"
																		class="pull-right" maxlength="200"
																		name="deliverystreetadd">
                                </div>
                                <div class="col-sm-4">
                                  <label>Address Line 2</label>
                                  <input type="text" id="deliverysuburb"
																		data-bind-order="deliverySuburb" maxlength="20"
																		class="pull-right" name="deliverysuburb">
                                </div>
                                
                                <div class="col-sm-4">
                                  <label>Company</label>
                                  <input type="text"
																		id="deliverycompany" data-bind-order="deliveryCompany"
																		class="pull-right" maxlength="100"
																		name="deliverycompany">
                                </div>
                              </div>
                              <div class="space-4"></div>
                              <div class="row">
                                <div class="col-sm-4">
                                  <label>City</label>
                                  <input type="text" id="deliverycity"
																		data-bind-order="deliveryCity" class="pull-right"
																		maxlength="50" name="deliverycity">
                                </div>
                                <div class="col-sm-4">
                                  <label>State/Province</label>
                                  <input type="text" id="deliverystate"
																		data-bind-order="deliveryState" class="pull-right"
																		maxlength="20" name="deliverystate">
                                </div>
                                <div class="col-sm-4">
                                  <label>Country</label>
                                  <input type="text"
																		id="deliverycountry" data-bind-order="deliveryCountry"
																		class="pull-right" maxlength="20"
																		name="deliverycountry">
                                </div>
                              </div>
                              <div class="space-4"></div>
                              <div class="row">
                                <div class="col-sm-4">
                                  <label>Zip</label>
                                  <input type="text" id="deliveryzip"
																		data-bind-order="deliveryZip" class="pull-right"
																		maxlength="20" name="deliveryzip">
                                </div>
                                <div class="col-sm-4">
                                  <label>Phone</label>
                                  <input type="text" id="deliveryphone"
																		data-bind-order="deliveryPhone" class="pull-right"
																		maxlength="20" name="deliveryphone">
                                </div>
                                <div class="col-sm-4">
                                  <label>Email</label>
                                  <input type="text" id="deliveryemail"
																		data-bind-order="deliveryEmail" class="pull-right"
																		maxlength="100" name="deliveryemail">
                                </div>
                              </div>
                              
                              </fieldset>
                              
                             
                              <div class="space-4"></div>
                              <div class="row">
                              <div class="col-sm-4">
                                  <label>Delivery Name</label>
                                  <input type="text" id="deliveryname"
																	data-bind-order="deliveryName" maxlength="200"
																	class="pull-right" name="deliveryname">
                                </div>
                               
                                <div class="col-md-4">
                                  <button type="button" id="updateorder"
																	class="btn btn-info btn-xs pull-left"> <i
																		class="icon-ok "></i>Update</button>
                                </div>
                              </div>
                            </form>
                            <!-- PAGE CONTENT BEGINS -->
                            
                            <div class="row">
                              <div class="col-sm-12">
                                <h4 class="modal-title">Product(s)</h4>
                                <div class="hr hr-2"></div>
                                <div class="space-4"></div>
                              </div>
                            </div>
                            <table id="editordermodaltable"
														class="table table-striped table-bordered table-hover table-responsive">
                              <thead>
                                <tr>
                                  <th>Sku</th>
                                  <th>Name</th>
                                  <th>Qty</th>
                                  <th>PO Number</th>
                                  <th>Supplier</th>
                                  <th>Status</th>
                                  <th></th>
                                  <th></th>
                                </tr>
                              </thead>
                              <tbody>
                              </tbody>
                            </table>
                          </div>
                        </div>
                      </div>
                    </div>
                    <!-- PAGE CONTENT ENDS --> 
                  </div>
                  <!-- /.col --> 
                </div>
                
                <!--  Tracking Modal Start -->
                <div aria-hidden="true" role="dialog" tabindex="-1"
								id="TrackingModal" class="modal fade">
								<div class="modal-dialog" id="myModaleditdailog">
					<div class="modal-content">
                      <div class="modal-header">
                        <button aria-hidden="true" data-dismiss="modal"
												class="close" type="button">&times;</button>
                        <h4 class="modal-title">Manual Tracking</h4>
                      </div>
                      <div class="modal-body ">
                       <form role="form" id="trackingForm"
												class="form-horizontal">
                      	 <table id="editOrderTrackingTable"
													class="table table-striped table-bordered table-hover table-responsive">
                              <thead>
                                <tr>
                                <th><i class="icon-plus-sign"
																title="Add" onclick="addRow();"></i></th>
                                 
                                  <th>Shipping Carrier</th>
                                  <th>Shipping Method</th>
                                  <th>Shipping Date</th>
                                  <th>Shipping Quantity</th>
                                  <th>Tracking Number</th>
                                </tr>
                              </thead>
                              <tbody>
                              </tbody>
                            </table>
                            </form>
                      </div>
                       <div class="modal-footer no-margin-top">
                       <button type="button" id="updateTracking"
												class="btn btn-info btn-xs pull-right"> <i
													class="icon-ok "></i>Update</button>
                      </div>
                      </div>
                      </div>
                
                </div>
                <!-- Tracking Modal End -->
								</div>
							</div>
						<!-- /.modal-content -->
						</div>
		</div>
	</div>		
	<!-- /.page-content -->
	
	<div aria-hidden="true" role="dialog" tabindex="-1"
			id="myModalOrderMods" class="modal fade">
										<div class="modal-dialog" style="width: 60%;">
											<div class="modal-content">
												<div class="modal-header">
													<button aria-hidden="true" data-dismiss="modal"
							class="close" type="button">&times;</button>
													<h4 class="modal-title">Order Audit Trail</h4>
												</div>
												<div class="modal-body ">
													<div class="row">
														<div class="col-xs-12">
															<!-- PAGE CONTENT BEGINS -->
															<table id="tableordermods" class="table">
																<thead>
																	<tr>
																		<th>Date</th>
																		<th>Status</th>
																		<th>Comment</th>
																		<th>Sku</th>
																		<th>PO Num</th>
																		<th>Supplier</th>
																		<th>Qty</th>
																		<th>Processing Date</th>
																		<th>Sale Price</th>
																	</tr>
																</thead>
																<tbody>
																</tbody>
															</table>
														</div>
													</div>
												</div>
											</div>
										</div>
									</div>
	</jsp:attribute>
	<jsp:attribute name="pagejs">
	<!-- inline scripts related to this page -->
	<script type="text/javascript">
		var table_xy = null;
		var MY_SUPPLIERS = null;
		function a(e) {
			var order = table_xy.row(e[0]).data();
			$('#tableorderdetails')
					.DataTable(
							{
								"aoColumns" : [
										{
											"mData" : "sku"
										},
										{
											"mData" : "productName"
										},
										{
											"mData" : "quantity"
										},
										{
											"mData" : "salePrice"
										},
										{
											"mData" : function(orderDetail) {
												return orderDetail.oimOrderStatuses == null ? ''
														: orderDetail.oimOrderStatuses.statusValue;
											}
										} ],
								"aaData" : order.oimOrderDetailses,
								"bDestroy" : true
							});
		}
		function b(e) {
			var orderDetail = tableModal.row(e[0]).data();
			updateOrderDetail(orderDetail, $(e[0]).find('input')[0], $(e[0])
					.find('input')[1], $(e[0]).find('input')[2], $(e[0]).find(
					'input')[3], $(e[0]).find('select')[0], $(e[0]).find(
					'select')[1], e[0]);
		}
		function addRow() {
			var table = $('#editOrderTrackingTable').DataTable();
			$('#editOrderTrackingTable').dataTable().fnAddData(
					[ '', '', '', '', '', '', '' ]);

		}
		function removeRow(r) {
			console.log(r);
			var table = $('#editOrderTrackingTable').DataTable();
			table.row(r).remove().draw();
		}
		function validateForm() {
			var str;
			$("#trackingForm :input").each(function() {
				if (this.value == '' || this.value == 'undefined') {
					str += this.name + ', ';
				}
			});
			if (str && str.length > 0) {
				$.gritter.add({
					title : "Error!!",
					text : "These fields can not be empty - " + str
				});
				return false;
			}
			return true;
		}

		function openTrackingData(e) {
			var orderDetail = tableModal.row(e[0]).data();
			var orderDetailTemp = JSON.parse(JSON.stringify(orderDetail));
			console.log(orderDetailTemp.orderTrackings);
			$('#editOrderTrackingTable')
					.DataTable(
							{
								"bFilter" : false,
								"bSort" : false,
								"bPaginate" : false,
								"bInfo" : false,
								"aoColumns" : [
										{
											"mData" : function() {
												return "<span><i class='icon-trash' title='Remove this row' onclick='removeRow($(this).parent().parent().parent()	);'></i></span>";
											}
										},
										{
											"mData" : function(orderTracking,
													row, a, b) {
												return "<input type=text class='width-100' name='shippingCarrier"
														+ b.row
														+ "' value='"
														+ (orderTracking.shippingCarrier ? orderTracking.shippingCarrier
																: '')
														+ "' required/> <input type='hidden' name='detailId' value='"+orderDetailTemp.detailId+"' /> <input type='hidden' name='trackingId"+b.row+"' value='"+orderTracking.orderTrackingId+"' />";
											}
										},
										{
											"mData" : function(orderTracking,
													row, a, b) {
												return "<input type=text class=width-100 name='shippingMethod"
														+ b.row
														+ "' value='"
														+ (orderTracking.shippingMethod ? orderTracking.shippingMethod
																: '')
														+ "' required/>";
											}
										},
										{
											"mData" : function(orderTracking,
													row, a, b) {
												return "<input type=text id='datefrom' class='span2 datepicker' data-provide='datepicker' placeholder='mm/dd/yyyy' name='shipDate"
														+ b.row
														+ "' value='"
														+ (orderTracking.shipDateString ? orderTracking.shipDateString
																: '')
														+ "' required/>";
											}
										},
										{
											"mData" : function(orderTracking,
													row, a, b) {
												return "<input type=text class=width-100 name='shipQuantity"
														+ b.row
														+ "' value='"
														+ (orderTracking.shipQuantity ? orderTracking.shipQuantity
																: '')
														+ "' required/>";

											}
										},
										{
											"mData" : function(orderTracking,
													row, a, b) {
												return "<input type=text class=width-100 name='trackingNumber"
														+ b.row
														+ "' value='"
														+ (orderTracking.trackingNumber ? orderTracking.trackingNumber
																: '')
														+ "' required/>";
											}
										} ],
								"aaData" : orderDetailTemp.orderTrackings,
								"bDestroy" : true
							});

		}

		function updateOrderDetail(orderDetail, sku, name, quantity, saleprice,
				supplier, status, button) {
			if (!sku.value) {
				alert('SKU is required');
				sku.focus();
				return false;
			}
			orderDetail.sku = sku.value;

			orderDetail.productName = $(name).val();
			if (!quantity.value) {
				alert('Quantity is required');
				quantity.focus();
				return false;
			}
			if (quantity.value <= 0) {
				alert('Quantity must be positive');
				quantity.focus();
				return false;
			}
			orderDetail.quantity = Math.round(quantity.value);
			orderDetail.salePrice = $(saleprice).val();
			if (!saleprice.value) {
				alert('Saleprice is required');
				saleprice.focus();
				return false;
			}

			if (saleprice.value <= 0) {
				alert('Saleprice must be more than zero.');
				saleprice.focus();
				return false;
			}
			orderDetail.oimOrderStatuses.statusId = $(status).val();
			orderDetail.oimOrderStatuses.statusValue = $(status).text();
			if ($(supplier).val()) {
				orderDetail.oimSuppliers = {};
				orderDetail.oimSuppliers.supplierId = $(supplier).val();
			} else {
				orderDetail.oimSuppliers = null;
			}

			$(button).CRUD(
					{
						method : "PUT",
						url : 'aggregators/orders/orderdetails/'
								+ orderDetail.detailId,
						data : JSON.stringify(orderDetail),
						success : function(data, textStatus, jqXHR) {
							$.gritter.add({
								title : "Update Order Detail",
								text : "Order Detail updated successfully."
							});
							table_xy.ajax.reload();
							getAlerts();
							$('#myModaledit').modal('hide');
						},
						error : function(data, textStatus, jqXHR) {
							$.gritter.add({
								title : "Update Order Detail",
								text : "Error in updating order detail."
							});
						}
					});
		}
		function c(e) {
			var order = table_xy.row(e[0]).data();
			var orderTemp = JSON.parse(JSON.stringify(order));
			tableModal = $('#editordermodaltable')
					.DataTable(
							{
								bSort : false,
								"aoColumns" : [
										{
											"mData" : function(orderDetail) {
												return "<input type=\"text\" class=\"width-100\" value=\""
														+ (orderDetail.sku ? orderDetail.sku
																: '') + "\"/>";
											}

										},
										{
											"mData" : function(orderDetail) {
												return '<input type="text" class=\"width-100\" value="'
														+ (orderDetail.productName != null ? orderDetail.productName
																: "") + '"/>';
											}
										},
										{
											"mData" : function(orderDetail) {
												return "<input type=\"text\" class=\"width-100\" value=\""
														+ (orderDetail.quantity ? orderDetail.quantity
																: 0) + "\"/>";
											}
										},
										{
											"mData" : function(orderDetail) {
												return "<input type=\"text\" class=\"width-100\" value=\""
														+ (orderDetail.supplierOrderNumber ? orderDetail.supplierOrderNumber
																: '') + "\"/>";
											}
										},
										{
											"mData" : function(orderDetail) {
												var s = $("<select id=\"selectId\" name=\"selectName\" class=\"pull-right width-100\" />");
												$("<option />", {
													value : "",
													text : ""
												}).appendTo(s);
												for (var val = 0; val < MY_SUPPLIERS.length; val++) {
													var option = $(
															"<option />",
															{
																value : MY_SUPPLIERS[val].oimSuppliers.supplierId,
																text : MY_SUPPLIERS[val].oimSuppliers.supplierName
															}).appendTo(s);
													if (orderDetail.oimSuppliers != null
															&& orderDetail.oimSuppliers.supplierId == MY_SUPPLIERS[val].oimSuppliers.supplierId) {
														option.attr('selected',
																true);
													}
												}
												return $('<div>').append(s)
														.html();

											}
										},
										{
											"mData" : function(orderDetail) {
												var s = $("<select id=\"selectId\" name=\"selectName\" />");
												for (var val = 0; val < STATUS.length; val++) {
													var option = $(
															"<option />",
															{
																value : STATUS[val].statusId,
																text : STATUS[val].statusValue
															}).appendTo(s);
													if (orderDetail.oimOrderStatuses != null
															&& orderDetail.oimOrderStatuses.statusId == STATUS[val].statusId) {
														option.attr('selected',
																true);
													}
												}
												return $('<div>').append(s)
														.html();
											}
										},
										{
											"mData" : function(orderDetail) {
												return '<button type="button" class="btn btn-info btn-xs pull-left" onclick="b($($(this).parent()).parent());"><i class="icon-ok"></i>Update</button>';
											}
										},
										{
											"mData" : function(orderDetail) {
												return '<a href="#TrackingModal" data-toggle="modal" onclick="openTrackingData($($(this).parent()).parent());" ><i class="icon-only icon-align-justify" title="Tracking"></i></a>';
											}
										} ],
								"aaData" : orderTemp.oimOrderDetailses,
								"bDestroy" : true
							});
			GenericBinder('order', orderTemp);
			$('#updateorder').unbind("click").on('click', orderTemp,
					function(e) {
						$(this).CRUD({
							method : "PUT",
							url : 'aggregators/orders',
							data : JSON.stringify(e.data),
							success : function(data, textStatus, jqXHR) {
								table_xy.ajax.reload();
								$.gritter.add({
									title : "Update Order",
									text : "Order updated successfully."
								});
								$('#myModaledit').modal('hide');

							},
							error : function(a, c, b) {
								$.gritter.add({
									title : "Update Order Detail",
									text : "Error in updating order detail."
								});
							}
						});
					});
		}

		jQuery(function($) {
			var order_status = '${param["order_status"]}';
			for (var val = 0; val < STATUS.length; val++) {
				if (order_status != '' && order_status == STATUS[val].statusId) {
					$("<option />", {
						value : STATUS[val].statusId,
						text : STATUS[val].statusValue
					}).attr('selected', 'selected').appendTo('#order_status');
				} else {
					$("<option />", {
						value : STATUS[val].statusId,
						text : STATUS[val].statusValue
					}).appendTo('#order_status');
				}
			}
			$(this).CRUD({
				url : "aggregators/suppliers",
				method : "GET",
				success : function(data) {
					MY_SUPPLIERS = data;
					for (var val = 0; val < MY_SUPPLIERS.length; val++) {
						$("<option />", {
							value : MY_SUPPLIERS[val].oimSuppliers.supplierId,
							text : MY_SUPPLIERS[val].oimSuppliers.supplierName
						}).appendTo("#suppliers");
					}
				}
			});
			$(this).CRUD({
				url : "aggregators/channels",
				method : "GET",
				success : function(data) {
					for (var val = 0; val < data.length; val++) {
						$("<option />", {
							value : data[val].channelId,
							text : data[val].channelName
						}).appendTo("#channels");
					}
				}
			});

			table_xy = $('#tableprocesschannel1')
					.DataTable(
							{
								"order" : [ [ 3, "desc" ] ],
								"processing" : true,
								"serverSide" : true,
								"sAjaxDataProp" : "data",
								"ajax" : function(data, callback, settings) {
									var d = $.CM.planify(data);
									var id = $('#tabs-1').is(':visible') ? '#tabs-1'
											: '#tabs-2';
									var map = {};
									$(id + " :input").each(function() {
										if (this.name) {
											if (map[this.name])
												map[this.name] += ',';
											map[this.name] = this.value;
										}
									});
									d.filters = map;
									$(this).CRUD({
										"method" : "POST",
										"cache" : true,
										"url" : 'aggregators/orders/search',
										"data" : JSON.stringify(d),
										"message" : true,
										"success" : function(d) {
											callback(d);
										}
									});
								},
								"bDestroy" : true,
								"aoColumns" : [
										{
											"mData" : function(order) {
												return '<label><input class="ace" type="checkbox" value="'+order.orderId+'"><span class="lbl"></span></label>';
											},
											"orderable" : false
										},
										{
											"mData" : "storeOrderId"
										},
										{
											"mData" : function(order) {
												var text = '';
												for (var i = 0; i < order.oimOrderDetailses.length; i++) {
													var orderDetail = order.oimOrderDetailses[i];

													text += '<strong>'
															+ orderDetail.sku
															+ '</strong><a href="#myModalOrderMods" title="Click to view Order Audit trail." data-toggle="modal" onclick="$.CM.getOrderModification('
															+ orderDetail.detailId
															+ ');"><i class=\"icon-book\"></i></a>';
													if (orderDetail.supplierOrderNumber)
														text += '&nbsp;<a style="cursor:pointer;" title="Click to refresh tracking" onclick="$.CM.trackOrder('
																+ orderDetail.detailId
																+ ');"><i class="icon-refresh"></i></a>';
													text += '<br/><span id="orderStatus'+orderDetail.detailId+'">'
															+ orderDetail.oimOrderStatuses.statusValue;
													if (orderDetail.supplierOrderStatus) {
														text += " - "
																+ orderDetail.supplierOrderStatus;
													}
													text += '</span><div class="space-2"></div>';
												}
												return text;
											}
										},
										{
											"mData" : function(order) {
												return "<span style='display:none'>"
														+ order.orderTm
														+ "</span>"
														+ new Date(
																order.orderTm)
																.toLocaleString();
											}
										},
										{
											"mData" : "deliveryName"
										},
										{
											"mData" : "oimOrderBatches.oimChannels.channelName"
										},
										{
											"mData" : "shippingAddress",
											"orderable" : false
										},
										{
											"mData" : "orderTotalAmount"
										},
										{
											"mData" : function(order) {
												return '<a href="#myModaledit" data-toggle="modal" onclick="c($($(this).parent()).parent());" class="btn btn-info btn-sm icon-pencil"></a>';
											},
											"orderable" : false
										} ]
							});
			$('.addresspop').popover({
				container : 'body'
			});
			$('table th input:checkbox').on(
					'click',
					function() {
						var that = this;
						$(this).closest('table').find(
								'tr > td:first-child input:checkbox').each(
								function() {
									this.checked = that.checked;
									$(this).closest('tr').toggleClass(
											'selected');
								});

					});

			$('#searchBtn').on('click', function() {
				table_xy.ajax.reload();
			});
			$('#clearBtn').on('click', function() {
				var id = $('#tabs-1').is(':visible') ? '#tabs-1' : '#tabs-2';
				$(id).find(':text').val('');
				$(id + " option:selected").removeAttr("selected");
				$('#searchBtn').click();
			});
			$('#processselect1')
					.on(
							'change',
							function(e) {
								if ('re-process' == $(this).val()) {
									var conf = confirm('Please make sure to edit the PO number according to supplier requirements in-order to avoid re-processing errors.');
									if (!confirm) {
										$(this).val('0');
										e.preventDefault();
									}
								}
							});
			$('#btnUpdateBulk')
					.click(
							function() {
								var orders = [];
								var selected = false;
								if ($('#processselect1 option:selected').val() == '0') {
									alert('Please select an action to apply to selected order(s).');
									return false;
								}
								$('table tr input:checkbox')
										.each(
												function(i, d) {
													if ($(this).is(':checked')) {
														selected = true;
														var o = {};
														var order = table_xy
																.row(
																		$(this)
																				.parents(
																						'tr'))
																.data();
														if (order != null) {
															for (var i = 0; i < order.oimOrderDetailses.length; i++) {
																var od = order.oimOrderDetailses[i];
																if (od.oimSuppliers
																		&& od.oimSuppliers.supplierId > 0) {
																	o['orderId'] = order.orderId;
																	o['supplierId'] = od.oimSuppliers.supplierId;
																}
																orders
																		.push(order.orderId);
															}
														}
													}
												});
								if (!selected) {
									orders.length = 0;
									alert('Please select an order.');
									return false;
								}
								$(this)
										.CRUD(
												{
													url : 'aggregators/orders/processed/bulk/'
															+ $(
																	'#processselect1 option:selected')
																	.val(),
													data : JSON
															.stringify(orders),
													method : 'POST',
													message : true,
													success : function(data) {
														$.gritter
																.add({
																	title : 'Order Bulk Update',
																	text : data.length
																			+ ' Orders '
																			+ $(
																					'#processselect1 option:selected')
																					.text()
																});
														table_xy.ajax.reload();
														$.CM
																.updateOrderSummary();
													}
												});
							});
			$('#updateTracking')
					.click(
							function() {
								if(validateForm()==false)
									return;
								
								var map = {};
								$("#trackingForm :input").each(function() {
									if (this.name) {
										if (map[this.name])
											map[this.name] += ',';
										map[this.name] = this.value;
									}
								});
								var detailId = map.detailId;
								delete (map.detailId);
								$(this)
										.CRUD(
												{
													url : 'aggregators/orders/orderHistory/trackingData/'
															+ detailId,
													data : JSON.stringify(map),
													method : 'POST',
													message : true,
													success : function(data) {
														$.gritter
																.add({
																	title : 'Order Tracking',
																	text : data
																});
														$('#myModaledit')
																.modal('hide');
														$('#TrackingModal')
																.modal('hide');
													}
												});
								table_xy.ajax.reload();
							});
		});
	</script>
</jsp:attribute>
</t:basepage>
