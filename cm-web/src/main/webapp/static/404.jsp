<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:openpage>
	<jsp:attribute name="maincontent">
		<div class="main-container-inner">
			<div class="page-header">
				<h1>
					Error<small><i class="ace-icon fa fa-angle-double-right"></i>
					</small>
				</h1>
			</div>
			<div class="container">
				<div class="row">
					<div class="widget-box">
						<div class="widget-header widget-header-flat">
							<h4 class="widget-title smaller">
								<i class="icon-cloud"></i>Server encountered an error while trying to process your request.
							</h4>
						</div>
						<div class="widget-body">
							<div class="widget-main">
								<div class="row">
									<div class="col-sm-12 col-md-12">
									<h3>Status Code:</h3>${pageContext.errorData.statusCode}
									<h3>URI:</h3>${pageContext.errorData.requestURI}
									<h3>Message:</h3>${pageContext.exception.message}
									</div>
								</div>
						</div>
						</div>
					</div>
				</div>
			</div>
		</div>
</jsp:attribute>
</t:openpage>