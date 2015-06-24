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
           <%-- <div class="row">
         <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Tracking Summary" name="chartTitle" /> 
              <jsp:param value="product-sales-piechart" name="targetId" />
              <jsp:param
									value="aggregators/reports/system/order-tracking"
									name="reportUrl" />
			<jsp:param value="formatTrackingData" name="dataFormatter" />		
             </jsp:include>
           </div> --%>
            <div class="space-2"></div>
            <div class="row">
         </div>
         </div>
      </div> 
    </div>
  </div>
  
</jsp:attribute>
	<jsp:attribute name="pagejs">
	 <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
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
						debugger;
						for (var i = 0; i < data.channel_import.length; i++) {
							channelSalesData.push([ data.channelsales[i].name,
									data.channelsales[i].totalSales ]);
						}
					}

					function formatTrackingData(data, trackingData) {

						for (var i = 0; i < data.productsales.length; i++) {
							trackingData.push([ data.productsales[i].sku,
									data.productsales[i].totalSales ]);
						}
					}
				</script>
	</jsp:attribute>
</t:basepage>