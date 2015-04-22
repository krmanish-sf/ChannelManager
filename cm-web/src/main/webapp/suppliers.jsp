<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="salesmachine.oim.api.OimConstants"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">

  <div class="main-content">
      <div class="breadcrumbs" id="breadcrumbs"> 
   
        <ul class="breadcrumb">
          <li><a href="index.jsp"><i
							class="icon-home home-icon"></i>Home</a></li>
          <li class="active">Supplier</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1> Supplier <small> <i
							class="icon-double-angle-right"></i> overview &amp; stats </small> </h1>
        </div>
        <div class="row">
        <div class="col-xs-12">
              <div class="row">
              <div class="col-sm-12 inline">
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
												<p>To process orders with a supplier, you need to configure your supplier by providing your specific account details with the supplier.
If you have not yet configured your supplier, click "Add Supplier" to do it now. If your supplier is not in the list, write to us at <a
														href="mailto:support@inventorysource.com">support@inventorysource.com</a> and our support team will help you out with setting up your supplier.</p>
											</div>
										</div>
									</div>
								</div> 
                      <h4 class="lighter pull-left">
									<i class="icon-truck orange"></i> My Supplier </h4>
									
              <a data-toggle="modal" href="#mySupplieradd"
									id="addSupplier" class="btn btn-success pull-right">Add Supplier</a>
              </div>
						</div>
            <div class="space-2"></div>
            <div class="row">
              <div class="widget-box transparent">
            <div class="widget-main no-padding ">
              <table id="tableSupplier"
										class="table table-bordered table-striped table-responsive">
                <thead class="thin-border-bottom">
                  <tr>
                    <th><i
													class="icon-user icon-2x blue visible-xs"></i>
													<span class="hidden-xs">Supplier Name</span></th>
                    <th><i
													class="icon-barcode icon-2x blue visible-xs"></i><span
													class="hidden-xs visible-sm">Account</span></th>
                    <%-- <th><i
													class="icon-plane icon-2x blue visible-xs"></i><span
													class="hidden-xs">Shipping Method</span></th> --%>
                    <th><i class="icon-off icon-2x blue visible-xs"></i><span
													class="hidden-xs">Test Mode</span></th>
                    <th class="hidden-xs">Order Push Details</th>
                    <th class="hidden-xs">Custom Supplier</th>
                    <th class="">Edit</th>
                    <th class=""></th>
                  </tr>
                </thead>
                <tbody>
                </tbody>
              </table>
              <!-- /widget-main --> 
            </div>
        
       </div>
         </div>
         </div>
        
        <div class="modal fade" id="mySupplieredit" tabindex="-1"
						role="dialog" aria-hidden="true">
              <div id="mysuppliereditdailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Edit</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                        <form class="form-horizontal" role="form"
												action="/aggregators/suppliers" method="PUT"
												id="supplierform">
                           <div class="form-group">
                            <div class="col-sm-5">
                              <label
															class="col-sm-offset-8 control-label no-padding-right">Username</label>
													</div>
                              <div class="col-sm-7">
                              <input class="width-70" name='login'
															type='text' data-bind-vendorsupplier="login"
															required="required" />
                            </div>
												</div>
                            <div class="form-group">
                            <div class="col-sm-5">
                              <label
															class="col-sm-offset-8 control-label no-padding-right">Password</label>
													</div>
                              <div class="col-sm-7">
                              <input class="width-70" name='password'
															type='password' data-bind-vendorsupplier="password"
															required="required" />
                            </div>
												</div>
                            <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Account</label>
                              <div class="col-sm-7">
                              <input class="width-70" name='accountno'
															type='text' data-bind-vendorsupplier="accountNumber"
															required="required" />
                            </div>
												</div>
                            <div class="form-group"
													style="display: none;">
                              <label
														class="col-sm-5 control-label no-padding-right">Default Shipping Method Code</label>
                              <div class="col-sm-5">
                              <input class="width-100"
															name='defshippingmc' type='text'
															data-bind-vendorsupplier="defShippingMethodCode"
															required="required" />
                            </div>
												</div>
                            <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Test Mode</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="testmode"
															data-bind-vendorsupplier="testMode">
                                <option value="1" selected>Enabled</option>
                                <option value="0">Disabled</option>
                              </select>
                            </div>
												</div>
                            <div class="form-group supplieremailDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Order Recipient Email</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															required="required" name='supplieremail' type='email'
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodNames.methodNameId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=1].attributeValue" />
                            </div>
												</div>
                            <div class="form-group"
													id="customSupplierFileFormatDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Order File Format</label>
                              <div class="col-sm-7">
                              <select class="width-70"
															name="customSupplierFileFormat"
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodNames.methodNameId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=8].attributeValue">
                                <option value="1">CSV</option>
                                <option value="2">XML</option>
                                <option value="3">Plain Text</option>
                              </select>                  
                            </div>
												</div>
                            <div class="form-group" id="orderaction">
                              <label
														class="col-sm-5 control-label no-padding-right">Mark Earlier Orders</label>
                              <div class="col-sm-7">
                              <select class="width-70"
															name="updatewithstatus">
                                <option value="">No Action</option>
                                <option value="0">Unprocessed</option>
                                <option value="2">Processed</option>
                                <option value="3">Failed</option>
                                <option value="5">Manual</option>
                                <option value="6">Canceled</option>
                              </select>
                            </div>
												</div>
                            <div class="form-group center">
                             <button class="btn btn-info btn-sm"
														id="update" type="button"> <i class="icon-ok "></i>Update</button>
                            </div>
                         
                         
                        </form>
                        <!-- PAGE CONTENT BEGINS --> 
                        
                      </div>
                      
                    </div>
                  </div>
                </div>
                <!-- PAGE CONTENT ENDS --> 
              </div>
              <!-- /.col --> 
            </div>
	 <div class="modal fade" id="mySupplieradd" tabindex="-1" role="dialog"
						aria-hidden="true">
              <div id="mysupplieradddailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Add</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                        <form class="form-horizontal" role="form"
												action="/aggregators/suppliers" method="PUT"
												id="supplierAddForm">
                        <div class="form-group">
                            <div class="col-sm-5">
                              <label for="suppliername"
															class="col-sm-offset-8 control-label no-padding-right">Supplier</label>
													</div>
                              <div class="col-sm-7">
                              <select class="width-70" id="Supplieradd"
															name="suppliername" required>
                                <option value="0">Custom</option>
                              </select>
                            </div>
												</div>
												 <div class="form-group" id="customname">
                            <div class="col-sm-5">
                              <label for="name"
															class="col-sm-offset-8 control-label no-padding-right">Name</label>
													</div>
                              <div class="col-sm-7">
                                 <input class="width-70" name="name"
															minlength="2" type="text" value="" required />
                            </div>
												</div>
                           <div class="form-group">
                            <div class="col-sm-5">
                              <label
															class="col-sm-offset-8 control-label no-padding-right">UserName</label>
													</div>
                              <div class="col-sm-7">
                              <input class="width-70" name="login"
															type="text" value="" required />
                            </div>
												</div>
                            <div class="form-group">
                            <div class="col-sm-5">
                              <label
															class="col-sm-offset-8 control-label no-padding-right">Password</label>
													</div>
                              <div class="col-sm-7">
                              <input class="width-70" name="password"
															type="text" value="" required />
                            </div>
												</div>
                            <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Account</label>
                              <div class="col-sm-7">
                              <input class="width-70" name="accountno"
															type="text" value="" required />
                            </div>
												</div>
                            <div class="form-group"
													style="display: none;">
                              <label
														class="col-sm-5 control-label no-padding-right">Default Shipping Method Code</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="defshippingmc" type="text" value="" required />
                            </div>
												</div>
                            <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Test Mode</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="testmode">
                                <option value="1" selected>Enabled</option>
                                <option value="0">Disabled</option>
                              </select>
                            </div>
												</div>
                            <div class="form-group supplieremailDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Order Recipient Email</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="supplieremail" type="email" value="" required />
                            </div>
												</div>
                            <div class="form-group customfileformatDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Order File Format</label>
                              <div class="col-sm-7">
                              <select class="width-70"
															name="customSupplierFileFormat" required>
                                <option value="1" SELECTED>CSV</option>
                                <option value="2">XML</option>
                                <option value="3">Plain Text</option>
                              </select>
                            </div>
												</div>
                             <div class="form-group center">
                             <button class="btn btn-info btn-sm"
														id="savesupplier" type="button"> <i
															class="icon-ok "></i>Save</button>
                            </div>
                         
                         
                        </form>
                        <!-- PAGE CONTENT BEGINS --> 
                        
                      </div>
                      
                    </div>
                  </div>
                </div>
                <!-- PAGE CONTENT ENDS --> 
              </div>
              <!-- /.col --> 
            </div>
            
            <div class="modal fade" id="editshippingmethods"
						tabindex="-1" role="dialog" aria-hidden="true">
              <div id="mysupplieradddailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Shipping Methods</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                      <div class="col-sm-12">
							<table id="tableShippingMap"
													class="table table-bordered table-striped table-responsive">
								<thead class="thin-border-bottom">
									<tr>
										<th><i class="icon-barcode icon-2x blue visible-xs"></i><span
																class="hidden-xs visible-sm">Shipping Carrier</span></th>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
																class="hidden-xs">Shipping Method</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
																class="hidden-xs">Supplier Shipping Carrier</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
																class="hidden-xs">Supplier Shipping Method</span></th>
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
      </div> 
    </div>
  </div>
