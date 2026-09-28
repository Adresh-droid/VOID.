/**
 * api/controllers/youtube.js
 *
 * JioSaavn API proxy + YouTube stream resolver.
 *
 * JioSaavn service URL is read from config (VOID_SAAVN_URL env var) —
 * never hardcoded. Update the env var when migrating Render accounts.
 *
 * NOTE: This controller is still named "youtube" for backward-compat with
 * existing frontend routes (/api/youtube/*), but it primarily proxies JioSaavn.
 * YouTube video IDs are forwarded to the void-playlist-service handler.
 */

'use strict';

const config = require('../../config');

// ── Service URL guard ─────────────────────────────────────────────────────────
function getSaavnBase() {
  const url = config.saavn.url;
  if (!url) {
    throw Object.assign(
      new Error('VOID_SAAVN_URL is not configured. Set it in your environment variables.'),
      { status: 503 }
    );
  }
  return url.replace(/\/$/, ''); // strip trailing slash
}

// ── Tiny in-memory cache ──────────────────────────────────────────────────────
const cache = new Map();
const CACHE_TTL = 5 * 60 * 1000;

function cacheGet(key) {
  const entry = cache.get(key);
  if (!entry) return null;
  if (Date.now() - entry.ts > CACHE_TTL) { cache.delete(key); return null; }
  return entry.data;
}
function cacheSet(key, data) {
  cache.set(key, { data, ts: Date.now() });
}

async function saavnFetch(path) {
  const res = await fetch(`${getSaavnBase()}${path}`, {
    signal: AbortSignal.timeout(12000),
    headers: { 'User-Agent': 'Mozilla/5.0' },
  });
  if (!res.ok) throw new Error(`JioSaavn API error: ${res.status}`);
  return res.json();
}

// YouTube videoId pattern: 11 chars, alphanumeric + _ + -
function isYouTubeVideoId(id) {
  return /^[A-Za-z0-9_-]{11}$/.test(id);
}

// ── Parsers ───────────────────────────────────────────────────────────────────
function parseSong(song) {
  const imgArr = Array.isArray(song.image) ? song.image : [];
  const img = imgArr.find(i => i.quality === '500x500')?.url
    || imgArr[imgArr.length - 1]?.url
    || '';

  const dlUrls = Array.isArray(song.downloadUrl) ? song.downloadUrl : [];
  let audioUrl = '';
  for (const q of ['320kbps', '160kbps', '96kbps', '48kbps', '12kbps']) {
    const entry = dlUrls.find(d => d.quality === q);
    if (entry && entry.url) { audioUrl = entry.url; break; }
  }
  if (!audioUrl && dlUrls.length) audioUrl = dlUrls[dlUrls.length - 1]?.url || '';

  let artist = '';
  if (song.artists?.primary?.length) {
    artist = song.artists.primary.map(a => a.name).join(', ');
  } else if (typeof song.primaryArtists === 'string') {
    artist = song.primaryArtists;
  }

  return {
    id: song.id,
    videoId: song.id,
    title: song.name || 'Unknown',
    artist,
    album: song.album?.name || '',
    duration: parseInt(song.duration || 0),
    thumbnail: img,
    audioUrl,
    source: 'jiosaavn',
    type: 'song',
  };
}

function parseArtist(a) {
  const imgArr = Array.isArray(a.image) ? a.image : [];
  const img = imgArr.find(i => i.quality === '500x500')?.url
    || imgArr[imgArr.length - 1]?.url || '';
  return {
    id: a.id,
    name: a.name || 'Unknown Artist',
    thumbnail: img,
    followerCount: a.followerCount || 0,
    type: 'artist',
    source: 'jiosaavn',
  };
}

function parseAlbum(al) {
  const imgArr = Array.isArray(al.image) ? al.image : [];
  const img = imgArr.find(i => i.quality === '500x500')?.url
    || imgArr[imgArr.length - 1]?.url || '';
  let artist = '';
  if (Array.isArray(al.artists?.primary)) artist = al.artists.primary.map(a => a.name).join(', ');
  else if (typeof al.primaryArtists === 'string') artist = al.primaryArtists;
  return {
    id: al.id,
    name: al.name || al.title || 'Unknown Album',
    artist,
    thumbnail: img,
    year: al.year || '',
    songCount: al.songCount || 0,
    type: 'album',
    source: 'jiosaavn',
  };
}

