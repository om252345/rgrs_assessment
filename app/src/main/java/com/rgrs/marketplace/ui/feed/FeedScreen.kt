package com.rgrs.marketplace.ui.feed

import android.icu.text.NumberFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.rgrs.marketplace.R
import com.rgrs.marketplace.domain.Listing
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(onCreateClick: () -> Unit) {
    val viewModel: FeedViewModel = hiltViewModel()

    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle(initialValue = true)
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle(initialValue = false)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadListings()
    }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            snackbarHostState.showSnackbar(
                message = "Device is offline. Changes will sync when online.",
                duration = SnackbarDuration.Indefinite
            )
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_listing)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (isSyncing) {
                        Box(
                            modifier = Modifier.padding(start = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        FeedContent(viewModel, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun FeedContent(viewModel: FeedViewModel, modifier: Modifier) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState.value) {
        is UiState.Loading -> LoadingScreen(modifier)
        is UiState.Success -> SuccessScreen(
            modifier,
            state.listings,
            onFavoriteClick = viewModel::toggleFavorite
        )

        is UiState.Error -> ErrorScreen(modifier, state.message)
    }
}

@Composable
fun LoadingScreen(modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        CircularProgressIndicator()
    }
}

@Composable
fun SuccessScreen(
    modifier: Modifier,
    listings: List<Listing>,
    onFavoriteClick: (Long) -> Unit = {}
) {
    LazyColumn(modifier = modifier) {
        items(listings, key = { it.id }) { listing ->
            ListingItem(listing, onFavoriteClick = onFavoriteClick)
        }
    }
}

@Composable
fun ErrorScreen(modifier: Modifier, message: String) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Error loading listings: $message")
    }
}

@Composable
fun ListingItem(listing: Listing, onFavoriteClick: (Long) -> Unit = {}) {
    /* dummy imageUrl is used as a photos storage backend, but it does not load image, will fallback to
       local image when photos url contains https://api.marketplace.rgrs.com/photos
    * */
    val imageModel = listing.imageUrl?.takeIf {
            it.isNotBlank() && it.contains("https://picsum.photos/") }
            ?: listing.localImagePath?.takeIf { it.isNotBlank()
        }
    val formattedPrice = remember(listing.price) {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(listing.price)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 40.dp)
                ) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            IconButton(
                onClick = { onFavoriteClick(listing.id) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (listing.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.favorite),
                    tint = if (listing.isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
