package com.example.newsexplorer.ui.screens

// Removed BookmarkButton import if logic moved to TopAppBar
// import com.example.newsexplorer.ui.components.BookmarkButton
// **** CORRECT ViewModel Import ****
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.viewmodel.ArticleDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O) // Keep if repo methods called require it
@Composable
fun ArticleDetailScreen(
    // **** CORRECT ViewModel PARAMETER TYPE ****
    viewModel: ArticleDetailViewModel,
    articleId: String,
    onBackClick: () -> Unit
){
    // Observe state from the CORRECT ViewModel instance
    val articleState by viewModel.article.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // --- State to control WebView visibility ---
    var showWebView by remember { mutableStateOf(false) }
    val webViewUrl by remember { derivedStateOf { articleState?.url } } // Track URL efficiently

    // --- Load article when articleId changes ---
    LaunchedEffect(articleId) {
        showWebView = false // Hide WebView when navigating to a new article
        viewModel.loadArticle(articleId) // Call function on correct ViewModel
    }

    // --- Handle system back press when WebView is shown ---
    BackHandler(enabled = showWebView) {
        showWebView = false // Close WebView on back press
    }

    // Show errors
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar("Error: $it") }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        articleState?.sourceName ?: "Article",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    // Handle back press differently if WebView is showing
                    IconButton(onClick = {
                        if (showWebView) {
                            showWebView = false
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = if (showWebView) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (showWebView) "Close WebView" else "Back"
                        )
                    }
                },
                actions = {
                    val currentArticle = articleState
                    Log.d("ArticleDetailScreen", "[SHARE DEBUG] Composing actions. Article present: ${currentArticle != null}")

                    if (currentArticle != null) {
                        IconButton(
                            onClick = {
                                Log.d("ArticleDetailScreen", "Share IconButton onClick triggered.")
                                // Toast.makeText(context, "Share Clicked!", Toast.LENGTH_SHORT).show() // Remove this
                                if (currentArticle != null) {
                                    shareArticle(context, currentArticle) // UNCOMMENT THIS
                                } else {
                                    Log.w("ArticleDetailScreen", "Share clicked but article was null!") // Keep this log just in case
                                }
                            },
                            enabled = true
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Article")
                    }
                        // Bookmark Action
                        IconButton(onClick = { viewModel.toggleBookmark(currentArticle.id) }) { // Call correct VM method
                            Icon(
                                imageVector = if (currentArticle.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Toggle Bookmark",
                                tint = if (currentArticle.isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
                // Removed TopAppBarDefaults colors = ... (use default M3 colors)
            )
        }
        // FloatingActionButton removed as Share is in AppBar now
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Use the specific article state from the ViewModel
            articleState != null -> {
                val article = articleState!! // Use non-nullable 'article' inside this block
                // Use Box to potentially overlay WebView or switch content
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                    // --- Article Details Column (Visible when WebView is hidden) ---
                    // Use Modifier.alpha to hide/show instead of removing from composition
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp) // Padding at bottom
                            .alpha(if (showWebView) 0f else 1f) // Hide if WebView is shown
                    ) {
                        article.urlToImage?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Article image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                            )
                        } ?: Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = article.sourceName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(article.publishedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = article.title, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            article.author?.let { author ->
                                Text(text = "By $author", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            val displayContent = when {
                                article.content.isNotBlank() && article.content != article.description -> "${article.description}\n\n${article.content}"
                                article.description.isNotBlank() -> article.description
                                article.content.isNotBlank() -> article.content
                                else -> "No content available."
                            }
                            Text(text = displayContent, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(24.dp))

                            // --- "Read more" Link ---
                            if (!article.url.isNullOrBlank()) { // Check if URL exists
                                Text(
                                    text = "Read the full article at the source",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { showWebView = true } // Just show WebView
                                )
                            }
                            // --- End Read more Link ---

                        } // End Content Padding Column
                    } // End Main Scrollable Column

                    // --- WebView (Conditionally Displayed) ---
                    if (showWebView && !webViewUrl.isNullOrBlank()) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        // Optional: Add error handling or progress indication here
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            Log.d("WebView", "Page finished loading: $url")
                                        }
                                    }
                                }
                            },
                            update = { webView ->
                                webViewUrl?.let { url ->
                                    // Load URL only if it's different from the current one
                                    if (webView.url != url) {
                                        webView.loadUrl(url)
                                        Log.d("WebView", "Loading URL: $url")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize() // Let WebView fill the Box
                        )
                    }
                    // --- End WebView ---

                } // End Box
            }
            else -> { // Article is null and not loading (e.g., not found or error)
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (error != null) "Error loading article." else "Article not found.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } // End When
    } // End Scaffold
}


// --- Helper Functions --- (Keep these)
fun shareArticle(context: Context, article: Article?) {
    if (article == null) {
        Log.w("ShareHelper", "Attempted to share with null article.")
        Toast.makeText(context, "Cannot share article details.", Toast.LENGTH_SHORT).show()
        return
    }

    val shareText = "Check out this article: ${article.title}\n${article.url}"
    Log.d("ShareHelper", "Sharing text: $shareText") // Did you see this log?

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "News Article: ${article.title}")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Article via")
    Log.d("ShareHelper", "Created share intent chooser.") // Did you see this log?

    try {
        Log.d("ShareHelper", "Attempting to start share activity...") // Did you see this log?
        context.startActivity(shareIntent)
        Log.d("ShareHelper", "Share activity started successfully.") // Did you see THIS log?
    } catch (e: Exception) {
        // Did you see THIS log instead?
        Log.e("ShareHelper", "Failed to start share intent activity.", e)
        Toast.makeText(context, "Could not open share options. No app available?", Toast.LENGTH_LONG).show()
    }
}
fun openInBrowser(context: Context, url: String?) { /* ... */ }