<!DOCTYPE html>
<head>
<title>新用户注册</title>
    <#include "head.ftl"/>
<style>
    .wrap{
        margin-top:0px;
    }
    .box_err{
        border:1px solid #e56315;
        box-shadow:0 0 15px #e56315;
    }
</style>
</head>
<body class="reg-login">
<div class="header-wrap" style="position:inherit">
    <a href="/pc/wechat/index"><i class="ico icon-chevron_left">&nbsp;&nbsp;返回</i></a>
</div>
<div class="wrap">
    <div class="reg-login-con" >
        <form id="frm_reg" action="/pc/user/register" method="post" >
            <div id="phone_box" class="comm-box mt5">
                <input type="tel" class="mob-input" id="mobile" name="phone" placeholder="手机号" maxlength="11" >
            </div>
            <p id="tel_tip" class="reg-tip phone-tip">&nbsp;</p>
            <div class="PnBox_code mt5">
                <input  type="text" class="Pn_code" id="send-number" name="verification" placeholder="手机验证码" maxlength="6">
                <input type="button" id="btn" value="获取验证码"  class="sendtoo getcode"/>
            </div>
            <p id="valid_tip" class="reg-tip code-tip">&nbsp;</p>
            <div class="tips"><span id="send-numberTip"></span></div>
            <div id="pw" class="PnBox mt5">
                <input type="password" class="Pn" id="password" name="password" placeholder="密码6-16位字母或数字" minlength="6" maxlength="16">
            </div>
            <p id="pw_tip" class="reg-tip password-tip">&nbsp;</p>
            <input type="button"  class="reg-login-btn mt-reg ajaxpost" value="注册" onclick="validate_form()"/>
        </form>
        <a href="/pc/user/login" class="bd-btn">我是老用户</a>
    </div>
    <div id="reg_tip" class="box_tip" style="display:none;">
        <div id="reg_tip_box" class="regafter"></div>
    </div>
</div>
<script src="http://ns.huatu.com/pc/wechat/js/register.js"></script>
</body>
</html>
