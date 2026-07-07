package com.vahitkeskin.commitcounter.presentation.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.util.Consumer
import com.vahitkeskin.commitcounter.domain.model.CommitState
import com.vahitkeskin.commitcounter.domain.usecase.CommitCounterService
import java.awt.event.MouseEvent

class CommitCounterWidget(private val project: Project) : StatusBarWidget, TextPresentation {
    private var statusBar: StatusBar? = null
    private val service = CommitCounterService.getInstance()

    override fun ID(): String = "CommitCounterWidget"

    override fun getPresentation(): StatusBarWidget.WidgetPresentation? = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        statusBar = null
    }

    override fun getTooltipText(): String {
        return when (val s = service.state) {
            is CommitState.LoggedIn -> "GitHub: Logged in as ${s.username}. Click for options."
            is CommitState.LoggedOut -> "GitHub: Not logged in. Click to login."
            is CommitState.Fetching -> "GitHub: Synchronizing commits..."
            is CommitState.Error -> "GitHub Error: ${s.message}. Click to retry."
        }
    }

    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return Consumer { event ->
            val currentState = service.state
            if (currentState is CommitState.LoggedOut || currentState is CommitState.Error) {
                service.startLoginFlow(project)
            } else {
                showWidgetMenu(event)
            }
        }
    }

    private fun showWidgetMenu(event: MouseEvent) {
        val options = listOf("Yenile (Refresh)", "Çıkış Yap (Logout)", "İptal")
        val popup = JBPopupFactory.getInstance().createListPopup(
            object : BaseListPopupStep<String>("GitHub Commit Counter", options) {
                override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                    if (finalChoice && selectedValue != null) {
                        when (selectedValue) {
                            "Yenile (Refresh)" -> service.manualRefresh()
                            "Çıkış Yap (Logout)" -> service.logout()
                        }
                    }
                    return PopupStep.FINAL_CHOICE
                }
            }
        )
        popup.showUnderneathof(event.component)
    }

    override fun getText(): String {
        return when (val s = service.state) {
            is CommitState.LoggedOut -> "GitHub: Click to Login"
            is CommitState.Fetching -> "GitHub: Fetching..."
            is CommitState.LoggedIn -> "Commits Today: ${s.commitsToday}"
            is CommitState.Error -> "GitHub: Error"
        }
    }

    override fun getAlignment(): Float = 0.5f
}
