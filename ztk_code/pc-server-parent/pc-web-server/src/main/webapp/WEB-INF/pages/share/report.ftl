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
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/common.css">
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/estimate_report.css">
</head>
<body>
<div class="header">
    <div class="icon"></div>
</div>
<div class="container">
    <div class="title">我的评估报告</div>
    <ul>
        <li>
            <p>预测分数：<span>${powerSummary.score}分</span></p>
        </li>
        <li>
            <p>击败人数：<span>击败了${powerSummary.beat}%的砖题库用户</span></p>
        </li>
        <li>
            <p>
            <div class="key">各项得分：</div>
            <span class="item">
                <#list moduleSummaries as moduleSummary>
                    <span>${moduleSummary.moduleName}${moduleSummary.score}分</span>
                </#list>
            </span></p>
        </li>
    </ul>
</div>

<div class="part">
    <div class="left"></div>
    <div class="right"></div>
</div>
<div class="weixin">
    <div class="erweima">
        <#if catgory==3>
            <div class="img5"></div>
            <span>扫一扫，下载砖题库！</span>
        <#else >
            <div class="img4"></div>
            <span>扫一扫，下载砖题库！</span>
        </#if>
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