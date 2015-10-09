<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="salesmachine.hibernatedb.Reps"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
<div class="main-content">
      <div id="breadcrumbs" class="breadcrumbs"> 
        <ul class="breadcrumb">
          <li><i class="icon-home home-icon active"></i>Home</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1> Dashboard <small> <i
							class="icon-double-angle-right"></i> overview &amp; stats </small> </h1>
        </div>
        <!-- /.page-header -->
        <div class="row">
              <!--	<div class="vspace-sm"></div>-->
              <div class="col-sm-12 infobox-container">
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
              </div>
            </div>
        <div class="row">
              <div id="jumbo2" class="col-sm-5 alert-info jumbotron">
                <div class="container">
                  <h2> Get Started </h2>
                  <ul class="nav nav-pills">
                    <li class="text-center">
                    <a href="javascript:$.CM.pullorder();"
									class="btn btn-info btn-app radius-4">
									<i class="icon-circle-arrow-left bigger-230"></i>
								</a>
                      <h6>Import Orders</h6>
                    </li>
                    <li class="text-center"><a
									href="processorder.jsp" class="btn btn-info btn-app radius-4"><i
										class="icon-cogs bigger-230"></i></a>
                      <h6>Process Orders</h6>
                    </li>
                    <li class="text-center"><a href="reportmax.jsp"
									class="btn btn-info btn-app radius-4"><i
										class="icon-archive bigger-230"></i></a>
                      <h6>View Reports</h6>
                    </li>
                  </ul>
                </div>
              </div>
              <div class="col-sm-7">
                <div class="widget-box transparent">
                  <div class="widget-header widget-header-flat">
                    <h4 class="lighter"> <i
										class="icon-star orange"></i> My Channels </h4>
                    <div class="widget-toolbar">
											<a class="btn btn-purple" onclick="channel('add')"
										data-toggle="modal" href="#mychanneledit">Add New Channel</a> <a
										data-action="collapse" href="#"> <i
										class="icon-chevron-up"></i> </a> </div>
                  </div>
                  <div class="widget-body">
                    <div id="tabledashmychannel"
									class="widget-main no-padding ">
                      <table id="tablechannels"
										class="table table-bordered table-striped dataTable">
										<thead class="thin-border-bottom">
											<tr>
												<th><i class="icon-key icon-2x visible-xs"></i><span
													class="hidden-xs">Name</span></th>
												<th><i class="icon-globe icon-2x visible-xs"></i><span
													class="hidden-xs">Url</span></th>
												<th><i class="icon-flag icon-2x visible-xs"></i><span
													class="hidden-xs">Type</span></th>
												<th class=" width-20"><i
													class="icon-adjust icon-2x visible-xs"></i><span
													class="hidden-xs">Actions</span></th>
												<th class=" width-20"></th>
											</tr>
										</thead>
									</table>
                    </div>
                    <!-- /widget-main --> 
                  </div>
                  <!-- /widget-body --> 
                </div>
                <!-- /widget-box --> 
              </div>
        </div>
        <div class="hr hr32 hr-dotted"></div>
        <div class="row">
          <div class="col-sm-5">
            <jsp:include page="include/leaderboard.jsp"></jsp:include>
            <!-- /widget-box --> 
          </div>
          <!-- /span -->
          <div class="col-sm-7">
          <jsp:include page="include/reportchart.jsp">
           <jsp:param value="Sales Summary" name="chartTitle" />
              <jsp:param value="sales-charts" name="targetId" />
              <jsp:param value="aggregators/reports/totalsales"
								name="reportUrl" />
				<jsp:param value="formatSalesData" name="dataFormatter" />
				<jsp:param value="drawLineChart" name="fnChart" />
				</jsp:include>
          </div>
        </div>
        <div class="hr hr-10 hr-dotted"></div>
        <!--  CHANNEL MODAL CONTENT -->
        <jsp:include page="include/channel-add-edit.jsp"></jsp:include>
         <!-- SINGLE ORDER ENTRY BEGINS -->
		<jsp:include page="include/single-order-import.jsp"></jsp:include>
		<!-- /SINGLE ORDER ENTRY ENDS -->
		<!-- UPLOAD ORDER FILE BEGIN -->
		<jsp:include page="include/upload-order-file.jsp"></jsp:include>
		<!-- /UPLOAD ORDER FILE END-->
        <!-- CHANNEL MODAL CONTENT ENDS --> 
        <jsp:include page="include/channel-shipping-mapping.jsp"></jsp:include>
      </div>
      <!-- /.col --> 
    </div>	
</jsp:attribute>
	<jsp:attribute name="pagejs">
