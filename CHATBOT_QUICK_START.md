# 🚀 Chatbot Quick Start

## 3 Steps to Get Chatbot Working

### Step 1: Get API Key (2 minutes)
1. Go to: https://makersuite.google.com/app/apikey
2. Sign in with Google
3. Click "Create API Key"
4. Copy the key

### Step 2: Set API Key (30 seconds)
```cmd
set GEMINI_API_KEY=paste_your_key_here
```

### Step 3: Restart App (1 minute)
```cmd
start.bat
```

## ✅ Done! 

Open `http://localhost:8087` and look for the **purple chat button** in the bottom-right corner!

---

## 💬 Example Questions

- "Show me all inventory items"
- "What items are low on stock?"
- "List all vendors"
- "How many borrowers do we have?"
- "Show me item with ID 1"
- "Which items have less than 5 in stock?"

---

## 🔍 Troubleshooting

**Button not showing?**
- Clear browser cache (Ctrl+Shift+R)
- Check browser console (F12)

**Bot says "Error"?**
- Verify API key is set correctly
- Check internet connection
- Restart application

**Test endpoint:**
`http://localhost:8087/api/chatbot/test`

---

**For full documentation, see CHATBOT_SETUP.md**
