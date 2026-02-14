import React, { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';

export default function Users() {
  const [users, setUsers] = useState({ content: [] });
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const load = () => {
    setLoading(true);
    setError(null);
    adminAPI.getUsers(search, { page: 0, size: 50 })
      .then((res) => setUsers(res.data?.data || res.data))
      .catch((err) => setError(err.response?.data?.message || err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleAction = (id, action) => {
    const fn = { suspend: adminAPI.suspendUser, unsuspend: adminAPI.unsuspendUser, lock: adminAPI.lockUser, unlock: adminAPI.unlockUser }[action];
    fn(id).then(() => load()).catch((err) => setError(err.response?.data?.message || err.message));
  };

  const list = users.content || [];

  return (
    <>
      <h1 className="page-title">Users</h1>
      {error && <div className="alert-error">{error}</div>}
      <div className="card">
        <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
          <input
            type="text"
            placeholder="Search by email or username"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ maxWidth: '280px' }}
          />
          <button type="button" className="btn-primary" onClick={load}>Search</button>
        </div>
        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table>
              <thead>
                <tr>
                  <th>User</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {list.map((u) => (
                  <tr key={u.id}>
                    <td>{u.displayName || u.username}</td>
                    <td>{u.email}</td>
                    <td>{u.role}</td>
                    <td>
                      {!u.enabled && <span style={{ color: 'var(--admin-danger)' }}>Suspended</span>}
                      {u.enabled && !u.accountNonLocked && <span style={{ color: 'var(--admin-warning)' }}>Locked</span>}
                      {u.enabled && u.accountNonLocked && <span style={{ color: 'var(--admin-success)' }}>Active</span>}
                    </td>
                    <td className="actions-cell">
                      {u.role !== 'ADMIN' && (
                        <>
                          {u.enabled ? (
                            <button type="button" className="btn-danger btn-sm" onClick={() => handleAction(u.id, 'suspend')}>Suspend</button>
                          ) : (
                            <button type="button" className="btn-success btn-sm" onClick={() => handleAction(u.id, 'unsuspend')}>Unsuspend</button>
                          )}
                          {u.accountNonLocked ? (
                            <button type="button" className="btn-danger btn-sm" onClick={() => handleAction(u.id, 'lock')}>Lock</button>
                          ) : (
                            <button type="button" className="btn-success btn-sm" onClick={() => handleAction(u.id, 'unlock')}>Unlock</button>
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
    </>
  );
}
