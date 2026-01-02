import React, { useState } from 'react';
import { categoryAPI } from '../services/api';

const CategorySection = () => {
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
      <h2>Categories</h2>
      
      <div className="nav-tabs">
        <button className={`nav-tab ${activeTab === 'list' ? 'active' : ''}`} onClick={() => setActiveTab('list')}>All Categories</button>
        <button className={`nav-tab ${activeTab === 'get' ? 'active' : ''}`} onClick={() => setActiveTab('get')}>Get by ID</button>
        <button className={`nav-tab ${activeTab === 'request' ? 'active' : ''}`} onClick={() => setActiveTab('request')}>Request Category</button>
        <button className={`nav-tab ${activeTab === 'my-requests' ? 'active' : ''}`} onClick={() => setActiveTab('my-requests')}>My Requests</button>
        <button className={`nav-tab ${activeTab === 'search' ? 'active' : ''}`} onClick={() => setActiveTab('search')}>Search</button>
      </div>

      {activeTab === 'list' && <CategoryList onResponse={handleResponse} />}
      {activeTab === 'get' && <GetCategory onResponse={handleResponse} />}
      {activeTab === 'request' && <RequestCategory onResponse={handleResponse} />}
      {activeTab === 'my-requests' && <MyRequests onResponse={handleResponse} />}
      {activeTab === 'search' && <SearchCategories onResponse={handleResponse} />}

      {response && (
        <div className={`response ${response.error ? 'error' : response.success ? 'success' : ''}`}>
          <pre>{JSON.stringify(response.data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

const CategoryList = ({ onResponse }) => {
  const loadCategories = () => {
    onResponse(categoryAPI.getAll());
  };

  return (
    <div>
      <button className="btn" onClick={loadCategories}>Get All Categories</button>
    </div>
  );
};

const GetCategory = ({ onResponse }) => {
  const [id, setId] = useState('');

  const getCategory = () => {
    if (id) {
      onResponse(categoryAPI.getById(id));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Category ID</label>
        <input type="number" value={id} onChange={(e) => setId(e.target.value)} />
      </div>
      <button className="btn" onClick={getCategory}>Get Category</button>
    </div>
  );
};

const RequestCategory = ({ onResponse }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    justification: '',
    parentCategoryId: ''
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    const data = {
      ...formData,
      parentCategoryId: formData.parentCategoryId ? parseInt(formData.parentCategoryId) : null
    };
    onResponse(categoryAPI.requestCategory(data));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Name *</label>
        <input type="text" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} required />
      </div>
      <div className="form-group">
        <label>Description</label>
        <textarea value={formData.description} onChange={(e) => setFormData({...formData, description: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Justification</label>
        <textarea value={formData.justification} onChange={(e) => setFormData({...formData, justification: e.target.value})} />
      </div>
      <div className="form-group">
        <label>Parent Category ID (optional)</label>
        <input type="number" value={formData.parentCategoryId} onChange={(e) => setFormData({...formData, parentCategoryId: e.target.value})} />
      </div>
      <button type="submit" className="btn">Request Category</button>
    </form>
  );
};

const MyRequests = ({ onResponse }) => {
  const loadRequests = () => {
    onResponse(categoryAPI.getMyRequests());
  };

  return (
    <div>
      <button className="btn" onClick={loadRequests}>Get My Category Requests</button>
    </div>
  );
};

const SearchCategories = ({ onResponse }) => {
  const [keyword, setKeyword] = useState('');

  const search = () => {
    if (keyword) {
      onResponse(categoryAPI.search(keyword));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Keyword</label>
        <input type="text" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
      </div>
      <button className="btn" onClick={search}>Search Categories</button>
    </div>
  );
};

export default CategorySection;

