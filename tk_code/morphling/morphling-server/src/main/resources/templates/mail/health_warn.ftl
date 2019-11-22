<h2 style="color:red">健康检查异常</h2>
<hr/>
<h5>${app.name}</h5>
<p>${app.description}</p>
<#list errors as item>
    <ul style="margin-top:20px">
        <li>主机：${item.host}</li>
        <li>端口：${item.port}</li>
        <li>描述：
            <pre>
${item.info}
            </pre>
        </li>
    </ul>
</#list>