</jsp:attribute>
	<jsp:attribute name="pagejs">
<script type="text/javascript">
	var table_vendorSuppliers;
	function edit(e) {
		var vendorSupplier = table_vendorSuppliers.fnGetData(e[0]);
		var vendorSupplierTemp = JSON.parse(JSON.stringify(vendorSupplier));
		//var rowIndex = table_vendorSuppliers.fnGetPosition(e[0]);
		GenericBinder('vendorsupplier', vendorSupplierTemp);
		if (vendorSupplier.oimSuppliers.isCustom) {
			$('#customSupplierFileFormatDiv').show();
			$('.supplieremailDiv').show();
		} else {
			$('#customSupplierFileFormatDiv').hide();
			$('.supplieremailDiv').hide();
		}

		$('#saveshippingmapping').off('click').on(
				'click',
				vendorSupplier,
				function(e) {
					var data = {};
					$('#shippingmethodsForm :input').each(function() {
						if (this.name) {
							data[this.name] = this.value;
						}
					});
					$(this).CRUD(
							{
								url : "aggregators/suppliers/"
										+ e.data.oimSuppliers.supplierId
										+ '/shippingmapping',
								method : "PUT",
								data : JSON.stringify(data)
							});
				});
		$('#supplierform').validate();
		$('#supplierform').prop('action',
				'aggregators/suppliers/' + vendorSupplier.vendorSupplierId);
		$('#update').off("click").on("click", function(e) {
			$('#supplierform').submit();
		});
	}

	function del(e) {
		bootbox
				.confirm(
						'This will remove this supplier record.  Click <b>OK</b> to remove it fully.',
						function(b) {
							if (b) {
								var vendorSupplier = table_vendorSuppliers
										.fnGetData(e[0]);
								$(e[0])
										.CRUD(
												{
													method : "DELETE",
													url : 'aggregators/suppliers/subscriptions/'
															+ vendorSupplier.vendorSupplierId,
													data : JSON
															.stringify(vendorSupplier),
													message : {
														title : 'Delete Supplier',
														text : 'Supplier Deleted successfully.'
													},
													success : function(data,
															textStatus, jqXHR) {
														table_vendorSuppliers
																.fnDeleteRow(e[0]);
														table_vendorSuppliers
																.fnDraw();
														getAlerts();
													}
												});
							}
						});
	}

	jQuery(function($) {
		table_vendorSuppliers = $('#tableSupplier')
				.dataTable(
						{
							"bPaginate" : false,
							"bLengthChange" : false,
							"sAjaxSource" : 'aggregators/suppliers',
							"fnServerData" : function(sSource, aoData,
									fnCallback, oSettings) {
								oSettings.jqXHR = $(this).CRUD({
									type : "GET",
									url : sSource,
									data : aoData,
									cache : false,
									success : fnCallback
								});
							},
							"sAjaxDataProp" : '',
							"aoColumns" : [

									{
										"mData" : function(row) {

											return '<a class="btn btn-default icon-info-sign btn-xs visible-xs addresspop" data-toggle="popover" data-container="body"  data-placement="bottom" data-content="'+row.oimSuppliers.supplierName+'" data-original-title="Supplier Name"></a><div class="hidden-xs">'
													+ row.oimSuppliers.supplierName
													+ '</div>';
										}
									},
									{
										"mData" : "accountNumber"
									},
									/* {
										"mData" : function(vendorSupplier) {
											return '<a class="btn btn-info btn-sm hidden-xs icon-exchange" href="#editshippingmethods"'
													+ ' data-toggle="modal" onclick="$.CM.viewSupplierShippingMap($(this).parent().parent());"></a><a class="btn btn-info btn-xs icon-info visible-xs btn-xs"'
													+ ' href="#editshippingmethods" data-toggle="modal" onclick="$.CM.viewSupplierShippingMap($(this).parent().parent());"></a>';
										}
									}, */
									{
										"mData" : function(vendorSupplier) {
											return vendorSupplier.testMode ? "Enabled"
													: "Disabled";
										}
									},
									{
										"sClass" : "hidden-xs",
										"mData" : function(o) {
											var ret = '';
											for (var i = 0; i < o.oimSuppliers.oimSupplierMethodses.length; i++) {
												var oimSupplierMethod = o.oimSuppliers.oimSupplierMethodses[i];
												if (oimSupplierMethod.oimSupplierMethodNames) {
													ret += oimSupplierMethod.oimSupplierMethodTypes.methodTypeName;
													ret += " : ";
													ret += oimSupplierMethod.oimSupplierMethodNames.methodName;
													ret += "<br/>";
												}
												if (oimSupplierMethod.oimSupplierMethodattrValueses) {
													for (var j = 0; j < oimSupplierMethod.oimSupplierMethodattrValueses.length; j++) {
														var attrVal = oimSupplierMethod.oimSupplierMethodattrValueses[j];
														ret += attrVal.oimSupplierMethodattrNames.attrName;
														ret += " : ";
														if (attrVal.oimSupplierMethodattrNames.attrId == 8) {
															switch (attrVal.attributeValue) {
															case "1":
																ret += "CSV";
																break;
															case "2":
																ret += "XML";
																break;
															case "3":
																ret += "Plain Text";
																break;
															}
														} else {
															ret += attrVal.attributeValue;
														}

														ret += "<br/>";
													}
												}
											}
											return ret;
										}
									},
									{
										"sClass" : "hidden-xs",
										"mData" : function(vendorSupplier) {
											if (vendorSupplier.oimSuppliers.isCustom)
												return "Yes";
											return "No";
										}
									},
									{
										"bSortable" : false,
										"mData" : function(vendorSupplier) {
											return '<a class="btn btn-info btn-sm hidden-xs icon-pencil" href="#mySupplieredit" data-toggle="modal" onclick="edit((($(this)).parent()).parent())"></a><a class="btn btn-info btn-xs icon-pencil visible-xs btn-xs" href="#mySupplieredit" data-toggle="modal" onclick="edit((($(this)).parent()).parent())"></a>';
										}
									},
									{
										"bSortable" : false,
										"mData" : function(vendorSupplier) {
											return '<a class="btn btn-danger btn-sm hidden-xs" onclick="del((($(this)).parent()).parent())"><i class="icon-trash"></i>Delete</a><a class="btn btn-danger visible-xs btn-xs icon-trash" onclick="delete((($(this)).parent()).parent())"></a>';
										}
									} ]
						});

		$('#Supplieradd').on('change', function() {
			var a = $(this).val();
			if (a == '0') {
				$('#customname').show();
				$('.customfileformatDiv').show();
				$('.supplieremailDiv').show();
			} else {
				$('#customname').hide();
				$('.customfileformatDiv').hide();
				$('.supplieremailDiv').hide();
			}
		});

		$('#addSupplier').on(
				'click',
				function() {
					$('#supplierAddForm :input').each(function() {
						this.value = '';
					});
					$('#Supplieradd').empty().append(
							"<option>Please wait...</option>");
					$(this).CRUD(
							{
								url : "aggregators/suppliers/unsubscribed",
								method : "GET",
								success : function(data) {
									$('#Supplieradd').empty();
									$("<option/>").val("").html(
											"Select Supplier").appendTo(
											$('#Supplieradd'));
									$.each(data, function() {
										$("<option/>").val(this.supplierId)
												.html(this.supplierName)
												.appendTo($('#Supplieradd'));
									});
									$("<option/>").val(0).html("Custom")
											.appendTo($('#Supplieradd'));
								}
							});
				});
		$('#supplierform')
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
										title : "Supplier information",
										text : message
									});
								}
							},
							submitHandler : function(form) {
								var formArray = $(form).serializeArray();
								var formObject = {};
								$.each(formArray, function(i, v) {
									formObject[v.name] = v.value;
								});
								$(this)
										.CRUD(
												{
													url : $(form)
															.attr('action'),
													method : "PUT",
													data : JSON
															.stringify(formObject),
													success : function(a, b, c) {
														$('.modal').modal(
																'hide');

														$.gritter
																.add({
																	title : 'Update Supplier',
																	text : 'Supplier updated successfully.'
																});
														table_vendorSuppliers
																.fnReloadAjax();
													}
												});
							},
							highlight : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-info').addClass('has-error');
							},
							success : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-error').addClass('has-info');
								$(e).remove();
							}
						});
		$('#supplierAddForm')
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
										title : "Supplier information",
										text : message
									});
								}
							},
							submitHandler : function(form) {
								var formArray = $(form).serializeArray();
								var formObject = {};
								$.each(formArray, function(i, v) {
									formObject[v.name] = v.value;
								});
								$(this)
										.CRUD(
												{
													url : "aggregators/suppliers",
													method : "PUT",
													data : JSON
															.stringify(formObject),

													success : function() {
														$.gritter
																.add({
																	title : 'Add Supplier',
																	text : 'Supplier added successfully.'
																});
														$('#mySupplieradd')
																.modal('hide');
														table_vendorSuppliers
																.fnReloadAjax();

													}
												});
							},
							highlight : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-info').addClass('has-error');
							},
							success : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-error').addClass('has-info');
								$(e).remove();
							}
						});
		$('#savesupplier').off('click').on('click', function() {
			$('#supplierAddForm').submit();
		});

		$(document).on('click', ".deleteparent", function() {
			$(this).parent().parent().remove();
		});

		$("#addshipping")
				.on(
						'click',
						function() {
							var time = (new Date()).getTime();
							var html = '<div class="form-group">'
									+ '<div class="col-sm-5 no-padding-left"><input type="text" class="width-100" name="shipping_text_'
									+ time
									+ '" /></div>'
									+ '<div class="col-sm-6 no-padding-left"><input type="text" class="width-100" name="shipping_method_text_'+time+'" /></div><div class="col-sm-1 no-padding-left"><button class="btn btn-info btn-sm deleteparent" type="button">'
									+ '<i class="icon-trash"></i></button></div></div>';
							$(this).parent().parent().prepend($(html));
						});

	});
</script>
</jsp:attribute>
</t:basepage>
