import React, { useState } from 'react';
import { authAPI } from '../services/api';

const LoginPage = ({ onLogin, onNavigate }) => {
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
    rememberDevice: false
  });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await authAPI.login(formData);
      const data = response.data?.data || response.data;
      
      if (data.accessToken) {
        onLogin(data.user, {
          accessToken: data.accessToken,
          refreshToken: data.refreshToken
        });
      } else {
        setError('Login failed. Please try again.');
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '500px', margin: '0 auto' }}>
      <div className="card">
        <h2 className="card-title">Login to StoneRidge Marketplace</h2>
        
        {error && (
          <div className="alert alert-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username or Email</label>
            <input
              type="text"
              value={formData.usernameOrEmail}
              onChange={(e) => setFormData({ ...formData, usernameOrEmail: e.target.value })}
              required
              placeholder="Enter your username or email"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              required
              placeholder="Enter your password"
            />
          </div>

          <div className="form-group">
            <label>
              <input
                type="checkbox"
                checked={formData.rememberDevice}
                onChange={(e) => setFormData({ ...formData, rememberDevice: e.target.checked })}
              />
              Remember this device
            </label>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-light)' }}>
          <p>
            Don't have an account?{' '}
            <button 
              className="btn btn-outline btn-sm" 
              onClick={() => onNavigate('register')}
              style={{ padding: '0.25rem 0.75rem' }}
            >
              Register
            </button>
          </p>
          <p style={{ marginTop: '0.5rem' }}>
            <button 
              className="btn btn-outline btn-sm" 
              onClick={() => {
                const email = prompt('Enter your email to reset password:');
                if (email) {
                  authAPI.forgotPassword(email)
                    .then(() => alert('Password reset email sent!'))
                    .catch(err => alert(err.response?.data?.message || 'Error sending reset email'));
                }
              }}
              style={{ padding: '0.25rem 0.75rem' }}
            >
              Forgot Password?
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;

