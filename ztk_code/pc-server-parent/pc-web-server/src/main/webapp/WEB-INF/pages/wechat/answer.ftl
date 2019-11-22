<!DOCTYPE html>
<html>
<head>
 <title>答题页</title>
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
        <a class="nav-back back" href="javascript:;">${catname}</a>
        <p class="nav-num f-fr"><span class="num-on f-cor-pink">1</span>/<span class="num-total">5</span></p>
    </div>
    <input type="hidden" name="starttime" id="starttime" value="${.now}">
    <input type="hidden" name="paperid" id="paperid" value="${paperid}">
    <ul class="view-cnt">
        <#--<if condition="$haveit eq 1">
            <div class="m-poptips m-poptips-2">
                <p>该模块测试题你已答完</p>
                <a class="u-btn" href="/pc/wechat/index">返回首页</a>
            </div>
        </if>-->
        <#if questionList?exists>
          <#list questionList as qlist>
            <#if qlist_index==0>
                <li class="view-page pageInt  page-0 now">
                <div class="m-topic contbox" id="now">
            <#else>
                <li class="view-page pageNew page-0">
                <div class="m-topic contbox">
            </#if>
            <h3 class="topic-tit"><span class="f-cor-pink">
                <span style="display:none" class='etype'>${qlist.type}</span>
                  <#if qlist.type==99>(单选题)<#else>(多选题)</#if>
                </span>
                ${qlist.stem}
            </h3>
            <ul class="topic-select">
                <#list qlist.choices as qc>
                    <input type="hidden" name="qid" id="${qlist_index}_qid" value="${qlist.id?c}">
                    <input type="hidden" name="answer" id="${qlist_index}_answer" value="${qlist.answer}">
                   <#if qlist.type==99>
                     <li class="sem">
                         <div class='select-left'><i class='sem_i'>
                             <#if qc_index==0>A<#elseif qc_index==1>B<#elseif qc_index==2>C<#elseif qc_index==3>D<#elseif qc_index==4>E
                             <#elseif qc_index==5>F<#elseif qc_index==6>G<#elseif qc_index==7>H</#if>
                         </i></div>
                         <div class="select-right">${qc}</div>
                     </li>
                   <#else>
                     <li class="dum">
                         <div class='select-left'><p class='dum_i'> <#if qc_index==0>A<#elseif qc_index==1>B<#elseif qc_index==2>C<#elseif qc_index==3>D<#elseif qc_index==4>E
                         <#elseif qc_index==5>F<#elseif qc_index==6>G<#elseif qc_index==7>H</#if></p></div>
                         <div class="select-right">${qc}</div>
                     </li>
                   </#if>
                </#list>
            </ul>
          </div>
          </li>
          <#if qlist.type!=99>
              <div class="u-agen u-badge-blue jiaojuan" style='display:none'>交卷</div>
          </#if>

          </#list>
        <#else>
            <div class="m-poptips m-poptips-2">
                <p>网络不佳，请重试~~</p>
                <a class="u-btn" id="refresh" href="javascript:;">刷新一下</a>
            </div>
        </#if>
    </ul>
</div>
<#if pointid == 754 >
    <div class="m-poptips m-poptips-1" style="display: none;">
        <i></i><p>正在加载</p>
    </div>
    <div class="m-material">
        <div id="mata-slide" class="mata-slide"><i></i>滑动查看资料</div>
        <div class="mata-cnt" ><span class="f-cor-pink">(材料)</span>
	        <span class='aly'>
                <#list questionList as qlst>
                    <div class="aly_${qlst_index+1}" <#if qlst_index!=0>style="display:none"</#if> onclick="cl(this)">
                    ${qlst.material!""}
                   </div>
                </#list>
            </span>
    </div>
    </div>
    <div class="imgbox" style="display:none;background:#000;position: fixed;top: 0;left: 0;width: 100%; height: 100%;z-index: 8008;">
        <div style="position:absolute;top: 0;left: 0;width: 100%; height: 100%;">
            <div class="hideimg"></div>
			    <span class="outspan" style="text-align:center;clean:both;transform-origin: 0% 0% 0px; position: absolute; transform: scale(1, 1) translate(0px, 0px);">
			   		<img class="imgbig" style=" display: table-cell;vertical-align:middle;width:100%" />
					</span>
        </div>
    </div>
    <script src="http://ns.huatu.com/pc/wechat/js/analysis.js"></script>
</#if>
</body>
<script>
    //材料题
    function this_aly(){
        return  this_href="/pc/wechat/cards"
    }
    function this_submit(){
        return this_href="/pc/wechat/cards/"
    }
    function back_index(){
        return this_href="/pc/wechat/index";
    }
    myDate = new Date();
    day=myDate.getDate();
    // 显示弹窗
    var i=0;
    function AutoShow(){
        i++;
        if(i<2)
            setTimeout("AutoShow();",1000);
        else{
            $(".tips_bg_1").fadeIn("slow");
            $(".tips_1").fadeIn("slow");
        }
    }
   /* if(({$viewsign}!='' )&& ($.cookie('close')!=day)){
        AutoShow();
    }*/
    //隐藏弹窗
    var time=5;
    function AutoClose()
    {
        time--;
        if(time>0)
            setTimeout("AutoClose();",1000);
        else{
            $(".tips_bg_1").fadeOut("slow");
            $(".tips_1").fadeOut("slow");
            $.cookie('close',day); // 存储 cookie,1天
        }
    }
    AutoClose();
    function adlast(){}
    function answer(){}
    //单选,如果有作答
    if($.cookie('select_arr')){
        var qu_num=$('.topic-select').length;
        var sel=$.cookie('select_arr').split(",");
        for(var i=1;i<=qu_num;i++){
            code = String(sel[i]).charCodeAt();
            $('.topic-select').eq(i-1).children().eq(code-65).addClass('topic-right');
        }
    }

</script>
<#include "footer.ftl"/>
</html>