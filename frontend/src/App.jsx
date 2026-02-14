import React, { useState, useEffect } from 'react';
import { authAPI } from './services/api';
import HomePage from './components/HomePage';
import ProductsPage from './components/ProductsPage';
import ProductDetailPage from './components/ProductDetailPage';
import CreateProductPage from './components/CreateProductPage';
import MyProductsPage from './components/MyProductsPage';
import ChatPage from './components/ChatPage';
import ProfilePage from './components/ProfilePage';
import CategoriesPage from './components/CategoriesPage';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import './index.css';

function App() {
  const [currentView, setCurrentView] = useState('home');
  const [selectedProductId, setSelectedProductId] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check authentication status
    const token = localStorage.getItem('accessToken');
    const userData = localStorage.getItem('userData');
    
    if (token && userData) {
      try {
        const userObj = JSON.parse(userData);
        setIsAuthenticated(true);
        setUser(userObj);
      } catch (e) {
        console.error('Error parsing user data:', e);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userData');
      }
    }
    setLoading(false);
  }, []);

  const handleLogin = (userData, tokens) => {
    setIsAuthenticated(true);
    setUser(userData);
    if (tokens.accessToken) {
      localStorage.setItem('accessToken', tokens.accessToken);
    }
    if (tokens.refreshToken) {
      localStorage.setItem('refreshToken', tokens.refreshToken);
    }
    if (userData) {
      localStorage.setItem('userData', JSON.stringify(userData));
    }
    setCurrentView('home');
  };

  const handleLogout = () => {
    authAPI.logout().finally(() => {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userData');
      setIsAuthenticated(false);
      setUser(null);
      setCurrentView('home');
    });
  };

  const handleViewProduct = (productId) => {
    setSelectedProductId(productId);
    setCurrentView('product-detail');
  };

  const handleBack = () => {
    setSelectedProductId(null);
    if (currentView === 'product-detail') {
      setCurrentView('home');
    } else {
      setCurrentView('home');
    }
  };

  if (loading) {
    return (
      <div className="app">
        <div className="loading">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }

  // Show login/register if not authenticated and trying to access protected pages
  const protectedViews = ['create-product', 'my-products', 'chats', 'profile', 'categories'];
  if (!isAuthenticated && protectedViews.includes(currentView)) {
    return (
      <div className="app">
        <div className="header">
          <div className="header-content">
            <div className="logo">
              <div className="logo-icon">🏠</div>
              <span>StoneRidge Marketplace</span>
            </div>
          </div>
        </div>
        <div className="main-content">
          <div className="card">
            <h2 className="card-title">Please Login</h2>
            <p className="text-center mt-2">You need to be logged in to access this page.</p>
            <div className="flex gap-2 mt-3" style={{ justifyContent: 'center' }}>
              <button className="btn btn-primary" onClick={() => setCurrentView('login')}>
                Login
              </button>
              <button className="btn btn-outline" onClick={() => setCurrentView('register')}>
                Register
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app">
      {/* Header */}
      <div className="header">
        <div className="header-content">
          <div className="logo" onClick={() => setCurrentView('home')} style={{ cursor: 'pointer' }}>
            <div className="logo-icon">🏠</div>
            <span>StoneRidge Marketplace</span>
          </div>
          
          <div className="header-actions">
            {isAuthenticated ? (
              <>
                <div className="user-info">
                  <div className="user-avatar">
                    {user?.displayName?.charAt(0)?.toUpperCase() || user?.firstName?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  <span>{user?.displayName || user?.firstName || 'User'}</span>
                </div>
                <button className="btn btn-sm btn-secondary" onClick={handleLogout}>
                  Logout
                </button>
              </>
            ) : (
              <>
                <button className="btn btn-sm btn-outline" onClick={() => setCurrentView('login')}>
                  Login
                </button>
                <button className="btn btn-sm btn-primary" onClick={() => setCurrentView('register')}>
                  Register
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Navigation */}
      {isAuthenticated && (
        <div className="nav">
          <div className="nav-tabs">
            <button 
              className={`nav-tab ${currentView === 'home' ? 'active' : ''}`}
              onClick={() => setCurrentView('home')}
            >
              🏠 Home
            </button>
            <button 
              className={`nav-tab ${currentView === 'products' ? 'active' : ''}`}
              onClick={() => setCurrentView('products')}
            >
              🔍 Browse Products
            </button>
            <button 
              className={`nav-tab ${currentView === 'create-product' ? 'active' : ''}`}
              onClick={() => setCurrentView('create-product')}
            >
              ➕ Sell Item
            </button>
            <button 
              className={`nav-tab ${currentView === 'my-products' ? 'active' : ''}`}
              onClick={() => setCurrentView('my-products')}
            >
              📦 My Products
            </button>
            <button 
              className={`nav-tab ${currentView === 'chats' ? 'active' : ''}`}
              onClick={() => setCurrentView('chats')}
            >
              💬 Messages
            </button>
            <button 
              className={`nav-tab ${currentView === 'categories' ? 'active' : ''}`}
              onClick={() => setCurrentView('categories')}
            >
              📂 Categories
            </button>
            <button 
              className={`nav-tab ${currentView === 'profile' ? 'active' : ''}`}
              onClick={() => setCurrentView('profile')}
            >
              👤 Profile
            </button>
          </div>
        </div>
      )}

      {/* Main Content */}
      <div className="main-content">
        {currentView === 'home' && (
          <HomePage 
            isAuthenticated={isAuthenticated}
            onViewProduct={handleViewProduct}
            onNavigate={setCurrentView}
          />
        )}
        
        {currentView === 'login' && (
          <LoginPage 
            onLogin={handleLogin}
            onNavigate={setCurrentView}
          />
        )}
        
        {currentView === 'register' && (
          <RegisterPage 
            onRegister={(userData) => {
              setCurrentView('login');
            }}
            onNavigate={setCurrentView}
          />
        )}
        
        {currentView === 'products' && (
          <ProductsPage 
            onViewProduct={handleViewProduct}
            isAuthenticated={isAuthenticated}
          />
        )}
        
        {currentView === 'product-detail' && selectedProductId && (
          <ProductDetailPage 
            productId={selectedProductId}
            onBack={handleBack}
            isAuthenticated={isAuthenticated}
            user={user}
            onNavigate={setCurrentView}
          />
        )}
        
        {currentView === 'create-product' && (
          <CreateProductPage 
            onBack={handleBack}
            onProductCreated={(productId) => {
              setSelectedProductId(productId);
              setCurrentView('product-detail');
            }}
          />
        )}
        
        {currentView === 'my-products' && (
          <MyProductsPage 
            onViewProduct={handleViewProduct}
            onBack={handleBack}
          />
        )}
        
        {currentView === 'chats' && (
          <ChatPage 
            user={user}
          />
        )}

        {currentView === 'categories' && (
          <CategoriesPage onNavigate={setCurrentView} />
        )}
        
        {currentView === 'profile' && (
          <ProfilePage 
            user={user}
            onUserUpdate={setUser}
          />
        )}
      </div>
    </div>
  );
}

export default App;
