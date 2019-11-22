<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable='no'">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>练习报告</title>
    <link href="http://tiku.huatu.com/cdn/share/share_v4/css/common.css" rel="stylesheet">
</head>
<body>
<div id="accurate_estimate">
    <header class="clearfix">
        <top></top>
    </header>
    <div class="mainbox">
        <div class="maintit">
            <p class="p1">${reportInfo.title} <br/>${(reportInfo.paper.name)}</p>
            <p class="p2"><span
                    style="float:left;">交卷时间:${(reportInfo.createTime)?number_to_datetime?string("yyyy年MM月dd日HH时mm分")}</span><span
                    style="float: right">答题用时${(reportInfo.expendTime/60)?int}'${reportInfo.expendTime%60}"</span></p>
        </div>
        <div class="scorebox magbot">
            <div class="date-jz">
                <p class="ptit">得分</p>
                <p class="pdet"><i>${reportInfo.score}</i>分</p>
            </div>
        </div>
        <div class="scorebox magbot">
            <div class="scorebox2">
                <div class="num-jz">
                    <p class="ptit">答对</p>
                    <p class="pdet"><i>${reportInfo.rcount}</i>道/${reportInfo.paper.qcount}道</p>
                </div>
                <div class="score-jz">
                    <p class="ptit">全站平均分</p>
                    <p class="pdet"><i>${reportInfo.cardUserMeta.average}</i>分</p>
                </div>
                <div class="percent-jz">
                    <p class="ptit">已击败考生</p>
                    <p class="pdet"><i>${reportInfo.cardUserMeta.beatRate}%</i></p>
                </div>
            </div>
        </div>
        <div class="questionbox">
            <ul>
                <#list reportInfo.points as first>
                    <li class="first">
                        <div class="firstques">
                            <div class="questitadd"><p class="p">${first.name}</p></div>
                            <div class="quesdet">共${first.qnum}道，答对${first.rnum}道，正确率${first.accuracy}%</div>
                        </div>
                        <ul class="secondbox" style="display: none">
                            <#list first.children as second>
                                <li class="second">
                                    <div class="secondques">
                                        <div class="secquestitadd"><p class="p">${second.name}</p></div>
                                        <div class="quesdet">共${second.qnum}道，答对${second.rnum}道，正确率${second.accuracy}%
                                        </div>
                                    </div>
                                    <ul class="thirdbox" style="display: none">
                                    <#list second.children as third>
                                        <li>
                                            <div class="thirdques">
                                                <div class="questit"><p class="p">${third.name}</p></div>
                                                <div class="quesdet">共${third.qnum}道，答对${third.rnum}
                                                    道，正确率${third.accuracy}%
                                                </div>
                                            </div>
                                        </li>
                                    </#list>
                                    </ul>
                                </li>
                            </#list>
                        </ul>
                    </li>
                </#list>
            </ul>
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
