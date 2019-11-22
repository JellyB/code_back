<!DOCTYPE html>
<html lang="en" data-dpr="2" style="font-size: 100px;">
<head>
    <title>ems</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telephone=no">
    <meta name="format-detection" content="email=no">
    <style>
        @charset "utf-8";
        /*! normalize.css v3.0.2 | MIT License | git.io/normalize */
        /* ==========================================================================
           Component: Base
         ============================================================================ */
        html{font-family:sans-serif;-ms-text-size-adjust:100%;-webkit-text-size-adjust:100%;}
        body,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,code,form,fieldset,legend,input,textarea,p,blockquote,th,td,header,hgroup,nav,section,article,aside,footer,figure,figcaption,menu,button{margin:0;padding:0;}
        body{background-color:#fff;-webkit-user-select:none;-webkit-text-size-adjust:100%;-webkit-tap-highlight-color:transparent;outline:0;line-height:1.5;font-size:16px;color:#070707;font-family:"Helvetica Neue",Helvetica,STHeiTi,sans-serif;}
        article,aside,details,figcaption,figure,footer,header,hgroup,main,menu,nav,section,summary{display:block;}
        audio,canvas,progress,video{display:inline-block;vertical-align:baseline;}
        audio:not([controls]){display:none;height:0;}
        [hidden],template{display:none;}
        a{background-color:transparent;-webkit-touch-callout:none;text-decoration:none;}
        a:active,a:hover{outline:0;}
        abbr[title]{border-bottom:1px dotted;}
        b,strong{font-weight:bold;}
        dfn{font-style:italic;}
        h1{font-size:2em;margin:0.67em 0;}
        mark{background:#ff0;color:#000;}
        small{font-size:80%;}
        sub,sup{font-size:75%;line-height:0;position:relative;vertical-align:baseline;}
        sup{top:-0.5em;}
        sub{bottom:-0.25em;}
        img{-webkit-box-sizing:border-box;box-sizing:border-box;border:0;background-color: white}
        svg:not(:root){overflow:hidden;}
        figure{margin:1em 40px;}
        hr{-moz-box-sizing:content-box;box-sizing:content-box;height:0;}
        pre{overflow:auto;}
        code,kbd,pre,samp{font-family:monospace,monospace;font-size:1em;}
        input,button,textarea,select,optgroup,option{margin:0;font-size:inherit;font-style:inherit;font-weight:inherit;color:inherit;outline:0;}
        button{overflow:visible;}
        button,select{text-transform:none;}
        button,html input[type="button"],input[type="reset"],input[type="submit"]{-webkit-appearance:button;cursor:pointer;}
        button[disabled],html input[disabled]{cursor:default;}
        button::-moz-focus-inner,input::-moz-focus-inner{border:0;padding:0;}
        input{line-height:normal;}
        input[type="checkbox"],input[type="radio"]{box-sizing:border-box;padding:0;}
        input[type="number"]::-webkit-inner-spin-button,input[type="number"]::-webkit-outer-spin-button{height:auto;}
        input[type="search"]{-webkit-appearance:textfield;-moz-box-sizing:content-box;-webkit-box-sizing:content-box;box-sizing:content-box;}
        input[type="search"]::-webkit-search-cancel-button,input[type="search"]::-webkit-search-decoration{-webkit-appearance:none;}
        fieldset{border:1px solid #c0c0c0;margin:0 2px;padding:0.35em 0.625em 0.75em;}
        legend{border:0;padding:0;}
        textarea{overflow:auto;}
        optgroup{font-weight:bold;}
        table{border-collapse:collapse;border-spacing:0;}
        td,th{padding:0;}
        li{list-style:none;}
        ins{text-decoration:none;}
        del{text-decoration:line-through;}
        :focus{outline:0;-webkit-tap-highlight-color:transparent;}
        em,i{font-style:normal;}
        a{color:#00a5e0;}
        ::-webkit-input-placeholder{color:#bbb;}

        /* rem */
        @media screen and (max-width: 319px) {
            html{font-size:85.33333px;}
        }
        @media screen and (min-width: 320px) and (max-width: 359px) {
            html{font-size:85.33333px;}
        }
        @media screen and (min-width: 360px) and (max-width: 374px) {
            html{font-size:96px;}
        }
        @media screen and (min-width: 375px) and (max-width: 383px) {
            html{font-size:100px;}
        }
        @media screen and (min-width: 384px) and (max-width: 399px) {
            html{font-size:102.4px;}
        }
        @media screen and (min-width: 400px) and (max-width: 413px) {
            html{font-size:106.66667px;}
        }
        @media screen and (min-width: 414px) {
            html{font-size:110.4px;}
        }

        body{
            background-color: #f6f6f7;
        }
        .back{

            width: 100%;
            height: 0.44rem;
            line-height: 0.44rem;
            font-size: 0.32rem;
            text-align: center;


        }

        .emsTop{
            overflow: hidden;
            background:#fff;
            padding:0.16rem 0.24rem;
        }
        .emsLogo{
            float: left;
            width:0.915rem;
            height:0.915rem;
            border:1px solid #ddd;
            margin-right:0.2rem;
            text-align: center;
            line-height: 0.915rem;
        }
        .emsLogo .img{
            width:0.915rem;
            height:0.915rem;
        }
        .emsLogo .img2{
            width:0.585rem;
            height:0.465rem;
            vertical-align: middle;
        }
        .emsBottom{
            background:#fff;
            margin-top:0.1rem;

        }
        .emstitile{
            padding-left:0.265rem;
            background:#fff;
            border-bottom:1px solid #ddd;
            height:0.585rem;
            line-height: 0.585rem;
            font-size:0.165rem;
            color:#333;
        }
        .emsul{
            background:url(http://tiku.huatu.com/cdn/images/vhuatu/tiku/course/border.png) repeat-y 0.265rem 0;
            background-size: 1px 1px;

        }
        /*.emsli{
            padding-left:0.265rem;
            font-size: 0.14rem;
            color:#999;
            /!*background:url(ems1.jpg) no-repeat 0.2rem 0;*!/
            background-size: 0.135rem 0.32rem;
        }*/
        .emsli_qs{
            padding-left:0.265rem;
            font-size: 0.16rem;
            color:#999;
            background:url(http://tiku.huatu.com/cdn/images/vhuatu/tiku/course/wuliu_active.png) no-repeat 0.22rem 0.18rem;
            background-size: 0.11rem 0.11rem;
        }
        .emsli2{
            padding-left:0.265rem;
            font-size: 0.16rem;
            color:#999;
            background:url(http://tiku.huatu.com/cdn/images/vhuatu/tiku/course/wuliu_inactive.png) no-repeat 0.23rem 0.18rem;
            background-size: 0.08rem 0.08rem;
        }
        .emslibd{
            margin-left: 0.25rem;
            padding: 0.12rem 0;
            border-bottom: 1px solid #ddd;
            line-height: 0.25rem;
        }
        .emslibottom{
            margin-left: 0.25rem;
            padding: 0.1rem 0;
            line-height: 0.25rem;


        }

        .emsP{
            color:#417636;
            font-weight: bold;
            margin-bottom: 0.04rem;
        }
        .emszt{
            margin-top:-0.05rem;
            color:#333;
            font-size: 0.165rem;
            margin-bottom:0.1rem;
        }
        .emszt1{
            /*color:#cc2759!important;*/
            margin-left:0.1rem;
        }
        .emszt4{
            color:#cc2759!important;
        }
        .emszt2{

            color:#999;
            font-size: 0.2rem;

        }
        .emsqk{
            font-size: 0.14rem;
            color:#333;
        }
        .emsbh1{
            margin-top:0.2rem;
        }
        .emsbh{
            font-size: 0.14rem;
            color:#999;
        }
        .zt_font{
            color: #333;
        }
        .y_qs{
            font-weight: bold;
            color: #417636;
        }
        .emsjl{
            margin-bottom: 0.04rem;
        }
        .emsjl .tel{
            color: #58a8f7;
        }
    </style>
</head>

<body>
<!-- <div class="back">
    <div class="desc">物流详情</div>
</div>头部end -->
<div class="emsTop">
<#if noMsgFlag>
    <p class="emsbh emsbh1">卖家还在备货中</p>
    <p class="emsbh">请稍后再来查看物流信息</p>
<#else>
    <div class="emszt2">物流状态:<span class="emszt1 y_qs">${statusDescription}</span></div>
    <div class="emszt2">运单编号:<span class="emszt1">${num}</span></div>
    <div class="emszt2">信息来源:<span class="emszt1">${result.data.com}</span></div>
</#if>
</div><!-- 物流公司详情end -->

<#if !noMsgFlag>
<div class="emsBottom">
    <ul class="emsul">
        <#list result.data.route as route>
            <#if route?is_first>
                <li class="emsli_qs">
                    <div class="emslibd">
                        <p class="emsP">${route.context}</p>
                        <p>${route.time}</p>
                    </div>
                </li>

            <#elseif route?is_last>
                <li class="emsli2">
                    <div class="emslibottom">
                        <p class="emsjl">${route.context}</p>
                        <p>${route.time}</p>
                    </div>
                </li>
            <#else>
                <li class="emsli2">
                    <div class="emslibd">
                        <p class="emsjl">${route.context}</p>
                        <p>${route.time}</p>
                    </div>
                </li>
            </#if>
        </#list>
    </ul>
</div>
</#if>

</body>
</html>