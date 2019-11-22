<!DOCTYPE html>
<html>
<head>
<#include "head.ftl"/>
<title>砖题库_${areaName!"国家"}公务员申论真题</title>
<link rel="stylesheet" href="http://ns.huatu.com/pc/wechat/css/base.css">
<link rel="stylesheet" href="http://ns.huatu.com/pc/wechat/css/app.css">
<style>
    .g-hd{box-sizing:border-box;position:fixed;top:0;left:0;width:100%;height:.44rem;line-height:.44rem;background:#41790c;z-index:8001;}
</style>
</head>
<body>
<div class="g-hd g-nav2">
    <a class="nav-back" href="http://ns.huatu.com/pc/wechat/shenlun/index"></a>
    <h3 class="nav-tit">${areaName!"国家"}公务员申论真题</h3>
</div>
<div class="g-bd">
    <ul class="m-list">
     <#if paperList?exists>
         <#list paperList as paper>
             <li>
                 <a href="/pc/wechat/shenlun/${paper.id?c}">${paper.name}</a>
             </li>
         </#list>

     </#if>
    </ul>
</div>
</body>
<script>
    var _hmt = _hmt || [];
    (function() {
        var hm = document.createElement("script");
        hm.src = "//hm.baidu.com/hm.js?1d39a05aeff8b455a179675a2ab26c58";
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(hm, s);
    })();
</script>
</body>
</html>