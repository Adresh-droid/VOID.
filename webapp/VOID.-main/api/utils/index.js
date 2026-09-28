/**
 * api/utils/index.js
 * Shared utility helpers for route handlers.
 */

'use strict';

/**
 * Wrap an async route handler so uncaught errors are forwarded to next().
 * Avoids try/catch boilerplate in every controller.
 *
 * Usage:
 *   router.get('/path', asyncHandler(async (req, res) => { ... }));
 */
exports.asyncHandler = (fn) => (req, res, next) =>
  Promise.resolve(fn(req, res, next)).catch(next);

/**
 * Standard API success response envelope.
 */
exports.ok = (res, data, status = 200) =>
  res.status(status).json({ success: true, data });

/**
 * Standard API error response envelope.
 */
exports.fail = (res, message, status = 400) =>
  res.status(status).json({ success: false, error: message });
