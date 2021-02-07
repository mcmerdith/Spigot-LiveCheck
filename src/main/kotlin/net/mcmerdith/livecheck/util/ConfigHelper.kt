package net.mcmerdith.livecheck.util

import net.mcmerdith.livecheck.Main
import net.mcmerdith.livecheck.config.ConfigField

object ConfigHelper {
    /**
     * Get the value identified by a ConfigField in 'config.yml'
     * DO NOT CALL THIS METHOD IF THE PLUGIN IS NOT ENABLED
     * @param field The ConfigField to check
     * @param T The DataType to be retrieved
     */
    fun <T> get(field: ConfigField<T>): T {
        return Main.getInstance().config.getObject(field.key, field.type, field.default)!!
    }
}