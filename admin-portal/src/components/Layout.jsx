import React, { useState } from 'react';

export default function Layout({ user, onLogout, children }) {
  const [page, setPage] = useState('dashboard');

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="sidebar-header">
          StoneRidge Admin
        </div>
        <nav className="sidebar-nav">
          <a className={page === 'dashboard' ? 'active' : ''} href="#" onClick={(e) => { e.preventDefault(); setPage('dashboard'); }}>📊 Dashboard</a>
          <a className={page === 'users' ? 'active' : ''} href="#" onClick={(e) => { e.preventDefault(); setPage('users'); }}>👥 Users</a>
          <a className={page === 'products' ? 'active' : ''} href="#" onClick={(e) => { e.preventDefault(); setPage('products'); }}>📦 Listings</a>
          <a className={page === 'category-requests' ? 'active' : ''} href="#" onClick={(e) => { e.preventDefault(); setPage('category-requests'); }}>📋 Category Requests</a>
          <a className={page === 'categories' ? 'active' : ''} href="#" onClick={(e) => { e.preventDefault(); setPage('categories'); }}>📂 Categories</a>
        </nav>
        <div className="sidebar-footer">
          <span style={{ display: 'block', fontSize: '0.75rem', color: '#94a3b8', marginBottom: '0.5rem' }}>{user?.email}</span>
          <button type="button" onClick={onLogout}>Log out</button>
        </div>
      </aside>
      <main className="main">
        {children({ page })}
      </main>
    </div>
  );
}
