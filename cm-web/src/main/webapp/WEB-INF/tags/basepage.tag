<%@tag import="com.inventorysource.cm.web.config.ApplicationProperties"%>
<%@tag import="org.springframework.context.annotation.Import"%>
<%@tag import="salesmachine.hibernatedb.Reps"%>
<%@tag language="java" trimDirectiveWhitespaces="true"
	description="Base content Page template" pageEncoding="UTF-8"%><%@taglib
	uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@taglib
	uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%><%@attribute
	name="maincontent" fragment="true"%><%@attribute name="pagejs"
	fragment="true"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html">
<meta name="description" content="overview &amp; stats">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<link rel="shortcut icon" href="static/favicon.ico" type="image/x-icon">
<link rel="icon" href="static/favicon.ico" type="image/x-icon">
<title>Inventory Source Channel Manager</title>
<!-- basic styles -->

<link rel="stylesheet" href="static/css/bootstrap.min.css" />
<link rel="stylesheet" href="static/css/font-awesome.min.css" />

<!--[if IE 7]>
		  <link rel="stylesheet" href="static/css/font-awesome-ie7.min.css" />
		<![endif]-->
<!-- page specific plugin styles -->
<!-- fonts -->
<link rel="stylesheet" href="static/css/ace-fonts.min.css" />
<!-- ace styles -->
<link rel="stylesheet" href="static/css/jquery.gritter.min.css" />
<link rel="stylesheet" href="static/css/ace.min.css" />
<link rel="stylesheet" href="static/css/inventory.min.css" />
<link rel="stylesheet" href="static/css/ace-rtl.min.css" />
<link rel="stylesheet" href="static/css/ace-skins.min.css" />
<link rel="stylesheet" href="static/css/jquery-ui-1.10.3.full.min.css" />
<!--[if lte IE 8]>
		  <link rel="stylesheet" href="static/css/ace-ie.min.css" />
		<![endif]-->
<!-- inline styles related to this page -->
<script type="text/javascript">
	var CM_SETTINGS = {
		rest_api_base_url : '${REST_URL}',
		repid : '${reps.getRepId()}'
	};
</script>

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->

<!--[if lt IE 9]>
		<script src="static/js/html5shiv.js"></script>
		<script src="static/js/respond.min.js"></script>
		<![endif]-->
<style type="text/css">
#wait-message {
	margin: 0 auto;
	width: 150px;
	display: none;
}
</style>

