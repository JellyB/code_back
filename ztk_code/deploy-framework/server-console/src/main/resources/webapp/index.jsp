<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="base.jsp"%>
<%
 String userName=(String)request.getSession().getAttribute("userName");
%>
<html lang="en" class="fuelux" >
<style type="text/css">
body {
		padding-top: 80px;
		padding-bottom: 40px;
}
</style>
<script type="text/javascript" src="/js/nano.js"></script>
<script type="text/javascript" src="/js/json2.js"></script>
<script type="text/javascript">
	var menu_info ='<li><a href="javascript:void(0);"'+
		' onclick="clickMenu(\'{menuUrl}\',this);">{menuName}</a></li>'
		
	$(function(){
		$.getJSON("/menu/queryMenu.do",function(data){
			var html = "";
			for(var i=0;i<data.length;i++){
				html = html + nano(menu_info,data[i]);
			}
			$("#menu_ul").append(html);
		})
		
	});

	function clickMenu(url,dom) {
		$("ul.nav-tabs > li").removeClass("active");
		$(dom).parent().addClass("active");
		$("#iframepage").attr("src", url);
		window.frames("iframepage").location.reload(true);
	}

	/**设置iframe自适应高度*/
	
	function iFrameHeight() {
		var iframe = document.getElementById("iframepage");
		var height = $(iframe).contents().find("html").height(); 
        iframe.height=height;
	}
	function logout() {
		document.getElementById("logoutForm").submit();
	}
</script>
<body data-spy="scroll">
<div class="navbar navbar-fixed-top navbar-inverse">
	<div class="navbar-inner">
		<div class="container">
			<a id="BrandLink" class="brand" href="/">SCM部署系统</a><br/><br/>
			<div class="nav-collapse">
				<ul id="menu_ul" class="nav nav-tabs">
					<li><a href="javascript:void(0);"
							onclick="clickMenu('/instance/initServerPackage.do',this);">工程打包</a></li>
					<li class="active"><a href="javascript:void(0);"
							onclick="clickMenu('/instance/instanceManager.do',this);">服务器实例管理</a></li>
				</ul>
				 <p class="navbar-text pull-right">
                    <%=userName %> 欢迎你 &nbsp&nbsp<a href="javascript:void(0)" onclick="logout();" class="navbar-link">Logout</a>
                </p>
			</div>
		</div>
	</div>
</div>
<div class="container" scrolling="yes"  height="100%">
<form id="logoutForm" action="/user/logout.do"></form>
	<iframe id="iframepage" name="iframepage"
					src="/instance/instanceManager.do"  frameborder="0" scrolling="no"   width="100%" onload="iFrameHeight(this);"></iframe>
	</div>
</body>
</html>