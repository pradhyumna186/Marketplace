import React, { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';

export default function Products() {
  const [products, setProducts] = useState({ content: [] });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const load = () => {
    setLoading(true);
    setError(null);
    adminAPI.getProducts({ page: 0, size: 50 })
      .then((res) => setProducts(res.data?.data || res.data))
      .catch((err) => setError(err.response?.data?.message || err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleDeactivate = (id) => {
    adminAPI.deactivateProduct(id).then(() => load()).catch((err) => setError(err.response?.data?.message || err.message));
  };

  const handleDelete = (id) => {
    if (!window.confirm('Permanently delete this listing?')) return;
    adminAPI.deleteProduct(id).then(() => load()).catch((err) => setError(err.response?.data?.message || err.message));
  };

  const list = products.content || [];

  return (
    <>
      <h1 className="page-title">Listings</h1>
      {error && <div className="alert-error">{error}</div>}
      <div className="card">
        <div style={{ marginBottom: '1rem' }}>
          <button type="button" className="btn-secondary" onClick={load}>Refresh</button>
        </div>
        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Price</th>
                  <th>Seller</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {list.map((p) => (
                  <tr key={p.id}>
                    <td>{p.title}</td>
                    <td>${p.price != null ? Number(p.price).toFixed(2) : '—'}</td>
                    <td>{p.sellerName || '—'}</td>
                    <td>{p.status}</td>
                    <td className="actions-cell">
                      {p.status === 'ACTIVE' && (
                        <button type="button" className="btn-secondary btn-sm" onClick={() => handleDeactivate(p.id)}>Deactivate</button>
                      )}
                      <button type="button" className="btn-danger btn-sm" onClick={() => handleDelete(p.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}
