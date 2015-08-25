<%@page import="salesmachine.util.ApplicationProperties"%>
<%@page import="salesmachine.oim.api.OimConstants"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>
<div class="modal fade" id="mychanneledit" tabindex="-1" role="dialog"
	aria-hidden="true">
	<div id="mychanneleditdailog" class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="modalTitle">Add Channel</h4>
			</div>
			<div class="modal-body ">
				<div class="row">
					<div class="container">
						<form action="/aggregators/channels" method="PUT"
							class="form-horizontal" role="form" id="channelForm">
							<div id="accordion" class="accordion-style1 panel-group">
								<div class="panel panel-default">
									<div class="panel-heading">
										<h4 class="panel-title">
											<a class="accordion-toggle" data-toggle="collapse"
												data-parent="#accordion" href="#collapseOne"> <i
												class="icon-angle-down bigger-110"
												data-icon-hide="icon-angle-down"
												data-icon-show="icon-angle-right"></i>&nbsp;Step 1 of 3 :
												Channel Type and Channel Access Details
											</a>
										</h4>
									</div>
									<div class="panel-collapse collapse" id="collapseOne">
										<div class="panel-body">
											<div class="form-group">
												<label class="col-sm-5 control-label no-padding-right">Channel
													Type</label>
												<div class="col-sm-7">
													<select name="supportedChannelId" required="required"
														data-bind-channel="oimSupportedChannels.supportedChannelId"
														id="channelselect" class="width-70 pull-left">
														<option selected>Please wait...</option>
													</select>
												</div>
											</div>
											<div class="form-group">
												<label class="col-sm-5 control-label no-padding-right">Channel
													Name</label>
												<div class="col-sm-7">
													<input type="text" name="channelname" class="width-70"
														data-bind-channel="channelName" required="required"
														placeholder="My Electronics Store" />
												</div>
											</div>
											<!-- <div class="form-group">
												<label for="emailupdates"
													class="col-sm-5 control-label no-padding-right no-padding-top">Receive
													order processing updates by email</label>
												<div class="col-sm-7">
													<label> <input type="checkbox" class="ace"
														name="emailupdates" data-bind-channel="emailNotifications" />
														<span class="lbl"></span>
													</label>
												</div>
											</div> -->
											<div class="form-group">
												<label for="storeurl"
													class="col-sm-5 control-label no-padding-right">Store
													URL</label>
												<div class="col-sm-7">
													<input type="url" name="storeurl" class="width-70"
														placeholder="http://www.example.com" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=1].detailFieldValue" />
												</div>
											</div>
											<div class="form-group store-info">
												<label for="ftpurl"
													class="col-sm-5 control-label no-padding-right">FTP
													URL</label>
												<div class="col-sm-7">
													<input type="url" id="ftpurl" name="ftpurl"
														pattern="[A-Za-z0-9.]+" placeholder="ftp.example.com"
														required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=2].detailFieldValue"
														class="width-70" />
												</div>
											</div>
											<div class="form-group store-info">
												<label for="ftplogin"
													class="col-sm-5 control-label no-padding-right">FTP
													Login</label>
												<div class="col-sm-7">
													<input type="text" id="ftplogin" required="required"
														placeholder="admin@example.com or admin"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=3].detailFieldValue"
														name="ftplogin" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info">
												<label for="ftppwd"
													class="col-sm-5 control-label no-padding-right">FTP
													Password:</label>
												<div class="col-sm-7">
													<input type="text" id="ftppwd" autocomplete="off"
														placeholder="Your FTP password" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=4].detailFieldValue"
														name="ftppwd" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info">
												<label for="scriptpath"
													class="col-sm-5 control-label no-padding-right">
													Script Path:</label>
												<div class="col-sm-7">
													<input type="url" autocomplete="off"
														placeholder="Script path" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=7].detailFieldValue"
														name="scriptpath" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info">
												<label for="authkey"
													class="col-sm-5 control-label no-padding-right">Authorization
													key:</label>
												<div class="col-sm-7">
													<input type="text" autocomplete="off"
														placeholder="Authorization Key" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=8].detailFieldValue"
														name="authkey" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info amazon-store">
												<label for="mws-seller-id"
													class="col-sm-5 control-label no-padding-right">Seller
													ID:</label>
												<div class="col-sm-7">
													<input type="text" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=14].detailFieldValue"
														name="mws-seller-id" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info amazon-store">
												<label for="mws-marketplace-id"
													class="col-sm-5 control-label no-padding-right">MWS
													Marketplace ID:</label>
												<div class="col-sm-7">
													<input type="text" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=16].detailFieldValue"
														name="mws-marketplace-id" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info amazon-store">
												<label for="mws-auth-token"
													class="col-sm-5 control-label no-padding-right">MWS
													Auth Token:</label>
												<div class="col-sm-7">
													<input type="text" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=15].detailFieldValue"
														name="mws-auth-token" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info yahoo-store">
												<label for="yahoostoreid"
													class="col-sm-5 control-label no-padding-right">Yahoo
													Store ID:</label>
												<div class="col-sm-7">
													<input type="text" name="yahoostoreid" class="width-70"
														required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=12].detailFieldValue" />
												</div>
											</div>
											<div class="form-group store-info shop-store">
												<label for="catalog-id"
													class="col-sm-5 control-label no-padding-right">Shop.com
													Catalog ID </label>
												<div class="col-sm-7">
													<input type="text" name="catalog-id" class="width-70"
														placeholder="Enter shop.com Catalog ID"
														required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=13].detailFieldValue" />
												</div>
											</div>
											<div class="form-group store-info bc-store">
												<label for="store-hash"
													class="col-sm-5 control-label no-padding-right">Store Hash</label>
												<div class="col-sm-7">
													<input type="text" id="store-hash" name="store-hash"
														class="width-70" placeholder="Store Hash"
														required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=18].detailFieldValue"
														readonly />
												</div>
											</div>
											<div class="form-group store-info bc-store">
												<label for="catalog-id"
													class="col-sm-5 control-label no-padding-right">Auth
													Token</label>
												<div class="col-sm-7">
													<input type="text" id="bc-auth-token" name="bc-auth-token"
														class="width-70" placeholder="Auth Token"
														required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=8].detailFieldValue"
														readonly />
												</div>
											</div>
											<div class="form-group store-info shopify-store">
												<label for="catalog-id"
													class="col-sm-5 control-label no-padding-right">Auth
													Token</label>
												<div class="col-sm-7">
													<input type="text" id="shopifyAuthId" name="shopifyAuth-id"
														class="width-70" placeholder="Access Token"
														required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=17].detailFieldValue"
														readonly />
												</div>
											</div>
											<div class="form-group store-info bc-store">
												<div class="col-sm-5"></div>
												<div class="col-sm-7"><a style="cursor: pointer;"
															onclick="fetchBigcommerceAuthData();">Get Access Token</a></div>
											</div>
											<div class="form-group store-info shopify-store">
												<div class="col-sm-5"></div>
												<div class="col-sm-7"><a style="cursor: pointer;"
															onclick="openUrl()">Get Access Token</a></div>
											</div>
											<div class="form-group center">
												<!-- <input type="button"
															class="btn btn-success btn-sm" value="Test Settings" /> -->
												<button class="btn btn-info btn-sm first-button"
													type="button">
													<i class="icon-ok "></i>Next
												</button>
											</div>
										</div>
									</div>
								</div>
								<div class="panel panel-default">
									<div class="panel-heading">
										<h4 class="panel-title">
											<a class="accordion-toggle collapsed" data-toggle="collapse"
												data-parent="#accordion" href="#collapseTwo"> <i
												class="icon-angle-right bigger-110"
												data-icon-hide="icon-angle-down"
												data-icon-show="icon-angle-right"></i> &nbsp;Step 2 of 3 :
												Supplier Settings
											</a>
										</h4>
									</div>
									<div class="panel-collapse collapse" id="collapseTwo">
										<div class="panel-body">
											<div id="suppliers"></div>
											<table class="table table-bordered table-striped "
												id="tablesuppliers">
												<thead class="thin-border-bottom">
													<tr>
														<th class="sorting_disabled"><i class="blue"></i></th>
														<th class="sorting_disabled"><i class="blue"></i>
															Name</th>
														<th class="sorting_disabled"><i class="blue"></i> SKU
															prefix</th>
														<th class="sorting_disabled"><i class="blue"></i>
															Order automation</th>
													</tr>
												</thead>
												<tbody>
												</tbody>
											</table>
											<div class="form-group"></div>
											<div class="form-group center">
												<button class="btn btn-info btn-sm second-button"
													type="button">
													<i class="icon-ok"></i>Next
												</button>
											</div>
										</div>
									</div>
								</div>
								<div class="panel panel-default">
									<div class="panel-heading">
										<h4 class="panel-title">
											<a class="accordion-toggle collapsed" data-toggle="collapse"
												data-parent="#accordion" href="#collapseThree"> <i
												class="icon-angle-right bigger-110"
												data-icon-hide="icon-angle-down"
												data-icon-show="icon-angle-right"></i> &nbsp;Step 3 of 3 :
												Order Settings
											</a>
										</h4>
									</div>
									<div class="panel-collapse collapse" id="collapseThree">
										<div class="form-group">
											<label class="col-sm-5 control-label no-padding-right">Orders
												To Pull From Channel</label>
											<div class="col-sm-7">
												Pull orders with status set to<br> <input type="text"
													required="required" name="pull-with-status"
													data-bind-channel="oimOrderProcessingRules[0].pullWithStatus" />
											</div>
										</div>
										<div class="form-group">
											<label class="col-sm-5 control-label no-padding-right">Order
												Status When imported to Channel Manager</label>
											<div class="col-sm-7">
												<input type="text" required="required"
													name="confirmed-status"
													data-bind-channel="oimOrderProcessingRules[0].confirmedStatus" />
											</div>
										</div>
										<div class="form-group">
											<label class="col-sm-5 control-label no-padding-right">Order
												Status When Processed</label>
											<div class="col-sm-7">
												<input type="text" required="required"
													name="processed-status"
													data-bind-channel="oimOrderProcessingRules[0].processedStatus" />
											</div>
										</div>
										<div class="form-group">
											<label class="col-sm-5 control-label no-padding-right">Order
												Status When Failed</label>
											<div class="col-sm-7">
												<input type="text" required="required" name="failed-status"
													data-bind-channel="oimOrderProcessingRules[0].failedStatus" />
											</div>
										</div>
										<div class="form-group center">
											<button class="btn btn-info btn-sm" type="button"
												id="btnSave">
												<i class="icon-save"></i>Save
											</button>
										</div>
									</div>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<script>
function openUrl(){
var strUrl = document.getElementsByName('storeurl')[0].value;
if(strUrl){
 	var m = window.open(strUrl+'/admin/oauth/authorize?client_id=<%=ApplicationProperties.getProperty(ApplicationProperties.SHOPIFY_API_KEY)%>&scope=read_content,write_content,read_products,write_products,read_customers,write_customers,read_orders,write_orders,read_shipping,write_shipping,read_fulfillments,write_fulfillments', 'shopify Access Permission', 
 	'height=600,width=600,toolbar=no,directories=no,status=no,menubar=no,scrollbars=no,location=no,resizable=yes,modal=yes');
// var m = window.open(strUrl+'/admin/oauth/authorize?client_id=bc312f5e1bebb835d7a910516cd5c7b9&scope=read_content,write_content,read_products,write_products,read_customers,write_customers,read_orders,write_orders,read_shipping,write_shipping,read_fulfillments,write_fulfillments', 'shopify Access Permission',
// 'height=600,width=600,toolbar=no,directories=no,status=no,menubar=no,scrollbars=no,location=no,resizable=yes,modal=yes');
}
else{
	alert('Please enter Store url');
	}
}

function put(val){
	$('#shopifyAuthId').val(val);
}
</script>
