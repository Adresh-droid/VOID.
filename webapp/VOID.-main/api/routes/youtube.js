'use strict';

const express    = require('express');
const router     = express.Router();
const controller = require('../controllers/youtube');

// Search (songs, artists, albums, podcasts)
// GET /api/youtube/search?q=&type=all|song|artist|album|podcast
router.get('/search', controller.search);

// Video/song metadata
// GET /api/youtube/videos?ids=
router.get('/videos', controller.videoDetails);

// Artist page (info + top songs)
// GET /api/youtube/artist?id=
router.get('/artist', controller.artistPage);

// Album page (info + songs)
// GET /api/youtube/album?id=
router.get('/album', controller.albumPage);

// Podcast show + episodes
// GET /api/youtube/podcast?id=
router.get('/podcast', controller.podcastPage);

// JioSaavn playlist import
// GET /api/youtube/playlist?id=PLAYLIST_ID_OR_URL
router.get('/playlist', controller.playlistItems);

// Stream proxy
// GET /api/youtube/stream?id=
router.get('/stream', controller.streamProxy);

// Synced Lyrics
// GET /api/youtube/lyrics?track=&artist=
router.get('/lyrics', controller.lyrics);

router.get('/audio', controller.audioProxy);

module.exports = router;
