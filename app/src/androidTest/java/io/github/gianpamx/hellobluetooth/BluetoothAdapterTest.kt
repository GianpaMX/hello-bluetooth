package io.github.gianpamx.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.any
import org.mockito.Mockito.mock

val ONE_SECOND = 1000L

@RunWith(AndroidJUnit4::class)
class BluetoothAdapterTest {
    lateinit var bluetoothAdapter: BluetoothAdapter

    @Before
    fun setUp() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        assertNotNull(bluetoothAdapter)

        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
            Thread.sleep(ONE_SECOND)
        }
        assertTrue(bluetoothAdapter.isEnabled)
    }

    @Test
    fun startDiscovery() {
        val receiver = mock(BroadcastReceiver::class.java)
        InstrumentationRegistry.getTargetContext().registerReceiver(receiver, IntentFilter(ACTION_DISCOVERY_STARTED))
        val captor = argumentCaptor<Intent>()

        bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.startDiscovery()

        Thread.sleep(ONE_SECOND)

        InstrumentationRegistry.getTargetContext().unregisterReceiver(receiver)
        verify(receiver).onReceive(any(Context::class.java), captor.capture())
        assertEquals(ACTION_DISCOVERY_STARTED, captor.firstValue.action)
    }

    @Test
    fun cancelDiscovery() {
        val receiver = mock(BroadcastReceiver::class.java)
        InstrumentationRegistry.getTargetContext().registerReceiver(receiver, IntentFilter(ACTION_DISCOVERY_FINISHED))
        val captor = argumentCaptor<Intent>()

        bluetoothAdapter.startDiscovery()
        bluetoothAdapter.cancelDiscovery()

        Thread.sleep(ONE_SECOND)

        InstrumentationRegistry.getTargetContext().unregisterReceiver(receiver)
        verify(receiver).onReceive(any(Context::class.java), captor.capture())
        assertEquals(ACTION_DISCOVERY_FINISHED, captor.firstValue.action)
    }

    @After
    fun tearDown() {
        bluetoothAdapter.cancelDiscovery()
    }
}
