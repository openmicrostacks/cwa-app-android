package de.rki.coronawarnapp.submission.server

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload

import de.rki.coronawarnapp.util.PaddingTool.requestPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class SubmissionServer @Inject constructor(
    private val submissionApi: Lazy<SubmissionApiV1>
) {

    private val api: SubmissionApiV1
        get() = submissionApi.get()

    data class SubmissionData(
        val authCode: String,
        val keyList: List<TemporaryExposureKey>,
        val consentToFederation: Boolean,
        val visitedCountries: List<String>
    )

    suspend fun submitKeysToServer(
        data: SubmissionData
    ) = withContext(Dispatchers.IO) {
        Timber.d("submitKeysToServer()")
        val authCode = data.authCode
        val keyList = data.keyList
        Timber.d("Writing ${keyList.size} Keys to the Submission Payload.")

        val randomAdditions = 0 // prepare for random addition of keys
        val fakeKeyCount = max(
            MIN_KEY_COUNT_FOR_SUBMISSION + randomAdditions - keyList.size,
            0
        )
        val fakeKeyPadding = requestPadding(FAKE_KEY_SIZE * fakeKeyCount)

        val submissionPayload = SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .setRequestPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .setConsentToFederation(data.consentToFederation)
            .addAllVisitedCountries(data.visitedCountries)
            .build()

        api.submitKeys(
            authCode = authCode,
            fake = "0",
            headerPadding = EMPTY_HEADER,
            requestBody = submissionPayload
        )
    }

    suspend fun submitKeysToServerFake() = withContext(Dispatchers.IO) {
        Timber.d("submitKeysToServerFake()")

        val randomAdditions = 0 // prepare for random addition of keys
        val fakeKeyCount = MIN_KEY_COUNT_FOR_SUBMISSION + randomAdditions

        val fakeKeyPadding = requestPadding(FAKE_KEY_SIZE * fakeKeyCount)

        val submissionPayload = SubmissionPayload.newBuilder()
            .setRequestPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .build()

        api.submitKeys(
            authCode = EMPTY_HEADER,
            fake = "1",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_SUBMISSION_FAKE),
            requestBody = submissionPayload
        )
    }

    companion object {
        const val EMPTY_HEADER = ""
        const val PADDING_LENGTH_HEADER_SUBMISSION_FAKE = 36
        const val MIN_KEY_COUNT_FOR_SUBMISSION = 14
        const val FAKE_KEY_SIZE = (1 * 16 /* key data*/) + (3 * 4 /* 3x int32*/)
    }
}
