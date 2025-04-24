package com.example.newsexplorer.ui.screens

// Removed BookmarkButton import - logic moved to TopAppBar actions
// import com.example.newsexplorer.ui.components.BookmarkButton
// **** CORRECT ViewModel Import ****
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.viewmodel.ArticleDetailViewModel
import com.example.newsexplorer.util.rememberHtmlAnnotatedString
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
// Add RequiresApi back if loadArticle/toggleBookmark -> repo methods need it
@RequiresApi(Build.VERSION_CODES.O)
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

    // Load article when articleId changes
    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId) // Call function on correct ViewModel
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
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val currentArticle = articleState // Capture state for lambdas
                    if (currentArticle != null) {
                        // Share Action
                        IconButton(onClick = { shareArticle(context, currentArticle) }) {
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
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            articleState != null -> {
                val article = articleState!! // Use non-nullable article inside this block
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    article.urlToImage?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Article image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp) // Limit image height
                        )
                    } ?: Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // ... Text elements using non-nullable 'article' ...
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = article.sourceName,
                            style = MaterialTheme.typography.titleSmall, // Use smaller title for source
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(article.publishedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = article.title, // Use non-nullable article
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        article.author?.let { author ->
                        Text(
                                text = "By $author",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // REMOVED internal bookmark button - moved to TopAppBar actions

                        // Content
                        val displayContent = when {
                            article.content.isNotBlank() && article.content != article.description ->
                                "${article.description}\n\n${article.content}"
                            article.description.isNotBlank() -> article.description
                            article.content.isNotBlank() -> article.content
                            else -> "No content available."
                        }
                        Text(
                            text = displayContent,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // "Read more" link
                        Text(
                            text = "Read the full article at the source",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                openInBrowser(context, article.url) // Use helper
                            }
                        )
                    } // End Content Padding Column
                } // End Main Scrollable Column
            }
            else -> { // Article is null and not loading
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


// --- Helper Functions (keep these in ArticleDetailScreen.kt or move to a util file) ---

fun shareArticle(context: Context, article: Article?) { // Accept nullable Article
    if (article == null) return
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out this article: ${article.title}\n${article.url}")
        putExtra(Intent.EXTRA_SUBJECT, "News Article: ${article.title}")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Article via")
    try {
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Log.e("ArticleDetailScreen", "Failed to start share intent", e)
        // Optionally show a Toast message
    }
}

fun openInBrowser(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("ArticleDetailScreen", "Failed to open URL in browser", e)
        // Optionally show a Toast message
    }
}

// Add other helper functions like addToCalendar if needed