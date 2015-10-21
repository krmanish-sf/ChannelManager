var STATUS = [ {
	"statusId" : 6,
	"statusValue" : "Canceled"
}, {
	"statusId" : 3,
	"statusValue" : "Failed"
}, {
	"statusId" : 5,
	"statusValue" : "Manually Processed"
}, {
	"statusId" : 2,
	"statusValue" : "Processed"
}, {
	"statusId" : 7,
	"statusValue" : "Shipped"
}, {
	"statusId" : 0,
	"statusValue" : "Unprocessed"
} ];

var $tooltip = $(
		"<div class='tooltip top in'><div class='tooltip-inner'></div></div>")
		.hide().appendTo('body');
var previousPoint = null;
function $$(selector, context) {
	return jQuery(selector.replace(/(\[|\])/g, '\\$1'), context);
};

function fetchBigcommerceAuthData() {
	var storeUrl = $("input[name=storeurl]").val();
	console.log(storeUrl);
	$(this)
			.CRUD(
					{
						url : "aggregators/channels/bc-app",
						method : "POST",
						data : storeUrl,
						success : function(data) {
							var keySize = Object.keys(data).length;
							if (keySize == 0) {
								$.gritter
										.add({
											title : "Error",
											text : "Verify Store Url, or app might not have been installed on your store"
										});
							}
							$('#bc-auth-token').val(data.authToken);
							$('#store-hash').val(data.context);
						}
					});
}

function fetchShopifyAuthData(){
	var storeUrl = $("input[name=storeurl]").val();
	console.log(storeUrl);
	$(this)
			.CRUD(
					{
						url : "aggregators/channels/shopifyApp",
						method : "POST",
						data : storeUrl,
						success : function(data) {
							var keySize = Object.keys(data).length;
							if (keySize == 0) {
								$.gritter
										.add({
											title : "Error",
											text : "Verify Store Url, or app might not have been installed on your store"
										});
							}
							$('#shopifyAuthId').val(data.authToken);
						}
					});
}

Date.prototype.getWeekRange = function() {
	var curr_date = this;
	var day = curr_date.getDay();
	// var diff = curr_date.getDate() - day + (day == 0 ? -6 : 1); // 0 for
	// sunday
	var diff = curr_date.getDate() - 6;
	var week_start_tstmp = curr_date.setDate(diff);
	var week_start = new Date(week_start_tstmp);
	var week_start_date = week_start.format();
	var week_end = new Date(week_start_tstmp); // first day of week
	week_end = new Date(week_end.setDate(week_end.getDate() + 6));
	var week_end_date = week_end.format();
	return {
		startDate : week_start,
		endDate : week_end,
		sStartDate : week_start_date,
		sEndDate : week_end_date
	};
};
Date.prototype.format = function(seperator) {
	if (!seperator)
		seperator = '/';
	var self = this;
	var m = ("0" + (self.getMonth() + 1)).slice(-2); // in javascript
	// month start from 0.
	var d = ("0" + self.getDate()).slice(-2); // add leading zero
	var y = self.getFullYear();
	return m + seperator + d + seperator + y;
};
Date.prototype.getMonthRange = function() {
	var self = this;
	var first_day = new Date(self.getFullYear(), self.getMonth(), 1);
	var last_day = new Date(self.getFullYear(), self.getMonth() + 1, 0);
	var month_start_date = first_day.format();
	var month_end_date = last_day.format();
	return {
		startDate : first_day,
		endDate : last_day,
		sStartDate : month_start_date,
		sEndDate : month_end_date
	};
};
Date.prototype.firstDayInPreviousMonth = function() {
	return new Date(this.getFullYear(), this.getMonth() - 1, 1);
};
Date.prototype.lastDayInPreviousMonth = function() {
	return new Date(this.getFullYear(), this.getMonth(), 0);
};
function DataBinder(object_id, dataObject) {
	// Use a jQuery object as simple PubSub
	var pubSub = jQuery({});
	// We expect a `data` element specifying the binding
	// in the form: data-bind-<object_id>="<property_name>"
	var data_attr = "bind-" + object_id, message = object_id + ":change";
	$("[data-" + data_attr + "]").each(function(e, i) {
		var $bound = jQuery(this);
		var attr_name = $bound.data(data_attr);
		var i = attr_name.indexOf(':');
		if (i > 0) {
			var arr = attr_name.split(":");
			if (eval('typeof ' + arr[0]) === 'function') {
				// DO the recursive object binding
				try {
					attr_name = eval(arr[0] + '(dataObject,"' + arr[1] + '")');
				} catch (e) {
				}
			}
		}
		var val = '';
		try {
			val = eval('dataObject.' + attr_name);
		} catch (e) {
		}
		if ($bound.is("input, textarea, select")) {
			if ($bound.is(':radio')) {
				if ($bound.val() == val)
					$bound.prop('checked', true);
				else
					$bound.prop('checked', false);
			} else if ($bound.is(':checkbox')) {
				if ($bound.val() == val)
					$bound.prop('checked', true);
				else
					$bound.prop('checked', false);
			} else
				$bound.val(val);
		} else {
			$bound.html(val);
		}
	});
	/*
	 * Listen to change events on elements with the data-binding attribute and
	 * proxy them to the PubSub, so that the change is "broadcasted" to all
	 * connected objects
	 */
	jQuery(document).on("change", "[data-" + data_attr + "]", function(evt) {
		var $input = jQuery(this);
		var d = $input.data(data_attr);
		pubSub.trigger(message, [ d, $input.val(), $input ]);
	});
	// PubSub propagates changes to all bound elements, setting value of
	// input tags or HTML content of other tags
	pubSub.on(message, function(evt, prop_name, new_val) {
		var attr_name = prop_name;
		var i = attr_name.indexOf(':');
		if (i > 0) {
			var arr = attr_name.split(":");
			if (eval('typeof ' + arr[0]) === 'function') {
				// DO the recursive object binding
				try {
					attr_name = eval(arr[0] + '(dataObject,"' + arr[1] + '")');
				} catch (e) {
				}
			}
		}

		try {
			if (isNaN(new_val))
				eval("dataObject." + attr_name + "='" + new_val + "'");
			else
				eval("dataObject." + attr_name + "=" + new_val);
		} catch (e) {
		}
	});
	return pubSub;
};

