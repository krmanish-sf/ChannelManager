<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.commons.httpclient.NameValuePair"%>
<%@page import="salesmachine.util.StringHandle"%>
<%@page import="org.apache.commons.httpclient.methods.PostMethod"%>
<%@page import="org.apache.commons.httpclient.HttpMethod"%>
<%@page import="org.apache.commons.httpclient.params.HttpClientParams"%>
<%@page import="org.apache.commons.httpclient.HttpClient"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="salesmachine.util.ApplicationProperties"%>
<%
	String responseText = null;
	try {
		String clientId = ApplicationProperties
		.getProperty(ApplicationProperties.SHOPIFY_API_KEY);
		String secretKey = ApplicationProperties
		.getProperty(ApplicationProperties.SHOPIFY_SECRET_KEY);
		String code = request.getParameter("code");
		String shop = request.getParameter("shop");
		String strUrl = "https://" + shop + "/admin/oauth/access_token";
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(strUrl);
		
		List<NameValuePair> parametersBody = new ArrayList<NameValuePair>();
		parametersBody.add(new NameValuePair("client_id",clientId));
		parametersBody.add(new NameValuePair("client_secret",secretKey));
		parametersBody.add(new NameValuePair("code",code));
		NameValuePair[] arr = new NameValuePair[parametersBody.size()];
		method.setRequestBody(parametersBody.toArray(arr));
		int responseCode = client.executeMethod(method);

		if (responseCode >= 200 & responseCode < 300) {
	responseText = method.getResponseBodyAsString();

		} else {
	//Handle Probale auth error and show appropriate message
		}
		if (!StringHandle.isNullOrEmpty(responseText)) {

		} else {
	//Handle Shopify error and show appropriate message
		}
	} catch (Exception e) {
		e.printStackTrace();
		//Handle exception and show user a proper message.
	}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Shopify Access Token</title>
<script src="//code.jquery.com/jquery-2.0.3.min.js"
	type="text/javascript"></script>
<script type="text/javascript">
	var tokenObj =<%=responseText%>;
	window.opener.put(tokenObj.access_token);
	window.close();
</script>

</head>
<body>

</body>
</html>