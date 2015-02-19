<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:openpage>
	<jsp:attribute name="maincontent">
		<div class="main-container-inner">
			<div class="page-header">
				<h1>
					Learn more about Channel Manager<small><i
						class="ace-icon fa fa-angle-double-right"></i> </small>
				</h1>
			</div>
			<div class="container">
				<div class="row">
					<div class="col-sm-6">
						<div class="widget-box">
							<div class="widget-header widget-header-flat">
								<h4 id="analyze" class="widget-title">
									<i class="fa-envelope"></i> Contact us
								</h4>
							</div>
							<div class="widget-body">
								<div class="widget-main">
									<div class="row">
										<div class="col-sm-12">
											<p>To speak with us or get more information about the
												Channel Manager system and what it can do for you, or to ask
												about special integrations or questions you have, please use
												the form below.</p>
											<hr>
											<form id="infoform" method="POST" class="form-horizontal"
												role="form">
												<input type="hidden" name="cmd" value="moreinfo">
												<div class="form-group">
													<label for="name"
														class="col-sm-3 control-label no-padding-right">
														Name </label>
													<div class="col-sm-9">
														<input type="text" class="col-xs-12 col-sm-8"
															id="form-field-1" name="name">
													</div>
												</div>
												<div class="form-group">
													<label for="email"
														class="col-sm-3 control-label no-padding-right">
														Email </label>
													<div class="col-sm-9">
														<input type="email" class="col-xs-10 col-sm-8" id="email"
															name="email">
													</div>
												</div>
												<div class="form-group">
													<label for="phone"
														class="col-sm-3 control-label no-padding-right">
														Phone </label>
													<div class="col-sm-9">
														<input type="tel" class="col-xs-10 col-sm-8" id="phone"
															name="phone">
													</div>
												</div>
												<div class="form-group">
													<label for="comments"
														class="col-sm-3 control-label no-padding-right">
														Comments/<br>Questions</label>
													<div class="col-sm-9">
														<textarea class="col-xs-10 col-sm-8" id="comments"
															name="comments"></textarea>
													</div>
												</div>
												<div class="clearfix form-actions">
													<div class="col-md-offset-3 col-md-9">
														<button type="button" class="btn btn-info">
															<i class="icon-ok"></i> Submit
														</button>

														&nbsp; &nbsp; &nbsp;
														<button type="reset" class="btn">
															<i class="icon-undo"></i> Reset
														</button>
													</div>
												</div>
											</form>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

	</jsp:attribute></t:openpage>
