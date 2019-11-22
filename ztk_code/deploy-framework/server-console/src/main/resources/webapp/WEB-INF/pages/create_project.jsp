<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="base.jsp"%>
<html lang="en" class="fuelux" >
<body>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta charset="utf-8">
<title>新增一个待管理project</title>
</head >
<body>
	<div class="well span12" style="margin-top: 100px">
<form id="projectForm" action="/project/createProject.do" method="post" class="form-horizontal well" style="width:500px; margin: 0px auto;background-color:#FFF" >
  <div class="control-group">
    <label class="control-label" for="gitUrl">project git url</label>
    <div class="controls">
      <input type="text" name="gitUrl" id="gitUrl" placeholder="http://192.168.100.20" required="required">
    </div>
  </div>
  <div class="control-group">
      <label class="control-label">操作</label>
    <div class="controls">
      <button id="button" type="submit"  class="btn btn-primary">新增</button>
    </div>
  </div>
</form>
</div>
</body>
</html>
