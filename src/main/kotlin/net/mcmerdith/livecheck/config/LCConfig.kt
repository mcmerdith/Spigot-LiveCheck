package net.mcmerdith.livecheck.config

import net.mcmerdith.livecheck.Main
import org.bukkit.ChatColor
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.lang.Exception

abstract class LCConfig(private val name: String) {
    /** File on disk to store config in */
    private lateinit var file: File

    /** Configuration */
    lateinit var config: YamlConfiguration

    /**
     * Initialize this configuration
     */
    fun initialize() {
        val plugin = Main.getInstance()

        file = File(plugin.dataFolder, "$name.yml")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            plugin.saveResource("$name.yml", false)
        }

        config = YamlConfiguration()

        try {
            config.load(file)
        } catch (e: Exception) {
            plugin.logger.warning("${ChatColor.RED}Couldn't open config file $name")
            e.printStackTrace()
        }
    }

    /**
     * Save this configuration to a file
     */
    fun save() {
        config.save(file)
    }
}