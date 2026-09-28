/**
 * config/index.js
 * Central configuration — reads from environment variables.
 *
 * ALL service URLs live here. To migrate to a new Render account,
 * update only the environment variables — no code changes needed.
 */
'use strict';

module.exports = {
  port: parseInt(process.env.PORT, 10) || 3000,
  env:  process.env.NODE_ENV || 'development',
  isDev: (process.env.NODE_ENV || 'development') === 'development',

  // ── YouTube ───────────────────────────────────────────────────────────────
  youtube: {
    apiKey: process.env.VOID_YT_API_KEY || '',
    searchEndpoint:   'https://www.googleapis.com/youtube/v3/search',
    videosEndpoint:   'https://www.googleapis.com/youtube/v3/videos',
    playlistEndpoint: 'https://www.googleapis.com/youtube/v3/playlistItems',
    pipedInstances: (process.env.VOID_PIPED_INSTANCES || '')
      .split(',').map(s => s.trim()).filter(Boolean).length
        ? (process.env.VOID_PIPED_INSTANCES || '').split(',').map(s => s.trim()).filter(Boolean)
        : [
            'https://pipedapi.darkness.services',
            'https://pipedapi.reallyaweso.me',
            'https://pipedapi.aeong.one',
            'https://pipedapi.syncpundit.io',
            'https://api.piped.yt',
            'https://pipedapi.tokhmi.xyz',
            'https://pipedapi.moomoo.me',
            'https://piped-api.cfe.re',
          ],
    invidiousInstances: (process.env.VOID_INVIDIOUS_INSTANCES || '')
      .split(',').map(s => s.trim()).filter(Boolean).length
        ? (process.env.VOID_INVIDIOUS_INSTANCES || '').split(',').map(s => s.trim()).filter(Boolean)
        : [
            'https://invidious.materialio.us',
            'https://invidious.privacyredirect.com',
            'https://invidious.dhusch.de',
            'https://invidious.perennialte.ch',
            'https://yt.drgnz.club',
            'https://invidious.asir.dev',
            'https://iv.nboeck.de',
          ],
  },

  // ── JioSaavn sidecar ─────────────────────────────────────────────────────
  // Self-hosted JioSaavn API proxy. Set VOID_SAAVN_URL to your new deployment.
  saavn: {
    url: process.env.VOID_SAAVN_URL || '',
  },

  // ── SoundCloud sidecar ────────────────────────────────────────────────────
  // Self-hosted SoundCloud service. Set VOID_SOUNDCLOUD_URL to your new deployment.
  soundcloud: {
    url: process.env.VOID_SOUNDCLOUD_URL || '',
  },

  // ── void-playlist-service (yt-dlp + Spotify resolver) ───────────────────
  handler: {
    url: process.env.VOID_HANDLER_URL || '',
  },

  // ── Google OAuth + Drive ──────────────────────────────────────────────────
  google: {
    clientId:     process.env.GOOGLE_CLIENT_ID     || '',
    clientSecret: process.env.GOOGLE_CLIENT_SECRET || '',
    // In prod this should be your Render URL; in dev it's localhost
    callbackUrl:  process.env.GOOGLE_CALLBACK_URL  || 'http://localhost:3000/api/auth/google/callback',
    // Drive folder name where VOID stores user files
    driveFolderName: 'VOID Player',
  },

  // ── Sessions (web) ────────────────────────────────────────────────────────
  session: {
    secret:  process.env.SESSION_SECRET || 'void-dev-secret-change-in-prod',
    maxAge:  30 * 24 * 60 * 60 * 1000, // 30 days
  },

  // ── JWT (Flutter mobile / API clients) ───────────────────────────────────
  // Issue a JWT after Google OAuth so Flutter doesn't need cookie sessions.
  jwt: {
    secret:     process.env.JWT_SECRET || 'void-jwt-dev-secret-change-in-prod',
    expiresIn:  process.env.JWT_EXPIRES_IN || '30d',
  },

  // ── CORS ──────────────────────────────────────────────────────────────────
  // ALLOWED_ORIGINS = comma-separated list of allowed origins.
  // Include your Render URL, localhost for dev, and your Flutter app's origin if needed.
  cors: {
    allowedOrigins: process.env.ALLOWED_ORIGINS
      ? process.env.ALLOWED_ORIGINS.split(',').map(s => s.trim())
      : ['*'],
  },

  // ── Rate limiting ─────────────────────────────────────────────────────────
  rateLimit: {
    windowMs: 15 * 60 * 1000,
    max: 200,
  },
};
