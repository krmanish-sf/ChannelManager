<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="salesmachine.util.ApplicationProperties"%>
<%
	response.setHeader("Access-Control-Allow-Origin", "https://invsrc.myshopify.com/admin/oauth/access_token");
	//response.addHeader("Access-Control-Allow-Methods", "POST");
	response.addHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS");
	response.addHeader("Access-Control-Allow-Headers","x-requested-with");
	response.addHeader("Access-Control-Max-Age","604800");
	//response.addHeader("Access-Control-Allow-Methods", "GET");
	//response.addHeader("Access-Control-Allow-Methods", "OPTIONS");
	//response.setHeader("Access-Control-Allow-Headers", "Content-Type");
	//response.setHeader("Access-Control-Max-Age", "86400");
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Shopify Access Token</title>
<script src="http://code.jquery.com/jquery-2.0.3.min.js"
	type="text/javascript"></script>
<script type="text/javascript">
		var url = document.URL;
		var clientId = '<%=ApplicationProperties
					.getProperty(ApplicationProperties.SHOPIFY_API_KEY)%>';
		var secretKey = '<%=ApplicationProperties
					.getProperty(ApplicationProperties.SHOPIFY_SECRET_KEY)%>';
	//http://localhost:8080/admin/shopifyRedirect.jsp?code=0309afe6fa8019f0c105dc16d57177c2&
	//hmac=cf3b3e3cae0ad2b5d5fd2f7d7d4f6cabf1a4d068b2fd8d631ffdb7caff4d6973&
	//shop=invsrc.myshopify.com&signature=75ecc1cee6b20e3ac092a423fa82b4a9&timestamp=1432298781
	var token = url.substring(url.indexOf("code=") + 5, url.indexOf("&hmac"));
	//alert('token --> '+token);
	var str = url
			.substring(url.indexOf("shop=") + 5, url.indexOf(".myshopify"));
	var strUrl = "https://" + str + ".myshopify.com/admin/oauth/access_token";
	
	var request = new XMLHttpRequest();
	var params = "client_id=" + clientId
	+ "&client_secret=" + secretKey
	+ "&code=" + token;
	request.open('POST', strUrl, true);
	request.onreadystatechange = function() {if (request.readyState==4) alert("It worked!");};
	request.setRequestHeader("Access-Control-Allow-Origin","*");
	request.setRequestHeader("Access-Control-Allow-Headers", "x-requested-with, x-requested-by")
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	request.setRequestHeader("Content-length", params.length);
	request.setRequestHeader("Connection", "close");
	request.send(params);
	
	
	
	
	
	//window.opener.put(token);
	/* $(document)
			.ready(
					function() {
						//debugger;
						$
								.ajax({
									type : 'POST',
									url : strUrl,
									cache:false,
									contentType : 'text/html',//'application/x-www-form-urlencoded',
									dataType : "json",
									crossDomain : true,
									data : "client_id=" + clientId
											+ "&client_secret=" + secretKey
											+ "&code=" + token,
					 headers : {
										 						'Access-Control-Allow-Origin' : 'https://invsrc.myshopify.com/admin/oauth/access_token',
										 						'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
										 						'Access-Control-Max-Age': '604800',
										 						'Access-Control-Allow-Headers': 'x-requested-with'
										// 						"Access-Control-Allow-Headers": "Content-Type"
										//'access-control-allow-origin' : '*',
										//'Access-Control-Allow-Methods' : 'OPTIONS, GET, POST, HEAD, PUT',
										//'Access-Control-Allow-Headers' : 'X-custom',
										//'Access-Control-Allow-Credentials' : 'true'
									},
									success : function(data, textStatus, jqXHR) {
										alert(data);
										console.log(data);
										console.log(textStatus);
										console.log(jqXHR);
									},
									error : function(jqXHR, textStatus,
											errorThrown) {
										alert(textStatus);
										console.log(errorThrown);
										console.log(textStatus);
										console.log(jqXHR);
									}
								});

					}); */
	//window.close();
</script>

</head>
<body>

</body>
</html>