<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>华图在线竞技赛场战绩</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://ns.huatu.com/pc/share/css/arena_summary.css">
</head>
<body>
<div class="title">
    <p>华图在线竞技赛场战绩</p>
</div>
<div class="banner">
    <div class="avatar">
        <img src="${avatar!""}">
    </div>
    <p>${nick!""}</p>

</div>
<div class="content">
    <div class="encourage">
        <ul>
            <li class="success_sum">
                <#if (winCount>9999)>
                    9999<sup class="up">+</sup>
                <#else >
                  ${winCount?c}
                </#if>

            </li>
            <li class="vs">vs</li>
            <li class="fail_sum">
            <#if (failCount>9999)>
                9999 <sup class="fail_up">+</sup>
            <#else >
              ${failCount?c}
            </#if>
            </li>
        </ul>
        <div class="des_icon clearfix">
            <span>胜(场)</span>
            <span>败(场)</span>
        </div>
        <div class="smile_icon">
            <div class="success_icon"></div>
            <div class="fail_icon"></div>
            <div class="progress">

                <div class="progress-bar progress-bar-danger progress-bar-striped active" role="progressbar" aria-valuenow="${winCount?c}" aria-valuemin="0" aria-valuemax="100" style="width: ${winPercent}">
                </div>
                <div class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="${failCount?c}" aria-valuemin="0" aria-valuemax="100" style="width: ${failPercent}">
                </div>

            </div>
        </div>
    </div>
    <div class="tip_icon"></div>
    <div class="pk_des">
        <p>我在华图在线公务员竞技赛场PK,</p>
        <p>共战胜${winCount?c}场。</p>
    </div>
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