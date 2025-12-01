import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './HistoryList.css';

const API_URL = 'http://localhost:8080/api/minutes';

function HistoryList({ setMinutes, setActiveTab }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const response = await axios.get(API_URL);
      setHistory(response.data);
    } catch (err) {
      console.error('Failed to fetch history:', err);
    } finally {
      setLoading(false);
    }
  };

  const viewMinutes = (item) => {
    setMinutes(item);
    setActiveTab('upload');
  };

  if (loading) {
    return <div className="history-loading">Loading history...</div>;
  }

  if (history.length === 0) {
    return (
      <div className="history-empty">
        <p>No meeting minutes yet. Generate your first one!</p>
      </div>
    );
  }

  return (
    <div className="history-list">
      <h2>Meeting History</h2>
      <div className="history-grid">
        {history.map((item) => (
          <div key={item.id} className="history-card" onClick={() => viewMinutes(item)}>
            <h3>{item.title}</h3>
            <p className="history-date">
              {new Date(item.createdAt).toLocaleDateString()} at{' '}
              {new Date(item.createdAt).toLocaleTimeString()}
            </p>
            <p className="history-summary">{item.summary?.substring(0, 100)}...</p>
            <button className="view-btn">View details →</button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default HistoryList;
