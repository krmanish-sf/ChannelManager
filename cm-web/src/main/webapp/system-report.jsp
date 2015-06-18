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
           <div class="row">
         <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Tracking Summary" name="chartTitle" /> 
              <jsp:param value="product-sales-piechart" name="targetId" />
              <jsp:param
									value="aggregators/reports/system/order-tracking"
									name="reportUrl" />
			<jsp:param value="formatProductData" name="dataFormatter" />		
             </jsp:include>
           </div>
            <div class="space-2"></div>
            <div class="row">
         </div>
         </div>
      </div> 
    </div>
  </div>
  
</jsp:attribute>
	<jsp:attribute name="pagejs">
	<script type="text/javascript">
		function formatImportData(data, salesData) {
			var automation_series = {
				label : "Auto pull",
				data : [],
				color : "#ccc"
			};
			var manual_series = {
				label : "Manual pull",
				data : [],
				color : "#6fb3e0"
			}
			salesData.push(automation_series);
			salesData.push(manual_series);
			for (var i = 0; i < data.channel_import.length; i++) {
				if (data.channel_import[i][3] == 1)
					automation_series.data.push([ data.channel_import[i][2],
							data.channel_import[i][0] ]);
				else
					manual_series.data.push([ data.channel_import[i][2],
							data.channel_import[i][0] ]);
			}
		}
		function formatOrderStatusData(data, supplierSalesData) {
			var unprocessed = {
				label : "Unprocessed",
				data : [],
				bars : {
					show : true,
					order : 1,
					barWidth : 0.2
				}
			};
			var processed = {
				label : "Processed",
				data : [],
				bars : {
					show : true,
					order : 2,
					barWidth : 0.2
				}
			};
			var failed = {
				label : "Failed",
				data : [],
				bars : {
					show : true,
					order : 3,
					barWidth : 0.2
				}
			};
			var manuallyProcessed = {
				label : "Manually Processed",
				data : [],
				bars : {
					show : true,
					order : 4,
					barWidth : 0.2
				}
			};
			var canceled = {
				label : "Canceled",
				data : [],
				bars : {
					show : true,
					order : 5,
					barWidth : 0.2
				}
			};
			var shipped = {
				label : "Shipped",
				data : [],
				bars : {
					show : true,
					order : 6,
					barWidth : 0.2
				}
			};
			supplierSalesData.push(unprocessed);
			supplierSalesData.push(processed);
			supplierSalesData.push(failed);
			supplierSalesData.push(manuallyProcessed);
			supplierSalesData.push(canceled);
			supplierSalesData.push(shipped);

			for (var i = 0; i < data.supplier_processing.length; i++) {
				var s;
				switch (data.supplier_processing[i][3]) {
				case 0://Unprocessed
					s = unprocessed;
					break;
				case 2://Processed
					s = processed;
					break;
				case 3://Failed
					s = failed;
					break;
				case 4://
					break;
				case 5://Manually Processed
					s = manuallyProcessed;
					break;
				case 6://Canceled
					s = canceled;
					break;
				case 7://Shipped
					s = shipped;
					break;
				}
				s.data.push([ data.supplier_processing[i][1],
						data.supplier_processing[i][0] ]);
			}
		}

		function formatChannelData(data, channelSalesData) {
			debugger;
			for (var i = 0; i < data.channel_import.length; i++) {
				channelSalesData.push([ data.channelsales[i].name,
						data.channelsales[i].totalSales ]);
			}
		}

		function formatProductData(data, prodSalesData) {
			for (var i = 0; i < data.productsales.length; i++) {
				prodSalesData.push([ data.productsales[i].sku,
						data.productsales[i].totalSales ]);
			}
		}
	</script>
	</jsp:attribute>
</t:basepage>