function GenericBinder(uid, dataObject) {
	var binder = new DataBinder(uid, dataObject);
	$.extend({
		// The attribute setter publish changes using the
		// DataBinder PubSub
		set : function(attr_name, val) {
			eval('this.attr_name = val');
			binder.trigger(uid + ":change", [ attr_name, val, this ]);
		},
		get : function(attr_name) {
			return eval('this.attr_name');
		}
	}, dataObject);
	// Subscribe to the PubSub
	binder.on(uid + ":change", function(evt, attr_name, new_val, initiator) {
		try {
			if (initiator !== dataObject)
				eval('dataObject.' + attr_name + ' = new_val');
		} catch (e) {
			// console.log(e);
		}
	});
};

function customMapper(obj, expr) {
	var l = expr.length;
	var ad = 0;// ArrayDepth
	var m = [];// ArrayDepthMapping
	var retVal = '';
	for (var i = 0; i < l; i++) {
		switch (expr.charAt(i)) {
		case '[':
			m[ad] = i;
			ad++;
			break;
		case ']':
			// var e = expr.substr(0, m[ad - 1]);
			var c = evalArray(eval('obj.' + retVal), expr.substring(
					m[ad - 1] + 1, i));
			retVal += '[' + c + ']';
			ad--;
			break;
		default:
			if (ad == 0)
				retVal += expr.charAt(i);
			break;
		}
	}
	// console.log(retVal);
	return retVal;
};

function evalArray(obj, expr) {
	if (typeof obj === 'object' && expr.indexOf('.') < 0) {
		return obj;
	} else if (typeof obj === 'array' || obj.length != null
			&& expr.indexOf('=') > 0) {
		for (var i = 0; i < obj.length; i++) {
			var j = eval('obj[' + i + '].' + expr.split('=')[0]);
			if (j == expr.split('=')[1])
				return i;
		}
	}
};

