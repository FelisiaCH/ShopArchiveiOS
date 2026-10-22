@file:OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)

package xyz.felismp.shoparchive.app.client

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.posix.O_CREAT
import platform.posix.O_TRUNC
import platform.posix.O_WRONLY
import platform.posix.close
import platform.posix.fsync
import platform.posix.open
import platform.posix.rename
import platform.posix.write

/** The platform's store: `<Application Support>/outbox`, where the app's other files live and which is not cleared like caches. */
fun createOutboxStore(): OutboxStore {
    val support = NSFileManager.defaultManager.URLsForDirectory(NSApplicationSupportDirectory, NSUserDomainMask).first() as NSURL
    return FileOutboxStore(support.path + "/outbox")
}

/** The JVM's FileOutboxStore on POSIX files: the same names, and a write reaches the disk (fsync) before `rename` replaces the old file. */
internal class FileOutboxStore(private val dir: String) : OutboxStore {
    private val files = NSFileManager.defaultManager
    private fun hash(text: String) = sha256(text.encodeToByteArray()).toHexUpper().lowercase()
    private fun prefix(serverId: String) = hash(serverId).take(16) + "-"
    private fun pathOf(serverId: String, user: String) = "$dir/" + prefix(serverId) + hash(serverId + "\n" + user).take(32) + ".json"

    private fun delete(path: String) {
        if (files.fileExistsAtPath(path) && !files.removeItemAtPath(path, null)) throw IllegalStateException("cannot delete $path")
    }

    override fun read(serverId: String, user: String): String? {
        val path = pathOf(serverId, user)
        if (!files.fileExistsAtPath(path)) return null
        return (NSData.dataWithContentsOfFile(path) ?: throw IllegalStateException("cannot read $path")).toByteArray().decodeToString()
    }

    override fun write(serverId: String, user: String, text: String?) {
        val path = pathOf(serverId, user)
        if (text == null) return delete(path)
        if (!files.createDirectoryAtPath(dir, true, null, null)) throw IllegalStateException("cannot create $dir")
        val tmp = "$path.tmp"
        val fd = open(tmp, O_WRONLY or O_CREAT or O_TRUNC, 420) // 0644
        check(fd >= 0) { "cannot open $tmp" }
        try {
            val bytes = text.encodeToByteArray()
            var done = 0
            while (done < bytes.size) {
                val n = bytes.usePinned { write(fd, it.addressOf(done), (bytes.size - done).convert()) }
                check(n > 0) { "cannot write $tmp" }
                done += n.toInt()
            }
            check(fsync(fd) == 0) { "cannot sync $tmp" }
        } finally {
            close(fd)
        }
        check(rename(tmp, path) == 0) { "cannot replace $path" }
    }

    override fun removeServer(serverId: String) {
        val prefix = prefix(serverId)
        files.contentsOfDirectoryAtPath(dir, null)?.forEach { name -> if ((name as String).startsWith(prefix)) delete("$dir/$name") }
    }
}
