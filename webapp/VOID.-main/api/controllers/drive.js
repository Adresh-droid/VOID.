/**
 * api/controllers/drive.js
 * Google Drive operations for VOID Player.
 *
 * Each user gets a "VOID Player" folder in their Drive.
 * Inside that folder:
 *   • void_library.json   — track metadata (titles, artists, playlists, settings)
 *   • audio/<blobId>.mp3  — uploaded local audio files (optional, if user enables)
 */

'use strict';

const { google } = require('googleapis');
const config = require('../../config');

const FOLDER_NAME  = config.google.driveFolderName;
const LIBRARY_FILE = 'void_library.json';
const AUDIO_FOLDER = 'audio';

// ── Build an authenticated Drive client from user tokens ──────────────────
function getDriveClient(user) {
  const oauth2 = new google.auth.OAuth2(
    config.google.clientId,
    config.google.clientSecret,
    config.google.callbackUrl
  );
  oauth2.setCredentials({
    access_token:  user.accessToken,
    refresh_token: user.refreshToken,
  });
  return google.drive({ version: 'v3', auth: oauth2 });
}

// ── Find or create a folder by name under a parent ───────────────────────
async function ensureFolder(drive, name, parentId = null) {
  const q = parentId
    ? `name='${name}' and mimeType='application/vnd.google-apps.folder' and '${parentId}' in parents and trashed=false`
    : `name='${name}' and mimeType='application/vnd.google-apps.folder' and trashed=false`;

  const res = await drive.files.list({ q, fields: 'files(id,name)', spaces: 'drive' });
  if (res.data.files.length > 0) return res.data.files[0].id;

  const meta = {
    name,
    mimeType: 'application/vnd.google-apps.folder',
    ...(parentId ? { parents: [parentId] } : {}),
  };
  const created = await drive.files.create({ resource: meta, fields: 'id' });
  return created.data.id;
}

// ── Find a file by name in a folder ──────────────────────────────────────
async function findFile(drive, name, folderId) {
  const q = `name='${name}' and '${folderId}' in parents and trashed=false`;
  const res = await drive.files.list({ q, fields: 'files(id,name)', spaces: 'drive' });
  return res.data.files.length > 0 ? res.data.files[0] : null;
}

// ── Download a JSON file from Drive ──────────────────────────────────────
async function downloadJSON(drive, fileId) {
  const res = await drive.files.get(
    { fileId, alt: 'media' },
    { responseType: 'arraybuffer' }
  );
  return JSON.parse(Buffer.from(res.data).toString('utf-8'));
}

// ── Upload / overwrite a JSON file in Drive ───────────────────────────────
async function uploadJSON(drive, name, data, folderId, existingFileId = null) {
  const { Readable } = require('stream');
  const content = Buffer.from(JSON.stringify(data, null, 2));
  // Create a fresh stream each call — Readable.from() is one-shot so we can't
  // reuse the same instance if googleapis retries internally.
  const makeStream = () => Readable.from(content);

  if (existingFileId) {
    await drive.files.update({
      fileId: existingFileId,
      media: { mimeType: 'application/json', body: makeStream() },
    });
    return existingFileId;
  }
  const res = await drive.files.create({
    resource: { name, parents: [folderId] },
    media: { mimeType: 'application/json', body: makeStream() },
    fields: 'id',
  });
  return res.data.id;
}

// ── Helper: list all audio files in the Drive audio folder ───────────────
async function listAudioFiles(drive, audioFolderId) {
  const files = [];
  let pageToken = null;
  do {
    const params = {
      q: `'${audioFolderId}' in parents and trashed=false`,
      fields: 'nextPageToken,files(id,name,size,mimeType)',
      spaces: 'drive',
      pageSize: 1000,
    };
    if (pageToken) params.pageToken = pageToken;
    const res = await drive.files.list(params);
    files.push(...(res.data.files || []));
    pageToken = res.data.nextPageToken || null;
  } while (pageToken);
  return files;
}

// ── Controller: GET /api/drive/library ───────────────────────────────────
exports.getLibrary = async (req, res) => {
  try {
    const drive    = getDriveClient(req.user);
    const rootId   = await ensureFolder(drive, FOLDER_NAME);

    // Always fetch the audio file list in parallel with the library JSON.
    // This lets the client reconstruct tracks from filenames if the JSON is missing.
    const audioFolderRes = await drive.files.list({
      q: `name='${AUDIO_FOLDER}' and mimeType='application/vnd.google-apps.folder' and '${rootId}' in parents and trashed=false`,
      fields: 'files(id)',
      spaces: 'drive',
    });
    const audioFolderId = audioFolderRes.data.files.length > 0
      ? audioFolderRes.data.files[0].id
      : null;

    const [libFile, audioFiles] = await Promise.all([
      findFile(drive, LIBRARY_FILE, rootId),
      audioFolderId ? listAudioFiles(drive, audioFolderId) : Promise.resolve([]),
    ]);

    // No library JSON at all — return just the audio file list so client can reconstruct
    if (!libFile) {
      return res.json({ library: null, audioFiles });
    }

    const data = await downloadJSON(drive, libFile.id);
    // Always attach audio file list so client can fill in any tracks missing a driveFileId
    res.json({ library: data, audioFiles });
  } catch (err) {
    console.error('[Drive] getLibrary error:', err.message);
    res.status(502).json({ error: 'Could not read from Google Drive', detail: err.message });
  }
};

