/**
 * api/index.js
 * Root API router — mounts all sub-routers under /api/*
 */

'use strict';

const express = require('express');
const router  = express.Router();

// ── Active routes ────────────────────────────────────────────────────────────
router.use('/health',     require('./routes/health'));
router.use('/suggest',    require('./routes/suggest'));
router.use('/youtube',    require('./routes/youtube'));
router.use('/soundcloud', require('./routes/soundcloud'));
router.use('/auth',       require('./routes/auth'));
router.use('/drive',      require('./routes/drive'));
router.use('/handler',    require('./routes/handler'));
router.use('/invidious',  require('./routes/invidious'));

module.exports = router;
