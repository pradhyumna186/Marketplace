import React, { useState, useEffect } from 'react';
import { adminAPI, categoryAPI } from '../services/api';

const TABS = ['dashboard', 'users', 'products', 'category-requests', 'categories'];

const AdminPage = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [dashboard, setDashboard] = useState(null);
  const [users, setUsers] = useState({ content: [] });
  const [products, setProducts] = useState({ content: [] });
  const [categoryRequests, setCategoryRequests] = useState([]);
  const [categories, setCategories] = useState([]);
  const [userSearch, setUserSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [categoryForm, setCategoryForm] = useState({ name: '', description: '', parentCategoryId: '' });

  useEffect(() => {
    if (activeTab === 'dashboard') loadDashboard();
    if (activeTab === 'users') loadUsers();
    if (activeTab === 'products') loadProducts();
    if (activeTab === 'category-requests') loadCategoryRequests();
    if (activeTab === 'categories') loadCategories();
  }, [activeTab]);

  const loadDashboard = async () => {
    setLoading(true);
    try {
      const res = await adminAPI.getDashboard();
      setDashboard(res.data?.data || res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadUsers = async () => {
    setLoading(true);
    try {
      const res = await adminAPI.getUsers(userSearch, { page: 0, size: 30 });
      const data = res.data?.data || res.data;
      setUsers(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    setLoading(true);
    try {
      const res = await adminAPI.getProducts({ page: 0, size: 30 });
      const data = res.data?.data || res.data;
      setProducts(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadCategoryRequests = async () => {
    setLoading(true);
    try {
      const res = await adminAPI.getPendingCategoryRequests();
      setCategoryRequests(res.data?.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const flattenCategories = (list) =>
    (list || []).reduce((acc, c) => acc.concat([c], flattenCategories(c.subcategories || [])), []);

  const loadCategories = async () => {
    try {
      const res = await categoryAPI.getAll();
      const tree = res.data?.data || res.data || [];
      setCategories(Array.isArray(tree) ? flattenCategories(tree) : []);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleUserAction = async (id, action) => {
    try {
      if (action === 'suspend') await adminAPI.suspendUser(id);
      if (action === 'unsuspend') await adminAPI.unsuspendUser(id);
      if (action === 'lock') await adminAPI.lockUser(id);
      if (action === 'unlock') await adminAPI.unlockUser(id);
      loadUsers();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleProductAction = async (id, action) => {
    try {
      if (action === 'deactivate') await adminAPI.deactivateProduct(id);
      if (action === 'delete') await adminAPI.deleteProduct(id);
      loadProducts();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleApproveRequest = async (id) => {
    try {
      await adminAPI.approveCategoryRequest(id);
      loadCategoryRequests();
      loadCategories();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleRejectRequest = async (id) => {
    const notes = window.prompt('Rejection reason (optional):');
    try {
      await adminAPI.rejectCategoryRequest(id, notes);
      loadCategoryRequests();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleCreateCategory = async (e) => {
    e.preventDefault();
    if (!categoryForm.name?.trim()) return;
    try {
      await adminAPI.createCategory({
        name: categoryForm.name.trim(),
        description: categoryForm.description?.trim() || null,
        parentCategoryId: categoryForm.parentCategoryId ? parseInt(categoryForm.parentCategoryId, 10) : null,
      });
      setCategoryForm({ name: '', description: '', parentCategoryId: '' });
      loadCategories();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleDeactivateCategory = async (id) => {
    if (!window.confirm('Deactivate this category? It will be hidden from listings.')) return;
    try {
      await adminAPI.deactivateCategory(id);
      loadCategories();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  return (
    <div>
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="nav-tabs" style={{ border: 'none', padding: 0, flexWrap: 'wrap' }}>
          {TABS.map((tab) => (
            <button
              key={tab}
              className={`nav-tab ${activeTab === tab ? 'active' : ''}`}
              onClick={() => { setActiveTab(tab); setError(null); }}
            >
              {tab === 'dashboard' && '📊 Dashboard'}
              {tab === 'users' && '👥 Users'}
              {tab === 'products' && '📦 Listings'}
              {tab === 'category-requests' && '📋 Category Requests'}
              {tab === 'categories' && '📂 Categories'}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="alert alert-error" onClick={() => setError(null)}>
          {error}
        </div>
      )}

      {activeTab === 'dashboard' && (
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>Dashboard</h2>
          {loading ? (
            <div className="loading"><div className="spinner" /></div>
          ) : dashboard ? (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '1rem' }}>
              <StatCard label="Total Users" value={dashboard.totalUsers} />
              <StatCard label="Total Products" value={dashboard.totalProducts} />
              <StatCard label="Active Listings" value={dashboard.activeProducts} />
              <StatCard label="Categories" value={dashboard.totalCategories} />
              <StatCard label="Pending Category Requests" value={dashboard.pendingCategoryRequests} highlight />
            </div>
          ) : null}
        </div>
      )}

      {activeTab === 'users' && (
        <div className="card">
          <div className="flex-between" style={{ marginBottom: '1rem', flexWrap: 'wrap', gap: '0.75rem' }}>
            <h2>Users</h2>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                type="text"
                placeholder="Search email or username"
                value={userSearch}
                onChange={(e) => setUserSearch(e.target.value)}
                style={{ padding: '0.5rem', width: '220px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}
              />
              <button className="btn btn-primary" onClick={() => loadUsers()}>Search</button>
            </div>
          </div>
          {loading ? <div className="loading"><div className="spinner" /></div> : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border)', textAlign: 'left' }}>
                    <th style={{ padding: '0.75rem' }}>User</th>
                    <th style={{ padding: '0.75rem' }}>Email</th>
                    <th style={{ padding: '0.75rem' }}>Role</th>
                    <th style={{ padding: '0.75rem' }}>Status</th>
                    <th style={{ padding: '0.75rem' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {(users.content || []).map((u) => (
                    <tr key={u.id} style={{ borderBottom: '1px solid var(--border)' }}>
                      <td style={{ padding: '0.75rem' }}>{u.displayName || u.username}</td>
                      <td style={{ padding: '0.75rem' }}>{u.email}</td>
                      <td style={{ padding: '0.75rem' }}>{u.role}</td>
                      <td style={{ padding: '0.75rem' }}>
                        {!u.enabled && <span style={{ color: 'var(--danger)' }}>Suspended</span>}
                        {u.enabled && !u.accountNonLocked && <span style={{ color: 'var(--warning)' }}>Locked</span>}
                        {u.enabled && u.accountNonLocked && <span style={{ color: 'var(--success)' }}>Active</span>}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {u.role !== 'ADMIN' && (
                          <>
                            {u.enabled ? (
                              <button className="btn btn-sm btn-danger" onClick={() => handleUserAction(u.id, 'suspend')}>Suspend</button>
                            ) : (
                              <button className="btn btn-sm btn-success" onClick={() => handleUserAction(u.id, 'unsuspend')}>Unsuspend</button>
                            )}
                            {u.accountNonLocked ? (
                              <button className="btn btn-sm btn-danger" onClick={() => handleUserAction(u.id, 'lock')}>Lock</button>
                            ) : (
                              <button className="btn btn-sm btn-success" onClick={() => handleUserAction(u.id, 'unlock')}>Unlock</button>
                            )}
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {activeTab === 'products' && (
        <div className="card">
          <div className="flex-between" style={{ marginBottom: '1rem' }}>
            <h2>All Listings</h2>
            <button className="btn btn-secondary" onClick={loadProducts}>Refresh</button>
          </div>
          {loading ? <div className="loading"><div className="spinner" /></div> : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border)', textAlign: 'left' }}>
                    <th style={{ padding: '0.75rem' }}>Title</th>
                    <th style={{ padding: '0.75rem' }}>Price</th>
                    <th style={{ padding: '0.75rem' }}>Seller</th>
                    <th style={{ padding: '0.75rem' }}>Status</th>
                    <th style={{ padding: '0.75rem' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {(products.content || []).map((p) => (
                    <tr key={p.id} style={{ borderBottom: '1px solid var(--border)' }}>
                      <td style={{ padding: '0.75rem' }}>{p.title}</td>
                      <td style={{ padding: '0.75rem' }}>${p.price?.toFixed(2)}</td>
                      <td style={{ padding: '0.75rem' }}>{p.sellerName}</td>
                      <td style={{ padding: '0.75rem' }}>{p.status}</td>
                      <td style={{ padding: '0.75rem' }}>
                        {p.status === 'ACTIVE' && (
                          <button className="btn btn-sm btn-secondary" onClick={() => handleProductAction(p.id, 'deactivate')}>Deactivate</button>
                        )}
                        <button className="btn btn-sm btn-danger" onClick={() => window.confirm('Delete this listing?') && handleProductAction(p.id, 'delete')}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {activeTab === 'category-requests' && (
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>Pending Category Requests</h2>
          {loading ? <div className="loading"><div className="spinner" /></div> : categoryRequests.length === 0 ? (
            <p style={{ color: 'var(--text-light)' }}>No pending requests.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {categoryRequests.map((req) => (
                <div key={req.id} style={{ padding: '1rem', background: 'var(--bg)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                  <div className="flex-between" style={{ flexWrap: 'wrap', gap: '0.5rem' }}>
                    <div>
                      <strong>{req.name}</strong>
                      {req.description && <div style={{ fontSize: '0.875rem', color: 'var(--text-light)' }}>{req.description}</div>}
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-light)' }}>Requested by {req.requestedByUsername}</div>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button className="btn btn-sm btn-success" onClick={() => handleApproveRequest(req.id)}>Approve</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleRejectRequest(req.id)}>Reject</button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'categories' && (
        <div className="card">
          <h2 style={{ marginBottom: '1rem' }}>Categories</h2>
          <form onSubmit={handleCreateCategory} style={{ marginBottom: '1.5rem', display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '0.75rem', alignItems: 'end' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Name</label>
              <input value={categoryForm.name} onChange={(e) => setCategoryForm({ ...categoryForm, name: e.target.value })} required placeholder="New category name" />
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Parent (optional)</label>
              <select value={categoryForm.parentCategoryId} onChange={(e) => setCategoryForm({ ...categoryForm, parentCategoryId: e.target.value })}>
                <option value="">None</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <button type="submit" className="btn btn-primary">Create Category</button>
          </form>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            {categories.map((c) => (
              <div key={c.id} className="flex-between" style={{ padding: '0.75rem', background: 'var(--bg)', borderRadius: 'var(--radius-sm)' }}>
                <span><strong>{c.name}</strong> {c.description && <span style={{ color: 'var(--text-light)', fontSize: '0.875rem' }}>— {c.description}</span>}</span>
                <button className="btn btn-sm btn-danger" onClick={() => handleDeactivateCategory(c.id)}>Deactivate</button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

const StatCard = ({ label, value, highlight }) => (
  <div style={{
    padding: '1rem',
    background: highlight ? 'var(--primary)' : 'var(--bg)',
    color: highlight ? 'white' : 'var(--text)',
    borderRadius: 'var(--radius)',
    textAlign: 'center',
  }}>
    <div style={{ fontSize: '1.5rem', fontWeight: 700 }}>{value}</div>
    <div style={{ fontSize: '0.875rem', opacity: 0.9 }}>{label}</div>
  </div>
);

export default AdminPage;
