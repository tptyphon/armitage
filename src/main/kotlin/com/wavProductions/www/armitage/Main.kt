package com.wavproductions.www.armitage

import com.wavproductions.viken.Viken
import com.wavproductions.www.armitage.Config.loadConfig

fun main() {
    loadConfig()
    var conn = Metasploit()
    Viken.init()
    Viken.createSync(800, 800, "Armitage")
    Viken.loop()
    while (!conn.consoleActive()) {
        Thread.onSpinWait()
        Thread.sleep(20000) //run for 20 seconds for testing
    }
    Viken.cleanup()
}