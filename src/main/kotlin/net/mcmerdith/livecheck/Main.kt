package net.mcmerdith.livecheck

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import net.mcmerdith.livecheck.commands.CommandLiveCheckConfig
import net.mcmerdith.livecheck.commands.CommandLiveCheckLink
import net.mcmerdith.livecheck.commands.LCCommand
import net.mcmerdith.livecheck.config.ConfigField
import net.mcmerdith.livecheck.config.ConfigFields
import net.mcmerdith.livecheck.config.TwitchNames
import net.mcmerdith.livecheck.events.PlayerMonitor
import net.mcmerdith.livecheck.util.ConfigHelper
import net.mcmerdith.livecheck.util.TwitchStreamer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class Main : JavaPlugin() {
    companion object {
        private var INSTANCE: Main? = null
        fun getInstance() = INSTANCE!!
    }

    override fun onEnable() {
        // Set up our instance
        INSTANCE = this

        saveDefaultConfig()

        ConfigFields.all().forEach { field ->
            // Add the default value from our config
            config.addDefault(field.key, field.default)

            // Get the current value in the config field
            val value = config.get(field.key)
            if (value != null) {
                if (field.type != value.javaClass) {
                    // If not reset it
                    config.set(field.key, field.default)
                    // Log the error
                    logger.warning("${ChatColor.RED}Invalid configuration field '${field.key}' = $value (${field.type.simpleName} != ${value.javaClass.simpleName}). Value is reset. Make sure you are using the correct datatype")
                }
            } else {
                logger.warning("${ChatColor.RED}NULL value for ${field.key}. Contact the developer")
            }
        }

        config.options().copyDefaults(true)
        saveConfig()

        // Initialize the config holder for twitch usernames
        TwitchNames.initialize()

        // Register commands and PlayerMonitor
        registerCommand("livechecklink", CommandLiveCheckLink())
        registerCommand("livecheckconfig", CommandLiveCheckConfig())
        registerEvent(PlayerMonitor())

        // Scoreboard init
        val scoreboard = Bukkit.getScoreboardManager()?.newScoreboard
        val objective = scoreboard?.registerNewObjective("viewercount", "dummy", "Viewer Count")
        objective?.displaySlot = DisplaySlot.PLAYER_LIST

        // Schedule a task to run every 100 ticks (~5seconds)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            // If the plugin is running
            if (PlayerMonitor.enabled) {
                // Get all the current players
                val players = Bukkit.getOnlinePlayers()
                Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                    // Get current configuration
                    val minViewerEnable = ConfigHelper.get(ConfigFields.PREFS_MIN_VIEWERS_ENABLED)
                    val minViewerThreshold = ConfigHelper.get(ConfigFields.PREFS_MIN_VIEWERS_THRESHOLD)

                    players.forEach { player ->
                        // Get the name we're working with
                        val twitchname = PlayerMonitor.checkPlayer(player)

                        if (twitchname != null) {
                            // If they have a twitch name, run an async task to get their user stats

                            // READ FROM API
                            try {
                                val apiUrl = URL("${ConfigHelper.get(ConfigFields.PREFS_API_URL)}$twitchname")
                                val connection = apiUrl.openConnection() as HttpURLConnection

                                // 2.5 seconds to connect, 2.5 seconds to read
                                // This is to make sure it either completes or times out before the next check
                                connection.connectTimeout = 2500
                                connection.readTimeout = 2500

                                // To store our response
                                val content = StringBuilder()

                                // Get the input stream of the connection
                                try {
                                    BufferedReader(InputStreamReader(connection.inputStream)).use { input ->
                                        var line: String?
                                        while (input.readLine().also { line = it } != null) {
                                            // Append each line of the response and separate them
                                            content.append(line)
                                        }
                                    }

                                    // Parse the JSON result
                                    val gson = Gson()
                                    val streamer = gson.fromJson(content.toString(), TwitchStreamer::class.java)

                                    player.setPlayerListName("${player.name} (${ChatColor.ITALIC}twitch.tv/$twitchname${ChatColor.RESET})")
                                    // Update the scoreboard
                                    objective?.getScore(player.name)?.score = streamer.viewers


                                    // If they're not online, schedule them to be kicked on the next tick
                                    val viewKick = minViewerEnable && streamer.viewers < minViewerThreshold
                                    if (!streamer.online || (viewKick)) {
                                        if (!player.isOp || ConfigHelper.get(ConfigFields.PREFS_KICK_OP)) {
                                            Bukkit.getScheduler().runTask(this, Runnable {
                                                player.kickPlayer(
                                                    "${ChatColor.RED}${ConfigHelper.get(ConfigFields.MESSAGES_OFFLINE)}${if (viewKick) " with more than $minViewerThreshold viewers" else ""}"
                                                )
                                            })
                                        }
                                    }
                                } catch (e: JsonSyntaxException) {
                                    logger.warning("${ChatColor.RED}Invalid JSON returned from api for user $twitchname")
                                } finally {
                                    connection.disconnect()
                                }
                            } catch (e: Exception) {
                                logger.warning("${ChatColor.RED}Failed to connect to API. Check your internet connection and api_url in config.yml. Retry in 60 seconds")
                                PlayerMonitor.enabled = false
                                Bukkit.getScheduler().runTaskLater(this, Runnable {
                                    PlayerMonitor.enabled = true
                                }, 1200)
                            }
                        }

                        if (scoreboard != null) player.scoreboard = scoreboard
                    }
                })
            }
        }, 0, 100)
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTasks(this)
    }

    private fun registerCommand(name: String, handler: LCCommand) {
        val command = getCommand(name)
        command?.setExecutor(handler)
        command?.tabCompleter = handler
    }

    private fun registerEvent(event: Listener) {
        server.pluginManager.registerEvents(event, this)
    }
}