function WSSHClient() {
}

WSSHClient.prototype._generateEndpoint = function () {
    let protocol;
    protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    return protocol + '127.0.0.1:8080/webssh';
};

WSSHClient.prototype.connect = function (options) {
    const endpoint = this._generateEndpoint();

    if (window.WebSocket) {
        //如果支持websocket
        this._connection = new WebSocket(endpoint);
    } else {
        //否则报错
        options.onError('WebSocket Not Supported');
        return;
    }

    this._connection.onopen = function () {
        options.onConnect();
    };

    this._connection.onmessage = function (evt) {
        const data = evt.data.toString();
        //data = base64.decode(data);
        options.onData(data);
    };


    this._connection.onclose = function (evt) {
        options.onClose();
    };
};

WSSHClient.prototype.send = function (data) {
    this._connection.send(JSON.stringify(data));
};

WSSHClient.prototype.sendInitData = function () {
    //连接参数
    const json = {"AuthToken": "", "columns": 100, "rows": 100};
    // 字符串形式的JSON
    const jsonString = JSON.stringify(json);
    this._connection.send(jsonString);
}

WSSHClient.prototype.sendClientData = function (data) {
    const socket = this._connection;

    // 判断WebSocket是否打开
    if (socket && socket.readyState !== WebSocket.OPEN) {
        return;
    }

    socket.send(data);
}

const client = new WSSHClient();
