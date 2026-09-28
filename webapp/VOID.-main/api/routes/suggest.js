'use strict';

const express = require('express');
const router  = express.Router();

// Simple in-memory cache so the same query doesn't re-hit Google
const cache = new Map();
const TTL   = 10 * 60 * 1000; // 10 minutes

// GET /api/suggest?q=cool+vibe+songs
// Proxies Google's autocomplete and returns the top suggestions as JSON.
// { suggestions: ["cool vibes R&B playlist", "cool vibe chill songs", ...] }
router.get('/', async (req, res) => {
  const q = (req.query.q || '').trim();
  if (!q) return res.json({ suggestions: [] });

  // Append "songs" hint if the query doesn't already sound music-specific
  const musicHint = /song|music|playlist|track|album|artist|beats|remix/i.test(q) ? q : `${q} songs`;

  const cacheKey = musicHint.toLowerCase();
  const cached   = cache.get(cacheKey);
  if (cached && Date.now() - cached.ts < TTL) {
    return res.json({ suggestions: cached.data });
  }

  try {
    const url = `https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=${encodeURIComponent(musicHint)}`;
    const response = await fetch(url, {
      signal: AbortSignal.timeout(4000),
      headers: { 'User-Agent': 'Mozilla/5.0' },
    });
    if (!response.ok) throw new Error(`Google suggest ${response.status}`);

    // Response is [query, [suggestions]]
    const data        = await response.json();
    const suggestions = (Array.isArray(data[1]) ? data[1] : []).slice(0, 6);

    cache.set(cacheKey, { data: suggestions, ts: Date.now() });
    res.json({ suggestions });
  } catch (err) {
    // Fail silently — frontend falls back to raw query
    res.json({ suggestions: [] });
  }
});

module.exports = router;
