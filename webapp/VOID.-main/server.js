/**
 * server.js
 * VOID Player — Express server entry point.
 *
 * Architecture:
 *   • Serves the web PWA from /public
 *   • All /api/* routes are defined in api/index.js
 *   • Auth supports both cookie sessions (web) AND Bearer JWT (Flutter/API)
 *   • All external service URLs are read from environment variables via config/index.js
 */

'use strict';

if (process.env.NODE_ENV !== 'production') {
  require('dotenv').config();
}

const BUILD_TS = Date.now().toString();
const fs   = require('fs');
const path = require('path');

const express     = require('express');
const helmet      = require('helmet');
const compression = require('compression');
const cors        = require('cors');
const rateLimit   = require('express-rate-limit');
const session     = require('cookie-session');
const passport    = require('./api/passport');

const config    = require('./config');
const HANDLER_URL = config.handler.url;
const apiRouter = require('./api');

const app = express();
app.set('trust proxy', 1);

// ── Security headers ────────────────────────────────────────────────────────
app.use(
  helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: [
          "'self'",
          "'unsafe-inline'",
          'https://cdn.jsdelivr.net',
          'https://cdnjs.cloudflare.com',
          'https://accounts.google.com',
        ],
        scriptSrcAttr: ["'unsafe-inline'"],
        styleSrc: [
          "'self'",
          "'unsafe-inline'",
          'https://fonts.googleapis.com',
        ],
        fontSrc: [
          "'self'",
          'https://fonts.gstatic.com',
        ],
        imgSrc: [
          "'self'",
          'data:',
          'blob:',
          'https://*.ytimg.com',
          'https://*.ggpht.com',
          'https://*.saavncdn.com',
          'https://*.jiosaavn.com',
          'https://*.sndcdn.com',
          'https://lh3.googleusercontent.com',
          'https://*.googleusercontent.com',
          'https://i1.sndcdn.com',
          'https://i2.sndcdn.com',
          'https://i3.sndcdn.com',
          'https://i4.sndcdn.com',
          'https://*.c.youtube.com',
          'https://*.googlevideo.com',
          'https://invidious.materialio.us',
          'https://*.invidious.io',
          'https://c.saavncdn.com',
          'https://c.jiosaavn.com',
          'https://inv.nadeko.net',
        ],
        connectSrc: [
          "'self'",
          'https://cdnjs.cloudflare.com',
          'https://*.saavncdn.com',
          'https://aac.saavncdn.com',
          'https://*.sndcdn.com',
          'https://cf-media.sndcdn.com',
          'https://cf-preview-media.sndcdn.com',
          'https://accounts.google.com',
          'https://oauth2.googleapis.com',
          'https://www.googleapis.com',
          ...(HANDLER_URL ? [HANDLER_URL] : []),
        ],
        mediaSrc: [
          "'self'",
          'blob:',
          'https://*.saavncdn.com',
          'https://cf-media.sndcdn.com',
          'https://cf-preview-media.sndcdn.com',
          ...(HANDLER_URL ? [HANDLER_URL] : []),
        ],
        frameSrc: [
          'https://accounts.google.com',
        ],
        workerSrc: ["'self'"],
        manifestSrc: ["'self'"],
      },
    },
  })
);

// ── Compression ─────────────────────────────────────────────────────────────
app.use(compression());

// ── CORS ─────────────────────────────────────────────────────────────────────
app.use(
  cors({
    origin: config.cors.allowedOrigins.includes('*')
      ? '*'
      : config.cors.allowedOrigins,
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization'],
    credentials: true,
  })
);

// ── Body parsing ─────────────────────────────────────────────────────────────
app.use(express.json({ limit: '5mb' }));
app.use(express.urlencoded({ extended: true, limit: '5mb' }));

// ── Sessions (required for Passport / web OAuth) ──────────────────────────────
// cookie-session stores the session entirely in a signed cookie — no server-side
// store needed, so auth persists across Render restarts and redeploys.
// Flutter clients use Bearer JWT instead and never touch this session.
app.use(
  session({
    name:   'void.sess',
    secret: config.session.secret,
    maxAge: config.session.maxAge,
    secure: !config.isDev,
    httpOnly: true,
    sameSite: 'lax',
  })
);

