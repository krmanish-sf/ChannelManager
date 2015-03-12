<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>
<div class="modal fade" id="uploadordermodal" tabindex="-1"
	role="dialog" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="modalTitle">Upload Order File</h4>
			</div>
			<div class="modal-body ">
				<div class="row">
					<div class="container">
						<form role="form" class="form-horizontal" method="post"
							id="fileuploadform" action="aggregators/channels/uploadfile">
							<div class="form-group">
								<label class="col-sm-5 control-label no-padding-right">Select
									the file</label>
								<div class="col-sm-7">
									<input type="file" name="upload" />
									<!-- <div class="ace-file-input width-70">
										<input type="file" name="upload"/><label
											data-title="Choose" class="file-label"><span
											data-title="Select Order file ..." class="file-name"><i
												class="icon-upload-alt"></i></span></label><a href="#" class="remove"><i
											class="icon-remove"></i></a>
									</div> -->
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-5 control-label no-padding-right">Select
									file type</label>
								<div class="col-sm-7">
									<select name="filetype" id="filetype"
										class="width-70 form-control">
									</select>
								</div>
							</div>
							<div class="form-group filetype">
								<label class="col-sm-5 control-label no-padding-right">Name</label>
								<div class="col-sm-7">
									<input type="text" value="" name="filename"
										class="width-70 form-control">
								</div>
							</div>
							<div class="form-group filetype">
								<label class="col-sm-5 control-label no-padding-right">Has
									Header</label>
								<div class="col-sm-7">
									<select name="hasheader" class="width-70 form-control">
										<option value="yes">Yes</option>
										<option value="no">No</option>
									</select>
								</div>
							</div>
							<div class="form-group filetype">
								<label class="col-sm-5 control-label no-padding-right">Field
									Delimiter</label>
								<div class="col-sm-7">
									<select name="fieldDelimiter" class="width-70 form-control">
										<option value="TAB">Tab</option>
										<option value="COMMA">Comma</option>
									</select>
								</div>
							</div>
							<div class="form-group filetype">
								<label class="col-sm-5 control-label no-padding-right">Text
									Delimiter</label>
								<div class="col-sm-7">
									<select id="textDelimiter" name="textDelimiter"
										class="width-70 form-control">
										<option value=""></option>
										<option value="QUOTES">Quotes</option>
									</select>
								</div>
							</div>
							<div id="sample-row" class="form-group upload-column-mapping"
								style="display: none;">
								<label class="col-sm-5 control-label no-padding-right"></label>
								<div class="col-sm-7">
									<input type="hidden" name="colindex_mapped_header_" value="" />
									<select name="colindex_fieldid_" id="colindex_"
										class="width-70 form-control">
										<option value="0">Ignore</option>
										<option value="1">SKU</option>
										<option value="2">Product Order Number</option>
										<option value="24">Product Name</option>
										<option value="25">Product Description</option>
										<option value="26">Product Cost</option>
										<option value="27">Product Sale Price</option>
										<option value="28">Order Total</option>
										<option value="3">Delivery Name</option>
										<option value="4">Delivery Address</option>
										<option value="5">Delivery City</option>
										<option value="6">Delivery State</option>
										<option value="7">Delivery Zip</option>
										<option value="8">Delivery Country</option>
										<option value="9">Quantity</option>
										<option value="10">Ship Method</option>
										<option value="11">Delivery Company</option>
										<option value="12">Delivery Suburb</option>
										<option value="29">Order Status</option>
									</select>
								</div>
							</div>
							<div class="row" id="tablerow">
								<div class="container">
									<table id="tableorderdetails"
										class="table table-striped table-bordered table-hover table-responsive">
										<thead>
											<tr>
												<th>Sku</th>
												<th>Name</th>
												<th>Quantity</th>
												<th>Sale Price</th>
												<!-- 	<th>Status</th> -->
											</tr>
										</thead>
										<tbody>
											<!-- 	<tr>
												<td><input type="text" value="RS1234" readonly=""
													class="pull-right width-100" name="billno"></td>
												<td><input type="text" value="Ravish Test Product"
													readonly="" class="pull-right width-100" name="orderdate"></td>
												<td><input type="text" value="4" readonly=""
													class="pull-right width-100" name="Quantity"></td>
												<td><input type="text" value="0.0" readonly=""
													class="pull-right width-100" name="SalePrice"></td>
												<td><input type="text" value="Unprocessed" readonly=""
													class="pull-right width-100" name="Status"></td>
											</tr> -->
										</tbody>
									</table>
								</div>
							</div>
							<div class="clearfix form-actions">
								<div class="col-md-offset-3 col-md-9">
									<button type="button" class="btn btn-info" id="submitOrderFile">
										<i class="icon-ok"></i> Submit
									</button>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
