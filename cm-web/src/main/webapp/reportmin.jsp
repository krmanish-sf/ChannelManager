<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
	 <div class="main-content">
      <div class="breadcrumbs" id="breadcrumbs"> 
         <ul class="breadcrumb">
			<li><a href="index.jsp"><i class="icon-home home-icon"></i>Home</a></li>
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
              <div class="row">
                <h5 class="center">Create a Visual Report</h5>
                <label class="col-sm-2 control-label no-padding-right">Report</label>
                <select class="form-control width-45 report-type">
                 <option value="">--Select--</option>
                       <option value="customerdata">Customer Database</option>
                  <option value="totalorder">Total Order</option>
                  <option value="breakdowndistributor">Order Break Down By Distributer</option>
                  <option value="breakdownproduct">Order Break Down By Product</option>
                </select>
				</div>
                <div class="space-4"></div>
                <div class="row">
                 <label class="col-sm-2 control-label no-padding-right">Period</label>
                    <select
										class="form-control width-45 select-time-range">
                     <option class="orange2 active" value="THIS_MONTH">This
						Month</option>
					<option class="orange2" value="LAST_SEVEN_DAYS">Last 7
						Days</option>
					<option class="orange2" value="LAST_MONTH">Last Month</option>
					<option class="orange2" value="ALL_TIME">All Time</option>
					<option class="orange2" value="CUSTOM">Custom</option>
                    </select>
								</div>
                <div class="space-4"></div>
                <div class="row">
                <label class="col-sm-2 control-label no-padding-right">Date</label>
                <input type="text" class="width-20 datepicker start"
										placeholder="mm/dd/yyyy" />
                <span>&nbsp;To&nbsp;</span>
                <input type="text" class="width-20 datepicker end"
										placeholder="mm/dd/yyyy" />
										
				</div>
				<div class="space-4"></div>
				<div class="row center col-sm-10">
               	<a href="javascript:;" id="generateReport"
										class="btn btn-info btn-sm">Generate Chart</a>
													<a href="javascript:;" id="downloadReport"
										class="btn btn-info btn-sm"><i class="icon-download"></i>Download Report</a> </div>
			</div>
             <!--  <div class="col-sm-5">
                <div class="widget-container-span">
                  <div class="widget-box">
                    <div class="widget-header header-color-green">
                      <h5>Generate a Sales Report</h5>

                    </div>
                    <div class="widget-body alert-success">
                      <div class="widget-main center">
                        <div class="row">
                         <label
														class="col-sm-2 control-label no-padding-right">Report</label>
														<div class="col-sm-6">
                <select class="form-control width-95" id="reportType">
                  <option value="">--Select--</option>
                  <option value="customerdata">Customer Database</option>
                  <option value="totalorder">Total Order</option>
                  <option value="breakdowndistributor">Order Break Down By Distributer</option>
                  <option value="breakdownproduct">Order Break Down By Product</option>
                </select>
													</div>
			<select class="form-comtrol select-time-range col-sm-3">
				<option value="THIS_MONTH" class="orange2 active">This
					Month</option>
				<option value="LAST_SEVEN_DAYS" class="orange2">Last 7 Days</option>
				<option value="LAST_MONTH" class="orange2">Last Month</option>
				<option value="ALL_TIME" class="orange2">All Time</option>
			</select>
												</div>
                        <div class="space-4"></div>
                        <div class="row">
                        <label
														class="col-sm-2 control-label no-padding-right">Date</label>
                        <input type="text" class="width-30 datepicker"
														placeholder="mm/dd/yyyy" />
                        <span>&nbsp;To&nbsp;</span>
                        <input type="text" class="width-30 datepicker"
														placeholder="mm/dd/yyyy" />
												</div>
                        <div class="space-4"></div>
                         <div class="row center">
													<a href="javascript:;" id="downloadReport"
														class="btn btn-info btn-sm">Download Report</a> </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div> -->
            </div>
            <div class="hr hr-10"></div>
            <div class="row">
            <div class="container">
            <jsp:include page="include/chart.jsp"> 
            <jsp:param value="Performance Summary" name="chartTitle" />
			</jsp:include>
			</div>
            </div>
            <div class="hr hr-10 hr-dotted"></div>
          </div>
          <!-- PAGE CONTENT ENDS --> 
        </div>
        <!-- /.col --> 
      </div>
      <!-- /.row --> 
    </div>
    <!-- /.page-content --> 
	</jsp:attribute>
	<jsp:attribute name="pagejs">
	
<!-- inline scripts related to this page --> 

<script type="text/javascript">
	jQuery(function($) {
		$('#generateReport').on(
				'click',
				function(e) {
					var widget = $(this).parent().parent();
					var reportType = widget.find(
							'select.report-type option:selected').val();
					if (!reportType) {
						$.gritter.add({
							title : 'Generate Report',
							text : 'Please select report type',
							class_name : 'gritter-error'
						});
						widget.find('select.report-type option:selected')
								.focus();
						return false;
					}
					var t1 = widget.find('.start').val()
							|| new Date().getMonthRange().sStartDate;
					var t2 = widget.find('.end').val()
							|| new Date().getMonthRange().sEndDate;
					$(this).CRUD(
							{
								method : "POST",
								url : "/aggregators/reports/" + reportType,
								data : JSON.stringify({
									startDate : t1,
									endDate : t2
								}),
								success : function(data, textStatus, jqXHR) {
									$(this).drawBarChart('sales-charts', data,
											'formatSalesData');
								}
							});
				});
		$('#downloadReport')
				.on(
						'click',
						function() {
							var widget = $(this).parent().parent();
							var reportType = widget.find(
									'select.report-type option:selected').val();
							if (!reportType) {
								$.gritter.add({
									title : 'Download Report',
									text : 'Please select report type',
									class_name : 'gritter-error'
								});
								widget.find(
										'select.report-type option:selected')
										.focus();
								return false;
							}
							var t1 = widget.find('.start').val()
									|| new Date().getMonthRange().sStartDate;
							var t2 = widget.find('.end').val()
									|| new Date().getMonthRange().sEndDate;
							window
									.open(
											CM_SETTINGS.rest_api_base_url
													+ "/aggregators/reports/download/"
													+ reportType + "/"
													+ Date.parse(t1) + "/"
													+ Date.parse(t2),
											'downloadWindow',
											'height=255,width=250,toolbar=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no ,modal=yes');

						});

	});

	function formatSalesData(data, salesData) {
		for (var i = 0; i < data.overAllSales.length; i++) {
			var date = new Date(data.overAllSales[i].date);
			salesData.push([ date.getTime(), data.overAllSales[i].totalSales ]);
		}
	}
</script>
	</jsp:attribute>
</t:basepage>