// cookie-session doesn't have req.session.save() — patch it in for passport compat
app.use(function(req, _res, next) {
  if (req.session && !req.session.save) {
    req.session.save = function(cb) { if (cb) cb(); };
  }
  if (req.session && !req.session.regenerate) {
    req.session.regenerate = function(cb) { if (cb) cb(); };
  }
  next();
});

// ── Passport ─────────────────────────────────────────────────────────────────
app.use(passport.initialize());
app.use(passport.session());

// ── Rate limiting (API only) ─────────────────────────────────────────────────
const limiter = rateLimit({
  windowMs: config.rateLimit.windowMs,
  max:      config.rateLimit.max,
  standardHeaders: true,
  legacyHeaders:   false,
  message: { error: 'Too many requests, please try again later.' },
});
app.use('/api', limiter);

// ── API routes ───────────────────────────────────────────────────────────────
app.use('/api', apiRouter);

// ── Version endpoint — lets the client detect a new deploy ───────────────────
app.get('/api/version', (_req, res) => {
  res.set('Cache-Control', 'no-store');
  res.json({ v: BUILD_TS });
});

// ── sw.js — served dynamically so __BUILD_TS__ is replaced each deploy ───────
app.get('/sw.js', (_req, res) => {
  const swPath = path.join(__dirname, 'public', 'sw.js');
  const content = fs.readFileSync(swPath, 'utf8').replace('__BUILD_TS__', BUILD_TS);
  res.set({
    'Content-Type': 'application/javascript',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    'Service-Worker-Allowed': '/',
  });
  res.send(content);
});

// ── Static files (PWA shell) ─────────────────────────────────────────────────
app.use(
  express.static(path.join(__dirname, 'public'), {
    maxAge:  config.isDev ? '0' : '1d',
    etag:    true,
    setHeaders(res, filePath) {
      if (filePath.endsWith('index.html')) {
        res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
        res.setHeader('Pragma', 'no-cache');
        res.setHeader('Expires', '0');
      }
      if (filePath.endsWith('sw.js')) {
        res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
        res.setHeader('Service-Worker-Allowed', '/');
      }
    },
  })
);

// ── SPA fallback ──────────────────────────────────────────────────────────────
app.get('*', (req, res) => {
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// ── Error handler ─────────────────────────────────────────────────────────────
// eslint-disable-next-line no-unused-vars
app.use((err, req, res, _next) => {
  const status = err.status || err.statusCode || 500;
  const message = config.isDev ? err.message : 'Internal server error';
  if (status >= 500) console.error('[VOID] Server error:', err);
  res.status(status).json({ error: message });
});

// ── Start ─────────────────────────────────────────────────────────────────────
app.listen(config.port, () => {
  console.log(`[VOID] Server running on port ${config.port} (${config.env})`);
  if (config.isDev) console.log(`[VOID] http://localhost:${config.port}`);

  // Config validation warnings
  if (!config.google.clientId)    console.warn('[VOID] Warning: GOOGLE_CLIENT_ID not set — Google OAuth/Drive sync disabled');
  if (!config.saavn.url)          console.warn('[VOID] Warning: VOID_SAAVN_URL not set — JioSaavn search will return 503');
  if (!config.soundcloud.url)     console.warn('[VOID] Warning: VOID_SOUNDCLOUD_URL not set — SoundCloud search will return 503');
  if (!config.handler.url)        console.warn('[VOID] Warning: VOID_HANDLER_URL not set — YouTube streaming disabled');
  if (!config.youtube.apiKey)     console.warn('[VOID] Warning: VOID_YT_API_KEY not set — YouTube API proxy will use Invidious fallback');
  if (config.jwt.secret === 'void-jwt-dev-secret-change-in-prod') {
    console.warn('[VOID] Warning: JWT_SECRET is using the insecure default — set it in production!');
  }

  // Warm up the handler sidecar so it isn't cold on first playback
  if (config.handler.url) {
    fetch(`${config.handler.url}/health`, { signal: AbortSignal.timeout(10000) })
      .then(() => console.log('[VOID] Handler sidecar warmed up'))
      .catch(e => console.warn('[VOID] Handler sidecar warm-up failed (may be starting):', e.message));
  }
});

module.exports = app;
