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
    console.log('Received message:', message);
    
    // If we have a sender ID, fetch the display name
    if (message.senderId) {
        fetch(`/api/users/${message.senderId}/public`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch user info');
                }
                return response.json();
            })
            .then(userInfo => {
                displayMessageInUI(message, userInfo.displayName);
            })
            .catch(error => {
                console.error('Error fetching user display name:', error);
                // Fall back to using the ID if we can't fetch the display name
                displayMessageInUI(message, message.senderId);
            });
    } else {
        // No sender ID, just use "Unknown"
        displayMessageInUI(message, "Unknown");
    }
}

function displayMessageInUI(message, displayName) {
    // Create a message container
    const messageContainer = $('<div>').addClass('message-container');
    
    // Create the username element with hover card functionality
    const usernameElement = $('<strong>')
        .text(displayName + ': ')
        .addClass('chat-username')
        .attr('data-user-id', message.senderId);
    
    // Add hover event handlers
    usernameElement.on('mouseenter', handleUsernameHoverStart);
    usernameElement.on('mouseleave', handleUsernameHoverEnd);
    
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
        usernameElement,
        $('<span>').text(message.content)
    );
    
    // Add to messages container
    $('#messages').append(messageContainer);
}

// Global variable to track the current hover card
let currentHoverCard = null;
let hoverCardTimer = null;

// Handle hover start on username
function handleUsernameHoverStart(event) {
    const userId = $(this).data('user-id');
    const element = this;
    
    // Clear any existing timer
    if (hoverCardTimer) {
        clearTimeout(hoverCardTimer);
    }
    
    // Set a small delay before showing the hover card
    hoverCardTimer = setTimeout(() => {
        // Create a loading hover card
        const hoverCard = $('<div>')
            .addClass('hover-card')
            .append($('<div>').addClass('loading').text('Loading user information...'));
        
        // Position the hover card near the username
        const rect = element.getBoundingClientRect();
        // Position slightly below and to the right of the cursor for better visibility
        hoverCard.css({
            top: (rect.bottom + window.scrollY + 5) + 'px',
            left: (rect.left + window.scrollX - 10) + 'px'
        });
        
        // Add to body and store reference
        $('body').append(hoverCard);
        currentHoverCard = hoverCard;
        
        // Fetch hover card info
        console.log(`Fetching hover card for user: ${userId}`);
        fetch(`/api/users/${userId}/hovercard`)
            .then(response => {
                console.log(`Hover card API response status: ${response.status}`);
                if (!response.ok) {
                    throw new Error(`API returned ${response.status}`);
                }
                return response.json();
            })
            .then(hoverCardInfo => {
                console.log('Hover card info received:', hoverCardInfo);
                
                // If the hover card was removed while we were fetching, do nothing
                if (!currentHoverCard) return;
                
                // Create the hover card content
                const hoverCardContent = $('<div>').addClass('hover-card-content');
                
                // Add avatar or placeholder
                if (hoverCardInfo.avatarUrl) {
                    hoverCardContent.append(
                        $('<img>')
                            .attr('src', hoverCardInfo.avatarUrl)
                            .addClass('user-avatar-large')
                    );
                } else {
                    hoverCardContent.append(
                        $('<div>')
                            .addClass('user-avatar-placeholder')
                            .text(hoverCardInfo.displayName.charAt(0))
                    );
                }
                
                // Add user info details
                const userInfoDetails = $('<div>').addClass('user-info-details');
                userInfoDetails.append(
                    $('<div>').addClass('user-display-name').text(hoverCardInfo.displayName)
                );
                
                // Add details if available
                if (hoverCardInfo.details && Object.keys(hoverCardInfo.details).length > 0) {
                    const detailsList = $('<dl>').addClass('user-details-list');
                    
                    Object.entries(hoverCardInfo.details).forEach(([key, value]) => {
                        detailsList.append(
                            $('<dt>').text(key),
                            $('<dd>').text(value)
                        );
                    });
                    
                    userInfoDetails.append(detailsList);
                } else {
                    // No details available
                    userInfoDetails.append(
                        $('<p>').addClass('no-details').text('No additional information available')
                    );
                }
                
                hoverCardContent.append(userInfoDetails);
                
                // Replace the loading content with the actual content
                currentHoverCard.empty().append(hoverCardContent);
            })
            .catch(error => {
                console.error('Error fetching hover card info:', error);
                
                // If the hover card was removed while we were fetching, do nothing
                if (!currentHoverCard) return;
                
                // Show error in hover card
                currentHoverCard.empty().append(
                    $('<div>').addClass('error').text('Error loading user information')
                );
            });
    }, 300); // 300ms delay before showing
}

// Handle hover end on username
function handleUsernameHoverEnd(event) {
    // Clear any pending timer
    if (hoverCardTimer) {
        clearTimeout(hoverCardTimer);
        hoverCardTimer = null;
    }
    
    // Remove hover card after a short delay (to allow moving to the card itself)
    setTimeout(() => {
        if (currentHoverCard) {
            currentHoverCard.remove();
            currentHoverCard = null;
        }
    }, 300);
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