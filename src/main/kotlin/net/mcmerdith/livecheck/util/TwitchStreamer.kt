package net.mcmerdith.livecheck.util

/**
 * @author mcmerdith
 * @param online Whether the Streamer is streaming
 * @param viewers Number of viewers watching the stream, 0 if not streaming
 */
data class TwitchStreamer(val online: Boolean = false, val viewers: Int = 0)