<script type="text/javascript" src="https://www.google.com/jsapi"></script>	
<!-- inline scripts related to this page --> 

<script type="text/javascript">
	google.load('visualization', '1.1', {
		'packages' : [ 'corechart', 'bar', 'line' ]
	});
	var tableimportchannel;
	jQuery(function($) {
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
		$.CM.updateOrderSummary();
		$(this).CRUD(
				{
					url : "aggregators/channels/supported-channels",
					method : "GET",
					success : function(data) {
						$('#channelselect').empty();
						$("<option/>").val("").html("Select Channel").appendTo(
								$('#channelselect'));
						$.each(data, function() {
							$("<option/>").val(this.supportedChannelId).html(
									this.channelName).appendTo(
									$('#channelselect'));
						});
					}
				});

		tableimportchannel = $.CM.bindChannels('#tablechannels');
		$('#tablesuppliers')
				.DataTable(
						{
							"sZeroRecords" : 'Note: Once you add a Supplier, you can edit this field.',
							"bPaginate" : false,
							"bSort" : false,
							"bLengthChange" : false,
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
										$.each(formArray, function(i, v) {
											formObject[v.name] = v.value;
										});
										$(this)
												.CRUD(
														{
															method : "PUT",
															url : $(form).attr(
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
								var warn = false;
								$(form).find('input[name=updateorders]').each(
										function(i, el) {
											if (el.value == 0
													&& $(el).is(':checked')) {
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
													url : 'aggregators/channels/'
															+ channel.channelId,
													success : function(data,
															textStatus, jqXHR) {
														tableimportchannel.row(
																e[0]).remove()
																.draw();
														$.gritter
																.add({
																	title : "Delete Channel",
																	text : "Channel deleted."
																});
													},
													error : function(data,
															textStatus, jqXHR) {
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
			var channel = JSON.parse(JSON.stringify(tableimportchannel
					.row(e[0]).data()));
			$('#channelForm').attr('action',
					'aggregators/channels/' + channel.channelId);
			GenericBinder('channel', channel);
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
							url : "aggregators/suppliers",
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
		$('#btnSaveOrder').off('click');
		$('#btnSaveOrder').on('click', channel, function(e) {
			var order = {};
			$("#singleOrderForm :input").each(function() {
				if (this.name) {
					order[this.name] = this.value;
				}
			});
			$(this).CRUD({
				method : 'PUT',
				url : '/aggregators/channels/' + e.data.channelId + '/orders',
				data : JSON.stringify(order)
			});
		});
	}

	function toggle(chk, target) {
		if ($(chk).is(':checked')) {
			$('#' + target).show();
		} else {
			$('#' + target).hide();
		}
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
							url : "/aggregators/channels/" + channel.channelId
									+ "/filetypes",
							success : function(data) {
								$('#filetype').empty();
								$('#filetype')
										.append(
												'<option value="-1">Set up a new file type</option>');
								$(data).each(
										function(i, e) {
											$('#filetype').append(
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
																	function(e) {
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
														$('#submitOrderFile')
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
	function formatSalesData(data, salesData) {
		salesData.push([ "Date", "Total Sales($)" ]);
		for (var i = 0; i < data.overAllSales.length; i++) {
			salesData.push(data.overAllSales[i]);
		}
	}
	function copyFields(from_prefix, to_prefix) {
		$('#' + to_prefix + 'NAME').val(('#' + from_prefix + 'NAME').val());
		$('#' + to_prefix + 'STREET_ADDRESS').val(
				('#' + from_prefix + 'STREET_ADDRESS').val());
		$('#' + to_prefix + 'SUBURB').val(('#' + from_prefix + 'SUBURB').val());
		$('#' + to_prefix + 'CITY').val(('#' + from_prefix + 'CITY').val());
		$('#' + to_prefix + 'STATE').val(('#' + from_prefix + 'STATE').val());
		$('#' + to_prefix + 'COUNTRY').val(
				('#' + from_prefix + 'COUNTRY').val());
		$('#' + to_prefix + 'ZIP').val(('#' + from_prefix + 'ZIP').val());
		$('#' + to_prefix + 'COMPANY').val(
				('#' + from_prefix + 'COMPANY').val());
		$('#' + to_prefix + 'COMPANY').val(
				('#' + from_prefix + 'COMPANY').val());
		$('#' + to_prefix + 'PHONE').val(('#' + from_prefix + 'PHONE').val());
		$('#' + to_prefix + 'EMAIL').val(('#' + from_prefix + 'EMAIL').val());
	}
</script>
</jsp:attribute>
</t:basepage>
