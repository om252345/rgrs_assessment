package com.rgrs.marketplace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rgrs.marketplace.ui.createlisting.CreateListing
import com.rgrs.marketplace.ui.feed.FeedScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "feed") {
        composable("feed") { FeedScreen(onCreateClick = { navController.navigate("create") }) }
        composable("create") { CreateListing(onBackClicked = { navController.popBackStack() }) }
    }
}