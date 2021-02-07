package net.mcmerdith.livecheck.events

import net.mcmerdith.livecheck.Main
import net.mcmerdith.livecheck.config.ConfigFields
import net.mcmerdith.livecheck.config.TwitchNames
import net.mcmerdith.livecheck.util.ChatUtil
import net.mcmerdith.livecheck.util.ConfigHelper
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

class PlayerMonitor : Listener {
    companion object {
        /** Whether or not the plugin's checks should run */
        var enabled = true
            set(value) {
                Main.getInstance().logger.info("${ChatColor.GREEN}Plugin State: $value")
                field = value
            }

        /** List of players with no linked Twitch */
        val noTwitch = mutableListOf<UUID>()

        /**
         * Link a Twitch username to a Player
         * @param user The UUID of the Player to link to
         * @param twitch The Twitch username to link
         */
        fun linkTwitch(user: UUID, twitch: String) {
            // Remove them from the noTwitch watchlist
            noTwitch.remove(user)
            // Update their UUID in the config
            TwitchNames.config.set(user.toString(), twitch)
            TwitchNames.save()
        }

        /**
         * Check if the player has a linked Twitch username
         * @param player The Player to check
         * @return The Players Twitch username, or NULL if the Player should be ignored
         */
        fun checkPlayer(player: Player): String? {
            // Skip execution if they're already on the watchlist OR if they're an OP and OPs are exempted
            if (noTwitch.contains(player.uniqueId)) return null

            // Get their name
            val twitchname = TwitchNames.config.getString(player.uniqueId.toString())

            if (twitchname == null && !(player.isOp && !ConfigHelper.get(ConfigFields.PREFS_KICK_OP))) {
                // If they don't have one, put them on a watchlist and inform them
                ChatUtil.info(player, ConfigHelper.get(ConfigFields.MESSAGES_NO_TWITCH))
                noTwitch.add(player.uniqueId)
            }

            return twitchname // Pass the users Twitch back to the calling function
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        checkPlayer(event.player) // Check the player as soon as they join
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (noTwitch.contains(event.player.uniqueId) && enabled) {
            event.isCancelled = true
            ChatUtil.actionError(event.player, ConfigHelper.get(ConfigFields.MESSAGES_NO_TWITCH))
        }
    }
}