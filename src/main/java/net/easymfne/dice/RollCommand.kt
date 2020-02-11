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

import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.max

/**
 * The class that handles all console and player commands for the plugin.
 *
 * @author Eric Hildebrand
 */
class RollCommand(private val plugin: Dice) : CommandExecutor {
    private val random: Random = Random()

    /**
     * Broadcast the results of a dice roll to the players of the server.
     * Configuration can be set so that messages are only set within the world
     * that the player resides, and also within a certain distance of them. Dice
     * rolled by non-players (e.g. the Console) are sent to all players.
     *
     * @param sender
     * The user rolling the dice
     * @param message
     * The fully-formatted message to display
     */
    private fun broadcast(sender: CommandSender, message: String?) {
        if (message == null) {
            return
        }
        val p1 = if (sender is Player) sender else null
        if (plugin.pluginConfig!!.isLogging) {
            plugin.logger.info(message)
        }
        for (p2 in plugin.server.onlinePlayers) {
            if (plugin.pluginConfig!!.isCrossworld || p1 == null || p1.world === p2.world) {
                if (plugin.pluginConfig!!.broadcastRange < 0 || p1 == null ||
                        getDSquared(p1, p2) < square(plugin.pluginConfig!!.broadcastRange)) {
                    p2.sendMessage(message)
                }
            }
        }
    }

    /**
     * Release the '/roll' command from its ties to this class.
     */
    fun close() {
        plugin.getCommand("roll")!!.setExecutor(null)
    }

    /**
     * Format and return a String that will be used to display the roll results.
     * This method replaces tags: {PLAYER}, {RESULT}, {COUNT}, {SIDES}, {TOTAL}.
     * This method also replaces '&' style color codes with proper ChatColors.
     *
     * @param sender
     * The user that rolled the dice
     * @param roll
     * The results of the roll, as an array
     * @param sides
     * The number of sides on the dice
     * @return The fancy-formatted message
     */
    private fun formatString(sender: CommandSender, roll: IntArray, sides: Int): String? {
        var result = if (broadcast(sender)) {
            plugin.pluginConfig!!.broadcastMessage
        } else {
            plugin.pluginConfig!!.privateMessage
        }
        if (result.isEmpty()) {
            return null
        }
        result = result.replace("\\{PLAYER}".toRegex(), sender.name)
        result = result.replace("\\{RESULT}".toRegex(), StringUtils.join(roll.toTypedArray(), ", "))
        result = result.replace("\\{COUNT}".toRegex(), "" + roll.size)
        result = result.replace("\\{SIDES}".toRegex(), "" + sides)
        result = result.replace("\\{TOTAL}".toRegex(), "" + roll.sum())
        return ChatColor.translateAlternateColorCodes('&', result)
    }

    /**
     * Get the squared distance between two players.
     *
     * @param p1
     * Player one
     * @param p2
     * Player two
     * @return The distance^2
     */
    private fun getDSquared(p1: Player, p2: Player): Int {
        val dx = p1.location.blockX - p2.location.blockX
        val dy = p1.location.blockY - p2.location.blockY
        val dz = p1.location.blockZ - p2.location.blockZ
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Show the results of a roll to a player privately.
     *
     * @param sender
     * The user rolling the dice
     * @param message
     * The fully-formatted message to display
     */
    private fun message(sender: CommandSender, message: String?) {
        if (message == null) {
            return
        }
        sender.sendMessage(message)
    }

    /**
     * This method handles user commands. Usage: "/roll <help,reload>" which
     * either shows help or reloads config. Usage: "/roll [count] [d<sides>]"
     * where the order of the arguments does not matter, but the number of sides
     * must be prefixed with 'd'.
     */
    override fun onCommand(sender: CommandSender, command: Command,
                           label: String, args: Array<String>): Boolean {
        if (args.size == 1) {
            if (args[0].equals("help", ignoreCase = true)
                    || args[0].equals("?", ignoreCase = true)) {
                showHelp(sender)
                return true
            }
            if (canReload(sender) && args[0].equals("reload", ignoreCase = true)) {
                plugin.reload()
                sender.sendMessage("Configuration reloaded")
                return true
            }
        }
        var count = plugin.pluginConfig!!.defaultCount
        var sides = plugin.pluginConfig!!.defaultSides
        /* Check for arguments representing dice count */
        if (args.isNotEmpty() && canRollMultiple(sender)) {
            for (arg in args) {
                if (Regex("^[0-9]+$").matches(arg)) {
                    count = arg.toInt()
                    break
                }
            }
        }
        /* Check for arguments representing dice sides */
        if (args.isNotEmpty() && canRollAnyDice(sender)) {
            for (arg in args) {
                if (Regex("^d[0-9]+$").matches(arg)) {
                    sides = arg.substring(1).toInt()
                    break
                }
            }
        }
        /* Check the loaded or parsed values against the defined maximums. */
        if (count > plugin.pluginConfig!!.maximumCount) {
            sender.sendMessage(ChatColor.RED
                    .toString() + "You can't roll that many dice at once")
            return false
        }
        if (sides > plugin.pluginConfig!!.maximumSides) {
            sender.sendMessage(ChatColor.RED
                    .toString() + "You can't roll dice with that many sides")
            return false
        }
        /* Roll the dice and handle the outcome */
        roll(sender, max(1, count), max(2, sides))
        return true
    }

    /**
     * Roll a set of dice for a user, and either broadcast the results publicly
     * or send them privately, depending on the user's permissions.
     *
     * @param sender
     * The user rolling the dice
     * @param count
     * The number of dice to roll
     * @param sides
     * The number of sides per die
     */
    private fun roll(sender: CommandSender, count: Int, sides: Int) {
        val result = IntArray(count) {
            random.nextInt(sides) + 1
        }
        if (broadcast(sender)) {
            broadcast(sender, formatString(sender, result, sides))
        } else {
            message(sender, formatString(sender, result, sides))
        }
    }

    /**
     * Show personalized usage help to the user, taking into account his or her
     * permissions.
     *
     * @param sender
     * The user to help
     */
    private fun showHelp(sender: CommandSender) {
        /* Treat the pair of booleans as 2^0 and 2^1 bits */
        val perms = ((if (canRollMultiple(sender)) 1 else 0)
                + if (canRollAnyDice(sender)) 2 else 0)
        when (perms) {
            1 -> {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /roll [count]")
                return
            }
            2 -> {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /roll [d<sides>]")
                return
            }
            3 -> {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /roll [count] [d<sides>]")
                return
            }
            else -> sender.sendMessage(ChatColor.RED.toString() + "Usage: /roll")
        }
    }

    /**
     * Square an input. Useful for de-cluttering the code.
     *
     * @param input
     * The number to be squared
     * @return The result
     */
    private fun square(input: Int): Int {
        return input * input
    }

    /**
     * Instantiate by getting a reference to the plugin instance, creating a new
     * Random, and registering this class to handle the '/roll' command.
     */
    init {
        plugin.getCommand("roll")!!.setExecutor(this)
    }
}