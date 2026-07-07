package com.vahitkeskin.commitcounter.data.repository

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent

object PasswordSafeStorage {
    private val tokenAttributes = CredentialAttributes(
        generateServiceName("CommitCounter", "GitHubToken")
    )
    private const val USERNAME_KEY = "com.vahitkeskin.commitcounter.username"

    fun saveToken(token: String) {
        PasswordSafe.instance.setPassword(tokenAttributes, token)
    }

    fun getToken(): String? {
        return PasswordSafe.instance.getPassword(tokenAttributes)
    }

    fun removeToken() {
        PasswordSafe.instance.setPassword(tokenAttributes, null)
    }

    fun saveUsername(username: String) {
        PropertiesComponent.getInstance().setValue(USERNAME_KEY, username)
    }

    fun getUsername(): String? {
        return PropertiesComponent.getInstance().getValue(USERNAME_KEY)
    }

    fun removeUsername() {
        PropertiesComponent.getInstance().unsetValue(USERNAME_KEY)
    }

    fun clearAll() {
        removeToken()
        removeUsername()
    }
}