function drawSalesReportTable(data) {
	// var table = $('#tasks2').DataTable({
	// 'dom' : 't'
	// });
	var table = $('#tasks2').DataTable();
	table.clear().draw();
	for (var i = 0; i < data.length; i++) {
		table.row.add([ data[i].sku, "$" + data[i].totalSales ]).draw();
	}
};
(function($) {
	$("select.select-time-range")
			.each(
					function(i, e) {
						var sel = $(this).val();
						var widget = $(this).parent().parent().parent();
						switch (sel) {
						case "THIS_MONTH":
							widget.find('.start').val(
									new Date().getMonthRange().sStartDate);
							widget.find('.end').val(
									new Date().getMonthRange().sEndDate);
							break;
						case "LAST_SEVEN_DAYS":
							widget.find('.start').val(
									new Date().getWeekRange().sStartDate);
							widget.find('.end').val(
									new Date().getWeekRange().sEndDate);
							break;
						case "LAST_MONTH":
							widget.find('.start').val(
									new Date().firstDayInPreviousMonth()
											.format());
							widget.find('.end').val(
									new Date().lastDayInPreviousMonth()
											.format());
							break;
						case "ALL_TIME":
							widget
									.find('.start')
									.val(
											new Date("2010-01-01")
													.getMonthRange().sStartDate);
							widget.find('.end').val(
									new Date().getWeekRange().sEndDate);
							break;
						}
					});
	$(document)
			.on(
					"change",
					".select-time-range",
					function() {
						var sel = $(this).val();
						var widget = $(this).parent().parent().parent();
						switch (sel) {
						case "THIS_MONTH":
							widget.find('.start').val(
									new Date().getMonthRange().sStartDate);
							widget.find('.end').val(
									new Date().getMonthRange().sEndDate);
							break;
						case "LAST_SEVEN_DAYS":
							widget.find('.start').val(
									new Date().getWeekRange().sStartDate);
							widget.find('.end').val(
									new Date().getWeekRange().sEndDate);
							break;
						case "LAST_MONTH":
							widget.find('.start').val(
									new Date().firstDayInPreviousMonth()
											.format());
							widget.find('.end').val(
									new Date().lastDayInPreviousMonth()
											.format());
							break;
						case "ALL_TIME":
							widget
									.find('.start')
									.val(
											new Date("2010-01-01")
													.getMonthRange().sStartDate);
							widget.find('.end').val(
									new Date().getWeekRange().sEndDate);
							break;
						}
					});
	$(document).on('click', 'input.datepicker', function(e) {
		var widget = $(this).parent().parent().parent();
		widget.find(".select-time-range").val("CUSTOM");
	});
	$.fn.drawPieChart = function(placeholder, data, data_formatter) {
		var options = {
			is3D : true
		};
		var widget = $('#' + placeholder).parent().parent().parent();
		if (typeof data === 'string') {
			$(this).CRUD({
				url : data,
				method : "POST",
				data : JSON.stringify({
					startDate : widget.find('.start').val(),
					endDate : widget.find('.end').val()
				}),
				success : function(data, textStatus, jqXHR) {
					var formatted_data = [];
					if (typeof data_formatter === 'string') {
						eval(data_formatter + '(data, formatted_data)');
						plotInternal(formatted_data, options);
					} else {
						console.log('data format provider not defined.');
					}
				}
			});
		} else {
			var formatted_data = [];
			if (typeof data_formatter === 'string') {
				eval(data_formatter + '(data, formatted_data)');
				plotInternal(formatted_data, options);
			} else {
				console.log('data format provider not defined.');
			}
		}

		function plotInternal(data, options) {
			var chart = new google.visualization.PieChart(document
					.getElementById(placeholder));
			var d = google.visualization.arrayToDataTable(data);
			chart.draw(d, options);
		}
	};
	$.fn.drawLineChart = function(placeholder, data, data_formatter) {
		var options = {
			pointSize : 20,
			pointShape : 'circle'
		};
		var widget = $('#' + placeholder).parent().parent();
		if (typeof data === 'string') {
			$(this).CRUD({
				url : data,
				method : "POST",
				data : JSON.stringify({
					startDate : widget.find('.start').val(),
					endDate : widget.find('.end').val()
				}),
				success : function(data, textStatus, jqXHR) {
					var formatted_data = [];
					if (typeof data_formatter === 'string') {
						eval(data_formatter + '(data, formatted_data)');
						plotInternal(formatted_data, options);
					} else {
						console.log('data format provider not defined.');
					}
				}
			});
		} else {
			var formatted_data = [];
			if (typeof data_formatter === 'string') {
				eval(data_formatter + '(data, formatted_data)');
				plotInternal(formatted_data, options);
			} else {
				console.log('data format provider not defined.');
			}
		}

		function plotInternal(data, options) {
			var chart = new google.charts.Line(document
					.getElementById(placeholder));
			var d = google.visualization.arrayToDataTable(data);
			chart.draw(d, options);
		}
	};

	$.fn.drawBarChart = function(placeholder, data, data_formatter) {
		var options = {
			chart : {
				// title : 'Company Performance',
				// subtitle : 'Sales, Expenses, and Profit: 2014-2017',
				bar : {
					groupWidth : "100"
				}
			}
		};
		var widget = $('#' + placeholder).parent().parent();
		$(this).CRUD({
			url : data,
			method : "POST",
			data : JSON.stringify({
				startDate : widget.find('.start').val(),
				endDate : widget.find('.end').val()
			}),
			success : function(data, textStatus, jqXHR) {
				var formatted_data = [];
				if (typeof data_formatter === 'string') {
					eval(data_formatter + '(data, formatted_data)');
					plotInternal(formatted_data, options);
				} else {
					console.log('data format provider not defined.');
				}
			}
		});

		var chart = new google.charts.Bar(document.getElementById(placeholder));

		function plotInternal(data, options) {
			var d = google.visualization.arrayToDataTable(data);
			chart.draw(d, options);
		}
	};

}(jQuery));

