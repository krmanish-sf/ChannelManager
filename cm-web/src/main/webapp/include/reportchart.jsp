<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>
<div class="widget-box transparent">
	<div class="widget-header widget-header-flat">
		<h4 class="lighter">
			<i class="icon-signal"></i>&nbsp;${param.chartTitle }
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
			type="text" class="width-25 datepicker end" placeholder="mm/dd/yyyy" />
		<a href="javascript:;"
			onclick="$(this).drawBarChart('${param.targetId }','${param.reportUrl }','${param.dataFormatter }');"
			class="btn btn-info btn-sm pull-right">Generate</a>
	</div>
</div>
<div class="space-2"></div>
<div class="widget-main padding-4">
	<div id="${param.targetId }"></div>

</div>
<!-- /widget-main -->
<!-- <a href="#" class="btn btn-warning btn-sm">Print</a>
<a href="#" class="btn btn-success btn-sm">Download as PDF</a> -->


