package com.wavproductions.www.armitage

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.security.InvalidParameterException
import javax.net.ssl.SSLSocketFactory

class Metasploit : Closeable {
    private var console: Process?
    private val temp: Path = Files.createTempFile("console", "MCArmitage")
    private val fileIn: InputStream
    private var rpc: Socket? = null //will be a direct connection, so most likely a socket

    init {
        fileIn = Files.newInputStream(temp)
        console = null
    }

    fun load(
        username: String = "msf",
        password: String = "pass",
        ip: InetAddress = InetAddress.getLoopbackAddress(),
        port: Int = 55552,
        ssl: Boolean = false,
        program: String? = null,
        local: Boolean = true
    ) {
        if (port < 0 || port > 65535) {
            throw InvalidParameterException("port is invalid! Valid values are 0-65535")
        }
        var connected = false
        try {
            connected = attemptConnect(username, password, ip, port, ssl)
        } catch (ignored: Exception) {
        }
        if (!connected && local) {
            val location = program ?: locateMetasploit() ?: throw NullPointerException("Unable to locate console!")
            val builder = ProcessBuilder(location)
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE)
            builder.redirectError(ProcessBuilder.Redirect.PIPE)
            builder.redirectInput(ProcessBuilder.Redirect.PIPE)
            console = builder.start()
            //start rpc
            connected = attemptConnect(username, password, ip, port, ssl)
        }
        if (!connected) {
            throw IOException("Failed to contact the RPC server")
        }
    }

    private fun locateMetasploit(): String? {
        val builder = ProcessBuilder("which", "msfconsole") //linux lookup for now... might add windows later
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)
        builder.redirectInput(ProcessBuilder.Redirect.DISCARD)
        console = builder.start()
        return null
    }

    private fun attemptConnect(username: String, password: String, ip: InetAddress, port: Int, ssl: Boolean): Boolean {
        val socket = if (ssl) SSLSocketFactory.getDefault().createSocket(ip, port) else Socket(ip, port)
        if (socket.isConnected) {
            rpc = socket
            return true
        }
        return false
    }

    fun consoleActive(): Boolean {
        return console?.isAlive ?: false
    }

    fun startRPC(): Boolean {
        if (!consoleActive()) { //make sure console is active to init rpc
            return false
        }
        return false //rpc failed to init!
    }

    fun sendConsoleCommand(command: ByteArray, flush: Boolean = false) {
        console?.outputStream?.write(command)
        if (flush) {
            console?.outputStream?.flush()
        }
    }

    fun readConsole(): ByteArray {
        return fileIn.readAllBytes()
    }

    fun readRPC(): ByteArray {
        return rpc?.inputStream?.readAllBytes() ?: ByteArray(0)
    }

    fun sendRPCCommand(command: ByteArray, flush: Boolean = false) {
        rpc?.outputStream?.write(command)
        if (flush) {
            rpc?.outputStream?.flush()
        }
    }

    override fun close() { //destroy resources
        console?.destroyForcibly()
        rpc?.close()
        fileIn.close()
        Files.deleteIfExists(temp)
    }
}