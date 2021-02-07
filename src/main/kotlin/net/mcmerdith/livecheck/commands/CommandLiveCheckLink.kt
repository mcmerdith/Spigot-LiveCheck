package net.mcmerdith.livecheck.commands

import net.mcmerdith.livecheck.config.ConfigFields
import net.mcmerdith.livecheck.events.PlayerMonitor
import net.mcmerdith.livecheck.util.ChatUtil
import net.mcmerdith.livecheck.util.ConfigHelper
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandLiveCheckLink : LCCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (isConsole(sender)) return false // Players only
        sender as Player

        if (args.isEmpty()) return false // Must have at least 1 argument

        val twitch = args[0] // Twitch name
        val mc = args.getOrNull(1) // MC Name

        // MC Name is ignored UNLESS the calling player is A) an operator B) a user and the config allows users to set other users names
        val player = if (mc != null && (sender.isOp || ConfigHelper.get(ConfigFields.PREFS_NON_OP_LINK_OTHER_PLAYERS))) Bukkit.getPlayer(mc) else sender

        if (player == null) {
            // No player
            ChatUtil.error(sender, "No player by name $mc")
            return true
        }

        // Link the players MC to their Twitch
        PlayerMonitor.linkTwitch(player.uniqueId, twitch)

        ChatUtil.success(player, "Your account has been linked to twitch.tv/$twitch")
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        // Return a list of online players only IF they're allowed to pick a Player
        val players = Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
        return if (args.size == 2 && (sender.isOp || ConfigHelper.get(ConfigFields.PREFS_NON_OP_LINK_OTHER_PLAYERS))) {
            players
        } else {
            mutableListOf()
        }
    }
}