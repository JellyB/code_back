<!DOCTYPE html>
<html>
<head>
 <title>砖题库申论</title>
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
        <h3 class="nav-tit">公务员申论</h3>
        <div class="nav-pull">
            <div class="nav-cnt">
                <a href="http://ns.huatu.com/pc/wechat/index" target="_self" class='xc'>公务员行测</a>
                <a href="http://ns.huatu.com/pc/wechat/shenlun/index" target="_self" class="sl">公务员申论</a>
            </div>
        </div>
    </div>
</div>
<div class="wrap wrap2" style="padding-bottom: 1rem;">
    <#if summaryList?exists>
        <#list summaryList as summary>
            <ul class="m-list">
                <a href="/pc/wechat/shenlun/list?areaId=${summary.areaId?c}"><li><i></i><span>${summary.count?c}套</span>${summary.areaName?replace("AA","")}</a></li>
            </ul>
        </#list>
    <#else>
        <div class="m-poptips m-poptips-2">
            <p>网络不佳，请重试~~</p>
            <a class="u-btn" id="refresh" href="javascript:;">刷新一下</a>
        </div>
    </#if>
</div>
<#include "download_app.ftl"/>
</body>
</html>
<script>
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

        if(((url!==getHost()) || (getHost()!== newurl)) && (url!='http://ns.huatu.com/pc/wechat/shenlun/index')){
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