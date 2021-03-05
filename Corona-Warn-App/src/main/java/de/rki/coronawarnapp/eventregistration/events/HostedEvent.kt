package de.rki.coronawarnapp.eventregistration.events

import org.joda.time.Instant

/**
 * A verified event that the host has been registered.
 * If you are a host, this is the event you created successfully.
 */
interface HostedEvent {

    val guid: String
    val description: String
    val location: String
    val startTime: Instant?
    val endTime: Instant?
    val defaultCheckInLengthInMinutes: Int?
    val signature: String
}
