@file:Suppress("unused")

package net.mcmerdith.livecheck.config

import net.mcmerdith.livecheck.Main
import org.bukkit.ChatColor
import java.lang.reflect.Field

object ConfigFields {
    @JvmStatic
    val PREFS_API_URL =
        ConfigField(
            "preferences.api_url",
            String::class.java,
            "https://api.filepipe.io/test/twitch.php?name=",
            "API URL to fetch Twitch data"
        )

    @JvmStatic
    val PREFS_KICK_OP =
        ConfigField(
            "preferences.kick_op",
            Boolean::class.javaObjectType,
            false,
            "Whether or not Admins should be kicked for being offline"
        )

    @JvmStatic
    val PREFS_NON_OP_LINK_OTHER_PLAYERS =
        ConfigField(
            "preferences.non_op_link_other_players",
            Boolean::class.javaObjectType,
            false,
            "Whether or not regular Users can change other users Twitch registration"
        )

    @JvmStatic
    val PREFS_MIN_VIEWERS_ENABLED =
        ConfigField(
            "preferences.minimum_viewers.enabled",
            Boolean::class.javaObjectType,
            false,
            "Kick players for having too low viewership"
        )

    @JvmStatic
    val PREFS_MIN_VIEWERS_THRESHOLD =
        ConfigField(
            "preferences.minimum_viewers.threshold",
            Int::class.javaObjectType,
            250,
            "Threshold to kick players. Viewership < Threshold = Kick"
        )

    @JvmStatic
    val MESSAGES_NO_TWITCH =
        ConfigField(
            "messages.no_twitch",
            String::class.java,
            "Please link your twitch. Use /livechecklink",
            "Message displayed when a player is frozen for not having a linked Twitch"
        )

    @JvmStatic
    val MESSAGES_OFFLINE =
        ConfigField(
            "messages.offline",
            String::class.java,
            "You must be streaming to play",
            "Kick Message shown to user when they're kicked for being offline"
        )

    private val logger = Main.getInstance().logger

    @JvmStatic
    fun valueOf(name: String): ConfigField<*>? {
        return try {
            val value = allConfigFields().firstOrNull { it.name == name }?.get(null)
            if (value is ConfigField<*>) value
            else null
        } catch (e: Exception) {
            logger.warning("${ChatColor.RED}Failed to read Config Property")
            e.printStackTrace()
            null
        }
    }

    /**
     * Get all available config fields
     * @return A list of available ConfigFields
     */
    @JvmStatic
    fun all(): List<ConfigField<*>> {
        return try {
            allConfigFields().mapNotNull { it.get(null) as? ConfigField<*> }
        } catch (e: Exception) {
            logger.warning("${ChatColor.RED}Failed to read Config Property")
            e.printStackTrace()
            listOf()
        }
    }

    /**
     * Get all ConfigField names
     * @return List of available ConfigField names
     */
    @JvmStatic
    fun names(): List<String> = allConfigFields().map { it.name }

    @JvmStatic
    private fun allConfigFields(): List<Field> =
        ConfigFields::class.java.declaredFields.filter { it.type.isAssignableFrom(ConfigField::class.java) }
}