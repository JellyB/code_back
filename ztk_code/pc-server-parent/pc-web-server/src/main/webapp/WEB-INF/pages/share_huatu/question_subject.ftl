<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>分享主观试题</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="applicable-device" content="mobile" />
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection" />
    <meta content="email=no" name="format-detection" />
    <link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/css/common.css">
    <link rel="stylesheet" href="http://tiku.huatu.com/cdn/share/css/share.css">
    <script type="text/javascript" src="http://cdn.bootcss.com/jquery/1.11.1/jquery.min.js"></script>
    <script type="text/javascript" src="http://cdn.bootcss.com/underscore.js/1.8.3/underscore-min.js"></script>
    <script type="text/javascript" src="http://tiku.huatu.com/cdn/share/js/pinchzoom.js"></script>
    <script type="text/javascript">
        $(function () {
            $('.outspan').each(function () {
                new RTP.PinchZoom($(this), {});
            });
        })
    </script>
</head>
<body>
<div class="header">
    <div class="left">
        <div class="img"></div>
        <div class="title">
            <span>华图在线</span>
            <p>过关才是硬道理</p>
            <ul>
                <li></li>
                <li></li>
                <li></li>
                <li></li>
                <li></li>

            </ul>
        </div>

    </div>
        <a class="right" href="http://a.app.qq.com/o/simple.jsp?pkgname=com.huatu.handheld_huatu">立即打开</a>
</div>
<div class="m-topic contbox now" id="now">
    <h3 class="topic-tit">
        <span class="f-cor-pink">
            <span style="display:none" class="etype">0</span>(${question.teachType!""})</span>${question.stem!""}</h3>


    <div class="view_analysis">查看解析</div>
    <div class="u-hr" style="display: block"></div>
    <dl class="topic-answer" style="display: none">
        <dt class="ans-info">
            <span class="u-badge u-badge-green">答案 </span>
            <span class="f-cor-green"></span>
        <div>${question.referAnalysis!""}</div>
        </dt>
        <#if (question.answerRequire??&&question.answerRequire?length>0)||(question.examPoint??&&question.examPoint?length>0)
        ||(question.scoreExplain??&&question.scoreExplain?length>0)||(question.solvingIdea??&&question.solvingIdea?length>0)>
            <dd class="ans-explain">
                <span class="u-badge u-badge-gray" style="margin-right:.1rem">解析</span>
                <div>
                    <#if question.answerRequire?? >
                        <p class="item-p">${question.answerRequire}</p>
                    </#if>
                    <#if question.examPoint?? >
                        <p class="item-p">${question.examPoint}</p>
                    </#if>
                    <#if question.scoreExplain?? >
                        <p class="item-p">${question.scoreExplain}</p>
                    </#if>
                    <#if question.solvingIdea?? >
                        <p class="item-p">${question.solvingIdea}</p>
                    </#if>
                </div>
            </dd>
        </#if>
    </dl>
