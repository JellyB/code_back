<!DOCTYPE html>
<html>
<head>
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
    <link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/css/estimate.css">
</head>
<body>

<div class="header">
    <div class="icon"></div>
</div>
<div class="container">
    <div class="title">我的练习报告</div>
    <div class="exericise_data">
        <ul>
            <li><span class="exericise_title">练习类型</span><span class="exercise_detail">${typeName}</span></li>
            <li><span class="exericise_title">答题用时</span><span class="exercise_detail">${minutes}分${seconds}秒</span></li>
            <li><span class="exericise_title">答对题目</span><span class="exercise_detail">${rcount}道/${qcount}道</span></li>
        </ul>
    </div>

</div>
<div class="weixin">
    <div class="erweima">
        <div class="img4"></div>
        <span>扫一扫，下载华图在线！</span>
    </div>
</div>
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