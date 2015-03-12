<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<t:openpage>
	<jsp:attribute name="maincontent">
		<div class="main-container-inner">
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
					<c:if test="${reps==null }">
					<div class="col-sm-4 login-form">
						<h2>Login</h2>
						<form class="form-horizontal" data-role="form"
								action="${pageContext.request.contextPath}/login" method="post">
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
									 <a href="${pageContext.request.contextPath}/signup">New User? Sign up</a>
										<br><a href="javascript:;"
											onclick="$('.login-form').hide();$('.forgot-password').show();">Forgot password?</a>
								</div>
							</div>
						</form>
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
				</div>
			</div>
		</div>
</jsp:attribute>
</t:openpage>