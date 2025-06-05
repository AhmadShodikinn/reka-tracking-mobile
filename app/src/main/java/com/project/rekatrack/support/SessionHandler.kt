package com.project.rekatrack.support

object SessionHandler {
    var onSessionExpired: (() -> Unit)? = null

    fun triggerSessionExpired() {
        onSessionExpired?.invoke()
    }
}