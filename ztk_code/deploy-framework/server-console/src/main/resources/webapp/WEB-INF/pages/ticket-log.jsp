<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ include file="base.jsp"%>
<html lang="en" class="fuelux">
<script type="text/javascript" src="/js/nano.js"></script>
<script type="text/javascript" src="/js/json2.js"></script>
<head>
    <meta charset="utf-8">
    <title>工单日志</title>
    <style>
        table tr.even {
            background: #ebebeb;
        }
        table tr.odd {
            background: #ffffff;
        }

        .text_overflow_1{
            word-break:keep-all;/* 不换行 */
            white-space:nowrap;/* 不换行 */
            overflow:hidden;/* 溢出隐藏 */
            text-overflow:ellipsis;/* 缺省超出部分以。。。代替显示 */
        }

        td:hover{
            width: auto;
        }
    </style>
    <script>
        //        function showTicketForm(){
        //            $('#myModal').modal();
        //        }
//        var ticket_info_str=
//                '<tr>'+
//                '<td>{index}</td>'+
//                '<td title="{releaseLog}" class="text_overflow_1"><strong>{releaseLog}</strong></td>'+
//                '<td><strong>{status}</strong></td>'+
//                '<td><strong>{deployer}</strong></td>'+
//                '<td><strong>{createBy}</strong></td>'+
//                '<td>'+
//                '<a href="javascript:void(0)" onclick="changeStatus(\'{id}\',\'2\');">关闭</a>&nbsp&nbsp'+
//                '<a href="javascript:void(0)" onclick="changeStatus(\'{id}\',\'1\');" >完成</a>&nbsp&nbsp'+
//                '</td>'+
//                '</tr>';
//
//        loadTicket(0,1,20);
//
//        function loadTicket(status,page,pageSize){
//            $.ajax({
//                url : "/ticket/listTicketByPage.do",
//                data:{page:page,pageSize:pageSize,status:status
//                },
//                async : true,
//                dataType : "json",
//                success : function(result) {
//                    var ticket_tbody = $("#ticket_table > tbody")
//                    ticket_tbody.empty();
//
//                    tickets = result.tickets
//                    for(var i=0;i<tickets.length;i++){
//                        tickets[i].index=i+1;
//                        var ticket_info =  nano(ticket_info_str,tickets[i])
//                        ticket_tbody.append(ticket_info);
//                    }
//                }
//            })
//        }
//
//        function changeStatus(ticketId,status){
//            $.ajax({
//                url : "/ticket/updateStatus.do",
//                data:{ticketId:ticketId,status:status
//                },
//                async : true,
//                dataType : "json",
//                success : function(data) {
//                    resultShow(data, function () {
//                        location.href = "/ticket/index.do";
//                    });
//                }
//            })
//        }

    </script>
</head>
<body>
<div class="container" id="mainDiv">
    <div>
        <div id="instance_base_info" style="font-size: 16px"></div>
        <%--<form class="form-inline">--%>
        <%--<button id="addTicketBtn" type="button" onclick="showTicketForm()"  class="btn btn-primary">新建工单</button>--%>
        <%--</form>--%>
        <table id="ticket_table" class="table table-bordered table-striped table-hover" style="table-layout:fixed;">

            <colgroup>
                <col style="width:10%;"></col>
                <col style="width:20%;"></col>
                <col style="width:20%;"></col>
                <col style="width:16%;"></col>
                <col style="width:16%;"></col>
                <col style="width:13%;"></col>
            </colgroup>

            <thead>
            <th>工单ID</th>
            <th>摘要</th>
            <th>状态</th>
            <th>处理人</th>
            <th>提交人</th>
            <th>操作</th>
            </thead>
            <tbody></tbody>
        </table>
    </div>
</div>
</body>
</html>