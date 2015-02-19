<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
														<option value="" selected>Select channel</option>
														<option value="4">Amazon Seller Central</option>
														<option value="1">CRE Loaded</option>
														<option value="3">Inventory Source - Hosted</option>
														<option value="2">Magento</option>
														<option value="5">Yahoo Store</option>
														<option value="6">Zen Cart</option>
														<option value="0">Custom Channel</option>
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
												<label for="merchanttoken"
													class="col-sm-5 control-label no-padding-right">Merchant
													Token:</label>
												<div class="col-sm-7">
													<input type="text" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=9].detailFieldValue"
														name="merchanttoken" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info amazon-store">
												<label for="amazonuser"
													class="col-sm-5 control-label no-padding-right">Amazon
													UserName:</label>
												<div class="col-sm-7">
													<input type="text" required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=10].detailFieldValue"
														name="amazonuser" class="width-70" />
												</div>
											</div>
											<div class="form-group store-info amazon-store">
												<label for="amazonpass"
													class="col-sm-5 control-label no-padding-right">Amazon
													Password:</label>
												<div class="col-sm-7">
													<input type="text" name="amazonpass" class="width-70"
														required="required"
														data-bind-channel="customMapper:oimChannelAccessDetailses[oimChannelAccessFields.fieldId=11].detailFieldValue" />
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
												To Process From Channel</label>
											<div class="col-sm-7">
												<input type="radio" value="1" name="processorders"
													id="processordersall" required="required"
													onclick="javascript:hideNext(this);"
													data-bind-channel="oimOrderProcessingRules[0].processAll" />
												Process All Orders<br /> <input type="radio" value="0"
													name="processorders" id="processorderscustom"
													required="required" onclick="javascript:toggleNext(this);"
													data-bind-channel="oimOrderProcessingRules[0].processAll" />
												Process only orders with status set to<br> <input
													type="text" required="required"
													name="processorderscustomstatus"
													id="processorderscustomstatus"
													data-bind-channel="oimOrderProcessingRules[0].processWithStatus" />
											</div>
										</div>
										<div class="form-group">
											<label class="col-sm-5 control-label no-padding-right">Edit
												Order When Complete</label>
											<div class="col-sm-7">
												<input type="radio" value="0" name="updateorders"
													id="updateordersno" required="required"
													onclick="javascript:hideNext(this);"
													data-bind-channel="oimOrderProcessingRules[0].updateStoreOrderStatus" />Do
												not update order status <br /> <input type="radio"
													value="1" name="updateorders" id="updateordersyes"
													onclick="javascript:toggleNext(this);" required="required"
													data-bind-channel="oimOrderProcessingRules[0].updateStoreOrderStatus" />Set
												the order status to<br /> <input type="text"
													required="required" name="updateorderscustomstatus"
													id="updateorderscustomstatus"
													data-bind-channel="oimOrderProcessingRules[0].updateWithStatus" />
											</div>
										</div>
										<script type="text/javascript">
											function hideNext(el) {
												if ($(el).is(":checked")) {
													$(el)
															.parent()
															.find(
																	'input[type="text"]')
															.hide();
												} else {
													$(el)
															.parent()
															.find(
																	'input[type="text"]')
															.show();
												}
											}
											function toggleNext(el) {
												if ($(el).is(":checked"))
													$(el).parent().find(
															'input[type=text]')
															.show();
												else
													$(el).parent().find(
															'input[type=text]')
															.hide();
											}
										</script>
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
