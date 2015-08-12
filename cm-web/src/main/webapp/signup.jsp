<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:openpage>
	<jsp:attribute name="maincontent">
	<div class="main-container-inner">
		<div class="container">
			<div class="row">
				<div class="col-sm-8">
					<h2>Getting Started With Channel Manager</h2>
					<p>InventorySource.com's Channel Manager Utility will help you "Simplify your order management, and simplify your success."</p>
					<p>
							<img alt="" src="static/images/19.jpg" style="width: 90%;">
					</p>
					<p>The Channel Manager service is currently offered by invitation only.  However, if you are ready to get started with an order management service, just fill in the form on the right and our team will review your details and contact you shortly about the options for your order integration approach.</p>
					<ul>
						<li>Enter your contact details.  If you already have an InventorySource.com provide your email ID for that account.</li>
						<li>In the "Wholesale Suppliers" field, list any dropship distributors where you need order management integrated.</li>
						<li>In the "Sales Channels" field, include the name of your website cart platform (i.e., Bigcommerce, 3dCart, Zen Cart, etc.) and/or any marketplaces (i.e., Amazon, eBay, etc) where you currently get orders.</li>
						<li>Our account management team will contact you to personally with the steps needed to sync your product sources and sales channels, as well as more information on our Channel Manager service.</li>
					</ul>
				</div>
				<br>
				<div class="col-sm-4"
						style="background: #438eb9 none repeat scroll 0 0; border: 1px solid #369dc8; box-shadow: 1px 5px 5px 1px #666666; color: white;">
					<%-- <a href="${pageContext.request.contextPath}/login.jsp">Sign Up using
						existing Inventory Source account </a> --%>
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
						<!-- <div class="form-group">
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
						</div>-->
						<div class="form-group">
							<label for="email"
									class="col-sm-5 control-label no-padding-right">Email</label>
							<div class="col-sm-7">
								<input type="text" value="" name="email" class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="suppliers"
									class="col-sm-5 control-label no-padding-right">Wholesale Suppliers</label>
							<div class="col-sm-7">
								<textarea name="suppliers" class="required"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label for="sales-changes"
									class="col-sm-5 control-label no-padding-right">Sales Changes</label>
							<div class="col-sm-7">
								<textarea name="sales-changes" class="required"></textarea>
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
