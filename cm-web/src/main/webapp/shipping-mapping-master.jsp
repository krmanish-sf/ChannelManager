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
          <li class="active">Shipping Mapping Master Data</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1>Shipping Mapping Master Data<small> <i
							class="icon-double-angle-right"></i> overview </small> </h1>
        </div>
        <div class="row">
        <div class="col-xs-12">
              <div class="row">
              <div class="col-sm-12 inline">
                      <h4 class="lighter pull-left">
									<i class="icon-exchange orange"></i> Shipping Mapping Master Data</h4>					
              <!-- <a data-toggle="modal" href="#addCarrierModal"
									id="addCarrier" class="btn btn-success pull-right">Add Carrier</a> -->
              
              </div>
			</div>
            <div class="space-2"></div>
            <div class="row">
              <div class="widget-box transparent">
            <div class="widget-main no-padding ">
              <table id="tableShippingMappings"
										class="table table-bordered table-striped table-responsive">
                <thead class="thin-border-bottom">
                  <tr>
                    <th>Carrier Name</th>
					<th>Method Name</th>
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
      </div> 
    </div>
  </div>
  <div class="modal fade" id="addCarrierModal" tabindex="-1"
			role="dialog" aria-hidden="true">
              <div id="mysupplieradddailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
							data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">In-coming Channel Shipping Methods</h4>
                  </div>
                <div class="modal-body ">
                	<div class="row">
                      <div class="col-sm-12">
						<div class="form-group">
							<!-- <label class="col-sm-5 control-label no-padding-right">Select Channel Type</label> -->
							<div class="col-sm-7">
								<select name="supportedChannelId" required="required"
											id="supportedChannelId" class="width-70 pull-left">
									<option value="" selected>Select channel</option>
									<option value="4">Amazon Seller Central</option>
									<!-- <option value="1">CRE Loaded</option> -->
									<!-- <option value="3">Inventory Source - Hosted</option> -->
									<!-- <option value="2">Magento</option> -->
									<option value="7">Shop.com</option>
									<!-- <option value="5">Yahoo Store</option> -->
									<option value="6">Zen Cart</option>
									<option value="0">Custom Channel</option>
								</select>
							</div>
						</div>
                    	</div>
					</div>
					<div class="row">
					<div class="col-sm-12">
						<div class="form-group">
							<div class="col-sm-7">
								<input type="text" class="width-70" required="required"
											name="regex" placeholder="In-coming shipping/RegEx">
							</div>
							<a href="javascript:;" id="addShippingRegex"
										class="btn btn-info pull-right">Add In-coming method</a>
						</div>
                    	</div>
					</div>
                    <div class="row">
                      <div class="container">
                      <div class="col-sm-12">
							<table id="tableChannelRegEx"
										class="table table-bordered table-striped table-responsive">
								<thead class="thin-border-bottom">
									<tr>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
													class="hidden-xs">Shipping RegEx</span></th>
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
</jsp:attribute>
	<jsp:attribute name="pagejs">
	<script type="text/javascript">
		var tableShippingMappings, dtChannelRegEx, shippingMethodId;

		function setVal(i) {
			$('#supportedChannelId').val("");
			$('#supportedChannelId').trigger('change');
			shippingMethodId = i;
			return false;
		}
		jQuery(function($) {
			dtChannelRegEx = $('#tableChannelRegEx').DataTable({
				"bPaginate" : false,
				"bLengthChange" : false,
				"sDom" : "t",
				"sAjaxDataProp" : '',
				"aoColumns" : [ {
					"mData" : "shippingRegEx"
				} ]
			});
			$('#supportedChannelId').on(
					'change',
					function() {
						if (!this.value) {
							dtChannelRegEx.clear().draw();
						} else {
							var supportedChannelId = this.value;
							dtChannelRegEx.ajax.url(
									'/cm-rest/aggregators/shipping/'
											+ shippingMethodId + '/'
											+ supportedChannelId).load();
						}
					});

			tableShippingMappings = $('#tableShippingMappings')
					.DataTable(
							{
								"bPaginate" : true,
								"bLengthChange" : true,
								"sAjaxSource" : 'aggregators/shipping/methods',
								"fnServerData" : function(sSource, aoData,
										fnCallback, oSettings) {
									oSettings.jqXHR = $(this).CRUD({
										type : "GET",
										url : sSource,
										data : aoData,
										success : function(data) {
											fnCallback(data);
										}
									});
								},
								"sAjaxDataProp" : '',
								"aoColumns" : [
										{

											"mData" : "shippingCarrier.name"
										},
										{
											"mData" : "name"
										},
										{
											"mData" : function(d) {
												return '<a data-toggle="modal" href="#addCarrierModal" onclick=setVal('
														+ d.id
														+ ') class="btn btn-info pull-right">Channel Mappings</a>';
											}
										} ]
							});
		});
	</script>
	</jsp:attribute>
</t:basepage>