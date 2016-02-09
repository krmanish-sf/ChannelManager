<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
<div class="main-content">
				<div class="breadcrumbs" id="breadcrumbs">
					<ul class="breadcrumb">
						<li><a href="index.jsp"><i class="icon-home home-icon"></i>Home</a>
						</li>
						<li class="active">Channels</li>
					</ul>
					<!-- .breadcrumb -->

					<!-- <div class="nav-search" id="nav-search">
						<form class="form-search">
							<span class="input-icon"> <input placeholder="Search ..."
							class="nav-search-input" id="nav-search-input" autocomplete="off"
							type="text"> <i class="icon-search nav-search-icon"></i>
							</span>
						</form>
					</div> -->
					<!-- #nav-search -->
				</div>
				<div class="page-content">
					<div class="page-header">
						<h1>
							Channels <small> <i class="icon-double-angle-right"></i>
								overview &amp; stats
							</small>
						</h1>
					</div>
					<!-- /.page-header -->

					<div class="row">
						<div class="space-6"></div>
						<!--	<div class="vspace-sm"></div>-->

						<div class="col-sm-8">
							<div class="row container">
								<h4 class="lighter pull-left">
									<i class="icon-random orange"></i> My Channels
								</h4>
								<a class="btn btn-purple pull-right" href="#mychanneledit"
								data-toggle="modal" onclick="channel('add')">Add New Channel</a>
							</div>
							<div class="space-2"></div>
							<div class="widget-main no-padding table-responsive">
																<table
								class="table table-bordered table-striped dataTable"
								id="tableimportchannel">
										<thead class="thin-border-bottom">
											<tr>
												<th><i class="icon-key icon-2x visible-xs "></i><span
											class="hidden-xs">Name</span></th>
												<th><i class="icon-globe icon-2x visible-xs "></i><span
											class="hidden-xs">Url</span></th>
												<th><i class="icon-flag icon-2x visible-xs "></i><span
											class="hidden-xs">Type</span></th>
												<th class=" width-20"><i
											class="icon-adjust icon-2x visible-xs "></i><span
											class="hidden-xs">Actions</span></th>
												<th id ="edit-delete" class=" width-20"></th>
											</tr>
										</thead>
									</table>
							</div>
							<!-- /widget-main -->
						</div>
						<div class="col-sm-4">
							<div class="widget-container-span">
								<div class="widget-box">
									<div class="widget-header header-color-green">
										<h5>Tip:</h5>
										<div class="widget-toolbar">
											<a href="#" data-action="close"> <i
											class="icon-remove white"></i>
											</a>
										</div>
									</div>
									<div class="widget-body alert-success">
										<div class="widget-main">
											<p>A channel is simply any marketplace where you sell
												products from. It can be eBay, Amazon, or your own website
												or blog.</p>
										</div>
									</div>
								</div>
								<div class="space-8"></div>
							</div>
						</div>
					</div>
				</div>
				<!-- /.page-content -->
			</div>
<jsp:include page="include/channel-add-edit.jsp"></jsp:include>
  <!-- SINGLE ORDER ENTRY BEGINS -->
