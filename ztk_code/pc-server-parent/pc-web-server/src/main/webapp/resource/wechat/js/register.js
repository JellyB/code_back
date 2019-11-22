var value={tel:'',vl:'',pw:''};
        $('#btn').blur(function() {
            $('#btn').css('background','#8CCC0F');
        })
        $('#btn').click(function() {
            $(this).css('background','#8E8E8E');
            $('#valid_tip').html('&nbsp;');
            $("#send-number").css({"border":"0px solid #e56315","box-shadow":""});
            var pval = $('#mobile').val();
            var _this = $(this),locker=false,limit=60,interval='';
            var locker = _this.data('locker');
            //验证手机号
            myreg=/^1(3|4|5|7|8)\d{9}$/;
            if(pval==""){
                $('#tel_tip').html('请输入手机号！');
                $("#phone_box").css({"border":"1px solid #e56315","box-shadow":"0 0 15px #e56315"});
                _this.css('background','#8CCC0F');
                return false;
            }else if(pval && (!myreg.test(pval))){
                $('#tel_tip').html('请正确输入手机号！');
                $("#phone_box").css({"border":"1px solid #e56315","box-shadow":"0 0 15px #e56315"});
                return false;
            }
            if(pval===value.tel){
                interval = setInterval(function(){
                    if(limit<0){
                        clearInterval(interval);
                        _this.data('locker',0).removeClass('sending').val('发送验证码');
                        _this.css('background','#8CCC0F');
                    }else{
                        _this.val((limit--)+'秒');
                        _this.css({'background':'#8E8E8E','color':"#fff"});
                    }
                },1000);
                $.ajax({ url:'http://ns.huatu.com/u/v1/users/captcha/'+pval,type:'get',dataType:'json'});
            }
            if(locker || _this.hasClass('sending')){
                $('#valid_tip').html('请稍等一会再发送!');
                return false;
            }
            _this.data('locker',1);
            _this.addClass('sending');

        })
        //手机号验证
        $("#mobile").blur(function(){
            var phone = $('#mobile').val(),
            myreg=/^1(3|4|5|7|8)\d{9}$/;
            if(phone && (!myreg.test(phone))){
                $('#tel_tip').html('请正确输入手机号！');
                $("#phone_box").css({"border":"1px solid #e56315","box-shadow":"0 0 15px #e56315"});
                return false;
            }else{
                value.tel=phone;
                $('#tel_tip').html('&nbsp;');
                $("#phone_box").css({"border":"0px solid #e56315","box-shadow":""});
                return true;
            }
        });
        // 密码验证
        $('#password').blur(function(){
            var password = $('#password').val(),
            myregpas=/^([a-zA-Z0-9]{6,16})$/;
            if(password && (!myregpas.test(password))){
                $(".PnBox").css({"border":"1px solid #e56315","box-shadow":"0 0 15px #e56315"});
                $('#pw_tip').html('请输入6-16位字母或数字！');
                return false;
            }else{
                value.pw=password;
                $(".PnBox").css({"border":"0px solid #e56315","box-shadow":""});
                $('#pw_tip').html('&nbsp;');
                return true;
            }
        })
        // 表单总体验证，并提交
        function validate_form(thisform){
            var bol_1=true, bol_2=true, bol_3=true,phone = $('#mobile').val(),
            verification=$('#send-number').val(),
            password = $('#password').val();
            if(phone&&phone===value.tel){
                bol_1= true;
            }else{
                $('#tel_tip').html('请正确输入手机号！');
                bol_1= false;
            }
            // 验证码
            if(verification==''){
                $('#valid_tip').html('请输入验证码！');
                bol_2= false;
            }
            // 密码
            if(password&&password===value.pw){
                bol_3= true;
            }else{
                $('#pw_tip').html('请输入6-16位字母或数字!');
                bol_3= false;
            }
            // 返回
            if(bol_1&&bol_2&&bol_3){
                $("#reg_tip").fadeIn("slow");
                $.ajax({
                    url: "/pc/user/register",
                    type: 'POST',
                    dataType: 'json',
                    data: {'phone':phone,'verification':verification,'password':password},
                    success:function(data){
                        if(data.code==1000000){
                            $('#reg_tip_box').html("注册成功");
                            location.href="/pc/wechat/index";
                        }else{
                            $('#reg_tip_box').html(data.message);
                        }
                    }
                })
                $("#reg_tip").fadeOut("slow");
            }else{
                return false;
            }
        }