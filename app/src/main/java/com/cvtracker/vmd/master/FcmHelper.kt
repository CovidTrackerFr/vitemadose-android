package com.cvtracker.vmd.master

import com.cvtracker.vmd.data.DisplayItem
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import timber.log.Timber

object FcmHelper {

    const val CHRONODOSE_SUFFIX = "_chronodoses"

    fun isTopicChronodose(topic: String) = topic.endsWith(CHRONODOSE_SUFFIX, true)

    fun subscribeToCenter(center: DisplayItem.Center, chronodose: Boolean) {
        subscribeToTopic(topicForCenter(center, chronodose))
    }

    fun unsubscribeFromCenter(center: DisplayItem.Center, chronodose: Boolean) {
        unsubscribeFromTopic(topicForCenter(center, chronodose))
    }

    fun unsubscribeFromDepartmentAndCenterId(department: String, centerId: String?, chronodose: Boolean) {
        unsubscribeFromTopic(topicWithDepartmentAndCenterId(department, centerId, chronodose))
    }

    private fun topicForCenter(center: DisplayItem.Center, chronodose: Boolean) =
            topicWithDepartmentAndCenterId(center.department, center.id, chronodose)

    /** Use for notification action **/
    private fun topicWithDepartmentAndCenterId(department: String, centerId: String?, chronodose: Boolean) =
            "department_${department}_center_${centerId}${if (chronodose) CHRONODOSE_SUFFIX else ""}"

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