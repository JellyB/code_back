<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>课程分享</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/course.css">
</head>
<body>
<div class="header">
    <div class="container">
        <div class="title">${courseName!""}</div>
        <div class="lesson_time">
            <span class="time"></span>${studyDate!""}（${timesLength!0}课时）
        </div>
        <div class="lesson_person">
            <span class="person"></span>${teacherNames!""}
        </div>
    </div>
</div>

<iframe src="${courseDescUrl}" id="iframepage" name="iframepage" frameBorder=0 scrolling=yes width="100%" height="100%"></iframe>
<div class="download">
    <div class="logo"></div>
    <#if catgory==1>
        <a class="down" href="http://a.app.qq.com/o/simple.jsp?pkgname=com.netschool.main.ui">立即下载</a>
    <#elseif  catgory==3>
        <a class="down" href="http://a.app.qq.com/o/simple.jsp?pkgname=com.huatu.zhuantiku.sydw">立即下载</a>
    </#if>

</div>
</body>
<script type="text/javascript" src="http://tiku.huatu.com/cdn/share/js/weixin.common.js?v=1.0"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script src="https://code.jquery.com/jquery-1.11.1.min.js"></script>
<script>
    window.onload=function () {
        initWeiXin("${title!''}","${description!''}","${imgUrl!''}","${url!''}");
    };
</script>
</html>