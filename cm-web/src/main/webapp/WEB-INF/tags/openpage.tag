<%@tag language="java" trimDirectiveWhitespaces="true"
	description="Channel Manager content Page template"
	pageEncoding="UTF-8"%><%@taglib uri="http://java.sun.com/jsp/jstl/core"
	prefix="c"%><%@taglib uri="http://java.sun.com/jsp/jstl/functions"
	prefix="fn"%><%@attribute name="maincontent" fragment="true"%><!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>Inventory Source Channel Manager</title>
<meta name="description" content="overview &amp; stats">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<!-- basic styles -->

<link href="${pageContext.request.contextPath}/static/css/bootstrap.css"
	rel="stylesheet">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/font-awesome.min.css">

<!--[if IE 7]>
		  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/font-awesome-ie7.min.css" />
		<![endif]-->

<!-- page specific plugin styles -->

<!-- fonts -->

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/ace-fonts.min.css">

<!-- ace styles -->

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/ace.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/inventory.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/ace-rtl.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/ace-skins.min.css">

<!--[if lte IE 8]>
		  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/ace-ie.min.css" />
		<![endif]-->

<!-- inline styles related to this page -->

<!-- ace settings handler -->

<script type="text/javascript"
	src="${pageContext.request.contextPath}/static/js/ace-extra.min.js"></script>

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->

<!--[if lt IE 9]>
		<script src="${pageContext.request.contextPath}/static/js/html5shiv.js"></script>
		<script src="${pageContext.request.contextPath}/static/js/respond.min.js"></script>
		<![endif]-->
</head>
<body>
	<div class="navbar navbar-default" id="navbar">
		<div class="navbar-container" id="navbar-container">
			<div class="navbar-header pull-left">
				<a href="${pageContext.request.contextPath}" class="navbar-brand"> <small> <img
						src="${pageContext.request.contextPath}/static/images/isbrandlogo.png"
						alt="logo"> Inventory
						Source
				</small>
				</a>
			</div>
			<div class="pull-right">
				<a data-toggle="modal" href="#learn-more"
					class="navbar-brand small14"><i class="icon-info"></i>&nbsp;Learn
					more</a> <a
					href="${pageContext.request.contextPath}/static/about-us.jsp"
					class="navbar-brand small14 ">About us</a> <a
					href="${pageContext.request.contextPath}/static/how-order-automation-works.jsp"
					class="navbar-brand small14 ">How it works</a> <a
					href="${pageContext.request.contextPath}/static/contact-us.jsp"
					class="navbar-brand small14 ">Contact us</a> <a
					href="${pageContext.request.contextPath}/static/ecommerce-testimonials.jsp"
					class="navbar-brand small14 ">Testimonials</a>
			</div>
		</div>
		<!-- /.container -->
	</div>
	<div class="main-container" id="main-container">
		<jsp:invoke fragment="maincontent" />
	</div>
	<div class="navbar navbar-bottom footer navbar-fixed-bottom">
		<div class="navbar-inner">
			<div class="navbar-container">
				<div class="navbar-header pull-left">
					<a href="${pageContext.request.contextPath}/static/terms.jsp"
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
	<!-- basic scripts -->

	<!--[if !IE]> -->

	<script type="text/javascript">
		window.jQuery
				|| document
						.write("<script src='${pageContext.request.contextPath}/static/js/jquery-2.0.3.min.js'>"
								+ "<"+"/script>");
	</script>

	<!-- <![endif]-->
	<!--[if IE]>
<script type="text/javascript">
 window.jQuery || document.write("<script src='${pageContext.request.contextPath}/static/js/jquery-1.10.2.min.js'>"+"<"+"/script>");
</script>
<![endif]-->
	<script type="text/javascript">
		if ("ontouchend" in document)
			document
					.write("<script src='${pageContext.request.contextPath}/static/js/jquery.mobile.custom.min.js'>"
							+ "<"+"/script>");
	</script>
	<script
		src="${pageContext.request.contextPath}/static/js/bootstrap.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/static/js/typeahead-bs2.min.js"></script>
	<!-- page specific plugin scripts -->
	<!--[if lte IE 8]>
		  <script src="${pageContext.request.contextPath}/static/js/excanvas.min.js"></script>
		<![endif]-->
	<script
		src="${pageContext.request.contextPath}/static/js/jquery-ui-1.10.3.custom.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/static/js/jquery.ui.touch-punch.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/static/js/jquery.slimscroll.min.js"></script>
	<script type="text/javascript"
		src="${pageContext.request.contextPath}/static/js/bootbox.min.js"></script>
	<!-- ace scripts -->
	<script
		src="${pageContext.request.contextPath}/static/js/ace-elements.min.js"></script>
	<script src="${pageContext.request.contextPath}/static/js/ace.min.js"></script>
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
