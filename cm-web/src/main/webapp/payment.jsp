<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:openpage>
	<jsp:attribute name="maincontent">
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
					<h3>Billing Information</h3>
					<form class="form-horizontal" data-role="form"
							action="${pageContext.request.contextPath}/signup" method="post">
						<input type="hidden" name="cmd" value="payment">
						<div class="form-group">
							<label class="col-sm-12 control-label no-padding-right">
							<font color="red">${error}</font>
							</label>
						</div>
						<div class="form-group">
							<label for="username"
									class="col-sm-12 control-label no-padding-right"> </label>
						</div>
						<div class="form-group">
							<label for="name_on_card"
									class="col-sm-5 control-label no-padding-right">Name On
								Card</label>
							<div class="col-sm-7">
								<input type="text" value="" name="name_on_card" class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="credit_card"
									class="col-sm-5 control-label no-padding-right">Credit
								Card Number</label>
							<div class="col-sm-7">
								<input type="text" value="" maxlength="16" name="credit_card"
										class="required" />
							</div>
						</div>
						<div class="form-group">
							<label for="company_name"
									class="col-sm-5 control-label no-padding-right">Expiry</label>
							<div class="col-sm-7">
								<input type="text" value="" maxlength="2" placeholder="MM"
										name="credit_card_exp_mon" class="width-20" /> <input
										type="text" value="" maxlength="2" placeholder="YY"
										class="width-20" name="credit_card_exp_year" />
							</div>
						</div>
						<div class="form-group">
							<label for="phone"
									class="col-sm-5 control-label no-padding-right">Credit
								Card Type</label>
							<div class="col-sm-7">
								<select name="credit_card_type">
									<option value="VISA">VISA</option>
									<option value="MasterCard">MasterCard</option>
									<option value="American Express">American Express</option>
									<option value="Discover Card">Discover Card</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<div class="col-sm-7 pull-right">
								<input class="btn btn-success btn-sm pull-left" type="submit"
										value="SignUp" />
							</div>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
	</jsp:attribute>
</t:openpage>