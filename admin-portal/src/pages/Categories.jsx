import React, { useState, useEffect } from 'react';
import { adminAPI, categoryAPI } from '../services/api';

function flatten(list) {
  return (list || []).reduce((acc, c) => acc.concat([c], flatten(c.subcategories || [])), []);
}

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ name: '', description: '', parentCategoryId: '' });

  const load = () => {
    categoryAPI.getAll()
      .then((res) => {
        const tree = res.data?.data || res.data || [];
        setCategories(Array.isArray(tree) ? flatten(tree) : []);
      })
      .catch((err) => setError(err.response?.data?.message || err.message));
  };

  useEffect(() => { load(); }, []);

  const handleCreate = (e) => {
    e.preventDefault();
    if (!form.name?.trim()) return;
    setError(null);
    adminAPI.createCategory({
      name: form.name.trim(),
      description: form.description?.trim() || null,
      parentCategoryId: form.parentCategoryId ? parseInt(form.parentCategoryId, 10) : null,
    })
      .then(() => {
        setForm({ name: '', description: '', parentCategoryId: '' });
        load();
      })
      .catch((err) => setError(err.response?.data?.message || err.message));
  };

  const handleDeactivate = (id) => {
    if (!window.confirm('Deactivate this category? It will be hidden from listings.')) return;
    adminAPI.deactivateCategory(id).then(() => load()).catch((err) => setError(err.response?.data?.message || err.message));
  };

  return (
    <>
      <h1 className="page-title">Categories</h1>
      {error && <div className="alert-error">{error}</div>}
      <div className="card">
        <h2 style={{ fontSize: '1rem', marginBottom: '1rem' }}>Create category</h2>
        <form onSubmit={handleCreate} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '0.75rem', alignItems: 'end', flexWrap: 'wrap' }}>
          <div className="form-group" style={{ margin: 0 }}>
            <label>Name</label>
            <input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required placeholder="Category name" />
          </div>
          <div className="form-group" style={{ margin: 0 }}>
            <label>Parent (optional)</label>
            <select value={form.parentCategoryId} onChange={(e) => setForm((f) => ({ ...f, parentCategoryId: e.target.value }))}>
              <option value="">None</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn-primary">Create</button>
        </form>
      </div>
      <div className="card">
        <h2 style={{ fontSize: '1rem', marginBottom: '1rem' }}>All categories</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          {categories.map((c) => (
            <div key={c.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', background: 'var(--admin-bg)', borderRadius: '0.375rem' }}>
              <span><strong>{c.name}</strong>{c.description && <span style={{ color: 'var(--admin-text-muted)', fontSize: '0.875rem' }}> — {c.description}</span>}</span>
              <button type="button" className="btn-danger btn-sm" onClick={() => handleDeactivate(c.id)}>Deactivate</button>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
