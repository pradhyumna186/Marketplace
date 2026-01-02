import React, { useState, useEffect } from 'react';
import { productAPI, categoryAPI } from '../services/api';

const ProductsPage = ({ onViewProduct, isAuthenticated }) => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [filters, setFilters] = useState({
    minPrice: '',
    maxPrice: ''
  });
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    loadCategories();
    loadProducts();
    
    // Listen for search events from HomePage
    const handleSearch = (e) => {
      setSearchQuery(e.detail);
      setPage(0);
      searchProducts(e.detail);
    };
    
    const handleCategoryFilter = (e) => {
      setSelectedCategory(e.detail);
      setPage(0);
      loadProductsByCategory(e.detail);
    };
    
    window.addEventListener('search', handleSearch);
    window.addEventListener('filterByCategory', handleCategoryFilter);
    
    return () => {
      window.removeEventListener('search', handleSearch);
      window.removeEventListener('filterByCategory', handleCategoryFilter);
    };
  }, []);

  const loadCategories = async () => {
    try {
      const res = await categoryAPI.getAll();
      setCategories(res.data?.data || []);
    } catch (error) {
      console.error('Error loading categories:', error);
    }
  };

  const loadProducts = async () => {
    setLoading(true);
    try {
      const res = await productAPI.getAll({ page, size: 20 });
      const data = res.data?.data || {};
      setProducts(data.content || []);
      setHasMore(!data.last);
    } catch (error) {
      console.error('Error loading products:', error);
    } finally {
      setLoading(false);
    }
  };

  const searchProducts = async (keyword) => {
    setLoading(true);
    try {
      const res = await productAPI.search(keyword, { page: 0, size: 20 });
      const data = res.data?.data || {};
      setProducts(data.content || []);
      setHasMore(!data.last);
    } catch (error) {
      console.error('Error searching products:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadProductsByCategory = async (categoryId) => {
    setLoading(true);
    try {
      const res = await productAPI.getByCategory(categoryId, { page: 0, size: 20 });
      const data = res.data?.data || {};
      setProducts(data.content || []);
      setHasMore(!data.last);
    } catch (error) {
      console.error('Error loading products by category:', error);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = async () => {
    if (!selectedCategory || !filters.minPrice || !filters.maxPrice) {
      alert('Please select a category and set both min and max price');
      return;
    }
    
    setLoading(true);
    try {
      const res = await productAPI.filter({
        categoryId: selectedCategory,
        minPrice: filters.minPrice,
        maxPrice: filters.maxPrice,
        page: 0,
        size: 20
      });
      const data = res.data?.data || {};
      setProducts(data.content || []);
      setHasMore(!data.last);
    } catch (error) {
      console.error('Error filtering products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      searchProducts(searchQuery);
    } else {
      loadProducts();
    }
  };

  if (loading && products.length === 0) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div>
      {/* Search and Filters */}
      <div className="filters">
        <form onSubmit={handleSearch} className="search-bar">
          <input
            type="text"
            className="search-input"
            placeholder="Search products..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Search</button>
        </form>

        <div style={{ marginTop: '1.5rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          <div className="form-group">
            <label>Category</label>
            <select
              value={selectedCategory || ''}
              onChange={(e) => setSelectedCategory(e.target.value ? parseInt(e.target.value) : null)}
            >
              <option value="">All Categories</option>
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Min Price</label>
            <input
              type="number"
              step="0.01"
              placeholder="0.00"
              value={filters.minPrice}
              onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label>Max Price</label>
            <input
              type="number"
              step="0.01"
              placeholder="1000.00"
              value={filters.maxPrice}
              onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
            />
          </div>
          <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button type="button" className="btn btn-primary" onClick={applyFilters} style={{ width: '100%' }}>
              Apply Filters
            </button>
          </div>
        </div>

        {(searchQuery || selectedCategory) && (
          <div style={{ marginTop: '1rem' }}>
            <button 
              className="btn btn-sm btn-secondary" 
              onClick={() => {
                setSearchQuery('');
                setSelectedCategory(null);
                setFilters({ minPrice: '', maxPrice: '' });
                loadProducts();
              }}
            >
              Clear Filters
            </button>
          </div>
        )}
      </div>

      {/* Products Grid */}
      {products.length > 0 ? (
        <>
          <div className="products-grid">
            {products.map(product => (
              <ProductCard key={product.id} product={product} onView={onViewProduct} />
            ))}
          </div>
          
          {hasMore && (
            <div style={{ textAlign: 'center', marginTop: '2rem' }}>
              <button 
                className="btn btn-outline" 
                onClick={() => {
                  setPage(page + 1);
                  // Load more logic would go here
                }}
              >
                Load More
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="empty-state">
          <div className="empty-state-icon">🔍</div>
          <h3>No products found</h3>
          <p>Try adjusting your search or filters</p>
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
        {product.originalPrice && product.originalPrice > product.price && (
          <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', textDecoration: 'line-through' }}>
            ${product.originalPrice.toFixed(2)}
          </div>
        )}
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

export default ProductsPage;

