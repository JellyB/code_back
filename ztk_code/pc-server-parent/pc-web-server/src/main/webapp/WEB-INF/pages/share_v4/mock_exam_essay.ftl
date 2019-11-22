<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable='no'">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>模考大赛</title>
    <link href="http://tiku.huatu.com/cdn/share/share_v4/css/common.css" rel="stylesheet">
</head>
<body>
<div id="accurate_estimate">
    <header class="clearfix">
        <top></top>
    </header>
    <div class="mainbox">
        <div class="mockbox">
                <p class="mocktit">${name}</p>
                <p class="mockdet"><i>${score}</i>分</p>
                <div class="mockdet2">
                    <ul>
                        <li><p class="p1">同地区排名</p>
                            <p class="p2">
                                <i>${matchMeta.positionRank}</i>/${matchMeta.positionCount}
                            </p></li>
                        <li><p class="p1">全站排名</p>
                            <p class="p2">
                                <i>${cardUserMeta.rank}</i>/${cardUserMeta.total}</p></li>
                        <li><p class="p1">全站最高分</p>
                            <p class="p2"><i>${cardUserMeta.max}</i></p></li>
                        <li><p class="p1">全站平均分</p>
                            <p class="p2">
                                <i>${matchMeta.scoreLine.series[matchMeta.scoreLine.series?size-1].data[0]}</i>
                            </p></li>
                    </ul>
                </div>
                <div class="questionbox">
                    <div class="quessummary">
                        <p>考试情况</p>
                        <p class="p">共${questionCount}道，答对${rcount}
                            道，未答${ucount}道，总计用时${(expendTime/60)?int}
                            '${expendTime%60}"</p>
                    </div>
                    <ul>
                        <li class="first">
                            <ul class="secondbox" >
                                <li class="second">
                                    <ul class="thirdbox" >
                                        <li>
                                             <#list questionList as first>
                                                 <div class="thirdques">
                                                     <div class="questit"><p class="p">${essay[first.type-1]}</p></div>
                                                     <div class="quesdet">得分${first.examScore}/${first.score}
                                                         ,用时${(first.spendTime/60)?int}
                                                         '${first.spendTime%60}"，${first.inputWordNum}字
                                                     </div>
                                                 </div>
                                             </#list>
                                        </li>
                                    </ul>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
    </div>
    <footer>
        <bottom></bottom>
    </footer>
</div>
<script type="text/javascript" src="http://tiku.huatu.com/cdn/share/share_v4/js/manifest.js"></script>
<script type="text/javascript" src="http://tiku.huatu.com/cdn/share/share_v4/js/vendor.js"></script>
<script type="text/javascript" src="http://tiku.huatu.com/cdn/share/share_v4/js/app.js?v=1"></script>
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
