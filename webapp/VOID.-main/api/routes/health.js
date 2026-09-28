/**
 * api/routes/health.js
 * Liveness / readiness probe.
 * Render uses this to verify the service is up.
 */

'use strict';

const express = require('express');
const router  = express.Router();

// GET /api/health
router.get('/', (_req, res) => {
  res.json({
    status: 'ok',
    app:    'void-player',
    time:   new Date().toISOString(),
  });
});

module.exports = router;
