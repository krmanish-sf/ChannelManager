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
            <!-- <div class="row">
              <div class="col-sm-12 inline">
                      <h4 class="lighter pull-left">
									<i class="icon-exchange orange"></i>Order Pull Report</h4>
									<div class="btn-group btn-overlap pull-right">
									<a class="btn btn-primary  btn-bold" title="" tabindex="0"
										data-original-title="Table view"><span><i
											class="icon icon-list-alt"></i></span></a>
											<a class="btn btn-primary  btn-bold" title="" tabindex="0"
										data-original-title="Graph view"><span><i
											class="icon icon-bar-chart"></i></span></a>
								</div>
							</div>
			</div> -->
            <div class="space-2"></div>
            <div class="row">
            <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Import Summary" name="chartTitle" />
              <jsp:param value="import-charts" name="targetId" />
              <jsp:param value="aggregators/reports/totalsales"
									name="reportUrl" />
				<jsp:param value="formatSalesData" name="dataFormatter" />
             </jsp:include>
              <%-- <div class="widget-box transparent">
            <div class="widget-main no-padding ">
              <table id="tableShippingMappings"
										class="table table-bordered table-striped table-responsive">
                <thead class="thin-border-bottom">
                  <tr>
                    <th>Batch Time</th>
					<th>Order Count</th>
					<th>Description</th>
                  </tr>
                </thead>
                <tbody>
                </tbody>
              </table>
            </div>
       </div> --%>
         </div>
         
         <div class="row">
         <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Processing Summary"
									name="chartTitle" />
                <jsp:param value="Supplier Sales" name="chartTitle" />
              <jsp:param value="supplier-sales-chart" name="targetId" />
              <jsp:param value="aggregators/reports/supplier"
									name="reportUrl" />
				<jsp:param value="formatSupplierData" name="dataFormatter" />	
             </jsp:include>
              <!-- <div class="col-sm-12 inline">
                      <h4 class="lighter pull-left">
									<i class="icon-exchange orange"></i>Order Processing Report</h4>
									<div class="btn-group btn-overlap pull-right">
									<a class="btn btn-primary  btn-bold" title="" tabindex="0"
										data-original-title="Table view"><span><i
											class="icon icon-list-alt"></i></span></a>
											<a class="btn btn-primary  btn-bold" title="" tabindex="0"
										data-original-title="Graph view"><span><i
											class="icon icon-bar-chart"></i></span></a>
								</div>
							</div> -->
			</div>
            <div class="space-2"></div>
            <div class="row">
              <%-- <div class="widget-box transparent">
            <div class="widget-main no-padding ">
              <table id="tableOrderSent"
										class="table table-bordered table-striped table-responsive">
                <thead class="thin-border-bottom">
                  <tr>
                    <th>Supplier Name</th>
                    <th>Status Details</th>
					<th>Order Count</th>
					<th>Description</th>
                  </tr>
                </thead>
                <tbody>
                </tbody>
              </table>
            </div>
       </div> --%>
         </div>
         
           <div class="row">
         <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Order Tracking Summary" name="chartTitle" /> 
              <jsp:param value="product-sales-piechart" name="targetId" />
              <jsp:param value="aggregators/reports/product"
									name="reportUrl" />
			<jsp:param value="formatProductData" name="dataFormatter" />		
             </jsp:include>
              <!-- <div class="col-sm-12 inline">
                      <h4 class="lighter pull-left">
									<i class="icon-exchange orange"></i>Order Tracking Report</h4>
									<div class="btn-group btn-overlap pull-right">
									<a class="btn btn-primary  btn-bold" title="" tabindex="0"
										data-original-title="Table view"><span><i
											class="icon icon-list-alt"></i></span></a>
											<a class="btn btn-primary  btn-bold" title="" tabindex="0"
										data-original-title="Graph view"><span><i
											class="icon icon-bar-chart"></i></span></a>
								</div>
							</div> -->
			</div>
            <div class="space-2"></div>
            <div class="row">
              <%-- <div class="widget-box transparent">
            <div class="widget-main no-padding ">
              <table id="tableOrderTracking"
										class="table table-bordered table-striped table-responsive">
                <thead class="thin-border-bottom">
                  <tr>
                    <th>Supplier Name</th>
                    <th>Status Details</th>
					<th>Order Count</th>
					<th>Description</th>
                  </tr>
                </thead>
                <tbody>
                </tbody>
              </table>
            </div>
       </div> --%>
         </div>
         
         </div>
      </div> 
    </div>
  </div>
  
</jsp:attribute>
	<jsp:attribute name="pagejs">
	<script type="text/javascript">
		function formatSalesData(data, salesData) {
			for (var i = 0; i < data.overAllSales.length; i++) {
				var date = new Date(data.overAllSales[i].date);
				console.log(date.getMonth());
				salesData.push([ date.getDate() + "/" + date.getMonth(),
						data.overAllSales[i].totalSales ]);

			}
			console.log(salesData);
		}
		function formatSupplierData(data, supplierSalesData) {
			for (var i = 0; i < data.suppliersales.length; i++) {
				supplierSalesData.push([ data.suppliersales[i].name,
						data.suppliersales[i].totalSales ]);
			}
		}

		function formatChannelData(data, channelSalesData) {
			for (var i = 0; i < data.channelsales.length; i++) {
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