import React, { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    adminAPI.getDashboard()
      .then((res) => { if (!cancelled) setData(res.data?.data || res.data); })
      .catch((err) => { if (!cancelled) setError(err.response?.data?.message || err.message); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  if (loading) return <div className="loading"><div className="spinner" /></div>;
  if (error) return <div className="alert-error">{error}</div>;
  if (!data) return null;

  return (
    <>
      <h1 className="page-title">Dashboard</h1>
      <div className="stat-grid">
        <div className="stat-card">
          <div className="value">{data.totalUsers}</div>
          <div className="label">Total Users</div>
        </div>
        <div className="stat-card">
          <div className="value">{data.totalProducts}</div>
          <div className="label">Total Products</div>
        </div>
        <div className="stat-card">
          <div className="value">{data.activeProducts}</div>
          <div className="label">Active Listings</div>
        </div>
        <div className="stat-card">
          <div className="value">{data.totalCategories}</div>
          <div className="label">Categories</div>
        </div>
        <div className="stat-card highlight">
          <div className="value">{data.pendingCategoryRequests}</div>
          <div className="label">Pending Category Requests</div>
        </div>
      </div>
    </>
  );
}
