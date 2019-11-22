<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>模考报告</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/match20180111/css/main.css">
    <link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/match20180111/css/reset.css">
    <script src="http://tiku.huatu.com/cdn/share/match20180111/js/rem.js">
    </script>
    <script src="http://tiku.huatu.com/cdn/share/match20180111/js/main.js">
    </script>
</head>

<body>
<div>
    <div class="header1 clearfix">
        <div class="icon001 fl">
        </div>
        <a href="http://a.app.qq.com/o/simple.jsp?pkgname=com.huatu.handheld_huatu">
            <div class="fr btn-download">
                立即下载
            </div>
        </a>
    </div>
    <div class="panel-box">
        <div class="title tac">
            我的模考报告
        </div>
        <div>
            <ul class="list">
                <li class="clearfix">
                    <div class="item-name">
                        模考总分
                    </div>
                    <div class="item-value">
                    ${score}分
                    </div>
                </li>
                <li class="clearfix">
                    <div class="item-name">
                        行测得分
                    </div>
                    <div class="item-value">
                        ${lineTestScore}分
                    </div>
                </li>
                <li class="clearfix">
                    <div class="item-name">
                        申论得分
                    </div>
                    <div class="item-value">
                        ${essayScore}分
                    </div>
                </li>
                <li class="clearfix">
                    <div class="item-name">
                        全站排名
                    </div>
                    <div class="item-value">
                        ${totalRank}
                    </div>
                </li>
                <li class="clearfix">
                    <div class="item-name">
                        同地区排名
                    </div>
                    <div class="item-value">
                        ${positionRank}
                    </div>
                </li>
                <li class="clearfix">
                    <div class="item-name">
                        全站平均分
                    </div>
                    <div class="item-value">
                        ${average}分
                    </div>
                </li>
            </ul>
        </div>
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