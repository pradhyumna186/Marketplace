import React, { useState, useEffect } from 'react';
import { productAPI, categoryAPI } from '../services/api';

const HomePage = ({ isAuthenticated, onViewProduct, onNavigate }) => {
  const [trending, setTrending] = useState([]);
  const [recent, setRecent] = useState([]);
  const [categories, setCategories] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [trendingRes, recentRes, categoriesRes] = await Promise.all([
        productAPI.getTrending(8),
        productAPI.getRecent(8),
        categoryAPI.getAll()
      ]);
      
      setTrending(trendingRes.data?.data || []);
      setRecent(recentRes.data?.data || []);
      setCategories(categoriesRes.data?.data || []);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      onNavigate('products');
      // Search will be handled in ProductsPage
      setTimeout(() => {
        const event = new CustomEvent('search', { detail: searchQuery });
        window.dispatchEvent(event);
      }, 100);
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div>
      {/* Hero Section */}
      <div className="card" style={{ background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)', color: 'white', marginBottom: '2rem' }}>
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <h1 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>Welcome to StoneRidge Marketplace</h1>
          <p style={{ fontSize: '1.25rem', marginBottom: '2rem', opacity: 0.9 }}>
            Buy and sell items within your community
          </p>
          
          <form onSubmit={handleSearch} className="search-bar" style={{ maxWidth: '600px', margin: '0 auto' }}>
            <input
              type="text"
              className="search-input"
              placeholder="Search for products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ background: 'white', color: '#1e293b' }}
            />
            <button type="submit" className="btn btn-lg" style={{ background: 'white', color: '#6366f1' }}>
              Search
            </button>
          </form>
        </div>
      </div>

      {/* Categories */}
      {categories.length > 0 && (
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ marginBottom: '1rem' }}>Browse by Category</h2>
          <div className="products-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))' }}>
            {categories.map(category => (
              <div
                key={category.id}
                className="card"
                onClick={() => {
                  onNavigate('products');
                  setTimeout(() => {
                    const event = new CustomEvent('filterByCategory', { detail: category.id });
                    window.dispatchEvent(event);
                  }, 100);
                }}
                style={{ cursor: 'pointer', textAlign: 'center', padding: '1.5rem' }}
              >
                {category.iconUrl ? (
                  <img src={category.iconUrl} alt={category.name} style={{ width: '64px', height: '64px', marginBottom: '0.5rem' }} />
                ) : (
                  <div style={{ fontSize: '3rem', marginBottom: '0.5rem' }}>📦</div>
                )}
                <div style={{ fontWeight: 600 }}>{category.name}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Trending Products */}
      {trending.length > 0 && (
        <div style={{ marginBottom: '2rem' }}>
          <div className="flex-between" style={{ marginBottom: '1rem' }}>
            <h2>🔥 Trending Products</h2>
            <button className="btn btn-outline btn-sm" onClick={() => onNavigate('products')}>
              View All
            </button>
          </div>
          <div className="products-grid">
            {trending.map(product => (
              <ProductCard key={product.id} product={product} onView={onViewProduct} />
            ))}
          </div>
        </div>
      )}

      {/* Recent Products */}
      {recent.length > 0 && (
        <div>
          <div className="flex-between" style={{ marginBottom: '1rem' }}>
            <h2>✨ Recently Added</h2>
            <button className="btn btn-outline btn-sm" onClick={() => onNavigate('products')}>
              View All
            </button>
          </div>
          <div className="products-grid">
            {recent.map(product => (
              <ProductCard key={product.id} product={product} onView={onViewProduct} />
            ))}
          </div>
        </div>
      )}

      {/* Empty State */}
      {trending.length === 0 && recent.length === 0 && (
        <div className="empty-state">
          <div className="empty-state-icon">📦</div>
          <h3>No products yet</h3>
          <p>Be the first to list an item!</p>
          {isAuthenticated && (
            <button className="btn btn-primary mt-2" onClick={() => onNavigate('create-product')}>
              List Your First Item
            </button>
          )}
        </div>
      )}
    </div>
  );
};

const ProductCard = ({ product, onView }) => {
  const imageUrl = product.imageUrls && product.imageUrls.length > 0 
    ? product.imageUrls[0] 
    : null;

  return (
    <div className="product-card" onClick={() => onView(product.id)}>
      {imageUrl ? (
        <img src={imageUrl} alt={product.title} className="product-image" />
      ) : (
        <div className="product-image-placeholder">📦</div>
      )}
      <div className="product-info">
        <div className="product-title">{product.title}</div>
        <div className="product-price">${product.price?.toFixed(2)}</div>
        <div className="product-meta">
          <span>{product.categoryName || 'Uncategorized'}</span>
          {product.negotiable && (
            <span className="product-badge badge-negotiable">Negotiable</span>
          )}
        </div>
      </div>
    </div>
  );
};

export default HomePage;

