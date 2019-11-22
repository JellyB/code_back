    //防止出错
    window.onerror=function(){return true;}
    //时间日期
    myDate = new Date();
    day=myDate.getDate();
    answer=answer();

$(".back").click(function(){
    if(answer){
        $(".tips-txt").html('是否放弃查看答案');
        $(".tips-goon").html('继续查看');
        $(".tips-give").html('放弃');
    }else{
        $(".tips-txt").html('是否放弃本次练习');
        $(".tips-goon").html('继续答题');
        $(".tips-give").html('放弃');
    }

    $(".m-asktips").fadeIn("slow");
    $(".asktips-bg").fadeIn("slow");
    $(".tips-give").attr("class","tips-back");


})


// 关闭按钮
$(".m-tips").on("click", ".tips-fock", function() {
    $(".m-tips").hide();
});

// 对话框关闭按钮(右按钮)
$(".m-asktips").on("click", ".tips-goon", function() {
    $(".m-asktips").fadeOut("slow");
    $(".asktips-bg").fadeOut("slow");
});
//对话框关闭按钮(左按钮)
$(".m-asktips").on("click", ".tips-give", function() {
    $(".m-asktips").fadeOut("slow");
    $(".asktips-bg").fadeOut("slow");

});
//对话框关闭按钮(左按钮,返回首页)
$(".m-asktips").on("click", ".tips-back", function() {
    $(".m-asktips").fadeOut("slow");
    $(".asktips-bg").fadeOut("slow");

    location.href = back_index();


});



//点选

 etype=$('.etype').html();

arr=new Array();
var pageNum = $(".g-bd .view-cnt .view-page").length;
var pageIndex = $(".g-bd .view-cnt .view-page").index() + 1;
var firstTime=true;
$(".g-nav .nav-num .num-on").text(pageIndex);

$(".g-nav .nav-num .num-total").text(pageNum);



