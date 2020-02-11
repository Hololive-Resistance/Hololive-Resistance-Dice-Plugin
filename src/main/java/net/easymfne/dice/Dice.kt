/*
 * This file is part of the Dice plugin by EasyMFnE.
 * 
 * Dice is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 * 
 * Dice is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU General Public License for details.
 * 
 * You should have received a copy of the GNU General Public License v3 along
 * with Dice. If not, see <http://www.gnu.org/licenses/>.
 */
package net.easymfne.dice

import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.mcstats.MetricsLite
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.Level

/**
 * This is the main class of the Dice plugin, responsible for its own setup,
 * logging, reloading, and shutdown. Maintains instances of Config and
 * RollCommand.
 *
 * @author Eric Hildebrand
 * @version 1.0
 */
open class Dice : JavaPlugin() {
    var pluginConfig: Config? = null
    private var rollCommand: RollCommand? = null
    /*
     * Strings used in the fancyLog() methods.
     */
    private val logPrefix = ChatColor.RED.toString() + "[Dice] "
    private val logColor = ChatColor.WHITE.toString()

    /**
     * Log a message to the console using color, with a specific logging Level.
     * If there is no console open, log the message without any coloration.
     *
     * @param level
     * Level at which the message should be logged
     * @param message
     * The message to be logged
     */
    protected fun fancyLog(level: Level, message: String) {
        server.logger.log(level, message)
    }

    /**
     * Log a message to the console using color, defaulting to the Info level.
     * If there is no console open, log the message without any coloration.
     *
     * @param message
     * The message to be logged
     */
    protected fun fancyLog(message: String) {
        fancyLog(Level.INFO, message)
    }

    /**
     * Unregister and null the command handler, then null the configuration
     * instance, before shutting down and displaying the milliseconds it took.
     */
    override fun onDisable() {
        val start = Calendar.getInstance().timeInMillis
        fancyLog("=== DISABLE START ===")
        rollCommand!!.close()
        rollCommand = null
        pluginConfig = null
        fancyLog("=== DISABLE COMPLETE ("
                + (Calendar.getInstance().timeInMillis - start)
                + "ms) ===")
    }

    /**
     * Load configuration from file, creating it from default if needed.
     * Instantiate the configuration helper and register and command handler,
     * displaying the number of milliseconds it took.
     */
    override fun onEnable() {
        val start = Calendar.getInstance().timeInMillis
        fancyLog("=== ENABLE START ===")
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            saveDefaultConfig()
            fancyLog("Saved default config.yml")
        }
        pluginConfig = Config(this)
        rollCommand = RollCommand(this)
        startMetrics()
        fancyLog("=== ENABLE COMPLETE ("
                + (Calendar.getInstance().timeInMillis - start)
                + "ms) ===")
    }

    /**
     * Reload the plugin's configuration from disk and show how long it took.
     */
    fun reload() {
        val start = Calendar.getInstance().timeInMillis
        fancyLog("=== RELOAD START ===")
        reloadConfig()
        fancyLog("=== RELOAD COMPLETE ("
                + (Calendar.getInstance().timeInMillis - start)
                + "ms) ===")
    }

    /**
     * If possible, instantiate Metrics and connect with mcstats.org
     */
    private fun startMetrics() {
        val metrics: MetricsLite
        try {
            metrics = MetricsLite(this)
            if (metrics.start()) {
                fancyLog("Metrics enabled.")
            }
        } catch (e: IOException) {
            fancyLog(Level.WARNING, "Metrics exception: " + e.message)
        }
    }
}