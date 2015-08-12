<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<t:openpage>
	<jsp:attribute name="maincontent">
		<div class="main-container-inner">
			<div class="container">
				<div class="row">
					<div class="col-sm-8">
						<h2>Channel Manager - Your Multi-Channel Order Management Solution</h2>
						<h3>A Big Solution For Small Business</h3>
						<p>Small and medium size online sellers still have big needs when it comes to their channel solutions. That is why we designed Channel Manager as a way to give even the smallest online seller an empowering service against the competitive eCommerce industry.</p>
						<strong>What We Provide</strong>
						<ul>
							<li><strong>Quick Launch-</strong> Managed on-boarding and training to ensure your success.</li>
							<li><strong>Pull Orders In-</strong> Automatically pull yours orders from multiple store platforms and marketplaces into your centralized order hub.</li>
							<li><strong>Manage Orders-</strong> Monitor your order and supply-chain data, and easily adjust for any processing issues from a centralized control panel.</li>
							<li><strong>Post Orders Out-</strong> Automatically submit single orders, or submit in bulk, directly to your account at your distributors in the exact format they need.</li>
							<li><strong>Update Customers With Tracking-</strong> Capture your shipping and tracking data from your suppliers and send them back into your sales channel for your customers to see the order progress and shipping tracking codes.</li>
							<li><strong>Grow Sales &amp; Achieve Success-</strong> Track your order data to learn about the most profitable sales channels and product types so you can grow your business and your profits</li>
						</ul>
						
						<p>Save time and avoid the headaches with an simple automated solution for order review in from multiple sales channels and order posting out to multiple wholesale dropship distributors all from a centralized dashboard and service.</p>
						<p>Get priority order fulfillment by being able to process more orders in less time and avoid out of stock hassles  When you simplify your order management you simplify your success with InventorySource.com's Channel Manager order automation utility.</p>
						</div>
						
					<c:if test="${reps==null }">
					<div class="col-sm-4 login-form">
						<br>
						<div class="row">
						<div class="col-sm-12"
									style="background: #438eb9 none repeat scroll 0 0; border: 1px solid #369dc8; box-shadow: 1px 5px 5px 1px #666666; color: white;">
						<h3>Order Management Members Area</h3>
						<form class="form-horizontal" data-role="form"
										action="${pageContext.request.contextPath}/login"
										method="post">
							<input type="hidden" name="done" value="${param.done}" />
							<div class="form-group">
							<label class="col-sm-12 control-label no-padding-right">
								<font color="red">${error}</font>
							</label>
							</div>
							<div class="form-group">
								<label for="username"
												class="col-sm-5 control-label no-padding-right">User
									Name </label>
								<div class="col-sm-7">
									<input type="text" name="username" placeholder="Email" />
								</div>
							</div>
							<div class="form-group">
								<label for="password"
												class="col-sm-5 control-label no-padding-right">Password
								</label>
								<div class="col-sm-7">
									<input type="password" name="password" placeholder="Password" />
								</div>
							</div>
							<div class="form-group">
								<div class="col-sm-7 pull-right">
									<input class="btn btn-success btn-sm pull-left width-90"
													type="submit" value="Login" />
								</div>
							</div>
							<div class="form-group">
								<div class="col-sm-7 pull-right">
									 <a href="${pageContext.request.contextPath}/signup"
													style="color: white;">Activate A New Account?</a>
										<br><a href="javascript:;"
													onclick="$('.login-form').hide();$('.forgot-password').show();"
													style="color: white;">Forgot password?</a>
								</div>
							</div>
						</form>
						</div>
						</div>
					</div>
					<div class="col-sm-4 forgot-password" style="display: none;">
						<h2>Forgot Password</h2>
						<form class="form-horizontal" data-role="form"
								action="${pageContext.request.contextPath}/signup" method="post">
								<input type="hidden" name="cmd" value="resend" />
							<div class="form-group">
								<label for="email"
										class="col-sm-5 control-label no-padding-right">Email</label>
								<div class="col-sm-7">
									<input type="text" name="email" placeholder="Email" />
								</div>
							</div>
							<div class="form-group">
								<div class="col-sm-7 pull-right">
									<input class="btn btn-success btn-sm pull-left width-90"
											type="submit" value="Resend Password" />
								</div>
								<div class="col-sm-7 pull-right">
										<a href="javascript:;"
											onclick="$('.login-form').show();$('.forgot-password').hide();">Go to Login</a>
								</div>
							</div>
						</form>
					</div>
					</c:if>
					<c:if test="${reps!=null }">
					<div class="col-sm-4 forgot-password">
						<h3>You are already logged in as...</h3>
							<input type="hidden" name="cmd" value="resend" />
							<div class="form-group">
							<label for="email" class="col-sm-7 control-label pull-left">${reps.getFirstName()} ${reps.getLastName()} 
							<a href="${pageContext.request.contextPath}/logout">Logout</a>
								</label>
							</div>
							<div class="form-group">
							<label for="email" class="col-sm-7 control-label pull-left">${reps.getLogin()}</label>
							</div>
							<div class="form-group">
								<div class="col-sm-7 pull-left">
										<a href="${pageContext.request.contextPath}/index.jsp">Go to Dashboard</a>
								</div>
							</div>
					</div>
					</c:if>
					<div class="col-sm-4">
					<br>
					<div class="row">
						<img alt="Happy Customer"
								src="static/images/large_article_im1951_happy_woman_in_warehouse.jpg"
								style="width: 100%;">
					</div>
					</div>
				</div>
			</div>
		</div>
</jsp:attribute>
</t:openpage>