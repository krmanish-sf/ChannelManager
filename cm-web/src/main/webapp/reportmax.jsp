<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
<style>
div.flot-x-axis>div.flot-tick-label {
	transform: rotate(-90deg);
	-ms-transform: rotate(-90deg); /* IE 9 */
	-moz-transform: rotate(-90deg); /* Firefox */
	-webkit-transform: rotate(-90deg); /* Safari and Chrome */
	-o-transform: rotate(-90deg); /* Opera */
}
</style>
	<div class="main-content">
      <div class="breadcrumbs" id="breadcrumbs">
        <ul class="breadcrumb">
          <li><a href="index.jsp"><i
							class="icon-home home-icon"></i>Home</a></li>
          <li class="active">Reports</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1> Reports <small> <i
							class="icon-double-angle-right"></i> overview &amp; stats </small> </h1>
        </div>
        <!-- /.page-header -->
        
        <div class="row">
          <div class="col-xs-12"> 
            <!-- PAGE CONTENT BEGINS -->
            <div class="row">
              <div class="col-sm-7 well repbkcolor_purple">
           <jsp:include page="include/leaderboard.jsp"></jsp:include>
			</div>
              <div class="col-sm-5">
                <div class="widget-container-span">
                  <div class="widget-box">
                    <div class="widget-header header-color-green">
                      <h5>Tip:</h5>
                      <div class="widget-toolbar"> <a href="#"
													data-action="close"> <i class="icon-remove white"></i> </a> </div>
                    </div>
                    <div class="widget-body alert-success">
                      <div class="widget-main">
                        <p>You can view different visual reports for the current month data by default. You can change any report date range to view it across time of your choice.</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="hr hr-10"></div>
            <div class="row">
            <div class="col-sm-6">
             <jsp:include page="include/reportchart.jsp">
             <jsp:param value="Sales Summary" name="chartTitle" />
              <jsp:param value="sales-charts" name="targetId" />
              <jsp:param value="aggregators/reports/totalsales"
										name="reportUrl" />
				<jsp:param value="formatSalesData" name="dataFormatter" />
             </jsp:include>
             </div>
             <!-- /widget-box -->
             <div class="col-sm-6">
             <jsp:include page="include/piechart.jsp">
              <jsp:param value="Product Sales" name="chartTitle" />
              <jsp:param value="product-sales-piechart" name="targetId" />
              <jsp:param value="aggregators/reports/product"
										name="reportUrl" />
			<jsp:param value="formatProductData" name="dataFormatter" />	
              </jsp:include>
             </div>
            </div>
            <div class="hr hr-10 hr-dotted"></div>
            <div class="row">
              <div class="col-sm-6">
              <jsp:include page="include/piechart.jsp">
              <jsp:param value="Supplier Sales" name="chartTitle" />
              <jsp:param value="supplier-sales-piechart" name="targetId" />
              <jsp:param value="aggregators/reports/supplier"
										name="reportUrl" />
				<jsp:param value="formatSupplierData" name="dataFormatter" />	
              </jsp:include>
              </div>
              <div class="col-sm-6">
              <jsp:include page="include/piechart.jsp">
              <jsp:param value="Channel Sales" name="chartTitle" />
              <jsp:param value="channel-sales-piechart" name="targetId" />
              <jsp:param value="aggregators/reports/channel"
										name="reportUrl" />
			<jsp:param value="formatChannelData" name="dataFormatter" />										
              </jsp:include>
              </div>
            </div>
          </div> 
        </div> 
      </div>      
    </div> 
	</jsp:attribute>
	<jsp:attribute name="pagejs">
	
<!-- inline scripts related to this page --> 

