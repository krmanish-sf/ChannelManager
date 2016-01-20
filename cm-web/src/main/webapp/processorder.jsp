<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
	<div class="main-content">
      <div id="breadcrumbs" class="breadcrumbs"> 
        <ul class="breadcrumb">
          <li><a href="index.jsp"><i
							class="icon-home home-icon"></i>Home</a> </li>
          <li class="active">Process Order</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1>Process Order <small> <i
							class="icon-double-angle-right"></i> overview &amp; stats </small> </h1>
        </div>
        <!-- /.page-header -->
        
        <div class="row">
          <div class="col-xs-12">
            <div class="row">
              <div class="col-sm-7 infobox-container">
              <div class="col-sm-12 ">
                <a class="infobox infobox-green" id="unprocessedCount"
										href="processorder.jsp#unprocessed">
                  <div class="infobox-icon"> <i class="icon-cogs"></i>
							</div>
                  <div class="infobox-data"> <span
												class="infobox-data-number">No</span>
                    <div class="infobox-content">Unprocessed Orders</div>
                  </div>
                </a>
                <a class="infobox infobox-green" id="unprocessedValue"
										href="processorder.jsp#unprocessed">
                  <div class="infobox-icon"> <i
												class="icon-dollar"></i> </div>
                  <div class="infobox-data"> <span
												class="infobox-data-number">$0</span>
                    <div class="infobox-content">Unprocessed Value</div>
                  </div>
                </a>
				</div>
                <div class="col-sm-12 ">
                 <a class="infobox infobox-red" id="unresolvedCount"
										href="processorder.jsp#unresolved">
                  <div class="infobox-icon"> <i
												class="icon-warning-sign"></i> </div>
                  <div class="infobox-data"> <span
												class="infobox-data-number">No</span>
                    <div class="infobox-content">Unresolved Orders</div>
                  </div>
                </a>
                 <a class="infobox infobox-red" id="unresolvedValue"
										href="processorder.jsp#unresolved">
                  <div class="infobox-icon"> <i
												class="icon-dollar"></i> </div>
                  <div class="infobox-data"> <span
												class="infobox-data-number">$0</span>
                    <div class="infobox-content">Unresolved Value</div>
                  </div>
                </a>
                <div class="space-6"></div>
                </div>
              </div>
              <div class="col-sm-5">
                <div class="widget-container-span">
                  <div class="widget-box">
                    <div class="widget-header header-color-green">
                      <h5>Tip:</h5>
                      <div class="widget-toolbar"> <a
													data-action="close" href="#"> <i
													class="icon-remove white"></i> </a> </div>
                    </div>
                    <div class="widget-body alert-success">
                      <div class="widget-main">
                        <p> You can view all your unprocessed and unresolved  orders from all of your channels. Click the action button next to an Order to process or resolve it.</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!--	<div class="vspace-sm"></div>--> 
            </div>
            <div class="space-6"></div>
            <!-- /widget-main -->
            <div class="row">
              <div class="col-sm-12">
                <h4 class="lighter pull-left"> <i
										class="icon-shopping-cart orange"></i> Manage your Order </h4>
              </div>
            </div>
            <div class="row">
              <div id="recent-box" class="widget-box transparent">
                <!-- <div class="widget-header">
                  <div class="widget-toolbar pull-left">
                    <ul id="recent-tab" class="nav nav-tabs">
                      <li class="active"> <a id="aResolve-tab"
												href="#Resolve-tab" class="red" data-toggle="tab">Resolve</a> </li>
                      <li> <a id="aProcess-tab" href="#Process-tab"
												class="green" data-toggle="tab">Process</a> </li>
                    </ul>
                  </div>
                </div> -->
                <div class="container">
                  <div class="col-sm-2">
                    <select id="processselect1"
											class=" width-100 pull-left">
						<option value="0">With selected</option>
                      <option value="process">Attempt to Process</option>
                      <option value="manually-processed">Mark as Manually Processed</option>
                      <option value="delete">Mark As Manually Canceled</option>
                    </select>
                  </div>
                  <div class="col-sm-10">
										<a id="btnUpdateBulk" class="btn btn-info pull-left"
											href="javascript:;">Update Selected Orders</a>
									</div>
                </div>
                <div class="widget-main no-padding">
                  <div class="tab-content padding-8 overflow-visible">
                    <div class="tab-pane active" id="Resolve-tab">
                      <div role="grid" class="dataTables_wrapper"
												id="tableprocesschannel_wrapper">
												<table id="tableprocesschannel"
													class="table table-bordered table-striped  table-responsive dataTable"
													aria-describedby="tableprocesschannel_info">
                        <thead class="thin-border-bottom">
                          <tr role="row">
															<th class="center sorting_disabled sorting_asc"
																role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 18px;" aria-sort="ascending"
																aria-label=": activate to sort column descending"><label>
                                <input type="checkbox" class="ace">
                                <span class="lbl"></span>
															</label>
                            </th>
															<th class="sorting" role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 45px;"
																aria-label="Order Id: activate to sort column ascending"><i
																class="icon-sort-by-order-alt icon-2x blue visible-xs"></i><span
																class="hidden-xs visible-sm">Order Id</span></th>
															<th class="sorting" role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 45px;"
																aria-label="Order Number: activate to sort column ascending"><i
																class="icon-sort-by-order-alt icon-2x blue visible-xs"></i><span
																class="hidden-xs visible-sm">Order Number</span></th>
															<th class="hidden-xs sorting" role="columnheader"
																tabindex="0" aria-controls="tableprocesschannel"
																rowspan="1" colspan="1" style="width: 37px;"
																aria-label="Date: activate to sort column ascending">Order Date</th>
															<th class="sorting" role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 67px;"
																aria-label="Customer: activate to sort column ascending"><i
																class="icon-home icon-2x blue visible-xs"></i><span
																class="hidden-xs">Customer</span></th>
															
															<th class="hidden-sm hidden-xs sorting"
																role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 75px;"
																aria-label="Channel Name: activate to sort column ascending">Channel Name</th>
															<th class="hidden-md hidden-sm hidden-xs "
																role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 80px;"
																aria-label="No Of Products">Unique SKU's</th>
															<th class="hidden-sm hidden-xs " role="columnheader"
																tabindex="0" aria-controls="tableprocesschannel"
																rowspan="1" colspan="1" style="width: 130px;"
																aria-label="Shipping: activate to sort column ascending"><span>Shipping</span></th>
															<%-- <th class="sorting" role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 84px;"
																aria-label="status: activate to sort column ascending"><i
																class="icon-bolt icon-2x blue visible-xs"></i><span
																class="hidden-xs visible-sm">status</span></th>
															<th class="hidden-xs sorting" role="columnheader"
																tabindex="0" aria-controls="tableprocesschannel"
																rowspan="1" colspan="1" style="width: 142px;"
																aria-label="Supplier: activate to sort column ascending">Supplier</th> --%>
															<th class="sorting" role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 52px;"
																aria-label="Order Total: activate to sort column ascending"><i
																class="icon-usd icon-2x blue visible-xs"></i><span
																class="hidden-xs">Order Total</span></th>
																<th class="sorting_disabled hidden-xs "
																role="columnheader" tabindex="0"
																aria-controls="tableprocesschannel" rowspan="1"
																colspan="1" style="width: 50px;" aria-label="Edit Order">Edit Order</th>
															<th class="sorting_disabled sorting" role="columnheader"
																tabindex="0" aria-controls="tableprocesschannel"
																rowspan="1" colspan="1" style="width: 87px;"
																aria-label=": activate to sort column ascending"></th>
														</tr>
                        </thead>
                        
                      <tbody role="alert" aria-live="polite"
														aria-relevant="all">
													</tbody>
												</table>
											</div>
                    </div>
                  </div>
                </div>
                <div aria-hidden="true" role="dialog" tabindex="-1"
									id="myModalResolve" class="modal fade">
                  <div id="myModalResolvecontent" class="modal-dialog">
                    <div class="modal-content">
                      <div class="modal-header">
                        <button aria-hidden="true" data-dismiss="modal"
													class="close" type="button">&times;</button>
                        <h4 class="modal-title">Resolve</h4>
                      </div>
                      <div id="processmodel" class="modal-body ">
                        <form role="form" class="form-horizontal">
                          <div class="row">
														<table id="tablemodel1"
															class="table table-striped table-bordered table-hover no-margin-bottom no-border-top dataTable">
                            <thead>
                              <tr role="row">
																	<th class="sorting_asc" role="columnheader"
																		tabindex="0" aria-controls="tablemodel1" rowspan="1"
																		colspan="1" style="width: 0px;" aria-sort="ascending"
																		aria-label="Sku: activate to sort column descending">Sku</th>
																	<th class="sorting" role="columnheader" tabindex="0"
																		aria-controls="tablemodel1" rowspan="1" colspan="1"
																		style="width: 0px;"
																		aria-label="Name: activate to sort column ascending">Name</th>
																	<th class="sorting" role="columnheader" tabindex="0"
																		aria-controls="tablemodel1" rowspan="1" colspan="1"
																		style="width: 0px;"
																		aria-label="Quantity: activate to sort column ascending">Quantity</th>
																	<th class="sorting" role="columnheader" tabindex="0"
																		aria-controls="tablemodel1" rowspan="1" colspan="1"
																		style="width: 0px;"
																		aria-label="Sale Price: activate to sort column ascending">Sale Price</th>
																		<th class="sorting" role="columnheader" tabindex="0"
																		aria-controls="tablemodel1" rowspan="1" colspan="1"
																		style="width: 0px;"
																		aria-label="Status: activate to sort column ascending">Supplier</th>
																	<th class="sorting" role="columnheader" tabindex="0"
																		aria-controls="tablemodel1" rowspan="1" colspan="1"
																		style="width: 0px;"
																		aria-label="Status: activate to sort column ascending">Status</th>
																	<th class="sorting_disabled" role="columnheader"
																		tabindex="0" aria-controls="tablemodel1" rowspan="1"
																		colspan="1" style="width: 0px;"
																		aria-label=": activate to sort column ascending"></th>
																</tr>
                            </thead>

                          <tbody role="alert" aria-live="polite"
																aria-relevant="all">
															</tbody>
														</table>
													</div>
                        </form>
                      </div>
                      <div class="modal-footer no-margin-top">
                        <button data-dismiss="modal"
													class="btn btn-sm btn-danger pull-left"> <i
														class="icon-remove"></i> Close </button>
                      </div>
                    </div>
                  </div>
                  <!-- /.modal-content --> 
                </div>
                <!-- / mobile.modal -->
                <div aria-hidden="true" role="dialog" tabindex="-1"
									id="myModalResolvemob" class="modal fade">
                  <div class="modal-dialog">
                    <div class="modal-content">
                      <div class="modal-header">
                        <button aria-hidden="true" data-dismiss="modal"
													class="close" type="button">&times;</button>
                        <h4 class="modal-title">Resolve</h4>
                      </div>
                      <div class="modal-body">
                        <div class="row">
                          <div id="processmodelM" class="col-xs-12"> 
                            <!-- PAGE CONTENT BEGINS -->
                            
                            <form role="form" class="form-horizontal">
                              <div class="form-group">
                                <label for="form-input-readonly1m"
																	class="col-sm-3 control-label no-padding-right"> Sku:</label>
                                <div class="col-sm-9">
                                  <input type="text" value=""
																		id="form-input-readonly1m" class="col-xs-10 col-sm-9"
																		readonly="readonly">
                                </div>
                              </div>
                              <div class="form-group">
                                <label for="form-field-1m"
																	class="col-sm-3 control-label no-padding-right">Quantity:</label>
                                <div class="col-sm-9">
                                  <input type="text"
																		class="col-xs-10 col-sm-5" placeholder="Username"
																		id="form-field-1m" value="4">
                                </div>
                              </div>
                              <div class="space-4"></div>
                              <div class="form-group">
                                <label for="form-field-select-1m"
																	class="col-sm-3 control-label no-padding-right">Status:</label>
                                <div class="col-sm-9">
                                  <select id="form-field-select-1m"
																		class="form-control width-85">
                                    <option value="">Unprocessed</option>
                                  </select>
                                </div>
                              </div>
                              <div class="clearfix form-actions">
                                <div class="col-md-12">
                                  <button type="button"
																		class="btn btn-info btn-xs"> <i
																			class="icon-ok "></i>Update</button>
                                </div>
                              </div>
                            </form>
                            <div class="hr hr-20"></div>
                            <form role="form" class="form-horizontal">
                              <div class="form-group">
                                <label for="form-input-readonly1ma"
																	class="col-sm-3 control-label no-padding-right"> Sku:</label>
                                <div class="col-sm-9">
                                  <input type="text" value=" NV8083538"
																		id="form-input-readonly1ma" class="col-xs-10 col-sm-9"
																		readonly="">
                                </div>
                              </div>
                              <div class="form-group">
                                <label for="form-field-1ma"
																	class="col-sm-3 control-label no-padding-right">Quantity:</label>
                                <div class="col-sm-9">
                                  <input type="text"
																		class="col-xs-10 col-sm-5" placeholder="Username"
																		id="form-field-1ma" value="4">
                                </div>
                              </div>
                              <div class="space-4"></div>
                              <div class="form-group">
                                <label for="form-field-select-1ma"
																	class="col-sm-3 control-label no-padding-right">Status:</label>
                                <div class="col-sm-9">
                                  <select id="form-field-select-1ma"
																		class="form-control width-85">
                                    <option value="">Unprocessed</option>
                                  </select>
                                </div>
                              </div>
                              <div class="clearfix form-actions">
                                <div class="col-md-12">
                                  <button type="button"
																		class="btn btn-info btn-xs"> <i
																			class="icon-ok "></i>Update</button>
                                </div>
                              </div>
                            </form>
                            <div class="hr hr-20"></div>
                          </div>
                          <!-- PAGE CONTENT ENDS --> 
                        </div>
                        <!-- /.col --> 
                      </div>
                    </div>
                  </div>
                  <!-- /.modal-content --> 
                </div>
                <!-- /.modal-dialog --> 
                <!--                edit modal-->
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
                            <form role="form" class="form-horizontal"
															id="order-form">
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
																			id="deliverycompany"
																			data-bind-order="deliveryCompany" class="pull-right"
																			maxlength="100" name="deliverycompany">
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
																			id="deliverycountry"
																			data-bind-order="deliveryCountry" class="pull-right"
																			maxlength="20" name="deliverycountry">
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
                               <div class="col-sm-4">
                                  <label>State Code</label>
                                  <input type="text"
																		id="deliveryStateCode"
																		data-bind-order="deliveryStateCode" class="pull-right"
																		maxlength="2" name="deliveryStateCode">
                                </div>
                                <div class="col-sm-4">
                                  <label>Country Code</label>
                                  <input type="text"
																		data-bind-order="deliveryCountryCode"
																		class="pull-right" maxlength="3"
																		name="deliveryCountryCode">
                                </div>
                                
                              </div>
                               <div class="space-4"></div>
                               
                            </form>
                            <!-- PAGE CONTENT BEGINS -->
                            <fieldset>
                              <legend>Shipping Method</legend>
                              <div class="row">
                              <div class="col-sm-4">
                                  <label>Store Shipping Text</label>
                                </div>
                                <div class="col-sm-8">
                                <label data-bind-order="shippingDetails"></label>
                                </div>
							</div>
																<div class="space-4"></div>
                                <div class="row">
                                <div class="col-sm-4">
                                <label>Mapped Shipping Method</label>
                                </div>
                                <div class="col-sm-8">
                                 <input type="text" id="shippingMethods"
																		placeholder="Shipping mapping is missing, search and select"
																		class="form-control ui-autocomplete-input"
																		name="mapped-shipping"
																		data-bind-order="shippingMethod.fullName">
                                </div>
							  </div>
																<div class="space-4"></div>
							  <div class="row">
							  <div class="col-md-8 pull-right">
                                	<button type="button" id="updateorder"
																		class="btn btn-info btn-xs pull-right"> <i
																			class="icon-ok "></i>Update</button>
									<strong class="pull-right"> Save changes in the above payment or shipping details &gt;&nbsp;</strong>
                                </div>
							  </div>
                              </fieldset>
                            <div class="row">
                              <div class="col-sm-12">
                                <h4 class="modal-title">Product(s)</h4>
                                
                                <div class="hr hr-2"></div>
                                <div class="space-4"></div>
                              </div>
							<div id="warning-text" class="col-sm-12"
																style="color: #FFF; background-color: red; margin: 3px 0px">
																<strong>This order has multiple suppliers. Edit the product details below, or click "Confirm" to process as a multiple supplier order.</strong>
                                <button
																	class="btn btn-info btn-xs pull-right"
																	id="confirm-order-shipment" type="button"> <i
																		class="icon-ok "></i>Confirm</button>
                                </div>                     
                                <div class="space-4"></div>     
                            <table id="editordermodaltable"
																class="table table-striped table-bordered table-hover table-responsive">
                              <thead>
                                <tr>
                                  <th>Sku</th>
                                  <th>Name</th>
                                  <th>Qty</th>
                                  <th>Sale Price</th>
                                  <th>Supplier</th>
                                  <th>Status</th>
                                  <th class="sorting_disabled"></th>
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
                    <!-- PAGE CONTENT ENDS --> 
                  </div>
                  <!-- /.col --> 
                </div>
              </div>
            </div>
          </div>
          <!-- /.modal-content --> 
          <!-- process confirmation start -->
          
              <div class="modal fade" id="processConfirmationModal"
						tabindex="-1" role="dialog" aria-hidden="true">
              <div id="processConfirmationdailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">The following items cannot be found:</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                      <div class="col-sm-12">
							<table id="tableUnavailableHGItems"
													class="table table-bordered table-striped table-responsive">
								<thead class="thin-border-bottom">
									<tr>
										<th><i class="icon-barcode icon-2x blue visible-xs"></i><span
																class="hidden-xs visible-sm">SKU</span></th>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
																class="hidden-xs">Name</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
																class="hidden-xs">Price</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
																class="hidden-xs">Quantity</span></th>
									</tr>
								</thead>
								<tbody>
								</tbody>
							</table>
						</div>
                      </div>
                    </div>
                  </div>
                   <div class="modal-footer no-margin-top">
                       <button type="button" id="processAvailItem"
												class="btn btn-info btn-xs pull-right"> <i
													class="icon-ok "></i>Process Available Items Anyway</button>
						<button type="button" id="dontProcess"
												class="btn btn-info btn-xs pull-left"> <i
													class="icon-stop"></i>Don't Process Any Right Now</button>
                      </div>
                </div>
			</div>
            </div>
          <!-- process confirmation end -->
        </div>
      </div>
    </div>
