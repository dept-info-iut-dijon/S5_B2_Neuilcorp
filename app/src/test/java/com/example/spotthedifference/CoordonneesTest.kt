package com.example.spotthedifference

import org.junit.Test
import org.junit.Assert.*

class CoordonneesTest {


    /// <summary>
    /// Vérifie que la lecture des coordonnées est faisable.
    /// </summary>
    @Test
    fun testCoordinateInitialization() {
        val coordinates = Coordonnees(10, 20)
        assertEquals(10, coordinates.x)
        assertEquals(20, coordinates.y)
    }
}