<script type="text/javascript">
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
		$(this)
				.CRUD(
						{
							method : "POST",
							url : 'aggregators/reports',
							data : JSON
									.stringify({
										startDate : new Date().getMonthRange().startDate
												.getTime(),
										endDate : new Date().getMonthRange().endDate
												.getTime()
									}),
							success : function(data, textStatus, jqXHR) {
								$("#tasks").empty();
								var productsales = data.productsales;
								var suppliersales = data.suppliersales;
								var channelsales = data.channelsales;
								var chartData = [];
								var pieChartData = [];
								for (var i = 0; i < data.overAllSales.length; i++) {
									var date = new Date(
											data.overAllSales[i].date);
									chartData.push([ date.getTime(),
											data.overAllSales[i].totalSales ]);
								}
								//chart1load(chartData, null);
								$(this).drawBarChart('sales-charts', chartData);
								for (var i = 0; i < channelsales.length; i++) {
									var row = '<li class="item-orange clearfix"><label class="inline"> <span>'
											+ channelsales[i].name
											+ '</span> </label><div class="pull-right">$'
											+ channelsales[i].totalSales
											+ ' </div></li>';
									$(row).appendTo("#tasks");
									pieChartData.push({
										label : channelsales[i].name,
										data : channelsales[i].totalSales
									});
								}
								$(this).drawPieChart('channel-sales-piechart',
										pieChartData);
								var supplierSalesData = [];
								for (var i = 0; i < suppliersales.length; i++) {
									var row = '<li class="item-orange clearfix"><label class="inline"> <span>'
											+ suppliersales[i].name
											+ '</span> </label><div class="pull-right">$'
											+ suppliersales[i].totalSales
											+ ' </div></li>';
									$(row).appendTo("#tasks1");
									supplierSalesData.push({
										label : suppliersales[i].name,
										data : suppliersales[i].totalSales
									});
								}
								var prodSalesPieData = [];
								for (var i = 0; i < productsales.length; i++) {
									var row = '<li class="item-orange clearfix"><label class="inline"> <span>'
											+ productsales[i].sku
											+ '</span> </label><div class="pull-right">$'
											+ productsales[i].totalSales
											+ '</div></li>';
									$(row).appendTo("#tasks2");
									prodSalesPieData.push({
										label : productsales[i].sku,
										data : productsales[i].totalSales
									});
								}
								$(this).drawPieChart('product-sales-piechart',
										prodSalesPieData);
								$(this).drawPieChart('supplier-sales-piechart',
										supplierSalesData);
							},
							error : function(data, textStatus, jqXHR) {
								alert("Error in getting report data.");
							}
						});

		/* $('#generateReport')
					.on(
							'click',
							function() {
								var reportType = $('#reportType option:selected')
										.val();
								if (!reportType) {
									$.gritter.add({
										title : 'Generate Report',
										text : 'Please select report type',
										class_name : 'gritter-error'
									});
									$('#reportType').focus();
									return false;
								}
								window
										.open(
												CM_SETTINGS.rest_api_base_url
														+ "/aggregators/reports/download/"
														+ reportType
														+ "/"
														+ new Date()
																.getMonthRange().startDate
																.getTime()
														+ "/"
														+ new Date()
																.getMonthRange().endDate
																.getTime(),
												'downloadWindow',
												'height=255,width=250,toolbar=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no ,modal=yes');

							}); */
	});
	function formatSupplierData(data, supplierSalesData) {
		for (var i = 0; i < data.suppliersales.length; i++) {
			supplierSalesData.push({
				label : data.suppliersales[i].name,
				data : data.suppliersales[i].totalSales
			});
		}
	}

	function formatChannelData(data, channelSalesData) {
		for (var i = 0; i < data.channelsales.length; i++) {
			channelSalesData.push({
				label : data.channelsales[i].name,
				data : data.channelsales[i].totalSales
			});
		}
	}

	function formatProductData(data, prodSalesData) {
		for (var i = 0; i < data.productsales.length; i++) {
			prodSalesData.push({
				label : data.productsales[i].sku,
				data : data.productsales[i].totalSales
			});
		}
	}

	function formatSalesData(data, salesData) {
		for (var i = 0; i < data.overAllSales.length; i++) {
			var date = new Date(data.overAllSales[i].date);
			salesData.push([ date.getTime(), data.overAllSales[i].totalSales ]);
		}
	}
</script>
	</jsp:attribute>
</t:basepage>
