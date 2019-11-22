<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="base.jsp"%>
<html lang="en" class="fuelux" style="height: 2000px">
<head>
<title>权限管理主界面</title>
</head>
<script type="text/javascript" src="/js/json2.js"></script>
<script type="text/javascript" src="/js/nano.js"></script>
<style>
	.hidden{
		display: none;
	}
</style>
<script type="text/javascript">

    var hidden_package_submit = {};
    var hidden_server_submit = {};

	var package_tr_info='<tr>'+
	'<td>{index}</td>'+
	'<td>{gitUrl}</td>'+
	'<td>{createBy}</td>'+
	'<td>'+
		'<label class="radio radio-custom inline"><input {checked1} name ="{id}" value="1" type="radio" onclick="addPackageParam(this.name, this.value)"><i class="radio"></i>不可见</label>'+
		'<label class="radio radio-custom inline"><input {checked2} name ="{id}" value="2" type="radio" onclick="addPackageParam(this.name, this.value)"><i class="radio"></i>只读</label>'+
		'<label class="radio radio-custom inline"><input {checked3} name ="{id}" value="3" type="radio" onclick="addPackageParam(this.name, this.value)"><i class="radio"></i>修改&可执行</label>'+
	'</td>'+
	'</tr>';
	
	var instance_tr_info='<tr>'+
	'<td>{index}</td>'+
	'<td>{serverName}</td>'+
	'<td>{createBy}</td>'+
	'<td>{mainClass}</td>'+
    '<td>'+
        '<label class="radio radio-custom inline"><input {checked1} name ="{id}" value="1" type="radio" onclick="addServerParam(this.name, this.value)"><i class="radio"></i>不可见</label>'+
        '<label class="radio radio-custom inline"><input {checked2} name ="{id}" value="2" type="radio" onclick="addServerParam(this.name, this.value)"><i class="radio"></i>只读</label>'+
        '<label class="radio radio-custom inline"><input {checked3} name ="{id}" value="3" type="radio" onclick="addServerParam(this.name, this.value)"><i class="radio"></i>修改&可执行</label>'+
    '</td>'+
	'</tr>';

	var menu_tr_info='<tr>'+
	'<td>{index}</td>'+
	'<td><label class="checkbox"><input type="checkbox" {checked} check_value="{id}" name="{id}" vaule="{id}">{menuName}</label></td>'+
	'<td>{remark}</td>'+
	'</tr>';

	$(function(){
		
		$.getJSON("/user/queryAllUser.do",function(data){
			var dataSource = new StaticDataSource({
				columns : [{
					property : 'passport',
					label : '通行证',
					sortable : true
				},{
					property : 'userName',
					label : '用户名',
					sortable : true
				},{
					property : 'createBy',
					label : '添加人',
					sortable : true
				},{
					property : 'passport',
					label : '操作',
					render:function(rowIndex,index,data){
						option_info='<a href="javascript:void(0)" onclick="del_user(\''+data.passport+'\')">删除</a>';
						return option_info;
					}
				}],
				data:data,
				delay : 250
			});

			$('#MyStretchGrid').datagrid({
				dataSource : dataSource,
				stretchHeight : false,
				oddRowCSS : 'odd',
				evenRowCSS : 'even'
			});
		})

		$("#package_btn").on("click",function(){
			showLoading();
			var queryString = arrToParam(hidden_package_submit);
            if (queryString != null && queryString != "") {
                var userCode = $("#package_user").val();
                var params = {
                    "param":queryString,
                    "userCode":userCode
                };
    			$.getJSON("/permissions/updatePackagePermissions.do",
    					params,function(data){
                            hidden_package_submit = {};
                            removeLoading();
                            resultShow(data);
    			        })
            } else {
                removeLoading();
            }
		});
		
		$("#instance_btn").on("click",function(){
			showLoading();
			var queryString = arrToParam(hidden_server_submit);
            if (queryString != null && queryString != "") {
                var userCode = $("#instance_user").val();
                var params = {
                    "param": queryString,
                    "userCode": userCode
                };
                $.getJSON("/permissions/updateInstancePermissions.do",
                        params, function (data) {
                            hidden_server_submit = {};
                            removeLoading();
                            resultShow(data);
                        });
            } else {
                removeLoading();
            }
		});
		
		$("#menu_btn").on("click",function(){
			showLoading();
			var queryString = $('#menu_form').formSerialize(); 
			var userCode = $("#menu_user").val();
			var params = {
					"param":queryString,
					"userCode":userCode
			};
			$.getJSON("/permissions/updateMenuPermissions.do",
					params,function(data){
				removeLoading();
				resultShow(data);
			})
		});
		
		$("#load_instance_btn").on("click",function(){
			showLoading();
			var userCode = $("#instance_user").val();
			var exist = true;
			$.ajax({
			      url: "/user/userExist.do",
			      global: false,
			      type: "POST",
			      data: {userCode:userCode},
			      dataType: "json",
			      async:false,
			      success: function(data){
			    	  exist = data.result;
			      }
			   })
			if(exist=="false"||exist==false){
				resultShow({msg:"用户不存在"});
				removeLoading();
				return;
			}
			$.getJSON( "/instance/getAllInstance.do",{userCode:userCode},function(data){
				var tbody = "";
				for(var i=0;i<data.length;i++){
					if(data[i].permissions=="1"){
						data[i].checked1="checked"
					}else if(data[i].permissions=="2"){
						data[i].checked2="checked"
					}else if(data[i].permissions=="3"){
						data[i].checked3="checked"
					}else{
						data[i].checked1="checked"
					}
					data[i].index=i+1;
					tbody = tbody + nano(instance_tr_info,data[i])
				}
				$("#instance_table>tbody").empty().html(tbody);
				$('.radio-custom > input[type=radio]').each(function () {
					var $this = $(this);
					if ($this.data('radio')) return;
					$this.radio($this.data());
				});
				$("#instance_btn").removeAttr("disabled")
				removeLoading();
		});
	});
		
		$("#load_package_btn").on("click",function(){
			showLoading();
			var userCode = $("#package_user").val();
			var exist = true;
			$.ajax({
			      url: "/user/userExist.do",
			      global: false,
			      type: "POST",
			      data: {userCode:userCode},
			      dataType: "json",
			      async:false,
			      success: function(data){
			    	  exist = data.result;
			      }
			   })
			if(exist=="false"||exist==false){
				resultShow({msg:"用户不存在"});
				removeLoading();
				return;
			}
			$.getJSON( "/project/queryAllProject.do",{userCode:userCode},function(data){
				var tbody = "";
				for(var i=0;i<data.length;i++){
					if(data[i].permissions=="1"){
						data[i].checked1="checked"
					}else if(data[i].permissions=="2"){
						data[i].checked2="checked"
					}else if(data[i].permissions=="3"){
						data[i].checked3="checked"
					}else{
						data[i].checked1="checked"
					}
					data[i].index=i+1;
					tbody = tbody + nano(package_tr_info,data[i])
				}
				$("#package_table>tbody").empty().html(tbody);
				$('.radio-custom > input[type=radio]').each(function () {
					var $this = $(this);
					if ($this.data('radio')) return;
					$this.radio($this.data());
				});
				$("#package_btn").removeAttr("disabled")
				removeLoading();
		});
	});
		$("#load_menu_btn").on("click",function(){
			showLoading();
			var userCode = $("#menu_user").val();
			var exist = true;
			$.ajax({
			      url: "/user/userExist.do",
			      global: false,
			      type: "POST",
			      data: {userCode:userCode},
			      dataType: "json",
			      async:false,
			      success: function(data){
			    	  exist = data.result;
			      }
			   })
			if(exist=="false"||exist==false){
				resultShow({msg:"用户不存在"});
				removeLoading();
				return;
			}
			$.getJSON( "/menu/queryAllMenu.do",{userCode:userCode},function(data){
				var tbody = "";
				for(var i=0;i<data.length;i++){
					if(data[i].userCode!=""){
						data[i].checked="checked"
					}
					data[i].index=i+1;
					tbody = tbody + nano(menu_tr_info,data[i])
				}
				$("#menu_table>tbody").empty().html(tbody);
				$("#menu_btn").removeAttr("disabled")
				removeLoading();
		});
	});
		$("#add_user_btn").on("click",function(){
			var passport = $("#add_user").val();
			var userName = $("#add_userName").val();
			if(passport==""||userName==""){
				resultShow({msg:"请填写用户信息"})
				return;
			}
			showLoading();
			$.getJSON( "/user/addUser.do",{passport:passport,userName:userName},function(data){
				$.getJSON("/user/queryAllUser.do",function(data1){
					$('#MyStretchGrid').datagrid('reload',data1);
					removeLoading();
					resultShow(data);
				});
			});
		});
		
		
	});

	function clickMenu(menu){
		$("#package,#server,#menu,#user").addClass("hidden");
		$("#"+menu).removeClass("hidden");
	}
	
	function del_user(passport){
		showLoading();
		$.getJSON( "/user/deleteUser.do",{passport:passport},function(data){
			$.getJSON("/user/queryAllUser.do",function(data1){
				$('#MyStretchGrid').datagrid('reload',data1);
				removeLoading();
				resultShow(data);
			});
		});
	}

    function addPackageParam(key, value) {
//        alert(key + ":" + value);
        hidden_package_submit[key] = value;
    }
    function addServerParam(key, value) {
//        alert(key + ":" + value);
        hidden_server_submit[key] = value;
    }

    function arrToParam(array){
        var param = "";
        if (array != null){
            for (var key in array){
                param += key + "=" + array[key] + "&";
            }
            if (param != "" && param.lastIndexOf("&") == param.length-1){
                param = param.substring(0, param.length-1);
            }
//            alert("param value:" + param);
        }
        return param;
    }
