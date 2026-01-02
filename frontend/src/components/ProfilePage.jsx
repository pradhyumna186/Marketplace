import React, { useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const ProfilePage = ({ user, onUserUpdate }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    displayName: '',
    email: '',
    apartmentNumber: '',
    buildingName: '',
    phoneNumber: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [trustedDevices, setTrustedDevices] = useState([]);
  const [showDevices, setShowDevices] = useState(false);

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        displayName: user.displayName || '',
        email: user.email || '',
        apartmentNumber: user.apartmentNumber || '',
        buildingName: user.buildingName || '',
        phoneNumber: user.phoneNumber || ''
      });
    }
  }, [user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);

    try {
      const res = await authAPI.updateProfile(formData);
      const updatedUser = res.data?.data || res.data;
      onUserUpdate(updatedUser);
      localStorage.setItem('userData', JSON.stringify(updatedUser));
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const loadTrustedDevices = async () => {
    try {
      const res = await authAPI.getTrustedDevices();
      setTrustedDevices(res.data?.data || []);
      setShowDevices(true);
    } catch (error) {
      alert('Failed to load devices: ' + (error.response?.data?.message || error.message));
    }
  };

  const revokeDevice = async (deviceId) => {
    if (!window.confirm('Are you sure you want to revoke this device?')) {
      return;
    }

    try {
      await authAPI.revokeDevice(deviceId);
      loadTrustedDevices();
    } catch (error) {
      alert('Failed to revoke device: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleLogoutAll = async () => {
    if (!window.confirm('Are you sure you want to logout from all devices?')) {
      return;
    }

    try {
      await authAPI.logoutAll();
      loadTrustedDevices();
      alert('Logged out from all devices successfully');
    } catch (error) {
      alert('Failed to logout from all devices: ' + (error.response?.data?.message || error.message));
    }
  };

  return (
    <div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        {/* Profile Form */}
        <div className="card">
          <h2 className="card-title">Profile Information</h2>
          
          {success && (
            <div className="alert alert-success">
              Profile updated successfully!
            </div>
          )}

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
                placeholder="How you want to be displayed"
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
              <p style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
                Changing email will require verification
              </p>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Building Name *</label>
                <input
                  type="text"
                  value={formData.buildingName}
                  onChange={(e) => setFormData({ ...formData, buildingName: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Apartment Number *</label>
                <input
                  type="text"
                  value={formData.apartmentNumber}
                  onChange={(e) => setFormData({ ...formData, apartmentNumber: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Phone Number</label>
              <input
                type="tel"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              />
            </div>

            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Updating...' : 'Update Profile'}
            </button>
          </form>
        </div>

        {/* Account Settings */}
        <div>
          <div className="card" style={{ marginBottom: '1.5rem' }}>
            <h2 className="card-title">Account Settings</h2>
            
            <div style={{ marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Trusted Devices</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-light)', marginBottom: '0.75rem' }}>
                Manage devices that can access your account
              </p>
              <button className="btn btn-outline btn-sm" onClick={loadTrustedDevices}>
                View Devices
              </button>
            </div>

            {showDevices && trustedDevices.length > 0 && (
              <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid var(--border)' }}>
                {trustedDevices.map(device => (
                  <div key={device.id} style={{ 
                    padding: '0.75rem', 
                    background: 'var(--bg)', 
                    borderRadius: 'var(--radius-sm)',
                    marginBottom: '0.5rem',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{device.deviceName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-light)' }}>
                        {device.deviceType} • Last used: {new Date(device.lastUsedAt).toLocaleDateString()}
                      </div>
                    </div>
                    <button 
                      className="btn btn-sm btn-danger"
                      onClick={() => revokeDevice(device.id)}
                    >
                      Revoke
                    </button>
                  </div>
                ))}
                <button 
                  className="btn btn-sm btn-secondary" 
                  onClick={handleLogoutAll}
                  style={{ width: '100%', marginTop: '0.5rem' }}
                >
                  Logout All Devices
                </button>
              </div>
            )}

            <div style={{ marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border)' }}>
              <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Danger Zone</h3>
              <button 
                className="btn btn-danger btn-sm"
                onClick={async () => {
                  if (window.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
                    try {
                      await authAPI.deleteAccount();
                      alert('Account deleted. Redirecting...');
                      window.location.reload();
                    } catch (error) {
                      alert('Failed to delete account: ' + (error.response?.data?.message || error.message));
                    }
                  }
                }}
              >
                Delete Account
              </button>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">User Information</h2>
            <div style={{ display: 'grid', gap: '0.75rem' }}>
              <div>
                <strong>Username:</strong> {user?.username}
              </div>
              <div>
                <strong>Email Verified:</strong> {user?.emailVerified ? '✅ Yes' : '❌ No'}
              </div>
              <div>
                <strong>Phone Verified:</strong> {user?.phoneVerified ? '✅ Yes' : '❌ No'}
              </div>
              <div>
                <strong>Role:</strong> {user?.role || 'USER'}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;

