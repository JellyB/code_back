<!DOCTYPE html>
<html>
<title>答案页</title>
<#include "head.ftl"/>
<script type="text/javascript">
    $(function () {
        $('.outspan').each(function () {
            new RTP.PinchZoom($(this), {});
        });
    })
</script>
</head>
<body style="overflow-y: hidden;">
<div class="g-bd">
    <div class="g-nav">
        <a class='nav-back' href="/pc/wechat/index">${catname}</a>
        <input type="hidden" name="pointid" id="pointid" value="${pointid}">
        <p class="nav-num f-fr"><span class="num-on f-cor-pink">1</span>/<span class="num-total">50</span></p>
    </div>
    <ul class="view-cnt">
    <#list questionList as qlist>
        <#if qlist_index==0>
        <li class="view-page pageInt  page-0 now" >
        <div class="m-topic2 contbox" id="now">
        <#else>
        <li class="view-page pageNew page-0">
        <div class="m-topic2 contbox" >
        </#if>
        <h3 class="topic-tit">
            <span class="f-cor-pink"><#if qlist.type==99>(单选题)<#else>(多选题)</#if></span>
                ${qlist.stem}
        </h3>
        <ul class="topic-select">
            <#list qlist.choices as qc>
                <#if qlist.type==99>
                    <li <#if answerCard.corrects[qlist_index]==1&&qc_index+1==answerCard.answers[qlist_index]?eval> class="topic-right" <#elseif (answerCard.corrects[qlist_index]==2&&qc_index+1==answerCard.answers[qlist_index]?eval)> class="topic-error" </#if> >
                        <div class='select-left'><i><#if qc_index==0>A<#elseif qc_index==1>B<#elseif qc_index==2>C<#elseif qc_index==3>D<#elseif qc_index==4>E
                        <#elseif qc_index==5>F<#elseif qc_index==6>G<#elseif qc_index==7>H</#if></i></div>
                        <div class="select-right">${qc}&nbsp;</div>

                </li>
                <#else >
                    <li <#if answerCard.corrects[qlist_index]==1&&qc_index+1==answerCard.answers[qlist_index]?eval> class="topic-right" <#elseif (answerCard.corrects[qlist_index]==2&&qc_index+1==answerCard.answers[qlist_index]?eval)> class="topic-error" </#if> >
						<div class='select-left'><i class='dum_i'><#if qc_index==0>A<#elseif qc_index==1>B<#elseif qc_index==2>C<#elseif qc_index==3>D<#elseif qc_index==4>E
                        <#elseif qc_index==5>F<#elseif qc_index==6>G<#elseif qc_index==7>H</#if></i></div>
                        <div class="select-right">${qc}&nbsp;</div>
                </li>
                </#if>
            </#list>
        </ul>
        <div class="u-hr"></div>
        <dl class="topic-answer">
            <dt class="ans-info"><span class="u-badge u-badge-green">答案 </span> <span class="f-cor-green"><#if qlist.answer==1>A<#elseif qlist.answer==2>B<#elseif qlist.answer==3>C<#elseif qlist.answer==4>D<#elseif qlist.answer==5>E
            <#elseif qlist.answer==6>F<#elseif qlist.answer==7>G<#elseif qlist.answer==8>H</#if></span>，
                <#if answerCard.corrects[qlist_index]==1>
                    <span class="ans-txt">答案正确</span>
                <#elseif answerCard.corrects[qlist_index]==2>
                    <span class="ans-txt">你的答案是:<#if answerCard.answers[qlist_index]?eval==1>A<#elseif answerCard.answers[qlist_index]?eval==2>B<#elseif answerCard.answers[qlist_index]?eval==3>C<#elseif answerCard.answers[qlist_index]?eval==4>D<#elseif answerCard.answers[qlist_index]?eval==5>E
                    <#elseif answerCard.answers[qlist_index]?eval==6>F<#elseif answerCard.answers[qlist_index]?eval==7>G<#elseif answerCard.answers[qlist_index]?eval==8>H</#if>
                        </span>
                <#else >
                    <span class="f-cor-pink">
                               尚未作答
                        </span>
                </#if>

            </dt>
            <dd class="ans-explain"><span class="u-badge u-badge-gray" style="float:left;margin-right:.1rem">解析</span><span>${qlist.analysis}</span></dd>
        </dl><br/>
    </div>
    </li>
    </#list>
    </ul>
</div>
<#if pointid==754>
<div class="m-poptips m-poptips-1" style="display: none;">
    <i></i><p>正在加载</p>
</div>
<div class="m-material">
    <div id="mata-slide" class="mata-slide"><i></i>滑动查看资料</div>
    <div class="mata-cnt" ><span class="f-cor-pink">(材料)</span>
                <span class='aly'>
                     <#list questionList as qlst>
                         <div class="aly_${qlst_index+1}" <#if qlst_index!=0>style="display:none"</#if> onclick="cl(this)">
                         ${qlst.material!}
                         </div>
                     </#list>
                    <span>
    </div>
</div>
<div class="imgbox" style="display:none;background:#000;position: fixed;top: 0;left: 0;width: 100%; height: 100%;z-index: 8008;">
    <div style="position:absolute;top: 0;left: 0;width: 100%; height: 100%;">
        <div class="hideimg"></div>
        <span class="outspan" style="text-align:center;clean:both;transform-origin: 0% 0% 0px; position: absolute; transform: scale(1, 1) translate(0px, 0px);"><img class="imgbig" style=" display: table-cell;vertical-align:middle;width:100%" /></span>
    </div>
</div>
<script src="http://ns.huatu.com/pc/wechat/js/analysis.js"></script>
<#else>
    <div class="m-material" style="width:auto" >
        <div class="mata-slide" id="m-material"></div>
    </div>
</#if>

</body>
</html>
<script>
    //材料题
    function this_aly(){
        return  this_href="/pc/cards";
    }
    //推荐弹窗关闭
    myDate = new Date();
    day=myDate.getDate();
    $(".colse-app-ad").click(function(){
        $(".ad-last").fadeOut("slow");
        $.cookie('clean_last', day); // 存储 cookie
    })
    //关闭弹窗后,推荐广告不再在本次操作弹出
    var c=$.cookie('clean-last'); // 读取 cookie
    if(c==day){
        $(".ad-last").fadeOut("slow");
    }
    function adlast(){
        return c=$.cookie('clean_last');
    }
    function answer(){}
</script>
<#include "footer.ftl"/>