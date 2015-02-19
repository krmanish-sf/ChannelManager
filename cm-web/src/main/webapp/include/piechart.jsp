<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<div class="widget-box">
	<div
		class="widget-header widget-header-flat widget-header-small well-sm repbkcolor_green">
		<div class="row container">
			<h5 class="center">${param.chartTitle}</h5>
		</div>
		<div class="container">
			<label class="col-sm-2 control-label no-padding-left">Period</label>
			<select class="form-control width-55 select-time-range">
				<option class="active" value="THIS_MONTH">This Month</option>
				<option value="LAST_SEVEN_DAYS">Last Seven Days</option>
				<option value="LAST_MONTH">Last Month</option>
				<option value="ALL_TIME">All Time</option>
				<option value="CUSTOM">Custom</option>
			</select>
		</div>
		<div class="space-4"></div>
		<div class="container">
			<label class="col-sm-2 control-label no-padding-left ">Date</label> <input
				type="text" class="width-25 datepicker start"
				placeholder="mm/dd/yyyy" /> <span>&nbsp;To&nbsp;</span> <input
				type="text" class="width-25 datepicker end" placeholder="mm/dd/yyyy" />
			<a href="javascript:;"
				onclick="$(this).drawPieChart('${param.targetId }','${param.reportUrl }','${param.dataFormatter }');"
				class="btn btn-info btn-sm pull-right">Generate Report</a>
		</div>
	</div>
	<div class="widget-body">
		<div class="widget-main">
			<div id="${param.targetId }"></div>
		</div>
	</div>
</div>
