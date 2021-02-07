package net.mcmerdith.livecheck.commands

import net.mcmerdith.livecheck.util.ChatUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player


abstract class LCCommand : CommandExecutor, TabCompleter {
    // List of tab completes
    private val completes = mutableMapOf<Int, List<String>>()

    /**
     * Add completions to a specified argument
     * @param position The argument that the TabComplete should appear on (starting at 0)
     * @param complete The TabComplete to be added
     */
    fun addComplete(position: Int, complete: List<String>) {
        val target = completes[position]?.toMutableList() ?: mutableListOf()
        target.addAll(complete)
        completes[position] = target.toList()
    }

    /**
     * Helper method to check if the sender is a player
     * and notify them if they're not
     * @return If the sender is NOT a player
     * @param sender The CommandSender
     */
    fun isConsole(sender: CommandSender): Boolean {
        return if (sender !is Player) {
            ChatUtil.error(sender, "You must be a player")
            true
        } else false
    }

    abstract override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String> {
        return completes[args.size - 1]?.filter { it.toLowerCase().startsWith(args.lastOrNull()?.toLowerCase() ?: "") }?.toMutableList() ?: mutableListOf()
    }
}