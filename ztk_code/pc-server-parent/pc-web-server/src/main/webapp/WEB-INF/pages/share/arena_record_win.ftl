<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>砖题库竞技赛场成绩统计</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/arena_record_win.css">
</head>
<body>
<div class="title">
    <p>砖题库竞技赛场成绩统计</p>
</div>
<div class="banner">
    <div class="avatar">
        <img src="${avatar!""}">
    </div>
    <p>${nick!""}</p>

</div>
<div class="content">
    <div class="shine">
        <div class="test_type">练习类型：<span>${typeName}</span></div>
        <div class="test_time">交卷时间：<span>${createtime?number_to_datetime?string("yyyy-MM-dd HH:mm")!}</span></div>
        <div class="center">
            <div class="tip_icon"></div>
        </div>
        <div class="pk_des">
            <p>我在砖题库公务员竞技赛场PK，</p>
            <p>又是一场毫无悬念的胜利！</p>
        </div>
    </div>
    <div class="down_btn">
        <a href="http://a.app.qq.com/o/simple.jsp?pkgname=com.netschool.main.ui">下载砖题库，马上开战！</a>
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