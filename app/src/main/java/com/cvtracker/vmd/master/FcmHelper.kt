package com.cvtracker.vmd.master

import com.cvtracker.vmd.data.DisplayItem
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import timber.log.Timber

object FcmHelper {

    fun subscribeToCenter(center: DisplayItem.Center) {
        subscribeToTopic(topicForCenter(center))
    }

    fun unsubscribeFromCenter(center: DisplayItem.Center) {
        unsubscribeFromTopic(topicForCenter(center))
    }

    fun unsubscribeFromDepartmentAndCenterId(department: String, centerId: String?) {
        unsubscribeFromTopic(topicWithDepartmentAndCenterId(department, centerId))
    }

    private fun topicForCenter(center: DisplayItem.Center) = topicWithDepartmentAndCenterId(center.department, center.id)

    /** Use for notification action **/
    private fun topicWithDepartmentAndCenterId(department: String, centerId: String?) = "department_${department}_center_${centerId}"

    private fun subscribeToTopic(topic: String) {
        Firebase.messaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                Timber.d("subscribeToTopic [$topic] : ${if (task.isSuccessful) "success" else "fail"}")
            }
    }

    private fun unsubscribeFromTopic(topic: String) {
        Firebase.messaging.unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                Timber.d("unsubscribeFromTopic [$topic] : ${if (task.isSuccessful) "success" else "fail"}")
            }
    }
}