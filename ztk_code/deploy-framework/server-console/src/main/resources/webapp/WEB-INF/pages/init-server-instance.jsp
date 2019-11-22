<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="base.jsp"%>
<html lang="en" class="fuelux">
<head>
<title>新增一个实例模板</title>
</head>
<script type="text/javascript" src="/js/json2.js"></script>
<script type="text/javascript">

$(function(){
	$('#deployForm').on("submit",function(){
		var ip_regex =/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
		var ips=$("#serverIps").val().split(",");
		var isIp = true;
		for(var i = 0;i<ips.length;i++){
			isIp=ip_regex.test(ips[i]);
			if(!isIp){
				alert("含有非法ip")
				return false;
			}
		}
        var serverMode = $("#serverMode").val();
        if(serverMode != 'online' && serverMode != 'test' && serverMode != 'develop'){
            alert("实例运行模式无效,必须是online/test/develop之一。")
            return false;
        }
		showLoading();
    });


    /**页面加载完后加载菜单*/
    $.getJSON('/project/queryProject.do', function (data) {
        projectData = data;
        var html = "";
        for (var i = 0; i < data.length; i++) {
            html += "<option onChange=\"renderModule(" + data[i].projectName +")\" value='" + data[i].projectName +"'><a href='javascript:void(0)'>" +data[i].projectName + "</a></option>" +
            "";
            $('#projectName').html(html);
        }
    });

    var module_template = '<label class="radio radio-custom inline" ><input type="radio" value="${module}" name="moduleName"><i class="radio"></i>${module}</label>';

    $('#projectName').change(function () {
        showLoading("#mainDiv");
        $.ajax({
            url : "/project/queryProjectModules.do",
            data:{project:$(this).children('option:selected').val()},
            async : true,
            dataType : "json",
            success : function(data) {

                if(data.status ==1){
                    resultShow({
                        msg:data.msg,
                        type:"fail"
                    })
                }

                var module = "";
                for (var i = 0; i < data.data.length; i++) {
                    module = module + module_template.replace(/\${module}/g, data.data[i])
                }
                $("#module").empty().html(module);

                $('.radio-custom > input[type=radio]').each(function () {
                    var $this = $(this);
                    if ($this.data('radio')) return;
                    $this.radio($this.data());
                });

                removeLoading();
            }
        })
    });

})
</script>
<body>
	<div class="span12 well">
		<div class="container-fluid">
		<div class="row-fluid">
			<div class="span9">
			<form id="deployForm" action="/instance/addInstance.do" class="form-horizontal">
			<fieldset id="args_fieldset">
				<legend id="args_fieldset_legend"><font color="red">添加实例模板</font></legend>
				<p>实例pom打包运行环境分：测试（develop），准线上（test），线上（online） 三个环境</p>
                <div class="control-group">
					<label class="control-label" for="projectName">Project Name</label>
                    <div class="controls">
                        <!-- COMBOBOX -->
                        <div id="branch_combobox" class="input-append combobox">
                            <select  id="projectName" name="projectName" type="text">
                                <%--<option value="develop">develop</option>--%>
                            </select>
                        </div>
                    </div>
				</div>
                <div class="control-group">
                    <label class="control-label" for="module">Module</label>
                    <div id="module" class="controls">
                    </div>
                </div>
				<div class="control-group">
					<label class="control-label" for="serverName">Server Name</label>

					<div class="controls">
						<input class="input-xlarge span12" id="serverName" name="serverName" type="text" required>
					</div>
				</div>

                <div class="control-group">
                    <label class="control-label" for="serverMode">Server Mode</label>
                    <div class="controls">
                        <select  id="serverMode" name="serverMode" type="text">
                            <option value="online">online</option>
                            <option value="test">test</option>
                            <option value="develop">develop</option>
                        </select>
                    </div>
                </div>

				<%--<div class="control-group">--%>
					<%--<label class="control-label" for="sourcePath">Source Path</label>--%>

					<%--<div class="controls">--%>
						<%--<input  placeholder="module-environment.zip" class="input-xlarge span12" id="sourcePath" name="sourcePath" type="text" required>--%>
					<%--</div>--%>
				<%--</div>--%>
				<div class="control-group">
					<label class="control-label" for="mainClass">Main Class</label>

					<div class="controls">
						<input class="input-xlarge span12" id="mainClass" name="mainClass" type="text" required>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="mainArgs">Main Args</label>
					<div class="controls">
						<input class="input-xlarge span12" id="mainArgs" name="mainArgs" type="text">
					</div>					
				</div>
				<div class="control-group">
					<label class="control-label" for="jvmArgs">Jvm Args</label>

					<div class="controls">
					<input class="input-xlarge span12" id="jvmArgs" name="jvmArgs" type="text" required></div>
				</div>
				<div class="control-group">
					<label class="control-label" for="serverIps">Server Ips</label>

					<div class="controls">
					<input class="input-xlarge span12" id="serverIps" name="serverIps" type="text" required></div>
				</div>

				<div class="control-group">
					<label class="control-label" for="remark">备注</label>

					<div class="controls">
					<input class="input-xlarge span12" id="remark" name="remark" type="text" required></div>
				</div>
				<div class="control-group">
					<label class="control-label" for="resetButton">操作</label>
					<div class="controls">
						<button id="resetButton" type="submit"  class="btn btn-primary span4">添加模板</button>
					</div>
				</div>
			</fieldset>
		</form>
			</div>
			</div>
		</div>
	</div>
</body>
</html>