// ── Controller: GET /api/drive/audio-files ───────────────────────────────
// Returns the list of audio files in the Drive audio folder.
// Useful for rescanning without a full library fetch.
exports.listAudioFiles = async (req, res) => {
  try {
    const drive    = getDriveClient(req.user);
    const rootId   = await ensureFolder(drive, FOLDER_NAME);
    const audioFolderRes = await drive.files.list({
      q: `name='${AUDIO_FOLDER}' and mimeType='application/vnd.google-apps.folder' and '${rootId}' in parents and trashed=false`,
      fields: 'files(id)',
      spaces: 'drive',
    });
    if (!audioFolderRes.data.files.length) return res.json({ audioFiles: [] });
    const audioFolderId = audioFolderRes.data.files[0].id;
    const audioFiles = await listAudioFiles(drive, audioFolderId);
    res.json({ audioFiles });
  } catch (err) {
    console.error('[Drive] listAudioFiles error:', err.message);
    res.status(502).json({ error: 'Could not list audio files from Google Drive', detail: err.message });
  }
};

// ── Controller: POST /api/drive/library ──────────────────────────────────
exports.saveLibrary = async (req, res) => {
  try {
    const { library } = req.body;
    if (!library || typeof library !== 'object') {
      return res.status(400).json({ error: 'Missing library payload' });
    }
    library._savedAt = new Date().toISOString();
    library._version = 1;

    const drive   = getDriveClient(req.user);
    const rootId  = await ensureFolder(drive, FOLDER_NAME);
    const libFile = await findFile(drive, LIBRARY_FILE, rootId);

    await uploadJSON(drive, LIBRARY_FILE, library, rootId, libFile?.id || null);
    res.json({ ok: true, savedAt: library._savedAt });
  } catch (err) {
    console.error('[Drive] saveLibrary error:', err.message);
    res.status(502).json({ error: 'Could not write to Google Drive', detail: err.message });
  }
};

// ── Controller: POST /api/drive/upload-audio ─────────────────────────────
// Accepts multipart/form-data with field "audio" (the audio file) + "blobId" + "meta" (JSON)
exports.uploadAudio = async (req, res) => {
  try {
    console.log('[Drive] uploadAudio called');
    console.log('[Drive] req.file:', req.file ? `${req.file.originalname} (${req.file.size} bytes)` : 'MISSING');
    console.log('[Drive] req.body:', JSON.stringify(req.body));
    console.log('[Drive] content-type:', req.headers['content-type']);
    if (!req.file) return res.status(400).json({ error: 'No file uploaded', debug: { body: req.body, contentType: req.headers['content-type'] } });

    const { blobId } = req.body;
    if (!blobId) return res.status(400).json({ error: 'Missing blobId' });

    const drive      = getDriveClient(req.user);
    const rootId     = await ensureFolder(drive, FOLDER_NAME);
    const audioId    = await ensureFolder(drive, AUDIO_FOLDER, rootId);

    // Use the real filename sent by the client (e.g. "Artist - Title.mp3").
    // Fall back to blobId if somehow missing. Strip any path components for safety.
    const rawName   = (req.file.originalname && req.file.originalname !== 'blob')
      ? req.file.originalname.replace(/^.*[/\\]/, '')
      : blobId + '.mp3';
    // Ensure it ends with a proper audio extension
    const hasAudioExt = /\.(mp3|m4a|flac|ogg|wav|aac|opus|weba)$/i.test(rawName);
    const fileName  = hasAudioExt ? rawName : rawName + '.mp3';

    // Dedup: check by blobId-tagged name (legacy) AND new filename to avoid re-uploading
    const existingByName  = await findFile(drive, fileName, audioId);
    if (existingByName) return res.json({ ok: true, fileId: existingByName.id, cached: true });
    const existingByBlob  = await findFile(drive, blobId, audioId);
    if (existingByBlob) {
      // Rename the legacy blobId file to the proper filename for future imports
      await drive.files.update({ fileId: existingByBlob.id, resource: { name: fileName } }).catch(() => {});
      return res.json({ ok: true, fileId: existingByBlob.id, cached: true });
    }

    // Stream upload to Drive
    const { Readable } = require('stream');
    const readable = Readable.from(req.file.buffer);

    const driveFile = await drive.files.create({
      resource: { name: fileName, parents: [audioId] },
      media:    { mimeType: req.file.mimetype || 'audio/mpeg', body: readable },
      fields:   'id,size',
    });

    res.json({ ok: true, fileId: driveFile.data.id });
  } catch (err) {
    console.error('[Drive] uploadAudio error:', err.message);
    res.status(502).json({ error: 'Could not upload audio to Google Drive', detail: err.message });
  }
};

