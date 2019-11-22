<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="base.jsp"%>
<html lang="en" class="fuelux">
<script type="text/javascript" src="/js/nano.js"></script>
<script type="text/javascript" src="/js/json2.js"></script>
<head>
	<meta charset="utf-8">
	<title>服务器实例管理</title>
 <style>
        table tr.even {
            background: #ebebeb;
        }
        table tr.odd {
            background: #ffffff;
        }
    </style>
	<script>
	var current_instance={};
	var instances = [];
	var instance_base_info="<strong>main class</strong>:{mainClass}<br/>"+
		"<strong>main args</strong>:{mainArgs}<br/>"+
		"<strong>jvm args</strong>:{jvmArgs}<br/>"+
		"<strong>source path</strong>:{sourcePath}<br/><br/>";
	var ip_tr_info=
		'<tr>'+
			'<td>{index}</td>'+
			'<td><label class="checkbox"><input type="checkbox" check_value="{ip}" name="checked_ip" vaule="{ip}">{ip}</label></td>'+
			'<td><strong>{stat}</strong></td>'+
            '<td><strong>{uptime}</strong></td>'+
            '<td><strong>{deployer}</strong></td>'+
			'<td>'+
					'<a href="javascript:void(0)" onclick="deploy(\'{ip}\',\'deploy\');">deploy</a>&nbsp&nbsp'+
					'<a href="javascript:void(0)" onclick="deploy(\'{ip}\',\'start\');" >start</a>&nbsp&nbsp'+
					'<a href="javascript:void(0)" onclick="deploy(\'{ip}\',\'restart\');">restart</a>&nbsp&nbsp'+
					'<a href="javascript:void(0)" onclick="deploy(\'{ip}\',\'stop\');">stop</a>&nbsp&nbsp'+
					'<a href="javascript:void(0)" onclick="deploy(\'{ip}\',\'delete_server\',\'/instance/delIp.do\');">delete</a>&nbsp&nbsp'+
					'<a href="javascript:void(0)" onclick="showlog(\'{ip}\',\'{serverName}\');">部署log</a>&nbsp&nbsp'+
                    '<a href="javascript:void(0)" onclick="deploy(\'{ip}\',\'dump\');">dump</a>&nbsp&nbsp'+
                    '<a href="javascript:void(0)" onclick="dashboard(\'{ip}\',\'{serverName}\');">dashboard</a>'+
				'</td>'+
		'</tr>';
		$(function() {
			
			var dataSource = new StaticDataSource({
				columns : [{
					property : 'serverName',
					label : 'Server Name',
					sortable : true
				},{
					property : 'sourcePath',
					label : 'Source Path',
					sortable : true
				},{
					property : 'createBy',
					label : 'CreateBy',
					sortable : true
				},{
					property : 'permissions',
					label : '权限',
					render:function(rowIndex,index,data){
						if(data.permissions==2){//查看权限
							return "只读";
						}else if(data.permissions==3){//可执行权限
							return "修改&可执行";
						}
					}
				},{
					property : 'createBy',
					label : '操作',
					render:function(rowIndex,index,data){
						instances.push(data);
						option_info='<a href="javascript:void(0)" onclick="show_instance('+rowIndex+')">查看</a>';
						if(data.permissions==3){//可执行权限
							option_info = option_info + '&nbsp;&nbsp;' +
                            '<a href="javascript:void(0)" onclick="del_instance('+rowIndex+')">删除</a>&nbsp;&nbsp;'+
							'<a href="javascript:void(0)" onclick="modify_instance('+rowIndex+')">修改</a>&nbsp;&nbsp;';
                            ;
						}
                        if(data.serverMode=="online"){//可申请部署
                            option_info = option_info + '&nbsp;&nbsp;' +
                            '<a href="javascript:void(0)" onclick="create_instance_ticket('+rowIndex+')">申请部署</a>&nbsp;&nbsp;';
                        }
						return option_info;
					}
				} ],
				data:[],
				delay : 250
			});

			$('#MyStretchGrid').datagrid({
				dataSource : dataSource,
				stretchHeight : false,
				rowClick : fillTemplate,
				oddRowCSS : 'odd',
				evenRowCSS : 'even'
			});
			$('#MyStretchGrid').on("loaded",function(event,data){
				instances=data
				//清空实例基本信息
				$("#instance_base_info").empty();
                $("#instance_log_info").empty();
				$("#ticket_log_info").empty();
				$("#ips_table > tbody").empty()
			});
			
			change_tab("online");
			
			$("#allCheck").on("click",function(){
				var checkboxArr=$("input:checkbox");
				if ($(this).prop("checked") == true) { // 全选
					checkboxArr.each(function() {
						   $(this).prop("checked", true);
					});
				}else{
					checkboxArr.each(function() {
						   $(this).prop("checked", false);
					});
				}
				$($("button[name='deploy']")).each(function(){
					getCheckboxVal();	
				});
			})
			
			$("#submitbtn").on("click",function(){
				$('#instanceForm').ajaxSubmit({ 
			        dataType:  'json',
			        url:"/instance/updateInstance.do",
			 		beforeSubmit:function(){
			 			$('#myModal').modal('hide');
						showLoading("#mainDiv");
					},
					success:function(data) {
						removeLoading();
						resultShow(data,function(){
							location.href="/instance/instanceManager.do";
						});
					}
			    }); 
			});

            $("#ticketSubmitbtn").on("click",function(){
                $('#ticketForm').ajaxSubmit({
                    dataType:  'json',
                    url:"/ticket/create.do",
                    beforeSubmit:function(){
                        $('#createTicketFrom').modal('hide');
                    },
                    success:function(data) {
                        resultShow(data);
                    }
                });
            });
			
		});
			
			function fillTemplate(index, data) {
				//保存当前的instance
				current_instance=data;
				var info = nano(instance_base_info, data);
				$("#instance_base_info").empty().html(info)

                //取消全选选择
				$("#allCheck").prop("checked",false);
				fillInstanceInfos(data.id,data.serverName);
				if(data.permissions==2){//查看权限
					$("#addIpBtn").attr("disabled","disabled");
					$("#batchTh>a").removeAttr("onclick");
				}else if(data.permissions==3){//可执行权限
					$("#addIpBtn").removeAttr("disabled","disabled");
				}
				
			}
			
			function del_instance(index){
				var instance=instances[index];
				bootbox.confirm("确定删除'"+instance.serverName+"'?",function(res){
					if(res){
						showLoading("#mainDiv");
						$.getJSON("/instance/delInstance.do",{instanceId:instance.id},function(data){
							removeLoading();
							resultShow(data,function(){
								location.href="/instance/instanceManager.do";
							});
							
						})
					}
					
				});
				
			}
			
			function modify_instance(index){
				var instance=instances[index];
				setFormVal(instance);
				$("#sourcePath,#mainArgs,#jvmArgs,#remark,#submitbtn").removeProp("disabled");
				$('#myModal').modal();
			}

            function create_instance_ticket(index){
                var instance=instances[index];
                clearForm('#ticketForm');
                setCreateTicketFromVal(instance);
                $('#createTicketFrom').modal();
            }

            function setCreateTicketFromVal(data){
                $("#ticketProjectName").val(data.projectName);
                $("#ticketServerName").val(data.serverName);
                $("#module").val(data.sourcePath.substr(0,data.sourcePath.length - "-dist".length));
            }

			function show_instance(index){
				var instance=instances[index];
				setFormVal(instance);
				$("#sourcePath,#mainArgs,#jvmArgs,#remark,#submitbtn").attr("disabled","disabled");
				$('#myModal').modal();
			}
			
			function setFormVal(data){
				$("#projectName").val(data.projectName);
				$("#serverName").val(data.serverName);
				$("#mainClass").val(data.mainClass);
				$("#sourcePath").val(data.sourcePath);
				$("#mainArgs").val(data.mainArgs);
				$("#jvmArgs").val(data.jvmArgs);
				$("#remark").val(data.remark);
				$("#instanceId").val(data.id);
				
			}
			
			
			
			function getCheckboxVal(){
				var arrChk ="";
				($("input[name='checked_ip']")).
				each(function(){
					if($(this).prop("checked")==true){
						arrChk+=$(this).attr("check_value") +',';
					}
				});
				if(arrChk.length>0){
					arrChk=arrChk.substr(0,arrChk.length-1);
				}
				return arrChk;
			}
			
			function deploy(ips,action,url){
				if(current_instance.permissions!=3){//不具有可执行权限
					return;
				}
				if(ips.length<1){
					resultShow({result:false,msg:"请选择服务器"})
					return;
				}
				if(typeof url == "undefined"){
					url = "/instance/deploy.do";
				}
                //批量操作时 1：串行 0：并行
                var batchType = 0;
                ($("input[name='deployType']")).
                    each(function(){
                        if($(this).prop("checked")==true){
                            batchType = $(this).val();;
                        }
                });
				showLoading("#mainDiv");
				var isExec = true;
				$.ajax({
					url : "/instance/verifyAction.do",
					data:{
						ips:ips,
						serverName:current_instance.serverName,
						action:action,
                        batchType:batchType
					},
					async : false,
					dataType : "json",
                    timeout:5*60*1000,
					success : function(data) {
						if(data.result==false||data.result=="false"){
							removeLoading();
							resultShow(data);
							isExec=false;
						}
					}
				});
				if(!isExec){//不能执行此操作
					return;
				}
				var params = {
						ips:ips,
						action:action,
						instanceId:current_instance.id,
                        batchType:batchType
				};
				$.post( url, params, function(data){
					removeLoading();
					fillInstanceInfos(current_instance.id,current_instance.serverName);
					resultShow(data);
					
				},"json")
			}


			function fillTickLogs(instance_id,server_name){
				$.ajax({
					url : "/ticket/listTicketByServerName.do",
					data:{serverName:server_name},
					async : true,
					dataType : "json",
					success : function(result) {
						tickets = result.tickets
						msg = "";
						for(var i=0;i<tickets.length;i++){
							tickets[i].index=i+1;
							msg = msg + i + ". " + tickets[i].releaseLog + "#" + tickets[i].createBy + "<br/>";
						}
						$("#ticket_log_info").empty().html(msg);
					}
				})
			}


			function fillInstanceInfos(instance_id,serverName){
				showLoading("#mainDiv");
				$.ajax({
					url : "/instance/getIps.do",
					data:{instanceId:instance_id},
					async : true,
					dataType : "json",
					success : function(result) {
						var ips_tbody = $("#ips_table > tbody")
						ips_tbody.empty();
                        ips = result.ips
						for(var i=0;i<ips.length;i++){
							ips[i].index=i+1;
							ips[i].serverName=serverName;
							var ip_info =  nano(ip_tr_info,ips[i])
							ips_tbody.append(ip_info);					
						}
                        logs = result.logs
                        msg = "";
                        for(var i=0;i<logs.length;i++){
                            logs[i].index=i+1;
                            msg = msg +logs[i].logMessage + "#" + logs[i].createBy + "#" + logs[i].createDate + "<br/>";
                        }
                        $("#instance_log_info").empty().html(msg);
						fillTickLogs(instance_id,serverName);
						removeLoading();
					}
				})
			}

			function addIp(){
				var ips=$("#addIps").val().trim();
				if(ips==""){
					resultShow({result:false,msg:"缺少必填项"})
					return;
				}
				var instanceId = current_instance.id;
				if(typeof(instanceId)=="undefined"){
					resultShow({result:false,msg:"请选择实例组"})
					return;
				}
				var ip_regex =/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
				var ip_arr=ips.split(",");
				var isIp = true;
				for(var i = 0;i<ip_arr.length;i++){
					isIp=ip_regex.test(ip_arr[i]);
					if(!isIp){
						resultShow({result:false,msg:"含有非法ip"})
						return;
					}
				}
				showLoading("#mainDiv");
				$.post( "/instance/addIp.do", {ips:ips,instanceId:instanceId,action:"init_server"}, function(data){
					removeLoading();
					fillInstanceInfos(instanceId,current_instance.serverName)
					resultShow(data)
				},"json")
			}
			
			function dashboard(ip,serverName){
				location.href="/monitor/dashboard.do?ip="+ip+"&serverName="+serverName;
			}
			
			function showlog(ip,serverName){
				$.post( "/instance/getShellDeployLog.do", {ip:ip,serverName:serverName}, function(data){
					$("#shellLog").empty().html(data.msg);
				},"json")				
			}
			
			function change_tab(env){
				$.ajax({
					url : "/instance/queryInstance.do",
					async : false,
					data:{"serverMode":env},
					dataType : "json",
					success : function(data) {
						$('#MyStretchGrid').datagrid('reload',data);
					}
				})
			}
	</script>


