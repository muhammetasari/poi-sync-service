package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.service.LocationSyncService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class LocationSyncControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var locationSyncService: LocationSyncService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val controller = LocationSyncController(locationSyncService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `should start location sync and return accepted status with correct body`() {
        mockMvc.perform(post("/api/sync/locations")
            .param("lat", "40.7128")
            .param("lng", "-74.0060")
            .param("radius", "1000.0")
            .param("type", "restaurant"))
            .andExpect(status().isAccepted)
            .andExpect(content().string("Synchronization started"))
    }
}
