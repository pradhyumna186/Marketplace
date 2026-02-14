import React, { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';

export default function CategoryRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const load = () => {
    setLoading(true);
    setError(null);
    adminAPI.getPendingCategoryRequests()
      .then((res) => setRequests(res.data?.data || []))
      .catch((err) => setError(err.response?.data?.message || err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleApprove = (id) => {
    adminAPI.approveCategoryRequest(id).then(() => load()).catch((err) => setError(err.response?.data?.message || err.message));
  };

  const handleReject = (id) => {
    const notes = window.prompt('Rejection reason (optional):');
    adminAPI.rejectCategoryRequest(id, notes).then(() => load()).catch((err) => setError(err.response?.data?.message || err.message));
  };

  return (
    <>
      <h1 className="page-title">Category Requests</h1>
      {error && <div className="alert-error">{error}</div>}
      <div className="card">
        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : requests.length === 0 ? (
          <p style={{ color: 'var(--admin-text-muted)' }}>No pending requests.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {requests.map((req) => (
              <div key={req.id} style={{ padding: '1rem', background: 'var(--admin-bg)', borderRadius: '0.5rem', border: '1px solid var(--admin-border)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
                  <div>
                    <strong>{req.name}</strong>
                    {req.description && <div style={{ fontSize: '0.875rem', color: 'var(--admin-text-muted)', marginTop: '0.25rem' }}>{req.description}</div>}
                    <div style={{ fontSize: '0.75rem', color: 'var(--admin-text-muted)', marginTop: '0.5rem' }}>Requested by {req.requestedByUsername}</div>
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button type="button" className="btn-success btn-sm" onClick={() => handleApprove(req.id)}>Approve</button>
                    <button type="button" className="btn-danger btn-sm" onClick={() => handleReject(req.id)}>Reject</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
}
