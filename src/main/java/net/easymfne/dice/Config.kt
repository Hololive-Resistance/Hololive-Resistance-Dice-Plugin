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

/**
 * Configuration helper class, with methods for accessing the configuration.
 *
 * @author Eric Hildebrand
 */
class Config(private val plugin: Dice) {

    /**
     * Get the desired broadcast message template. The default case looks like:
     * [Dice] EasyMFnE rolled 2, 3, 6, 1, 1 (5d6)
     *
     * @return The broadcast message template
     */
    val broadcastMessage: String
        get() = plugin.config.getString("message.broadcast",
                "&c[&fDice&c] &f{PLAYER} rolled {RESULT} &7({COUNT}d{SIDES})")!!

    /**
     * @return The allowable broadcast range, defaulting to -1
     */
    val broadcastRange: Int
        get() = plugin.config.getInt("broadcast.range", -1)

    /**
     * @return The default number of dice to roll, defaulting to 1.
     */
    val defaultCount: Int
        get() = plugin.config.getInt("default.count", 1)

    /**
     * @return The default number of sides on the dice, defaulting to 6.
     */
    val defaultSides: Int
        get() = plugin.config.getInt("default.sides", 6)

    /**
     * @return The maximum number of dice that can be rolled at once.
     */
    val maximumCount: Int
        get() = plugin.config.getInt("maximum.count", 6)

    /**
     * @return The maximum number of sides on a die.
     */
    val maximumSides: Int
        get() = plugin.config.getInt("maximum.sides", 20)

    /**
     * Get the desired private message template. The default case looks like:
     * [Dice] You rolled 2, 3, 6, 1, 1 (5d6)
     *
     * @return The private message template
     */
    val privateMessage: String
        get() = plugin.config.getString("message.private",
                "&4[&fDice&4] &fYou rolled {RESULT} &7({COUNT}d{SIDES})")!!

    /**
     * @return Do dice broadcasts travel between worlds?
     */
    val isCrossworld: Boolean
        get() = plugin.config.getBoolean("broadcast.crossworld", false)

    /**
     * @return Are we logging all dice rolls?
     */
    val isLogging: Boolean
        get() = plugin.config.getBoolean("logging", false)

}