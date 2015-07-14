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
								<div class="widget-main no-padding">
								<table id="tableprocesschannel1"
								class="table table-bordered table-striped  table-responsive dataTable">
											<thead class="thin-border-bottom">
												<tr role="row">
													<th><i
											class="icon-sort-by-order-alt icon-2x blue visible-xs"></i><span
											class="hidden-xs visible-sm">Order Id</span></th>
												<th>Order Status</th>
													<th class="hidden-xs sorting" role="columnheader">Date</th>
													<th class="sorting" role="columnheader"><i
											class="icon-home icon-2x blue visible-xs"></i><span
											class="hidden-xs">Customer</span></th>
													<th class="hidden-sm hidden-xs sorting">Channel
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
																	<tr>
																		<td><input type="text" value="RS1234" readonly=""
																	class="pull-right width-100" name="billno"></td>
																		<td><input type="text"
																	value="Ravish Test Product" readonly=""
																	class="pull-right width-100" name="orderdate"></td>
																		<td><input type="text" value="4" readonly=""
																	class="pull-right width-100" name="Quantity"></td>
																		<td><input type="text" value="0.0" readonly=""
																	class="pull-right width-100" name="SalePrice"></td>
																		<td><input type="text" value="Unprocessed"
																	readonly="" class="pull-right width-100" name="Status"></td>
																	</tr>
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
																	data-bind-order="orderTm" readonly="readonly"
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
                                  <th>Quantity</th>
                                  <th>Sale Price</th>
                                  <th>Supplier</th>
                                  <th>Status</th>
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
								</div>
							</div>
						<!-- /.modal-content -->
						</div>
		</div>
	</div>		
	<!-- /.page-content -->
	
	<div aria-hidden="true" role="dialog" tabindex="-1"
			id="myModalOrderMods" class="modal fade">
										<div class="modal-dialog">
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
																		<th>Sku</th>
																		<th>Comment</th>
																		<th>Quantity</th>
																		<th>Sale Price</th>
																		<th>Status</th>
																		<th>Change Date</th>
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
		var STATUS = [ {
			"statusId" : 3,
			"statusValue" : "Failed"
		}, {
			"statusId" : 0,
			"statusValue" : "Unprocessed"
		}, {
			"statusId" : 2,
			"statusValue" : "Processed"
		}, {
			"statusId" : 5,
			"statusValue" : "Manually Processed"
		}, {
			"statusId" : 6,
			"statusValue" : "Canceled"
		}, {
			"statusId" : 7,
			"statusValue" : "Shipped"
		} ];

		function a(e) {
			var order = table_xy.row(e[0]).data();
			$('#tableorderdetails')
					.dataTable(
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
							}).width('100%');
		}
		function b(e) {
			var orderDetail = tableModal.fnGetData(e[0]);
			updateOrderDetail(orderDetail, $(e[0]).find('input')[0], $(e[0])
					.find('input')[1], $(e[0]).find('input')[2], $(e[0]).find(
					'input')[3], $(e[0]).find('select')[0], $(e[0]).find(
					'select')[1], e[0]);
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
					.dataTable(
							{
								bSort : false,
								"aoColumns" : [
										{
											"mData" : function(orderDetail) {
												return "<input type=\"text\" class=\"width-100\" value=\""
														+ (orderDetail.sku ? orderDetail.sku
																: '') + "\"/>";
											},
											"sWidth" : "20%"
										},
										{
											"mData" : function(orderDetail) {
												return '<input type="text" class=\"width-100\" value="'
														+ (orderDetail.productName != null ? orderDetail.productName
																: "") + '"/>';
											},
											"sWidth" : "35%"
										},
										{
											"mData" : function(orderDetail) {
												return "<input type=\"text\" class=\"width-100\" value=\""
														+ (orderDetail.quantity ? orderDetail.quantity
																: 0) + "\"/>";
											},
											"sWidth" : "7%"
										},
										{
											"mData" : function(orderDetail) {
												return "<input type=\"text\" class=\"width-100\" value=\""
														+ (orderDetail.salePrice ? orderDetail.salePrice
																: 0) + "\"/>";
											},
											"sWidth" : "8%"
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

											},
											"sWidth" : "10%"
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
											},
											"sWidth" : "10%"
										},
										{
											"mData" : function(orderDetail) {
												var text = '';

												if (orderDetail.supplierOrderStatus) {
													text = '<span id="orderStatus'+orderDetail.detailId+'">';
													text += orderDetail.supplierOrderStatus
															+ '</span>';
												}
												if (orderDetail.supplierOrderNumber)
													text += '<br><a onclick="$.CM.trackOrder('
															+ orderDetail.detailId
															+ ');">Refresh</a>';
												return text
														+ '<button type="button" class="btn btn-info btn-xs pull-left " onclick="b($($(this).parent()).parent());"> <i class="icon-ok"></i>Update</button>';
											},
											"sWidth" : "10%"
										} ],
								"aaData" : orderTemp.oimOrderDetailses,
								"bDestroy" : true,
								"bAutoWidth" : false
							}).width('100%');
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
								"order" : [ [ 2, "desc" ] ],
								"processing" : true,
								"serverSide" : false,
								"sAjaxDataProp" : "",
								"ajax" : function(data, callback, settings) {
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
									$(this).CRUD({
										"method" : "POST",
										"cache" : true,
										"url" : 'aggregators/orders/search',
										"data" : JSON.stringify(map),
										"message" : true,
										"success" : function(d) {
											callback(d);
										}
									});
								},
								"bDestroy" : true,
								"aoColumns" : [
										{
											"mData" : "storeOrderId"
										},
										{
											"mData" : function(order) {
												var text = '';
												for (var i = 0; i < order.oimOrderDetailses.length; i++) {
													var orderDetail = order.oimOrderDetailses[i];
													text += '<div>';
													text += '<a href="#myModalOrderMods" title="Click to view Order Audit trail." data-toggle="modal" onclick="$.CM.getOrderModification('
															+ orderDetail.detailId
															+ ');"><strong>'
															+ orderDetail.sku
															+ "</strong></a>";
													if (orderDetail.supplierOrderStatus) {
														text += ':<span id="orderStatus'+orderDetail.detailId+'">';
														text += orderDetail.supplierOrderStatus
																+ '</span>';
													}
													if (orderDetail.supplierOrderNumber)
														text += '<br><a style="cursor:pointer;" title="Click to refresh tracking" onclick="$.CM.trackOrder('
																+ orderDetail.detailId
																+ ');"><i class="icon-refresh"></i></a>';
													text += '</div>';
												}
												return text;
											},
											"bSortable" : false
										},
										{
											"mData" : function(order) {
												return "<span style='display:none'>"
														+ order.orderTm
														+ "</span>"
														+ new Date(
																order.orderTm)
																.toLocaleDateString();
											}
										},
										{
											"mData" : "deliveryName"
										},
										{
											"mData" : function(order) {
												return order.oimOrderBatches.oimChannels.channelName;
											}
										},
										{
											"mData" : "shippingDetails"
										},
										{
											"mData" : "orderTotalAmount"
										},
										{
											"mData" : function(order) {
												return '<a href="#myModaledit" data-toggle="modal" onclick="c($($(this).parent()).parent());" class="btn btn-info btn-sm icon-pencil"></a>';
											}
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
			$('#recent-box [data-rel="tooltip"]').tooltip({
				placement : tooltip_placement
			});
			function tooltip_placement(context, source) {
				var $source = $(source);
				var $parent = $source.closest('.tab-content');
				var off1 = $parent.offset();
				var w1 = $parent.width();

				var off2 = $source.offset();
				var w2 = $source.width();

				if (parseInt(off2.left) < parseInt(off1.left)
						+ parseInt(w1 / 2))
					return 'right';
				return 'left';
			}

			$('.dialogs,.comments').slimScroll({
				height : '300px'
			});

			//Android's default browser somehow is confused when tapping on label which will lead to dragging the task
			//so disable dragging when clicking on label
			var agent = navigator.userAgent.toLowerCase();
			if ("ontouchstart" in document && /applewebkit/.test(agent)
					&& /android/.test(agent))
				$('#tasks').on('touchstart', function(e) {
					var li = $(e.target).closest('#tasks li');
					if (li.length == 0)
						return;
					var label = li.find('label.inline').get(0);
					if (label == e.target || $.contains(label, e.target))
						e.stopImmediatePropagation();
				});

			$('#tasks').sortable({
				opacity : 0.8,
				revert : true,
				forceHelperSize : true,
				placeholder : 'draggable-placeholder',
				forcePlaceholderSize : true,
				tolerance : 'pointer',
				stop : function(event, ui) {//just for Chrome!!!! so that dropdowns on items don't appear below other items after being moved
					$(ui.item).css('z-index', 'auto');
				}
			});
			$('#tasks').disableSelection();
			$('#tasks input:checkbox').removeAttr('checked').on('click',
					function() {
						if (this.checked)
							$(this).closest('li').addClass('selected');
						else
							$(this).closest('li').removeClass('selected');
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
		});
	</script>
</jsp:attribute>
</t:basepage>
