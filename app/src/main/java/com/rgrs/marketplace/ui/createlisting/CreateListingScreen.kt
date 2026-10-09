package com.rgrs.marketplace.ui.createlisting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun CreateListing(onBackClicked: () -> Unit) {
    val viewModel: CreateListingViewModel = hiltViewModel()
    CreateListingContent(viewModel, onBackClicked)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingContent(
    viewModel: CreateListingViewModel,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    val formState = viewModel.formFields.collectAsStateWithLifecycle()

    var showPickerSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onLocalImagePathChanged(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            viewModel.onLocalImagePathChanged(tempCameraUri.toString())
        }
    }

    fun launchCamera() {
        val tempFile = File.createTempFile("camera_photo_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Create New Listing",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(24.dp))

            // Image Preview / Add Image Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showPickerSheet = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!formState.value.localImagePath.isNullOrBlank()) {
                        AsyncImage(
                            model = formState.value.localImagePath,
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Photo",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Tap to add photo",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextField(
                value = formState.value.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Title") },
                isError = !formState.value.titleError.isNullOrEmpty(),
                supportingText = {
                    if (!formState.value.titleError.isNullOrEmpty()) {
                        Text(formState.value.titleError!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            TextField(
                value = if (formState.value.price == 0.0) "" else formState.value.price.toString(),
                onValueChange = {
                    viewModel.onPriceChanged(it.toDoubleOrNull() ?: 0.0)
                },
                isError = !formState.value.priceError.isNullOrEmpty(),
                supportingText = {
                    if (!formState.value.priceError.isNullOrEmpty()) {
                        Text(formState.value.priceError!!)
                    }
                },
                singleLine = true,
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Row {
                Button(
                    enabled = formState.value.isFormValid,
                    onClick = {
                        viewModel.createListing(
                            formState.value.title,
                            formState.value.price,
                            formState.value.localImagePath
                        )
                        onBackClicked()
                    }
                ) {
                    Text("Create")
                }
                Spacer(Modifier.size(16.dp))
                Button(onClick = onBackClicked) {
                    Text("Cancel")
                }
            }
        }
    }

    // Modal Bottom Sheet for Camera / Gallery Selection (WhatsApp style)
    if (showPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPickerSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Select Photo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Camera") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera"
                        )
                    },
                    modifier = Modifier.clickable {
                        showPickerSheet = false
                        launchCamera()
                    }
                )

                ListItem(
                    headlineContent = { Text("Gallery") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery"
                        )
                    },
                    modifier = Modifier.clickable {
                        showPickerSheet = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                if (!formState.value.localImagePath.isNullOrBlank()) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Remove Photo",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Photo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.clickable {
                            showPickerSheet = false
                            viewModel.onLocalImagePathChanged(null)
                        }
                    )
                }
            }
        }
    }
}
