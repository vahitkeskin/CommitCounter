package com.vahitkeskin.commitcounter.domain.model

sealed class CommitState {
    object LoggedOut : CommitState()
    object Fetching : CommitState()
    data class LoggedIn(val username: String, val commitsToday: Int) : CommitState()
    data class Error(val message: String) : CommitState()
}
