package com.vahitkeskin.commitcounter.presentation.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.vahitkeskin.commitcounter.presentation.CommitCounterBundle
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout

class VerificationDialog(
    project: Project,
    private val userCode: String,
    private val verificationUri: String,
    private val onCancel: () -> Unit
) : DialogWrapper(project) {

    init {
        title = CommitCounterBundle.message("dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        val message = CommitCounterBundle.message("dialog.body", verificationUri, userCode)
        panel.add(JLabel(message), BorderLayout.CENTER)
        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(cancelAction)
    }

    override fun doCancelAction() {
        onCancel()
        super.doCancelAction()
    }
}