</head>
<body>
	<div style="position: fixed; width: 100%; z-index: 1041;">
		<div class="pull-center alert alert-warning" id="wait-message">
			<i class="icon-spinner icon-spin green bigger-125"></i>Please wait...
		</div>
	</div>
	<div class="modal fade" id="wait-modal" tabindex="-1" role="dialog"
		aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header alert alert-warning">
					<i class="icon-spinner icon-spin orange bigger-125"></i> Please
					wait...
				</div>
			</div>
		</div>
	</div>
	<div class="navbar navbar-default" id="navbar">
		<div class="navbar-container" id="navbar-container">
			<div class="navbar-header pull-left">
				<a href="#" class="navbar-brand"> <small> <img
						src="static/images/isbrandlogo.png" alt="logo"> <!--<i class="icon-leaf"></i>-->
						Channel Manager:<small style="font-size: 16px;"> Simplify
							Your Order Management, Simplify Your Success.</small>
				</small>
				</a>
				<!-- /.brand -->
			</div>
			<!-- /.navbar-header -->
			<div class="navbar-header pull-right" role="navigation">
				<ul class="nav ace-nav" id="header-nav">
					<li class="purple"><a data-toggle="dropdown"
						class="dropdown-toggle" href="#"> <i
							class="icon-bell-alt icon-animated-bell"></i> <span
							class="badge badge-important alert-count"></span>
					</a>
						<ul
							class="pull-right dropdown-navbar navbar-pink dropdown-menu dropdown-caret dropdown-close alert-details">
							<li class="dropdown-header"><i class="icon-warning-sign"></i>
								Errors</li>
						</ul></li>
					<li class="light-blue"><a data-toggle="dropdown" href="#"
						class="dropdown-toggle"> <!-- <img class="nav-user-photo"
								src="static/images/user.jpg" alt="Jason's Photo"> --> <span
							class="user-info info1"> <small>Welcome,</small> <c:out
									value="${sessionScope.reps.getFirstName()}" />
						</span> <i class="icon-caret-down"></i>
					</a>
						<ul
							class="user-menu pull-right dropdown-menu dropdown-yellow dropdown-caret dropdown-close">
							<!-- <li><a href="#"> <i class="icon-cog"></i> Settings
								</a></li>
								<li><a href="#"> <i class="icon-user"></i> Profile
								</a></li> 
								<li class="divider"></li>-->
							<li><a href="${pageContext.request.contextPath}/logout">
									<i class="icon-off"></i> Logout
							</a></li>
						</ul></li>
				</ul>
				<!-- /.ace-nav -->
			</div>
			<!-- /.navbar-header -->
			<div class="pull-right">
				<!-- <a data-toggle="modal" href="#learn-more"
					class="navbar-brand small14"><i class="icon-info"></i>&nbsp;Learn
					more</a> -->
				<a href="static/how-order-automation-works.jsp" target="_blank"
					class="navbar-brand small14 ">How it works</a> <a
					href="static/integrations.jsp" target="_blank"
					class="navbar-brand small14 ">Integrations</a> <a
					href="static/pricing.jsp" target="_blank"
					class="navbar-brand small14 ">Pricing</a> <a
					href="static/contact-us.jsp" target="_blank"
					class="navbar-brand small14 ">Contact us</a>
				<!-- <a
					href="static/ecommerce-testimonials.jsp" target="_blank"
					class="navbar-brand small14 ">Testimonials</a> -->
			</div>

		</div>
		<!-- /.container -->
	</div>
	<div class="main-container" id="main-container">
		<div class="main-container-inner">
			<a class="menu-toggler" id="menu-toggler" href="#"> <span
				class="menu-text"></span>
			</a>
			<div class="sidebar" id="sidebar">
				<div class="sidebar-shortcuts" id="sidebar-shortcuts">
					<div class="sidebar-shortcuts-large" id="sidebar-shortcuts-large">
						<button class="btn btn-yellow" data-rel="tooltip"
							data-original-title="Pull Orders (all channels)"
							onclick="javascript:$.CM.pullorder();">
							<i class="icon-circle-arrow-left"></i>
						</button>
						<a class="btn btn-success" data-rel="tooltip"
							href="processorder.jsp#unprocessed"
							data-original-title="Unprocessed Orders"> <i
							class="icon-cogs"></i>
						</a> <a class="btn btn-danger" data-rel="tooltip"
							href="processorder.jsp#unresolved"
							data-original-title="Unresolved Orders"> <i
							class="icon-warning-sign"></i>
						</a>
						<button class="btn btn-info" data-rel="tooltip"
							data-original-title="Process Orders (all channels/suppliers)"
							onclick="$.CM.processOrders();">
							<i class="icon-share"></i>
						</button>
					</div>
					<div class="sidebar-shortcuts-mini" id="sidebar-shortcuts-mini">
						<span class="btn btn-success"></span><span class="btn btn-info"></span>
						<span class="btn btn-warning"></span><span class="btn btn-danger"></span>
					</div>
				</div>
				<!-- #sidebar-shortcuts -->


				<ul id="navigation" class="nav nav-list">
					<li
						<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"index.jsp\")}"> class="active" </c:if>><a
						href="index.jsp"> <i class="icon-dashboard"></i> <span
							class="menu-text">Dashboard</span>
					</a></li>
					<li
						<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"importchannel.jsp\")}"> class="active" </c:if>>
						<a href="importchannel.jsp"> <i class="icon-random"></i> <span
							class="menu-text">Channels</span>
					</a>
					</li>
					<li
						<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"suppliers.jsp\")}"> class="active" </c:if>>
						<a href="suppliers.jsp"> <i class="icon-truck"></i> <span
							class="menu-text">Suppliers</span>
					</a>
					</li>
					<li
						<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"shipping-mappings.jsp\")}"> class="active" </c:if>>
						<a href="shipping-mappings.jsp"> <i class="icon-exchange"></i>
							<span class="menu-text">Shipping Mappings</span>
					</a>
					</li>
					<li
						<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"processorder.jsp\") || fn:containsIgnoreCase(pageContext.request.requestURI, \"orderhistory.jsp\")}"> class="open" </c:if>>
						<a class="dropdown-toggle" href="#"> <i
							class="icon-shopping-cart"></i> <span class="menu-text">Orders</span>
							<b class="arrow icon-angle-down"></b>
					</a>
						<ul class="submenu"
							<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"processorder.jsp\") || fn:containsIgnoreCase(pageContext.request.requestURI, \"order-tracking.jsp\")|| fn:containsIgnoreCase(pageContext.request.requestURI, \"orderhistory.jsp\")}"> style="display: block;" </c:if>>
							<li
								<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"processorder.jsp\")}"> class="active" </c:if>>
								<a href="processorder.jsp"> <i class="icon-share"></i>
									Process Orders
							</a>

							</li>
							<%-- <li
								<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"order-tracking.jsp\")}"> class="active" </c:if>>
								<a href="order-tracking.jsp"> <i class="icon-resize-small"></i>
									Track Orders
							</a>

							</li>--%>
							<li
								<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"orderhistory.jsp\")}"> class="active" </c:if>>
								<a href="orderhistory.jsp"> <i class="icon-time"></i> Order
									History
							</a>
							</li>
						</ul>
					</li>
					<li
						<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"reportmax.jsp\") || fn:containsIgnoreCase(pageContext.request.requestURI, \"reportmin.jsp\")}"> class="open" </c:if>>
						<a href="#" class="dropdown-toggle"> <i class="icon-lightbulb"></i>
							<span class="menu-text">Channel Intelligence</span> <b
							class="arrow icon-angle-down"></b>
					</a>
						<ul class="submenu"
							<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"reportmax.jsp\") || fn:containsIgnoreCase(pageContext.request.requestURI, \"reportmin.jsp\")}"> style="display: block;"</c:if>>
							<li
								<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"reportmax.jsp\")}"> class="active" </c:if>>
								<a href="reportmax.jsp"> <i class="icon-resize-small"></i>
									View All Reports
							</a>
							</li>
							<li
								<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"reportmin.jsp\")}"> class="active" </c:if>>
								<a href="reportmin.jsp"> <i class="icon-resize-small"></i>
									Generate Visual Report
							</a>
							</li>
						</ul>
					</li>
					<c:if
						test="${ ApplicationProperties.showReport() && reps.getKbadminAllowed()==1}">
						<li
							<c:if test="${fn:containsIgnoreCase(pageContext.request.requestURI, \"system-report.jsp\")}"> class="active" </c:if>>
							<a href="system-report.jsp"> <i class="icon-cog"></i> <span
								class="menu-text">System Report</span>
						</a>
						</li>
					</c:if>
					<!-- <li><a href="#l"> <i class="icon-cog"></i> <span
							class="menu-text">Support</span>
					</a></li>
					<li><a href="#" class="dropdown-toggle"> <i
							class="icon-tag"></i> <span class="menu-text">More Pages</span> <b
							class="arrow icon-angle-down"></b>
					</a>
						<ul class="submenu">
							<li><a href="profile.jsp"> <i
									class="icon-double-angle-right"></i> User Profile
							</a></li>
							<li><a href="inbox.jsp"> <i
									class="icon-double-angle-right"></i> Inbox
							</a></li>
							<li><a href="pricing.jsp"> <i
									class="icon-double-angle-right"></i> Pricing Tables
							</a></li>
							<li><a href="invoice.jsp"> <i
									class="icon-double-angle-right"></i> Invoice
							</a></li>
							<li><a href="timeline.jsp"> <i
									class="icon-double-angle-right"></i> Timeline
							</a></li>
							<li><a href="login.jsp"> <i
									class="icon-double-angle-right"></i> Login &amp; Register
							</a></li>
						</ul></li> -->
				</ul>

				<!-- /.nav-list -->

				<div class="sidebar-collapse" id="sidebar-collapse">
					<i class="icon-double-angle-left"
						data-icon1="icon-double-angle-left"
						data-icon2="icon-double-angle-right"></i>
				</div>
				<!-- <script type="text/javascript">
						try {
							ace.settings.check('sidebar', 'collapsed');
						} catch (e) {
						}
					</script> -->
			</div>
			<jsp:invoke fragment="maincontent" />
		</div>
	</div>
	<br>
	<div class="navbar navbar-bottom footer navbar-fixed-bottom">
		<div class="navbar-inner">
			<div class="navbar-container">
				<div class="navbar-header pull-left">
					<a href="static/about-us.jsp" target="_blank"
						class="navbar-brand small14 ">About us</a> <span
						class="navbar-brand small14">|</span> <a href="static/terms.jsp"
						target="_blank" class="navbar-brand small14 ">Terms of Use</a><span
						class="navbar-brand small14">|</span><a
						href="http://www.inventorysource.com"
						class="navbar-brand  small14">InventorySource LLC</a>
				</div>
				<div class="navbar-header pull-right">
					<small class="navbar-brand  small14">&copy; 2003-<%@ taglib
							uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%><jsp:useBean
							id="date" class="java.util.Date" /><fmt:formatDate
							value="${date}" pattern="yyyy" /> Inventory Source. All Rights
						Reserved.
					</small><a href="#" id="btn-scroll-up"
						class="btn-scroll-up btn btn-sm btn-success"> <i
						class="icon-double-angle-up icon-only bigger-110"></i>
					</a>
				</div>
			</div>
		</div>
	</div>
	<!-- basic scripts -->

	<!--[if !IE]> -->
	<div class="modal fade" id="learn-more" tabindex="-1" role="dialog"
		aria-hidden="true">
		<div id="mysupplieradddailog" class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title">Learn more</h4>
				</div>
				<div class="modal-body ">
					<div class="row">
						<div class="container">Inventory Source is currently in a
							Full Beta Release. We have selected our initial round of users
							for this Beta release, but if you would like more details on
							getting started with this order automation utility, please
							contact us at support@inventorysource.com for more details about
							service pricing and getting your business included as part of
							this Beta version before our production release scheduled for
							January 2015.</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		window.jQuery
				|| document
						.write("<script src='static/js/jquery-2.0.3.min.js'>"
								+ "<"+"/script>");
	</script>

	<!-- <![endif]-->
	<!--[if IE]>
