<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<% 
String userName=(String)request.getHeader("X-SohuPassport-UserId");
     if(userName==null||"".equals(userName.trim())){
			userName= (String)session.getAttribute("X-SohuPassport-UserId");
		}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>没有权限</title>
</head>
<body>
	<h3 align="center"><font color="red">您的账户 <%=userName %> 没有权限进入发布系统，请向相关人员申请权限。</font><br>
	<a href="http://passport.sohu.com/sso/logout_js.jsp?ru=http://deploy.k.sohu.com/user/logout.do" class="navbar-link">Logout</a></h3>
</body>
</html>