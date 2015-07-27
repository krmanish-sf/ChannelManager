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
          <li class="active">Admin Report</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1>Admin Report<small> <i
							class="icon-double-angle-right"></i> overview </small> </h1>
        </div>
        <div class="row">
        <div class="col-xs-12">
           <div class="space-2"></div>
           <div class="row">
            <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Import Summary" name="chartTitle" />
              <jsp:param value="order-import" name="targetId" />
              <jsp:param
									value="aggregators/reports/system/order-summary"
									name="reportUrl" />
				<jsp:param value="formatOrderSummary" name="dataFormatter" />
				<jsp:param value="drawBarChart" name="fnChart" />
				
             </jsp:include>
         </div>
            <div class="row">
            <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Import by Channel"
									name="chartTitle" />
              <jsp:param value="import-charts" name="targetId" />
              <jsp:param value="aggregators/reports/system/order-import"
									name="reportUrl" />
				<jsp:param value="formatImportData" name="dataFormatter" />
				<jsp:param value="drawBarChart" name="fnChart" />
				
             </jsp:include>
         </div>
         <div class="row">
         <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Processing Summary"
									name="chartTitle" />
                <jsp:param value="Supplier Sales" name="chartTitle" />
              <jsp:param value="supplier-sales-chart" name="targetId" />
              <jsp:param
									value="aggregators/reports/system/order-processing"
									name="reportUrl" />
				<jsp:param value="formatOrderStatusData" name="dataFormatter" />
				<jsp:param value="drawBarChart" name="fnChart" />	
             </jsp:include>
			</div>
            <div class="space-2"></div>
           <div class="row">
         <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Status by Channel"
									name="chartTitle" /> 
              <jsp:param value="order-status-by-channel" name="targetId" />
              <jsp:param
									value="aggregators/reports/system/order-tracking"
									name="reportUrl" />
			<jsp:param value="formatChannelData" name="dataFormatter" />
			<jsp:param value="drawBarChart" name="fnChart" />		
             </jsp:include>
           </div> 
            <div class="space-2"></div>
           <%--  <div class="row">
            <div class="widget-main no-padding table-responsive">
																<table
									class="table table-bordered table-striped dataTable"
									id="tableimporthistory">
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
												<th class=" width-20"></th>
											</tr>
										</thead>
									</table>
							</div>
         </div>
         <div class="space-2"></div> --%>
            
           <div class="row">
           <div class="widget-box transparent">
	<div class="widget-header widget-header-flat">
		<h4 class="lighter">
			<i class="icon-signal"></i>&nbsp;Vendor Supplier Error History
		</h4>
		<div class="widget-toolbar">
			<select class="select2-drop select-time-range" id="chartselecttime">
				<option class="orange2 active" value="THIS_MONTH">This
					Month</option>
				<option class="orange2" value="LAST_SEVEN_DAYS">Last 7 Days</option>
				<option class="orange2" value="LAST_MONTH">Last Month</option>
				<option class="orange2" value="ALL_TIME">All Time</option>
				<option class="orange2" value="CUSTOM">Custom</option>
			</select>
		</div>
	</div>
	<div class="space-4"></div>
	<div class="container">
		<label class="col-sm-2 control-label no-padding-right">Date</label> <input
										type="text" class="width-25 datepicker start"
										placeholder="mm/dd/yyyy" /> <span>&nbsp;To&nbsp;</span> <input
										type="text" class="width-25 datepicker end"
										placeholder="mm/dd/yyyy" />
		<a href="javascript:;" onclick="getData(this);"
										class="btn btn-info btn-sm pull-right">Generate</a>
	</div>
</div>
<div class="space-2"></div>
<div class="widget-main padding-4">
	<table class="table table-bordered table-striped dataTable"
									id="tableorderhistory">
										<thead class="thin-border-bottom">
											<tr>
												<th><i class="icon-key icon-2x visible-xs "></i><span
												class="hidden-xs">Vendor</span></th>
												<th><i class="icon-key icon-2x visible-xs "></i><span
												class="hidden-xs">Supplier</span></th>
												<th><i class="icon-globe icon-2x visible-xs "></i><span
												class="hidden-xs">Order Processing Time</span></th>
												<th><i class="icon-flag icon-2x visible-xs "></i><span
												class="hidden-xs">Error Type</span></th>
												<th><i class="icon-adjust icon-2x visible-xs "></i><span
												class="hidden-xs">Description</span></th>
											</tr>
										</thead>
									</table>