(function($) {
	$.ajaxSetup({
		cache : false,
		xhrFields : {
			withCredentials : true
		}
	});
	$.fn.CRUD = function(options) {
		var settings = $.extend({
			url : '',
			method : "GET",
			cache : false,
			data : null,
			contentType : "application/json"
		}, options);
		settings.url = '/cm-rest/' + settings.url;
		$.ajax(settings);
		return this;
	};
	$(document).ajaxError(function(event, jqxhr, settings, exception) {
		console.log(event);
		console.log(jqxhr);
		console.log(settings);
		console.log(exception);
	});
	$(document)
			.ajaxSend(
					function(event, jqXHR, ajaxOptions) {
						switch (typeof ajaxOptions.message) {
						case "undefined":
							$('#wait-message')
									.html(
											'<i class="icon-spinner icon-spin green bigger-125"></i> Please wait...');
							$('#wait-message').show();
							break;
						case "string":
							$('#wait-message').html(ajaxOptions.message);
							$('#wait-message').show();
							break;
						case "boolean":
							$('#wait-modal').modal('show');
							break;
						case "function":
							ajaxOptions.message();
							break;
						default:
							$('#wait-message').show();
							break;
						}
					});

	$(document).ajaxError(function(event, XMLHttpRequest, ajaxOptions) {
		if (typeof ajaxOptions.message == 'object') {
			$.gritter.add({
				title : ajaxOptions.message.title,
				text : ajaxOptions.message.text
			});
		}
		if (XMLHttpRequest.status == 401) {
			$.gritter.add({
				title : "Session expired",
				text : "Your session has expired. Please login again.",
				after_close : function(e, manual_close) {
					window.location = 'login.jsp';
				}
			});

		}
	});

	$.gritter.options.position = 'center-top';
	$(document).ajaxSuccess(function(event, XMLHttpRequest, ajaxOptions) {
		if (typeof ajaxOptions.message === 'object') {
			$.gritter.add({
				title : ajaxOptions.message.title,
				text : ajaxOptions.message.text

			});
		}
	});
	$(document).ajaxComplete(function(event, jqXHR, ajaxOptions) {

		if (jQuery.active - 1 == 0) {
			$('#wait-message').hide();
			$('#wait-modal').modal('hide');
		}
	});
	if ($.CM == null || typeof $.CM == 'undefined')
		$.CM = {};
	$.CM.pullorder = function(e) {
		var url = "/aggregators/channels/pull";
		if (e != null) {
			var channel = tableimportchannel.row(e[0]).data();
			url = "/aggregators/channels/" + channel.channelId + "/pull";
		}
		$(this).CRUD({
			method : "GET",
			url : url,
			success : function(data) {
				$.gritter.add({
					title : 'Pull Order',
					text : data
				});
				$.CM.updateOrderSummary();
			},
			error : function(data, jqXhr, msg) {

			},
			message : true
		});
	};
	$.CM.getOrderModification = function(detailId) {
		$('#tableordermods')
				.DataTable(
						{
							// "order" : [ [ 4, "desc" ] ],
							bSort : false,
							bFilter : false,
							"language" : {
								"emptyTable" : "No Order modification history found."
							},
							"bDestroy" : true,
							"sAjaxDataProp" : "",
							"ajax" : function(data, callback, settings) {
								$(this)
										.CRUD(
												{
													"method" : "GET",
													"url" : 'aggregators/orders/orderdetails/'
															+ detailId
															+ '/modifications',
													"message" : true,
													"success" : function(d) {
														callback(d);
													}
												});
							},
							"aoColumns" : [
									{
										"mData" : function(d) {
											return "<span style='display:none'>"
													+ d.insertionTm
													+ "</span>"
													+ new Date(d.insertionTm)
															.toLocaleString();
										}
									},
									{
										"mData" : function(orderDetail) {
											var status = '';
											for (var i = 0; i < STATUS.length; i++) {
												if (STATUS[i].statusId == orderDetail.statusId)
													status = STATUS[i].statusValue
											}
											return status;
										}
									},
									{
										"mData" : "operation"
									},
									{
										"mData" : "sku"
									},
									{
										"mData" : "supplierOrderNumber"
									},
									{
										"mData" : "supplier.supplierName",
										"defaultContent" : "--"
									},
									{
										"mData" : "quantity"
									},
									{
										"mData" : function(d) {
											return d.processingTm ? new Date(
													d.processingTm)
													.toLocaleString() : '';
										}
									}, {
										"mData" : "salePrice"
									} ]
						});
	};

	$.CM.processOrders = function() {
		$(this).CRUD({
			url : 'aggregators/orders/processed/bulk/process',
			data : JSON.stringify(null),
			method : 'POST',
			success : function(data) {
				$.gritter.add({
					title : 'Order Processing Update',
					text : data.length + ' Order(s) processed.'

				});
				if (typeof $.CM.updateOrderSummary === 'function')
					$.CM.updateOrderSummary();
			},
			message : true
		});
	};
	$.CM.planify = function(data) {
		for (var i = 0; i < data.columns.length; i++) {
			column = data.columns[i];
			column.searchRegex = column.search.regex;
			column.searchValue = column.search.value;
			delete (column.search);
		}
		return data;
	};
	$.CM.updateOrderSummary = function() {
		$(this)
				.CRUD(
						{
							method : "POST",
							url : 'aggregators/reports',
							data : JSON
									.stringify({
										startDate : new Date().getMonthRange().sStartDate,
										endDate : new Date().getMonthRange().sEndDate
									}),
							success : function(data, textStatus, jqXHR) {
								try {
									$("#tasks").empty();
									var productsales = data.productsales;
									var suppliersales = data.suppliersales;
									var channelsales = data.channelsales;
									var chartData = [];
									var pieChartData = [];
									for (var i = 0; i < data.overAllSales.length; i++) {
										var date = new Date(
												data.overAllSales[i].date);
										chartData
												.push([
														date.getTime(),
														data.overAllSales[i].totalSales ]);
									}

									$(this).drawLineChart('sales-charts', data,
											'formatSalesData');

									for (var i = 0; i < channelsales.length; i++) {
										var row = '<li class="item-orange clearfix"><label class="inline"> <span>'
												+ channelsales[i].name
												+ '</span> </label><div class="pull-right">$'
												+ channelsales[i].totalSales
												+ '</div></li>';
										$(row).appendTo("#tasks");
										pieChartData.push({
											label : channelsales[i].name,
											data : channelsales[i].totalSales
										});
									}
									// drawPieChart('piechart-placeholder',
									// pieChartData);
									var supplierSalesData = [];
									for (var i = 0; i < suppliersales.length; i++) {
										var row = '<li class="item-orange clearfix"><label class="inline"> <span>'
												+ suppliersales[i].name
												+ '</span> </label><div class="pull-right">$'
												+ suppliersales[i].totalSales
												+ '</div></li>';
										$(row).appendTo("#tasks1");
										supplierSalesData.push({
											label : suppliersales[i].name,
											data : suppliersales[i].totalSales
										});
									}

									var prodSalesPieData = [];
									for (var i = 0; i < productsales.length; i++) {
										prodSalesPieData.push({
											label : productsales[i].sku,
											data : productsales[i].totalSales
										});
									}
									drawSalesReportTable(productsales);

								} catch (e) {
									console.log('Report box not present.');
								}
								$('#unprocessedCount span.infobox-data-number')
										.html(
												data.OrderSummaryData.unprocessedCount);
								$('#unprocessedValue span.infobox-data-number')
										.html(
												'$'
														+ data.OrderSummaryData.unprocessedAmount
																.toFixed(2));
								$('#unresolvedCount span.infobox-data-number')
										.html(
												data.OrderSummaryData.unresolvedCount);
								$('#unresolvedValue span.infobox-data-number')
										.html(
												'$'
														+ data.OrderSummaryData.unresolvedAmount
																.toFixed(2));
								$('strong.unresolvedcount').html(
										data.OrderSummaryData.unresolvedCount);
							}
						});
	};
	var tableShippingMap;
	var existingShippingMapping;
	$.CM.viewShippingMap = function(e) {
		var channel = tableimportchannel.row(e[0]).data();
		$("#channelHidenField").val(channel.channelId);

		tableShippingMap = $('#tableShippingMap')
				.DataTable(
						{
							"bPaginate" : false,
							"bDestroy" : true,
							"bLengthChange" : false,
							"sAjaxSource" : "/aggregators/channels/shipping/"
									+ channel.channelId,
							"fnServerData" : function(sSource, aoData,
									fnCallback, oSettings) {
								oSettings.jqXHR = $(this).CRUD({
									type : "GET",
									url : sSource,
									data : aoData,
									cache : false,
									success : function(data) {
										fnCallback(data);
										existingShippingMapping = data;

									}
								});

							},
							"sAjaxDataProp" : '',
							"aoColumns" : [
									{
										"mData" : "shippingRegEx"
									},
									{
										"mData" : "oimShippingCarrier.name"
									},
									{
										"mData" : "shippingMethod.name"
									},

									{
										"mData" : function(obj) {
											if (obj.oimChannel == null) {
												return '<a class="btn btn-info btn-minier radius-2 dropdown-hover" disabled><i class="icon-pencil"></i></a>'
														+ '<a class="btn btn-danger btn-minier radius-2 dropdown-hover" disabled><i class="icon-trash" disabled></i></a>'
											} else {
												var channelId = obj.oimChannel != null ? obj.oimChannel.channelId
														: null;
												return '<a class="btn btn-info btn-minier radius-2 dropdown-hover" onclick="$.CM.editShippingMethod($(this).parent().parent(), '
														+ obj.id
														+ ','
														+ channelId
														+ ')";><i class="icon-pencil"></i>'
														+ '<span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">Update</span></a>'
														+ '<a class="btn btn-danger btn-minier radius-2 dropdown-hover" onclick="$.CM.deleteShippingMethod($(this).parent().parent(), '
														+ obj.id
														+ ','
														+ channelId
														+ ')";><i class="icon-trash"></i>'
														+ '<span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">Delete</span></a>';

											}
										},
										"bsortable" : "false"
									} ]
						});
	};

	$.CM.deleteShippingMethod = function(e, shippingMethodId, channelId) {
		if (!channelId) {
			$.gritter
					.add({
						title : 'Not Allowed',
						text : "This shipping method is not specific to any channel. So you can not delete it."
					});
			return;
		}
		var isDelete = confirm("You are trying to delete an existing tracking. Are you sure?");
		if (isDelete) {
			$(this).CRUD(
					{
						url : 'aggregators/shipping/deleteShipping/'
								+ shippingMethodId,
						method : 'DELETE',
						message : true,
						success : function(data) {
							$.gritter.add({
								title : 'Shipping Method',
								text : "Deleted Successfully."
							});
							tableShippingMap.ajax.reload();
						}
					});

		}

	}

	var shippingMapEditTable;
	var tempShippingMethod;
	$.CM.editShippingMethod = function(e, objId, channelId) {
		tempShippingMethod = null;
		$(this).CRUD({
			type : "GET",
			url : "aggregators/suppliers/shippingmethods",
			message : true,
			cache : true,
			success : function(json) {
				var data = new Array();
				$.each(json, function(i, e) {
					data.push({
						label : e.fullName,
						value : e
					});
				});
				$("#shippingCarrierMap1").autocomplete({
					minLength : 0,
					appendTo : $("#shippingCarrierMap1").parent(),
					source : data,
					select : function(event, ui) {
						event.preventDefault();
						$('#shippingCarrierMap1').val(ui.item.label);
						tempShippingMethod = ui.item.value;
					}
				});
			}
		});
		if (!channelId) {
			$.gritter
					.add({
						title : 'Not Allowed',
						text : "This shipping method is not specific to any channel. So you can not update it."
					});
			return;
		}
		// href="#EditChannelShippingModal" data-toggle="modal"
		$('#EditChannelShippingModal').modal('show');
		var shipping = tableShippingMap.row(e[0]).data();
		var shippingTemp = JSON.parse(JSON.stringify(shipping));
		var shippingArray = null;
		shippingArray = new Array(shippingTemp);
		$('#tableChannelShippingMap').DataTable().destroy();
		shippingMapEditTable = $('#tableChannelShippingMap')
				.DataTable(
						{
							"dom" : 't',
							"sort" : false,
							"data" : shippingArray,
							"destroy" : true,
							"columns" : [
									{
										"mData" : function(obj) {
											return "<input type=text class='width-100' id='shippingText1' name='shippingText' value='"
													+ shippingArray[0].shippingRegEx
													+ "' required/>";
										}
									},
									{
										"mData" : function() {
											return "<input type=text class='form-control ui-autocomplete-input width-100' id='shippingCarrierMap1' name='shippingCarrier' value='"
													+ shippingArray[0].shippingMethod.fullName
													+ "' required/>";
										}
									},
									{
										"mData" : function() {
											return '<a class="btn btn-success radius-2 dropdown-hover" onclick="$.CM.updateShippingMethod($(this).parent().parent(),'
													+ shippingArray[0].id
													+ ')";><i class="icon-save">Update</i>'
													+ '<span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">update</span></a>';
										}
									} ]
						});

		return;

	}
	// shippingCarrierMap1

	$.CM.updateShippingMethod = function(e, channelShippingId) {
		var d = shippingMapEditTable.row(e[0]).data();
		var carrierId;
		var methodId
		if (!tempShippingMethod) {
			carrierId = d.oimShippingCarrier.id;
			methodId = d.shippingMethod.id;
		} else {
			var carrierId = tempShippingMethod.shippingCarrier.id;
			var methodId = tempShippingMethod.id;
		}
		var channelId = d.oimChannel.channelId;
		var shippingText = $("#shippingText1").val();
		var mappingText = $("#shippingCarrierMap1").val();

		var requestData = {
			"channelId" : channelId,
			"methodId" : methodId,
			"carrierId" : carrierId,
			"shippingText" : shippingText,
			"channelShippingId" : channelShippingId,
			"mappingText" : mappingText

		};
		// {"channelId":3041,"methodId":34,"carrierId":2,"shippingText":"Tier3","channelShippingId":102}
		$(this).CRUD({
			url : 'aggregators/shipping/updateShipping/' + channelShippingId,
			method : 'POST',
			data : JSON.stringify(requestData),
			message : true,
			success : function(data) {
				$.gritter.add({
					title : 'Shipping Method',
					text : data
				});
				$('#EditChannelShippingModal').modal('hide');
				tableShippingMap.ajax.reload();
			}
		});

	}
	$.CM.viewSupplierShippingMap = function(e, tableId) {
		var supplierId;
		if (typeof e == 'number')
			supplierId = e;
		else {
			var vendor_supplier = table_vendorSuppliers.row(e[0]).data();
			supplierId = vendor_supplier.oimSuppliers.supplierId;
		}
		if (!tableId)
			tableId = '#tableShippingMap';
		$(tableId)
				.DataTable(
						{
							"sDom" : 't',
							"sAjaxSource" : "aggregators/suppliers/"
									+ supplierId + "/shippingcarriers",
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
										"mData" : function(d) {
											return "Default";
										}
									},
									{
										"mData" : "name"
									},
									{
										"mData" : function(d) {
											return "<a style='cursor:pointer;' onclick='$.CM.showChannelSupplierShipMap($(this).parent().parent(),"
													+ supplierId
													+ ")'>Edit</a>";
										}
									} ],
							"bDestroy" : true
						});
	};
	$.CM.showChannelSupplierShipMap = function(tr, supplierId) {
		$('#editshippingmethods').modal('show');
		var table = $($(tr).closest('table')).DataTable();
		var row = table.row(tr);
		var d = row.data();
		$('#editshippingmethods h4.modal-title').text(
				d.name + " Shipping Mapping");
		$('#tableShippingMap')
				.DataTable(
						{
							"language" : {
								"emptyTable" : "Shipping Overides not permitted for the Supplier."
							},
							"sDom" : 't',
							"sAjaxSource" : "aggregators/suppliers/"
									+ supplierId + "/shippingcarriers/" + d.id,
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
										"mData" : function(d) {
											if (d.override) {
												return "Overridden";
											}
											return "Default";
										}
									},
									{
										"mData" : "shippingMethod.name"
									},

									{
										"mData" : "carrierName"
									},
									{
										"mData" : function(d) {
											if (d.override) {
												return '<input type="text" value="'
														+ d.overrideMethod
														+ '"/><a style="cursor:pointer;" onclick="$.CM.deleteShippingOverride(this,'
														+ supplierId
														+ ','
														+ d.id
														+ ')">Delete</a>';
											}
											return '<input type="text" value="'
													+ d.name
													+ '"/><a style="cursor:pointer;" onclick="$.CM.saveShippingOverride(this,'
													+ supplierId + ',' + d.id
													+ ')">Override</a>';

										}
									} ],
							"bDestroy" : true
						});
	};
	$.CM.deleteShippingOverride = function(e, supplierId, supplierMethodId) {
		table = $(e).closest('table');
		table = table.DataTable();
		$(this)
				.CRUD(
						{
							method : "DELETE",
							url : 'aggregators/suppliers/' + supplierId
									+ '/shippingcarriers/overrides/'
									+ supplierMethodId,
							success : function(a, b, c) {
								table.ajax.reload();
							},
							message : {
								title : "Delete Shipping Override",
								text : "Override deleted successfully."
							}
						});
	};
	$.CM.saveShippingOverride = function(e, supplierId, supplierMethodId) {
		table = $(e).closest('table');
		table = table.DataTable();
		$(this)
				.CRUD(
						{
							method : "POST",
							url : 'aggregators/suppliers/' + supplierId
									+ '/shippingcarriers/overrides/'
									+ supplierMethodId,
							data : $(e).parent().find('input').val(),
							success : function(a, b, c) {
								table.ajax.reload();
							},
							message : {
								title : "Save Shipping Override",
								text : "Override saved successfully."
							}
						});
	};
	$.CM.processOrder = function(order) {
		$(this).CRUD({
			url : "aggregators/orders/processed/" + order.orderId,
			method : "POST",
			data : JSON.stringify(order),
			success : function(order) {
				$.gritter.add({
					title : 'Order Processing',
					text : 'Order Processed successfully.',
					class_name : 'gritter-success'
				});
				table_xy.ajax.reload();
				getAlerts();
			},
			error : function(a, b, c) {
				$.gritter.add({
					title : 'Order Processing',
					text : 'Order Processing Failed.',
					class_name : 'gritter-error'
				});
				table_xy.ajax.reload();
				getAlerts();
			}
		});
	};
	$.CM.trackOrder = function(orderDetailId) {
		$(this).CRUD({
			url : "aggregators/orders/track/" + orderDetailId,
			method : "GET",
			success : function(status) {
				$('#orderStatus' + orderDetailId).text(status);
				$.gritter.add({
					title : 'Order Tracking',
					text : 'Order Status : ' + status,
					class_name : 'gritter-success'
				});

			},
			error : function(a, b, c) {
				$.gritter.add({
					title : 'Order Tracking',
					text : 'Order Tracking Failed.',
					class_name : 'gritter-error'
				});
			}

		});
	};
	$.CM.bindChannels = function(tableId) {
		return $(tableId)
				.DataTable(
						{
							"bPaginate" : false,
							"bLengthChange" : false,
							"bPaginate" : false,
							"bInfo" : false,
							"bFilter" : false,
							"bJQueryUI" : true,
							"sDom" : 'lfrtip',
							"sAjaxSource" : 'aggregators/channels',
							"fnServerData" : function(sSource, aoData,
									fnCallback, oSettings) {
								oSettings.jqXHR = $(this).CRUD({
									type : "GET",
									url : sSource,
									data : aoData,
									success : fnCallback
								});
							},
							"sAjaxDataProp" : '',
							"aoColumns" : [
									{
										"mData" : "channelName"
									},
									{
										"mData" : function(channel) {
											var r = '';
											for (var i = 0; i < channel.oimChannelAccessDetailses.length; i++) {
												if (channel.oimChannelAccessDetailses[i].oimChannelAccessFields.fieldId == 1) {
													if (channel.oimChannelAccessDetailses[i].detailFieldValue == null)
														channel.oimChannelAccessDetailses[i].detailFieldValue = "N/A";
													r = '<a class="btn btn-default icon-info-sign btn-xs visible-xs addresspop" data-toggle="popover" data-container="body"  data-placement="bottom" data-content="'
															+ channel.oimChannelAccessDetailses[i].detailFieldValue
															+ '" data-original-title="Url"></a><div class="hidden-xs">'
															+ channel.oimChannelAccessDetailses[i].detailFieldValue
															+ '</div>';
												}
											}
											if (channel.lastFetch)
												r += '<p><i class="icon-circle-arrow-left" title="Last pull"></i><small>'
														+ channel.lastFetch
														+ '</small></p>';
											return r;
										}
									},
									{
										"mData" : function(channel) {
											if (channel.testMode) {
												return channel.oimSupportedChannels.channelName
														+ "<p><strong>(Test Mode)</strong></p>"
											} else {
												return channel.oimSupportedChannels.channelName
											}

										}
									},
									{
										"bSortable" : false,
										"sWidth" : "100px",
										"mData" : function(channel) {
											var toolTip = "Import Orders";
											var onClick = 'onclick="$.CM.pullorder($(this).parent().parent())"';
											var icon = "icon-circle-arrow-left";
											if (channel.oimSupportedChannels.supportedChannelId == 0
													|| channel.oimSupportedChannels.supportedChannelId == 7) {
												toolTip += " not supported";
												onClick = "";
												icon = "icon-ban-circle";
											}
											return '<a class="btn btn-yellow btn-minier radius-2 dropdown-hover"  '
													+ onClick
													+ '><i class="'
													+ icon
													+ '"></i> <span role="menu" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">'
													+ toolTip
													+ '</span></a>&nbsp;<a class="btn btn-success btn-minier radius-2 dropdown-hover" data-toggle="modal" href="#uploadordermodal" onclick="uploadFile($(this).parent().parent().parent())"><i class="icon-upload"></i> <span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">File Upload</span></a>&nbsp;'
													+ '<a href="#singleordermodal" onclick="singleorder($(this).parent().parent().parent())" class="btn btn-purple btn-minier radius-2 dropdown-hover" data-toggle="modal"><i class=" icon-plus-sign"></i> <span role="menu" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">Single Order Entry</span> </a>'
													+ '&nbsp;<a href="#channelShippingModal" onclick="$.CM.viewShippingMap($(this).parent().parent())" class="btn btn-purple btn-minier radius-2 dropdown-hover" data-toggle="modal"><i class="icon-exchange"></i> <span role="menu" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">View Shipping Mapping</span></a>';
										}
									},
									{
										"bSortable" : false,
										"mData" : function(row) {
											return '<a class="btn btn-info btn-minier radius-2 dropdown-hover" data-toggle="modal" href="#mychanneledit" onclick="channel(\'edit\',$(this).parent().parent())"><i class="icon-pencil"></i><span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">Edit Channel Setting</span></a><a class="btn btn-danger btn-minier radius-2 dropdown-hover" onclick="del($($($(this).parent()).parent()))"><i class="icon-trash"></i><span data-rel="tooltip" class="dropdown-menu tooltip-success purple dropdown-menu dropdown-yellow pull-right dropdown-caret dropdown-close">Delete Channel</span></a>';
										}
									} ]
						});
	};
	$.CM.resetChannelForm = function(val) {
		switch (val) {
		case "1":
		case "2":
		case "3":
		case "6":
			$(".store-info").each(
					function(i, e) {
						if ($(this).hasClass('amazon-store')
								|| $(this).hasClass('yahoo-store')
								|| $(this).hasClass('shop-store')
								|| $(this).hasClass('shopify-store')
								|| $(this).hasClass('bc-store'))
							$(this).hide();
						else
							$(this).show();
					});
			break;
		case "8":
			$(".store-info").each(function(i, e) {
				if ($(this).hasClass('shopify-store'))
					$(this).show();
				else
					$(this).hide();
			});
			break;
		case "9":
			$(".store-info").each(function(i, e) {
				if ($(this).hasClass('bc-store'))
					$(this).show();
				else
					$(this).hide();
			});
			break;
		case "7":
			$(".store-info").each(function(i, e) {
				if ($(this).hasClass('shop-store'))
					$(this).show();
				else
					$(this).hide();
			});
			break;
		case "4":
			$(".store-info").each(function(i, e) {
				if ($(this).hasClass('amazon-store'))
					$(this).show();
				else
					$(this).hide();
			});
			break;
		case "5":
			$(".store-info").each(function(i, e) {
				if ($(this).hasClass('yahoo-store'))
					$(this).show();
				else
					$(this).hide();
			});
			break;
		case "0":
		default:
			$(".store-info").hide();
			break;
		}
		$('table#tablesuppliers tbody tr').each(function(i, e) {
			var checkbox = $(e).find('input[type="checkbox"]');
			if (val == "0") {
				checkbox.prop('checked', false);
			}
			var input = $(e).find('input[type="text"]');
			var select = $(e).find('select');
			if (checkbox.is(":checked")) {
				input.show();
				select.show();
			} else {
				input.hide();
				select.hide();
			}
		});
	};

	// test

	$.CM.addShippingMapping = function(shippingMethod) {
		var methodId = shippingMethod.id;
		var carrierId = shippingMethod.shippingCarrier.id;
		var channelId = $("#channelHidenField").val();
		var shippingText = $("#shippingText").val();
		// existingShippingMapping
		for (var i = 0; i < existingShippingMapping.length; i++) {
			var obj = existingShippingMapping[i];
			if (shippingText.localeCompare(obj.shippingRegEx) == 0) {
				$.gritter
						.add({
							title : 'Add Shipping',
							text : "Shipping Text already exists. Please try different value",
							class_name : 'gritter-error'
						});
				return;
			}
		}
		var d = {
			"channelId" : channelId,
			"methodId" : methodId,
			"carrierId" : carrierId,
			"shippingText" : shippingText
		};
		var data = JSON.stringify(d);
		$(this).CRUD({
			method : "POST",
			url : 'aggregators/channels/addShippingMethods/addShipping',
			data : data,
			success : function(a, b, c) {
				$.gritter.add({
					title : 'Add Shipping',
					text : a,
					class_name : 'gritter-success'
				});
				tableShippingMap.ajax.reload();
			}

		});
	};

}(jQuery));
$.fn.dataTableExt.oApi.fnReloadAjax = function(oSettings, sNewSource,
		fnCallback, bStandingRedraw) {
	// DataTables 1.10 compatibility - if 1.10 then versionCheck exists.
	// 1.10s API has ajax reloading built in, so we use those abilities
	// directly.
	if ($.fn.dataTable.versionCheck) {
		var api = new $.fn.dataTable.Api(oSettings);

		if (sNewSource) {
			api.ajax.url(sNewSource).load(fnCallback, !bStandingRedraw);
		} else {
			api.ajax.reload(fnCallback, !bStandingRedraw);
		}
		return;
	}

	if (sNewSource !== undefined && sNewSource !== null) {
		oSettings.sAjaxSource = sNewSource;
	}

	// Server-side processing should just call fnDraw
	if (oSettings.oFeatures.bServerSide) {
		this.fnDraw();
		return;
	}

	this.oApi._fnProcessingDisplay(oSettings, true);
	var that = this;
	var iStart = oSettings._iDisplayStart;
	var aData = [];

	this.oApi._fnServerParams(oSettings, aData);

	oSettings.fnServerData.call(oSettings.oInstance, oSettings.sAjaxSource,
			aData, function(json) {
				/* Clear the old information from the table */
				that.oApi._fnClearTable(oSettings);

				/* Got the data - add it to the table */
				var aData = (oSettings.sAjaxDataProp !== "") ? that.oApi
						._fnGetObjectDataFn(oSettings.sAjaxDataProp)(json)
						: json;

				for (var i = 0; i < aData.length; i++) {
					that.oApi._fnAddData(oSettings, aData[i]);
				}

				oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();

				that.fnDraw();

				if (bStandingRedraw === true) {
					oSettings._iDisplayStart = iStart;
					that.oApi._fnCalculateEnd(oSettings);
					that.fnDraw(false);
				}

				that.oApi._fnProcessingDisplay(oSettings, false);

				/* Callback user function - for event handlers etc */
				if (typeof fnCallback == 'function' && fnCallback !== null) {
					fnCallback(oSettings);
				}
			}, oSettings);
};

// USAGE: $("#form").serializefiles();
(function($) {
	$.fn.serializefiles = function() {
		var obj = $(this);
		/* ADD FILE TO PARAM AJAX */
		var formData = new FormData();
		$.each($(obj).find("input[type=file]"), function(i, tag) {
			$.each($(tag)[0].files, function(i, file) {
				formData.append(tag.name, file);
			});
		});
		var params = $(obj).serializeArray();
		$.each(params, function(i, val) {
			formData.append(val.name, val.value);
		});
		return formData;
	};
})(jQuery);
