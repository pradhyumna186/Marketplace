import React, { useState, useEffect } from 'react';
import Login from './pages/Login';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Products from './pages/Products';
import CategoryRequests from './pages/CategoryRequests';
import Categories from './pages/Categories';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('admin_accessToken');
    const saved = localStorage.getItem('admin_user');
    if (token && saved) {
      try {
        const u = JSON.parse(saved);
        if (u.role === 'ADMIN') setUser(u);
        else localStorage.removeItem('admin_accessToken');
      } catch (_) {
        localStorage.removeItem('admin_accessToken');
        localStorage.removeItem('admin_user');
      }
    }
    setLoading(false);
  }, []);

  const onLogin = (userData, tokens) => {
    if (userData.role !== 'ADMIN') {
      throw new Error('Only administrators can access this portal.');
    }
    localStorage.setItem('admin_accessToken', tokens.accessToken || '');
    if (tokens.refreshToken) localStorage.setItem('admin_refreshToken', tokens.refreshToken);
    localStorage.setItem('admin_user', JSON.stringify(userData));
    setUser(userData);
  };

  const onLogout = () => {
    localStorage.removeItem('admin_accessToken');
    localStorage.removeItem('admin_refreshToken');
    localStorage.removeItem('admin_user');
    setUser(null);
  };

  if (loading) {
    return (
      <div className="login-page">
        <div className="loading"><div className="spinner" /></div>
      </div>
    );
  }

  if (!user) {
    return <Login onLogin={onLogin} />;
  }

  return (
    <Layout user={user} onLogout={onLogout}>
      {({ page }) => {
        if (page === 'dashboard') return <Dashboard />;
        if (page === 'users') return <Users />;
        if (page === 'products') return <Products />;
        if (page === 'category-requests') return <CategoryRequests />;
        if (page === 'categories') return <Categories />;
        return <Dashboard />;
      }}
    </Layout>
  );
}

export default App;
