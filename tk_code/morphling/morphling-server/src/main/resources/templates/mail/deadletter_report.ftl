<h2 style="color:red">死信队列重发报告</h2>
<hr/>
<table border="1" cellspacing="0">
    <thead>
    <tr>
        <th>环境</th>
        <th>死信数</th>
        <th>重发数</th>
    </tr>
    </thead>
    <#list items as item>
        <tr>
            <td>${item.env}</td>
            <td>${item.hitCount}</td>
            <td>${item.ackCount}</td>
        </tr>
    </#list>
</table>


