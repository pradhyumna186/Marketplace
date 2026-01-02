import React, { useState } from 'react';
import { adminAPI } from '../services/api';

const AdminSection = () => {
  const [activeTab, setActiveTab] = useState('pending');
  const [response, setResponse] = useState(null);

  const handleResponse = (promise) => {
    setResponse({ loading: true });
    promise
      .then(res => setResponse({ success: true, data: res.data }))
      .catch(err => setResponse({ error: true, data: err.response?.data || { message: err.message } }));
  };

  return (
    <div className="section">
      <h2>Admin (Admin Only)</h2>
      
      <div className="nav-tabs">
        <button className={`nav-tab ${activeTab === 'pending' ? 'active' : ''}`} onClick={() => setActiveTab('pending')}>Pending Requests</button>
        <button className={`nav-tab ${activeTab === 'approve' ? 'active' : ''}`} onClick={() => setActiveTab('approve')}>Approve</button>
        <button className={`nav-tab ${activeTab === 'reject' ? 'active' : ''}`} onClick={() => setActiveTab('reject')}>Reject</button>
      </div>

      {activeTab === 'pending' && <PendingRequests onResponse={handleResponse} />}
      {activeTab === 'approve' && <ApproveRequest onResponse={handleResponse} />}
      {activeTab === 'reject' && <RejectRequest onResponse={handleResponse} />}

      {response && (
        <div className={`response ${response.error ? 'error' : response.success ? 'success' : ''}`}>
          <pre>{JSON.stringify(response.data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

const PendingRequests = ({ onResponse }) => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadPending = () => {
    setLoading(true);
    adminAPI.getPendingCategoryRequests()
      .then(res => {
        // Handle nested response structure
        let requestsData = res.data;
        if (requestsData && requestsData.data) {
          requestsData = requestsData.data;
        }
        // Ensure it's an array
        const requestsArray = Array.isArray(requestsData) ? requestsData : [];
        // Log for debugging
        console.log('Received requests:', requestsArray);
        // Validate each request has an ID
        const validRequests = requestsArray.filter(req => {
          if (!req) return false;
          // Check if ID exists and is valid
          const hasId = req.id !== undefined && req.id !== null && req.id !== '';
          if (!hasId) {
            console.warn('Request missing ID:', req);
          }
          return hasId;
        });
        setRequests(validRequests);
        setLoading(false);
        onResponse(Promise.resolve(res));
      })
      .catch(err => {
        setLoading(false);
        onResponse(Promise.reject(err));
      });
  };

  const handleApprove = (id) => {
    if (!id || id === 'undefined' || id === undefined) {
      alert('Invalid request ID');
      return;
    }
    const requestId = Number(id);
    if (isNaN(requestId)) {
      alert('Invalid request ID');
      return;
    }
    adminAPI.approveCategoryRequest(requestId)
      .then(res => {
        onResponse(Promise.resolve(res));
        loadPending(); // Reload list after approval
      })
      .catch(err => onResponse(Promise.reject(err)));
  };

  const handleReject = (id, reviewNotes) => {
    if (!id || id === 'undefined' || id === undefined) {
      alert('Invalid request ID');
      return;
    }
    if (!reviewNotes || reviewNotes.trim() === '') {
      alert('Please provide review notes for rejection');
      return;
    }
    const requestId = Number(id);
    if (isNaN(requestId)) {
      alert('Invalid request ID');
      return;
    }
    adminAPI.rejectCategoryRequest(requestId, reviewNotes)
      .then(res => {
        onResponse(Promise.resolve(res));
        loadPending(); // Reload list after rejection
      })
      .catch(err => onResponse(Promise.reject(err)));
  };

  return (
    <div>
      <button className="btn" onClick={loadPending} disabled={loading}>
        {loading ? 'Loading...' : 'Get Pending Category Requests'}
      </button>
      
      {requests.length > 0 && (
        <div className="grid" style={{ marginTop: '20px' }}>
          {requests.map(request => (
            <RequestCard 
              key={request.id} 
              request={request} 
              onApprove={handleApprove}
              onReject={handleReject}
            />
          ))}
        </div>
      )}
    </div>
  );
};

const RequestCard = ({ request, onApprove, onReject }) => {
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [reviewNotes, setReviewNotes] = useState('');

  const handleRejectClick = () => {
    if (reviewNotes.trim()) {
      onReject(request.id, reviewNotes);
      setShowRejectForm(false);
      setReviewNotes('');
    } else {
      setShowRejectForm(true);
    }
  };

  return (
    <div className="card">
      <h3>{request.name}</h3>
      {request.description && <p><strong>Description:</strong> {request.description}</p>}
      {request.justification && <p><strong>Justification:</strong> {request.justification}</p>}
      {request.parentCategory && <p><strong>Parent:</strong> {request.parentCategory.name}</p>}
      <p><strong>Requested by:</strong> {request.requestedBy?.username || `User ID: ${request.requestedBy?.id}`}</p>
      <p><strong>Status:</strong> {request.status}</p>
      <p><strong>Created:</strong> {new Date(request.createdAt).toLocaleString()}</p>
      
      <div style={{ marginTop: '10px' }}>
        <button className="btn btn-success" onClick={() => {
          if (request.id) {
            onApprove(request.id);
          } else {
            alert('Request ID is missing');
          }
        }}>
          Approve
        </button>
        {!showRejectForm ? (
          <button className="btn btn-danger" onClick={() => setShowRejectForm(true)}>
            Reject
          </button>
        ) : (
          <div style={{ marginTop: '10px' }}>
            <textarea 
              value={reviewNotes} 
              onChange={(e) => setReviewNotes(e.target.value)}
              placeholder="Enter rejection reason..."
              style={{ width: '100%', marginBottom: '10px', minHeight: '60px' }}
            />
            <button className="btn btn-danger" onClick={handleRejectClick}>
              Confirm Reject
            </button>
            <button className="btn" onClick={() => {
              setShowRejectForm(false);
              setReviewNotes('');
            }} style={{ marginLeft: '10px' }}>
              Cancel
            </button>
          </div>
        )}
      </div>
      <p style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>Request ID: {request.id}</p>
    </div>
  );
};

const ApproveRequest = ({ onResponse }) => {
  const [id, setId] = useState('');

  const approve = () => {
    if (!id || id === 'undefined' || id === undefined) {
      alert('Please enter a valid request ID');
      return;
    }
    const requestId = Number(id);
    if (isNaN(requestId)) {
      alert('Please enter a valid numeric request ID');
      return;
    }
    onResponse(adminAPI.approveCategoryRequest(requestId));
  };

  return (
    <div>
      <div className="form-group">
        <label>Category Request ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <button className="btn btn-success" onClick={approve}>Approve Request</button>
    </div>
  );
};

const RejectRequest = ({ onResponse }) => {
  const [id, setId] = useState('');
  const [reviewNotes, setReviewNotes] = useState('');

  const reject = () => {
    if (!id || id === 'undefined' || id === undefined) {
      alert('Please enter a valid request ID');
      return;
    }
    if (!reviewNotes || reviewNotes.trim() === '') {
      alert('Please provide review notes for rejection');
      return;
    }
    const requestId = Number(id);
    if (isNaN(requestId)) {
      alert('Please enter a valid numeric request ID');
      return;
    }
    onResponse(adminAPI.rejectCategoryRequest(requestId, reviewNotes));
  };

  return (
    <div>
      <div className="form-group">
        <label>Category Request ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <div className="form-group">
        <label>Review Notes *</label>
        <textarea value={reviewNotes} onChange={(e) => setReviewNotes(e.target.value)} required />
      </div>
      <button className="btn btn-danger" onClick={reject}>Reject Request</button>
    </div>
  );
};

export default AdminSection;

