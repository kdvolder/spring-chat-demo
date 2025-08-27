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
            document.getElementById('userInfo').textContent = `User: ${user.displayName} Connected`;
            console.log('Current user:', user);
        })
        .catch(error => {
            console.error('Error fetching user info:', error);
            document.getElementById('userInfo').textContent = 'Authentication Required';
            // Don't set currentUser - this will prevent sending messages
            // Redirect to login page after a short delay
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
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
    // Always fetch the sender's display name from the public endpoint
    // since the message only contains the sender ID
    fetch(`/api/users/${message.senderId}/public`)
        .then(response => response.json())
        .then(userInfo => {
            // Display the message with the fetched display name
            displayMessageInUI(message, userInfo.displayName);
        })
        .catch(error => {
            console.error('Error fetching user info:', error);
            // Fall back to using the ID if we can't fetch the display name
            displayMessageInUI(message, message.senderId);
        });
}

function displayMessageInUI(message, displayName) {
    // Create a message container
    const messageContainer = $('<div>').addClass('message-container');
    
    // Fetch user avatar if we have a sender ID
    if (message.senderId) {
        fetch(`/api/users/${message.senderId}/public`)
            .then(response => response.json())
            .then(userInfo => {
                // Add avatar if available
                if (userInfo.avatarUrl) {
                    const avatar = $('<img>')
                        .attr('src', userInfo.avatarUrl)
                        .addClass('user-avatar')
                        .attr('title', displayName);
                    
                    messageContainer.prepend(avatar);
                }
            })
            .catch(error => {
                console.error('Error fetching user avatar:', error);
            });
    }
    
    // Add username and message content
    messageContainer.append(
        $('<strong>').text(displayName + ': '),
        $('<span>').text(message.content)
    );
    
    // Add to messages container
    $('#messages').append(messageContainer);
}

function sendMessage() {
    const messageContent = document.getElementById('messageInput').value.trim();
    if (messageContent && stompClient && currentUser) {
        const chatMessage = {
            content: messageContent,
            senderId: currentUser.id
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