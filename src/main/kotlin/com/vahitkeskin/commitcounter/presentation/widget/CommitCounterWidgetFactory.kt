package com.vahitkeskin.commitcounter.presentation.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class CommitCounterWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "CommitCounterWidget"

    override fun getDisplayName(): String = "GitHub Commit Counter"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = CommitCounterWidget(project)

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}
