package com.wavproductions.www.armitage

import com.wavproductions.viken.Viken
import com.wavproductions.viken.VikenImage
import com.wavproductions.www.armitage.Config.loadConfig
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    loadConfig()
    val resources = Paths.get("resources").toAbsolutePath()
    var location = ""
    if (Config.javaClass.getResource("icon.png") == null) {
        location = Paths.get("src", "main", "resources").toAbsolutePath().toString()
    }
    if (Files.notExists(resources)) {
        Files.createDirectories(resources)
    }
    val iconPath = Paths.get(resources.toString(), "icon.png")
    if (Files.notExists(iconPath)) {
        var stream: InputStream? = Config.javaClass.getResourceAsStream("icon.png")
        if (stream == null) {
            stream = Files.newInputStream(Paths.get(location, "icon.png"))
        }
        if (stream == null) {
            throw RuntimeException("Corrupted jar resources!")
        }
        Files.write(iconPath, stream.readAllBytes())
        stream.close()
    }
    val conn = Metasploit()
    Viken.init()
    val icon = VikenImage(iconPath)
    val window = Viken.createSync(800, 800, "Armitage ReWrite")
    requireNotNull(window) { "Could not create Window!" }
    window.setIconSync(icon.convertToIcon())
    window.showSync()
    Viken.loop()
    while (!conn.consoleActive()) {
        Thread.onSpinWait()
    }
    Thread.sleep(20000) //run for 20 seconds for testing
    Viken.cleanup()
}