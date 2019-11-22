<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ include file="base.jsp" %>
<html lang="en" class="fuelux">
<head>
    <title>新增一个server实例</title>
</head>
<style>
    .hidden {
        display: none;
    }
</style>
<script type="text/javascript" src="/js/json2.js"></script>
<script type="text/javascript" src="<%=contextPath %>/js/nano.js"></script>
<script type="text/javascript">
    var projectData = [];

    var module_template = '<label class="radio radio-custom inline" ><input type="radio" value="${module}" name="moduleName"><i class="radio"></i>${module}</label>';
    $.fn.scrollBottom = function () {
        return $(document).height() - this.scrollTop() - this.height();
    };

    $(function () {
        /**页面加载完后加载菜单*/
        $.getJSON('/project/queryProject.do', function (data) {
            projectData = data;
            for (var i = 0; i < data.length; i++) {
                var html = "<li onclick='load_project_info(" + i

                    + ",this)'><a href='javascript:void(0)'>" + data[i].gitUrl + "</a></li>";

                $('#project_list').append(html);
            }
        });

        $('#myTab a:first').tab('show');
        $('#myTab a').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        })

        $("#queryTagBtn").on("click", queryTag);
        $("#backQueryTagBtn").on("click", queryTag);
        function queryTag() {
            var projectName = $("#projectName").val();
            if (projectName == "") {
                resultShow({type: "fail", msg: "请选择工程"});
                return false;
            }
            var tagTemplate = '<a href="javascript:void(0)" onclick="setTag(\'{tagName}\')"><strong>{tagName}&nbsp&nbsp&nbsp{remark}</strong></a><br/>';
            showLoading();
            $.getJSON("/project/queryTags.do", {projectName: projectName}, function (data) {
                removeLoading();
                var table = "<table id='tag_table' class=\"table table-striped table-hover\">\n" +
                    "            <thead>\n" +
                    "                <th>Tag</th>\n" +
                    "                <th>Branch</th>\n" +
                    "                <th>Module</th>\n" +
                    "                <th>Remark</th>\n" +
                    "                <th>Create Time</th>\n" +
                    "                <th>Create By</th>\n" +
                    "            </thead>\n" +
                    "        </table>";

                $("#result").empty().html(table);
                var tr = "<tr><td><a href=\"javascript:void(0)\" onclick=\"setTag(\'{tagName}\')\"><strong>{tagName}</strong></a></td>\n" +
                    "            <td>{branch}</td>\n" +
                    "            <td>{module}</td>\n" +
                    "            <td>{remark}</td>\n" +
                    "            <td>{createTime}</td>\n" +
                    "            <td>{createBy}</td></tr>";
                var trContent = "";
                for (var i = 0; i < data.length; i++) {
                    trContent = trContent + nano(tr, data[i])
                }

                $("#tag_table").append("<tbody>" + trContent + "</tbody>")
            });
        }
    })

    function echoLog(loggerName) {
        var interval  = setInterval(function(){
            var url = "/log/echo.do?loggerName=" + loggerName;
            $.ajax({
                url: url,
                type: 'GET',
                async: true,
                success: function(data){
                    $("#result-div").attr('style',"" +
                        "position: absolute;" +
                        "top: 40px;" +
                        "left: 0; " +
                        "width: 100%;" +
                        "z-index: 1000;" +
                        "height: 100%;" +
                        "color: #3C3C3C;" +
                        "opacity:0.9;"
                    );

                    $("#result").attr('style',"height:" + $("#result-div").height() + "px;");

                    if(data.indexOf("##LogEnd##") == -1){
                        $("#result").empty().html(data);
                        //window.scrollTo(0,9999);
                    }else{
                        $("#result").empty().html(data);
                        //window.scrollTo(0,0);
                        $("#result-div").attr('style',"");
                        $("#result").attr('style',"");
                        clearInterval(interval);
                    }
                },
                error: function(){
                    return false;
                }
            });
        },500);
    }

    $(function () {
        var moduleName = "";
        $('#packageForm').ajaxForm({
            dataType: 'html',
            url: "/project/package.do",
            beforeSubmit: function () {
                var environment = $('input[name=environment]').fieldValue();
                moduleName = $('input[name=moduleName]').fieldValue();
                var remark = $("#remark").val();
                var gitUrl = $("#gitUrl").val();
                if (gitUrl == "") {
                    resultShow({type: "fail", msg: "请选择工程"});
                    return false;
                }
                if (environment == 'online' && $("#tag").val() == '') {
                    resultShow({type: "fail", msg: "线上环境必须填写Tag"});
                    return false;
                }
                if (moduleName == "") {
                    resultShow({type: "fail", msg: "请选择module"});
                    return false;
                }

                if (environment == 'online' && remark.trim() == '') {
                    resultShow({type: "fail", msg: "请填写备注"});
                    return false;
                }
                $("#result").empty();
//                showLoading("div.container-fluid");
            },
            async: true,
            timeout: 1000,
            success: function (data) {
                $("#result").empty().html(data);
            },
            error : function(xhr,textStatus){
                if(textStatus=='timeout'){
                    echoLog(moduleName)
                }else{}
            }
        });


        $('#tagBackForm').ajaxForm({
            dataType: 'html',
            url: "/project/tagBack.do",
            beforeSubmit: function () {
                var gitUrl = $("#gitUrl").val();
                var tag = $("#tagBackForm :text").fieldValue()[0];
                var module = $("#tagBackForm :radio").fieldValue()[0];

                if (gitUrl == "") {
                    resultShow({type: "fail", msg: "请选择工程"});
                    return false;
                }
                if (tag == '') {
                    resultShow({type: "fail", msg: "请填写Tag"});
                    return false;
                }

                if (typeof module == "undefined" || module == "") {
                    resultShow({type: "fail", msg: "请选择module"});
                    return false;
                }
                $("#result").empty();
                showLoading("tagBackForm");
            },
            success: function (data) {
                removeLoading("tagBackForm");
                $("#result").empty().html(data);
            }
        });


    });

    function setTag(tag) {
        $("input[name=tag]").val(tag);
    }

    function load_project_info(index, dom) {
        $("#result").empty();
        $("#project_list > li").removeClass("tr-click")
        $(dom).addClass("tr-click")
        var project = projectData[index];
        $('.radio-custom i').removeClass('checked');
        $('.radio-custom i[name="default_environment"]').addClass('checked');
        radio_click('online')
        // $('.radio-custom input').prop('checked', false);
        $("#packageForm").resetForm();
        $('#branch_combobox').combobox('selectByText', 'master');
        $("legend[name=pro_fieldset_legend]").empty().append(project.gitUrl);
        $("#gitUrl").val(project.gitUrl);
        $("input[name=projectName]").val(project.projectName);
        $("#projectId").val(project.id);
        $.getJSON('/project/autoGenerateTag.do', {projectId: project.id}, function (data) {
            $("#tag").val(data.newTag);
        });
        $("#moduleDiv").empty();
        $("#backModuleDiv").empty();
        $.getJSON('/project/queryProjectModules.do', {project: project.projectName}, function (data) {
            //请求失败
            if(data.status ==1){
                resultShow({
                    msg:data.msg,
                    type:"fail"
                })
            }

            var module = "";
            for (var i = 0; i < data.data.length; i++) {
                module = module + module_template.replace(/\${module}/g, data.data[i])
            }
            $("#moduleDiv").empty().html(module);
            $("#backModuleDiv").empty().html(module);

            $('.radio-custom > input[type=radio]').each(function () {
                var $this = $(this);
                if ($this.data('radio')) return;
                $this.radio($this.data());
            });
        });

        if (project.permissions == 2) {//查看权限
            $("#packageBtn,#backBtn").attr("disabled", "disabled");
        } else if (project.permissions == 3) {//可执行权限
            $("#packageBtn,#backBtn").removeAttr("disabled", "disabled");
        }


    }
    function radio_click(value) {
        if (value == "online") {//线上环境
            $("#remarkDiv").removeClass("hidden");
            $("#tagDiv").removeClass("hidden");
        } else {
            $("#remarkDiv").addClass("hidden");
            $("#tagDiv").addClass("hidden");
        }
    }
