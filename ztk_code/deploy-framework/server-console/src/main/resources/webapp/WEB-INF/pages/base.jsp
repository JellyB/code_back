<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<meta charset="utf-8">
<%
    String contextPath = request.getContextPath();
    String returnMsg=(String)request.getAttribute("returnMsg");
    if(returnMsg==null){
    	returnMsg="";
    }
%>
<link rel="shortcut icon" href="<%=contextPath %>/images/ico/favicon.ico">
<link rel="apple-touch-icon-precomposed" sizes="144x144"
      href="<%=contextPath %>/images/ico/apple-touch-icon-144-precomposed.png">
<link rel="apple-touch-icon-precomposed" sizes="114x114"
      href="<%=contextPath %>/images/ico/apple-touch-icon-114-precomposed.png">
<link rel="apple-touch-icon-precomposed" sizes="72x72"
      href="<%=contextPath %>/images/ico/apple-touch-icon-72-precomposed.png">
<link rel="apple-touch-icon-precomposed"
      href="<%=contextPath %>/images/ico/apple-touch-icon-57-precomposed.png">

<link href="<%=contextPath %>/fuelux/dist/css/fuelux.min.css" rel="stylesheet" />
<link href="<%=contextPath %>/fuelux/dist/css/fuelux-responsive.min.css" rel="stylesheet" />
<link href="<%=contextPath %>/fuelux/dist/css/extend.css" rel="stylesheet" />
<link href="<%=contextPath %>/css/bootstrap-docs.css" rel="stylesheet" />
<link href="<%=contextPath %>/css/panelLoading.plugin.css" rel="stylesheet" />
<link href="<%=contextPath %>/scrollUp/demo/css/themes/tab.css" rel="stylesheet" />
<style>
.modal-body {
  position: relative;
  overflow-y: auto;
  max-height: 400px;
  padding: 15px;
}


</style>
<script type="text/javascript" src="<%=contextPath %>/jquery/jquery-1.9.0.min.js"></script>
<script type="text/javascript" src="<%=contextPath %>/jquery/jquery.form.js"></script>
<script type="text/javascript" src="<%=contextPath %>/fuelux/lib/underscore-min.js"></script>
<script type="text/javascript" src="<%=contextPath %>/fuelux/dist/loader.js"></script>
<script type="text/javascript" src="<%=contextPath %>/fuelux/sample/data.js"></script>
<script type="text/javascript" src="<%=contextPath %>/fuelux/sample/datasource.js"></script>
<script type="text/javascript" src="<%=contextPath %>/fuelux/sample/datasourceTree.js"></script>
<script type="text/javascript" src="<%=contextPath %>/js/bootbox.min.js"></script>
<script type="text/javascript" src="<%=contextPath %>/js/panelLoading.plugin.js"></script>
<script type="text/javascript" src="<%=contextPath %>/scrollUp/jquery.scrollUp.min.js"></script>


<style>
.loading-text-info {
color: #ECF5FF;
font-size: 15px;
font-weight: bolder;
}
</style>


<script type="text/javascript">
    $(function () {
        $.scrollUp({
            scrollName: 'scrollUp', // Element ID
            topDistance: '300', // Distance from top before showing element (px)
            topSpeed: 100, // Speed back to top (ms)
            animation: 'fade', // Fade, slide, none
            animationInSpeed: 200, // Animation in speed (ms)
            animationOutSpeed: 200, // Animation out speed (ms)
            scrollText: '返回顶部', // Text for element
            activeOverlay: true // Set CSS color to display scrollUp active point, e.g '#00FFFF'
        });
    });


    //5分钟超时
    $.ajaxSetup({
        timeout: 5*60*1000
    });
	var returnMsg = '<%=returnMsg %>';
	$(function(){
		if(returnMsg!=""){
			returnMsg = eval("("+returnMsg+")");
			resultShow(returnMsg);
		}
	})
	bootbox.setLocale("zh_CN");
	function resultShow(data,callback){
		var options = {};
		var handler={};
		if(data.type=="success"){
			handler={
			    "label" : "确定",
			    "class" : "btn-success"
			}
			options={
				"header":"SUCCESS"
			}
		}else if(data.type=="fail"){
			handler={
			    "label" : "确定",
			    "class" : "btn-danger"
			}
			options={
				"header":"FAIL"
			}
		}else{
            handler={
                "label" : "确定",
                "class" : "btn-success"
            }
            options={
                "header":"INFO"
            }
        }
		
		if(data.header){
			options={
				"header":data.header
			}
			
			handler={
			    "label" : "确定",
			    "class" : "btn-success"
			}
		}
		
		if(callback){
			options.onEscape=callback;
			handler.callback=callback;
		}
		
		bootbox.dialog(data.msg,handler,options);
	}
	
	/**显示请求遮罩*/
	function showLoading(target,loadingText){
		var text = typeof loadingText !="undefined"? loadingText:"正在处理请求,请稍候...";
		var jqueryTarget = $('div');
		if(arguments.length>=1){
			jqueryTarget = $(target);
		}
		jqueryTarget.panelLoading({
	 	    setLoading:         true,
	 	    color:              '#3C3C3C',
	 	    opacity:            '0.7',
	 	    text:               '<span style="font:normal bold 12pt ">'+text+'</span>',
	 	    textClass:          'loading-text-info',
	 	    loaderClass:        'progress-striped active',
	 	    loaderWidth:        '100%',
	 	    fadeDuration:       700,
	 	    fromTop:            true
	 	});
	}
	/**移除请求遮罩**/
	function removeLoading(target){
		var jqueryTarget = $('div');
		if(arguments.length>=1){
			jqueryTarget = $(target);
		}
		$('div').panelLoading({
	 	    setLoading:         false
	 	});
	}


    function clearForm(objE){//objE为form表单
        $(objE).find(':input').each(
                function(){
                    switch(this.type){
                        case 'passsword':
                        case 'select-multiple':
                        case 'select-one':
                        case 'text':
                        case 'textarea':
                            $(this).val('');
                            break;
                        case 'checkbox':
                        case 'radio':
                            this.checked = false;
                    }
                }
        );
    }
	
	
</script>


