var $tooltip = $(
		"<div class='tooltip top in'><div class='tooltip-inner'></div></div>")
		.hide().appendTo('body');
var previousPoint = null;
function $$(selector, context) {
	return jQuery(selector.replace(/(\[|\])/g, '\\$1'), context);
};
Date.prototype.getWeekRange = function() {
	var curr_date = this;
	var day = curr_date.getDay();
	var diff = curr_date.getDate() - day + (day == 0 ? -6 : 1); // 0 for sunday
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
	$("[data-" + data_attr + "]").each(
			function(e, i) {
				var $bound = jQuery(this);
				var attr_name = $bound.data(data_attr);
				// console.log($bound.attr('name') + " : " + attr_name);
				var i = attr_name.indexOf(':');
				if (i > 0) {
					var arr = attr_name.split(":");
					if (eval('typeof ' + arr[0]) === 'function') {
						// DO the recursive object binding
						try {
							attr_name = eval(arr[0] + '(dataObject,"' + arr[1]
									+ '")');
						} catch (e) {
							console.log("Can't evaluate " + data_attr + " : "
									+ attr_name + " on element"
									+ $bound.attr('name'));

						}
					}
				}
				var val = '';
				try {
					val = eval('dataObject.' + attr_name);
				} catch (e) {
					console.log('Error in evaluating expression ' + attr_name);
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
		// debugger;
		var attr_name = prop_name;
		// console.log($bound.attr('name') + " : " + attr_name);
		var i = attr_name.indexOf(':');
		if (i > 0) {
			var arr = attr_name.split(":");
			if (eval('typeof ' + arr[0]) === 'function') {
				// DO the recursive object binding
				try {
					attr_name = eval(arr[0] + '(dataObject,"' + arr[1] + '")');
				} catch (e) {
					console.log("Can't evaluate " + prop_name + " : "
							+ attr_name);

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
	console.log(retVal);
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
(function($) {
	$("select.select-time-range").each(function(i, e) {
		var widget = $(this).parent().parent().parent();
		widget.find('.start').val(new Date().getMonthRange().sStartDate);
		widget.find('.end').val(new Date().getMonthRange().sEndDate);
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
											new Date("2000-01-01")
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
			series : {
				pie : {
					show : true,
					tilt : 0.8,
					highlight : {
						opacity : 0.25
					},
					stroke : {
						color : '#fff',
						width : 2
					},
					startAngle : 2
				}
			},
			legend : {
				show : true,
				position : "ne",
				labelBoxBorderColor : null,
				margin : [ 0, 0 ]
			},
			grid : {
				hoverable : true,
				clickable : true
			},
			colors : [ "green", "blue", "yellow", "purple" ]
		};
		if (typeof placeholder == 'string')
			placeholder = $('#' + placeholder);
		var widget = placeholder.parent().parent().parent();
		if (typeof data == 'string') {
			$(this).CRUD({
				url : data,
				method : "POST",
				data : JSON.stringify({
					startDate : Date.parse(widget.find('.start').val()),
					endDate : Date.parse(widget.find('.end').val())
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
			plotInternal(data, options);
		}
		function plotInternal(data, options) {
			if (!data || !data.length) {
				placeholder.text('No Data to draw.');
				return;
			} else {
				placeholder.empty().css({
					'width' : '90%',
					'min-height' : '210px'
				});
				$.plot(placeholder, data, options);
			}
		}
		placeholder.on('plothover', function(event, pos, item) {
			if (item) {
				if (previousPoint != item.seriesIndex) {
					previousPoint = item.seriesIndex;
					var tip = item.series['label'] + " : "
							+ item.series['percent'].toFixed(2) + '%';
					$tooltip.show().children(0).text(tip);
				}
				$tooltip.css({
					top : pos.pageY + 10,
					left : pos.pageX + 10
				});
			} else {
				$tooltip.hide();
				previousPoint = null;
			}
		});
	};
	$.fn.drawBarChart = function(placeholder, data, data_formatter) {
		var options = {
			hoverable : true,
			shadowSize : 0,
			series : {
				lines : {
					show : true
				},
				points : {
					show : true
				}
			},
			xaxes : [ {
				mode : "time",
				timeformat : "%m/%d",
				tickPadding : 0,
				// tickSize : 'auto',
				/*
				 * tickFormatter : function(val, axis) { var d = new Date(val);
				 * return (d.getUTCMonth() + 1) + "/" + d.getUTCDate(); },
				 */
				color : "black",
				axisLabel : "Date(mm/dd)",
				position : "bottom",
				axisLabelUseCanvas : false,
				axisLabelFontSizePixels : 12,
				axisLabelFontFamily : 'Verdana, Arial',
				axisLabelPadding : 10
			} ],
			yaxes : [ {
				axisLabel : "Total sale(USD)",
				min : 0,
				axisLabelUseCanvas : true,
				axisLabelFontSizePixels : 12,
				axisLabelFontFamily : 'Verdana, Arial',
				axisLabelPadding : 3,
				tickFormatter : function(v, axis) {
					return $.formatNumber(v, {
						format : "#,###",
						locale : "us"
					});
				}
			} ],
			grid : {
				backgroundColor : {
					colors : [ "#fff", "#fff" ]
				},
				borderWidth : 1,
				borderColor : '#555'
			}
		};

		if (typeof placeholder == 'string')
			placeholder = $('#' + placeholder);
		var widget = placeholder.parent().parent();
		if (typeof data == 'string') {
			$(this).CRUD({
				url : data,
				method : "POST",
				data : JSON.stringify({
					startDate : Date.parse(widget.find('.start').val()),
					endDate : Date.parse(widget.find('.end').val())
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
			if (typeof data_formatter === 'string') {
				var formatted_data = [];
				eval(data_formatter + '(data, formatted_data)');
				data = formatted_data;
			}
			plotInternal(data, options);
		}
		function plotInternal(data, options) {
			if (!data || !data.length) {
				placeholder.text('No Data to draw.');
				return;
			} else {
				placeholder.empty().css({
					'width' : '90%',
					'min-height' : '150px'
				});
				$.plot(placeholder, [ {
					label : "Total sales",
					data : data
				} ], options);
			}
			$(document).on(
					'plothover',
					placeholder,
					function(event, pos, item) {
						// debugger;
						if (item) {
							if (previousPoint != item.seriesIndex) {
								previousPoint = item.seriesIndex;
								var tip = item.series['label'] + " : "
										+ item.series['percent'].toFixed(2)
										+ '%';
								console.log(tip);
								$tooltip.show().children(0).text(tip);
							}
							$tooltip.css({
								top : pos.pageY + 10,
								left : pos.pageX + 10
							});
						} else {
							$tooltip.hide();
							previousPoint = null;
						}
					});
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
			var channel = tableimportchannel.fnGetData(e[0]);
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
	$.CM.updateOrderSummary = function() {
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
									$(this).drawBarChart('sales-charts',
											chartData);
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
									$(
											'#unprocessedCount span.infobox-data-number')
											.html(
													data.OrderSummaryData.unprocessedCount);
									$(
											'#unprocessedValue span.infobox-data-number')
											.html(
													'$'
															+ data.OrderSummaryData.unprocessedAmount
																	.toFixed(2));
									$(
											'#unresolvedCount span.infobox-data-number')
											.html(
													data.OrderSummaryData.unresolvedCount);
									$(
											'#unresolvedValue span.infobox-data-number')
											.html(
													'$'
															+ data.OrderSummaryData.unresolvedAmount
																	.toFixed(2));
									$('strong.unresolvedcount')
											.html(
													data.OrderSummaryData.unresolvedCount);
								} catch (e) {
									console.log('Report box not present.');
								}
							}
						});
	};
	$.CM.viewShippingMap = function(e) {
		var channel = tableimportchannel.fnGetData(e[0]);
		$('#tableShippingMap').DataTable(
				{
					"bPaginate" : false,
					"bLengthChange" : false,
					"sAjaxSource" : "/aggregators/channels/shipping/"
							+ channel.oimSupportedChannels.supportedChannelId,
					"fnServerData" : function(sSource, aoData, fnCallback,
							oSettings) {
						oSettings.jqXHR = $(this).CRUD({
							type : "GET",
							url : sSource,
							data : aoData,
							cache : false,
							success : fnCallback
						});
					},
					"sAjaxDataProp" : '',
					"aoColumns" : [ {
						"mData" : "shippingRegEx"
					}, {
						"mData" : "oimShippingCarrier.name"
					}, {
						"mData" : "shippingMethod.name"
					} ]
				});
	};
	$.CM.viewSupplierShippingMap = function(e, tableId) {
		// debugger;
		var supplierId;
		if (typeof e == 'number')
			supplierId = e;
		else {
			var vendor_supplier = table_vendorSuppliers.fnGetData(e[0]);
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
		// debugger;
		$('#editshippingmethods').modal('show');
		var table = $($(tr).closest('table')).DataTable();
		var row = table.row(tr);
		var d = row.data();
		$('#editshippingmethods h4.modal-title').text(
				d.name + " Shipping Mapping");
		$('#tableShippingMap')
				.DataTable(
						{
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
				table_xy.fnReloadAjax();
				getAlerts();
			},
			error : function(a, b, c) {
				$.gritter.add({
					title : 'Order Processing',
					text : 'Order Processing Failed.',
					class_name : 'gritter-error'
				});
				table_xy.fnReloadAjax();
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
				.dataTable(
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
											for (var i = 0; i < channel.oimChannelAccessDetailses.length; i++) {
												if (channel.oimChannelAccessDetailses[i].oimChannelAccessFields.fieldId == 1) {
													if (channel.oimChannelAccessDetailses[i].detailFieldValue == null)
														channel.oimChannelAccessDetailses[i].detailFieldValue = "N/A";
													return '<a class="btn btn-default icon-info-sign btn-xs visible-xs addresspop" data-toggle="popover" data-container="body"  data-placement="bottom" data-content="'
															+ channel.oimChannelAccessDetailses[i].detailFieldValue
															+ '" data-original-title="Url"></a><div class="hidden-xs">'
															+ channel.oimChannelAccessDetailses[i].detailFieldValue
															+ '</div>';
												}
											}
										}
									},
									{
										"mData" : "oimSupportedChannels.channelName"
									},
									{
										"bSortable" : false,
										"sWidth" : "100px",
										"mData" : function(channel) {
											var toolTip = "Pull Order";
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
