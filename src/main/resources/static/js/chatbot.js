// Chatbot Widget JavaScript
class ChatbotWidget {
    constructor() {
        this.isOpen = false;
        this.messages = [];
        this.init();
    }

    init() {
        this.createWidget();
        this.attachEventListeners();
        this.addWelcomeMessage();
    }

    createWidget() {
        const widgetHTML = `
            <div class="chatbot-widget">
                <button class="chatbot-toggle" id="chatbotToggle">
                    <i class="fas fa-comments"></i>
                </button>
                <div class="chatbot-window" id="chatbotWindow">
                    <div class="chatbot-header">
                        <h4><i class="fas fa-robot"></i> IMS Assistant</h4>
                        <button class="chatbot-close" id="chatbotClose">&times;</button>
                    </div>
                    <div class="chatbot-messages" id="chatbotMessages">
                        <div class="typing-indicator" id="typingIndicator">
                            <span></span><span></span><span></span>
                        </div>
                    </div>
                    <div class="chatbot-input-area">
                        <input type="text" class="chatbot-input" id="chatbotInput" 
                               placeholder="Ask about inventory..." />
                        <button class="chatbot-send" id="chatbotSend">
                            <i class="fas fa-paper-plane"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', widgetHTML);
    }

    attachEventListeners() {
        document.getElementById('chatbotToggle').addEventListener('click', () => this.toggleChat());
        document.getElementById('chatbotClose').addEventListener('click', () => this.toggleChat());
        document.getElementById('chatbotSend').addEventListener('click', () => this.sendMessage());
        document.getElementById('chatbotInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    }

    toggleChat() {
        this.isOpen = !this.isOpen;
        const window = document.getElementById('chatbotWindow');
        const toggle = document.getElementById('chatbotToggle');
        
        if (this.isOpen) {
            window.classList.add('active');
            toggle.innerHTML = '<i class="fas fa-times"></i>';
        } else {
            window.classList.remove('active');
            toggle.innerHTML = '<i class="fas fa-comments"></i>';
        }
    }

    addWelcomeMessage() {
        this.addMessage('bot', 'Hello! I\'m your IMS Assistant. Ask me about inventory items, stock levels, vendors, or borrowers!');
    }

    addMessage(sender, text) {
        const messagesContainer = document.getElementById('chatbotMessages');
        const messageHTML = `
            <div class="message ${sender}">
                <div class="message-avatar">
                    <i class="fas fa-${sender === 'bot' ? 'robot' : 'user'}"></i>
                </div>
                <div class="message-content">${this.escapeHtml(text)}</div>
            </div>
        `;
        
        const typingIndicator = document.getElementById('typingIndicator');
        messagesContainer.insertBefore(
            this.createElementFromHTML(messageHTML),
            typingIndicator
        );
        
        this.scrollToBottom();
    }

    createElementFromHTML(htmlString) {
        const div = document.createElement('div');
        div.innerHTML = htmlString.trim();
        return div.firstChild;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    async sendMessage() {
        const input = document.getElementById('chatbotInput');
        const sendBtn = document.getElementById('chatbotSend');
        const message = input.value.trim();

        if (!message) return;

        // Add user message
        this.addMessage('user', message);
        input.value = '';
        sendBtn.disabled = true;

        // Show typing indicator
        this.showTyping(true);

        try {
            const response = await fetch('/api/chatbot/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: message })
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            this.showTyping(false);
            this.addMessage('bot', data.response || 'Sorry, I couldn\'t process that request.');
        } catch (error) {
            console.error('Error:', error);
            this.showTyping(false);
            this.addMessage('bot', 'Sorry, I encountered an error. Please try again.');
        } finally {
            sendBtn.disabled = false;
        }
    }

    showTyping(show) {
        const indicator = document.getElementById('typingIndicator');
        if (show) {
            indicator.classList.add('active');
        } else {
            indicator.classList.remove('active');
        }
        this.scrollToBottom();
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('chatbotMessages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

// Initialize chatbot when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new ChatbotWidget();
});