</head>

<body>
<div class="container" id="mainDiv">
	<ul id="myTab" class="nav nav-tabs">
	  <li class="active"><a href="#main" onclick="change_tab('online')" data-toggle="tab"><strong>线上环境</strong></a></li>
	  <li><a href="#main" onclick="change_tab('develop')" data-toggle="tab"><strong>测试环境</strong></a></li>
	  <li><a href="#main" onclick="change_tab('test')" data-toggle="tab"><strong>准线上环境</strong></a></li>
	</ul>
	<!-- STRETCH DATAGRID -->
	<div id="main" style="height:300%;width:100%">
		<table id="MyStretchGrid" class="table table-bordered datagrid">

			<thead>
			<tr>
				<th>
					<div class="datagrid-header-left">
						<div class="input-append search datagrid-search">
							<input type="text" class="input-medium" placeholder="filter">
							<button class="btn"><i class="icon-search"></i></button>
						</div>
					</div>
					<div class="datagrid-header-right">
						<span style="font-size: 17px" >实例模板列表</span>
					</div>
				</th>
			</tr>
			</thead>

			<tfoot>
			<tr>
				<th>
					<div class="datagrid-footer-left" style="display:none;">
						<div class="grid-controls">
							<span>
								<span class="grid-start"></span> -
								<span class="grid-end"></span> of
								<span class="grid-count"></span>
							</span>
							<div class="select grid-pagesize" data-resize="auto">
								<button data-toggle="dropdown" class="btn dropdown-toggle">
									<span class="dropdown-label"></span>
									<span class="caret"></span>
								</button>
								<ul class="dropdown-menu">
									<li data-value="5" ><a href="#">5</a></li>
									<li data-value="10"><a href="#">10</a></li>
									<li data-value="20"><a href="#">20</a></li>
									<li data-value="50" data-selected="true"><a href="#">50</a></li>
									<li data-value="100"><a href="#">100</a></li>
								</ul>
							</div>
							<span>Per Page</span>
						</div>
					</div>
					<div class="datagrid-footer-right" style="display:none;">
						<div class="grid-pager">
							<button type="button" class="btn grid-prevpage"><i class="icon-chevron-left"></i></button>
							<span>Page</span>

							<div class="input-append dropdown combobox">
								<input class="span1" type="text">
								<button class="btn" data-toggle="dropdown"><i class="caret"></i></button>
								<ul class="dropdown-menu"></ul>
							</div>
							<span>of <span class="grid-pages"></span></span>
							<button type="button" class="btn grid-nextpage"><i class="icon-chevron-right"></i></button>
						</div>
					</div>
				</th>
			</tr>
			</tfoot>

		</table>
	</div>
			<div  class="bs-docs-example">
				<div id="instance_base_info" style="font-size: 16px"></div>
				<form class="form-inline">
				  <input type="text" id="addIps" placeholder="172.17.12.69"/>
				  <button id="addIpBtn" type="button" onclick="addIp()"  class="btn btn-primary">新增ip</button>
				</form>
				<table id="ips_table" class="table table-bordered table-striped">
				<thead>
					<th>#</th>
					<th>ip</th>
					<th>Server Status</th>
                    <th>最后部署时间</th>
                    <th>最后部署人</th>
					<th>操作</th>
				</thead>
				<tbody></tbody>
				<tfoot>
					<th>批量操作</th>
					<th><label class="checkbox inline"><input id="allCheck" type="checkbox">全选</label>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label class="radio radio-custom inline"><input value="1" name="deployType" checked type="radio"><i class="radio"></i>串行部署</label>
                        <label class="radio radio-custom inline"><input value="0" name="deployType" type="radio"><i class="radio"></i>并行部署</label>
                    </th>
					<th></th>
                    <th></th>
                    <th></th>
					<th id="batchTh">
						<a href="javascript:void(0)" onclick="deploy(getCheckboxVal(),'deploy');">deploy</a>
						<a href="javascript:void(0)" onclick="deploy(getCheckboxVal(),'start');">start</a>
						<a href="javascript:void(0)" onclick="deploy(getCheckboxVal(),'restart');">restart</a>
						<a href="javascript:void(0)" onclick="deploy(getCheckboxVal(),'stop');">stop</a>
						<a href="javascript:void(0)" onclick="deploy(getCheckboxVal(),'delete_server','/instance/delIp.do');">delete</a>
					</th>
				</tfoot>
			</table>
            </div>

        <div class="bs-docs-example-logs">
            <div id="instance_log_info" style="font-size: 14px"></div>
        </div>

		<div class="bs-docs-example-ticket">
			<div id="ticket_log_info" style="font-size: 14px"></div>
		</div>



    </div>
