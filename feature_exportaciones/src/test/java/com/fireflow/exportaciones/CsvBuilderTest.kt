package com.fireflow.exportaciones

import com.fireflow.domain.model.Client
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.MotorType
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.model.PressureMeasurement
import com.fireflow.domain.model.Revision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvBuilderTest {

    private val testClient = Client(
        id = 1,
        name = "Cliente Test",
        cif = "B12345678",
        address = "Calle Mayor 1",
        province = "Madrid",
        contactPerson = "Juan García",
        phone = "911234567",
        email = "test@example.com",
        latitude = 40.4168,
        longitude = -3.7038,
        notes = "",
        createdAt = 0,
        updatedAt = 0,
        isActive = true
    )

    private val testGroup = PressureGroup(
        id = 1,
        clientId = 1,
        brand = "Wilo",
        model = "HydroMulti-E",
        serialNumber = "ABC123",
        pumpNumber = "P001",
        manufacturer = "Wilo SE",
        power = "15 kW",
        installationDate = null,
        maintenanceDate = null,
        createdAt = 0,
        updatedAt = 0,
        isActive = true
    )

    private val testRevision = Revision(
        id = 1,
        groupId = 1,
        date = 1735689600000, // 2025-01-01
        technicianName = "Técnico A",
        notes = "Revisión anual completa",
        checklistResults = mapOf(
            "Fugas externas" to true,
            "Ruidos anormales" to false
        ),
        createdAt = 0,
        updatedAt = 0
    )

    private val testMotor = Motor(
        id = 1,
        groupId = 1,
        motorType = MotorType.ELECTRIC,
        nominalFlow = 100.0, // L/min
        manometricHeight = 50.0,
        createdAt = 0,
        updatedAt = 0
    )

    private val testMeasurement = PressureMeasurement(
        id = 1,
        motorId = 1,
        year = 2025,
        pressureAt0 = 50.0,
        pressureAt50 = 48.5,
        pressureAt100 = 45.0,
        pressureAt140 = 40.0,
        isActive = true,
        createdAt = 0,
        updatedAt = 0
    )

    @Test
    fun `build includes BOM and semicolon separator`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.startsWith(CsvBuilder.BOM))
        assertTrue(csv.contains(";"))
    }

    @Test
    fun `build includes client data`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("CLIENTE"))
        assertTrue(csv.contains("Cliente Test"))
        assertTrue(csv.contains("B12345678"))
        assertTrue(csv.contains("Calle Mayor 1"))
        assertTrue(csv.contains("Madrid"))
        assertTrue(csv.contains("Juan García"))
        assertTrue(csv.contains("911234567"))
        assertTrue(csv.contains("test@example.com"))
    }

    @Test
    fun `build includes group data`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("GRUPO DE PRESIÓN"))
        assertTrue(csv.contains("Wilo"))
        assertTrue(csv.contains("HydroMulti-E"))
        assertTrue(csv.contains("ABC123"))
        assertTrue(csv.contains("15 kW"))
    }

    @Test
    fun `build includes revision data`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("REVISIÓN"))
        assertTrue(csv.contains("Técnico A"))
        assertTrue(csv.contains("Revisión anual completa"))
    }

    @Test
    fun `build includes checklist`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("CHECKLIST"))
        assertTrue(csv.contains("Fugas externas"))
        assertTrue(csv.contains("Sí"))
        assertTrue(csv.contains("Ruidos anormales"))
        assertTrue(csv.contains("No"))
    }

    @Test
    fun `build includes curve data with flow calculations`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("CURVAS DE RENDIMIENTO"))
        assertTrue(csv.contains("Eléctrico"))
        assertTrue(csv.contains("0%"))
        assertTrue(csv.contains("50%"))
        assertTrue(csv.contains("100%"))
        assertTrue(csv.contains("140%"))
        assertTrue(csv.contains("50.0"))
        assertTrue(csv.contains("48.5"))
        assertTrue(csv.contains("45.0"))
        assertTrue(csv.contains("40.0"))
    }

    @Test
    fun `build skips revision without measurements`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to null))

        assertFalse(csv.contains("CURVAS DE RENDIMIENTO"))
    }

    @Test
    fun `build handles empty checklist`() {
        val revisionNoChecklist = testRevision.copy(checklistResults = emptyMap())
        val csv = CsvBuilder.build(testClient, testGroup, revisionNoChecklist, listOf(testMotor), mapOf(1L to testMeasurement))

        assertFalse(csv.contains("CHECKLIST"))
    }

    @Test
    fun `escape wraps fields with separator`() {
        val clientWithSemicolon = testClient.copy(name = "Cliente; con punto y coma")
        val csv = CsvBuilder.build(clientWithSemicolon, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("\"Cliente; con punto y coma\""))
    }

    @Test
    fun `escape wraps fields with quotes`() {
        val clientWithQuotes = testClient.copy(name = "Cliente \"Especial\"")
        val csv = CsvBuilder.build(clientWithQuotes, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("\"Cliente \"\"Especial\"\"\""))
    }

    @Test
    fun `escape wraps fields with newlines`() {
        val clientWithNewline = testClient.copy(address = "Calle 1\nPiso 2")
        val csv = CsvBuilder.build(clientWithNewline, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("\"Calle 1\nPiso 2\""))
    }

    @Test
    fun `build includes motor type labels`() {
        val dieselMotor = testMotor.copy(motorType = MotorType.DIESEL)
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(dieselMotor), mapOf(1L to testMeasurement))

        assertTrue(csv.contains("Diésel"))
    }

    @Test
    fun `build calculates flow correctly`() {
        val csv = CsvBuilder.build(testClient, testGroup, testRevision, listOf(testMotor), mapOf(1L to testMeasurement))

        assertTrue("CSV should contain 0.0 flow", csv.contains("0.0"))
        assertTrue("CSV should contain 50.0 flow", csv.contains("50.0"))
        assertTrue("CSV should contain 100.0 flow", csv.contains("100.0"))
        assertTrue("CSV should contain 140.0 flow", csv.contains("140.0"))
    }
}
