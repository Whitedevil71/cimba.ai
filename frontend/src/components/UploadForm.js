import React, { useState } from 'react';
import axios from 'axios';
import './UploadForm.css';

const API_URL = 'http://localhost:8080/api/minutes';

function UploadForm({ setMinutes, loading, setLoading }) {
  const [title, setTitle] = useState('');
  const [transcript, setTranscript] = useState('');
  const [audioFile, setAudioFile] = useState(null);
  const [inputMode, setInputMode] = useState('text');
  const [error, setError] = useState('');

  const handleTextSubmit = async (e) => {
    e.preventDefault();
    if (!title || !transcript) {
      setError('Please provide both title and transcript');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await axios.post(`${API_URL}/transcript`, {
        title,
        transcript
      });
      setMinutes(response.data);
      setTitle('');
      setTranscript('');
    } catch (err) {
      setError('Failed to generate minutes. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAudioSubmit = async (e) => {
    e.preventDefault();
    if (!title || !audioFile) {
      setError('Please provide both title and audio file');
      return;
    }

    setLoading(true);
    setError('');

    const formData = new FormData();
    formData.append('title', title);
    formData.append('file', audioFile);

    try {
      const response = await axios.post(`${API_URL}/audio`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setMinutes(response.data);
      setTitle('');
      setAudioFile(null);
    } catch (err) {
      setError('Failed to process audio. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-form">
      <div className="mode-selector">
        <button 
          className={inputMode === 'text' ? 'mode-btn active' : 'mode-btn'}
          onClick={() => setInputMode('text')}
        >
          <span className="icon">✎</span> Text
        </button>
        <button 
          className={inputMode === 'audio' ? 'mode-btn active' : 'mode-btn'}
          onClick={() => setInputMode('audio')}
        >
          <span className="icon">♪</span> Audio
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}



      {inputMode === 'text' ? (
        <form onSubmit={handleTextSubmit}>
          <div className="form-group">
            <label>Meeting Title</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Q4 Planning Meeting"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label>Meeting Transcript</label>
            <textarea
              value={transcript}
              onChange={(e) => setTranscript(e.target.value)}
              placeholder="Paste your meeting transcript here..."
              rows="10"
              disabled={loading}
            />
          </div>

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? 'Generating...' : 'Generate Minutes'}
          </button>
        </form>
      ) : (
        <form onSubmit={handleAudioSubmit}>
          <div className="form-group">
            <label>Meeting Title</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Q4 Planning Meeting"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label>Audio/Video File (MP3, WAV, MP4, etc.)</label>
            <input
              type="file"
              accept="audio/*,video/*,.mp3,.wav,.m4a,.mp4,.mpeg,.mpga,.webm"
              onChange={(e) => setAudioFile(e.target.files[0])}
              disabled={loading}
            />
            {audioFile && <p className="file-name">Selected: {audioFile.name}</p>}
          </div>

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? 'Processing...' : 'Transcribe & Generate'}
          </button>
        </form>
      )}
    </div>
  );
}

export default UploadForm;
