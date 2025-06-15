let stompClient = null;
let playerCar = null;
const canvas = document.getElementById('gameArea');
const ctx = canvas.getContext('2d');
const carImage = new Image();
carImage.src = 'data:image/svg+xml;base64,' + btoa(`
<svg width="40" height="20" xmlns="http://www.w3.org/2000/svg">
    <rect width="40" height="20" rx="5" fill="COLOR_PLACEHOLDER"/>
    <circle cx="10" cy="15" r="4" fill="black"/>
    <circle cx="30" cy="15" r="4" fill="black"/>
</svg>`.trim());

// 键盘状态
const keys = {
    up: false,
    down: false,
    left: false,
    right: false,
    space: false
};

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket');
        
        // 订阅聊天消息
        stompClient.subscribe('/topic/messages', function(messageOutput) {
            const message = JSON.parse(messageOutput.body);
            displayMessage(message);
        });

        // 订阅游戏状态
        stompClient.subscribe('/topic/racing', function(stateOutput) {
            const state = JSON.parse(stateOutput.body);
            updateGameState(state);
        });
    });
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

function updateGameState(state) {
    // 清空画布
    ctx.fillStyle = '#4a4a4a';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 画网格线
    ctx.strokeStyle = '#666666';
    ctx.lineWidth = 1;
    for(let i = 0; i < canvas.width; i += 50) {
        ctx.beginPath();
        ctx.moveTo(i, 0);
        ctx.lineTo(i, canvas.height);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(0, i);
        ctx.lineTo(canvas.width, i);
        ctx.stroke();
    }

    // 绘制所有车辆
    Object.values(state.cars).forEach(car => {
        if (car.health > 0) {
            // 如果是当前玩家的车，更新信息显示
            if (car.username === username) {
                document.getElementById('playerName').textContent = username;
                document.getElementById('health').textContent = car.health;
                document.getElementById('speed').textContent = Math.round(car.speed);
                playerCar = car;
            }

            // 绘制车辆
            ctx.save();
            ctx.translate(car.x, car.y);
            ctx.rotate(car.angle * Math.PI / 180);
            
            // 绘制车身
            ctx.fillStyle = car.color;
            ctx.fillRect(-20, -10, 40, 20);
            ctx.strokeStyle = 'black';
            ctx.strokeRect(-20, -10, 40, 20);
            
            // 绘制炮管
            ctx.fillStyle = 'black';
            ctx.fillRect(15, -2, 10, 4);
            
            // 绘制玩家名字
            ctx.restore();
            ctx.fillStyle = 'white';
            ctx.font = '12px Arial';
            ctx.textAlign = 'center';
            ctx.fillText(car.username, car.x, car.y - 20);
        }
    });

    // 绘制所有子弹
    ctx.fillStyle = 'yellow';
    state.bullets.forEach(bullet => {
        ctx.beginPath();
        ctx.arc(bullet.x, bullet.y, 3, 0, Math.PI * 2);
        ctx.fill();
    });
}

// 键盘事件处理
window.addEventListener('keydown', function(e) {
    if (e.key === 'ArrowUp' || e.key.toLowerCase() === 'w') {
        keys.up = true;
        sendInput('UP');
    }
    if (e.key === 'ArrowDown' || e.key.toLowerCase() === 's') {
        keys.down = true;
        sendInput('DOWN');
    }
    if (e.key === 'ArrowLeft' || e.key.toLowerCase() === 'a') {
        keys.left = true;
        sendInput('LEFT');
    }
    if (e.key === 'ArrowRight' || e.key.toLowerCase() === 'd') {
        keys.right = true;
        sendInput('RIGHT');
    }
    if (e.key === ' ') {
        keys.space = true;
        sendInput('SPACE');
    }
});

window.addEventListener('keyup', function(e) {
    if (e.key === 'ArrowUp' || e.key.toLowerCase() === 'w') keys.up = false;
    if (e.key === 'ArrowDown' || e.key.toLowerCase() === 's') keys.down = false;
    if (e.key === 'ArrowLeft' || e.key.toLowerCase() === 'a') keys.left = false;
    if (e.key === 'ArrowRight' || e.key.toLowerCase() === 'd') keys.right = false;
    if (e.key === ' ') keys.space = false;
});

function sendInput(input) {
    if (stompClient) {
        stompClient.send("/app/racing/input", {}, JSON.stringify({
            username: username,
            input: input
        }));
    }
}

// 为发送消息按钮添加Enter键支持
document.getElementById('message-input').addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
});

// 初始化
document.getElementById('playerName').textContent = username;
connectWebSocket();
