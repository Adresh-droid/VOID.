'use strict';

const express = require('express');
const router  = express.Router();
const config  = require('../../config');

function base() {
  const url = config.handler.url;
  if (!url) throw Object.assign(new Error('VOID_HANDLER_URL not configured'), { status: 503 });
  return url;
}

// POST /api/handler/playlist/resolve
// Body: { url } — YouTube or Spotify playlist URL
router.post('/playlist/resolve', async (req, res, next) => {
  try {
    const upstream = await fetch(`${base()}/playlist/resolve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ url: req.body.url }),
      signal: AbortSignal.timeout(30000),
    });
    const data = await upstream.json().catch(() => ({ error: 'Non-JSON response from handler' }));
    if (!upstream.ok) {
      console.error('[handler/playlist/resolve] upstream error:', upstream.status, data);
    }
    res.status(upstream.status).json(data);
  } catch (e) {
    console.error('[handler/playlist/resolve]', e.message);
    // Return a structured error so the frontend can fall back gracefully
    res.status(502).json({ error: 'Handler unavailable', detail: e.message });
  }
});

// POST /api/handler/playlist/resolve-track
// Body: { searchQuery } — lazy videoId resolution for Spotify tracks
router.post('/playlist/resolve-track', async (req, res, next) => {
  try {
    const upstream = await fetch(`${base()}/playlist/resolve-track`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ searchQuery: req.body.searchQuery }),
      signal: AbortSignal.timeout(15000),
    });
    const data = await upstream.json();
    res.status(upstream.status).json(data);
  } catch (e) { next(e); }
});

// GET /api/handler/stream/:videoId
// Redirects browser directly to the handler — avoids double-proxying audio
router.get('/stream/:videoId', (req, res) => {
  try {
    res.redirect(`${base()}/stream/${req.params.videoId}`);
  } catch (e) {
    res.status(503).json({ error: e.message });
  }
});

// GET /api/handler/stream/info/:videoId
router.get('/stream/info/:videoId', async (req, res, next) => {
  try {
    const upstream = await fetch(
      `${base()}/stream/info/${req.params.videoId}`,
      { signal: AbortSignal.timeout(15000) }
    );
    const data = await upstream.json();
    res.status(upstream.status).json(data);
  } catch (e) { next(e); }
});

module.exports = router;
