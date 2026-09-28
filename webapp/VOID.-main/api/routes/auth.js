/**
 * api/routes/auth.js
 * Google OAuth 2.0 authentication routes.
 *
 * Web flow:
 *   GET  /api/auth/google           → redirect to Google consent screen
 *   GET  /api/auth/google/callback  → Passport exchanges code, sets cookie session,
 *                                     redirects to /?auth=success
 *
 * Flutter / API client flow:
 *   GET  /api/auth/google           → same redirect (open in WebView or browser)
 *   GET  /api/auth/google/callback?mode=token
 *                                   → redirects to /?auth=success&token=<JWT>
 *                                     (Flutter intercepts the deep-link and extracts the token)
 *   POST /api/auth/token            → while cookie session is still live, exchange for JWT
 *
 * Both flows share:
 *   GET  /api/auth/me               → current user (session OR Bearer JWT); returns { user: null } if not authed
 *   POST /api/auth/logout           → invalidate session (web); JWT clients just discard token client-side
 *   GET  /api/auth/photo            → proxies Google profile photo (avoids CSP/SW issues)
 */

'use strict';

const express  = require('express');
const passport = require('passport');
const router   = express.Router();
const { requireAuth, issueJWT } = require('../middleware/auth');

// ── Initiate OAuth flow ────────────────────────────────────────────────────
router.get('/google',
  passport.authenticate('google', {
    scope: [
      'profile',
      'email',
      'https://www.googleapis.com/auth/drive.file',
    ],
    accessType: 'offline',
    prompt: 'consent',
  })
);

// ── OAuth callback ─────────────────────────────────────────────────────────
router.get('/google/callback',
  passport.authenticate('google', { failureRedirect: '/?auth=error' }),
  (req, res) => {
    // ?mode=token → embed JWT in redirect fragment for Flutter deep-link interception
    if (req.query.mode === 'token') {
      const token = issueJWT(req.user);
      return res.redirect(`/?auth=success&token=${encodeURIComponent(token)}`);
    }
    // Default web flow: session cookie is already set by Passport
    res.redirect('/?auth=success');
  }
);

// ── Exchange session for JWT ───────────────────────────────────────────────
// POST /api/auth/token
// Called immediately after OAuth callback while the session cookie is live.
// Returns a JWT the client can store locally for future requests.
router.post('/token', requireAuth, (req, res) => {
  const token = issueJWT(req.user);
  res.json({
    token,
    user: {
      id:          req.user.id,
      displayName: req.user.displayName,
      email:       req.user.email,
      photo:       req.user.photo,
    },
  });
});

// ── Get current user ───────────────────────────────────────────────────────
// Accepts both cookie session (web) and Bearer JWT (Flutter).
// Returns { user: null } instead of 401 when not authenticated,
// so the frontend can call this on every page load without error handling.
router.get('/me', (req, res) => {
  // Strategy 1: Passport cookie session
  if (req.isAuthenticated && req.isAuthenticated()) {
    return res.json({
      user: {
        id:          req.user.id,
        displayName: req.user.displayName,
        email:       req.user.email,
        photo:       req.user.photo,
      },
    });
  }

  // Strategy 2: Bearer JWT
  const header = req.headers.authorization;
  if (header && header.startsWith('Bearer ')) {
    const jwt = require('jsonwebtoken');
    const config = require('../../config');
    try {
      const payload = jwt.verify(header.slice(7), config.jwt.secret);
      return res.json({
        user: {
          id:          payload.id,
          displayName: payload.displayName,
          email:       payload.email,
          photo:       payload.photo,
        },
      });
    } catch {
      // Invalid/expired token — treat as unauthenticated
    }
  }

  res.json({ user: null });
});

// ── Logout ─────────────────────────────────────────────────────────────────
router.post('/logout', (req, res, next) => {
  req.logout((err) => {
    if (err) return next(err);
    req.session = null;
    res.clearCookie('void.sess');
    res.json({ ok: true });
  });
});

// ── Proxy Google profile photo ─────────────────────────────────────────────
router.get('/photo', requireAuth, async (req, res) => {
  if (!req.user.photo) return res.status(404).end();
  try {
    const response = await fetch(req.user.photo);
    if (!response.ok) return res.status(502).end();
    res.setHeader('Content-Type', response.headers.get('content-type') || 'image/jpeg');
    res.setHeader('Cache-Control', 'public, max-age=86400');
    const buf = await response.arrayBuffer();
    res.send(Buffer.from(buf));
  } catch {
    res.status(502).end();
  }
});

module.exports = router;
