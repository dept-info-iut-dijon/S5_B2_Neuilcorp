package com.example.spotthedifference

import org.junit.Test
import org.junit.Assert.*

class GameSessionTest {

    /// <summary>
    /// Vérifie que l'initialisation de la partie avec l'ajout des joueurs se fait correctement.
    /// </summary>
    @Test
    fun testGameSessionInitialization() {
        val jean = Player("1", "Jean")
        val leo = Player("2", "Leo")
        val listPlayers =  ArrayList<Player>()
        listPlayers.add(jean)
        listPlayers.add(leo)
        val gameSession = GameSession(listPlayers)

        assertEquals(listPlayers, gameSession.getPlayers())
    }
}
