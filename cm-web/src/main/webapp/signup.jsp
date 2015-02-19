<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>Inventory Source Channel Manager</title>
<meta name="description" content="overview &amp; stats">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<!-- basic styles -->

<link href="static/css/bootstrap.css" rel="stylesheet">
<link rel="stylesheet" href="static/css/font-awesome.css">

<!--[if IE 7]>
		  <link rel="stylesheet" href="static/css/font-awesome-ie7.min.css" />
		<![endif]-->

<!-- page specific plugin styles -->

<!-- fonts -->

<link rel="stylesheet" href="static/css/ace-fonts.css">

<!-- ace styles -->

<link rel="stylesheet" href="static/css/ace.css">
<link rel="stylesheet" href="static/css/inventory.css">
<link rel="stylesheet" href="static/css/ace-rtl.css">
<link rel="stylesheet" href="static/css/ace-skins.css">

<!--[if lte IE 8]>
		  <link rel="stylesheet" href="static/css/ace-ie.min.css" />
		<![endif]-->

<!-- inline styles related to this page -->

<!-- ace settings handler -->

<script type="text/javascript" src="static/js/ace-extra.js"></script>

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
	<div class="main-container-inner">
		<a class="menu-toggler" id="menu-toggler" href="#"> <span
			class="menu-text"></span>
		</a>
		<div class="container">
			<div class="row">
				<div class="col-sm-8">
					<h2>Get Started With Channel Manager</h2>
					<p>To get started automating your sales and take your business
						to the next level, create an account below. If you already use the
						Inventory Source inventory automation services, you may login to
						Channel Manager with your existing Inventory Source login.</p>
					<ul>
						<li>Create your account or login with your InventorySource
							account below</li>
						<li>Request activation of your Channel Manager setup process</li>
						<li>Our account management team will contact you to
							personally set up your channels and provide additional
							information to activate your account and begin automating your
							orders</li>
					</ul>
					<h3>Pricing</h3>
					<strong>Setup</strong>: $500<br> <strong>Monthly</strong>: $99<br>
					<br> Includes personal account setup, training, and ongoing
					support for your Channel Manager service.
				</div>
				<div class="col-sm-4">
					<a href="<%=request.getContextPath()%>/login.jsp">Sign Up using
						existing Inventory Source account </a>
					<h3>Account Information</h3>
					<form class="form-horizontal" data-role="form"
						action="<%=request.getContextPath()%>/signup" method="post">
						<input type="hidden" name="cmd" value="signup">
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
								class="col-sm-12 control-label no-padding-right"> </label>
						</div>
						<div class="form-group">
							<label for="first_name"
								class="col-sm-5 control-label no-padding-right">First
								Name</label>
							<div class="col-sm-7">
								<input type="text" value="" name="first_name" class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="last_name"
								class="col-sm-5 control-label no-padding-right">Last
								Name</label>
							<div class="col-sm-7">
								<input type="text" value="" name="last_name" class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="company_name"
								class="col-sm-5 control-label no-padding-right">Company
								Name</label>
							<div class="col-sm-7">
								<input type="text" value="" name="company_name" class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="phone"
								class="col-sm-5 control-label no-padding-right">Phone</label>
							<div class="col-sm-7">
								<input type="text" value="" name="phone" class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="email"
								class="col-sm-5 control-label no-padding-right">Email</label>
							<div class="col-sm-7">
								<input type="text" value="" name="email" class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="password"
								class="col-sm-5 control-label no-padding-right">Password</label>
							<div class="col-sm-7">
								<input type="text" value="" name="password" class="required" />
							</div>
						</div>
						<div class="form-group">
							<div class="col-sm-7 pull-right">
								<input class="btn btn-success btn-sm pull-left" type="submit"
									value="Continue" />
							</div>
						</div>
					</form>
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
					<small class="navbar-brand  small14">&copy; 2003-2013
						Inventory Source. All Rights Reserved.</small><a href="#"
						id="btn-scroll-up" class="btn-scroll-up btn btn-sm btn-success">
						<i class="icon-double-angle-up icon-only bigger-110"></i>
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
						.write("<script src='static/js/uncompressed/jquery-2.0.3.js'>"
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
	<script src="static/js/bootstrap.js"></script>
	<script src="static/js/typeahead-bs2.js"></script>
	<!-- page specific plugin scripts -->
	<!--[if lte IE 8]>
		  <script src="static/js/excanvas.min.js"></script>
		<![endif]-->
	<script src="static/js/jquery-ui-1.10.3.custom.min.js"></script>
	<script src="static/js/jquery.ui.touch-punch.min.js"></script>
	<script src="static/js/jquery.slimscroll.min.js"></script>
	<script type="text/javascript" src="static/js/bootbox.min.js"></script>
	<!-- ace scripts -->
	<script src="static/js/ace-elements.js"></script>
	<script src="static/js/ace.js"></script>
	<script type="text/JavaScript">
		// prepare the form when the DOM is ready 
		$(document).ready(function() {
			$('#signupform').validate({
				errorPlacement : function(error, element) {
					var target = element.next().next();
					error.appendTo(target);
				}
			});
			$("div.new_method").find('input').removeAttr('disabled');
			$("div.is_method").find('input').attr('disabled', true);
			$(".typeselector").change(function() {
				var method = $(this).val();
				if (method == "new") {
					$("div.new_method").find('input').removeAttr('disabled');
					$("div.is_method").find('input').attr('disabled', true);
				} else {
					$("div.is_method").find('input').removeAttr('disabled');
					$("div.new_method").find('input').attr('disabled', true);
				}
			});
		});
	</script>
</body>
</html>

