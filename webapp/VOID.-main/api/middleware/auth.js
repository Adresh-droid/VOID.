/**
 * api/middleware/auth.js
 *
 * Auth helpers shared across routes.
 *
 * VOID supports two auth strategies in parallel:
 *
 *   1. Cookie session (web app) — Passport sets req.user via deserializeUser.
 *      req.isAuthenticated() returns true. No changes needed for existing web flow.
 *
 *   2. Bearer JWT (Flutter / API clients) — client sends
 *      Authorization: Bearer <token> after exchanging the OAuth code at
 *      POST /api/auth/token. The middleware decodes + attaches req.user.
 *
 * Both strategies expose the same req.user shape:
 *   { id, displayName, email, photo, accessToken, refreshToken }
 *
 * Usage:
 *   const { requireAuth } = require('../middleware/auth');
 *   router.get('/protected', requireAuth, handler);
 */

'use strict';

const jwt    = require('jsonwebtoken');
const config = require('../../config');

/**
 * requireAuth
 *
 * Accepts either a valid Passport session OR a valid Bearer JWT.
 * Returns 401 if neither is present / valid.
 */
exports.requireAuth = (req, res, next) => {
  // ── Strategy 1: Passport cookie session (web) ──────────────────────────
  if (req.isAuthenticated && req.isAuthenticated()) return next();

  // ── Strategy 2: Bearer JWT (Flutter / API clients) ────────────────────
  const header = req.headers.authorization;
  if (header && header.startsWith('Bearer ')) {
    const token = header.slice(7);
    try {
      const payload = jwt.verify(token, config.jwt.secret);
      req.user = payload;
      return next();
    } catch (err) {
      const status = err.name === 'TokenExpiredError' ? 403 : 401;
      return res.status(status).json({ error: err.message });
    }
  }

  res.status(401).json({ error: 'Authentication required' });
};

/**
 * issueJWT
 *
 * Signs a JWT from a Passport user object.
 * Called after successful OAuth to give API clients a token they can store.
 *
 * @param {object} user  — req.user from Passport
 * @returns {string}     — signed JWT
 */
exports.issueJWT = (user) =>
  jwt.sign(
    {
      id:           user.id,
      displayName:  user.displayName,
      email:        user.email,
      photo:        user.photo,
      accessToken:  user.accessToken,
      refreshToken: user.refreshToken,
    },
    config.jwt.secret,
    { expiresIn: config.jwt.expiresIn }
  );