<script type="text/javascript">
 window.jQuery || document.write("<script src='static/js/jquery-1.10.2.min.js'>"+"<"+"/script>");
</script>
<![endif]-->
	<script type="text/javascript">
		if ("ontouchend" in document)
			document
					.write("<script src='static/js/jquery.mobile.custom.min.js'>"
							+ "<"+"/script>");
	</script>
	<script src="static/js/bootstrap.min.js"></script>
	<script src="static/js/typeahead-bs2.min.js"></script>
	<!-- page specific plugin scripts -->
	<!--[if lte IE 8]>
		  <script src="static/js/excanvas.min.js"></script>
		<![endif]-->
	<script src="static/js/jquery.gritter.min.js"></script>
	<script src="static/js/jquery-ui-1.10.3.custom.min.js"></script>
	<script src="static/js/jquery.ui.touch-punch.min.js"></script>
	<script src="static/js/jquery.slimscroll.min.js"></script>
	<!-- <script src="static/js/jquery.easy-pie-chart.min.js"></script>
	<script src="static/js/jquery.sparkline.min.js"></script> -->
	<script src="static/js/flot/jquery.flot.min.js"></script>
	<!-- <script src="static/js/flot/jquery.flot.orderBars.js"></script> -->
	<script type="text/javascript"
		src="static/js/flot/jquery.flot.time.min.js"></script>
	<script src="static/js/flot/jquery.flot.axislabels.min.js"></script>
	<script type="text/javascript" src="static/js/jshashtable-3.0.min.js"></script>
	<script type="text/javascript"
		src="static/js/jquery.numberformatter-1.2.4.min.js"></script>
	<script src="static/js/flot/jquery.flot.pie.min.js"></script>
	<script src="static/js/flot/jquery.flot.resize.min.js"></script>
	<script src="static/js/flot/jquery.flot.categories.js"></script>
	<script type="text/javascript"
		src="//cdn.datatables.net/1.10.3/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript"
		src="static/js/jquery.dataTables.bootstrap.min.js"></script>
	<script type="text/javascript"
		src="static/js/date-time/bootstrap-datepicker.min.js"></script>
	<script type="text/javascript" src="static/js/bootbox.min.js"></script>
	<!-- <script type="text/javascript" src="static/js/dropzone.min.js"></script> -->
	<!-- <script type="text/javascript"
		src="static/js/jquery.maskedinput.min.js"></script> -->
	<script type="text/javascript" src="static/js/is.js"></script>

	<!-- ace scripts -->
	<script src="static/js/ace-elements.min.js"></script>
	<script src="static/js/ace.min.js"></script>
	<script src="static/js/ace-extra.min.js"></script>
	<script src="static/js/jquery.validate.min.js"></script>
	<jsp:invoke fragment="pagejs" />
	<script type="text/javascript">
		$(document).ready(function() {
			ace.settings.check('#navbar', 'fixed');
			ace.settings.check('#sidebar', 'fixed');
			$('.datepicker').datepicker();
			$('body').popover({
				container : 'body',
				selector : '.addresspop'
			});
			$(document).on('shown.bs.popover', '.addresspop', function() {
				setTimeout(function() {
					$('.addresspop').popover('hide');
				}, 10000);

			});
			$(document).on('hidden.bs.popover', '.addresspop', function() {
				$("div[class='popover fade bottom']").remove();
			});
			$('.dialogs,.comments').slimScroll({
				height : '300px'
			});
			$('[data-rel="tooltip"]').tooltip();
			getAlerts();
		});
		function getAlerts() {
			$(this)
					.CRUD(
							{
								url : 'aggregators/reports/notifications',
								method : 'GET',
								success : function(data) {
									var count = 0;
									$('#header-nav li ul.alert-details')
											.empty();
									$
											.each(
													data.supplierErrors,
													function(i) {
														var url;
														if (data.supplierErrors[i].errorcode == 3)
															url = 'orderhistory.jsp?order_status=3';
														else
															url = 'suppliers.jsp';
														count++;
														var cldiv = $('<div>')
																.addClass(
																		'clearfix');
														var li = $('<li>');
														var a = $('<a style="white-space: normal;text-align: left;" href="${pageContext.request.contextPath}/'+url+'">');
														var d2 = cldiv.clone();
														$('<span>')
																.addClass(
																		'pull-left')
																.html(
																		"Supplier: "
																				+ data.supplierErrors[i].supplier)
																.appendTo(cldiv);
														$('<span>')
																.addClass(
																		'pull-left')
																.html(
																		"Message: "
																				+ data.supplierErrors[i].errormsg)
																.appendTo(d2);
														a.append(cldiv);
														a.append(d2);
														li
																.append(a)
																.appendTo(
																		'#header-nav li ul.alert-details');
													});
									$('#header-nav li span.alert-count').html(
											count);
								}
							});
		}
	</script>
	<script>
		(function(i, s, o, g, r, a, m) {
			i['GoogleAnalyticsObject'] = r;
			i[r] = i[r] || function() {
				(i[r].q = i[r].q || []).push(arguments)
			}, i[r].l = 1 * new Date();
			a = s.createElement(o), m = s.getElementsByTagName(o)[0];
			a.async = 1;
			a.src = g;
			m.parentNode.insertBefore(a, m)
		})(window, document, 'script',
				'//www.google-analytics.com/analytics.js', 'ga');

		ga('create', 'UA-345327-11', 'inventorysource.com');
		ga('send', 'pageview');
	</script>
</body>
</html>
