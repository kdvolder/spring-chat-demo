// Chat demo JavaScript
let stompClient = null;
let currentUser = null;

function fetchUserInfo() {
    fetch('/api/user')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to fetch user info');
            }
        })
        .then(user => {
            currentUser = user;
            document.getElementById('userInfo').textContent = `User: ${user.username} Connected`;
            console.log('Current user:', user);
        })
        .catch(error => {
            console.error('Error fetching user info:', error);
            document.getElementById('userInfo').textContent = 'User: Unknown';
            currentUser = { username: 'Unknown', roles: [] };
        });
}

function connect() {
    const socket = new SockJS('/ws');
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        onConnect: (frame) => {
            console.log('Connected');
            document.getElementById('status').textContent = 'Connected';
            document.getElementById('sendButton').disabled = false;
            
            stompClient.subscribe('/topic/public', (message) => {
                showMessage(JSON.parse(message.body));
            });
        },
        onDisconnect: () => {
            console.log('Disconnected');
            document.getElementById('status').textContent = 'Disconnected';
            document.getElementById('sendButton').disabled = true;
        }
    });

    stompClient.activate();
}

function showMessage(message) {
    $('#messages').append(
        $('<div>').append(
            $('<strong>').text(message.sender + ': '),
            $('<span>').text(message.content)
        )
    );
}

function sendMessage() {
    const messageContent = document.getElementById('messageInput').value.trim();
    if (messageContent && stompClient && currentUser) {
        const chatMessage = {
            content: messageContent,
            sender: currentUser.username
        };
        
        stompClient.publish({
            destination: '/app/chat.sendMessage',
            body: JSON.stringify(chatMessage)
        });
        
        document.getElementById('messageInput').value = '';
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('sendButton').addEventListener('click', sendMessage);
    
    // Allow Enter key to send message
    document.getElementById('messageInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
    
    // Fetch user info first, then connect
    fetchUserInfo();
    connect();
}); 