//单选类型
    var dataArr = [];
    var tempTime=[];

    $(".sem").bind('click', function() {
    //开始时间
    var starttime=new Date($("#starttime").val()).getTime();

    $(this).parent().children().removeClass("topic-right");
    $(this).addClass("topic-right");
    $(this>+'.sem_i').css('color','#fff');
    var index = $(".m-topic .topic-select li").index(this);
    select=$(this).children().children().html();
    arr[pageIndex]=select;
    $.cookie('select_arr',arr);
     var sel=select.replace(/\s+/g, "");
       sel=sel.replace("A",1);
       sel=sel.replace("B",2);
       sel=sel.replace("C",3);
       sel=sel.replace("D",4);
       sel=sel.replace("E",5);
       sel=sel.replace("F",6);
       sel=sel.replace("G",7);
       sel=sel.replace("H",8);
        //新增
     var paperid=$("#paperid").val();
     var qid=$("#"+(pageIndex*1-1)+"_qid").val();
     var answer=$("#"+(pageIndex*1-1)+"_answer").val();
     var correct=2;
        if(sel==""){
            correct=0;
        } else if(sel==answer){
            correct=1;
        }
        var limittime=0;
        /**
         *
         * 获取时间
         */
        var currentTime=new Date().getTime();
          if(pageIndex==1){
              limittime=currentTime-starttime;
          }else{
              limittime=(currentTime-tempTime[pageIndex-2])*1;
          }
          tempTime.push(currentTime);
        if(limittime<0){
            limittime=1000;
        }
  var row={"questionId":qid,"answer":sel,"time":limittime,"correct":correct}
        dataArr.push(row);
        $.cookie('data_arr',dataArr)
    if(pageIndex==pageNum){
       var answers= JSON.stringify(dataArr)
        var this_href= this_submit();
                $('.m-poptips-1').fadeIn("fast");
                $.ajax({
                    url: this_href+paperid,
                    type: 'PUT',
                    dataType: 'json',
                    data: {'arr': arr,'code':1,'answers':answers},
                    success:function(data){
                        if(data.code==1000000){
                            $('.m-poptips-1').fadeOut("slow");
                            location.href="/pc/wechat/cards/"+paperid
                        }else{
                            $('.m-poptips-1').fadeOut("slow");
                            $('.m-poptips-2').fadeIn('slow');
                        }
                    },
                    error:function() {
                        /*$('.m-poptips-1').fadeOut("slow");
                        $('.m-poptips-outtime').fadeIn('slow');*/

                    }
                })

                // })
             /* webkit内核浏览器的实现，例如safari */
            $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-webkit-transform' : 'translate3d(0%, 0px, 0px)' , '-webkit-transition' : '-webkit-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});

              /* IE浏览器的实现 */
            $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-ms-transform' : 'translate3d(0%, 0px, 0px)' , '-ms-transition' : '-ms-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
             /* firefox 的实现 */
            $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-moz-transform' : 'translate3d(0%, 0px, 0px)' , '-moz-transition' : '-moz-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});

    }else{
        /* webkit内核浏览器的实现，例如safari */
        $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-webkit-transform' : 'translate3d(-100%, 0px, 0px)' , '-webkit-transition' : '-webkit-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});

          /* IE浏览器的实现 */
        $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-ms-transform' : 'translate3d(-100%, 0px, 0px)' , '-ms-transition' : '-ms-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
         /* firefox 的实现 */
          $(".g-bd .view-cnt .view-page").eq(pageIndex-1).css({'-moz-transform' : 'translate3d(-100%, 0px, 0px)' , '-moz-transition' : '-moz-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
    }
    //修改当前页的class和id的参数
        $(".g-bd .view-cnt .view-page ").eq(pageIndex-1).removeClass("now");
        $(".g-bd .view-cnt .view-page ").eq(pageIndex).addClass('now');

        $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex-1).removeAttr("id");
        $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex).attr("id",'now');

       /* webkit内核浏览器的实现，例如safari */
        $(".g-bd .view-cnt .view-page").eq(pageIndex).css({'-webkit-transform' : 'translate3d(0%, 0px, 0px)' , '-webkit-transition' : '-webkit-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
          /* IE浏览器的实现 */
        $(".g-bd .view-cnt .view-page").eq(pageIndex).css({'-ms-transform' : 'translate3d(0%, 0px, 0px)' , '-ms-transition' : '-ms-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
         /* firefox 的实现 */
        $(".g-bd .view-cnt .view-page").eq(pageIndex).css({'-moz-transform' : 'translate3d(0%, 0px, 0px)' , '-moz-transition' : '-moz-transform 350ms cubic-bezier(0.42, 0, 0.58, 1.0)'});
        $(".g-nav .nav-num .num-on").text(pageIndex);
    if(pageIndex === pageNum){
        pageIndex = pageNum;
    }else{
        pageIndex += 1;
        $(".g-nav .nav-num .num-on").text(pageIndex);

    }
    //材料题
    $('.aly_'+pageIndex).prevAll().hide();
    $('.aly_'+pageIndex).show();

});
    //开始时间
    var starttime=new Date($("#starttime").val()).getTime();

//多选类型

$(".dum").bind('click', function() {
    //$(this).parent().children().removeClass("topic-right");

    $(this).toggleClass("topic-right");
    var index = $(".m-topic .topic-select li").index(this);

    select='';
    $('#now >.topic-select> .topic-right').each(function(index) {
        arr[pageIndex]='';
            select=select+$(this).children().children().html();
            arr[pageIndex]=select;
    });
    console.log(arr);
    $.cookie('select_arr',arr);
    var sel=arr[pageIndex].replace(/\s+/g, "");
    sel=sel.replace("A",1);
    sel=sel.replace("B",2);
    sel=sel.replace("C",3);
    sel=sel.replace("D",4);
    sel=sel.replace("E",5);
    sel=sel.replace("F",6);
    sel=sel.replace("G",7);
    sel=sel.replace("H",8);
    //新增
    var paperid=$("#paperid").val();
    var qid=$("#"+(pageIndex*1-1)+"_qid").val();
    var answer=$("#"+(pageIndex*1-1)+"_answer").val();
    answer=answer.replace(/,/,"");
    //获取做题时间
    var limittime=0;
    var currentTime=new Date().getTime();
    if(pageIndex==1){
        limittime=currentTime-starttime;
    }else{
        limittime=(currentTime-tempTime[pageIndex-2])*1;
    }
    tempTime.push(currentTime);
    if(limittime<0){
        limittime=1000;
    }

    var correct=2;
    if(sel==""){
        correct=0;
    } else if(sel==answer){
        correct=1;
    }
    var row={"questionId":qid,"answer":sel,"time":limittime,"correct":correct};
    if(dataArr.length<=0){
        dataArr.push(row);
    }else{
        var bln=0;
        for(var i=0;i<dataArr.length;i++){
            if(dataArr[i].questionId==qid){
                dataArr[i].answer=sel;
                dataArr[i].time=dataArr[i].time+limittime;
                if(sel==""){
                    dataArr[i].correct=0;
                }else if(sel==answer){
                    dataArr[i].correct=1;
                }else{
                    dataArr[i].correct=2;
                }
                bln=1;
                break;
            }
        }
        if(bln==0){
            dataArr.push(row);
        }
    }
    if(pageIndex==pageNum){
        var answers= JSON.stringify(dataArr)
        var this_href=this_submit();

           $('.jiaojuan').click(function(){
               $('.m-poptips-1').fadeIn("fast");
                $.ajax({
                    url: this_href+paperid,
                    type: 'PUT',
                    dataType: 'json',
                    data: {'arr': arr,'code':1,'answers':answers},
                    success:function(data){
                       console.log(data.code);
                        if(data.code==1000000){
                            $('.m-poptips-1').fadeOut("slow");
                            location.href="/pc/wechat/cards/"+paperid;
                        }else{
                            $('.m-poptips-1').fadeOut("slow");
                            $('.m-poptips-2').fadeIn('slow');
                        }
                    },
                    error:function() {
                        $('.m-poptips-1').fadeOut("slow");
                        $('.m-poptips-outtime').fadeIn('slow');

                    }
                })
                 })


    }

    $(".g-nav .nav-num .num-on").text(pageIndex);
    if(pageIndex === pageNum){
        pageIndex = pageNum;
    }else{
        $(".g-nav .nav-num .num-on").text(pageIndex);

    }
    //材料题
    $('.aly_'+pageIndex).prevAll().hide();
    $('.aly_'+pageIndex).show();

});


//材料题高度
// window.onload = function() {
// 	var hall=$(window).height();

// 	$('.mata-cnt').css('height',(hall/2-40) + "px");
// }

//拖拽效果4
var $dragBtn = document.querySelector(".mata-slide");//滑块
cnt=document.querySelector(".mata-cnt"),//下面盒子
    nav=document.querySelector(".g-nav"),
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
            cnt.style.height = client - touchY-nav.offsetHeight+ "px";
            document.querySelector(".now").style.height = touchY-$dragBtn.offsetHeight+ "px";
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


var this_aly=this_aly();
//滑动

//左右拖动
$(".view-cnt,.m-material").swipe(
        {
             swipeLeft: function (event, direction, distance, duration, fingerCount) {
                    if($.cookie('select_arr')){
                        arr[pageIndex]=$.cookie('select_arr').split(",")[pageIndex];
                    }
                	if(pageIndex === pageNum){
            	        pageIndex = pageNum;
            	    }else{
            	        pageIndex += 1;
                        // 弹窗关闭后不再弹出
                        ad_last=adlast();

                        if(pageIndex === pageNum){
                            $("#orther-5").show();
                            $('.g-bd').css('padding-bottom','.5rem')

                            $('.m-appad-03').hide();


                            if($.cookie('clean_last')==day){
                                $(".ad-last").hide();
                            }else{
                                $(".ad-last").show();
                            }
                            if(answer!==1){
                             $('.g-bd').css('padding-bottom','0rem')
                            }
                            $('.jiaojuan').show()
                         }else{
                            $("#orther-5").hide();
                          //   $('.m-appad-03').show();
                            $(".ad-last").hide();
                           if(ad_last==day){
                                $('.g-bd').css('padding-bottom','0')
                            }
                    }

            	        $(".g-nav .nav-num .num-on").text(pageIndex);

                         //材料题
                         $('.aly_'+pageIndex).prevAll().hide();
                         $('.aly_'+pageIndex).show();



            	    }
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex-2).removeClass("now");
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex-1).addClass('now');
                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex-2).removeAttr("id");
                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex-1).attr("id",'now');
                    if(this_aly){
                        $(".g-bd .view-cnt .view-page .now_box").eq(pageIndex-2).removeAttr("id");
                        $(".g-bd .view-cnt .view-page .now_box").eq(pageIndex-1).attr("id",'now_box');
                    }


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
                    if($.cookie('select_arr')){
                        arr[pageIndex]=$.cookie('select_arr').split(",")[pageIndex];
                    }

                     $("#orther-5").hide();

                     if(Number($.cookie('clean_down'))!==day){
                       $('.m-appad-03').show();
                     }else{
                        $('.m-appad-03').hide();
                     }
                     $(".ad-last").hide();
                     $('.jiaojuan').hide()

                     if((Number($.cookie('clean_down'))==day)||($.cookie('userid'))){
                                $('.g-bd').css('padding-bottom','0rem')
                            }

                    if(pageIndex<=1){
        	        	pageIndex=1;
        	        }else{
        	        	 pageIndex -= 1;

            	        $(".g-nav .nav-num .num-on").text(pageIndex);

                        //材料题
                        $('.aly_'+pageIndex).nextAll().hide();
                         $('.aly_'+pageIndex).show();


            	    }
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex).removeClass("now");
                    $(".g-bd .view-cnt .view-page ").eq(pageIndex-1).addClass('now');

                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex).removeAttr("id");
                    $(".g-bd .view-cnt .view-page .contbox").eq(pageIndex-1).attr("id",'now');

                    if(this_aly){
                        $(".g-bd .view-cnt .view-page .now_box").eq(pageIndex).removeAttr("id");
                        $(".g-bd .view-cnt .view-page .now_box").eq(pageIndex-1).attr("id",'now_box');
                    }

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

