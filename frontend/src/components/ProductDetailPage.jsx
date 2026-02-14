import React, { useState, useEffect } from 'react';
import { productAPI, chatAPI } from '../services/api';

const ProductDetailPage = ({ productId, onBack, isAuthenticated, user, onNavigate }) => {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [chatStarted, setChatStarted] = useState(false);

  useEffect(() => {
    loadProduct();
  }, [productId]);

  const loadProduct = async () => {
    setLoading(true);
    try {
      const res = await productAPI.getById(productId);
      setProduct(res.data?.data || res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load product');
    } finally {
      setLoading(false);
    }
  };

  const handleStartChat = async () => {
    if (!isAuthenticated) {
      onNavigate('login');
      return;
    }

    try {
      await chatAPI.startChat(productId);
      setChatStarted(true);
      onNavigate('chats');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to start chat');
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="card">
        <div className="alert alert-error">
          {error || 'Product not found'}
        </div>
        <button className="btn btn-primary" onClick={onBack}>Go Back</button>
      </div>
    );
  }

  const isOwner = user && product.sellerId === user.id;
  const imageUrls = product.imageUrls || [];

  return (
    <div>
      <button className="btn btn-secondary mb-2" onClick={onBack}>
        ← Back
      </button>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
        {/* Images */}
        <div className="card">
          {imageUrls.length > 0 ? (
            <div>
              <img 
                src={imageUrls[0]} 
                alt={product.title} 
                style={{ width: '100%', borderRadius: 'var(--radius)', marginBottom: '1rem' }}
              />
              {imageUrls.length > 1 && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.5rem' }}>
                  {imageUrls.slice(1, 5).map((url, idx) => (
                    <img 
                      key={idx}
                      src={url} 
                      alt={`${product.title} ${idx + 2}`}
                      style={{ width: '100%', aspectRatio: '1', objectFit: 'cover', borderRadius: 'var(--radius-sm)', cursor: 'pointer' }}
                    />
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="product-image-placeholder" style={{ height: '400px' }}>
              📦
            </div>
          )}
        </div>

        {/* Product Info */}
        <div>
          <div className="card">
            <h1 style={{ fontSize: '2rem', marginBottom: '1rem' }}>{product.title}</h1>
            
            <div style={{ marginBottom: '1rem' }}>
              <div className="product-price" style={{ fontSize: '2rem' }}>
                ${product.price?.toFixed(2)}
              </div>
              {product.originalPrice && product.originalPrice > product.price && (
                <div style={{ fontSize: '1rem', color: 'var(--text-light)', textDecoration: 'line-through' }}>
                  ${product.originalPrice.toFixed(2)}
                </div>
              )}
            </div>

            <div style={{ marginBottom: '1.5rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <span className="product-badge" style={{ background: '#dbeafe', color: '#1e40af' }}>
                {product.categoryName || 'Uncategorized'}
              </span>
              <span className="product-badge" style={{ background: '#fef3c7', color: '#92400e' }}>
                {product.condition}
              </span>
              {product.negotiable && (
                <span className="product-badge badge-negotiable">Negotiable</span>
              )}
              {product.status === 'SOLD' && (
                <span className="product-badge badge-sold">Sold</span>
              )}
            </div>

            <div style={{ marginBottom: '1.5rem', padding: '1rem', background: 'var(--bg)', borderRadius: 'var(--radius-sm)' }}>
              <h3 style={{ marginBottom: '0.5rem' }}>Seller Information</h3>
              <p><strong>Name:</strong> {product.sellerName || 'Unknown'}</p>
              <p><strong>Building:</strong> {product.sellerBuilding || 'N/A'}</p>
              <p><strong>Apartment:</strong> {product.sellerApartment || 'N/A'}</p>
            </div>

            {!isOwner && product.status === 'ACTIVE' && (
              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button className="btn btn-primary btn-lg" onClick={handleStartChat} style={{ flex: 1 }}>
                  💬 Message Seller
                </button>
              </div>
            )}

            {isOwner && (
              <div className="alert alert-info">
                This is your product. You can edit it from "My Products" page.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Description */}
      <div className="card">
        <h2 style={{ marginBottom: '1rem' }}>Description</h2>
        <p style={{ whiteSpace: 'pre-wrap', lineHeight: '1.8' }}>{product.description}</p>
      </div>

      {/* Additional Details */}
      <div className="card">
        <h2 style={{ marginBottom: '1rem' }}>Product Details</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          <div>
            <strong>Condition:</strong> {product.condition}
          </div>
          <div>
            <strong>Status:</strong> {product.status}
          </div>
          <div>
            <strong>Negotiable:</strong> {product.negotiable ? 'Yes' : 'No'}
          </div>
          {product.locationDetails && (
            <div>
              <strong>Location:</strong> {product.locationDetails}
            </div>
          )}
          {product.viewCount !== undefined && (
            <div>
              <strong>Views:</strong> {product.viewCount}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;

