import React, { useState, useEffect } from 'react';
import { categoryAPI } from '../services/api';

const CategoriesPage = ({ onNavigate }) => {
  const [categories, setCategories] = useState([]);
  const [myRequests, setMyRequests] = useState([]);
  const [activeTab, setActiveTab] = useState('browse'); // 'browse' | 'request' | 'my-requests'
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    justification: '',
    parentCategoryId: ''
  });

  useEffect(() => {
    loadCategories();
    loadMyRequests();
  }, []);

  const loadCategories = async () => {
    setLoading(true);
    try {
      const res = await categoryAPI.getAll();
      setCategories(res.data?.data || []);
    } catch (err) {
      console.error('Error loading categories:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadMyRequests = async () => {
    try {
      const res = await categoryAPI.getMyRequests();
      setMyRequests(res.data?.data || []);
    } catch (err) {
      console.error('Error loading my requests:', err);
    }
  };

  const handleSubmitRequest = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);

    try {
      await categoryAPI.requestCategory({
        name: formData.name.trim(),
        description: formData.description.trim() || null,
        justification: formData.justification.trim() || null,
        parentCategoryId: formData.parentCategoryId ? parseInt(formData.parentCategoryId, 10) : null
      });
      setSuccess('Category request submitted! An admin will review it.');
      setFormData({ name: '', description: '', justification: '', parentCategoryId: '' });
      loadMyRequests();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to submit request');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="nav-tabs" style={{ border: 'none', padding: 0 }}>
          <button
            className={`nav-tab ${activeTab === 'browse' ? 'active' : ''}`}
            onClick={() => setActiveTab('browse')}
          >
            📂 Browse Categories
          </button>
          <button
            className={`nav-tab ${activeTab === 'request' ? 'active' : ''}`}
            onClick={() => setActiveTab('request')}
          >
            ➕ Request Category
          </button>
          <button
            className={`nav-tab ${activeTab === 'my-requests' ? 'active' : ''}`}
            onClick={() => setActiveTab('my-requests')}
          >
            📋 My Requests
          </button>
        </div>
      </div>

      {activeTab === 'browse' && (
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>All Categories</h2>
          {loading ? (
            <div className="loading"><div className="spinner" /></div>
          ) : categories.length > 0 ? (
            <div className="products-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))' }}>
              {categories.map((cat) => (
                <div
                  key={cat.id}
                  className="card"
                  onClick={() => {
                    onNavigate('products');
                    setTimeout(() => {
                      window.dispatchEvent(new CustomEvent('filterByCategory', { detail: cat.id }));
                    }, 100);
                  }}
                  style={{ cursor: 'pointer', textAlign: 'center', padding: '1.25rem' }}
                >
                  {cat.iconUrl ? (
                    <img src={cat.iconUrl} alt={cat.name} style={{ width: '48px', height: '48px', marginBottom: '0.5rem', borderRadius: 'var(--radius-sm)' }} />
                  ) : (
                    <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📦</div>
                  )}
                  <div style={{ fontWeight: 600 }}>{cat.name}</div>
                  {cat.description && (
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                      {cat.description}
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>No categories yet. Request one below!</p>
            </div>
          )}
        </div>
      )}

      {activeTab === 'request' && (
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>Request a New Category</h2>
          <p style={{ color: 'var(--text-light)', marginBottom: '1.5rem' }}>
            Don't see the category you need? Submit a request. An admin will review and approve it.
          </p>

          {success && <div className="alert alert-success">{success}</div>}
          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmitRequest}>
            <div className="form-group">
              <label>Category Name *</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
                minLength={2}
                maxLength={50}
                placeholder="e.g., Electronics, Furniture"
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                maxLength={500}
                rows={3}
                placeholder="Brief description of what this category is for"
              />
            </div>
            <div className="form-group">
              <label>Justification</label>
              <textarea
                value={formData.justification}
                onChange={(e) => setFormData({ ...formData, justification: e.target.value })}
                maxLength={1000}
                rows={3}
                placeholder="Why would this category be useful for the community?"
              />
            </div>
            <div className="form-group">
              <label>Parent Category (optional)</label>
              <select
                value={formData.parentCategoryId}
                onChange={(e) => setFormData({ ...formData, parentCategoryId: e.target.value })}
              >
                <option value="">None – top-level category</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </form>
        </div>
      )}

      {activeTab === 'my-requests' && (
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>My Category Requests</h2>
          {myRequests.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {myRequests.map((req) => (
                <div
                  key={req.id}
                  style={{
                    padding: '1rem',
                    background: 'var(--bg)',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border)'
                  }}
                >
                  <div className="flex-between">
                    <div>
                      <div style={{ fontWeight: 600 }}>{req.name}</div>
                      {req.description && (
                        <div style={{ fontSize: '0.875rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
                          {req.description}
                        </div>
                      )}
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.5rem' }}>
                        Submitted {req.createdAt ? new Date(req.createdAt).toLocaleDateString() : '—'}
                      </div>
                    </div>
                    <span
                      className="product-badge"
                      style={{
                        background: req.status === 'APPROVED' ? '#d1fae5' : req.status === 'REJECTED' ? '#fee2e2' : '#fef3c7',
                        color: req.status === 'APPROVED' ? '#065f46' : req.status === 'REJECTED' ? '#991b1b' : '#92400e'
                      }}
                    >
                      {req.status || 'PENDING'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>You haven't submitted any category requests yet.</p>
              <button className="btn btn-primary mt-2" onClick={() => setActiveTab('request')}>
                Request a Category
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default CategoriesPage;
