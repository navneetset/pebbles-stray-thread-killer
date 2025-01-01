package tech.sethi.pebbles.straythreadkiller

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

object StrayThreadKiller : ModInitializer {
    private val logger = LoggerFactory.getLogger("stray-thread-killer")
    var server: MinecraftServer? = null
    var isServerRunning = false

    private val watchThread = Executors.newSingleThreadExecutor()

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            this.server = server
            isServerRunning = true

            ConfigHandler

            if (!ConfigHandler.config.enabled) {
                logger.info("Stray Thread Killer is disabled. Skipping initialization.")
                return@register
            }

            watchThread.execute {
                try {
                    while (isServerRunning) {
                        Thread.sleep(5000)

                        if (server == null || !server.isRunning) {
                            logger.info("Server is no longer running, stopping thread monitoring.")
                            Thread.sleep(ConfigHandler.config.waitToShutdownSeconds * 1000L)
                            isServerRunning = false
                            forceShutdownStrayThreads()
                        }
                    }
                } catch (e: InterruptedException) {
                    logger.warn("Watch thread interrupted", e)
                } finally {
                    logger.info("Shutting down watch thread executor.")
                    watchThread.shutdown()
                }
            }
        }
    }

    private fun forceShutdownStrayThreads() {
        logger.info("Attempting to shutdown stray threads...")

        Thread.getAllStackTraces().keys.forEach { thread ->
            if (!thread.isDaemon) {
                logger.info("Interrupting thread: ${thread.name}")
                thread.interrupt()
            }
        }

        logger.info("All non-daemon threads have been interrupted. Forcing server shutdown.")

        Runtime.getRuntime().halt(0)
    }
}