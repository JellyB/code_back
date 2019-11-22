<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>练习报告</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/common.css">
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/practice.css">
</head>
<body>
<div class="header">
    <div class="icon"></div>
</div>
<div class="container">
    <div class="title">我的练习报告</div>
    <ul>
        <li>
            <p class="item">练习类型：<span>${typeName}</span></p>
        </li>
        <li>
            <p>答题用时：<span>${minutes}分${seconds}秒</span></p>
        </li>
        <li>
            <p>答对题目：<span>${rcount}道/${qcount}道</span></p>
        </li>
    </ul>
</div>

<div class="part">
    <div class="left"></div>
    <div class="right"></div>
</div>
<#if catgory==3>
<div class="weixin">
    <div class="erweima">
        <div class="img5"></div>
        <span>扫一扫，下载砖题库！</span>
    </div>
</div>
<#else >
<div class="weixin">
    <div class="erweima">
        <div class="img4"></div>
        <span>扫一扫，下载砖题库！</span>
    </div>
</div>
</#if>
<script type="text/javascript" src="http://tiku.huatu.com/cdn/share/js/weixin.common.js?v=1.0"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script src="https://code.jquery.com/jquery-1.11.1.min.js"></script>
<script>
    window.onload=function () {
        initWeiXin("${title!''}","${description!''}","${imgUrl!''}","${url!''}");
    };
</script>
</body>
</html>