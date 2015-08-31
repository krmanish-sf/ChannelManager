<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">
	<div class="main-content">
				<div id="breadcrumbs" class="breadcrumbs">
					<ul class="breadcrumb">
						<li><a href="index.jsp"><i class="icon-home home-icon"></i>Home</a>
						</li>
						<li class="active">Order Tracking</li>
					</ul>
				</div>
		<div class="page-content">
		<div class="page-header">
				<h1>
							Order Tracking<small> <i class="icon-double-angle-right"></i>
								Orders to Track
							</small>
						</h1>
					</div>
		<div class="row">
	<div class="col-sm-12">
                <h4 class="lighter pull-left"> <i
								class="icon-time orange"></i> Your Posted Orders </h4>
    </div>
	<div class="col-xs-12">
								<div class="space-2"></div>
								<div class="widget-main no-padding">
								<table id="table-order-tracking"
								class="table table-bordered table-striped  table-responsive dataTable">
											<thead class="thin-border-bottom">
												<tr role="row">
													<th><i
											class="icon-sort-by-order-alt icon-2x blue visible-xs"></i><span
											class="hidden-xs visible-sm">Order Id</span></th>
											<th>Order Status</th>
													<th class="hidden-xs">Order Date</th>
													<th><i class="icon-home icon-2x blue visible-xs"></i><span
											class="hidden-xs">Customer</span></th>
													<th class="hidden-sm hidden-xs">Channel
														Name</th>
													<th class="hidden-sm hidden-xs sorting"><span>Shipping</span></th>
													<th class="sorting"><i
											class="icon-usd icon-2x blue visible-xs"></i><span
											class="hidden-xs">Order Total</span></th>
								
								</thead>
									</table>
									</div>
								</div>
							</div>
						</div>
		</div>	
	<!-- /.page-content -->
	</jsp:attribute>
	<jsp:attribute name="pagejs">
	<!-- inline scripts related to this page -->
	<script type="text/javascript">
		var table_xy = null;
		var MY_SUPPLIERS = null;
		var STATUS = [ {
			"statusId" : 3,
			"statusValue" : "Failed"
		}, {
			"statusId" : 0,
			"statusValue" : "Unprocessed"
		}, {
			"statusId" : 2,
			"statusValue" : "Processed"
		}, {
			"statusId" : 5,
			"statusValue" : "Manually Processed"
		}, {
			"statusId" : 6,
			"statusValue" : "Canceled"
		}, {
			"statusId" : 7,
			"statusValue" : "Shipped"
		} ];

		jQuery(function($) {
			table_xy = $('#table-order-tracking')
					.DataTable(
							{
								"order" : [ [ 2, "desc" ] ],
								"processing" : true,
								"serverSide" : true,
								"sAjaxDataProp" : "data",
								"ajax" : function(data, callback, settings) {
									var d = $.CM.planify(data);
									$(this).CRUD({
										method : "POST",
										url : 'aggregators/orders/posted',
										cache : true,
										data : JSON.stringify(d),
										"message" : true,
										"success" : function(result) {
											callback(result);
										}
									});
								},
								"bDestroy" : true,
								"aoColumns" : [
										{
											"mData" : "storeOrderId"
										},
										{
											"mData" : function(order) {
												var text = '';
												for (var i = 0; i < order.oimOrderDetailses.length; i++) {
													var orderDetail = order.oimOrderDetailses[i];
													text += '<strong>'
															+ orderDetail.sku
															+ '</strong>';
													if (orderDetail.supplierOrderNumber)
														text += '&nbsp;<a style="cursor:pointer;" title="Click to refresh tracking" onclick="$.CM.trackOrder('
																+ orderDetail.detailId
																+ ');"><i class="icon-refresh"></i></a>';
													if (orderDetail.supplierOrderStatus) {
														text += '<br/><span id="orderStatus'+orderDetail.detailId+'">';
														text += orderDetail.supplierOrderStatus
																+ '</span>';
													}
													text += '<div class="space-2"></div>';
												}
												return text;
											},
											"bSortable" : false
										},
										{
											"mData" : function(order) {
												return "<span style='display:none'>"
														+ order.orderTm
														+ "</span>"
														+ new Date(
																order.orderTm)
																.toLocaleString();
											}
										},
										{
											"mData" : "deliveryName"
										},
										{
											"mData" : function(order) {
												return order.oimOrderBatches.oimChannels.channelName;
											}
										}, {
											"mData" : "shippingDetails"
										}, {
											"mData" : "orderTotalAmount"
										} ]
							});
			$('.addresspop').popover({
				container : 'body'
			});
			$('table th input:checkbox').on(
					'click',
					function() {
						var that = this;
						$(this).closest('table').find(
								'tr > td:first-child input:checkbox').each(
								function() {
									this.checked = that.checked;
									$(this).closest('tr').toggleClass(
											'selected');
								});

					});
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

				if (parseInt(off2.left) < parseInt(off1.left)
						+ parseInt(w1 / 2))
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

			for (var val = 0; val < STATUS.length; val++) {
				$("<option />", {
					value : STATUS[val].statusId,
					text : STATUS[val].statusValue
				}).appendTo('#order_status');
			}
		});
	</script>
</jsp:attribute>
</t:basepage>
