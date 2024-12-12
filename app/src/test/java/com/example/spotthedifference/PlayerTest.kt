package com.example.spotthedifference

import com.example.spotthedifference.models.Player
import org.junit.Test
import org.junit.Assert.*

class PlayerTest {

    /**
     * Vérifie que le joueur est correctement initialisé.
     */
    @Test
    fun testPlayerConstructor() {
        val player = Player("1", "Leo")
        assertEquals("Leo", player.name)
    }

    /**
     * Vérifie que le nom du joueur peut être modifié.
     */
    @Test
    fun testPlayerNameModification() {
        val player = Player("1", "Leo")
        player.name = "oeL"
        assertEquals("oeL", player.name)
    }
}
