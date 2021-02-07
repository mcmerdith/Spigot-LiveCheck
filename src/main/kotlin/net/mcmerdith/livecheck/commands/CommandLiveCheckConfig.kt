package net.mcmerdith.livecheck.commands

import net.mcmerdith.livecheck.Main
import net.mcmerdith.livecheck.config.ConfigFields
import net.mcmerdith.livecheck.events.PlayerMonitor
import net.mcmerdith.livecheck.util.ChatUtil
import net.mcmerdith.livecheck.util.ConfigHelper
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandLiveCheckConfig : LCCommand() {
    init {
        // Set up the tab completions
        val configs = ConfigFields.names().toMutableList()
        configs.add("ENABLED")
        addComplete(0, configs)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // No arguments shows current config
        if (args.isEmpty()) {
            val config = Main.getInstance().config
            // Get all keys EXCLUDING top level keys
            config.getKeys(true).forEach {
                if (!config.isConfigurationSection(it)) {
                    ChatUtil.msg(sender, ChatColor.RESET, "$it: ${ChatColor.YELLOW}${config.get(it)}")
                }
            }
            return true
        }

        // Field names are uppercase
        val field = args[0].toUpperCase()
        val cfg = ConfigFields.valueOf(field)

        if (cfg == null) {
            // No field by name
            ChatUtil.error(sender, "Invalid config field")
            return true
        }

        // 1 argument shows the value of 1 config
        if (args.size == 1) {
            val value = ConfigHelper.get(cfg)
            ChatUtil.info(sender, "${cfg.key} = $value")
            return true
        }

        // Field value
        val value = args.slice(1 until args.size).joinToString(" ")

        // Make sure we get the right data type
        val asBool = if (listOf("true", "false").contains(value.toLowerCase())) value.toBoolean() else null
        val asInt = value.toIntOrNull()
        val asDouble = value.toDoubleOrNull()
        val finalValue: Any = asBool ?: asInt ?: asDouble ?: value

        if (field == "ENABLED") {
            // Changing plugin state
            if (asBool != null) PlayerMonitor.enabled = asBool
            ChatUtil.success(sender, "Plugin Enabled: ${PlayerMonitor.enabled}")
        } else if (cfg.type == finalValue.javaClass) {
            // Update the config IF that field uses that datatype
            val main = Main.getInstance()
            main.config.set(cfg.key, finalValue)
            main.saveConfig()

            // Since the config has changed, there is no guarantee that all players on the watchlist belong there
            // Clear the watchlist and let it be regenerated on the next cycle
            PlayerMonitor.noTwitch.clear()

            ChatUtil.success(sender, "Set ${cfg.key} to $value")
        } else {
            ChatUtil.error(sender, "$field does not support the DataType ${finalValue.javaClass.simpleName}")
        }

        return true
    }
}