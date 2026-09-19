/* RublinMart AI Assistant Chatbot Client */

function initChatbot() {
    if (document.getElementById('rublinmart-chatbot-wrapper')) return;

    const wrapper = document.createElement('div');
    wrapper.id = 'rublinmart-chatbot-wrapper';
    wrapper.innerHTML = `
        <button id="chat-toggle-btn" class="chat-widget-btn" onclick="toggleChatPanel()">
            <i class="fas fa-robot"></i>
        </button>
        <div id="chat-panel" class="chat-panel">
            <div class="chat-header">
                <span><i class="fas fa-robot"></i> RublinMart AI Assistant</span>
                <button onclick="toggleChatPanel()" style="background:none; border:none; color:white; font-size:1.2rem; cursor:pointer;">&times;</button>
            </div>
            <div id="chat-messages" class="chat-body">
                <div class="chat-bubble bot">
                    Hello! Welcome to RublinMart. I'm your AI shopping assistant. How can I help you today?
                </div>
            </div>
            <form class="chat-footer" onsubmit="sendChatMessage(event)">
                <input type="text" id="chat-input" class="chat-input" placeholder="Ask about products, shipping, orders..." autocomplete="off">
                <button type="submit" class="btn btn-sm btn-primary" style="border-radius:20px;"><i class="fas fa-paper-plane"></i></button>
            </form>
        </div>
    `;
    document.body.appendChild(wrapper);
}

function toggleChatPanel() {
    const panel = document.getElementById('chat-panel');
    if (panel) {
        panel.classList.toggle('active');
        if (panel.classList.contains('active')) {
            document.getElementById('chat-input').focus();
        }
    }
}

async function sendChatMessage(event) {
    event.preventDefault();
    const input = document.getElementById('chat-input');
    const messagesContainer = document.getElementById('chat-messages');

    const userMsg = input.value.trim();
    if (!userMsg) return;

    // Append User Bubble
    const userBubble = document.createElement('div');
    userBubble.className = 'chat-bubble user';
    userBubble.textContent = userMsg;
    messagesContainer.appendChild(userBubble);
    input.value = '';
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    // Append Typing Indicator
    const typingBubble = document.createElement('div');
    typingBubble.className = 'chat-bubble bot';
    typingBubble.id = 'chat-typing-indicator';
    typingBubble.innerHTML = '<i class="fas fa-ellipsis-h fa-spin"></i> Assistant is typing...';
    messagesContainer.appendChild(typingBubble);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    try {
        const res = await fetch(`${API_BASE}/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: userMsg })
        });
        const json = await res.json();

        // Remove Typing Indicator
        const indicator = document.getElementById('chat-typing-indicator');
        if (indicator) indicator.remove();

        const botBubble = document.createElement('div');
        botBubble.className = 'chat-bubble bot';

        if (json.success && json.data) {
            botBubble.textContent = json.data.reply;
        } else {
            botBubble.textContent = json.error ? json.error.message : "I am having trouble answering right now.";
        }

        messagesContainer.appendChild(botBubble);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

    } catch (e) {
        const indicator = document.getElementById('chat-typing-indicator');
        if (indicator) indicator.remove();

        const errorBubble = document.createElement('div');
        errorBubble.className = 'chat-bubble bot';
        errorBubble.textContent = "Unable to connect to assistant server.";
        messagesContainer.appendChild(errorBubble);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

document.addEventListener('DOMContentLoaded', initChatbot);