// ── Controller: GET /api/drive/audio/:fileId ─────────────────────────────
// Streams an audio file from Drive back to the browser (so the audio element can play it)
exports.streamAudio = async (req, res) => {
  try {
    const { fileId } = req.params;
    const drive = getDriveClient(req.user);

    // Get file metadata for content-type
    const meta = await drive.files.get({ fileId, fields: 'name,mimeType,size' });
    const mimeType = meta.data.mimeType || 'audio/mpeg';
    const size     = parseInt(meta.data.size || '0', 10);

    // Cache aggressively — audio files don't change after upload
    res.setHeader('Cache-Control', 'private, max-age=86400');
    res.setHeader('X-Content-Type-Options', 'nosniff');

    // Range request support (needed for <audio> seeking)
    const rangeHeader = req.headers.range;
    if (rangeHeader && size > 0) {
      const [startStr, endStr] = rangeHeader.replace('bytes=', '').split('-');
      const start = parseInt(startStr, 10);
      const end   = endStr ? parseInt(endStr, 10) : size - 1;
      const chunkSize = end - start + 1;

      res.writeHead(206, {
        'Content-Range':  `bytes ${start}-${end}/${size}`,
        'Accept-Ranges':  'bytes',
        'Content-Length': chunkSize,
        'Content-Type':   mimeType,
        'Cache-Control':  'private, max-age=86400',
      });

      const stream = await drive.files.get(
        { fileId, alt: 'media' },
        { responseType: 'stream', headers: { Range: `bytes=${start}-${end}` } }
      );
      stream.data.pipe(res);
    } else {
      if (size > 0) res.setHeader('Content-Length', size);
      res.setHeader('Content-Type', mimeType);
      res.setHeader('Accept-Ranges', 'bytes');

      const stream = await drive.files.get({ fileId, alt: 'media' }, { responseType: 'stream' });
      stream.data.pipe(res);
    }
  } catch (err) {
    console.error('[Drive] streamAudio error:', err.message);
    res.status(502).json({ error: 'Could not stream audio from Google Drive' });
  }
};

// ── Controller: DELETE /api/drive/audio/:fileId ──────────────────────────
exports.deleteAudio = async (req, res) => {
  try {
    const { fileId } = req.params;
    const drive = getDriveClient(req.user);
    await drive.files.delete({ fileId });
    res.json({ ok: true });
  } catch (err) {
    console.error('[Drive] deleteAudio error:', err.message);
    res.status(502).json({ error: 'Could not delete from Google Drive' });
  }
};

// ── Controller: POST /api/drive/prune-audio ───────────────────────────────
// Body: { knownBlobIds: ['blob_...', ...] }
// Lists all files in the Drive audio folder and deletes any whose name is NOT
// in knownBlobIds. Call this after saveLibrary to keep Drive tidy.
exports.pruneAudio = async (req, res) => {
  try {
    const { knownBlobIds } = req.body;
    if (!Array.isArray(knownBlobIds)) {
      return res.status(400).json({ error: 'knownBlobIds must be an array' });
    }

    const known = new Set(knownBlobIds);
    const drive   = getDriveClient(req.user);
    const rootId  = await ensureFolder(drive, FOLDER_NAME);
    const audioId = await ensureFolder(drive, AUDIO_FOLDER, rootId);

    // List every file in the audio folder
    const q   = `'${audioId}' in parents and trashed=false`;
    const res2 = await drive.files.list({ q, fields: 'files(id,name)', spaces: 'drive' });
    const files = res2.data.files || [];

    // Delete anything not in the known set
    const toDelete = files.filter(f => !known.has(f.name));
    await Promise.all(toDelete.map(f =>
      drive.files.delete({ fileId: f.id }).catch(e =>
        console.warn('[Drive] pruneAudio: could not delete', f.name, e.message)
      )
    ));

    console.log(`[Drive] pruneAudio: ${toDelete.length} orphaned file(s) deleted out of ${files.length} total`);
    res.json({ ok: true, deleted: toDelete.length, total: files.length });
  } catch (err) {
    console.error('[Drive] pruneAudio error:', err.message);
    res.status(502).json({ error: 'Could not prune Drive audio folder', detail: err.message });
  }
};
