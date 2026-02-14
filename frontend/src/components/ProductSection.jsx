import React, { useState } from 'react';
import { productAPI, categoryAPI } from '../services/api';

const ProductSection = () => {
  const [activeTab, setActiveTab] = useState('list');
  const [response, setResponse] = useState(null);

  const handleResponse = (promise) => {
    setResponse({ loading: true });
    promise
      .then(res => setResponse({ success: true, data: res.data }))
      .catch(err => setResponse({ error: true, data: err.response?.data || { message: err.message } }));
  };

  return (
    <div className="section">
      <h2>Products</h2>
      
      <div className="nav-tabs">
        <button className={`nav-tab ${activeTab === 'create' ? 'active' : ''}`} onClick={() => setActiveTab('create')}>Create</button>
        <button className={`nav-tab ${activeTab === 'list' ? 'active' : ''}`} onClick={() => setActiveTab('list')}>List</button>
        <button className={`nav-tab ${activeTab === 'get' ? 'active' : ''}`} onClick={() => setActiveTab('get')}>Get by ID</button>
        <button className={`nav-tab ${activeTab === 'update' ? 'active' : ''}`} onClick={() => setActiveTab('update')}>Update</button>
        <button className={`nav-tab ${activeTab === 'delete' ? 'active' : ''}`} onClick={() => setActiveTab('delete')}>Delete</button>
        <button className={`nav-tab ${activeTab === 'search' ? 'active' : ''}`} onClick={() => setActiveTab('search')}>Search</button>
        <button className={`nav-tab ${activeTab === 'filter' ? 'active' : ''}`} onClick={() => setActiveTab('filter')}>Filter</button>
        <button className={`nav-tab ${activeTab === 'by-category' ? 'active' : ''}`} onClick={() => setActiveTab('by-category')}>By Category</button>
        <button className={`nav-tab ${activeTab === 'by-building' ? 'active' : ''}`} onClick={() => setActiveTab('by-building')}>By Building</button>
        <button className={`nav-tab ${activeTab === 'my-products' ? 'active' : ''}`} onClick={() => setActiveTab('my-products')}>My Products</button>
        <button className={`nav-tab ${activeTab === 'mark-sold' ? 'active' : ''}`} onClick={() => setActiveTab('mark-sold')}>Mark as Sold</button>
        <button className={`nav-tab ${activeTab === 'trending' ? 'active' : ''}`} onClick={() => setActiveTab('trending')}>Trending</button>
      </div>

      {activeTab === 'create' && <CreateProduct onResponse={handleResponse} />}
      {activeTab === 'list' && <ProductList onResponse={handleResponse} />}
      {activeTab === 'get' && <GetProduct onResponse={handleResponse} />}
      {activeTab === 'update' && <UpdateProduct onResponse={handleResponse} />}
      {activeTab === 'delete' && <DeleteProduct onResponse={handleResponse} />}
      {activeTab === 'search' && <SearchProducts onResponse={handleResponse} />}
      {activeTab === 'filter' && <FilterProducts onResponse={handleResponse} />}
      {activeTab === 'by-category' && <ProductsByCategory onResponse={handleResponse} />}
      {activeTab === 'by-building' && <ProductsByBuilding onResponse={handleResponse} />}
      {activeTab === 'my-products' && <MyProducts onResponse={handleResponse} />}
      {activeTab === 'mark-sold' && <MarkSold onResponse={handleResponse} />}
      {activeTab === 'trending' && <TrendingProducts onResponse={handleResponse} />}

      {response && (
        <div className={`response ${response.error ? 'error' : response.success ? 'success' : ''}`}>
          <pre>{JSON.stringify(response.data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

const CreateProduct = ({ onResponse }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: '',
    originalPrice: '',
    condition: 'GOOD',
    categoryId: '',
    negotiable: true,
    locationDetails: '',
    imageUrls: ''
  });
  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);

  // Fetch categories when component mounts
  React.useEffect(() => {
    setLoadingCategories(true);
    categoryAPI.getAll()
      .then(res => {
        const categoriesData = res.data.data || res.data;
        setCategories(Array.isArray(categoriesData) ? categoriesData : []);
        setLoadingCategories(false);
      })
      .catch(err => {
        console.error('Error loading categories:', err);
        setLoadingCategories(false);
      });
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    const data = {
      ...formData,
      price: parseFloat(formData.price),
      originalPrice: formData.originalPrice ? parseFloat(formData.originalPrice) : null,
      categoryId: parseInt(formData.categoryId),
      imageUrls: formData.imageUrls ? formData.imageUrls.split(',').map(url => url.trim()) : []
    };
    onResponse(productAPI.create(data));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Title *</label>
        <input type="text" value={formData.title} onChange={(e) => setFormData({...formData, title: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Description *</label>
        <textarea value={formData.description} onChange={(e) => setFormData({...formData, description: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Price *</label>
        <input type="number" step="0.01" value={formData.price} onChange={(e) => setFormData({...formData, price: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Original Price</label>
        <input type="number" step="0.01" value={formData.originalPrice} onChange={(e) => setFormData({...formData, originalPrice: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Condition *</label>
        <select value={formData.condition} onChange={(e) => setFormData({...formData, condition: e.target.value})} required>
          <option value="NEW">NEW</option>
          <option value="LIKE_NEW">LIKE_NEW</option>
          <option value="GOOD">GOOD</option>
          <option value="FAIR">FAIR</option>
          <option value="POOR">POOR</option>
        </select>
      </div>
      <div className="form-group">
        <label>Category *</label>
        {loadingCategories ? (
          <div>Loading categories...</div>
        ) : (
          <select 
            value={formData.categoryId} 
            onChange={(e) => setFormData({...formData, categoryId: e.target.value})} 
            required
          >
            <option value="">Select a category</option>
            {categories.map(category => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        )}
      </div>
      <div className="form-group">
        <label>
          <input type="checkbox" checked={formData.negotiable} onChange={(e) => setFormData({...formData, negotiable: e.target.checked})} />
          Negotiable
        </label>
      </div>
      <div className="form-group">
        <label>Location Details</label>
        <input type="text" value={formData.locationDetails} onChange={(e) => setFormData({...formData, locationDetails: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Image URLs (comma-separated)</label>
        <input type="text" value={formData.imageUrls} onChange={(e) => setFormData({...formData, imageUrls: e.target.value})} />
      </div>
      <button type="submit" className="btn">Create Product</button>
    </form>
  );
};

const ProductList = ({ onResponse }) => {
  const [params, setParams] = useState({ page: '0', size: '20' });

  const loadProducts = () => {
    onResponse(productAPI.getAll(params));
  };

  return (
    <div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={loadProducts}>Get All Products</button>
    </div>
  );
};

const GetProduct = ({ onResponse }) => {
  const [id, setId] = useState('');

  const getProduct = () => {
    if (id) {
      onResponse(productAPI.getById(id));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Product ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <button className="btn" onClick={getProduct}>Get Product</button>
    </div>
  );
};

const UpdateProduct = ({ onResponse }) => {
  const [id, setId] = useState('');
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: '',
    condition: '',
    status: '',
    categoryId: '',
    negotiable: '',
    locationDetails: ''
  });
  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);

  // Fetch categories when component mounts
  React.useEffect(() => {
    setLoadingCategories(true);
    categoryAPI.getAll()
      .then(res => {
        const categoriesData = res.data.data || res.data;
        setCategories(Array.isArray(categoriesData) ? categoriesData : []);
        setLoadingCategories(false);
      })
      .catch(err => {
        console.error('Error loading categories:', err);
        setLoadingCategories(false);
      });
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    const data = {};
    Object.keys(formData).forEach(key => {
      if (formData[key] !== '') {
        if (key === 'price') data[key] = parseFloat(formData[key]);
        else if (key === 'categoryId') data[key] = parseInt(formData[key]);
        else if (key === 'negotiable') data[key] = formData[key] === 'true';
        else data[key] = formData[key];
      }
    });
    onResponse(productAPI.update(id, data));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Product ID *</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} required />
      </div>
      <div className="form-group">
        <label>Title</label>
        <input type="text" value={formData.title} onChange={(e) => setFormData({...formData, title: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Description</label>
        <textarea value={formData.description} onChange={(e) => setFormData({...formData, description: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Price</label>
        <input type="number" step="0.01" value={formData.price} onChange={(e) => setFormData({...formData, price: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Condition</label>
        <select value={formData.condition} onChange={(e) => setFormData({...formData, condition: e.target.value})}>
          <option value="">Select...</option>
          <option value="NEW">NEW</option>
          <option value="LIKE_NEW">LIKE_NEW</option>
          <option value="GOOD">GOOD</option>
          <option value="FAIR">FAIR</option>
          <option value="POOR">POOR</option>
        </select>
      </div>
      <div className="form-group">
        <label>Status</label>
        <select value={formData.status} onChange={(e) => setFormData({...formData, status: e.target.value})}>
          <option value="">Select...</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="SOLD">SOLD</option>
          <option value="PENDING">PENDING</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </div>
      <div className="form-group">
        <label>Category</label>
        {loadingCategories ? (
          <div>Loading categories...</div>
        ) : (
          <select 
            value={formData.categoryId} 
            onChange={(e) => setFormData({...formData, categoryId: e.target.value})}
          >
            <option value="">Select a category (optional)</option>
            {categories.map(category => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        )}
      </div>
      <div className="form-group">
        <label>Negotiable</label>
        <select value={formData.negotiable} onChange={(e) => setFormData({...formData, negotiable: e.target.value})}>
          <option value="">Select...</option>
          <option value="true">True</option>
          <option value="false">False</option>
        </select>
      </div>
      <button type="submit" className="btn">Update Product</button>
    </form>
  );
};

const DeleteProduct = ({ onResponse }) => {
  const [id, setId] = useState('');

  const deleteProduct = () => {
    if (id) {
      onResponse(productAPI.delete(id));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Product ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <button className="btn btn-danger" onClick={deleteProduct}>Delete Product</button>
    </div>
  );
};

const SearchProducts = ({ onResponse }) => {
  const [keyword, setKeyword] = useState('');
  const [params, setParams] = useState({ page: '0', size: '20' });

  const search = () => {
    if (keyword) {
      onResponse(productAPI.search(keyword, params));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Keyword *</label>
        <input type="text" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
      </div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={search}>Search Products</button>
    </div>
  );
};

const FilterProducts = ({ onResponse }) => {
  const [params, setParams] = useState({
    categoryId: '',
    minPrice: '',
    maxPrice: '',
    page: '0',
    size: '20'
  });

  const filter = () => {
    if (params.categoryId && params.minPrice && params.maxPrice) {
      onResponse(productAPI.filter(params));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Category ID *</label>
        <input type="number" value={params.categoryId} onChange={(e) => setParams({...params, categoryId: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Min Price *</label>
        <input type="number" step="0.01" value={params.minPrice} onChange={(e) => setParams({...params, minPrice: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Max Price *</label>
        <input type="number" step="0.01" value={params.maxPrice} onChange={(e) => setParams({...params, maxPrice: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={filter}>Filter Products</button>
    </div>
  );
};

const ProductsByCategory = ({ onResponse }) => {
  const [categoryId, setCategoryId] = useState('');
  const [params, setParams] = useState({ page: '0', size: '20' });

  const loadProducts = () => {
    if (categoryId) {
      onResponse(productAPI.getByCategory(categoryId, params));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Category ID *</label>
        <input type="number" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} />
      </div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={loadProducts}>Get Products by Category</button>
    </div>
  );
};

const ProductsByBuilding = ({ onResponse }) => {
  const [building, setBuilding] = useState('');
  const [params, setParams] = useState({ page: '0', size: '20' });

  const loadProducts = () => {
    if (building) {
      onResponse(productAPI.getByBuilding(building, params));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Building Name *</label>
        <input type="text" value={building} onChange={(e) => setBuilding(e.target.value)} placeholder="e.g., Building AA" />
      </div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={loadProducts}>Get Products by Building</button>
    </div>
  );
};

const MyProducts = ({ onResponse }) => {
  const [status, setStatus] = useState('ACTIVE');
  const [params, setParams] = useState({ page: '0', size: '20' });

  const loadMyProducts = () => {
    onResponse(productAPI.getMyProducts({ ...params, status }));
  };

  return (
    <div>
      <div className="form-group">
        <label>Status</label>
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="ACTIVE">ACTIVE</option>
          <option value="SOLD">SOLD</option>
          <option value="PENDING">PENDING</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </div>
      <div className="form-group">
        <label>Page</label>
        <input type="number" value={params.page} onChange={(e) => setParams({...params, page: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Size</label>
        <input type="number" value={params.size} onChange={(e) => setParams({...params, size: e.target.value})} />
      </div>
      <button className="btn" onClick={loadMyProducts}>Get My Products</button>
    </div>
  );
};

const MarkSold = ({ onResponse }) => {
  const [id, setId] = useState('');
  const [buyerId, setBuyerId] = useState('');
  const [soldPrice, setSoldPrice] = useState('');

  const markSold = () => {
    if (id && buyerId && soldPrice) {
      onResponse(productAPI.markSold(id, buyerId, soldPrice));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Product ID *</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} required />
      </div>
      <div className="form-group">
        <label>Buyer ID *</label>
        <input type="number" value={buyerId} onChange={(e) => setBuyerId(e.target.value)} required />
      </div>
      <div className="form-group">
        <label>Sold Price *</label>
        <input type="number" step="0.01" value={soldPrice} onChange={(e) => setSoldPrice(e.target.value)} required />
      </div>
      <button className="btn btn-success" onClick={markSold}>Mark as Sold</button>
    </div>
  );
};

const TrendingProducts = ({ onResponse }) => {
  const [limit, setLimit] = useState('10');

  const loadTrending = () => {
    onResponse(productAPI.getTrending(limit));
  };

  return (
    <div>
      <div className="form-group">
        <label>Limit</label>
        <input type="number" value={limit} onChange={(e) => setLimit(e.target.value)} />
      </div>
      <button className="btn" onClick={loadTrending}>Get Trending Products</button>
      <button className="btn" onClick={() => onResponse(productAPI.getRecent(limit))}>Get Recent Products</button>
    </div>
  );
};

export default ProductSection;

