package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class JioPanalLaunchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun fullLaunchLoginAndDashboardSucceeds() {
        composeRule.waitUntil(timeoutMillis = 8000L) {
            composeRule.onAllNodesWithTag("gmail_input").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("gmail_input").performScrollTo().performTextInput("test.partner@gmail.com")
        composeRule.onNodeWithTag("send_otp_button").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 5000L) {
            composeRule.onAllNodesWithTag("autofill_otp_button").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("autofill_otp_button").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("verify_otp_button").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 5000L) {
            composeRule.onAllNodesWithTag("quick_deposit_btn").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("quick_deposit_btn").performScrollTo().assertIsDisplayed().performClick()

        composeRule.waitUntil(timeoutMillis = 5000L) {
            composeRule.onAllNodesWithTag("utr_input").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("utr_input").performScrollTo().assertIsDisplayed()
    }
}
