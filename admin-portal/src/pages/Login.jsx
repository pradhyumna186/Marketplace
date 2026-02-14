import React, { useState } from 'react';
import { authAPI } from '../services/api';

export default function Login({ onLogin }) {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(() => {
    const msg = sessionStorage.getItem('admin_403_message');
    if (msg) {
      sessionStorage.removeItem('admin_403_message');
      return msg;
    }
    return '';
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await authAPI.login({ usernameOrEmail, password, rememberDevice: false });
      const data = res.data?.data || res.data;
      if (!data.accessToken || !data.user) {
        setError('Invalid response from server.');
        return;
      }
      if (data.user.role !== 'ADMIN') {
        setError('Only administrators can access this portal. Use the main marketplace to log in as a user.');
        return;
      }
      onLogin(data.user, { accessToken: data.accessToken, refreshToken: data.refreshToken });
    } catch (err) {
      if (err.response?.status === 403) {
        setError('Access denied (403). Your account does not have admin privileges. Update your role in the database or contact an administrator.');
      } else {
        setError(err.response?.data?.message || err.message || 'Login failed.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-box">
        <h1>Admin Portal</h1>
        <p>StoneRidge Marketplace – sign in with an admin account.</p>
        {error && <div className="alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username or email</label>
            <input
              type="text"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              required
              autoComplete="username"
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </div>
          <button type="submit" className="btn-primary" style={{ width: '100%', padding: '0.75rem' }} disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
      </div>
    </div>
  );
}
