const BASE = '/api';

/**
 * Streams the AI reply from the backend and invokes onChunk(text) for every
 * incremental delta received. Uses fetch + a manual ReadableStream reader
 * because native EventSource can't send a POST body.
 */

export async function streamReply(originalEmail, instructions, onChunk, onDone, onError) {
  try {
    const res = await fetch(`${BASE}/reply/stream`,  {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ originalEmail, instructions })
    });

    if (!res.ok || !res.body){
      throw new Error(`Stream request failed: ${res.status}`);
    }

    const reader = res.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true){
      const{value, done } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });

      // SSE frames are separated by a blank line; each frame's payload line
      // starts with "data:". Spring emits one "data:" line per Flux element.
      const frames = buffer.split('\n\n');
      buffer = frames.pop(); // last (possibly incomplete) frame stays in buffer

      for (const frame of frames) {
        const line = frame.trim();
        if (line.startsWith('data:')) {
          const text = line.slice(5).trimStart();
          if (text.length) onChunk(text);
        }
      }
    }
    onDone?.();
  } catch (err) {
    onError?.(err);
  }
}

export async function generateMultiTone(originalEmail, instructions) {
  const res = await fetch(`${BASE}/reply/multi-tone`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json' },
    body: JSON.stringify({ originalEmail, instructions })
  });

  if (!res.ok) throw new Error(`Multi-tone request failed: ${res.status}`);
  return res.json();
}

export async function analyzePriority(originalEmail) {
  const res = await fetch(`${BASE}/analyze/priority`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ originalEmail })
  });

  if (!res.ok) throw new Error(`Priority analysis failed: ${res.status}`);
  return res.json();
}

export async function checkHealth() {
  const res = await fetch(`${BASE}/health`);
  if (!res.ok) throw new Error('Health check failed');
  return res.json();
}
