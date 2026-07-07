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
import com.vahitkeskin.commitcounter.presentation.CommitCounterBundle
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
            is CommitState.LoggedIn -> CommitCounterBundle.message("widget.tooltip.loggedin", s.username)
            is CommitState.LoggedOut -> CommitCounterBundle.message("widget.tooltip.login")
            is CommitState.Fetching -> CommitCounterBundle.message("widget.tooltip.fetching")
            is CommitState.Error -> CommitCounterBundle.message("widget.tooltip.error", s.message)
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
        val refreshLabel = CommitCounterBundle.message("menu.refresh")
        val logoutLabel = CommitCounterBundle.message("menu.logout")
        val cancelLabel = "Cancel"
        val options = listOf(refreshLabel, logoutLabel, cancelLabel)
        val popup = JBPopupFactory.getInstance().createListPopup(
            object : BaseListPopupStep<String>("GitHub Commit Counter", options) {
                override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                    if (finalChoice && selectedValue != null) {
                        when (selectedValue) {
                            refreshLabel -> service.manualRefresh()
                            logoutLabel -> service.logout()
                        }
                    }
                    return PopupStep.FINAL_CHOICE
                }
            }
        )
        popup.showUnderneathOf(event.component)
    }

    override fun getText(): String {
        return when (val s = service.state) {
            is CommitState.LoggedOut -> CommitCounterBundle.message("widget.click.to.login")
            is CommitState.Fetching -> CommitCounterBundle.message("widget.fetching")
            is CommitState.LoggedIn -> CommitCounterBundle.message("widget.commits.today", s.commitsToday)
            is CommitState.Error -> CommitCounterBundle.message("widget.error")
        }
    }

    override fun getAlignment(): Float = 0.5f
}