</jsp:attribute>
	<jsp:attribute name="pagejs">
	
<!-- inline scripts related to this page --> 
<script type="text/javascript"
			src="static/js/jquery-ui-1.10.3.full.min.js"></script>
<script type="text/javascript">
	var table_xy = null, tableModal = null, editOrder = null;
	var MY_SUPPLIERS = [];
	function showResolve(e) {
		var order = table_xy.row(e[0]).data();
		$('#order-form').hide();
		showOrderEdit(order);
	}

	function process(e) {
		var order = table_xy.row(e[0]).data();
		$.CM.processOrder(order);
		$.CM.updateOrderSummary();
	}

	function b(e) {
		var orderDetail = tableModal.row(e[0]).data();
		updateOrderDetail(orderDetail, $(e[0]).find('input')[0], $(e[0]).find(
				'input')[1], $(e[0]).find('input')[2],
				$(e[0]).find('input')[3], $(e[0]).find('select')[0], $(e[0])
						.find('select')[1], e[0]);
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

		$(button).CRUD({
			method : "PUT",
			url : 'aggregators/orders/orderdetails/' + orderDetail.detailId,
			data : JSON.stringify(orderDetail),
			success : function(data, textStatus, jqXHR) {
				$.gritter.add({
					title : "Update Order Detail",
					text : "Order Detail updated successfully."
				});
				table_xy.ajax.reload();
				$('#myModalResolve').modal('hide');
				$('#myModalEdit').modal('hide');
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
		$('#order-form').show();
		showOrderEdit(order);
	}
	function showOrderEdit(order) {
		var orderTemp = JSON.parse(JSON.stringify(order));
		editOrder = orderTemp;
		if (order.reason) {
			$('#warning-text').show();
		} else
			$('#warning-text').hide();
		$('#confirm-order-shipment').off('click').on('click', order,
				function(e) {
					order.reason = null;
					$.CM.processOrder(order);
				});
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
													+ (orderDetail.salePrice ? orderDetail.salePrice
															: 0) + "\"/>";
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
											return $('<div>').append(s).html();

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
											return $('<div>').append(s).html();
										}
									},
									{
										"mData" : function(orderDetail) {
											return '<button type="button" class="btn btn-info btn-xs pull-left " onclick="b($($(this).parent()).parent());"> <i class="icon-ok"></i>Update</button>';
										}
									} ],
							"aaData" : orderTemp.oimOrderDetailses,
							"bDestroy" : true
						});
		GenericBinder('order', orderTemp);
		if (!orderTemp.shippingMethod) {
			$('#shippingMethods').parents('.row').addClass('has-warning');
		}
		$('#updateorder').unbind("click").on('click', orderTemp, function(e) {
			$(this).CRUD({
				method : "PUT",
				url : 'aggregators/orders',
				data : JSON.stringify(e.data),
				success : function(data, textStatus, jqXHR) {
					$.gritter.add({
						title : "Update Order",
						text : "Order updated successfully."
					});
					$('#myModaledit').modal('hide');
					table_xy.ajax.reload();
				},
				error : function(a, c, b) {
					$.gritter.add({
						title : "Update Order",
						text : "Error in updating Order."
					});
				}
			});
		});
	}

	jQuery(function($) {
		$.CM.updateOrderSummary();
		$(this).CRUD({
			type : "GET",
			url : "aggregators/suppliers/shippingmethods",
			message : true,
			cache : true,
			success : function(json) {
				var data = new Array();
				$.each(json, function(i, e) {
					data.push({
						label : e.fullName,
						value : e
					});
				});
				$("#shippingMethods").autocomplete({
					minLength : 0,
					appendTo : $("#shippingMethods").parent(),
					source : data,
					select : function(event, ui) {
						event.preventDefault();
						$('#shippingMethods').val(ui.item.label);
						editOrder.shippingMethod = ui.item.value;
					}
				});
			}
		});
		$(this).CRUD({
			url : "aggregators/suppliers",
			method : "GET",
			success : function(data) {
				MY_SUPPLIERS = data;
			}
		});

		table_xy = $('#tableprocesschannel')
				.DataTable(
						{
							"order" : [ 2, "desc" ],
							"bProcessing" : true,
							"serverSide" : true,
							"sAjaxDataProp" : 'data',
							"ajax" : function(data, callback, settings) {
								var d = $.CM.planify(data);
								$(this)
										.CRUD(
												{
													method : "POST",
													url : 'aggregators/orders/unresolved',
													cache : true,
													message : true,
													data : JSON.stringify(d),
													success : function(result) {
														var json = result.data;
														for (var i = 0; i < json.length; i++) {
															var order = json[i];
															order.unresolved = false;
															var sameSupplier = true;
															var firstSupplier = null;
															for (var j = 0; j < order.oimOrderDetailses.length; j++) {
																var od = order.oimOrderDetailses[j];
																if (!od.oimSuppliers
																		|| od.oimSuppliers.supplierId <= 0
																		|| (firstSupplier && firstSupplier.supplierId != od.oimSuppliers.supplierId)
																		|| !od.salePrice
																		|| od.salePrice <= 0
																		|| !od.quantity
																		|| od.quantity <= 0) {
																	order.unresolved = true;
																	if ((firstSupplier
																			&& od.oimSuppliers && firstSupplier.supplierId != od.oimSuppliers.supplierId))
																		order.reason = 'This order has multiple suppliers. Edit the product details below, or click “Confirm” to process as a multiple supplier order.';
																	break;
																}
																firstSupplier = od.oimSuppliers;
															}
															if (!order.shippingMethod) {
																order.unresolved = true;
															}
															if (!order.deliveryStateCode) {
																order.unresolved = true;
															}
														}
														callback(result);
													}
												});
							},
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
										"mData" : "orderNumber"
									},
									{
										"mData" : "orderTmString"
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
										"mData" : function(order) {
											return order.oimOrderDetailses.length;
										},
										"orderable" : false
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
										}
									},
									{
										"mData" : function(order) {
											if (!order.unresolved) {
												return '<div class="panel panel-default"><a class="btn btn-success hidden-xs" href="javascript:;" onclick="process($(this).parent().parent().parent());">Process</a><a class="btn btn-success visible-xs btn-xs" href="javascript:;" onclick="process($(this).parent().parent().parent());">Process</a></div>';
											} else {
												var ret = '<div class="panel panel-default"><a class="btn  btn-danger hidden-xs" href="#myModaledit" onclick="showResolve($(this).parent().parent().parent());" data-toggle="modal">Resolve</a><a class="btn btn-danger visible-xs btn-xs" href="#myModalResolvemob"  onclick="a($($($(this).parent()).parent().parent()));" data-toggle="modal">Resolve</a></div>';
												if (!order.shippingMethod) {
													ret += '<small>Shipping Mapping Error</small>';
												}
												if (!order.deliveryStateCode) {
													ret += '<small>This order is missing Delivery State Code. Please update to resolve.</small>';
												}
												return ret;
											}
										}
									} ]
						});
		var hash = window.location.hash;
		window.onhashchange = function() {
			var hash = window.location.hash;
			if (hash && hash == '#unprocessed') {
				table_xy.order([ 9, 'asc' ]).draw();
			} else {
				table_xy.order([ 9, 'desc' ]).draw();
			}
		};
		if (hash && hash == '#unprocessed') {
			//table_xy.order([ 9, 'asc' ]).draw();
		} else if (hash == '#unresolved') {
			//table_xy.order([ 9, 'desc' ]).draw();
		} else {
			//table_xy.order([ 2, 'desc' ]).draw();
		}
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
								$(this).closest('tr').toggleClass('selected');
							});

				});
		$('#recent-box [data-rel="tooltip"]').tooltip({
			placement : tooltip_placement
		});
		function tooltip_placement(context, source) {
			var $source = $(source);
			var $parent = $source.closest('.tab-content')
			var off1 = $parent.offset();
			var w1 = $parent.width();

			var off2 = $source.offset();
			var w2 = $source.width();

			if (parseInt(off2.left) < parseInt(off1.left) + parseInt(w1 / 2))
				return 'right';
			return 'left';
		}

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
													var order = table_xy.row(
															$(this).parents(
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
												data : JSON.stringify(orders),
												method : 'POST',
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
													$.CM.updateOrderSummary();
												}
											});
						});
		
	

	});
</script>
</jsp:attribute>
</t:basepage>