</script>
<body>
<div class="container-fluid">
  <div class="row-fluid">
    <div class="span2">
      <ul id="project_list" class="nav nav-tabs nav-stacked">
      	<li><a href="javascript:void(0)" onclick="clickMenu('package');">打包权限管理</a></li>
      	<li><a href="javascript:void(0)"  onclick="clickMenu('server')">server部署权限管理</a></li>
      	<li><a href="javascript:void(0)"  onclick="clickMenu('menu')">用户菜单管理</a></li>
      	<li><a href="javascript:void(0)"  onclick="clickMenu('user')">用户管理</a></li>
      </ul>
    </div>
    <div class="span10">
    	<!-- 打包权限管理 -->
      <div id="package">
      	 <fieldset id="pro_fieldset">
			<legend><strong>打包权限管理</strong></legend>
		</fieldset>
      	<div class="input-append">
      		<span class="add-on">账户</span>
			  <input id="package_user" type="text" required="required">
			  <button id="load_package_btn" class="btn" type="button">加载权限</button>
		</div>
      	<form id="package_form">
			<table id="package_table" class="table table-bordered table-striped">
				<thead>
					<th>#</th>
					<th>project</th>
					<th>创建人</th>
					<th>权限</th>
				</thead>
				<tbody></tbody>
				<tfoot>
					<th>操作</th>
					<th colspan="3">
						<button id="package_btn" type="button" disabled  class="btn btn-primary span3">提交</button>
					</th>
				</tfoot>
			</table>
		</form>
      </div>
      <!-- server部署权限管理 -->
      <div id="server" class="hidden" style="height: 1000px">
      <fieldset id="pro_fieldset">
			<legend><strong>server部署权限管理</strong></legend>
		</fieldset>
      <div class="input-append">
      		<span class="add-on">账户</span>
			  <input id="instance_user" type="text">
			  <button id="load_instance_btn" class="btn" type="button">加载权限</button>
		</div>
		<form id="instance_form" action="">
      	<table id="instance_table" class="table table-bordered table-striped">
				<thead>
					<th>#</th>
					<th>实例模板</th>
					<th>创建人</th>
					<th>mainClass</th>
					<th>权限</th>
				</thead>
				<tbody>
				</tbody>
				<tfoot>
					<th>操作</th>
					<th colspan="4">
						<button id="instance_btn" type="button"  disabled class="btn btn-primary span3">提交</button>
					</th>
				</tfoot>
			</table>
		</form>
      </div>
      <!-- 菜单管理 -->
      <div id="menu" class="hidden" style="height: 1000px">
      <fieldset id="pro_fieldset">
			<legend><strong>用户菜单管理</strong></legend>
		</fieldset>
      <div class="input-append">
      		<span class="add-on">账户</span>
			  <input id="menu_user" type="text">
			  <button id="load_menu_btn" class="btn" type="button">加载权限</button>
		</div>
		<form id="menu_form" action="">
      	<table id="menu_table" class="table table-bordered table-striped">
				<thead>
					<th>#</th>
					<th>菜单名称</th>
					<th>备注</th>
				</thead>
				<tbody>
				</tbody>
				<tfoot>
					<th>操作</th>
					<th colspan="4">
						<button id="menu_btn" type="button" disabled  class="btn btn-primary span3">提交</button>
					</th>
				</tfoot>
			</table>
		</form>
      </div>
      <!-- 添加新用户 -->
      <div id="user" class="hidden" style="height: 1000px">
      <fieldset id="pro_fieldset">
			<legend><strong>用户管理</strong></legend>
		</fieldset>
      <div class="input-append">
      		  <span class="add-on">账户</span>
			  <input id="add_user" type="text">&nbsp&nbsp&nbsp
			  <span class="add-on">用户名</span>
			  <input id="add_userName" type="text">
			  <button id="add_user_btn" class="btn" type="button">添加新用户</button>
	  </div>
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
						<span style="font-size: 17px" >用户列表</span>
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
									<li data-value="5" data-selected="true"><a href="#">5</a></li>
									<li data-value="10"><a href="#">10</a></li>
									<li data-value="20"><a href="#">20</a></li>
									<li data-value="50"><a href="#">50</a></li>
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
    </div>
  </div>
</div>
</body>
</html>