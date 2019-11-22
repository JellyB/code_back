<!DOCTYPE html>
<html>
<head>
    <title>用户登录</title>
<#include "head.ftl"/>
    <style>
        .wrap {
            margin-top: 0px;
        }

        html {
            background-color: '';
        }

    </style>
</head>
<body class="reg-login">
<div class="header-wrap" style="position:inherit">
    <a href="/pc/wechat/index"><i class="ico icon-chevron_left">&nbsp;&nbsp;返回</i></a>
</div>
<div class="wrap">
    <div class="reg-login-con pt-reg">
        <form id="thisform" action="/pc/wechat/index" method="PUT">
            <div class="PnBox2">
                <i class="ico icon-user"></i><input type="text" class="Pn" id="username" name="username"
                                                    placeholder="用户名／邮箱／手机号">
            </div>
            <div class="PnBox2 mt5">
                <i class="ico icon-password"></i><input type="password" id="password" name="password" class="Pn"
                                                        placeholder="密码">
            </div>
            <p class="tips"></p>
            <a href="javascript:void(0)" class="reg-login-btn mt-login" onclick="tologin()">登录</a>
        </form>
        <a href="/pc/user/register" class="bd-btn">我是新用户</a>
    </div>
</div>
</body>
</html>
<script type="text/javascript">
    function tologin() {
        var username = $('#username').val();
        var password = $('#password').val();
        if (username == '' || password == '') {
            $(".tips").html('用户名或密码不能为空');
            $(".PnBox2").css("border-color", "#e56315");
            $(".PnBox2 .ico").css("color", "#e56315");
        } else {
            $.ajax({
                url: '/pc/user/login',
                type: 'POST',
                dataType: 'json',
                data: {'account': username, 'password': password},
                success: function (data) {
                    if (data.code == 1000000) {
                        window.location="/pc/wechat/index";
                    } else {
                        $(".tips").html(data.message);
                        $(".PnBox2").css("border-color", "#e56315");
                        $(".PnBox2 .ico").css("color", "#e56315");
                    }
                }
            })
        }
    }

</script>