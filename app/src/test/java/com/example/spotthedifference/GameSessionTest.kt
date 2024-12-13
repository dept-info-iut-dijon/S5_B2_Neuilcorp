package com.example.spotthedifference

import com.example.spotthedifference.models.GameSession
import com.example.spotthedifference.models.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class GameSessionTest {

    /// <summary>
    /// Vérifie que l'initialisation de la partie avec l'ajout des joueurs se fait correctement.
    /// </summary>
    @Test
    fun testGameSessionInitialization() {
        val jean = Player("Jean")
        val leo = Player("Leo")
        val listPlayers =  ArrayList<Player>()
        listPlayers.add(jean)
        listPlayers.add(leo)
        val gameSession =
            GameSession(listPlayers)

        assertEquals(listPlayers, gameSession.getPlayers())
    }
}