function parsePodcast(show) {
  const imgArr = Array.isArray(show.image) ? show.image : [];
  const img = imgArr.find(i => i.quality === '500x500')?.url
    || imgArr[imgArr.length - 1]?.url || '';
  return {
    id: show.id,
    name: show.name || show.title || 'Unknown Show',
    artist: show.header_desc || show.subTitle || '',
    thumbnail: img,
    type: 'podcast',
    source: 'jiosaavn',
  };
}

function mergeSongs(arrays) {
  const seen = new Set();
  const out = [];
  for (const arr of arrays) {
    for (const song of arr) {
      if (!seen.has(song.id)) { seen.add(song.id); out.push(song); }
    }
  }
  return out;
}

async function fetchPlaylistSongsForQuery(q) {
  try {
    const data = await saavnFetch(`/search/playlists?query=${encodeURIComponent(q)}&page=1&limit=3`);
    const playlists = data.data?.results || data.results || [];
    if (!playlists.length) return [];
    const plId = playlists[0].id;
    if (!plId) return [];
    const plData = await saavnFetch(`/playlists?id=${encodeURIComponent(plId)}`);
    const d = plData.data || plData;
    return (d.songs || []).map(parseSong);
  } catch (e) {
    console.warn('[playlist songs fetch]', e.message);
    return [];
  }
}

async function searchYouTubeInnerTube(query, type = 'all') {
  const url = 'https://music.youtube.com/youtubei/v1/search';
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'X-YouTube-Client-Name': '67',
        'X-YouTube-Client-Version': '1.20240101.01.00'
      },
      body: JSON.stringify({
        context: {
          client: {
            clientName: 'WEB_REMIX',
            clientVersion: '1.20240101.01.00',
            hl: 'en',
            gl: 'US'
          }
        },
        query: query
      }),
      signal: AbortSignal.timeout(8000)
    });

    if (!res.ok) return null;
    const data = await res.json();
    const contents = data?.contents?.tabbedSearchResultsRenderer?.tabs?.[0]?.tabRenderer?.content?.sectionListRenderer?.contents || [];

    const songs = [];
    const artists = [];
    const albums = [];

    for (const section of contents) {
      const shelf = section?.shelfRenderer?.content?.verticalListRenderer?.items || section?.itemSectionRenderer?.contents || [];
      for (const item of shelf) {
        const itemData = item?.musicResponsiveListItemRenderer;
        if (!itemData) continue;

        const videoId = itemData?.playlistItemData?.videoId || itemData?.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId;
        const title = itemData?.flexColumns?.[0]?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.[0]?.text;
        const subtitleRuns = itemData?.flexColumns?.[1]?.musicResponsiveListItemFlexColumnRenderer?.text?.runs || [];
        const artist = subtitleRuns.map(r => r.text).join('');
        const thumbnails = itemData?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails || [];
        const thumbnail = thumbnails[thumbnails.length - 1]?.url || '';

        if (videoId && title) {
          songs.push({
            id: videoId,
            videoId: videoId,
            title: title,
            artist: artist || 'YouTube Music',
            thumbnail: thumbnail,
            type: 'song',
            source: 'youtube'
          });
        }
      }
    }

    if (songs.length > 0) {
      return { items: songs, artists, albums, podcasts: [], source: 'youtube' };
    }
  } catch (e) {
    console.warn('[YouTube InnerTube search error]', e.message);
  }
  return null;
}

// ── Controllers ───────────────────────────────────────────────────────────────

