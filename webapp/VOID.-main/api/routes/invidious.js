'use strict';

const express    = require('express');
const router     = express.Router();
const controller = require('../controllers/invidious');

// GET /api/invidious/playlist?id=PLAYLIST_ID
router.get('/playlist', controller.playlist);

module.exports = router;
