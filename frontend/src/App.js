import React, { useState, useEffect } from 'react';
import './App.css';
import UploadForm from './components/UploadForm';
import MinutesDisplay from './components/MinutesDisplay';
import HistoryList from './components/HistoryList';

function App() {
  const [minutes, setMinutes] = useState(null);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('upload');
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'dark' ? 'light' : 'dark');
  };

  return (
    <div className="App">
      <button className="theme-toggle" onClick={toggleTheme} aria-label="Toggle theme">
        {theme === 'dark' ? '☀' : '☾'}
      </button>

      <header className="app-header">
        <h1>Meeting <span>Minutes</span></h1>
        <p>Transform conversations into clear, actionable notes</p>
      </header>

      <div className="tabs">
        <button 
          className={activeTab === 'upload' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('upload')}
        >
          Generate
        </button>
        <button 
          className={activeTab === 'history' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('history')}
        >
          History
        </button>
      </div>

      <div className="container">
        {activeTab === 'upload' ? (
          <>
            <UploadForm 
              setMinutes={setMinutes} 
              loading={loading} 
              setLoading={setLoading} 
            />
            {minutes && <MinutesDisplay minutes={minutes} />}
          </>
        ) : (
          <HistoryList setMinutes={setMinutes} setActiveTab={setActiveTab} />
        )}
      </div>
    </div>
  );
}

export default App;