exports.search = async (req, res, next) => {
  try {
    const q = String(req.query.q || '').trim();
    const type = String(req.query.type || 'all').trim();
    if (!q) return res.status(400).json({ error: 'Missing query parameter: q' });

    const cacheKey = `yt_search:${q}:${type}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json({ ...cached, _cached: true });

    // Primary: Native YouTube InnerTube Search
    const ytSearchResults = await searchYouTubeInnerTube(q, type);
    if (ytSearchResults && ytSearchResults.items && ytSearchResults.items.length > 0) {
      cacheSet(cacheKey, ytSearchResults);
      return res.json(ytSearchResults);
    }

    if (type === 'all') {
      const [songsData, artistsData, albumsData, podcastData, playlistSongs] = await Promise.allSettled([
        saavnFetch(`/search/songs?query=${encodeURIComponent(q)}&page=1&limit=15`),
        saavnFetch(`/search/artists?query=${encodeURIComponent(q)}&page=1&limit=5`),
        saavnFetch(`/search/albums?query=${encodeURIComponent(q)}&page=1&limit=5`),
        saavnFetch(`/search/podcasts?query=${encodeURIComponent(q)}&page=1&limit=5`),
        fetchPlaylistSongsForQuery(q),
      ]);

      const directSongs = songsData.status === 'fulfilled'
        ? (songsData.value.data?.results || songsData.value.results || []).map(parseSong) : [];
      const pl_songs = playlistSongs.status === 'fulfilled' ? playlistSongs.value : [];
      const songs = mergeSongs([directSongs, pl_songs]);
      const artists = artistsData.status === 'fulfilled'
        ? (artistsData.value.data?.results || artistsData.value.results || []).map(parseArtist) : [];
      const albums = albumsData.status === 'fulfilled'
        ? (albumsData.value.data?.results || albumsData.value.results || []).map(parseAlbum) : [];
      const podcasts = podcastData.status === 'fulfilled'
        ? (podcastData.value.data?.results || podcastData.value.results || []).map(parsePodcast) : [];

      const result = { items: songs, artists, albums, podcasts, source: 'jiosaavn' };
      cacheSet(cacheKey, result);
      return res.json(result);
    }

    if (type === 'artist') {
      const data = await saavnFetch(`/search/artists?query=${encodeURIComponent(q)}&page=1&limit=20`);
      const results = data.data?.results || data.results || [];
      const result = { items: [], artists: results.map(parseArtist), albums: [], podcasts: [], source: 'jiosaavn' };
      cacheSet(cacheKey, result);
      return res.json(result);
    }

    if (type === 'album') {
      const data = await saavnFetch(`/search/albums?query=${encodeURIComponent(q)}&page=1&limit=20`);
      const results = data.data?.results || data.results || [];
      const result = { items: [], artists: [], albums: results.map(parseAlbum), podcasts: [], source: 'jiosaavn' };
      cacheSet(cacheKey, result);
      return res.json(result);
    }

    if (type === 'podcast') {
      const data = await saavnFetch(`/search/podcasts?query=${encodeURIComponent(q)}&page=1&limit=20`);
      const results = data.data?.results || data.results || [];
      const result = { items: [], artists: [], albums: [], podcasts: results.map(parsePodcast), source: 'jiosaavn' };
      cacheSet(cacheKey, result);
      return res.json(result);
    }

    const [songsData, pl_songs] = await Promise.allSettled([
      saavnFetch(`/search/songs?query=${encodeURIComponent(q)}&page=1&limit=20`),
      fetchPlaylistSongsForQuery(q),
    ]);
    const direct = songsData.status === 'fulfilled'
      ? (songsData.value.data?.results || songsData.value.results || []).map(parseSong) : [];
    const fromPlaylist = pl_songs.status === 'fulfilled' ? pl_songs.value : [];
    const songs = mergeSongs([direct, fromPlaylist]);
    const result = { items: songs, artists: [], albums: [], podcasts: [], source: 'jiosaavn' };
    cacheSet(cacheKey, result);
    res.json(result);
  } catch (err) {
    console.error('[JioSaavn search error]', err.message);
    next(err);
  }
};

exports.artistPage = async (req, res, next) => {
  try {
    const id = String(req.query.id || '').trim();
    if (!id) return res.status(400).json({ error: 'Missing id' });

    const cacheKey = `saavn_artist:${id}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    const [infoData, pop0, pop1, pop2, lat0, lat1] = await Promise.allSettled([
      saavnFetch(`/artists/${id}`),
      saavnFetch(`/artists/${id}/songs?page=0&sortBy=popularity&sortOrder=desc&songCount=50`),
      saavnFetch(`/artists/${id}/songs?page=1&sortBy=popularity&sortOrder=desc&songCount=50`),
      saavnFetch(`/artists/${id}/songs?page=2&sortBy=popularity&sortOrder=desc&songCount=50`),
      saavnFetch(`/artists/${id}/songs?page=0&sortBy=latest&sortOrder=desc&songCount=50`),
      saavnFetch(`/artists/${id}/songs?page=1&sortBy=latest&sortOrder=desc&songCount=50`),
    ]);

    let artistInfo = {};
    if (infoData.status === 'fulfilled') {
      const d = infoData.value.data || infoData.value;
      const imgArr = Array.isArray(d.image) ? d.image : [];
      artistInfo = {
        id: d.id || id,
        name: d.name || '',
        bio: (Array.isArray(d.bio) ? d.bio.map(b => b.text).join(' ') : d.bio) || '',
        thumbnail: imgArr.find(i => i.quality === '500x500')?.url || imgArr[imgArr.length - 1]?.url || '',
        followerCount: d.followerCount || 0,
        dominantType: d.dominantType || '',
      };
    }

    const seenPop = new Set(), seenLat = new Set();
    const popularSongs = [], latestSongs = [];
    for (const page of [pop0, pop1, pop2]) {
      if (page.status !== 'fulfilled') continue;
      const d = page.value.data || page.value;
      for (const s of (d.songs || d.results || []).map(parseSong)) {
        if (!seenPop.has(s.id)) { seenPop.add(s.id); popularSongs.push(s); }
      }
    }
    for (const page of [lat0, lat1]) {
      if (page.status !== 'fulfilled') continue;
      const d = page.value.data || page.value;
      for (const s of (d.songs || d.results || []).map(parseSong)) {
        if (!seenLat.has(s.id)) { seenLat.add(s.id); latestSongs.push(s); }
      }
    }

    const result = { artist: artistInfo, songs: popularSongs, latestSongs, source: 'jiosaavn' };
    cacheSet(cacheKey, result);
    res.json(result);
  } catch (err) {
    console.error('[JioSaavn artist error]', err.message);
    next(err);
  }
};

