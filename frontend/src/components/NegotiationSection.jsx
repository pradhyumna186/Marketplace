import React, { useState } from 'react';
import { negotiationAPI } from '../services/api';

const NegotiationSection = () => {
  const [activeTab, setActiveTab] = useState('offer');
  const [response, setResponse] = useState(null);

  const handleResponse = (promise) => {
    setResponse({ loading: true });
    promise
      .then(res => setResponse({ success: true, data: res.data }))
      .catch(err => setResponse({ error: true, data: err.response?.data || { message: err.message } }));
  };

  return (
    <div className="section">
      <h2>Negotiations</h2>
      
      <div className="nav-tabs">
        <button className={`nav-tab ${activeTab === 'offer' ? 'active' : ''}`} onClick={() => setActiveTab('offer')}>Make Offer</button>
        <button className={`nav-tab ${activeTab === 'accept' ? 'active' : ''}`} onClick={() => setActiveTab('accept')}>Accept Offer</button>
        <button className={`nav-tab ${activeTab === 'reject' ? 'active' : ''}`} onClick={() => setActiveTab('reject')}>Reject Offer</button>
        <button className={`nav-tab ${activeTab === 'list' ? 'active' : ''}`} onClick={() => setActiveTab('list')}>Chat Negotiations</button>
        <button className={`nav-tab ${activeTab === 'pending' ? 'active' : ''}`} onClick={() => setActiveTab('pending')}>Pending Offers</button>
      </div>

      {activeTab === 'offer' && <MakeOffer onResponse={handleResponse} />}
      {activeTab === 'accept' && <AcceptOffer onResponse={handleResponse} />}
      {activeTab === 'reject' && <RejectOffer onResponse={handleResponse} />}
      {activeTab === 'list' && <ChatNegotiations onResponse={handleResponse} />}
      {activeTab === 'pending' && <PendingOffers onResponse={handleResponse} />}

      {response && (
        <div className={`response ${response.error ? 'error' : response.success ? 'success' : ''}`}>
          <pre>{JSON.stringify(response.data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

const MakeOffer = ({ onResponse }) => {
  const [chatId, setChatId] = useState('');
  const [formData, setFormData] = useState({
    offeredPrice: '',
    message: '',
    validityHours: '24'
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (chatId) {
      const data = {
        ...formData,
        offeredPrice: parseFloat(formData.offeredPrice),
        validityHours: parseInt(formData.validityHours)
      };
      onResponse(negotiationAPI.makeOffer(chatId, data));
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Chat ID *</label>
        <input type="number" value={chatId} onChange={(e) => setChatId(e.target.value)} required />
      </div>
      <div className="form-group">
        <label>Offered Price *</label>
        <input type="number" step="0.01" value={formData.offeredPrice} onChange={(e) => setFormData({...formData, offeredPrice: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Message</label>
        <textarea value={formData.message} onChange={(e) => setFormData({...formData, message: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Validity Hours</label>
        <input type="number" value={formData.validityHours} onChange={(e) => setFormData({...formData, validityHours: e.target.value})} />
      </div>
      <button type="submit" className="btn">Make Offer</button>
    </form>
  );
};

const AcceptOffer = ({ onResponse }) => {
  const [id, setId] = useState('');

  const acceptOffer = () => {
    if (id) {
      onResponse(negotiationAPI.acceptOffer(id));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Negotiation/Offer ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <button className="btn btn-success" onClick={acceptOffer}>Accept Offer</button>
    </div>
  );
};

const RejectOffer = ({ onResponse }) => {
  const [id, setId] = useState('');
  const [reason, setReason] = useState('');

  const rejectOffer = () => {
    if (id) {
      onResponse(negotiationAPI.rejectOffer(id, reason));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Negotiation/Offer ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <div className="form-group">
        <label>Reason (optional)</label>
        <input type="text" value={reason} onChange={(e) => setReason(e.target.value)} />
      </div>
      <button className="btn btn-danger" onClick={rejectOffer}>Reject Offer</button>
    </div>
  );
};

const ChatNegotiations = ({ onResponse }) => {
  const [chatId, setChatId] = useState('');

  const loadNegotiations = () => {
    if (chatId) {
      onResponse(negotiationAPI.getChatNegotiations(chatId));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Chat ID</label>
        <input type="number" value={chatId} onChange={(e) => setChatId(e.target.value)} />
      </div>
      <button className="btn" onClick={loadNegotiations}>Get Chat Negotiations</button>
    </div>
  );
};

const PendingOffers = ({ onResponse }) => {
  const loadPending = () => {
    onResponse(negotiationAPI.getPendingOffers());
  };

  return (
    <div>
      <button className="btn" onClick={loadPending}>Get Pending Offers</button>
    </div>
  );
};

export default NegotiationSection;