<pre id="shellLog"></pre>                                                                             
<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    <h3 id="myModalLabel">模板信息</h3>
  </div>
  <div class="modal-body">
    <form id="instanceForm" action="/instance/updateInstance.do" class="form-horizontal">
    			<input id="instanceId" name="instanceId" type="hidden" />
				<div class="control-group">
					<label class="control-label" for="projectName">Project Name</label>

					<div class="controls">
						<input class="input-xlarge " id="projectName" name="projectName" type="text" disabled>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="serverName">Server Name</label>

					<div class="controls">
						<input class="input-xlarge " id="serverName" name="serverName" type="text" disabled>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="mainClass">Main Class</label>

					<div class="controls">
						<input class="input-xlarge " id="mainClass" name="mainClass" type="text" disabled>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="sourcePath">Source Path</label>

					<div class="controls">
						<input class="input-xlarge " id="sourcePath" name="sourcePath" type="text" required>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="mainArgs">Main Args</label>
					<div class="controls">
						<input class="input-xlarge" id="mainArgs" name="mainArgs" type="text" required>
					</div>					
				</div>
				<div class="control-group">
					<label class="control-label" for="jvmArgs">Jvm Args</label>

					<div class="controls">
					<input class="input-xlarge " id="jvmArgs" name="jvmArgs" type="text" required></div>
				</div>
				<div class="control-group">
					<label class="control-label" for="remark">备注</label>

					<div class="controls">
					<input class="input-xlarge" id="remark" name="remark" type="text" required></div>
				</div>
		</form>
  </div>
  <div class="modal-footer">
    <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
    <button id="submitbtn" type="submit" class="btn btn-primary">提交</button>
  </div>
