package com.vahitkeskin.commitcounter.presentation.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
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
        title = "GitHub Authentication"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        val message = """
            <html>
            <body style='width: 320px; font-family: sans-serif; padding: 5px;'>
            <h3>GitHub Authentication Required</h3>
            GitHub authentication has been initiated. A browser window should have opened automatically.<br><br>
            If not, go to:<br>
            <b>$verificationUri</b><br><br>
            Enter the following user code (which has been copied to your clipboard):<br>
            <h1 style='text-align: center; color: #3574F0; margin: 10px 0;'>$userCode</h1>
            This dialog will close automatically once authentication is completed.
            </body>
            </html>
        """.trimIndent()
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
