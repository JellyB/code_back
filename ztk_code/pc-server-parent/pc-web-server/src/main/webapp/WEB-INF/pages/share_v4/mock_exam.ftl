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
        <div class="mockmenu">
            <ul>
                <li class="hover">行测</li>
                <li>申论</li>
                <li>总体</li>
            </ul>
        </div>
        <div class="mocklist">
            <div class="mockbox">
                <p class="mocktit">${resultMapMatch.name}</p>
                <p class="mockdet"><i>${resultMapMatch.score}</i>分</p>
                <div class="mockdet2">
                    <ul>
                        <li><p class="p1">同地区排名</p>
                            <p class="p2">
                                <i>${resultMapMatch.matchMeta.positionRank}</i>/${resultMapMatch.matchMeta.positionCount}
                            </p></li>
                        <li><p class="p1">全站排名</p>
                            <p class="p2">
                                <i>${resultMapMatch.cardUserMeta.rank}</i>/${resultMapMatch.cardUserMeta.total}</p></li>
                        <li><p class="p1">全站最高分</p>
                            <p class="p2"><i>${resultMapMatch.cardUserMeta.max}</i></p></li>
                        <li><p class="p1">全站平均分</p>
                            <p class="p2">
                                <i>${resultMapMatch.matchMeta.scoreLine.series[resultMapMatch.matchMeta.scoreLine.series?size-1].data[0]}</i>
                            </p></li>
                    </ul>
                </div>
                <div class="questionbox">
                    <div class="quessummary">
                        <p>考试情况</p>
                        <p class="p">共${resultMapMatch.paper.qcount}道，答对${resultMapMatch.rcount}
                            道，未答${resultMapMatch.ucount}道，总计用时${(resultMapMatch.expendTime/60)?int}
                            '${resultMapMatch.expendTime%60}"</p>
                    </div>
                    <ul>
                    <#list resultMapMatch.points as first>
                        <li class="first">
                            <div class="firstques">
                                <div class="questitadd"><p class="p">${first.name}</p></div>
                                <div class="quesdet">共${first.qnum}道，答对${first.rnum}道，正确率${first.accuracy}
                                    %
                                </div>
                            </div>
                            <ul class="secondbox" style="display: none">
                                <#list first.children as second>
                                    <li class="second">
                                        <div class="secondques">
                                            <div class="secquestitadd"><p class="p">${second.name}</p></div>
                                            <div class="quesdet">共${second.qnum}道，答对${second.rnum}
                                                道，正确率${second.accuracy}%
                                            </div>
                                        </div>
                                        <ul class="thirdbox" style="display: none">
                                        <#list second.children as third>
                                            <li>
                                                <div class="thirdques">
                                                    <div class="questit"><p class="p">${third.name}</p>
                                                    </div>
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
            <div class="mockbox" style="display:none;">
                <p class="mocktit">${resultMapEssay.name}</p>
                <p class="mockdet"><i>${resultMapEssay.score}</i>分</p>
                <div class="mockdet2">
                    <ul>
                        <li><p class="p1">同地区排名</p>
                            <p class="p2">
                                <i>${resultMapEssay.matchMeta.positionRank}</i>/${resultMapEssay.matchMeta.positionCount}
                            </p></li>
                        <li><p class="p1">全站排名</p>
                            <p class="p2">
                                <i>${resultMapEssay.cardUserMeta.rank}</i>/${resultMapEssay.cardUserMeta.total}</p></li>
                        <li><p class="p1">全站最高分</p>
                            <p class="p2"><i>${resultMapEssay.cardUserMeta.max}</i></p></li>
                        <li><p class="p1">全站平均分</p>
                            <p class="p2">
                                <i>${resultMapEssay.matchMeta.scoreLine.series[resultMapEssay.matchMeta.scoreLine.series?size-1].data[0]}</i>
                            </p></li>
                    </ul>
                </div>
                <div class="questionbox">
                    <div class="quessummary">
                        <p>考试情况</p>
                        <p class="p">共道${resultMapEssay.questionCount}，答对${resultMapEssay.rcount}
                            道，未答${resultMapEssay.ucount}道，总计用时${(resultMapEssay.expendTime/60)?int}
                            '${resultMapEssay.expendTime%60}"</p>
                    </div>
                    <ul>
                        <li class="first">
                            <ul class="secondbox" >
                                <li class="second">
                                    <ul class="thirdbox" >
                                        <li>
                                             <#list resultMapEssay.questionList as first>
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
            <div class="mockbox" style="display:none;">
                <p class="mocktit">${all.name}</p>
                <p class="mockdet"><i>${all.score}</i>分</p>
                <div class="mockdet2">
                    <ul>
                        <li><p class="p1">同地区排名</p>
                            <p class="p2">
                                <i>${all.matchMeta.positionRank}</i>/${all.matchMeta.positionCount}
                            </p></li>
                        <li><p class="p1">全站排名</p>
                            <p class="p2">
                                <i>${all.cardUserMeta.rank}</i>/${all.cardUserMeta.total}</p></li>
                        <li><p class="p1">全站最高分</p>
                            <p class="p2"><i>${all.cardUserMeta.max}</i></p></li>
                        <li><p class="p1">全站平均分</p>
                            <p class="p2">
                                <i>${all.matchMeta.scoreLine.series[all.matchMeta.scoreLine.series?size-1].data[0]}</i>
                            </p></li>
                    </ul>
                </div>
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