exports.albumPage = async (req, res, next) => {
  try {
    const id = String(req.query.id || '').trim();
    if (!id) return res.status(400).json({ error: 'Missing id' });

    const cacheKey = `saavn_album:${id}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    const data = await saavnFetch(`/albums?id=${encodeURIComponent(id)}`);
    const d = data.data || data;
    const imgArr = Array.isArray(d.image) ? d.image : [];
    let artist = '';
    if (Array.isArray(d.artists?.primary)) artist = d.artists.primary.map(a => a.name).join(', ');
    else if (typeof d.primaryArtists === 'string') artist = d.primaryArtists;

    const albumInfo = {
      id: d.id || id,
      name: d.name || d.title || '',
      artist,
      thumbnail: imgArr.find(i => i.quality === '500x500')?.url || imgArr[imgArr.length - 1]?.url || '',
      year: d.year || '',
      description: d.description || '',
    };
    const result = { album: albumInfo, songs: (d.songs || []).map(parseSong), source: 'jiosaavn' };
    cacheSet(cacheKey, result);
    res.json(result);
  } catch (err) {
    console.error('[JioSaavn album error]', err.message);
    next(err);
  }
};

exports.podcastPage = async (req, res, next) => {
  try {
    const id = String(req.query.id || '').trim();
    if (!id) return res.status(400).json({ error: 'Missing id' });

    const cacheKey = `saavn_podcast:${id}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    const data = await saavnFetch(`/podcasts/${id}`);
    const d = data.data || data;
    const imgArr = Array.isArray(d.image) ? d.image : [];

    const showInfo = {
      id: d.id || id,
      name: d.name || d.title || '',
      artist: d.header_desc || d.subTitle || d.subtitle || '',
      thumbnail: imgArr.find(i => i.quality === '500x500')?.url || imgArr[imgArr.length - 1]?.url || '',
      description: d.description || d.fan_count || '',
    };

    const episodes = (d.episodes || d.songs || []).map(ep => {
      const epImg = Array.isArray(ep.image) ? ep.image : [];
      const epDlUrls = Array.isArray(ep.downloadUrl) ? ep.downloadUrl : [];
      let audioUrl = '';
      for (const q of ['320kbps', '160kbps', '96kbps', '48kbps', '12kbps']) {
        const entry = epDlUrls.find(d => d.quality === q);
        if (entry && entry.url) { audioUrl = entry.url; break; }
      }
      return {
        id: ep.id,
        videoId: ep.id,
        title: ep.name || ep.title || 'Episode',
        artist: showInfo.name,
        album: '',
        duration: parseInt(ep.duration || 0),
        thumbnail: epImg.find(i => i.quality === '500x500')?.url || epImg[epImg.length - 1]?.url || showInfo.thumbnail,
        audioUrl,
        source: 'jiosaavn',
        type: 'podcast_episode',
      };
    });

    const result = { show: showInfo, episodes, source: 'jiosaavn' };
    cacheSet(cacheKey, result);
    res.json(result);
  } catch (err) {
    console.error('[JioSaavn podcast error]', err.message);
    next(err);
  }
};

