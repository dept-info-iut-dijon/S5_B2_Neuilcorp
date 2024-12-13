package com.example.spotthedifference

import com.example.spotthedifference.models.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTest {

    /// <summary>
    /// Vérifie que le joueur se construit correctement.
    /// </summary>
    @Test
    fun testPlayerInitialization() {
        val player = Player("Leo")
        assertEquals("Leo", player.name)
        player.name = "oeL"
        assertEquals("oeL", player.name)
    }
}
