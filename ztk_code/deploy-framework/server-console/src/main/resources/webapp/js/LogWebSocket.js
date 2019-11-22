var LogWebSocket = function (options) {
	this.options = options;
//	this.options = {
//		target:"log_target",
//		uri:"websocket server",
//		server_name:serverName,
//		max_line:500,
//		protocol:"ws",
//		log_type:"log"
//	}
	
	var url = this.options.protocol+"://"+this.options.uri+"?serverName="+this.options.server_name+"&log_type="+this.options.log_type;
	var socket;
	var current_line_count=0;
	if(window.MozWebSocket){
		socket = new MozWebSocket(url);
    }else if (window.WebSocket){
    	socket = new WebSocket(url);
    }else{
    	alert("Your browser does not support Web Socket.");
    	return;
    }
	socket.onopen = onopen;
    socket.onmessage = onmessage;
    socket.onclose = onclose;
    this.socket=socket;
    function onopen(event) {
        getTextAreaElement().innerHTML = "connect to the "+options.server_name+"."+options.log_type;
    }

    function onmessage(event) {
        appendTextArea(event.data);
    }
    
    function onclose(event) {
        appendTextArea("Web Socket closed");
    }
    
    function appendTextArea(newData) {
    	//达到最大行数，清楚历史记录
    	if(current_line_count>options.max_line){
    		el.innerHTML = "";
    		current_line_count=0;
    	}
    	
        var el = getTextAreaElement();
        el.innerHTML = el.innerHTML + '<br>' + newData;
        el.scrollTop = el.scrollHeight; 
        current_line_count ++;
    }
    
    function getTextAreaElement() {
        return document.getElementById(options.target);
    }
}

LogWebSocket.prototype = {
	constructor:LogWebSocket,
	close:function(){
		this.socket.close();
	}
}