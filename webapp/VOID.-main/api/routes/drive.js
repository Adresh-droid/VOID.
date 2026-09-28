/**
 * api/routes/drive.js
 * Drive sync routes — all require authentication.
 * Supports both cookie session (web) and Bearer JWT (Flutter).
 */

'use strict';

const express    = require('express');
const multer     = require('multer');
const router     = express.Router();
const controller = require('../controllers/drive');
const { requireAuth } = require('../middleware/auth');

// multer: memory storage (we stream straight to Drive, no disk writes)
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 100 * 1024 * 1024 },
});

// All drive routes require auth (cookie session OR Bearer JWT)
router.use(requireAuth);

// Library (track metadata + playlists + settings)
router.get('/library',       controller.getLibrary);
router.post('/library',      express.json({ limit: '5mb' }), controller.saveLibrary);

// Audio file listing
router.get('/audio-files',   controller.listAudioFiles);

// Audio file upload/stream/delete
router.post('/upload-audio', upload.single('audio'), controller.uploadAudio);
router.get('/audio/:fileId', controller.streamAudio);
router.delete('/audio/:fileId', controller.deleteAudio);
router.post('/prune-audio', express.json({ limit: '1mb' }), controller.pruneAudio);

module.exports = router;
