package com.vahitkeskin.commitcounter.domain.usecase

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.vahitkeskin.commitcounter.data.repository.GitHubRepository
import com.vahitkeskin.commitcounter.data.repository.PasswordSafeStorage
import com.vahitkeskin.commitcounter.domain.model.CommitState
import com.vahitkeskin.commitcounter.presentation.widget.VerificationDialog
import java.awt.datatransfer.StringSelection
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class CommitCounterService {
    var state: CommitState = CommitState.LoggedOut
        private set

    private var schedulerFuture: ScheduledFuture<*>? = null

    init {
        val savedToken = PasswordSafeStorage.getToken()
        val savedUsername = PasswordSafeStorage.getUsername()
        if (savedToken != null && savedUsername != null) {
            state = CommitState.Fetching
            startScheduler()
        } else {
            state = CommitState.LoggedOut
        }
    }

    fun updateState(newState: CommitState) {
        state = newState
        notifyWidgets()
    }

    private fun notifyWidgets() {
        ApplicationManager.getApplication().invokeLater {
            for (project in ProjectManager.getInstance().openProjects) {
                WindowManager.getInstance().getStatusBar(project)?.updateWidget("CommitCounterWidget")
            }
        }
    }

    fun startScheduler() {
        stopScheduler()
        val executor = AppExecutorUtil.getAppScheduledExecutorService()
        schedulerFuture = executor.scheduleWithFixedDelay({
            refreshCommits()
        }, 0, 15, TimeUnit.MINUTES)
    }

    fun stopScheduler() {
        schedulerFuture?.cancel(false)
        schedulerFuture = null
    }

    fun refreshCommits() {
        val token = PasswordSafeStorage.getToken()
        val username = PasswordSafeStorage.getUsername()
        if (token == null || username == null) {
            updateState(CommitState.LoggedOut)
            stopScheduler()
            return
        }

        updateState(CommitState.Fetching)

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val commits = GitHubRepository.fetchCommitsToday(token, username)
                if (commits != null) {
                    updateState(CommitState.LoggedIn(username, commits))
                } else {
                    updateState(CommitState.Error("Failed to fetch commits"))
                }
            } catch (e: Exception) {
                updateState(CommitState.Error("Error: ${e.message}"))
            }
        }
    }

    fun manualRefresh() {
        startScheduler()
    }

    fun logout() {
        stopScheduler()
        PasswordSafeStorage.clearAll()
        updateState(CommitState.LoggedOut)
    }

    fun loginSuccess(token: String, username: String) {
        PasswordSafeStorage.saveToken(token)
        PasswordSafeStorage.saveUsername(username)
        updateState(CommitState.LoggedIn(username, 0))
        startScheduler()
    }

    fun startLoginFlow(project: Project) {
        updateState(CommitState.Fetching)
        ApplicationManager.getApplication().executeOnPooledThread {
            val response = GitHubRepository.requestDeviceCode()
            if (response == null) {
                updateState(CommitState.Error("Failed to request device code"))
                return@executeOnPooledThread
            }

            // Copy user code to clipboard
            val selection = StringSelection(response.userCode)
            CopyPasteManager.getInstance().setContents(selection)

            // Open browser
            BrowserUtil.browse(response.verificationUri)

            // Show dialog
            ApplicationManager.getApplication().invokeLater {
                var isPolling = true
                val dialog = VerificationDialog(project, response.userCode, response.verificationUri) {
                    isPolling = false
                    updateState(CommitState.LoggedOut)
                }

                // Start polling
                ApplicationManager.getApplication().executeOnPooledThread {
                    val intervalMs = response.interval * 1000L
                    val expiresAt = System.currentTimeMillis() + (response.expiresIn * 1000L)
                    while (isPolling && dialog.isShowing && System.currentTimeMillis() < expiresAt) {
                        try {
                            Thread.sleep(intervalMs)
                        } catch (e: InterruptedException) {
                            break
                        }

                        if (!isPolling || !dialog.isShowing) break

                        val pollResult = GitHubRepository.pollAccessToken(response.deviceCode)
                        if (pollResult != null) {
                            if (pollResult.accessToken != null) {
                                val username = GitHubRepository.fetchUsername(pollResult.accessToken)
                                if (username != null) {
                                    ApplicationManager.getApplication().invokeLater {
                                        dialog.close(com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE)
                                    }
                                    loginSuccess(pollResult.accessToken, username)
                                } else {
                                    ApplicationManager.getApplication().invokeLater {
                                        dialog.close(com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE)
                                    }
                                    updateState(CommitState.Error("Failed to fetch username"))
                                }
                                break
                            } else if (pollResult.error != null) {
                                if (pollResult.error == "authorization_pending") {
                                    // Keep polling
                                } else {
                                    ApplicationManager.getApplication().invokeLater {
                                        dialog.close(com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE)
                                    }
                                    updateState(CommitState.Error("Auth error: ${pollResult.error}"))
                                    break
                                }
                            }
                        }
                    }

                    if (isPolling && dialog.isShowing) {
                        ApplicationManager.getApplication().invokeLater {
                            dialog.close(com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE)
                        }
                        updateState(CommitState.Error("Authentication timed out"))
                    }
                }

                dialog.show()
            }
        }
    }

    companion object {
        fun getInstance(): CommitCounterService = service()
    }
}
