import React, { useState, useEffect } from 'react';
import { productAPI } from '../services/api';

const MyProductsPage = ({ onViewProduct, onBack }) => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('ACTIVE');
  const [selectedProduct, setSelectedProduct] = useState(null);

  useEffect(() => {
    loadProducts();
  }, [status]);

  const loadProducts = async () => {
    setLoading(true);
    try {
      const res = await productAPI.getMyProducts({ status, page: 0, size: 50 });
      const data = res.data?.data || {};
      setProducts(data.content || []);
    } catch (error) {
      console.error('Error loading products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (productId) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }

    try {
      await productAPI.delete(productId);
      loadProducts();
    } catch (error) {
      alert('Failed to delete product: ' + (error.response?.data?.message || error.message));
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
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="flex-between">
          <h2>My Products</h2>
          <div className="form-group" style={{ margin: 0, width: '200px' }}>
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="ACTIVE">Active</option>
              <option value="SOLD">Sold</option>
              <option value="PENDING">Pending</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </div>
        </div>
      </div>

      {products.length > 0 ? (
        <div className="products-grid">
          {products.map(product => (
            <div key={product.id} className="product-card">
              {product.imageUrls && product.imageUrls.length > 0 ? (
                <img src={product.imageUrls[0]} alt={product.title} className="product-image" />
              ) : (
                <div className="product-image-placeholder">📦</div>
              )}
              <div className="product-info">
                <div className="product-title">{product.title}</div>
                <div className="product-price">${product.price?.toFixed(2)}</div>
                <div className="product-meta">
                  <span className="product-badge" style={{ 
                    background: product.status === 'SOLD' ? '#fee2e2' : '#dbeafe',
                    color: product.status === 'SOLD' ? '#991b1b' : '#1e40af'
                  }}>
                    {product.status}
                  </span>
                  {product.viewCount !== undefined && (
                    <span>👁️ {product.viewCount} views</span>
                  )}
                </div>
                <div style={{ marginTop: '0.75rem', display: 'flex', gap: '0.5rem' }}>
                  <button 
                    className="btn btn-sm btn-primary" 
                    onClick={() => onViewProduct(product.id)}
                    style={{ flex: 1 }}
                  >
                    View
                  </button>
                  <button 
                    className="btn btn-sm btn-danger" 
                    onClick={() => handleDelete(product.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-state-icon">📦</div>
          <h3>No products found</h3>
          <p>You haven't listed any products with status "{status}" yet.</p>
        </div>
      )}
    </div>
  );
};

export default MyProductsPage;