exports.videoDetails = async (req, res, next) => {
  try {
    const ids = String(req.query.ids || '').trim();
    if (!ids) return res.status(400).json({ error: 'Missing query parameter: ids' });

    const cacheKey = `saavn_song:${ids}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    const data = await saavnFetch(`/songs/${ids}`);
    const results = data.data || data.results || [];
    const songs = (Array.isArray(results) ? results : [results]).map(parseSong);
    cacheSet(cacheKey, { items: songs });
    res.json({ items: songs });
  } catch (err) {
    console.error('[JioSaavn videoDetails error]', err.message);
    next(err);
  }
};

exports.playlistItems = async (req, res, next) => {
  try {
    const rawId = String(req.query.id || req.query.url || '').trim();
    if (!rawId) return res.status(400).json({ error: 'Missing id or url' });

    let plId = rawId;
    const urlMatch = rawId.match(/\/featured\/[^/]+\/([^/?]+)/);
    if (urlMatch) plId = urlMatch[1];

    const cacheKey = `saavn_pl:${plId}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    const data = await saavnFetch(`/playlists?id=${encodeURIComponent(plId)}`);
    const d = data.data || data;
    const imgArr = Array.isArray(d.image) ? d.image : [];
    const plInfo = {
      id: d.id || plId,
      name: d.name || d.title || 'Playlist',
      description: d.description || '',
      thumbnail: imgArr.find(i => i.quality === '500x500')?.url || imgArr[imgArr.length - 1]?.url || '',
      songCount: d.songCount || 0,
    };
    const result = { playlist: plInfo, songs: (d.songs || []).map(parseSong), source: 'jiosaavn' };
    cacheSet(cacheKey, result);
    res.json(result);
  } catch (err) {
    console.error('[JioSaavn playlist error]', err.message);
    next(err);
  }
};

