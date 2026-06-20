package com.example.sy43___ae_app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.sy43___ae_app.ui.screens.NewsScreen
import org.junit.Rule
import org.junit.Test

class NewsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNewsScreenEmptyState() {
        composeTestRule.setContent {
            NewsScreen(db = null)
        }

        // Initially it should show loading or empty if db is null
        composeTestRule.onNodeWithText("Chargement des nouvelles...").assertIsDisplayed()
    }
}
