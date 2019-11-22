<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>竞技场排行</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/rank.css">
</head>
<body>
<div class="title">
    <p>华图在线竞技赛场排行榜</p>
</div>
<div class="banner">
    <div class="avatar">
        <img src="${avatar!""}">
    </div>
    <p>${nick!""}</p>

</div>
<div class="content">
    <div class="encourage">
        <div class="icon"></div>
        <p>你的表现太出色啦！</p>
        <p>在华图在线竞技赛场排行榜中排名前三</p>
        <p>请继续努力，上岸指日可待！</p>
    </div>
    <div class="tip_icon"></div>
    <p>毫无悬念，你敢来挑战吗？</p>
    <div class="down_btn">
        <a href="http://a.app.qq.com/o/simple.jsp?pkgname=com.huatu.handheld_huatu">下载华图在线，马上开战！</a>
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