<jsp:include page="include/single-order-import.jsp"></jsp:include>
<!-- /SINGLE ORDER ENTRY ENDS -->
<!-- UPLOAD ORDER FILE BEGIN -->
<jsp:include page="include/upload-order-file.jsp"></jsp:include>
<!-- /UPLOAD ORDER FILE END-->
<jsp:include page="include/channel-shipping-mapping.jsp"></jsp:include>
</jsp:attribute>
	<jsp:attribute name="pagejs">
	<!-- inline scripts related to this page -->
	<script type="text/javascript">
		var tableimportchannel;
		jQuery(function($) {
			$(document).on('click', '.btn-delete-product', function(e) {
				$(this).parent().parent().parent().remove();
			});
			$(this).CRUD(
					{
						url : "aggregators/channels/supported-channels",
						method : "GET",
						success : function(data) {
							$('#channelselect').empty();
							$("<option/>").val("").html("Select Channel")
									.appendTo($('#channelselect'));
							$.each(data, function() {
								$("<option/>").val(this.supportedChannelId)
										.html(this.channelName).appendTo(
												$('#channelselect'));
							});
						}
					});
			tableimportchannel = $.CM.bindChannels('#tableimportchannel');

			$("#add_product_row").click(function() {
				var $tr = $("#product_row");
				var $clone = $tr.clone();
				$clone.find(':text').val('');
				$tr.after($clone);
				$clone.find('.btn-delete-product').show();
			});
			try {
				$('#prod_table').DataTable();
			} catch (e) {
			}

			$('#tablesuppliers')
					.DataTable(
							{
								"sDom" : "t",
								"sZeroRecords" : 'Note: Once you add a Supplier, you can edit this field.',
								"sAjaxSource" : 'aggregators/suppliers',
								"fnServerData" : function(sSource, aoData,
										fnCallback, oSettings) {
									oSettings.jqXHR = $(this).CRUD({
										type : "GET",
										url : sSource,
										data : aoData,
										success : fnCallback
									});
								},
								"sAjaxDataProp" : '',
								"aoColumns" : [
										{
											"mData" : function(s) {
												return '<input type="checkbox" class="toggle" name="sid_'+s.oimSuppliers.supplierId+'" data-bind-channel="customMapper:oimChannelSupplierMaps[oimSuppliers.supplierId='+s.oimSuppliers.supplierId+'].oimSuppliers.supplierId" value="'+s.oimSuppliers.supplierId+'"/>';
											}
										},
										{
											"mData" : "oimSuppliers.supplierName"
										},
										{
											"mData" : function(s) {
												return '<input type="text" class="width-100 pp"  style="display:none;" name="ss_'+s.oimSuppliers.supplierId	+'_skuprefix" id="ss_'+s.oimSuppliers.supplierId+'_skuprefix" data-bind-channel="customMapper:oimChannelSupplierMaps[oimSuppliers.supplierId='+ s.oimSuppliers.supplierId+'].supplierPrefix"/>';
											}
										},
										{
											"mData" : function(s) {
												return '<select id="ss_' +s.oimSuppliers.supplierId	+'_enableorderauto" class="pp" style="display:none;" name="ss_'+ s.oimSuppliers.supplierId	+'_enableorderauto" data-bind-channel="customMapper:oimChannelSupplierMaps[oimSuppliers.supplierId='+ s.oimSuppliers.supplierId	+ '].enableOrderAutomation" required="required"><option selected="" value="1">Yes</option><option value="0">No</option></select>';
											}
										},
										{
											"mData" : function(s) {
												if(s.oimSuppliers.supplierId == 1822){
													return '<input type="text" class="width-100 pp hide"  style="display:none;" name="ss_'+s.oimSuppliers.supplierId	+'_whLocation" id="ss_'+s.oimSuppliers.supplierId+'_warehouseLocation" data-bind-channel="customMapper:oimChannelSupplierMaps[oimSuppliers.supplierId='+ s.oimSuppliers.supplierId+'].warehouseLocation" keyattr=""/>'+'<select multiple id="warehouseLocation" class="pp" style="display:none;" name="ss_'+ s.oimSuppliers.supplierId	+'_warehouseLocation" required="required" ><option value="PHI">PHI</option><option value="HVA">HVA</option></select>';
												}
												else{
													return '<span></span>';
												}
											}
										} ]
							});
			$('#channelselect').off('change').on('change', function() {
				var val = $(this).val();
				$.CM.resetChannelForm(val);
			});

			$('#customer_shipping')
					.on(
							'click',
							function() {
								var customer_details = $('#CUSTOMER table input');
								var billing_details = $('#SHIPPING table input');
								var checked = $(this).is(':checked');
								for (var i = 0; i < customer_details.length; i++) {
									billing_details[i].value = (checked ? customer_details[i].value
											: '');
								}
							});
			$('#customer_billing')
					.on(
							'click',
							function() {
								var customer_details = $('#CUSTOMER table input');
								var billing_details = $('#BILLING table input');
								var checked = $(this).is(':checked');
								for (var i = 0; i < customer_details.length; i++) {
									billing_details[i].value = (checked ? customer_details[i].value
											: '');
								}
							});
			$('#shipping_billing')
					.on(
							'click',
							function() {
								var customer_details = $('#SHIPPING table input');
								var billing_details = $('#BILLING table input');
								var checked = $(this).is(':checked');
								for (var i = 0; i < customer_details.length; i++) {
									billing_details[i].value = (checked ? customer_details[i].value
											: '');
								}
							});
			$('#singleOrderForm')
					.validate(
							{

								invalidHandler : function(event, validator) {
									// 'this' refers to the form
									var errors = validator.numberOfInvalids();
									if (errors) {
										var message = errors == 1 ? 'You missed 1 field. It has been highlighted'
												: 'You missed '
														+ errors
														+ ' fields. They have been highlighted';

										$.gritter.add({
											title : "Single order information",
											text : message
										});
									}
								},

								highlight : function(e) {
									$(e).closest('.form-group').removeClass(
											'has-info').addClass('has-error');
								},
								success : function(e) {
									$(e).closest('.form-group').removeClass(
											'has-error').addClass('has-info');
									$(e).remove();
								},
								submitHandler : function(form) {
									var order = {};
									var req = [ 'sku', 'quantity', 'saleprice' ];
									var valid = true;
									$("#singleOrderForm :input")
											.each(
													function() {
														if (this.name) {
															if (req
																	.indexOf(this.name) >= 0) {
																if (!this.value) {
																	alert(this.name
																			+ " Required");
																	this
																			.focus();
																	valid = false;
																	return false;
																}
															}
															order[this.name] = this.value;
														}
													});
									if (!valid)
										return false;
									$(this).CRUD(
											{
												method : 'PUT',
												url : '/aggregators/channels/'
														+ e.data.channelId
														+ '/orders',
												data : JSON.stringify(order)
											});
								}
							});
			$('#channelForm')
					.validate(
							{
								//ignore : [],
								invalidHandler : function(event, validator) {
									// 'this' refers to the form
									var errors = validator.numberOfInvalids();
									if (errors) {
										var message = errors == 1 ? 'You missed 1 field. It has been highlighted'
												: 'You missed '
														+ errors
														+ ' fields. They have been highlighted';

										$.gritter.add({
											title : "Add/Edit Channel",
											text : message
										});
									}
								},

								highlight : function(e) {
									$(e).closest('.form-group').removeClass(
											'has-info').addClass('has-error');
								},
								success : function(e) {
									$(e).closest('.form-group').removeClass(
											'has-error').addClass('has-info');
									$(e).remove();
								},
								submitHandler : function(form) {
									function submitForm(b) {
										if (b) {
											var formArray = $(form)
													.serializeArray();
											var formObject = {};
											var count=0;
											$.each(formArray, function(i, v) {
												if(v.name=='ss_1822_warehouseLocation' && count==0){
													v.name='ss_1822_warehouseLocation1';
													count++;
												}
												formObject[v.name] = v.value;
											});
											$(this)
													.CRUD(
															{
																method : "PUT",
																url : $(form)
																		.attr(
																				'action'),
																data : JSON
																		.stringify(formObject),
																success : function() {
																	$(
																			'#mychanneledit')
																			.modal(
																					'hide');
																	tableimportchannel.ajax
																			.reload();
																},
																error : function(
																		jqXhr,
																		textStatus,
																		errorThrown) {
																	if (jqXhr.status == 409) {
																		$.gritter
																				.add({
																					title : "Add Channel",
																					text : "Channel with this name already exists."
																				});
																		$(
																				'a[href="#collapseOne"]')
																				.click();
																	}
																}
															});
										}
									}
									//debugger;
									var warn = false;
									$(form)
											.find('input[name=updateorders]')
											.each(
													function(i, el) {
														if (el.value == 0
																&& $(el)
																		.is(
																				':checked')) {
															warn = true;
														}
													});
									if (warn)
										bootbox
												.confirm(
														'<p class="text-info"><i class="icon-info"></i> We recommend that you have the order status change on your channel. If the value is not	changed, you will need to adjust the status on your channel	for the orders you have processed to prevent duplicates.</p>',
														function(b) {
															submitForm(b);
														});
									else
										submitForm(true);

								}
							});
			$('.first-button').click(function(e) {
				if ($('#channelForm').valid()) {
					$('a[href="#collapseTwo"]').click();
				}
			});
			$('.second-button').click(function(e) {
				if ($('#channelForm').valid()) {
					$('a[href="#collapseThree"]').click();
				}
			});
			$(document).on('change', '.toggle', function(e) {
				if ($(this).is(':checked')) {
					$(this).parent().parent().find('.pp').show();
				} else {
					$(this).parent().parent().find('.pp').hide();
				}
			});
		});

		function del(e) {
			bootbox
					.confirm(
							'This will remove this channel record.  Click <b>OK</b> to remove it fully.',
							function(b) {
								if (b) {
									var channel = tableimportchannel.row(e[0])
											.data();
									$(this)
											.CRUD(
													{
														method : "DELETE",
														url : '/aggregators/channels/'
																+ channel.channelId,
														success : function(
																data,
																textStatus,
																jqXHR) {
															tableimportchannel
																	.row(e[0])
																	.remove()
																	.draw();
															$.gritter
																	.add({
																		title : "Delete Channel",
																		text : "Channel deleted."
																	});
														},
														error : function(data,
																textStatus,
																jqXHR) {
															$.gritter
																	.add({
																		title : "Delete Channel",
																		text : "Error occured in deleting channel."
																	});
														}
													});
								} else {
									$.gritter.add({
										title : "Delete Channel",
										text : "Action cancelled"
									});
								}
							});
		}

		function channel(cmd, e) {
			switch (cmd) {
			case "add":
				$('#modalTitle').text("Add Channel");
				$("#channelForm").find('input').each(function(i, el) {
					el = $(el);
					if (el.is(':radio') || el.is(':checkbox')) {
						el.prop('checked', false);
					} else
						el.val('');
				});
				$("#channelForm").find('select').val('');
				$('#channelForm').attr('action', 'aggregators/channels');
				$('#btnSave').off('click').on('click', function(e) {
					$('#channelForm').validate();
					$('#channelForm').submit();
				});
				break;
			case "edit":
				$('#modalTitle').text("Edit Channel");
				var channel = JSON.parse(JSON.stringify(tableimportchannel.row(
						e[0]).data()));
				$('#channelForm').attr('action',
						'aggregators/channels/' + channel.channelId);
				GenericBinder('channel', channel);
				$('#ss_1822_warehouseLocation').trigger("chosen:updated");
				var location = $("#ss_1822_warehouseLocation").attr('keyattr'); // PHI~HVA , PHI, HVA
				var valArr = [];
				if(location){
				if(location.indexOf('~')!=-1){
					//show both
					valArr.push('HVA');
					valArr.push('PHI');
				}
				else if(location=='PHI'){
					valArr.push('PHI');
				}
				else if(location == 'HVA')
					valArr.push('HVA');
				//warehouseLocation
				
				i = 0, size = valArr.length;
				for(i; i < size; i++){
				  $("#warehouseLocation option[value='" + valArr[i] + "']").attr("selected", 1);
				}
			}
				$('#btnSave').off('click').on('click', channel, function(e) {
					$('#channelForm').validate();
					$('#channelForm').submit();
				});
				break;
			}
			$.CM.resetChannelForm($('#channelselect').val());
			if (!$('#collapseOne').hasClass('in'))
				$('a[href="#collapseOne"]').click();
		}

		function singleorder(e) {
			var channel = tableimportchannel.row(e[0]).data();
			$(this)
					.CRUD(
							{
								method : "GET",
								url : "/aggregators/suppliers",
								success : function(data) {
									$('[name=supplier]')
											.each(
													function() {
														var drop = $(this);
														$
																.each(
																		data,
																		function() {
																			var html = '<option value="'+this.oimSuppliers.supplierId+'">'
																					+ this.oimSuppliers.supplierName
																					+ '</option>';
																			drop
																					.append(html);
																		});

													});
								}

							});
			$('#singleOrderForm').find('input').val('');
			$('#btnSaveOrder').off('click');
			$('#btnSaveOrder').on('click', channel, function(e) {
				$('#singleOrderForm').submit();
			});
		}

		function uploadFile(e) {
			var channel = tableimportchannel.row(e[0]).data();
			$('#tablerow').hide();
			$('#filetype').off('change').on('change', function() {
				if ($(this).val() == "-1") {
					$('.filetype').show();
				} else {
					$('.filetype').hide();
				}
			});
			$(this)
					.CRUD(
							{
								method : "GET",
								url : "/aggregators/channels/"
										+ channel.channelId + "/filetypes",
								success : function(data) {
									$('#filetype').empty();
									$('#filetype')
											.append(
													'<option value="-1">Set up a new file type</option>');
									$(data)
											.each(
													function(i, e) {
														$('#filetype')
																.append(
																		'<option value="'+e.fileTypeId+'">'
																				+ e.fileTypeName
																				+ '</option>');
													});
								},
								error : function(data, jqXhr, msg) {

								},
								message : true
							});

			$('#submitOrderFile')
					.off('click')
					.on(
							'click',
							channel,
							function(e) {
								var formdata = $("#fileuploadform")
										.serializefiles();
								$(this)
										.CRUD(
												{
													method : 'POST',
													url : 'aggregators/channels/'
															+ e.data.channelId
															+ '/uploadfile',
													data : formdata,
													processData : false,
													contentType : false,
													success : function(data) {
														$('#submitOrderFile')
																.off('click')
																.on(
																		'click',
																		data,
																		function(
																				e) {
																			var data = e.data;
																			if (data.header) {
																				data.header = data.header.length;
																				data.colcount = data.header;
																			}
																			if (e.data.filetypeId > 0
																					|| checkCompulsaryMaps(e.data.header)) {
																				$(
																						"#fileuploadform")
																						.find(
																								"input,select")
																						.each(
																								function(
																										i,
																										e) {
																									if (this.name) {
																										data[this.name] = this.value;
																									}
																								});

																				$(
																						this)
																						.CRUD(
																								{
																									method : 'POST',
																									url : 'aggregators/channels/'
																											+ e.data.channelId
																											+ '/newfile',
																									data : JSON
																											.stringify(data),
																									success : function(
																											data) {
																										$(
																												'.delete-row')
																												.remove();
																										$(
																												"#fileuploadform")
																												.find(
																														"input,select")
																												.each(
																														function(
																																i,
																																e) {
																															this.value = '';
																														});
																										$(
																												'#submitOrderFile')
																												.text(
																														'Confirm')
																												.off(
																														'click')
																												.on(
																														'click',
																														data,
																														function(
																																e) {
																															$(
																																	this)
																																	.CRUD(
																																			{
																																				method : 'POST',
																																				url : 'aggregators/channels/'
																																						+ e.data.oimChannels.channelId
																																						+ '/confirmupload',
																																				data : JSON
																																						.stringify(e.data),
																																				success : function(
																																						data) {
																																					$(
																																							'#uploadordermodal')
																																							.modal(
																																									'hide');
																																					$.gritter
																																							.add({
																																								title : 'Order file processing',
																																								text : "Imported "
																																										+ data
																																										+ " Orders"
																																							});
																																				}
																																			});
																														});
																										$(
																												'#tablerow')
																												.show();
																										var dataTable = $(
																												'#tableorderdetails')
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
																																	} ],
																															"bDestroy" : true
																														})
																												.show();

																										for (var i = 0; i < data.oimOrderses.length; i++) {
																											for (var j = 0; j < data.oimOrderses[i].oimOrderDetailses.length; j++) {
																												dataTable
																														.fnAddData(data.oimOrderses[i].oimOrderDetailses[j]);
																											}
																										}
																									}
																								});
																			}
																			;
																		});
														if (data.filetypeId > 0) {
															$(
																	'#submitOrderFile')
																	.click();
														} else {
															var new_Row = $('#sample-row');
															$
																	.each(
																			data.header,
																			function(
																					i,
																					e) {
																				var newRow = new_Row
																						.clone();
																				newRow
																						.removeAttr('id');
																				newRow
																						.find(
																								'label')
																						.text(
																								e);
																				newRow
																						.find('input[type="hidden"]')[0].name += i;
																				newRow
																						.find(
																								'input[type="hidden"]')
																						.val(
																								e);
																				newRow
																						.find('select')[0].name += i;
																				newRow
																						.find('select')[0].id += i;
																				newRow
																						.show();
																				newRow
																						.addClass("delete-row");
																				newRow
																						.insertAfter('#sample-row');
																			});
														}
														$.gritter
																.add({
																	title : 'Order file upload',
																	text : data.status
																});
													}
												});
							});
		}
		function checkCompulsaryMaps(numCols) {
			var reqdfields = new Array();
			reqdfields["1"] = "SKU";
			reqdfields["2"] = "Product Order Number";
			reqdfields["3"] = "Delivery Name";
			reqdfields["4"] = "Delivery Address";
			//reqdfields["5"] = "Delivery City";
			//reqdfields["6"] = "Delivery State";
			//reqdfields["8"] = "Delivery Country";
			reqdfields["10"] = "Ship Method";
			reqdfields["9"] = "Quantity";

			var mappings = new Array();
			for (i = 0; i < 15; i++) {
				mappings["" + i] = 0;
			}

			for (i = 0; i < numCols; i++) {
				var mapid = $("#colindex_" + i).val();
				mappings["" + mapid] = mappings["" + mapid] + 1;
			}

			for (i in reqdfields) {
				//alert("checking mapping for "+reqdfields[i]+" => "+mappings[i]);
				if (mappings[i] == 0) {
					alert(reqdfields[i] + " Mapping Missing");
					return false;
				}
				if (mappings[i] > 1) {
					alert(reqdfields[i] + " has multiple mappings. "
							+ reqdfields[i]
							+ " field needs to be uniquely mapped to a column.");
					return false;
				}
			}
			return true;
		}
		function copyFields(from_prefix, to_prefix) {
			$('#' + to_prefix + 'NAME').val(('#' + from_prefix + 'NAME').val());
			$('#' + to_prefix + 'STREET_ADDRESS').val(
					('#' + from_prefix + 'STREET_ADDRESS').val());
			$('#' + to_prefix + 'SUBURB').val(
					('#' + from_prefix + 'SUBURB').val());
			$('#' + to_prefix + 'CITY').val(('#' + from_prefix + 'CITY').val());
			$('#' + to_prefix + 'STATE').val(
					('#' + from_prefix + 'STATE').val());
			$('#' + to_prefix + 'COUNTRY').val(
					('#' + from_prefix + 'COUNTRY').val());
			$('#' + to_prefix + 'ZIP').val(('#' + from_prefix + 'ZIP').val());
			$('#' + to_prefix + 'COMPANY').val(
					('#' + from_prefix + 'COMPANY').val());
			$('#' + to_prefix + 'COMPANY').val(
					('#' + from_prefix + 'COMPANY').val());
			$('#' + to_prefix + 'PHONE').val(
					('#' + from_prefix + 'PHONE').val());
			$('#' + to_prefix + 'EMAIL').val(
					('#' + from_prefix + 'EMAIL').val());
		}
	</script>

	
	</jsp:attribute>
</t:basepage>