</div>
<div class="m-material">
    <div id="mata-slide" class="mata-slide"><i></i>滑动查看资料</div>
        <div class="mata-cnt" >
        <#if (question.materials?exists) && (question.materials?size>0)>
            <span class="f-cor-pink">(材料)</span>
             <span class="aly">
                 <#list question.materials as qm>
                     <div class="aly_${qm_index+1}" onclick="cl(this)">
                         <p> ${qm!""}</p>
                     </div>
                 </#list>
            </span>
        </#if>
        <#if question.require??>
            <span class="f-cor-pink">（注意事项/要求）</span>
             <span class="aly">
                     <div class="aly_1" onclick="cl(this)">
                         <p> ${question.require!""}</p>
                     </div>
            </span>
        </#if>
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
<script type="text/javascript">
    //pc，移动端判断，修改参数,材料分析使用
    function IsPC() {
        var userAgentInfo = navigator.userAgent;
        var Agents = ["Android", "iPhone",
            "SymbianOS", "Windows Phone",
            "iPad", "iPod"];
        var flag = true;
        for (var v = 0; v < Agents.length; v++) {
            if (userAgentInfo.indexOf(Agents[v]) > 0) {
                flag = false;
                break;
            }
        }
        return flag;
    }
    var pc=IsPC();
    h=$(".view-cnt").height();
    if(pc){
        $(".mata-cnt").height(h/2);
        $(".mata-slide").hide();
    }else{
        //材料高度
        var hall=$(window).height();
        $('.mata-cnt').css('height',2+ "rem");
        $('.now').height(hall-$('.mata-cnt').height()-148);
        document.querySelector(".now").style.height = (document.body.clientHeight - $(".mata-cnt").height() -170) + "px";

    }
    //材料题图片
    $(".swipebox").click(function() {
        url=$(this).attr('src');

        $('.imgbig').attr('src',url);
        //$('.imgbig').css('z-index',10)
        $('.imgbox').css({'display':'table',"top":0});

        if($('.imgbig').height()>$(window).height()-40){
            old_height=$('.imgbig').height();
            old_width=$('.imgbig').width();
            $('.imgbig').css('height',$(window).height()-80);
            $('.imgbig').css('width',($(window).height()-80)/old_height*old_width);
            $(".outspan").css('top',40);
        }else{
            $(".pinch-zoom-container").css('top',($(window).height()-($(window).width()/$('.imgbig').width()*$('.imgbig').height()))/2);
        }
    });


    //关闭大图
    $(".imgbox div:not(.pinch-zoom-container)").click(function() {
        $('.imgbox').hide();
        $("div").remove('.pinch-zoom-container');
        var contain = $('<span class="outspan" style="text-align:center;clean:both;transform-origin: 0% 0% 0px; position: absolute; transform: scale(1, 1) translate(0px, 0px);"><img class="imgbig" style=" display: table-cell;vertical-align:middle;width:100%" /></span>');
        $('.imgbox').append(contain);
        $('.outspan').each(function () {
            new RTP.PinchZoom($(this), {});
        });
    });
    //材料为空时重新加载
    function cl(a){
        if($(a).html()==''){
            $('.m-poptips-1').show();
            $(a).show();
            $('.m-poptips-1').hide();
        }
    }
    window.onload = function() {
        document.querySelector(".now").style.height = (document.body.clientHeight - $(".mata-cnt").height() - 100) + "px";
    }
    //拖拽效果4
    var $dragBtn = document.querySelector(".mata-slide");//滑块
    cnt=document.querySelector(".mata-cnt"),//下面盒子
            nav=document.querySelector(".header"),
            client=document.documentElement.clientHeight;


    /*var constVar = {
     config: {
     stopStep: 0.8
     }
     };*/
    var EventHandler = {
        touchmove: function () {2
            Proxy.throttle.apply(this, arguments);
        },
        touchstart:function(ev){
            ev.preventDefault();
        },
        scroll:function(opts){
            var touchY=opts.touchY || 0;
            if(touchY>=130) {
                cnt.style.height = client - touchY-40+ "px";
                document.querySelector(".now").style.height = touchY-$dragBtn.offsetHeight-40+ "px";
            }
        }
    };
    $dragBtn.addEventListener("touchstart", EventHandler.touchstart, false);
    $dragBtn.addEventListener("touchmove", EventHandler.touchmove, false);

    var Proxy = (function () {
        return {
            throttle: function (ev) {
                ev.preventDefault();
                ev.stopPropagation();
                Touch.move.apply(this, arguments);
            }
        }
    })();

    var Touch = (function () {
        return {
            move: function (ev,callback) {
                if (ev.target.id != "mata-slide") {
                    return;
                }
                var touches = ev.touches, touch;
                if (touches && touches.length) {
                    touch = touches[0];
                    EventHandler.scroll.call(this,{
                        touchX:touch.clientX,
                        touchY:touch.clientY
                    });
                }
                callback && callback();
            }
        }
    })();
    //选项高度
    var opt_num=$('.select-right').length;
    for(i=0;i<opt_num;i++){
        $('.select-left').eq(i).attr('style','line-height:'+$('.select-right').eq(i).height()+'px')
    }
    //点选
    /*点击菜单*/
    /*$("#list_menu_main").on('click', 'a', function (e) {
     $(this).siblings().removeClass("list_active");
     $(this).toggleClass('list_active');

     })*/
    var itemss={};
    itemss.etype=$('.etype').html();
    $(".m-topic .topic-select .sem").bind('click', function() {
        $(this).parent().children().removeClass("topic-right");
        $(this).toggleClass("topic-right");
        $(this>+'.sem_i').css('color','#fff');
        /*var index = $(".m-topic .topic-select li").index(this);*/
        itemss.select=$(this).children().children().html();
    })

    $(".m-topic .topic-select .dum").bind('click', function() {
        $(this).toggleClass("topic-right");
        $(this>+'.dum_i').css('color','#fff');
        /*var index = $(".m-topic .topic-select li").index(this);*/
        itemss.select='';
        $('#now >.topic-select> .topic-right').each(function(index) {
            itemss.select=itemss.select+$(this).children().children().html();

        });
    })

    $('.view_analysis').bind('click',function(){
            $('.view_analysis').css('display','none');
            $('.u-hr').css('display','block');
            $('.topic-answer').css('display','block');
            $('.f-cor-green').html(itemss.select);
            /*$('.f-cor-pink').html=item.select;*/
            $(".m-topic .topic-select .sem").unbind('click');
            $(".m-topic .topic-select .dum").unbind('click');
    })



</script>
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