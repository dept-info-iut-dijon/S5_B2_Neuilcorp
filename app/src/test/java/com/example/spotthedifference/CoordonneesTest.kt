package com.example.spotthedifference

import com.example.spotthedifference.models.Coordonnees
import org.junit.Test
import org.junit.Assert.*

class CoordonneesTest {

    /**
     * Vérifie que les coordonnées sont correctement initialisées.
     */
    @Test
    fun testCoordinateInitialization() {
        val coordinates = Coordonnees(10, 20)
        assertEquals(10, coordinates.x)
        assertEquals(20, coordinates.y)
    }
}
