package com.voidplayer.music.utils.sabr

import android.content.Context
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.voidplayer.music.utils.cipher.CipherDeobfuscator
import com.voidplayer.music.utils.cipher.PlayerJsFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object EjsNTransformSolver {
    private const val TAG = "VOID_EjsNSolver"

    private var solverWebView: SolverWebView? = null

    
    suspend fun transformNParamInUrl(url: String): String {
        val nMatch = Regex("[?&]n=([^&]+)").find(url)
        if (nMatch == null) {
            Timber.tag(TAG).d("No 'n' parameter in SABR URL")
            return url
        }
        val nValue = Uri.decode(nMatch.groupValues[1])
        Timber.tag(TAG).d("SABR n-param: $nValue")

        return withContext(NonCancellable) {
            val solver = getOrCreateSolver()
            if (solver == null) {
                return@withContext url
            }

            if (!solver.nFunctionAvailable) {
                Timber.tag(TAG).e("EJS n-solver not available")
                return@withContext url
            }

            try {
                val transformed = solver.transformN(nValue)
                Timber.tag(TAG).d("SABR n-param transformed: $nValue -> $transformed")
                url.replaceFirst(
                    Regex("([?&])n=[^&]+"),
                    "$1n=${Uri.encode(transformed)}"
                )
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "SABR n-transform failed: ${e.message}")
                url
            }
        }
    }

    suspend fun deobfuscateSignature(signature: String): String? {
        return withContext(NonCancellable) {
            val solver = getOrCreateSolver() ?: return@withContext null
            if (!solver.sigFunctionAvailable) {
                Timber.tag(TAG).e("EJS sig-solver not available")
                return@withContext null
            }

            try {
                val deobfuscated = solver.deobfuscateSig(signature)
                Timber.tag(TAG).d("Signature deobfuscated via EJS")
                deobfuscated
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "EJS sig deobfuscation failed: ${e.message}")
                null
            }
        }
    }

    private suspend fun getOrCreateSolver(): SolverWebView? {
        solverWebView?.let { 
            if (it.isDead) solverWebView = null
            else return it 
        }

        return withContext(NonCancellable) {
            solverWebView?.let { return@withContext it }

            val result = PlayerJsFetcher.getPlayerJs(forceRefresh = false)
            if (result == null) {
                Timber.tag(TAG).e("Failed to get player JS for EJS solver")
                return@withContext null
            }
            val (playerJs, hash) = result
            Timber.tag(TAG).d("Creating EJS n-solver (player hash=$hash, ${playerJs.length} chars)")

            try {
                val sv = SolverWebView.create(CipherDeobfuscator.appContext, playerJs)
                solverWebView = sv
                sv
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to create EJS solver: ${e.message}")
                null
            }
        }
    }

    suspend fun close() {
        withContext(Dispatchers.Main) {
            solverWebView?.close()
        }
        solverWebView = null
    }

    class SolverWebView private constructor(
        context: Context,
        private val playerJs: String,
        private val initContinuation: Continuation<SolverWebView>,
    ) {
        private val webView = WebView(context)
        private var nContinuation: Continuation<String>? = null
        private var sigContinuation: Continuation<String>? = null

        @Volatile
        var nFunctionAvailable: Boolean = false
            private set

        @Volatile
        var sigFunctionAvailable: Boolean = false
            private set

        @Volatile
        var isDead: Boolean = false
            private set

        init {
            val settings = webView.settings
            @Suppress("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            @Suppress("DEPRECATION")
            settings.allowFileAccessFromFileURLs = true
            settings.blockNetworkLoads = true

            webView.addJavascriptInterface(this, "EjsBridge")

            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                    val msg = m.message()
                    if (msg.contains("Uncaught")) {
                        Timber.tag(TAG).e("EJS WebView error: $msg at ${m.sourceId()}:${m.lineNumber()}")
                        if (msg.contains("render process gone")) {
                            isDead = true
                        }
                    }
                    return super.onConsoleMessage(m)
                }
            }
        }

        private fun loadSolverAndPlayer() {
            val cacheDir = File(webView.context.cacheDir, "ejs_solver")
            cacheDir.mkdirs()

            val assetManager = webView.context.assets
            for (file in listOf("meriyah.js", "astring.js", "yt.solver.core.js")) {
                try {
                    assetManager.open("solver/$file").use { input ->
                        File(cacheDir, file).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to copy solver asset: $file")
                }
            }

            File(cacheDir, "player.js").writeText(playerJs)
            Timber.tag(TAG).d("EJS solver assets written (${playerJs.length} chars)")

            val html = """<!DOCTYPE html>
<html><head>
<script src="meriyah.js"></script>
<script src="astring.js"></script>
<script src="yt.solver.core.js"></script>
<script>
var _nSolver = null;
var _sigSolver = null;

function initSolver() {
    try {
        EjsBridge.onLog('Reading player.js via XHR...');
        var xhr = new XMLHttpRequest();
        xhr.open('GET', 'player.js', true);
        xhr.onload = function() {
            try {
                var playerCode = xhr.responseText;
                if (!playerCode || playerCode.length < 1000) {
                    EjsBridge.onSolverError('Player JS too small: ' + (playerCode ? playerCode.length : 0));
                    return;
                }
                EjsBridge.onLog('Preprocessing with EJS solver (' + playerCode.length + ' chars)...');

                var result = jsc({
                    type: 'player',
                    player: playerCode,
                    requests: [],
                    output_preprocessed: true
                });

                if (result.type === 'error') {
                    EjsBridge.onSolverError('EJS preprocess: ' + result.error);
                    return;
                }

                EjsBridge.onLog('Evaluating preprocessed code...');
                var resultObj = {n: null, sig: null};
                (new Function('_result', result.preprocessed_player))(resultObj);

                _nSolver = resultObj.n;
                _sigSolver = resultObj.sig;
                EjsBridge.onSolverReady((typeof _nSolver === 'function').toString(), (typeof _sigSolver === 'function').toString());
            } catch(e) {
                EjsBridge.onSolverError(e.toString() + '\n' + (e.stack || ''));
            }
        };
        xhr.onerror = function() {
            EjsBridge.onSolverError('XHR failed to read player.js');
        };
        xhr.send();
    } catch(e) {
        EjsBridge.onSolverError(e.toString() + '\n' + (e.stack || ''));
    }
}

function transformN(nValue) {
    try {
        if (!_nSolver) {
            EjsBridge.onNError('N solver not available');
            return;
        }
        var result = _nSolver(nValue);
        if (result === undefined || result === null) {
            EjsBridge.onNError('N solver returned null/undefined');
            return;
        }
        EjsBridge.onNResult(String(result));
    } catch(e) {
        EjsBridge.onNError(e.toString() + '\n' + (e.stack || ''));
    }
}

function deobfuscateSig(sigValue) {
    try {
        if (!_sigSolver) {
            EjsBridge.onSigError('Sig solver not available');
            return;
        }
        var result = _sigSolver(sigValue);
        if (result === undefined || result === null) {
            EjsBridge.onSigError('Sig solver returned null/undefined');
            return;
        }
        EjsBridge.onSigResult(String(result));
    } catch(e) {
        EjsBridge.onSigError(e.toString() + '\n' + (e.stack || ''));
    }
}
</script>
</head><body onload="initSolver()"></body></html>"""

            webView.loadDataWithBaseURL(
                "file://${cacheDir.absolutePath}/",
                html, "text/html", "utf-8", null
            )
        }

        @JavascriptInterface
        fun onLog(message: String) {
            Timber.tag(TAG).d(message)
        }

        @JavascriptInterface
        fun onSolverReady(nAvailable: String, sigAvailable: String) {
            nFunctionAvailable = nAvailable == "true"
            sigFunctionAvailable = sigAvailable == "true"
            Timber.tag(TAG).d("EJS solver ready: n=$nFunctionAvailable, sig=$sigFunctionAvailable")
            initContinuation.resume(this)
        }

        @JavascriptInterface
        fun onSolverError(error: String) {
            Timber.tag(TAG).e("EJS solver error: $error")
            initContinuation.resume(this)
        }

        suspend fun transformN(nValue: String): String {
            if (!nFunctionAvailable) {
                throw SabrException("EJS n-transform not available")
            }

            return withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { cont ->
                    nContinuation = cont
                    webView.evaluateJavascript(
                        "transformN('${escapeJsString(nValue)}')",
                        null
                    )
                }
            }
        }

        suspend fun deobfuscateSig(sigValue: String): String {
            if (!sigFunctionAvailable) {
                throw SabrException("EJS sig-transform not available")
            }

            return withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { cont ->
                    sigContinuation = cont
                    webView.evaluateJavascript(
                        "deobfuscateSig('${escapeJsString(sigValue)}')",
                        null
                    )
                }
            }
        }

        @JavascriptInterface
        fun onNResult(result: String) {
            Timber.tag(TAG).d("N-transform result: ${result.take(50)}")
            nContinuation?.resume(result)
            nContinuation = null
        }

        @JavascriptInterface
        fun onNError(error: String) {
            Timber.tag(TAG).e("N-transform error: $error")
            nContinuation?.resumeWithException(SabrException("EJS n-transform failed: $error"))
            nContinuation = null
        }

        @JavascriptInterface
        fun onSigResult(result: String) {
            Timber.tag(TAG).d("Sig result: ${result.take(50)}")
            sigContinuation?.resume(result)
            sigContinuation = null
        }

        @JavascriptInterface
        fun onSigError(error: String) {
            Timber.tag(TAG).e("Sig error: $error")
            sigContinuation?.resumeWithException(SabrException("EJS sig deobfuscation failed: $error"))
            sigContinuation = null
        }

        fun close() {
            webView.clearHistory()
            webView.clearCache(true)
            webView.loadUrl("about:blank")
            webView.onPause()
            webView.removeAllViews()
            webView.destroy()
            Timber.tag(TAG).d("EJS solver WebView closed")
        }

        private fun escapeJsString(s: String): String {
            return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
        }

        companion object {
            suspend fun create(context: Context, playerJs: String): SolverWebView {
                return withContext(Dispatchers.Main) {
                    suspendCancellableCoroutine { cont ->
                        val sv = SolverWebView(context, playerJs, cont)
                        sv.loadSolverAndPlayer()
                    }
                }
            }
        }
    }
}


