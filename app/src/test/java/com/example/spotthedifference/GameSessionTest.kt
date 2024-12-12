package com.example.spotthedifference

import com.example.spotthedifference.models.GameSession
import com.example.spotthedifference.models.Player
import org.junit.Test
import org.junit.Assert.*

class GameSessionTest {

    /**
     * Vérifie que la session de jeu est correctement initialisée avec les joueurs.
     */
    @Test
    fun testGameSessionInitialization() {
        val jean = Player("1", "Jean")
        val leo = Player("2", "Leo")
        val listPlayers = ArrayList<Player>()
        listPlayers.add(jean)
        listPlayers.add(leo)
        val gameSession = GameSession(listPlayers)

        assertEquals(listPlayers, gameSession.getPlayers())
    }
}
