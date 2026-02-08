package nl.openschoolcloud.calendar.data.repository

import nl.openschoolcloud.calendar.data.local.dao.AccountDao
import nl.openschoolcloud.calendar.data.remote.auth.CredentialStorage
import nl.openschoolcloud.calendar.data.remote.nextcloud.AppointmentsClient
import nl.openschoolcloud.calendar.domain.model.BookingConfig
import nl.openschoolcloud.calendar.domain.model.BookingVisibility
import nl.openschoolcloud.calendar.domain.repository.BookingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val credentialStorage: CredentialStorage,
    private val appointmentsClient: AppointmentsClient
) : BookingRepository {

    override suspend fun getBookingConfigs(): Result<List<BookingConfig>> {
        val account = accountDao.getDefault()
            ?: return Result.failure(IllegalStateException("Geen account geconfigureerd"))

        val password = credentialStorage.getPassword(account.id)
            ?: return Result.failure(IllegalStateException("Inloggegevens niet gevonden"))

        val result = appointmentsClient.getAppointmentConfigs(
            serverUrl = account.serverUrl,
            username = account.username,
            password = password
        )

        return result.map { configs ->
            configs.map { config ->
                val bookingUrl = buildBookingUrl(account.serverUrl, config.token)
                BookingConfig(
                    id = config.id,
                    name = config.name,
                    description = config.description,
                    duration = config.duration,
                    token = config.token,
                    bookingUrl = bookingUrl,
                    calendarId = config.targetCalendarUri,
                    visibility = when (config.visibility) {
                        "PRIVATE" -> BookingVisibility.PRIVATE
                        else -> BookingVisibility.PUBLIC
                    }
                )
            }
        }
    }

    private fun buildBookingUrl(serverUrl: String, token: String): String {
        return "${serverUrl.trimEnd('/')}/index.php/apps/calendar/appointment/$token"
    }
}
