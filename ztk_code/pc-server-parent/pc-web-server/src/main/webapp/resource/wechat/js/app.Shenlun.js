
//申论
window.onerror=function(){return true;} 
//拖拽效果
var firstTime=true;
 var $dragBtn = document.querySelector(".mata-slide");//滑块
cnt=document.querySelector(".mata-cnt"),//下面盒子
    nav=document.querySelector(".g-nav3"),
    client=document.documentElement.clientHeight;


var constVar = {
    config: {
        stopStep: 4
    }
};
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
            cnt.style.height = client - touchY-$dragBtn.offsetHeight+ "px";
            document.querySelector(".now").style.height = touchY -nav.offsetHeight + "px";
        }
    }
};
$dragBtn.addEventListener("touchstart", EventHandler.touchstart, false);
$dragBtn.addEventListener("touchmove", EventHandler.touchmove, false);
var Proxy = (function () {
    var timer = null;
    return {
        throttle: function (ev) {
            var first=true;
                ev.preventDefault();
                ev.stopPropagation();
                window.clearTimeout(timer);
                if (firstTime) {
                    if(first){
                        first = false;
                        Touch.move.apply(this, arguments);
                    }

            } else if (!timer) {
                timer = window.setTimeout(Touch.move.bind(this,ev,function(){
                    timer=null;
                }), constVar.config.stopStep);
            }
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



//左右拖动
var pageNum = $(".g-bd .view-cnt .view-page").length;
var pageIndex = $(".g-bd .view-cnt .view-page").index() + 1;
$(".g-nav3 .nav-num .num-on").text(pageIndex);
$(".g-nav3 .nav-num .num-total").text(pageNum);

$(".view-cnt").swipe( 
        {  
             swipeLeft: function (event, direction, distance, duration, fingerCount) {
                	if(pageIndex === pageNum){
            	        pageIndex = pageNum;
            	    }else{
            	        pageIndex += 1;
            	        $(".g-nav3 .nav-num .num-on").text(pageIndex);

                         //材料题
                         $('.aly_'+pageIndex).prevAll().hide();
                         $('.aly_'+pageIndex).show();                        
            	    }
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex-2).removeClass("now");
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex-1).addClass('now');
                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex-2).removeAttr("id");
                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex-1).attr("id",'now');

                    $(".g-bd .view-cnt .view-page .m-material").eq(pageIndex-2).removeAttr("id");
                    $(".g-bd .view-cnt .view-page .m-material").eq(pageIndex-1).attr("id",'now_any');
                    $(".now").scrollTop(0);

                     /* webkit内核浏览器的实现，例如safari */ 
                	$(".g-bd .view-cnt .view-page").eq(pageIndex-2).css({'-webkit-transform' : 'translate3d(-100%, 0px, 0px)' , '-webkit-transition' : '-webkit-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
               		$(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-webkit-transform' : 'translate3d(0%, 0px, 0px)' , '-webkit-transition' : '-webkit-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                     /* IE 的实现 */ 
                  $(".g-bd .view-cnt .view-page").eq(pageIndex-2).css({'-ms-transform' : 'translate3d(-100%, 0px, 0px)' , '-ms-transition' : '-ms-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                    $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-ms-transform' : 'translate3d(0%, 0px, 0px)' , '-ms-transition' : '-ms-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                     /* firefox 的实现 */ 
                  $(".g-bd .view-cnt .view-page").eq(pageIndex-2).css({'-moz-transform' : 'translate3d(-100%, 0px, 0px)' , '-moz-transition' : '-moz-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                    $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-moz-transform' : 'translate3d(0%, 0px, 0px)' , '-moz-transition' : '-moz-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});

                    //材料题高度
                        var hall=$(window).height();
                        $('.mata-cnt').css('height',2+ "rem");
                        //$('.now').height(hall-$('.mata-cnt').height()-48);
                        if($('.mata-cnt').height()>0){
                        document.querySelector(".now").style.height = (document.body.clientHeight - $(".mata-cnt").height() - 70) + "px";
                        }
                        firstTime=true;
                        
               },
               swipeRight : function (event, direction, distance, duration, fingerCount) {    
                    if(pageIndex<=1){
        	        	pageIndex=1;
        	        }else{
        	        	 pageIndex -= 1;     	
            	        $(".g-nav3 .nav-num .num-on").text(pageIndex);      
            	    }
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex).removeClass("now");
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex-1).addClass('now');

                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex).removeAttr("id");
                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex-1).attr("id",'now');

                    $(".g-bd .view-cnt .view-page .m-material").eq(pageIndex).removeAttr("id");
                    $(".g-bd .view-cnt .view-page .m-material").eq(pageIndex-1).attr("id",'now_any');
                    $(".now").scrollTop(0);
                     /* webkit内核浏览器的实现，例如safari */ 
                    $(".g-bd .view-cnt .view-page").eq(pageIndex).css({'-webkit-transform' : 'translate3d(100%, 0px, 0px)' , '-webkit-transition' : '-webkit-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                  	$(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-webkit-transform' : 'translate3d(0%, 0px, 0px)' , '-webkit-transition' : '-webkit-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                      /* IE 的实现 */ 
                    $(".g-bd .view-cnt .view-page").eq(pageIndex).css({'-ms-transform' : 'translate3d(100%, 0px, 0px)' , '-ms-transition' : '-ms-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                    $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-ms-transform' : 'translate3d(0%, 0px, 0px)' , '-ms-transition' : '-ms-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                     /* firefox 的实现 */ 
                     $(".g-bd .view-cnt .view-page").eq(pageIndex).css({'-moz-transform' : 'translate3d(100%, 0px, 0px)' , '-moz-transition' : '-moz-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                    $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-moz-transform' : 'translate3d(0%, 0px, 0px)' , '-moz-transition' : '-moz-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
                        //材料题高度
                        var hall=$(window).height();
                        $('.mata-cnt').css('height',2+ "rem");
                       // $('.now').height(hall-$('.mata-cnt').height());
                        if($('.mata-cnt').height()>0){
                        document.querySelector(".now").style.height = (document.body.clientHeight - $(".mata-cnt").height() - 70) + "px";
                        }    
                        firstTime=true;             
                }
        }        
 ); 


window.onload = function() { 
     document.querySelector(".now").style.height = (document.body.clientHeight - $(".mata-cnt").height() - 70) + "px";
 }


var _hmt = _hmt || [];
(function() {
  var hm = document.createElement("script");
  hm.src = "//hm.baidu.com/hm.js?1d39a05aeff8b455a179675a2ab26c58";
  var s = document.getElementsByTagName("script")[0]; 
  s.parentNode.insertBefore(hm, s);
})();
