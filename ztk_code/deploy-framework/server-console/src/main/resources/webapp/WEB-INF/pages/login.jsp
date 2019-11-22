<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<script src="/js/tw.new.index.min.0.0.14.js" type="text/javascript"></script>
<script>(function(){var d=new Date(),gc=function(s){var t,r=new RegExp("(^| )"+s+"=([^;]*)(;|$)","gi");if(t=r.exec(unescape(document.cookie)))return(t[2]);return null;},fix=function(k){return k>10?k:('0'+k);},suv=gc('SUV');if(!suv){var random=''+Math.floor(Math.random()*10000)+'0000',str=d.getFullYear().toString().substr(2)+fix(d.getMonth()+1)+fix(d.getDate())+fix(d.getHours())+fix(d.getMinutes())+fix(d.getSeconds())+random.substr(0,4),exp=new Date();exp.setTime(exp.getTime()+100*365*24*60*60*1000);document.cookie='SUV'+"="+escape(str)+"; expires="+exp.toGMTString()+"; path=/; domain=sohu.com";}var a=[],n=document.createElement('script'),r;a.push('t='+d.getTime());if(typeof(r=document.referrer)=='string'&&r.length>0){a.push('r='+encodeURIComponent(r));}n.src='http://z.t.sohu.com/pv.gif?'+a.join('&');document.getElementsByTagName('head')[0].appendChild(n);})();</script>
<title>发布系统</title>
<style type="text/css">
html {min-width:972px;overflow-y:scroll;*margin-right:1px}
body {margin:0 0 1px}
h1,h2,h3,h4,h5,h6,p,ul,ol,li,dl,dt,dd,th,td,form,fieldset,legend,button,input,textarea,blockquote,address,pre {margin:0;padding:0}
table {border-collapse:collapse;border-spacing:0}
img,fieldset,hr,iframe {border:0}
img {vertical-align:middle}
a,del,ins {text-decoration:none}
li {list-style:none}
q:before,q:after {content:''}
sup,sub {vertical-align:baseline}
body,input,button,textarea,select {font:normal 12px/1.667 Tahoma,Arial,"\5b8b\4f53",sans-serif;color:#333}
h1,h2,h3,h4,h5,h6,b {font-size:1em;font-weight:normal}
textarea {line-height:1.4;resize:vertical;max-height:348px}
i,cite,em,var,dfn,address {font-style:normal}
a {text-decoration:none;outline:0}/*exp~*/
a:link,a:visited {color:#3284ae}
a:hover {text-decoration:underline}
a,label {cursor:pointer}
embed,object {display:block;outline:0}

.c0,.main,.header .c0 {*zoom:1}
.c0:after,.main:after,.header .c0:after{overflow:hidden;clear:both;display:block;height:0;content:"\20"}

body {background:#ccecff}
.bgs {background:url(//s1.cr.itc.cn/img/i2/t/131.jpg) no-repeat top center;height:410px;margin:0 auto -410px}
.header .c0,.footer .c0,.bdy .c0,.B1,.B2 {width:968px;margin:0 auto}

/* header */
.header {height:103px}
.header .c0 {width:960px;height:100%}
.logo {width:174px;height:53px;margin:30px 0 -53px 7px;float:left}
.logo .png {height:100%;margin:0 0 -53px;background:url(//s1.cr.itc.cn/img/i2/t/130.png) no-repeat;display:block}
.logo a {overflow:hidden;display:block;height:100%;text-indent:-99em}

/*layout*/
.B1,.B2,.B4 {height:10px;margin:0 auto;clear:both;overflow:hidden;font-size:0;line-height:0}
.Bi,.bdy .c0 {background:url(//s1.cr.itc.cn/img/i2/t/132.png) no-repeat}
.Bi {display:block;width:300%;height:100%}/*fit*/
.B2 .Bi {margin:0 0 0 -100%;background-position:0 bottom}

.bdy .c0 {background-position:100% 0;background-repeat:repeat-y;-webkit-background-size:300% 100%;_background:none!important}
.B0 {display:none;_overflow:hidden;_display:inline;_zoom:1;_width:100%;_height:32766px;_margin:0 0 -32766px}
.B0 .Bi {display:none;_display:block;_margin:0 0 0 -200%}
.B1 {display:none}
.B4 {z-index:200;position:relative;margin:0 0 -10px;background:url(//s1.cr.itc.cn/img/t/518.png) no-repeat}

/*slogan*/
.slogan {height:350px;padding:4px 0 0;margin:0 4px;*zoom:1;position:relative;z-index:100}
.slobgs {overflow:hidden;width:960px;margin:0 auto -350px;height:350px}
.slobgs .bg1 {background-repeat:no-repeat;height:117px}
.slobgs .bg2 {background-repeat:no-repeat;height:116px}
.slobgs .bg3 {background-repeat:no-repeat;height:117px}
.slobgs img {vertical-align:top}
.passBy{ height:350px; margin-top:-350px;position:relative}
.passBy .imgMask{margin-left:100px;overflow:hidden;height:350px; border-left:5px solid #000}

.login {width:250px;height:319px;padding:0 26px;margin:18px 0 0 100px}
.loginBg {height:319px;overflow:hidden;margin:0 -26px -319px}
.loginBg .png {height:319px;margin:0 0 -319px;background:url(//s2.cr.itc.cn/i/3/015.png) no-repeat left top;width:280px}

.regbtn {margin:20px auto 8px;width:226px;position:relative}
.regbtn a {display:block;background-image:url(//s2.cr.itc.cn/img/i2/t/141_ie.png);background-repeat:no-repeat;width:226px;height:62px;text-indent:-999em}
.regbtn a:hover {background-position:0 -64px;text-decoration:none}
.regbtn a:focus,.regbtn a:active {background-position:0 -128px}

.logform {overflow:hidden;background:url(//s1.cr.itc.cn/img/i2/t/161.png) no-repeat top center;_background:url(//s1.cr.itc.cn/img/i2/t/161_ie.png) no-repeat top center; height:143px}
.logform .tip {overflow:hidden;height:28px;line-height:31px;color:#666;word-wrap:break-word;word-break:break-all;white-space:nowrap}
.logform .tip em {color:#f00;background:none;word-wrap:break-word;word-break:break-all}
.logform .frm {margin:0 0 5px;*margin-bottom:6px;_margin-bottom:4px}
.logform .frm .txt {width:226px;height:33px;display:block;*zoom:1}
.logform .frm .txt input {border:none;font-size:12px;padding:4px 7px;width:213px;margin:1px;height:24px;line-height:24px;background:url(//s2.cr.itc.cn/i/3/016.png) no-repeat;outline:0}
.logform .frm .done {background-position:0 -40px!important}
.ppsel {position:absolute;border:1px solid #6a9ab2;background:#fff;padding:1px;margin:63px 0 0 0;width:221px;margin-left:1px!important;margin-top:63px!important;background:#fff}
.ppsel ul li.tit {background:#f0f0f0;color:#999;padding:4px 6px}
.ppsel ul li a {padding:3px 6px;display:block;*zoom:1;color:#656565}
.ppsel ul li a.active,.ppsel ul li a:hover {background:#48b5ec!important;color:#fff!important;text-decoration:none}

.ppWaitMsg {text-align:center;padding-top:40px;width:228px;height:111px}

.logbtn {overflow:hidden;display:block;float:left;background-image:url(//s1.cr.itc.cn/img/i2/t/156.png);background-repeat:no-repeat;width:82px;height:34px;text-indent:-99em}
a.logbtn:hover {background-position:0 -34px}
a.logbtn:focus,a.logbtn:active {background-position:0 -68px}
a.bindMsn{margin-left:6px;background:url(//s2.cr.itc.cn/img/i2/065.png) no-repeat left center;_background:url(//s2.cr.itc.cn/img/i2/065_ie.png) no-repeat left center;padding:5px 0 5px 25px;display:inline-block}
.fucrem {display:inline-block;background:url(//s1.cr.itc.cn/img/i2/t/155.png) no-repeat 0 0;_background:url(//s1.cr.itc.cn/img/i2/t/155_ie.png) no-repeat 0 0;padding-left:18px;margin:0 6px 0 9px;*margin-left:6px;height:16px;line-height:16px;color:#666!important}
a.fucrem:hover {text-decoration:none;background-position:0 -20px}
.fucrem,.logform .frm .fuc0 {margin-top:16px}

.blur {color:#999}


.footer .c0 {text-align:center;line-height:20px;color:#666;padding:10px 0 15px}
.footer .c0 a {color:#666}
    


</style>
        </head>

<body>
<div class="bgs"><p class="bg1"></p><p class="bg2"></p></div>

<div class="header">
        <div class="c0">

    </div>
</div>

<div class="bdy">
    <p class="B1"><i class="Bi"></i></p>
    <div class="c0">
        <p class="B0"><i class="Bi"></i></p>
        <!--S 登录区域-->
        <p class="B4"></p>
        <div class="slogan row">
        
            <div class="login">
			<legend><font color="#A3A3A3" size="4"><strong><em>部署系统</em></strong></font></legend>
                <div class="logform tw_new_login" id="ppcontid">
                   <form action="" method="post">
                  username: <input type="text" name="username" value=""><br>
                  password:  <input type="text" name="pwd" value=""><br>
                    <input type="submit" />
                   </form>
                </div><br/>



            </div>

        <!--E 登录区域-->

        <!--S 内容区域 -->

        <!--E 内容区域 -->
        
    </div>
    <p class="B2"><i class="Bi"></i></p>
</div>


<div class="footer">
    <div class="c0">
        <a href="" target="_blank">帮助信息</a> - <a href="" target="_blank">联系我们</a> - <a href="">意见建议</a> - <a href="">举报</a> - <br>Copyright <b class="copy">&copy; </b> Inc. All Rights Reserved.<a class="uline" href="" target="_blank">版权所有</a>
    </div>
</div>
<script type="text/javascript">

</script>

</body>

</html>
