<%@ page language="java" contentType="text/html; charset=UTF-8"
        pageEncoding="UTF-8"%>
<%@ include file="base.jsp"%>
<%
        String ip = request.getParameter("ip");
        String serverName = request.getParameter("serverName");
%>
<html id="myHtml" lang="en" class="fuelux">
<head>
        <meta charset="utf-8">
        <title>server信息查看平台</title>
</head>
<script type="text/javascript" src="<%=contextPath %>/js/LogWebSocket.js"></script>
<script type="text/javascript">
        var serverName = "<%=serverName %>";
        var ip = "<%=ip %>";
        var socket ;
        $(function(){
                var height=$(top).height()-200;
                var width = $(top).width();
                var style="height:"+height+"px";
                $("#log").css("height",height);
        })
        function execRequest(command){
                $("#tmpIframe").css("display","");
                if(socket){
                        socket.close();
                }
                var url ="http://"+ip+":37443/cmd/"+command;
                var height=$(top).height()-200;
                $("#tmpIframe").attr("src",url)
                $("#tmpIframe").attr("height",height);
        }

        function showLog(log_type){
                $("#tmpIframe").css("display","none");
                if(socket){
                        socket.close();
                }
                socket = new LogWebSocket({
                target:"log",
                uri:ip+":37442/flume/log",
                server_name:serverName,
                max_line:2000,
                protocol:"ws",
                log_type:log_type
        });
        }

</script>

<body>
                <div class="container-fluid">
        <div class="span3 sidebar-nav">
                <ul id="project_list" class="nav nav-tabs nav-stacked">
                <li><a href='javascript:void(0)' onclick="execRequest('top')">top</a></li>
                <li><a href='javascript:void(0)' onclick="execRequest('jps')">jps</a></li>
                <li><a href='javascript:void(0)' onclick="execRequest('ps')">ps</a></li>
                <li><a href='javascript:void(0)' onclick="showLog('log')">log</a></li>
                <li><a href='javascript:void(0)' onclick="showLog('err')">err log</a></li>
                </ul>
            </div>
                <div id="content" class="span9">
                 <fieldset id="pro_fieldset">                                                                                                 
		              <legend><strong><%=ip %>/<%=serverName %></strong></legend>                                                      
		          </fieldset> 
                <iframe id="tmpIframe" style="display:none;" scrolling=auto height="100%" width="100%" frameborder="0"  ></iframe>
                <div id="log"  style="overflow:scroll;"></div>
        </div>
</div>
</body>
</html>