import { useEffect, useState } from 'react';

const API_BASE = 'http://3.238.107.244:8080';
const STATUSES = ['QUEUED', 'RUNNING', 'RETRYING', 'DONE', 'DEAD_LETTERED'];

function App() {
  const [jobs, setJobs] = useState([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    fetch(`${API_BASE}/jobs`)
      .then((res) => res.json())
      .then(setJobs)
      .catch((err) => console.error('Initial fetch failed:', err));

    const eventSource = new EventSource(`${API_BASE}/jobs/stream`);

    eventSource.onopen = () => setConnected(true);

    eventSource.addEventListener('job-update', (event) => {
      const updatedJob = JSON.parse(event.data);
      setJobs((prevJobs) => {
        const exists = prevJobs.some((j) => j.id === updatedJob.id);
        if (exists) {
          return prevJobs.map((j) => (j.id === updatedJob.id ? updatedJob : j));
        }
        return [updatedJob, ...prevJobs];
      });
    });

    eventSource.onerror = (err) => {
      console.error('SSE connection error', err);
      setConnected(false);
    };

    return () => eventSource.close();
  }, []);

  const submitJob = async () => {
    try {
      await fetch(`${API_BASE}/jobs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          jobType: 'SEND_EMAIL',
          payload: '{}',
          maxAttempts: 3,
        }),
      });
    } catch (err) {
      console.error('Submit failed:', err);
    }
  };

  const jobsByStatus = (status) => jobs.filter((j) => j.status === status);

  return (
    <div className="dashboard">
      <div className="header">
        <div>
          <h1>Job Scheduler Dashboard</h1>
          <span className={`status-dot ${connected ? 'connected' : 'disconnected'}`}>
            {connected ? 'Live' : 'Connecting...'}
          </span>
        </div>
        <button onClick={submitJob}>Submit Job (fails ~50%)</button>
      </div>

      <div className="board">
        {STATUSES.map((status) => (
          <div key={status} className={`column column-${status.toLowerCase()}`}>
            <h2>
              {status.replace('_', ' ')}
              <span className="count">{jobsByStatus(status).length}</span>
            </h2>
            <div className="cards">
              {jobsByStatus(status).map((job) => (
                <div key={job.id} className="card">
                  <div className="card-id">{job.id.slice(0, 8)}</div>
                  <div className="card-type">{job.jobType}</div>
                  <div className="card-meta">
                    attempt {job.failureCount}/{job.maxAttempts}
                  </div>
                  {job.lastError && <div className="card-error">{job.lastError}</div>}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;