</div>

<div id="createTicketFrom" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="createTicketFromLable">工单信息</h3>
    </div>
    <div class="modal-body">
        <form id="ticketForm" action="/ticket/create.do" class="form-horizontal">
            <div class="control-group">
                <label class="control-label" for="projectName">Project Name</label>
                <div class="controls">
                    <input class="input-xlarge " id="ticketProjectName" name="projectName" type="text" >
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="serverName">Module</label>
                <div class="controls">
                    <input class="input-xlarge " id="module" name="module" type="text" >
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="mainClass">Instance Name</label>
                <div class="controls">
                    <input class="input-xlarge " id="ticketServerName" name="serverName" type="text" >
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="type">分支</label>
                <div class="controls">
                    <!-- COMBOBOX -->
                    <div id="branch_combobox" class="input-append dropdown combobox">
                        <input class="span2" name="branch" type="text" required>
                        <button class="btn" data-toggle="dropdown"><i class="caret"></i></button>
                        <ul class="dropdown-menu">
                            <li data-value="develop"><a href="#">develop</a></li>
                            <li data-value="master" data-selected="true"><a href="#">master</a></li>
                        </ul>
                    </div>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="sourcePath">部署说明</label>
                <div class="controls">
                    <textarea class="span3" rows="5" id="releaseLog" name="releaseLog"></textarea>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="deployer">处理人</label>
                <div class="controls">
                    <select  id="deployer" name="deployer" type="text">

                    </select>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="type">部署类型</label>
                <div class="controls">
                    <select  id="type" name="type" type="text">
                        <option value="1">周二部署</option>
                        <option value="2">周四部署</option>
                        <option value="3">紧急部署</option>
                    </select>
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button id="ticketSubmitbtn" type="submit" class="btn btn-primary">提交</button>
    </div>
</div>

</body>
</html>