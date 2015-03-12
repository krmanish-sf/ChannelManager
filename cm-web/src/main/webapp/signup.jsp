<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:openpage>
	<jsp:attribute name="maincontent">
	<div class="main-container-inner">
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
					<a href="${pageContext.request.contextPath}/login.jsp">Sign Up using
						existing Inventory Source account </a>
					<h3>Account Information</h3>
					<form class="form-horizontal" data-role="form"
							action="${pageContext.request.contextPath}/signup" method="post">
						<input type="hidden" name="cmd" value="signup">
						<div class="form-group">
							<label class="col-sm-12 control-label no-padding-right">
								<c:if test="${error!=null}">
								<font color="red"> ${error}</font>
								</c:if> 
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
								<input type="password" value="" name="password" class="required" />
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
</jsp:attribute>
</t:openpage>