async function resolveYouTubeInnerTubeStream(videoId) {
  const personas = [
    // 1. ANDROID_VR 1.65.10 — Primary Persona from Android app InnerTube.kt
    {
      url: 'https://music.youtube.com/youtubei/v1/player',
      clientName: 'ANDROID_VR',
      clientVersion: '1.65.10',
      userAgent: 'com.google.android.apps.youtube.vr.oculus/1.65.10 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip',
      headers: {
        'Origin': 'https://music.youtube.com',
        'Referer': 'https://music.youtube.com/',
        'X-YouTube-Client-Name': '93',
        'X-YouTube-Client-Version': '1.65.10'
      },
      clientContext: {
        clientName: 'ANDROID_VR',
        clientVersion: '1.65.10',
        deviceMake: 'Oculus',
        deviceModel: 'Quest 3',
        osName: 'Android',
        osVersion: '12L',
        androidSdkVersion: 32,
        hl: 'en',
        gl: 'US'
      }
    },
    // 2. ANDROID_VR 1.61.48 — Secondary Persona
    {
      url: 'https://music.youtube.com/youtubei/v1/player',
      clientName: 'ANDROID_VR',
      clientVersion: '1.61.48',
      userAgent: 'com.google.android.apps.youtube.vr.oculus/1.61.48 (Linux; U; Android 12; en_US; Quest 3; Build/SQ3A.220605.009.A1; Cronet/132.0.6808.3)',
      headers: {
        'Origin': 'https://music.youtube.com',
        'Referer': 'https://music.youtube.com/',
        'X-YouTube-Client-Name': '93',
        'X-YouTube-Client-Version': '1.61.48'
      },
      clientContext: {
        clientName: 'ANDROID_VR',
        clientVersion: '1.61.48',
        deviceMake: 'Oculus',
        deviceModel: 'Quest 3',
        osName: 'Android',
        osVersion: '12',
        androidSdkVersion: 32,
        hl: 'en',
        gl: 'US'
      }
    }
  ];

  for (const config of personas) {
    try {
      const res = await fetch(config.url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': config.userAgent,
          ...config.headers
        },
        body: JSON.stringify({
          context: {
            client: config.clientContext
          },
          videoId: videoId,
          contentCheckOk: true,
          racyCheckOk: true
        }),
        signal: AbortSignal.timeout(15000)
      });

      if (!res.ok) continue;
      const data = await res.json();
      const formats = data?.streamingData?.adaptiveFormats || [];

      const audioFormat = formats
        .filter(f => f.mimeType && f.mimeType.startsWith('audio/') && f.url)
        .sort((a, b) => (b.bitrate || 0) - (a.bitrate || 0))[0];

      if (audioFormat && audioFormat.url) {
        return {
          url: audioFormat.url,
          mimeType: audioFormat.mimeType,
          bitrate: audioFormat.bitrate,
          durationSeconds: parseInt(data.videoDetails?.lengthSeconds || 0)
        };
      }
    } catch (e) {
      console.warn(`[InnerTube Persona ${config.clientName} failed]`, e.message);
    }
  }
  return null;
}

function parseLrc(lrcText) {
  const lines = lrcText.split('\n');
  const result = [];
  const timeRegex = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/;

  for (const line of lines) {
    const match = timeRegex.exec(line);
    if (match) {
      const min = parseInt(match[1], 10);
      const sec = parseInt(match[2], 10);
      const ms = parseInt(match[3].padEnd(3, '0'), 10);
      const timeMs = min * 60000 + sec * 1000 + ms;
      const text = line.replace(timeRegex, '').trim();
      if (text) {
        result.push({ timeMs, text });
      }
    }
  }
  return result.sort((a, b) => a.timeMs - b.timeMs);
}

exports.lyrics = async (req, res, next) => {
  try {
    const trackName = String(req.query.track || req.query.q || '').trim();
    const artistName = String(req.query.artist || '').trim();
    if (!trackName) return res.status(400).json({ error: 'Missing track' });

    const cacheKey = `lyrics:${trackName}:${artistName}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    try {
      const lrcUrl = `https://lrclib.net/api/get?track_name=${encodeURIComponent(trackName)}&artist_name=${encodeURIComponent(artistName)}`;
      const lrcRes = await fetch(lrcUrl, { signal: AbortSignal.timeout(6000) });
      if (lrcRes.ok) {
        const lrcData = await lrcRes.json();
        if (lrcData.syncedLyrics) {
          const result = {
            provider: 'LrcLib',
            synced: true,
            lines: parseLrc(lrcData.syncedLyrics)
          };
          cacheSet(cacheKey, result);
          return res.json(result);
        }
      }
    } catch (e) {}

    return res.status(404).json({ error: 'Lyrics not found' });
  } catch (err) {
    next(err);
  }
};

