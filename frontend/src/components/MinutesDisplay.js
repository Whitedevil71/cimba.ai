import React from 'react';
import './MinutesDisplay.css';

function MinutesDisplay({ minutes }) {
  const downloadReport = () => {
    const content = `
MEETING MINUTES
===============

Title: ${minutes.title}
Date: ${new Date(minutes.createdAt).toLocaleString()}

SUMMARY
-------
${minutes.summary}

KEY DECISIONS
-------------
${minutes.keyDecisions}

ACTION ITEMS
------------
${minutes.actionItems}
    `.trim();

    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `meeting-minutes-${minutes.id}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="minutes-display">
      <div className="minutes-header">
        <h2>{minutes.title}</h2>
        <button onClick={downloadReport} className="download-btn">
          ↓ Download
        </button>
      </div>

      <div className="minutes-section">
        <h3>Summary</h3>
        <p>{minutes.summary}</p>
      </div>

      <div className="minutes-section">
        <h3>Key Decisions</h3>
        <div className="content-box">
          {minutes.keyDecisions.split('\n').map((line, idx) => (
            line.trim() && <p key={idx}>{line}</p>
          ))}
        </div>
      </div>

      <div className="minutes-section">
        <h3>Action Items</h3>
        <div className="content-box">
          {minutes.actionItems.split('\n').map((line, idx) => (
            line.trim() && <p key={idx}>{line}</p>
          ))}
        </div>
      </div>
    </div>
  );
}

export default MinutesDisplay;
