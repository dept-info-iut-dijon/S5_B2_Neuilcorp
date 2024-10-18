package com.example.spotthedifference

import org.junit.Test
import org.junit.Assert.*

class PlayerTest {

    /// <summary>
    /// Vérifie que le joueur se construit correctement.
    /// </summary>
    @Test
    fun testPlayerInitialization() {
        val player = Player("1", "Leo")
        assertEquals("Leo", player.name)
        player.name = "oeL"
        assertEquals("oeL", player.name)
    }
}
