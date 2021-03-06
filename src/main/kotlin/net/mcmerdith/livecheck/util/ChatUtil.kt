@file:Suppress("unused", "unused", "unused", "unused", "unused", "MemberVisibilityCanBePrivate")

package net.mcmerdith.livecheck.util

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ChatUtil {
    fun royal(player: CommandSender, msg: String) = msg(player, ChatColor.GOLD, msg)
    fun success(player: CommandSender, msg: String) = msg(player, ChatColor.GREEN, msg)
    fun error(player: CommandSender, msg: String) = msg(player, ChatColor.RED, msg)
    fun info(player: CommandSender, msg: String) = msg(player, ChatColor.AQUA, msg)
    fun msg(player: CommandSender, color: ChatColor, msg: String) = player.sendMessage("$color$msg")

    fun title(player: Player, title: String, subtitle: String?, fadein: Int = 10, stay: Int = 70, fadeout: Int = 20) =
        player.sendTitle(title, subtitle, fadein, stay, fadeout)

    fun actionRoyal(player: Player, msg: String) = actionMsg(player, ChatColor.GOLD, msg)
    fun actionSuccess(player: Player, msg: String) = actionMsg(player, ChatColor.GREEN, msg)
    fun actionError(player: Player, msg: String) = actionMsg(player, ChatColor.RED, msg)
    fun actionInfo(player: Player, msg: String) = actionMsg(player, ChatColor.AQUA, msg)
    fun actionMsg(player: Player, color: ChatColor, msg: String) =
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("$color$msg"))
}