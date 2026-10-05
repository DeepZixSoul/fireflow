package com.igrupos.conversiones

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConverterViewModelTest {

    private lateinit var viewModel: ConverterViewModel

    @Before
    fun setup() {
        viewModel = ConverterViewModel()
    }

    @Test
    fun `initial state has empty values`() = runTest {
        val state = viewModel.values.value
        assertEquals("", state.lmin)
        assertEquals("", state.m3h)
        assertEquals("", state.gpm)
        assertEquals("", state.psi)
        assertEquals("", state.bar)
        assertEquals("", state.kpa)
        assertEquals("", state.mca)
    }

    @Test
    fun `updateValue LMIN converts to m3h and gpm`() = runTest {
        viewModel.updateValue(ConverterUnit.LMIN, "100")

        val state = viewModel.values.value
        assertEquals("100", state.lmin)
        assertEquals("6", state.m3h)
        assertEquals("26.42", state.gpm)
    }

    @Test
    fun `updateValue M3H converts to lmin and gpm`() = runTest {
        viewModel.updateValue(ConverterUnit.M3H, "6")

        val state = viewModel.values.value
        assertEquals("100", state.lmin)
        assertEquals("6", state.m3h)
        assertEquals("26.42", state.gpm)
    }

    @Test
    fun `updateValue GPM converts to lmin and m3h`() = runTest {
        viewModel.updateValue(ConverterUnit.GPM, "26.42")

        val state = viewModel.values.value
        assertEquals("100.01", state.lmin)
        assertEquals("6.00", state.m3h)
        assertEquals("26.42", state.gpm)
    }

    @Test
    fun `updateValue PSI converts to bar, kpa, mca`() = runTest {
        viewModel.updateValue(ConverterUnit.PSI, "14.5")

        val state = viewModel.values.value
        assertEquals("14.50", state.psi)
        assertEquals("1.00", state.bar)
        assertEquals("99.97", state.kpa)
    }

    @Test
    fun `updateValue BAR converts to psi, kpa, mca`() = runTest {
        viewModel.updateValue(ConverterUnit.BAR, "1")

        val state = viewModel.values.value
        assertEquals("14.50", state.psi)
        assertEquals("1", state.bar)
        assertEquals("100", state.kpa)
        assertEquals("10.20", state.mca)
    }

    @Test
    fun `updateValue KPA converts to psi, bar, mca`() = runTest {
        viewModel.updateValue(ConverterUnit.KPA, "100")

        val state = viewModel.values.value
        assertEquals("14.50", state.psi)
        assertEquals("1", state.bar)
        assertEquals("100", state.kpa)
        assertEquals("10.20", state.mca)
    }

    @Test
    fun `updateValue MCA converts to psi, bar, kpa`() = runTest {
        viewModel.updateValue(ConverterUnit.MCA, "10")

        val state = viewModel.values.value
        assertEquals("14.22", state.psi)
        assertEquals("0.98", state.bar)
        assertEquals("98.07", state.kpa)
        assertEquals("10", state.mca)
    }

    @Test
    fun `invalid input is ignored`() = runTest {
        viewModel.updateValue(ConverterUnit.LMIN, "abc")

        val state = viewModel.values.value
        assertEquals("", state.lmin)
        assertEquals("", state.m3h)
        assertEquals("", state.gpm)
    }

    @Test
    fun `comma is replaced by dot for decimal input`() = runTest {
        viewModel.updateValue(ConverterUnit.LMIN, "50,5")

        val state = viewModel.values.value
        assertEquals("50.50", state.lmin)
    }

    @Test
    fun `empty string does not change state`() = runTest {
        viewModel.updateValue(ConverterUnit.LMIN, "100")
        viewModel.updateValue(ConverterUnit.LMIN, "")

        val state = viewModel.values.value
        assertEquals("100", state.lmin)
    }

    @Test
    fun `flow update preserves pressure values`() = runTest {
        viewModel.updateValue(ConverterUnit.BAR, "2")
        viewModel.updateValue(ConverterUnit.LMIN, "50")

        val state = viewModel.values.value
        assertEquals("29.01", state.psi)
        assertEquals("2", state.bar)
        assertEquals("200", state.kpa)
        assertEquals("20.39", state.mca)
        assertEquals("50", state.lmin)
    }

    @Test
    fun `pressure update preserves flow values`() = runTest {
        viewModel.updateValue(ConverterUnit.LMIN, "100")
        viewModel.updateValue(ConverterUnit.BAR, "2")

        val state = viewModel.values.value
        assertEquals("100", state.lmin)
        assertEquals("6", state.m3h)
        assertEquals("26.42", state.gpm)
        assertEquals("29.01", state.psi)
        assertEquals("2", state.bar)
    }

    @Test
    fun `large values are handled correctly`() = runTest {
        viewModel.updateValue(ConverterUnit.LMIN, "10000")

        val state = viewModel.values.value
        assertEquals("10000", state.lmin)
        assertEquals("600", state.m3h)
        assertEquals("2641.72", state.gpm)
    }

    @Test
    fun `zero value is converted correctly`() = runTest {
        viewModel.updateValue(ConverterUnit.BAR, "0")

        val state = viewModel.values.value
        assertEquals("0", state.psi)
        assertEquals("0", state.bar)
        assertEquals("0", state.kpa)
        assertEquals("0", state.mca)
    }

    @Test
    fun `integer result omits trailing decimals`() = runTest {
        viewModel.updateValue(ConverterUnit.BAR, "1")

        val state = viewModel.values.value
        assertEquals("1", state.bar)
        assertEquals("100", state.kpa)
    }
}