exports.streamProxy = async (req, res, next) => {
  const songId = String(req.query.id || '').trim();
  if (!songId) return res.status(400).json({ error: 'Missing id' });

  if (isYouTubeVideoId(songId)) {
    const cacheKey = `yt_stream:${songId}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    // Primary: Native VOID InnerTube Multi-Persona Resolution
    const innerTubeResult = await resolveYouTubeInnerTubeStream(songId);
    if (innerTubeResult) {
      cacheSet(cacheKey, innerTubeResult);
      return res.json(innerTubeResult);
    }

    const handlerUrl = config.handler.url;
    if (!handlerUrl) return res.status(503).json({ error: 'VOID_HANDLER_URL not configured' });

    try {
      const cacheKey = `yt_stream:${songId}`;
      const cached = cacheGet(cacheKey);
      if (cached) return res.json(cached);

      const upstream = await fetch(`${handlerUrl}/stream/info/${songId}`, {
        signal: AbortSignal.timeout(20000),
      });
      if (!upstream.ok) throw new Error(`Handler returned HTTP ${upstream.status}`);
      const data = await upstream.json();

      const url = data.url || data.audioUrl;
      if (!url) return res.status(502).json({ error: 'No stream URL from handler' });

      const result = { url, mimeType: data.mimeType || 'audio/webm' };
      cacheSet(cacheKey, result);
      return res.json(result);
    } catch (e) {
      console.error('[YouTube stream error — handler failed, trying Invidious fallback]', e.message);

      const instances = config.youtube?.invidiousInstances || [];
      for (const instance of instances) {
        try {
          const r = await fetch(`${instance}/api/v1/videos/${songId}`, {
            signal: AbortSignal.timeout(8000),
          });
          if (!r.ok) continue;
          const d = await r.json();
          const stream =
            d.adaptiveFormats?.find(f => f.type?.startsWith('audio/')) ||
            d.formatStreams?.[0];
          if (stream?.url) {
            const result = { url: stream.url, mimeType: stream.type || 'audio/webm' };
            cacheSet(`yt_stream:${songId}`, result);
            return res.json(result);
          }
        } catch (invErr) {
          console.warn(`[YouTube stream] Invidious instance ${instance} failed:`, invErr.message);
        }
      }

      return res.status(502).json({ error: 'Stream resolve failed — all sources exhausted', detail: e.message });
    }
  }

  try {
    const cacheKey = `saavn_url:${songId}`;
    const cached = cacheGet(cacheKey);
    if (cached) return res.json(cached);

    const data = await saavnFetch(`/songs/${songId}`);
    const results = data.data || data.results || [];
    const arr = Array.isArray(results) ? results : [results];
    const song = parseSong(arr[0] || {});

    if (!song.audioUrl) return res.status(502).json({ error: 'No stream URL found' });

    const result = { url: song.audioUrl, mimeType: 'audio/mpeg' };
    cacheSet(cacheKey, result);
    res.json(result);
  } catch (e) {
    console.error('[JioSaavn stream error]', e.message);
    next(e);
  }
};

exports.audioProxy = async (req, res, next) => {
  const songId = String(req.query.id || '').trim();
  if (!songId) return res.status(400).json({ error: 'Missing id' });

  try {
    const data = await saavnFetch(`/songs/${songId}`);
    const results = data.data || data.results || [];
    const arr = Array.isArray(results) ? results : [results];
    const song = parseSong(arr[0] || {});

    if (!song.audioUrl) return res.status(502).send('No audio URL');

    const audioRes = await fetch(song.audioUrl, { signal: AbortSignal.timeout(60000) });
    if (!audioRes.ok) return res.status(502).send('Upstream failed');

    res.setHeader('Content-Type', 'audio/mpeg');
    const contentLength = audioRes.headers.get('Content-Length');
    if (contentLength) res.setHeader('Content-Length', contentLength);
    res.setHeader('Accept-Ranges', 'bytes');

    const buf = await audioRes.arrayBuffer();
    res.send(Buffer.from(buf));
  } catch (e) {
    console.error('[JioSaavn audio error]', e.message);
    next(e);
  }
};
