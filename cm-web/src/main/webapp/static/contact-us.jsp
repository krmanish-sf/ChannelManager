<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:openpage>
	<jsp:attribute name="maincontent">
		<div class="main-container-inner">
			
			<div class="container">
			
				<div class="row">
				<center>
						<table style="width: 70%">
					<tr>
							<td><center><img alt="" style="width: 75%;"
									src="${pageContext.request.contextPath}/static/images/channel-manager.png"></center></td>
						</tr>
					<tr>
						<td>
						<h3 style="color: #438EB9;">What is Channel Manager? </h3>
						<p>Channel Manager is an order automation & management tool provided by Inventory Source. It provides a 
						dashboard of the incoming orders from your multiple sales channels and allows you to automatically place them into your supplier's account.
						Orders are dynamically updated with shipment tracking codes, easily trackable in your order history, and quickly analyzed with the advanced reporting engine.</p>
						<br />
						<br />
						<h3 style="color: #438EB9;">How Much Does It Cost?</h3>
						<table border="1" style="width: 100%;">
						<tr style="background-color: #BCBCBC;">
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;"><b>Startup Plan</b></td>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;"><b>Growth Plan</b></td>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;"><b>Enterprise Plan</b></td>
						</tr>
						<tr>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;">&lt; 100 orders/month</td>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;">&lt; 1,000 orders/month</td>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;">&lt; 10,000 orders/month</td>
						</tr>
						<tr>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;"><b>$49/month</b></td>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;"><b>$99/month</b></td>
						<td style="text-align: center;padding-top: 4px;padding-bottom: 4px;"><b>$299/month</b></td>
						</tr>
						</table> 
						<br />
						</td>
					</tr>
					<tr>
					<td>
					<center>
					<table style="width: 70%;">
					<tr>
					<td>
					&nbsp;&nbsp;
					<%-- <img alt="" style="width: 75%;"
									src="${pageContext.request.contextPath}/static/images/support_phicon.png"> --%>
									<center><h2 style="width: auto;
background: url(images/support_phicon.png) no-repeat left top;
padding: 0 0 20px 0;
margin: 0;
color: #cc202a;
font-family: Arial, Helvetica, sans-serif;
line-height: normal;
font-size: 54px;
font-weight: normal;
text-shadow: 1px 1px 1px #333;">888-351-3497</h2></center>
					</td>
					</tr>
					<tr>
					<td>
					
					
					<div >
					<center><h2><b>Request a Demo Today!</b></h2></center>
					<form id="infoform"
											action="${pageContext.request.contextPath}/signup"
											method="post" class="form-horizontal" role="form">
												<input type="hidden" name="cmd" value="contactus">
												<div class="form-group">
													<label class="col-sm-9 pull-right">
														<font color="red">${message}</font>
													</label>
												</div>
												
												<div class="form-group">
													<label for="name"
													class="col-sm-3 control-label no-padding-right">
														Name </label>
													<div class="col-sm-9">
														<input type="text" class="col-xs-12 col-sm-8"
														id="form-field-1" name="name" required="required">
													</div>
												</div>
												<div class="form-group">
													<label for="email"
													class="col-sm-3 control-label no-padding-right">
														Email </label>
													<div class="col-sm-9">
														<input type="email" class="col-xs-10 col-sm-8" id="email"
														name="email" required="required">
													</div>
												</div>
												<div class="form-group">
													<label for="phone"
													class="col-sm-3 control-label no-padding-right">
														Phone </label>
													<div class="col-sm-9">
														<input type="tel" class="col-xs-10 col-sm-8" id="phone"
														name="phone" required="required">
													</div>
												</div>
												<div class="form-group">
													<label for="comments"
													class="col-sm-3 control-label no-padding-right">
														Comments/<br>Questions</label>
													<div class="col-sm-9">
														<textarea class="col-xs-10 col-sm-8" id="comments"
														rows="8" name="comments" required="required"></textarea>
													</div>
												</div>
												<div class="form-group">
													<div class="col-md-offset-3 col-md-6">
														<input type="submit" class="btn btn-info width-100"
														value="Submit" />
															
														
													</div>
												</div>
												<div class="form-group">
													<label class="col-sm-9 pull-right">
														<font color="red">All fields are mandatory</font>
													</label>
												</div>
											</form>
											</div>
											</td></tr>
											</table>
											</center>
					</td>
					</tr>
				</table>
					</center>
				</div>
			</div>
		</div>

	</jsp:attribute></t:openpage>
<script src="js/jquery.validate.min.js"></script>
<script type="text/javascript">
	function sendEmail() {
		$('#infoform').submit();
	}
</script>