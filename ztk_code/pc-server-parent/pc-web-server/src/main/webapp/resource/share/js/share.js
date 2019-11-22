/**
 * Created by wsc on 2016/9/7.
 */
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
    $('.now').height(hall-$('.mata-cnt').height()+48);
    document.querySelector(".now").style.height = (document.body.clientHeight - $(".mata-cnt").height()+70) + "px";

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
    document.querySelector(".now").style.height = (document.body.clientHeight - $(".mata-cnt").height() - 70) + "px";
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
    touchmove: function () {
        Proxy.throttle.apply(this, arguments);
    },
    touchstart:function(ev){
        ev.preventDefault();
    },
    scroll:function(opts){
        var touchY=opts.touchY || 0;
        if(touchY>=130) {
            cnt.style.height = client - touchY-40+ "px";
            document.querySelector(".now").style.height = touchY-$dragBtn.offsetHeight+ "px";
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