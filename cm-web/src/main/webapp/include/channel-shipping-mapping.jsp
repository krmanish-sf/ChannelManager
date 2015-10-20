<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>
<div class="modal fade" id="channelShippingModal" tabindex="-1"
	role="dialog" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h4 class="modal-title">Channel Shipping Mapping</h4>
			</div>
			<div class="modal-body ">
				<div class="row">
					<div class="container">
						<div class="col-sm-12">
							<table id="tableAddShippingMap"
								class="table table-bordered table-striped table-responsive">
								<input type="hidden" id="channelHidenField" />
								<tbody>
									<tr>
										<td><input type="text" id="shippingText"
											placeholder="Enter Shipping text" name="shippingText"
											required="required" /></td>
										<td><input type="text" id="shippingMappingVal"
											placeholder="Enter Shipping mapping"
											name="shippingMappingVal"
											class="form-control ui-autocomplete-input"
											required="required"></td>
										<td>
											<button type="button" id="addShipping"
												class="btn btn-info btn-xs">
												<i class="icon-ok "></i>Add
											</button>
										</td>
									</tr>
								</tbody>
							</table>
							<table id="tableShippingMap"
								class="table table-bordered table-striped table-responsive">
								<thead class="thin-border-bottom">
									<tr>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
											class="hidden-xs">Channel Shipping Method</span></th>
										<th><i class="icon-barcode icon-2x blue visible-xs"></i><span
											class="hidden-xs visible-sm">Shipping Carrier</span></th>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
											class="hidden-xs">Shipping Method</span></th>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
											class="hidden-xs">Edit/Delete</span></th>
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
</div>

<div class="modal fade" id="EditChannelShippingModal" tabindex="-1"
	role="dialog" aria-hidden="true">
	<div id="mysupplieradddailog" class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h4 class="modal-title">Shipping Methods</h4>
			</div>
			<div class="modal-body ">
				<div class="row">
					<div class="container">
						<div class="col-sm-12">
							<table id="tableChannelShippingMap"
								class="table table-bordered table-striped table-responsive">
								<thead class="thin-border-bottom">
									<tr>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
											class="hidden-xs">Shipping Text</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
											class="hidden-xs">Shipping Carrier and Method</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
											class="hidden-xs">Update</span></th>
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
</div>

<script type="text/javascript" src="static/js/jquery-2.0.3.min.js"></script>
<script type="text/javascript"
	src="static/js/jquery-ui-1.10.3.full.min.js"></script>
<script type="text/javascript">
	jQuery(function($) {
		var shippingMethod;
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
				$("#shippingMappingVal").autocomplete({
					minLength : 0,
					appendTo : $("#shippingMappingVal").parent(),
					source : data,
					select : function(event, ui) {
						event.preventDefault();
						$('#shippingMappingVal').val(ui.item.label);
						//console.log(ui.item.value);
						shippingMethod = ui.item.value;
					}
				});
			}
		});
		$("#addShipping").click(function() {
			if (!$("#shippingText").val() || !$("#shippingMappingVal").val()) {
				$.gritter.add({
					title : 'Add Shipping',
					text : "All fields are required to be filled.",
					class_name : 'gritter-error'
				});
				return;
			}
			if (!shippingMethod) {
				$.gritter.add({
					title : 'Add Shipping',
					text : "Invalid shipping method.",
					class_name : 'gritter-error'
				});
				return;
			}
			$.CM.addShippingMapping(shippingMethod);

		});
	});
</script>