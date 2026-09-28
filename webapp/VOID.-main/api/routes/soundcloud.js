'use strict';

const express    = require('express');
const router     = express.Router();
const controller = require('../controllers/soundcloud');

// Search tracks, artists, playlists
// GET /api/soundcloud/search?q=&type=all|song|artist|playlist
router.get('/search', controller.search);

// Artist/user page (profile + tracks)
// GET /api/soundcloud/artist?id=USER_ID
router.get('/artist', controller.artistPage);

// Playlist page (info + tracks)
// GET /api/soundcloud/playlist?id=PLAYLIST_ID_OR_URL
router.get('/playlist', controller.playlistPage);

// Stream proxy
// GET /api/soundcloud/stream?id=
router.get('/stream', controller.streamProxy);

// Audio proxy
// GET /api/soundcloud/audio?id=
router.get('/audio', controller.audioProxy);

module.exports = router;
