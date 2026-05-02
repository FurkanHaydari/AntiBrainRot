package com.brainfocus.numberdetective.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

object PlayReviewManager {
    fun requestReview(context: Context) {
        val activity = context as? Activity ?: return
        val reviewManager = ReviewManagerFactory.create(context)
        
        val requestFlow = reviewManager.requestReviewFlow()
        requestFlow.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                }
            }
        }
    }
}
