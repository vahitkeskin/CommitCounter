package com.vahitkeskin.commitcounter.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object GitHubRepository {
    private val logger = com.intellij.openapi.diagnostic.Logger.getInstance(GitHubRepository::class.java)

    // Note: To use the plugin, register a GitHub OAuth App with Device Flow enabled and put the Client ID here.
    private const val CLIENT_ID = "Ov23li8Z1n2wZ1Gz1e7G" 

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val gson = Gson()

    data class DeviceCodeResponse(
        val deviceCode: String,
        val userCode: String,
        val verificationUri: String,
        val interval: Int,
        val expiresIn: Int
    )

    data class PollResult(
        val accessToken: String?,
        val error: String?
    )

    fun requestDeviceCode(): DeviceCodeResponse? {
        val requestBody = gson.toJson(mapOf(
            "client_id" to CLIENT_ID,
            "scope" to "repo,read:user"
        ))

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://github.com/login/device/code"))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "CommitCounter-IntelliJ-Plugin")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val json = gson.fromJson(response.body(), JsonObject::class.java)
                return DeviceCodeResponse(
                    deviceCode = json.get("device_code").asString,
                    userCode = json.get("user_code").asString,
                    verificationUri = json.get("verification_uri").asString,
                    interval = json.get("interval")?.asInt ?: 5,
                    expiresIn = json.get("expires_in").asInt
                )
            } else {
                logger.warn("GitHub requestDeviceCode failed: status ${response.statusCode()}, body: ${response.body()}")
            }
        } catch (e: Exception) {
            logger.error("Error requesting device code", e)
        }
        return null
    }

    fun pollAccessToken(deviceCode: String): PollResult? {
        val requestBody = gson.toJson(mapOf(
            "client_id" to CLIENT_ID,
            "device_code" to deviceCode,
            "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"
        ))

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://github.com/login/oauth/access_token"))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "CommitCounter-IntelliJ-Plugin")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val json = gson.fromJson(response.body(), JsonObject::class.java)
                val accessToken = json.get("access_token")?.asString
                val error = json.get("error")?.asString
                return PollResult(accessToken, error)
            } else {
                logger.warn("GitHub pollAccessToken failed: status ${response.statusCode()}, body: ${response.body()}")
            }
        } catch (e: Exception) {
            logger.error("Error polling access token", e)
        }
        return null
    }

    fun fetchUsername(token: String): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/user"))
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "CommitCounter-IntelliJ-Plugin")
            .GET()
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val json = gson.fromJson(response.body(), JsonObject::class.java)
                return json.get("login").asString
            } else {
                logger.warn("GitHub fetchUsername failed: status ${response.statusCode()}, body: ${response.body()}")
            }
        } catch (e: Exception) {
            logger.error("Error fetching username", e)
        }
        return null
    }

    fun fetchCommitsToday(token: String, username: String): Int? {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val query = "author:$username committer-date:$todayStr"
        val encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8)
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/search/commits?q=$encodedQuery"))
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github.cloak-preview+json")
            .header("User-Agent", "CommitCounter-IntelliJ-Plugin")
            .GET()
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val json = gson.fromJson(response.body(), JsonObject::class.java)
                return json.get("total_count").asInt
            } else {
                logger.warn("GitHub fetchCommitsToday failed: status ${response.statusCode()}, body: ${response.body()}")
            }
        } catch (e: Exception) {
            logger.error("Error fetching daily commits", e)
        }
        return null
    }
}
