'use strict';

const config = require('../../config');

// Make a thumbnail URL absolute relative to the Invidious instance origin.
function absThumb(url, instanceOrigin) {
  if (!url) return null;
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('//')) return 'https:' + url;
  if (url.startsWith('/')) return instanceOrigin + url;
  return url;
}

// Pick the best thumbnail quality from an Invidious videoThumbnails array.
function bestThumb(thumbnails, instanceOrigin) {
  if (!Array.isArray(thumbnails) || !thumbnails.length) return null;
  const preferred = ['maxres', 'high', 'sddefault', 'medium', 'default'];
  for (const q of preferred) {
    const t = thumbnails.find(x => x.quality === q);
    if (t && t.url) return absThumb(t.url, instanceOrigin);
  }
  const first = thumbnails.find(t => t.url);
  return first ? absThumb(first.url, instanceOrigin) : null;
}

/**
 * GET /api/invidious/playlist?id=PLAYLIST_ID
 *
 * Server-side proxy for the Invidious playlist API.
 * Tries each configured instance in order and returns the first success.
 * Paginates through all pages to return the full video list.
 * Normalises thumbnail URLs to absolute so the frontend never gets broken /vi/… paths.
 */
exports.playlist = async (req, res, next) => {
  const plId = String(req.query.id || '').trim();
  if (!plId) return res.status(400).json({ error: 'Missing playlist id' });

  const instances = config.youtube.invidiousInstances;
  let lastErr = null;

  for (const inst of instances) {
    try {
      // Fetch first page to get title + initial videos
      const firstUrl = `${inst}/api/v1/playlists/${encodeURIComponent(plId)}?page=1`;
      const firstRes = await fetch(firstUrl, { signal: AbortSignal.timeout(15000) });
      if (!firstRes.ok) {
        lastErr = `${inst} → HTTP ${firstRes.status}`;
        continue;
      }
      const firstData = await firstRes.json();

      const instanceOrigin = new URL(inst).origin;
      let videos = (firstData.videos || []).map(v => ({
        ...v,
        videoThumbnails: (v.videoThumbnails || []).map(t => ({
          ...t,
          url: absThumb(t.url, instanceOrigin),
        })),
        _thumb: bestThumb(v.videoThumbnails, instanceOrigin),
      }));

      // Paginate: Invidious returns up to 100 per page; keep fetching while we get a full page
      let page = 2;
      while (videos.length > 0 && videos.length % 100 === 0 && page <= 10) {
        try {
          const pageUrl = `${inst}/api/v1/playlists/${encodeURIComponent(plId)}?page=${page}`;
          const pageRes = await fetch(pageUrl, { signal: AbortSignal.timeout(12000) });
          if (!pageRes.ok) break;
          const pageData = await pageRes.json();
          const pageVideos = (pageData.videos || []).map(v => ({
            ...v,
            videoThumbnails: (v.videoThumbnails || []).map(t => ({
              ...t,
              url: absThumb(t.url, instanceOrigin),
            })),
            _thumb: bestThumb(v.videoThumbnails, instanceOrigin),
          }));
          if (!pageVideos.length) break;
          videos = videos.concat(pageVideos);
          page++;
        } catch (pageErr) {
          console.warn(`[invidious/playlist] page ${page} failed:`, pageErr.message);
          break;
        }
      }

      return res.json({
        title: firstData.title,
        playlistThumbnail: firstData.playlistThumbnail
          ? absThumb(firstData.playlistThumbnail, instanceOrigin)
          : null,
        videoCount: firstData.videoCount || videos.length,
        videos,
      });
    } catch (e) {
      lastErr = `${inst} → ${e.message}`;
    }
  }

  console.error('[invidious/playlist] all instances failed:', lastErr);
  res.status(502).json({ error: 'All Invidious instances failed', detail: lastErr });
};
