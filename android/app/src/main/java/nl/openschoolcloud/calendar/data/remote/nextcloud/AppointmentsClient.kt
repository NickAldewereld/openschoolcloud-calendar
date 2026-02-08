/*
 * OpenSchoolCloud Calendar
 * Copyright (C) 2025 OpenSchoolCloud / Aldewereld Consultancy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package nl.openschoolcloud.calendar.data.remote.nextcloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for Nextcloud Calendar Appointments OCS API.
 *
 * Nextcloud Calendar v3+ supports "Appointments" - a Calendly-style booking system.
 * Teachers create appointment configs, parents book via public link.
 *
 * API: GET /ocs/v2.php/apps/calendar/api/v1/appointment_configs
 */
@Singleton
class AppointmentsClient @Inject constructor(
    private val httpClient: OkHttpClient
) {
    companion object {
        private const val OCS_APPOINTMENTS_PATH =
            "/ocs/v2.php/apps/calendar/api/v1/appointment_configs"
    }

    data class AppointmentConfig(
        val id: Long,
        val name: String,
        val description: String?,
        val duration: Int,
        val token: String,
        val targetCalendarUri: String?,
        val visibility: String // "PUBLIC" or "PRIVATE"
    )

    /**
     * Fetch all appointment configurations for the authenticated user.
     *
     * @param serverUrl Base server URL (e.g., https://cloud.school.nl)
     * @param username Username
     * @param password App password
     * @return List of appointment configs, or empty if not supported
     */
    suspend fun getAppointmentConfigs(
        serverUrl: String,
        username: String,
        password: String
    ): Result<List<AppointmentConfig>> = withContext(Dispatchers.IO) {
        val url = "${serverUrl.trimEnd('/')}$OCS_APPOINTMENTS_PATH"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(username, password))
            .header("OCS-APIRequest", "true")
            .header("Accept", "application/json")
            .get()
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val body = response.body?.string() ?: "{}"
                        val configs = parseAppointmentConfigs(body)
                        Result.success(configs)
                    }
                    response.code == 404 -> {
                        // Appointments app not installed or not supported
                        Result.success(emptyList())
                    }
                    response.code == 401 || response.code == 403 -> {
                        Result.failure(IOException("Authenticatie mislukt"))
                    }
                    else -> {
                        Result.failure(
                            IOException("Server fout: ${response.code} ${response.message}")
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Result.failure(IOException("Netwerkfout: ${e.message}", e))
        }
    }

    private fun parseAppointmentConfigs(json: String): List<AppointmentConfig> {
        return try {
            val root = JSONObject(json)
            val ocs = root.getJSONObject("ocs")
            val data = ocs.optJSONArray("data") ?: return emptyList()

            (0 until data.length()).map { i ->
                val obj = data.getJSONObject(i)
                AppointmentConfig(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    description = obj.optString("description", null),
                    duration = obj.optInt("length", 30),
                    token = obj.getString("token"),
                    targetCalendarUri = obj.optString("targetCalendarUri", null),
                    visibility = obj.optString("visibility", "PUBLIC")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
