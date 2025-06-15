class Body {
    constructor(x, y, mass, color) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.mass = mass;
        this.color = color;
        this.radius = 10;
    }

    getSpeed() {
        return Math.sqrt(this.vx * this.vx + this.vy * this.vy).toFixed(2);
    }

    getPosition() {
        return `(${this.x.toFixed(0)}, ${this.y.toFixed(0)})`;
    }
}

let stompClient = null;
let bodies = [];
let simulationStartTime = Date.now();

const canvas = document.getElementById('simulationArea');
const ctx = canvas.getContext('2d');

// WebSocket连接设置
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket');
        
        // 发送加入消息
        stompClient.send("/app/join", {}, username);
        
        // 订阅聊天消息
        stompClient.subscribe('/topic/messages', function(messageOutput) {
            const message = JSON.parse(messageOutput.body);
            displayMessage(message);
        });

        // 订阅三体运动状态
        stompClient.subscribe('/topic/simulation', function(simulationOutput) {
            const state = JSON.parse(simulationOutput.body);
            updateSimulationState(state);
        });
    });
}

function updateSimulationState(state) {
    bodies = state.bodies;
    simulationStartTime = Date.now() - state.elapsedTime;
    
    // 更新显示
    updateDisplay(state);
}

function displayMessage(message) {
    const messageArea = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = message.system ? 'system-message' : 'user-message';
    
    const timestamp = `<span class="timestamp">[${message.timestamp}] </span>`;
    
    if (message.system) {
        messageDiv.innerHTML = `${timestamp}${message.content}`;
    } else {
        messageDiv.innerHTML = `${timestamp}<span class="username">${message.username}:</span> ${message.content}`;
    }
    
    messageArea.appendChild(messageDiv);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function sendMessage() {
    const messageInput = document.getElementById('message-input');
    const message = messageInput.value.trim();
    
    if (message && stompClient) {
        const chatMessage = {
            username: username,
            content: message,
            system: false
        };
        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

// 为发送消息按钮添加Enter键支持
document.getElementById('message-input').addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
});

function formatTime(ms) {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    return `${hours}:${minutes % 60}:${seconds % 60}`;
}

function updateDisplay(state) {
    // 清除画布
    ctx.fillStyle = 'black';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 绘制小球
    for (const body of bodies) {
        ctx.beginPath();
        ctx.arc(body.x, body.y, body.radius, 0, Math.PI * 2);
        ctx.fillStyle = body.color;
        ctx.fill();
        ctx.closePath();
    }

    // 更新信息显示
    document.getElementById('currentTime').textContent = new Date().toLocaleTimeString();
    document.getElementById('simulationTime').textContent = formatTime(Date.now() - simulationStartTime);
    document.getElementById('gravityConstant').textContent = state.gravityConstant;
    document.getElementById('timeStep').textContent = state.timeStep;
    document.getElementById('dampingFactor').textContent = state.dampingFactor;
    
    if (bodies.length >= 3) {
        document.getElementById('redSpeed').textContent = bodies[0].speed.toFixed(2) + ' px/s';
        document.getElementById('yellowSpeed').textContent = bodies[1].speed.toFixed(2) + ' px/s';
        document.getElementById('greenSpeed').textContent = bodies[2].speed.toFixed(2) + ' px/s';
        document.getElementById('redPos').textContent = bodies[0].position;
        document.getElementById('yellowPos').textContent = bodies[1].position;
        document.getElementById('greenPos').textContent = bodies[2].position;
    }
}

// 初始化WebSocket连接
connectWebSocket();
