<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>${courseName!""}</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/css/common_1.css">
</head>
<body>
<div class="g-fixed-head-wrap">
    <div class="g-fixed-head">
        <div class=" logo"></div>
        <!-- <a target="_blank" href="http://a.app.qq.com/o/simple.jsp?pkgname=com.huatu.handheld_huatu"> -->
        <a href="${handheld_huatu_url}">
            <div class="open-now">
                立即打开
            </div>
        </a>
    </div>
</div>
<div class="course-head">
    <div class="title">
    ${courseName!""}
    </div>
    <div class="time clearfix">
        <div class="icon fl">
        </div>
         <#if studyDate?default("")?trim?length gt 1>
            <div class="date fl">
                ${studyDate!""}
            </div>
            <div class="hour fl">
                （${timesLength!0}课时）
            </div>
         <#else>
             <div class="date fl"></div>
             <div class="hour fl">
                 ${timesLength!0}课时
             </div>
         </#if>
    </div>
    <div class="teachers clearfix">

        <#list teacherRoundPhoto as photo>
            <div class="teachers-item">
                <img src="${photo}" alt="">
            </div>
        </#list>


    <#--<div class="teachers-item">-->
    <#--<img src="http://upload.htexam.net/teacherphoto/1509364416.jpg" alt="">-->
    <#--</div>-->
    <#--<div class="teachers-item">-->
    <#--<img src="http://upload.htexam.net/teacherphoto/1509364416.jpg" alt="">-->
    <#--</div>-->
    </div>
</div>
<iframe src="${courseDescUrl}" id="iframepage" name="iframepage" frameBorder=0 scrolling=yes width="100%" height="100%"></iframe>
<#--<iframe src="http://123.103.79.69:8022/h5/detail_zhuanti_contents.php?rid=${courseid}" id="iframepage" name="iframepage" frameBorder=0 scrolling=yes width="100%" height="100%"></iframe>-->
<#--<iframe src="http://tk.htexam.com/h5/detail_zhuanti_contents.php?rid=${courseid}" id="iframepage" name="iframepage" frameBorder=0 scrolling=yes width="100%" height="100%"></iframe>-->


<#--<div class="header">-->
<#--<div class="container">-->
<#--<div class="title">${courseName!""}</div>-->
<#--<div class="lesson_time">-->
<#--<span class="time"></span>${studyDate!""}（${timesLength!0}课时）-->
<#--</div>-->
<#--<div class="lesson_person">-->
<#--<span class="person"></span>${teacherRoundPhoto!""}-->
<#--</div>-->
<#--</div>-->
<#--</div>-->

<#--<iframe src="https://apitk.huatu.com/h5/detail_zhuanti_contents.php?rid=${courseid}" id="iframepage" name="iframepage" frameBorder=0 scrolling=yes width="100%" height="100%"></iframe>-->
<#--<div class="download">-->
<#--<div class="logo"></div>-->
<#--<a class="down" href="http://a.app.qq.com/o/simple.jsp?pkgname=com.huatu.handheld_huatu">立即下载</a>-->
<#--</div>-->
</body>
<script>
    (function() {
        var timer,
                on = 'addEventListener',
                d = document,
                w = window,
                doc = d.documentElement,
                ps = 'pageshow';

        d[on]('DOMContentLoaded', resizeFontSize);

        if ('on' + ps in w)
            w[on](ps, function(e) {
                if (e.persisted) debouceResize();
            });
        else w[on]('load', debouceResize);

        w[on]('resize', debouceResize);

        function debouceResize() {
            clearTimeout(timer);
            setTimeout(resizeFontSize, 300);
        }

        function resizeFontSize() {
            console.log("13:59");
            doc.style.fontSize = getWidth() * 100 / 750 + 'px';
        }

        function getWidth() {
            return doc.getBoundingClientRect().width;
        }
    })();
</script>
<#--<script src="http://res.wx.qq.com/open/js/jweixin-1.2.0.js">-->

<#--</script>-->
<#--<script>-->

    <#--function  makeShare() {-->
        <#--wx.onMenuShareAppMessage({-->
            <#--title: '测试foo', // 分享标题-->
            <#--desc: '分享描述', // 分享描述-->
            <#--link: 'https://www.baidu.com/s?wd=%E6%9C%89%E9%81%93%E8%AF%8D%E5%85%B8&usm=2&ie=utf-8&rsv_cq=a&rsv_dl=0_right_recommends_merge_20826&euri=50aa9196b5ec49b19cc994934ddf35d0', // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致-->
            <#--imgUrl: 'https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=991890567,3530662074&fm=58&s=0FF6E812C5A54D035854BAF400000035', // 分享图标-->
<#--//                type: '', // 分享类型,music、video或link，不填默认为link-->
<#--//                dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空-->
            <#--success: function () {-->
                <#--alert('分享成功');-->
<#--// 用户确认分享后执行的回调函数-->
            <#--},-->
            <#--cancel: function () {-->
<#--// 用户取消分享后执行的回调函数-->
            <#--}-->
        <#--});-->

    <#--}-->
    <#--makeShare();-->
<#--</script>-->
<script type="text/javascript" src="http://tiku.huatu.com/cdn/share/js/weixin.common.js?v=1.0"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script src="https://code.jquery.com/jquery-1.11.1.min.js"></script>
<script>
    window.onload=function () {
        initWeiXin("${title!''}","${description!''}","${imgUrl!''}","${url!''}");
    };
</script>
</html>