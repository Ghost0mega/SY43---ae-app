package com.example.sy43___ae_app

import com.example.sy43___ae_app.ui.utils.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import android.content.Context

class LocationHelperTest {

    @Test
    fun testCalculateDistance() {
        // Mock context (not used for distance calculation)
        val context = mock(Context::class.java)
        val locationHelper = LocationHelper(context)

        // Coordinates for ME Belfort and UTBM Sevenans
        val lat1 = 47.641262
        val lon1 = 6.846063
        val lat2 = 47.584100
        val lon2 = 6.862200

        // Calculated distance should be around 6.46 km (6460 meters)
        val distance = locationHelper.calculateDistance(lat1, lon1, lat2, lon2)
        
        // Check if the distance is within a 100m margin (Haversine vs straight line)
        assertEquals(6460f, distance, 100f)
    }

    @Test
    fun testFormatDistance() {
        val context = mock(Context::class.java)
        val locationHelper = LocationHelper(context)

        assertEquals("500m", locationHelper.formatDistance(500f))
        assertEquals("1,5km", locationHelper.formatDistance(1500f).replace(".", ","))
    }
}
