<!DOCTYPE html>
<head>
<title>我的记录</title>
    <#include "head.ftl"/>
</head>
<body>
<div class="header-wrap">
    <a href="/pc/wechat/index"><i class="ico icon-chevron_left"></i></a>我的记录
    <a style="right:15px;position:absolute;" href="/pc/user/logout">注销</a>
</div>
<div class="wrap" >
    <#include "download_app.ftl"/>
    <ul class="rec-list" style="background:#F5F5F5">
        <#if list_data?exists>
        <#list list_data.resutls as lresult>
            <li>
                <a href="/pc/wechat/cards/${lresult.id?c}">
                    <div class="fr rec-num">
                        <p class="link-grey f20"><i class="f25 link-yel mr5">${lresult.rcount}</i>/&nbsp;${lresult.rcount+lresult.wcount+lresult.ucount}</p>
                    </div>
                    <h3>${lresult.name}</h3>
                    <div class="clearfix link-grey mt10 con">
                        <span><i class="ico icon-clock"></i>${lresult.createTime?number_to_datetime?string("yyyy-MM-dd HH:mm:ss")!}</span>
                    </div>
                </a>
            </li>
        </#list>
        <#else>
            <div class="record-tip">
                <p>还没有记录，赶快去刷题吧~</p>
                <div class="pre-ex">
                    先来5道
                </div>
            </div>
        </#if>
    </ul>

    <div class="no-more" style='display:none'><img src="http://tiku.huatu.com/cdn/images/vhuatu/tiku/wechat/nomre-ico.png">再拉也没有了~</div>


</div>

<script type="text/javascript">
    $(".pre-ex").click(function(){
        location.href="/pc/wechat/exercise?pointid=392";
    })

    //推荐弹窗关闭
    myDate = new Date();
    day=myDate.getDate();
    $(".appad-close").click(function(){
        $(".m-appad-02").hide();
        $.cookie('clean-re', day); // 存储 cookie
    })

    //关闭弹窗后,推荐广告不再在本次操作弹出
    var c=$.cookie('clean-re'); // 读取 cookie
    if(c==day){
        $(".m-appad-02").hide();
    }else{
        $(".m-appad-02").show();
    }

    $(window).scroll(function(){
        var scrollTop = $(this).scrollTop();
        var scrollHeight = $(document).height();
        var windowHeight = $(this).height();
        if(scrollTop + windowHeight == scrollHeight){
            $(".no-more").fadeIn('slow');
        }else{
            $(".no-more").fadeOut('slow');
        }
    });

</script>
</body>
</html>