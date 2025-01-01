package tech.sethi.pebbles.straythreadkiller

import com.google.gson.GsonBuilder
import java.io.File

object ConfigHandler {
    val configFile = File("config/straythreadkiller.json")

    val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    var config = Config()

    init {
        reload()
    }

    fun reload() {
        if (configFile.exists()) {
            configFile.parentFile.mkdirs()
            val json = configFile.readText()
            config = gson.fromJson(json, Config::class.java)
        } else {
            save()
        }
    }

    fun save() {
        val json = gson.toJson(config)
        configFile.writeText(json)
    }


    data class Config(
        val enabled: Boolean = true,
        val waitToShutdownSeconds: Int = 5,
    )
}