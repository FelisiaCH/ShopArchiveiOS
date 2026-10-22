package xyz.felismp.shoparchive.app.client

import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileOutboxStoreIosTest {
    private val dir = NSTemporaryDirectory() + "outbox-" + NSUUID().UUIDString
    private val store = FileOutboxStore(dir)

    private fun withStore(test: () -> Unit) = try {
        test()
    } finally {
        NSFileManager.defaultManager.removeItemAtPath(dir, null)
    }

    @Test fun nothingWrittenReadsAsNull() = withStore {
        assertNull(store.read("s1", "alice"))
    }

    @Test fun aWrittenTextComesBackAndTheLastWriteWins() = withStore {
        store.write("s1", "alice", "one ฿")
        assertEquals("one ฿", FileOutboxStore(dir).read("s1", "alice"))
        store.write("s1", "alice", "two")
        assertEquals("two", store.read("s1", "alice"))
    }

    @Test fun writingNullRemovesItAndIsRepeatable() = withStore {
        store.write("s1", "alice", "one")
        store.write("s1", "alice", null)
        assertNull(store.read("s1", "alice"))
        store.write("s1", "alice", null)
    }

    @Test fun twoUsersAreKeptApart() = withStore {
        store.write("s1", "alice", "a")
        store.write("s1", "bob", "b")
        assertEquals("a", store.read("s1", "alice"))
        assertEquals("b", store.read("s1", "bob"))
    }

    @Test fun removeServerRemovesOnlyThatServersFiles() = withStore {
        store.write("s1", "alice", "a")
        store.write("s1", "bob", "b")
        store.write("s2", "alice", "c")
        store.removeServer("s1")
        assertNull(store.read("s1", "alice"))
        assertNull(store.read("s1", "bob"))
        assertEquals("c", store.read("s2", "alice"))
    }
}
