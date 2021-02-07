package net.mcmerdith.livecheck.config

data class ConfigField<T>(val key: String, val type: Class<T>, val default: T, val help: String)