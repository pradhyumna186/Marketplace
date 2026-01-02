import React, { useState } from 'react';
import { chatAPI } from '../services/api';

const ChatSection = () => {
  const [activeTab, setActiveTab] = useState('list');
  const [response, setResponse] = useState(null);

  const handleResponse = (promise) => {
    setResponse({ loading: true });
    promise
      .then(res => setResponse({ success: true, data: res.data }))
      .catch(err => setResponse({ error: true, data: err.response?.data || { message: err.message } }));
  };

  return (
    <div className="section">
      <h2>Chats</h2>
      
      <div className="nav-tabs">
        <button className={`nav-tab ${activeTab === 'start' ? 'active' : ''}`} onClick={() => setActiveTab('start')}>Start Chat</button>
        <button className={`nav-tab ${activeTab === 'list' ? 'active' : ''}`} onClick={() => setActiveTab('list')}>My Chats</button>
        <button className={`nav-tab ${activeTab === 'get' ? 'active' : ''}`} onClick={() => setActiveTab('get')}>Get Chat</button>
        <button className={`nav-tab ${activeTab === 'messages' ? 'active' : ''}`} onClick={() => setActiveTab('messages')}>Messages</button>
        <button className={`nav-tab ${activeTab === 'send' ? 'active' : ''}`} onClick={() => setActiveTab('send')}>Send Message</button>
        <button className={`nav-tab ${activeTab === 'unread' ? 'active' : ''}`} onClick={() => setActiveTab('unread')}>Unread</button>
      </div>

      {activeTab === 'start' && <StartChat onResponse={handleResponse} />}
      {activeTab === 'list' && <ChatList onResponse={handleResponse} />}
      {activeTab === 'get' && <GetChat onResponse={handleResponse} />}
      {activeTab === 'messages' && <GetMessages onResponse={handleResponse} />}
      {activeTab === 'send' && <SendMessage onResponse={handleResponse} />}
      {activeTab === 'unread' && <UnreadChats onResponse={handleResponse} />}

      {response && (
        <div className={`response ${response.error ? 'error' : response.success ? 'success' : ''}`}>
          <pre>{JSON.stringify(response.data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

const StartChat = ({ onResponse }) => {
  const [productId, setProductId] = useState('');

  const startChat = () => {
    if (productId) {
      onResponse(chatAPI.startChat(productId));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Product ID</label>
        <input type="number" value={productId} onChange={(e) => setProductId(e.target.value)} />
      </div>
      <button className="btn" onClick={startChat}>Start Chat</button>
    </div>
  );
};

const ChatList = ({ onResponse }) => {
  const [params, setParams] = useState({ page: '0', size: '20' });

  const loadChats = () => {
    onResponse(chatAPI.getAll(params));
  };

  return (
    <div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={loadChats}>Get My Chats</button>
    </div>
  );
};

const GetChat = ({ onResponse }) => {
  const [id, setId] = useState('');

  const getChat = () => {
    if (id) {
      onResponse(chatAPI.getById(id));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Chat ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <button className="btn" onClick={getChat}>Get Chat</button>
      <button className="btn" onClick={() => id && onResponse(chatAPI.markRead(id))}>Mark as Read</button>
      <button className="btn btn-danger" onClick={() => id && onResponse(chatAPI.closeChat(id))}>Close Chat</button>
    </div>
  );
};

const GetMessages = ({ onResponse }) => {
  const [id, setId] = useState('');
  const [params, setParams] = useState({ page: '0', size: '50' });

  const getMessages = () => {
    if (id) {
      onResponse(chatAPI.getMessages(id, params));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Chat ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={getMessages}>Get Messages</button>
    </div>
  );
};

const SendMessage = ({ onResponse }) => {
  const [id, setId] = useState('');
  const [formData, setFormData] = useState({
    content: '',
    messageType: 'text'
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (id) {
      onResponse(chatAPI.sendMessage(id, formData));
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Chat ID *</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} required />
      </div>
      <div className="form-group">
        <label>Content *</label>
        <textarea value={formData.content} onChange={(e) => setFormData({...formData, content: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Message Type</label>
        <select value={formData.messageType} onChange={(e) => setFormData({...formData, messageType: e.target.value})}>
          <option value="text">text</option>
          <option value="image">image</option>
          <option value="offer">offer</option>
        </select>
      </div>
      <button type="submit" className="btn">Send Message</button>
    </form>
  );
};

const UnreadChats = ({ onResponse }) => {
  const loadUnread = () => {
    onResponse(chatAPI.getUnread());
  };

  return (
    <div>
      <button className="btn" onClick={loadUnread}>Get Unread Chats</button>
    </div>
  );
};

export default ChatSection;

