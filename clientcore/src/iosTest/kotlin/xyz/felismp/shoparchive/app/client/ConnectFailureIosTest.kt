@file:OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)

package xyz.felismp.shoparchive.app.client

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import platform.Foundation.NSError
import platform.Foundation.NSURLErrorBadServerResponse
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Which Darwin errors let a call go on to the server's next address, and which may have reached the server already. */
class ConnectFailureIosTest {
    private fun darwin(code: Long) = DarwinHttpRequestException(NSError.errorWithDomain(NSURLErrorDomain, code, null))

    @Test
    fun aConnectionThatWasNeverMadeTriesTheNextAddress() {
        for (code in listOf(NSURLErrorCannotFindHost, NSURLErrorCannotConnectToHost, NSURLErrorNotConnectedToInternet, NSURLErrorTimedOut)) {
            assertTrue(isConnectFailure(darwin(code)), "code $code")
        }
        assertTrue(isConnectFailure(Exception("wrapped", darwin(NSURLErrorCannotConnectToHost))))
    }

    @Test
    fun aRequestThatMayHaveReachedTheServerIsNotSentAgain() {
        assertFalse(isConnectFailure(darwin(NSURLErrorNetworkConnectionLost)))
        assertFalse(isConnectFailure(darwin(NSURLErrorBadServerResponse)))
    }
}
