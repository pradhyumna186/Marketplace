import React, { useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthSection = () => {
  const [activeTab, setActiveTab] = useState('login');
  const [response, setResponse] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [token, setToken] = useState(localStorage.getItem('accessToken'));

  useEffect(() => {
    setIsAuthenticated(!!token);
  }, [token]);

  const handleResponse = (promise) => {
    setResponse({ loading: true });
    promise
      .then(res => {
        // Handle nested response structure: res.data.data contains the actual data
        const responseData = res.data.data || res.data;
        if (responseData.accessToken) {
          localStorage.setItem('accessToken', responseData.accessToken);
          if (responseData.refreshToken) {
            localStorage.setItem('refreshToken', responseData.refreshToken);
          }
          // Store user data from login response
          if (responseData.user) {
            localStorage.setItem('userData', JSON.stringify(responseData.user));
          }
          setToken(responseData.accessToken);
          setIsAuthenticated(true);
        }
        setResponse({ success: true, data: res.data });
      })
      .catch(err => {
        setResponse({ 
          error: true, 
          data: err.response?.data || { message: err.message } 
        });
      });
  };

  const handleLogout = () => {
    if (token) {
      authAPI.logout().finally(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setToken(null);
        setIsAuthenticated(false);
        setResponse(null);
      });
    }
  };

  return (
    <div className="section">
      <h2>Authentication</h2>
      
      <div className={`auth-status ${isAuthenticated ? 'authenticated' : ''}`}>
        <span>Status: {isAuthenticated ? '✅ Authenticated' : '❌ Not Authenticated'}</span>
        {isAuthenticated && (
          <button className="btn btn-danger" onClick={handleLogout}>Logout</button>
        )}
      </div>

      <div className="nav-tabs">
        <button className={`nav-tab ${activeTab === 'login' ? 'active' : ''}`} onClick={() => setActiveTab('login')}>Login</button>
        <button className={`nav-tab ${activeTab === 'register' ? 'active' : ''}`} onClick={() => setActiveTab('register')}>Register</button>
        <button className={`nav-tab ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab('profile')}>Profile</button>
        <button className={`nav-tab ${activeTab === 'devices' ? 'active' : ''}`} onClick={() => setActiveTab('devices')}>Devices</button>
        <button className={`nav-tab ${activeTab === 'password' ? 'active' : ''}`} onClick={() => setActiveTab('password')}>Password</button>
      </div>

      {activeTab === 'login' && <LoginForm onResponse={handleResponse} />}
      {activeTab === 'register' && <RegisterForm onResponse={handleResponse} />}
      {activeTab === 'profile' && <ProfileForm onResponse={(promise) => {
        promise.then(res => {
          // Update stored user data after profile update
          const responseData = res.data.data || res.data;
          if (responseData && responseData.id) {
            localStorage.setItem('userData', JSON.stringify(responseData));
          }
          handleResponse(promise);
        }).catch(err => handleResponse(Promise.reject(err)));
      }} isAuthenticated={isAuthenticated} />}
      {activeTab === 'devices' && <DevicesSection onResponse={handleResponse} />}
      {activeTab === 'password' && <PasswordSection onResponse={handleResponse} />}

      {response && (
        <div className={`response ${response.error ? 'error' : response.success ? 'success' : ''}`}>
          <pre>{JSON.stringify(response.data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

const LoginForm = ({ onResponse }) => {
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
    rememberDevice: false
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    onResponse(authAPI.login(formData));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Username or Email</label>
        <input type="text" value={formData.usernameOrEmail} onChange={(e) => setFormData({...formData, usernameOrEmail: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Password</label>
        <input type="password" value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>
          <input type="checkbox" checked={formData.rememberDevice} onChange={(e) => setFormData({...formData, rememberDevice: e.target.checked})} />
          Remember Device
        </label>
      </div>
      <button type="submit" className="btn">Login</button>
    </form>
  );
};

const RegisterForm = ({ onResponse }) => {
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

  const handleSubmit = (e) => {
    e.preventDefault();
    onResponse(authAPI.register(formData));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>First Name *</label>
        <input type="text" value={formData.firstName} onChange={(e) => setFormData({...formData, firstName: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Last Name</label>
        <input type="text" value={formData.lastName} onChange={(e) => setFormData({...formData, lastName: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Display Name</label>
        <input type="text" value={formData.displayName} onChange={(e) => setFormData({...formData, displayName: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Username *</label>
        <input type="text" value={formData.username} onChange={(e) => setFormData({...formData, username: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Email *</label>
        <input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Password *</label>
        <input type="password" value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Apartment Number *</label>
        <input type="text" value={formData.apartmentNumber} onChange={(e) => setFormData({...formData, apartmentNumber: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Building Name *</label>
        <input type="text" value={formData.buildingName} onChange={(e) => setFormData({...formData, buildingName: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Phone Number</label>
        <input type="text" value={formData.phoneNumber} onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})} />
      </div>
      <div className="form-group">
        <label>
          <input type="checkbox" checked={formData.acceptTerms} onChange={(e) => setFormData({...formData, acceptTerms: e.target.checked})} required />
          Accept Terms *
        </label>
      </div>
      <button type="submit" className="btn">Register</button>
    </form>
  );
};

const ProfileForm = ({ onResponse, isAuthenticated }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    displayName: '',
    email: '',
    apartmentNumber: '',
    buildingName: '',
    phoneNumber: ''
  });

  // Load user data from localStorage when component mounts or when authenticated
  React.useEffect(() => {
    if (isAuthenticated) {
      const userDataStr = localStorage.getItem('userData');
      if (userDataStr) {
        try {
          const userData = JSON.parse(userDataStr);
          setFormData({
            firstName: userData.firstName || '',
            lastName: userData.lastName || '',
            displayName: userData.displayName || '',
            email: userData.email || '',
            apartmentNumber: userData.apartmentNumber || '',
            buildingName: userData.buildingName || '',
            phoneNumber: userData.phoneNumber || ''
          });
        } catch (e) {
          console.error('Error parsing user data:', e);
        }
      }
    }
  }, [isAuthenticated]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onResponse(authAPI.updateProfile(formData));
  };

  const loadProfile = () => {
    const userDataStr = localStorage.getItem('userData');
    if (userDataStr) {
      try {
        const userData = JSON.parse(userDataStr);
        setFormData({
          firstName: userData.firstName || '',
          lastName: userData.lastName || '',
          displayName: userData.displayName || '',
          email: userData.email || '',
          apartmentNumber: userData.apartmentNumber || '',
          buildingName: userData.buildingName || '',
          phoneNumber: userData.phoneNumber || ''
        });
        onResponse(Promise.resolve({ data: { data: userData } }));
      } catch (e) {
        onResponse(Promise.reject({ response: { data: { message: 'Error loading profile data' } } }));
      }
    } else {
      onResponse(Promise.reject({ response: { data: { message: 'No user data found. Please login first.' } } }));
    }
  };

  return (
    <div>
      <button className="btn" onClick={loadProfile} style={{ marginBottom: '15px' }}>Load Current Profile</button>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>First Name *</label>
          <input type="text" value={formData.firstName} onChange={(e) => setFormData({...formData, firstName: e.target.value})} required />
        </div>
        <div className="form-group">
          <label>Last Name</label>
          <input type="text" value={formData.lastName} onChange={(e) => setFormData({...formData, lastName: e.target.value})} />
        </div>
        <div className="form-group">
          <label>Display Name</label>
          <input type="text" value={formData.displayName} onChange={(e) => setFormData({...formData, displayName: e.target.value})} />
        </div>
        <div className="form-group">
          <label>Email *</label>
          <input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} required />
        </div>
        <div className="form-group">
          <label>Apartment Number *</label>
          <input type="text" value={formData.apartmentNumber} onChange={(e) => setFormData({...formData, apartmentNumber: e.target.value})} required />
        </div>
        <div className="form-group">
          <label>Building Name *</label>
          <input type="text" value={formData.buildingName} onChange={(e) => setFormData({...formData, buildingName: e.target.value})} required />
        </div>
        <div className="form-group">
          <label>Phone Number</label>
          <input type="text" value={formData.phoneNumber} onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})} />
        </div>
        <button type="submit" className="btn">Update Profile</button>
      </form>
    </div>
  );
};

const DevicesSection = ({ onResponse }) => {
  const [devices, setDevices] = useState([]);

  const loadDevices = () => {
    authAPI.getTrustedDevices()
      .then(res => {
        setDevices(res.data);
        onResponse(Promise.resolve(res));
      })
      .catch(err => onResponse(Promise.reject(err)));
  };

  const revokeDevice = (deviceId) => {
    authAPI.revokeDevice(deviceId)
      .then(() => loadDevices())
      .catch(err => onResponse(Promise.reject(err)));
  };

  return (
    <div>
      <button className="btn" onClick={loadDevices}>Get Trusted Devices</button>
      <button className="btn" onClick={() => onResponse(authAPI.logoutAll())}>Logout All Devices</button>
      {devices.length > 0 && (
        <div className="grid">
          {devices.map(device => (
            <div key={device.id} className="card">
              <h3>Device {device.id}</h3>
              <p>IP: {device.ipAddress}</p>
              <p>User Agent: {device.userAgent?.substring(0, 50)}...</p>
              <p>Trusted: {device.trusted ? 'Yes' : 'No'}</p>
              <button className="btn btn-danger" onClick={() => revokeDevice(device.id)}>Revoke</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const PasswordSection = ({ onResponse }) => {
  const [email, setEmail] = useState('');
  const [resetData, setResetData] = useState({ token: '', newPassword: '' });

  return (
    <div>
      <h3>Forgot Password</h3>
      <div className="form-group">
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      </div>
      <button className="btn" onClick={() => onResponse(authAPI.forgotPassword(email))}>Send Reset Email</button>

      <h3 style={{ marginTop: '20px' }}>Reset Password</h3>
      <div className="form-group">
        <label>Token</label>
        <input type="text" value={resetData.token} onChange={(e) => setResetData({...resetData, token: e.target.value})} />
      </div>
      <div className="form-group">
        <label>New Password</label>
        <input type="password" value={resetData.newPassword} onChange={(e) => setResetData({...resetData, newPassword: e.target.value})} />
      </div>
      <button className="btn" onClick={() => onResponse(authAPI.resetPassword(resetData))}>Reset Password</button>
    </div>
  );
};

export default AuthSection;