</div>
           
           </div>
           <div class="space-2"></div>
         </div>
         
         
         
      </div> 
    </div>
  </div>
  
</jsp:attribute>
	<jsp:attribute name="pagejs">
	 <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
					function getData(e) {
						var widget = $(e).parent().parent();
						$('#tableorderhistory')
								.DataTable(
										{
											"bDestroy" : true,
											"bLengthChange" : true,
											"bPaginate" : true,
											"bInfo" : true,
											"bFilter" : true,
											"bJQueryUI" : true,
											"sAjaxSource" : 'aggregators/reports/system/vendor-supplier-history',
											"fnServerData" : function(sSource,
													aoData, fnCallback,
													oSettings) {
												oSettings.jqXHR = $(this)
														.CRUD(
																{
																	method : "POST",
																	url : sSource,
																	data : JSON
																			.stringify({
																				startDate : widget
																						.find(
																								'input.start')
																						.val(),
																				endDate : widget
																						.find(
																								'input.end')
																						.val()
																			}),
																	success : fnCallback
																});
											},
											"sAjaxDataProp" : '',
											"aoColumns" : [
													{
														"mData" : "vendors.vendorId"
													},
													{
														"mData" : "oimSuppliers.supplierName"
													},
													{
														"mData" : function(vsho) {
															return new Date(
																	vsho.processingTm);
														}
													},
													{
														"mData" : function(vsho) {
															var errorMessage = '';
															switch (vsho.errorCode) {
															case 1:
																errorMessage = "Supplier Configuration Error";
																break;
															case 2:
																errorMessage = "Supplier Communication Error";
																break;
															case 3:
																errorMessage = "Order Processing Error";
																break;
															}
															return errorMessage;
														}
													}, {
														"bSortable" : false,
														"mData" : "description"
													} /* ,
																																																																																																																																																														{
																																																																																																																																																															"bSortable" : false,
																																																																																																																																																															"mData" : function(row) {
																																																																																																																																																																return '<a class="btn btn-info btn-minier radius-2 dropdown-hover" data-toggle="modal" href="#mychanneledit" onclick="channel(\'edit\',$(this).parent().parent())"><i class="icon-pencil"></i><span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">Edit Channel Setting</span></a><a class="btn btn-danger btn-minier radius-2 dropdown-hover" onclick="del($($($(this).parent()).parent()))"><i class="icon-trash"></i><span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">Delete Channel</span></a>';
																																																																																																																																																															}
																																																																																																																																																														} */]
										});
					}
					// Load the Visualization API and the piechart package.
					google.load('visualization', '1.0', {
						'packages' : [ 'bar' ]
					});

					// Set a callback to run when the Google Visualization API is loaded.
					//google.setOnLoadCallback(drawChart);

					function formatImportData(data, salesData) {
						salesData.push([ 'Channel Type', 'Automated Pull',
								"Manual Pull" ]);
						for (var i = 0; i < data.order_import.length; i++) {
							salesData.push(data.order_import[i]);
						}
						console.log(salesData);
					}
					function formatOrderStatusData(data, supplierSalesData) {
						supplierSalesData.push([ "Supplier Name",
								'Unprocessed', 'Processed', 'Failed',
								'Manually Processed', 'Canceled', 'Shipped' ]);
						for (var i = 0; i < data.order_processing.length; i++) {
							supplierSalesData.push(data.order_processing[i]);
						}
						console.log(supplierSalesData);
					}

					function formatChannelData(data, channelSalesData) {
						channelSalesData.push([ "Channel Name", 'Unprocessed',
								'Processed', 'Failed', 'Manually Processed',
								'Canceled', 'Shipped' ]);
						for (var i = 0; i < data.order_tracking.length; i++) {
							channelSalesData.push(data.order_tracking[i]);
						}
					}

					function formatOrderSummary(data, importSummary) {
						importSummary.push([ "Date", "Orders Imported" ]);
						for (var i = 0; i < data.order_summary.length; i++) {
							importSummary.push(data.order_summary[i]);
						}
					}
				</script>
	</jsp:attribute>
</t:basepage>