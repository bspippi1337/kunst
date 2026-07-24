package org.blckswan.art

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.InputStream
import java.util.zip.GZIPInputStream

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(10, 10, 11)
        window.navigationBarColor = Color.rgb(10, 10, 11)

        webView = WebView(this).apply {
            setBackgroundColor(Color.rgb(10, 10, 11))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.setSupportZoom(true)
            webChromeClient = WebChromeClient()
            addJavascriptInterface(AndroidBridge(), "Android")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return false
                    if (uri.scheme == "file") return false
                    openExternal(uri)
                    return true
                }

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    val uri = url?.let(Uri::parse) ?: return false
                    if (uri.scheme == "file") return false
                    openExternal(uri)
                    return true
                }
            }
        }

        setContentView(webView)
        loadGallery()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                webView.evaluateJavascript(
                    "document.getElementById('viewer')?.classList.contains('open') === true"
                ) { result ->
                    when {
                        result == "true" -> webView.evaluateJavascript("closeWork()", null)
                        webView.canGoBack() -> webView.goBack()
                        else -> finish()
                    }
                }
            }
        })
    }

    private fun loadGallery() {
        runCatching { decodeGalleryHtml() }
            .onSuccess { html ->
                webView.loadDataWithBaseURL(
                    "file:///android_asset/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
            .onFailure { error ->
                val message = error.message.orEmpty()
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                webView.loadDataWithBaseURL(
                    null,
                    """<!doctype html><html><body style='margin:0;padding:32px;background:#0A0A0B;color:#F2F2F2;font-family:sans-serif'><h1>\\$/</h1><h2>Kunne ikke åpne kunstarkivet</h2><p>$message</p></body></html>""",
                    "text/html",
                    "UTF-8",
                    null
                )
            }
    }

    private fun decodeGalleryHtml(): String {
        val encoded = buildString {
            append(readAssetText("v3.part00"))
            append(readAssetText("v3.part01"))
        }
        val compressed = Base64.decode(encoded, Base64.DEFAULT)
        GZIPInputStream(ByteArrayInputStream(compressed)).use { tar ->
            return extractIndexFromTar(tar)
                ?: error("index.html mangler i kunstarkivet")
        }
    }

    private fun readAssetText(name: String): String =
        assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun extractIndexFromTar(input: InputStream): String? {
        while (true) {
            val header = readExactOrNull(input, TAR_BLOCK) ?: return null
            if (header.all { it == 0.toByte() }) return null

            val name = tarString(header, 0, 100)
            val prefix = tarString(header, 345, 155)
            val path = if (prefix.isBlank()) name else "$prefix/$name"
            val size = tarOctal(header, 124, 12)
            require(size in 0..MAX_ENTRY_BYTES) { "Ugyldig arkivstørrelse" }

            val data = readExact(input, size.toInt())
            val padding = ((TAR_BLOCK - (size % TAR_BLOCK)) % TAR_BLOCK).toInt()
            if (padding > 0) readExact(input, padding)

            if (path.endsWith("blckswan-kunst/app/src/main/assets/index.html")) {
                return data.toString(Charsets.UTF_8)
            }
        }
    }

    private fun tarString(bytes: ByteArray, offset: Int, length: Int): String {
        val end = (offset until offset + length)
            .firstOrNull { bytes[it] == 0.toByte() } ?: offset + length
        return bytes.copyOfRange(offset, end).toString(Charsets.UTF_8).trim()
    }

    private fun tarOctal(bytes: ByteArray, offset: Int, length: Int): Long {
        val value = tarString(bytes, offset, length).trim()
        return if (value.isBlank()) 0L else value.toLong(8)
    }

    private fun readExactOrNull(input: InputStream, length: Int): ByteArray? {
        val buffer = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val count = input.read(buffer, offset, length - offset)
            if (count < 0) {
                if (offset == 0) return null
                throw EOFException("Avkortet kunstarkiv")
            }
            offset += count
        }
        return buffer
    }

    private fun readExact(input: InputStream, length: Int): ByteArray =
        readExactOrNull(input, length) ?: throw EOFException("Avkortet kunstarkiv")

    private fun openExternal(uri: Uri) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }

    private inner class AndroidBridge {
        @JavascriptInterface
        fun share(title: String, text: String, url: String) {
            runOnUiThread {
                val body = listOf(text, url).filter { it.isNotBlank() }.joinToString("\n\n")
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                startActivity(Intent.createChooser(intent, "Del verket"))
            }
        }
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface("Android")
        webView.destroy()
        super.onDestroy()
    }

    private companion object {
        const val TAR_BLOCK = 512
        const val MAX_ENTRY_BYTES = 16L * 1024L * 1024L
    }
}