</script>
<body style="overflow-y:none;">
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span4 sidebar-nav">
            <ul id="project_list" class="nav nav-tabs nav-stacked">
            </ul>
        </div>
        <div class="span8">
            <ul id="myTab" class="nav nav-tabs">
                <li><a href="#packageDiv" data-toggle="tab"><strong>编译打包</strong></a></li>
                <li><a href="#tagBackDiv" data-toggle="tab"><strong>版本回退</strong></a></li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="packageDiv">
                    <form id="packageForm" class="form-horizontal">
                        <fieldset id="pro_fieldset">
                            <legend name="pro_fieldset_legend"><font color="red">请选择工程</font></legend>
                            <input type="hidden" id="projectName" name="projectName"/>
                            <input type="hidden" id="gitUrl" name="gitUrl"/>
                            <input type="hidden" id="projectId" name="projectId"/>

                            <div id="branchDiv" class="control-group">
                                <label class="control-label" for="branch_combobox">Branch</label>

                                <div class="controls">
                                    <!-- COMBOBOX -->
                                    <div id="branch_combobox" class="input-append dropdown combobox">
                                        <input class="span9" name="branch" type="text" required>
                                        <button class="btn" data-toggle="dropdown"><i class="caret"></i></button>
                                        <ul class="dropdown-menu">
                                            <li data-value="develop"><a href="#">develop</a></li>
                                            <li data-value="master" data-selected="true"><a href="#">master</a></li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="moduleDiv">Module</label>

                                <div id="moduleDiv" class="controls">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Environment</label>

                                <div class="controls">
                                    <label class="radio radio-custom inline" onclick="radio_click('online')"><input
                                            checked="checked" type="radio" value="online" name="environment"><i
                                            class="radio" name="default_environment"></i>线上环境</label>
                                    <label class="radio radio-custom inline" onclick="radio_click('develop')"><input
                                            type="radio" value="develop" name="environment"><i
                                            class="radio"></i>测试环境</label>
                                    <label class="radio radio-custom inline" onclick="radio_click('test')"><input
                                            type="radio" value="test" name="environment"><i
                                            class="radio"></i>准线上环境</label>
                                </div>
                            </div>
                            <div id="tagDiv" class="control-group">
                                <label class="control-label" for="tag">Tag(以r_开头)</label>

                                <div class="controls">
                                    <input class="span6" id="tag" name="tag" type="text"></div>
                            </div>
                            <div id="dependency" class="control-group">
                                <label class="control-label">更新全部依赖</label>

                                <div class="controls">
                                    <label class="checkbox checkbox-custom">
                                        <input type="checkbox" name="updateDependency"><i
                                            class="checkbox"></i>更新</label>
                                </div>
                            </div>
                            <div id="remarkDiv" class="control-group">
                                <label class="control-label" for="remark">备注</label>

                                <div class="controls">
                                    <textarea class="span6" rows="5" id="remark" name="remark"></textarea></div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">操作</label>

                                <div class="controls">
                                    <button id="packageBtn" type="submit" class="btn btn-primary span4">编译打包</button>
                                    <button id="queryTagBtn" type="button" class="btn btn-primary span4">查看tag</button>
                                </div>

                            </div>
                        </fieldset>
                    </form>
                </div>
                <div class="tab-pane" id="tagBackDiv">
                    <form id="tagBackForm" class="form-horizontal" action="/project/tagBack.do">
                        <fieldset id="pro_fieldset">
                            <legend name="pro_fieldset_legend"><font color="red">请选择工程</font></legend>
                            <input type="hidden" id="projectName" name="projectName"/>

                            <div class="control-group">
                                <label class="control-label" for="backModuleDiv">Module</label>

                                <div id="backModuleDiv" class="controls">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="tag">Tag</label>

                                <div class="controls">
                                    <input class="span6" id="tag" name="tag" type="text"></div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">操作</label>

                                <div class="controls">
                                    <button id="backBtn" type="submit" class="btn btn-primary span4">回退</button>
                                    <button id="backQueryTagBtn" type="button" class="btn btn-primary span4">查看tag
                                    </button>
                                </div>

                            </div>
                        </fieldset>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="result-div" class="container-fluid">
    <pre id="result"></pre>
</div>
<div id="bottom"/>
</body>
</html>