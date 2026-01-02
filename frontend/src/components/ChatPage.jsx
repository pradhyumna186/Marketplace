import React, { useState, useEffect, useRef } from 'react';
import { chatAPI, negotiationAPI } from '../services/api';

const ChatPage = ({ user }) => {
  const [chats, setChats] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [negotiations, setNegotiations] = useState([]);
  const [pendingOffers, setPendingOffers] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [offerAmount, setOfferAmount] = useState('');
  const [showOfferForm, setShowOfferForm] = useState(false);
  const [isSeller, setIsSeller] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    loadChats();
  }, []);

  useEffect(() => {
    if (selectedChat) {
      loadMessages();
      loadNegotiations();
      checkIfSeller();
      const interval = setInterval(() => {
        loadMessages();
        loadNegotiations();
      }, 3000); // Poll every 3 seconds
      return () => clearInterval(interval);
    }
  }, [selectedChat]);

  useEffect(() => {
    // Load pending offers for sellers
    if (user) {
      loadPendingOffers();
      const interval = setInterval(loadPendingOffers, 5000);
      return () => clearInterval(interval);
    }
  }, [user]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadChats = async () => {
    setLoading(true);
    try {
      const res = await chatAPI.getAll({ page: 0, size: 50 });
      const data = res.data?.data || {};
      setChats(data.content || []);
      if (data.content && data.content.length > 0 && !selectedChat) {
        setSelectedChat(data.content[0]);
      }
    } catch (error) {
      console.error('Error loading chats:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMessages = async () => {
    if (!selectedChat) return;
    
    try {
      const res = await chatAPI.getMessages(selectedChat.id, { page: 0, size: 100 });
      const data = res.data?.data || {};
      setMessages(data.content || []);
    } catch (error) {
      console.error('Error loading messages:', error);
    }
  };

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || !selectedChat) return;

    setSending(true);
    try {
      await chatAPI.sendMessage(selectedChat.id, { content: newMessage.trim() });
      setNewMessage('');
      loadMessages();
    } catch (error) {
      alert('Failed to send message: ' + (error.response?.data?.message || error.message));
    } finally {
      setSending(false);
    }
  };

  const loadNegotiations = async () => {
    if (!selectedChat) return;
    
    try {
      const res = await negotiationAPI.getChatNegotiations(selectedChat.id);
      setNegotiations(res.data?.data || []);
    } catch (error) {
      console.error('Error loading negotiations:', error);
    }
  };

  const loadPendingOffers = async () => {
    try {
      const res = await negotiationAPI.getPendingOffers();
      setPendingOffers(res.data?.data || []);
    } catch (error) {
      console.error('Error loading pending offers:', error);
    }
  };

  const checkIfSeller = () => {
    if (selectedChat && user) {
      // Check if current user is the seller (you might need to adjust this based on your chat structure)
      setIsSeller(selectedChat.sellerId === user.id);
    }
  };

  const makeOffer = async () => {
    if (!offerAmount || !selectedChat) return;

    try {
      await negotiationAPI.makeOffer(selectedChat.id, {
        offeredPrice: parseFloat(offerAmount),
        message: `I'm offering $${offerAmount} for this item.`
      });
      setShowOfferForm(false);
      setOfferAmount('');
      loadNegotiations();
      loadMessages();
    } catch (error) {
      alert('Failed to submit offer: ' + (error.response?.data?.message || error.message));
    }
  };

  const acceptOffer = async (negotiationId) => {
    if (!window.confirm('Are you sure you want to accept this offer? This will mark the product as sold.')) {
      return;
    }

    try {
      await negotiationAPI.acceptOffer(negotiationId);
      loadNegotiations();
      loadMessages();
      loadPendingOffers();
      alert('Offer accepted! Product marked as sold.');
    } catch (error) {
      alert('Failed to accept offer: ' + (error.response?.data?.message || error.message));
    }
  };

  const rejectOffer = async (negotiationId) => {
    const reason = window.prompt('Enter a reason for rejecting this offer (optional):');
    
    try {
      await negotiationAPI.rejectOffer(negotiationId, reason || '');
      loadNegotiations();
      loadMessages();
      loadPendingOffers();
    } catch (error) {
      alert('Failed to reject offer: ' + (error.response?.data?.message || error.message));
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="chat-container">
      {/* Chat List */}
      <div className="chat-list">
        <div style={{ padding: '1rem', borderBottom: '1px solid var(--border)' }}>
          <h3>Messages</h3>
        </div>
        {chats.length > 0 ? (
          chats.map(chat => (
            <div
              key={chat.id}
              className={`chat-item ${selectedChat?.id === chat.id ? 'active' : ''}`}
              onClick={() => setSelectedChat(chat)}
            >
              <div style={{ fontWeight: 600, marginBottom: '0.25rem' }}>
                {chat.productTitle || 'Product Chat'}
              </div>
              <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>
                {chat.otherParticipantName || 'Unknown User'}
              </div>
              {chat.unreadCount > 0 && (
                <div style={{ 
                  marginTop: '0.25rem', 
                  fontSize: '0.75rem',
                  background: 'var(--danger)',
                  color: 'white',
                  padding: '0.125rem 0.5rem',
                  borderRadius: 'var(--radius-sm)',
                  display: 'inline-block'
                }}>
                  {chat.unreadCount} new
                </div>
              )}
            </div>
          ))
        ) : (
          <div className="empty-state" style={{ padding: '2rem' }}>
            <div className="empty-state-icon">💬</div>
            <p>No chats yet</p>
          </div>
        )}
      </div>

      {/* Chat Window */}
      <div className="chat-window">
        {selectedChat ? (
          <>
            <div style={{ padding: '1rem', borderBottom: '1px solid var(--border)' }}>
              <div className="flex-between">
                <div>
                  <h3>{selectedChat.productTitle || 'Product Chat'}</h3>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-light)' }}>
                    {selectedChat.otherParticipantName || 'Unknown User'}
                  </p>
                </div>
                <div>
                  {!isSeller && (
                    <button 
                      className="btn btn-sm btn-outline"
                      onClick={() => setShowOfferForm(!showOfferForm)}
                    >
                      💰 Make Offer
                    </button>
                  )}
                  {isSeller && pendingOffers.filter(offer => offer.chatId === selectedChat.id).length > 0 && (
                    <span style={{ 
                      background: 'var(--warning)', 
                      color: 'white', 
                      padding: '0.25rem 0.75rem', 
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.75rem',
                      fontWeight: 600
                    }}>
                      {pendingOffers.filter(offer => offer.chatId === selectedChat.id).length} Pending
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* Pending Offers Section for Sellers */}
            {isSeller && pendingOffers.length > 0 && (
              <div style={{ padding: '1rem', borderBottom: '1px solid var(--border)', background: '#fff3cd' }}>
                <h4 style={{ marginBottom: '0.75rem', fontSize: '0.875rem', fontWeight: 600 }}>
                  ⚠️ You have {pendingOffers.length} pending offer{pendingOffers.length > 1 ? 's' : ''}
                </h4>
                {pendingOffers.filter(offer => offer.chatId === selectedChat?.id).map(offer => (
                  <div key={offer.id} style={{ 
                    padding: '0.75rem', 
                    background: 'white', 
                    borderRadius: 'var(--radius-sm)',
                    marginBottom: '0.5rem',
                    border: '1px solid var(--border)'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: 600 }}>
                          ${offer.offeredPrice?.toFixed(2)} from {offer.offeredByName}
                        </div>
                        {offer.message && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
                            {offer.message}
                          </div>
                        )}
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
                          Original: ${offer.originalPrice?.toFixed(2)}
                        </div>
                      </div>
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button 
                          className="btn btn-sm btn-success"
                          onClick={() => acceptOffer(offer.id)}
                        >
                          ✓ Accept
                        </button>
                        <button 
                          className="btn btn-sm btn-danger"
                          onClick={() => rejectOffer(offer.id)}
                        >
                          ✗ Reject
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Active Negotiations in Chat */}
            {negotiations.length > 0 && (
              <div style={{ padding: '1rem', borderBottom: '1px solid var(--border)', background: 'var(--bg)' }}>
                <h4 style={{ marginBottom: '0.75rem', fontSize: '0.875rem', fontWeight: 600 }}>💰 Offers</h4>
                {negotiations.map(offer => (
                  <div key={offer.id} style={{ 
                    padding: '0.75rem', 
                    background: 'white', 
                    borderRadius: 'var(--radius-sm)',
                    marginBottom: '0.5rem',
                    border: '1px solid var(--border)'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: 600 }}>
                          ${offer.offeredPrice?.toFixed(2)} 
                          {offer.isOwnOffer ? ' (Your offer)' : ` from ${offer.offeredByName}`}
                        </div>
                        {offer.message && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
                            {offer.message}
                          </div>
                        )}
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
                          Status: <span style={{ 
                            color: offer.status === 'ACCEPTED' ? 'var(--success)' : 
                                   offer.status === 'REJECTED' ? 'var(--danger)' : 'var(--warning)'
                          }}>
                            {offer.status}
                          </span>
                        </div>
                      </div>
                      {offer.canRespond && !offer.isOwnOffer && (
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button 
                            className="btn btn-sm btn-success"
                            onClick={() => acceptOffer(offer.id)}
                          >
                            ✓ Accept
                          </button>
                          <button 
                            className="btn btn-sm btn-danger"
                            onClick={() => rejectOffer(offer.id)}
                          >
                            ✗ Reject
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Make Offer Form (for buyers) */}
            {!isSeller && showOfferForm && (
              <div style={{ padding: '1rem', borderBottom: '1px solid var(--border)', background: 'var(--bg)' }}>
                <div className="form-group" style={{ marginBottom: '0.75rem' }}>
                  <label>Offer Amount ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={offerAmount}
                    onChange={(e) => setOfferAmount(e.target.value)}
                    placeholder="Enter your offer"
                  />
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="btn btn-sm btn-primary" onClick={makeOffer}>
                    Submit Offer
                  </button>
                  <button className="btn btn-sm btn-secondary" onClick={() => setShowOfferForm(false)}>
                    Cancel
                  </button>
                </div>
              </div>
            )}

            <div className="chat-messages">
              {messages.length > 0 ? (
                messages.map(message => (
                  <div
                    key={message.id}
                    className={`message ${message.senderId === user?.id ? 'own' : ''}`}
                  >
                    <div className="message-content">
                      <div style={{ marginBottom: '0.25rem', fontSize: '0.75rem', opacity: 0.7 }}>
                        {message.senderName || 'Unknown'}
                      </div>
                      <div>{message.content}</div>
                      <div style={{ fontSize: '0.75rem', opacity: 0.7, marginTop: '0.25rem' }}>
                        {new Date(message.createdAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="empty-state" style={{ padding: '2rem' }}>
                  <p>No messages yet. Start the conversation!</p>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            <form onSubmit={sendMessage} className="message-input-area">
              <input
                type="text"
                className="message-input"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                placeholder="Type a message..."
                disabled={sending}
              />
              <button type="submit" className="btn btn-primary" disabled={sending || !newMessage.trim()}>
                Send
              </button>
            </form>
          </>
        ) : (
          <div className="empty-state" style={{ padding: '3rem' }}>
            <div className="empty-state-icon">💬</div>
            <p>Select a chat to start messaging</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatPage;

