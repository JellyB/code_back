<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>评估报告</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <meta name="apple-touch-fullscreen" content="yes"/>
    <meta name="applicable-device" content="mobile"/>
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <meta content="email=no" name="format-detection"/>
    <link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/css/estimate.css">
</head>
<body>
<div class="header">
    <div class="icon"></div>
</div>
<div class="container" style="height:auto">
    <div class="title">我的练习报告</div>
    <div class="forecast_data">
        <div class="left_data">
            <p class="data_title">预测分数</p>
            <div>${powerSummary.score}分</div>
        </div>
        <div class="right_data">
            <p class="data_title">击败人数</p>
            <div class="data_outer"><div class="des"><p>击败了<span>${powerSummary.beat}%</span>的</p> <p>华图在线用户</p></p></div>
        </div>
    </div>
</div>
    <div class="key_outer" style="height:auto; overflow:auto; padding-bottom:.2rem"><span class="key">各项得分 </span><div class="item">
        <#list moduleSummaries as moduleSummary>
            <span>${moduleSummary.moduleName}${moduleSummary.score}分</span><br/>
        </#list>
            </div></div>

</div>
<div class="weixin" style="padding-left:.25rem">
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