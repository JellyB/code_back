<!DOCTYPE html>
<html>
<head>
  <#include "head.ftl"/>
  <title>砖题库_${paper.name!""}</title>
  <style>
      .g-hd{box-sizing:border-box;
          position:absolute;
          top:0;
          left:0;
          width:100%;
          height:.44rem;
          line-height:.44rem;
          background:#41790c;
          z-index:8001;
      }
  </style>
</head>
<body>
<div class="g-hd g-nav3">
    <a class="nav-back f-fl" href="javascript:history.back(-1)">${paper.name!""}</a>
    <p class="nav-num f-fr"><span class="num-on f-cor-pink">2</span>/<span class="num-total">30</span></p>
</div>
<div class="g-bd">

    <ul class="view-cnt">
     <#if paper.questions ? exists>
        <#list paper.questions as question>
            <#if question_index==0>
                <li class="view-page pageInt  page-01 now" >
                <div class="m-topic contbox" id="now">
            <#else >
                <li class="view-page pageNew page-01">
                <div class="m-topic contbox" >
            </#if>

            <#if question.type==2>
                <h3 class="topic-tit">${question.stem!""}<br/>${question.restrict!""}
                <#list question.subQuestions as subQuestion>
                    <div class="topic-cnt">
                        <h3 class="topic-tit">${subQuestion.stem!""}<br/>${subQuestion.restrict!""}
                        </h3>
                        <div class="topic-answer">
                            <h3 class="ans-tit">砖题库参考答案</h3>
                            <p class="ans-txt">${subQuestion.answer!""}</p>
                        </div>
                    </div>
                </#list>

            <#else >
                <div class="topic-cnt">
                    <h3 class="topic-tit">${question.stem!""}<br/>${question.restrict!""}
                    </h3>
                    <div class="topic-answer">
                        <h3 class="ans-tit">砖题库参考答案</h3>
                        <p class="ans-txt">${question.answer!""}</p>
                    </div>
                </div>
            </#if>

        </div>
        </li>
        </#list>
       </#if>
<div class="m-material">
    <div id="mata-slide" class="mata-slide"><i></i>滑动查看资料</div>
    <div class="mata-cnt">
        <#list paper.materials as data>
            <p><span class="f-cor-pink">(材料${data_index+1})</span>${data}</p>
        </#list>
    </div>
</div>

</ul>
</div>

<script src="http://ns.huatu.com/pc/wechat/js/app.Shenlun.js"></script>
</body>
</html>