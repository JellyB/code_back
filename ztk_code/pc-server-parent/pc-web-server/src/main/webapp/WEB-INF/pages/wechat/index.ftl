<!DOCTYPE html>
<html>
<head>
	<title>砖题库</title>
    <#include "head.ftl"/>
	<style>
	body{
		height:auto;
		}
	</style>
</head>
<body>
<div class="header-wrap im-header">
	<div class="g-hd g-nav-s">
	    <h3 class="nav-tit">公务员行测</h3>
	    <div class="nav-pull">
	        <div class="nav-cnt">
	        	<a href="http://ns.huatu.com/pc/wechat/index" target="_self" class='xc'>公务员行测</a>
	            <a href="http://ns.huatu.com/pc/wechat/shenlun/index" target="_self" class="sl">公务员申论</a>
	        </div>
	    </div>
	</div>
	<a  href="/pc/wechat/cards"><span class='jilu'>我的记录</span></a>
</div>
<form action="/pc/wechat/cards" method="POST" id="pointForm">
    <input type="hidden" name="pointid" id="pointid">
</form>
<!-- 菜单不可用样式是在li上加一个class="dis-menu" -->
<!-- 行测 -->
<div class="wrap wrap1">
    <ul class="menu-list" >
             <li><a id="chouti('392')" href="javascript:create_practice(392)"> <i class='ico icon-circle icon-paper-pen'> </i>常识判断</a> </li>
             <li><a id="chouti('435')" href="javascript:create_practice(435)"> <i class='ico icon-circle icon-book'> </i>言语理解与表达</a></li>
             <li><a id="chouti('482')" href="javascript:create_practice(482)"> <i class='ico icon-circle icon-pen-rule'></i> 数量关系 </a></li>
             <li><a id="chouti('642')" href="javascript:create_practice(642)"> <i class='ico icon-circle icon-book-open'></i>判断推理</a></li>
             <li><a id="chouti('754')" href="javascript:create_practice(754)" > <i class='ico icon-circle icon-search'> </i>资料分析</a></li>
</ul>
</div>
<#include "download_app.ftl"/>
</body>
</html>
<script type="text/javascript">
	function create_practice(pointId) {
        $('#pointid').val(pointId);
		$("#pointForm").submit();
    }
//推荐弹窗关闭
 	myDate = new Date();
	day=myDate.getDate(); 
$(".appad-close").click(function(){
		$(".m-appad").hide();
		$.cookie('clean', day); // 存储 cookie 
})
//关闭弹窗后,推荐广告不再在本次操作弹出
var c=$.cookie('clean'); // 读取 cookie 
if(c==day){
	$(".m-appad").hide();
}else{
	$(".m-appad").show();
}
if($.cookie('re')!=day){
	//获取url参数
	url=location.href;
	//获取路径
	function getHost(url){ 
	if (!url) url = window.location.href; 
	var reg = new RegExp("([^?]*)", "i"); 
	if (reg.test(url)){ 
	return unescape(RegExp.$1.replace(/\+/g, " ")); 
	}else{ 
	return ""; 
	} 
	}; 
	var a=url.lastIndexOf("/");
	var newurl=url.substr(0,a+1)+"index";

	if(((url!==getHost()) || (getHost()!== newurl)) && (url!='http://ns.huatu.com/pc/wechat/index')){
	 location.replace(newurl);
	 $.cookie('re',day);
	}
}
// 下拉导航
$('.g-nav-s').click(function(){
	$('.g-nav-s .nav-pull').fadeToggle();
})
</script>
<#include "footer.ftl"/>