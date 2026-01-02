import React, { useState } from 'react';
import { authAPI } from '../services/api';

const RegisterPage = ({ onRegister, onNavigate }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    displayName: '',
    username: '',
    email: '',
    password: '',
    apartmentNumber: '',
    buildingName: '',
    phoneNumber: '',
    acceptTerms: false
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await authAPI.register(formData);
      setSuccess(true);
      setTimeout(() => {
        onNavigate('login');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div style={{ maxWidth: '500px', margin: '0 auto' }}>
        <div className="card">
          <div className="alert alert-success">
            <div>
              <strong>Registration Successful!</strong>
              <p style={{ marginTop: '0.5rem' }}>
                Please check your email to verify your account before logging in.
              </p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <div className="card">
        <h2 className="card-title">Join StoneRidge Marketplace</h2>
        <p style={{ color: 'var(--text-light)', marginBottom: '1.5rem' }}>
          Create an account to buy and sell items in your community
        </p>
        
        {error && (
          <div className="alert alert-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>First Name *</label>
              <input
                type="text"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Display Name</label>
            <input
              type="text"
              value={formData.displayName}
              onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
              placeholder="How you want to be displayed (optional)"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Username *</label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Email *</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Password *</label>
            <input
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              required
              minLength={6}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Building Name *</label>
              <input
                type="text"
                value={formData.buildingName}
                onChange={(e) => setFormData({ ...formData, buildingName: e.target.value })}
                required
                placeholder="e.g., Building AA"
              />
            </div>
            <div className="form-group">
              <label>Apartment Number *</label>
              <input
                type="text"
                value={formData.apartmentNumber}
                onChange={(e) => setFormData({ ...formData, apartmentNumber: e.target.value })}
                required
                placeholder="e.g., 101"
              />
            </div>
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="tel"
              value={formData.phoneNumber}
              onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              placeholder="Optional"
            />
          </div>

          <div className="form-group">
            <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={formData.acceptTerms}
                onChange={(e) => setFormData({ ...formData, acceptTerms: e.target.checked })}
                required
                style={{ marginRight: '0.5rem' }}
              />
              <span>
                I accept the <a href="#" onClick={(e) => { e.preventDefault(); alert('Terms and Conditions would be displayed here'); }} style={{ color: 'var(--primary)', textDecoration: 'underline' }}>Terms and Conditions</a> *
              </span>
            </label>
          </div>

          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%' }} 
            disabled={loading || !formData.acceptTerms}
          >
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-light)' }}>
          <p>
            Already have an account?{' '}
            <button 
              className="btn btn-outline btn-sm" 
              onClick={() => onNavigate('login')}
              style={{ padding: '0.25rem 0.75rem' }}
            >
              Login
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;

