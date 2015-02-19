<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>Inventory Source Channel Manager</title>
<meta name="description" content="overview &amp; stats">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<!-- basic styles -->

<link href="static/css/bootstrap.css" rel="stylesheet">
<link rel="stylesheet" href="static/css/font-awesome.min.css">

<!--[if IE 7]>
		  <link rel="stylesheet" href="static/css/font-awesome-ie7.min.css" />
		<![endif]-->

<!-- page specific plugin styles -->

<!-- fonts -->

<link rel="stylesheet" href="static/css/ace-fonts.min.css">

<!-- ace styles -->

<link rel="stylesheet" href="static/css/ace.min.css">
<link rel="stylesheet" href="static/css/inventory.min.css">
<link rel="stylesheet" href="static/css/ace-rtl.min.css">
<link rel="stylesheet" href="static/css/ace-skins.min.css">

<!--[if lte IE 8]>
		  <link rel="stylesheet" href="static/css/ace-ie.min.css" />
		<![endif]-->

<!-- inline styles related to this page -->

<!-- ace settings handler -->

<script type="text/javascript" src="static/js/ace-extra.min.js"></script>

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->

<!--[if lt IE 9]>
		<script src="static/js/html5shiv.js"></script>
		<script src="static/js/respond.min.js"></script>
		<![endif]-->
</head>
<body>
	<div class="navbar navbar-default" id="navbar">
		<!-- <script type="text/javascript">
			try {
				ace.settings.check('navbar', 'fixed');
			} catch (e) {
			}
		</script> -->
		<div class="navbar-container" id="navbar-container">
			<div class="navbar-header pull-left">
				<a href="#" class="navbar-brand"> <small> <img
						src="static/images/isbrandlogo.png" alt="logo"> <!--<i class="icon-leaf"></i>-->
						Inventory Source
				</small>
				</a>
				<!-- /.brand -->
			</div>
			<!-- /.navbar-header -->
		</div>
		<!-- /.container -->
	</div>
	<div class="main-container" id="main-container">
		<!-- <script type="text/javascript">
			try {
				ace.settings.check('main-container', 'fixed');
			} catch (e) {
			}
		</script> -->
		<div class="main-container-inner">
			<a class="menu-toggler" id="menu-toggler" href="#"> <span
				class="menu-text"></span>
			</a>
			<div class="container">
				<div class="row">
					<div class="col-sm-8">
						<h2>Channel Manager - Your dropship order management
							solution.</h2>
						<p>Welcome to Chanel Manager, developed exclusively by the
							team at InventorySource.com. Inventory Source is the leader in
							Drop Ship Inventory Management Solutions, giving you easy to use
							automated controls to sell more and work less while you run your
							eCommerce business. At Inventory Source, we understand the
							challenges that resellers face, and provide services and
							solutions to make selling a simpler experience. Since 2003,
							Inventory Source has provided data integration services between
							wholesale distributors and eCommerce sales channels used by our
							members, and we have provided integration solutions for all major
							website platforms and marketplaces.</p>
						<h3 class="hdrcolor">Are you looking for a better way to
							manage your sales?</h3>
						<p>Streamline your operations while reducing the costs and
							time spent managing your eCommerce sales and order processing.
							Utilize a control panel and managed service designed and priced
							for small-to-medium businesses like yours. The Inventory Source
							Channel Manager solution is exactly what your business needs.</p>
						<ul>
							<li>Developed by Inventory Source which has helped thousands
								of small-to-medium size Web merchants like you for over 10
								years.</li>
							<li>Ideal for multi-channel retailers. Combine orders from
								sale platforms out to supplier sources with shared inventory and
								customer lists while retaining the branding of your different
								sites.</li>
							<li>Fast implementation, superior support and responsive
								developers.</li>
						</ul>
						<h3 class="hdrcolor">Effective</h3>
						<p>A solution like Inventory Source's Channel Manager is the
							most effective and scalable way to manage and process your
							orders, while getting the most value for your money. Channel
							Manager is used by resellers from some of the larger and most
							popular wholesale distributor programs across all major store
							platforms and marketplaces as a vital link in their supply chain
							management needs.</p>

						<h3 class="hdrcolor">Easy</h3>
						<p>We have custom integrations with a variety of suppliers and
							sales channels to help you process your orders at the click of a
							button. Inventory Source's Channel Manager is designed with
							simplicity in mind, giving you a dynamic and scalable order
							management solution with the core features you need, easy to edit
							controls, and order processing at the push of a button.</p>
					</div>
					<div class="col-sm-4">
						<h2>Login</h2>
						<form class="form-horizontal" data-role="form"
							action="<%=request.getContextPath()%>/login" method="post">
							<div class="form-group">
								<label class="col-sm-12 control-label no-padding-right">
									<%
										if (request.getAttribute("error") != null) {
									%><font color="red"> <%=request.getAttribute("error")%></font>
									<%
										}
									%>
								</label>
							</div>
							<div class="form-group">
								<label for="username"
									class="col-sm-5 control-label no-padding-right">User
									Name </label>
								<div class="col-sm-7">
									<input type="text" name="username" />
								</div>
							</div>
							<div class="form-group">
								<label for="password"
									class="col-sm-5 control-label no-padding-right">Password
								</label>
								<div class="col-sm-7">
									<input type="password" name="password" />
								</div>
							</div>
							<div class="form-group">
								<div class="col-sm-7 pull-right">
									<input class="btn btn-success btn-sm pull-left" type="submit"
										value="Login" />New User? <a
										href="<%=request.getContextPath()%>/signup.jsp">Signup</a>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>

	<div class="navbar navbar-default">
		<div class="navbar-inner">
			<div class="navbar-container">
				<div class="navbar-header pull-left">
					<a href="#" class="navbar-brand small14 ">Terms of Use</a><span
						class="navbar-brand small14">|</span><a href="#"
						class="navbar-brand  small14">support@inventorysource.com</a>
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
	<script src="static/js/jquery-ui-1.10.3.custom.min.js"></script>
	<script src="static/js/jquery.ui.touch-punch.min.js"></script>
	<script src="static/js/jquery.slimscroll.min.js"></script>
	<script type="text/javascript" src="static/js/bootbox.min.js"></script>
	<!-- ace scripts -->
	<script src="static/js/ace-elements.min.js"></script>
	<script src="static/js/ace.min.js"></script>
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