//重新提交答案
$('#resubmit,#resubmit_outtime').click(function(){
       $('.m-poptips-2').fadeOut('fast');
       $('.m-poptips-outtime').fadeOut('fast');
       $('.m-poptips-1').fadeIn("slow");
        var this_href=this_submit()
        var answers= JSON.stringify(dataArr);
    var paperid=$("#paperid").val();
        $.ajax({
            url: this_href+paperid,
            type: 'PUT',
            dataType: 'json',
            data: {'arr': arr,'code':1,'answers':answers},
            success:function(data){
                if(data.code==1000000){
                    $('.m-poptips-1').fadeOut("slow");
                    location.href="/pc/wechat/cards/"+paperid
                }else{
                    $('.m-poptips-1').fadeOut("slow");
                    $('.m-poptips-2').fadeIn('slow');
                }
            },
            error:function() {
                    $('.m-poptips-1').fadeOut("slow");
                    $('.m-poptips-outtime').fadeIn('slow');

                    }
        })
})
//刷新当前页
$("#refresh").click(function(){
    location.reload()
})

//样式去除
$("#_baidu_bookmark_start_50").parent().hide();
//选项高度
var opt_num=$('.select-right').length;
for(i=0;i<opt_num;i++){
    $('.select-left').eq(i).attr('style','line-height:'+$('.select-right').eq(i).height()+'px')
}
