import React, { useState } from 'react';
import { fileAPI } from '../services/api';

const FileUploadSection = () => {
  const [activeTab, setActiveTab] = useState('product');
  const [response, setResponse] = useState(null);

  const handleResponse = (promise) => {
    setResponse({ loading: true });
    promise
      .then(res => setResponse({ success: true, data: res.data }))
      .catch(err => setResponse({ error: true, data: err.response?.data || { message: err.message } }));
  };

  return (
    <div className="section">
      <h2>File Upload</h2>
      
      <div className="nav-tabs">
        <button className={`nav-tab ${activeTab === 'product' ? 'active' : ''}`} onClick={() => setActiveTab('product')}>Product Images</button>
        <button className={`nav-tab ${activeTab === 'category' ? 'active' : ''}`} onClick={() => setActiveTab('category')}>Category Icon</button>
      </div>

      {activeTab === 'product' && <ProductImageUpload onResponse={handleResponse} />}
      {activeTab === 'category' && <CategoryIconUpload onResponse={handleResponse} />}

      {response && (
        <div className={`response ${response.error ? 'error' : response.success ? 'success' : ''}`}>
          <pre>{JSON.stringify(response.data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

const ProductImageUpload = ({ onResponse }) => {
  const [files, setFiles] = useState(null);

  const handleFileChange = (e) => {
    setFiles(e.target.files);
  };

  const handleUpload = () => {
    if (files && files.length > 0) {
      onResponse(fileAPI.uploadProductImages(files));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Select Images (multiple allowed, max 5MB each, 25MB total)</label>
        <input type="file" multiple accept="image/*" onChange={handleFileChange} className="file-input" />
      </div>
      {files && (
        <div>
          <p>Selected files: {files.length}</p>
          <ul>
            {Array.from(files).map((file, index) => (
              <li key={index}>{file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)</li>
            ))}
          </ul>
        </div>
      )}
      <button className="btn" onClick={handleUpload} disabled={!files || files.length === 0}>Upload Images</button>
    </div>
  );
};

const CategoryIconUpload = ({ onResponse }) => {
  const [file, setFile] = useState(null);

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleUpload = () => {
    if (file) {
      onResponse(fileAPI.uploadCategoryIcon(file));
    }
  };

  return (
    <div>
      <div className="form-group">
        <label>Select Icon Image (max 5MB)</label>
        <input type="file" accept="image/*" onChange={handleFileChange} className="file-input" />
      </div>
      {file && (
        <div>
          <p>Selected file: {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)</p>
        </div>
      )}
      <button className="btn" onClick={handleUpload} disabled={!file}>Upload Icon</button>
    </div>
  );
};

export default FileUploadSection;

