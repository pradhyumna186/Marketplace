import React, { useState, useEffect } from 'react';
import { productAPI, categoryAPI, fileAPI, authAPI } from '../services/api';

const CreateProductPage = ({ onBack, onProductCreated }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: '',
    originalPrice: '',
    condition: 'GOOD',
    categoryId: '',
    negotiable: true,
    locationDetails: ''
  });
  const [categories, setCategories] = useState([]);
  const [imageFiles, setImageFiles] = useState([]);
  const [imageUrls, setImageUrls] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const res = await categoryAPI.getAll();
      setCategories(res.data?.data || []);
    } catch (error) {
      console.error('Error loading categories:', error);
    }
  };

  const handleImageUpload = async (files, retried = false) => {
    if (files.length === 0) return;
    const token = localStorage.getItem('accessToken');
    if (!token && !localStorage.getItem('refreshToken')) {
      alert('Please log in to upload images.');
      return;
    }
    setLoading(true);
    try {
      const res = await fileAPI.uploadProductImages(Array.from(files));
      const uploadedUrls = res.data?.data || [];
      setImageUrls((prev) => [...prev, ...uploadedUrls]);
    } catch (err) {
      const status = err.response?.status;
      const msg = err.response?.data?.message || err.message;
      if ((status === 401 || status === 403) && !retried) {
        const refresh = localStorage.getItem('refreshToken');
        if (refresh) {
          try {
            const refreshRes = await authAPI.refreshToken(refresh);
            const data = refreshRes.data?.data || refreshRes.data;
            if (data?.accessToken) {
              localStorage.setItem('accessToken', data.accessToken);
              return handleImageUpload(files, true);
            }
          } catch (_) {}
        }
        alert('Session expired or access denied. Please log in again and try uploading.');
      } else if (status === 401 || status === 403) {
        alert('Session expired or access denied. Please log in again and try uploading.');
      } else {
        alert('Failed to upload images: ' + msg);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const data = {
        ...formData,
        price: parseFloat(formData.price),
        originalPrice: formData.originalPrice ? parseFloat(formData.originalPrice) : null,
        categoryId: parseInt(formData.categoryId),
        imageUrls: imageUrls
      };

      const res = await productAPI.create(data);
      const product = res.data?.data || res.data;
      setSuccess(true);
      
      setTimeout(() => {
        onProductCreated(product.id);
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create product');
    } finally {
      setLoading(false);
    }
  };

  const removeImage = (index) => {
    setImageUrls(imageUrls.filter((_, i) => i !== index));
  };

  if (success) {
    return (
      <div>
        <button className="btn btn-secondary mb-2" onClick={onBack}>← Back</button>
        <div className="card">
          <div className="alert alert-success">
            <strong>Product created successfully!</strong>
            <p style={{ marginTop: '0.5rem' }}>Redirecting to product page...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <button className="btn btn-secondary mb-2" onClick={onBack}>← Back</button>
      
      <div className="card">
        <h2 className="card-title">List a New Product</h2>
        
        {error && (
          <div className="alert alert-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Product Title *</label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
              placeholder="e.g., Vintage Coffee Table"
            />
          </div>

          <div className="form-group">
            <label>Description *</label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              required
              rows={6}
              placeholder="Describe your product in detail..."
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Price ($) *</label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.price}
                onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Original Price ($)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.originalPrice}
                onChange={(e) => setFormData({ ...formData, originalPrice: e.target.value })}
                placeholder="Optional"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Condition *</label>
              <select
                value={formData.condition}
                onChange={(e) => setFormData({ ...formData, condition: e.target.value })}
                required
              >
                <option value="NEW">New</option>
                <option value="LIKE_NEW">Like New</option>
                <option value="GOOD">Good</option>
                <option value="FAIR">Fair</option>
                <option value="POOR">Poor</option>
              </select>
            </div>
            <div className="form-group">
              <label>Category *</label>
              <select
                value={formData.categoryId}
                onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                required
              >
                <option value="">Select a category</option>
                {categories.map(cat => (
                  <option key={cat.id} value={cat.id}>{cat.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Location Details</label>
            <input
              type="text"
              value={formData.locationDetails}
              onChange={(e) => setFormData({ ...formData, locationDetails: e.target.value })}
              placeholder="e.g., Building AA, Lobby pickup"
            />
          </div>

          <div className="form-group">
            <label>
              <input
                type="checkbox"
                checked={formData.negotiable}
                onChange={(e) => setFormData({ ...formData, negotiable: e.target.checked })}
              />
              Price is negotiable
            </label>
          </div>

          {/* Image Upload */}
          <div className="form-group">
            <label>Product Images</label>
            <input
              type="file"
              accept="image/*"
              multiple
              onChange={(e) => handleImageUpload(e.target.files)}
              disabled={loading}
            />
            <p style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.5rem' }}>
              You can upload multiple images
            </p>
          </div>

          {imageUrls.length > 0 && (
            <div style={{ marginBottom: '1.5rem' }}>
              <label>Uploaded Images</label>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))', gap: '0.75rem', marginTop: '0.5rem' }}>
                {imageUrls.map((url, index) => (
                  <div key={index} style={{ position: 'relative' }}>
                    <img 
                      src={url} 
                      alt={`Upload ${index + 1}`}
                      style={{ width: '100%', aspectRatio: '1', objectFit: 'cover', borderRadius: 'var(--radius-sm)' }}
                    />
                    <button
                      type="button"
                      className="btn btn-sm btn-danger"
                      onClick={() => removeImage(index)}
                      style={{ position: 'absolute', top: '0.25rem', right: '0.25rem', padding: '0.25rem 0.5rem' }}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button type="submit" className="btn btn-primary btn-lg" disabled={loading}>
              {loading ? 'Creating...' : 'List Product'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={onBack}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateProductPage;

