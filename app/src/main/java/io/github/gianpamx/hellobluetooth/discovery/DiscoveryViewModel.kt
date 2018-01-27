package io.github.gianpamx.hellobluetooth.discovery

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DiscoveryViewModel : ViewModel {
    companion object {
        val TAG = "DiscoveryViewModel"
    }

    val error = MutableLiveData<String>()
    val status = MutableLiveData<State>()
    val devices = MutableLiveData<List<Device>>()
    val discovering = MutableLiveData<Boolean>()

    private val bluetoothAdapter: BluetoothAdapter?

    constructor(bluetoothAdapter: BluetoothAdapter?) {
        this.bluetoothAdapter = bluetoothAdapter

        if (this.bluetoothAdapter == null) {
            error.value = "Devices do not support bluetooth"
            return
        }

        discovering.postValue(this.bluetoothAdapter.isDiscovering)

        checkStatus()
    }

    fun checkStatus() {
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                status.postValue(State.STATE_OFF)
            } else {
                getDevices()
            }
        }
    }

    fun getDevices() {
        bluetoothAdapter?.let {
            val bondedDevices = it.bondedDevices.map { device -> Device(device.name, device.address, true) }
            devices.postValue(bondedDevices)
        }
    }

    val stateChangeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.extras.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_OFF) {
                    devices.postValue(emptyList())
                    status.postValue(DiscoveryViewModel.State.STATE_OFF)
                } else if (it.extras.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_ON) {
                    status.postValue(DiscoveryViewModel.State.STATE_ON)
                }
            }
        }
    }

    val discoveryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == intent?.action) {
                discovering.postValue(true)
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent?.action) {
                discovering.postValue(false);
            }
        }
    }

    fun discover() {
        bluetoothAdapter?.let {
            it.startDiscovery()
        }
    }

    fun cancelDiscover() {
        bluetoothAdapter?.let {
            it.cancelDiscovery()
        }
    }

    var actionBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_FOUND == intent?.action) {
                devices.value?.let {
                    val btDevice: BluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(EXTRA_DEVICE)
                    val device = Device(btDevice.name ?: btDevice.address, btDevice.address, false)

                    if (!it.contains(device)) {
                        val newDevices = ArrayList(it)
                        newDevices.add(device)
                        devices.postValue(newDevices)
                    }
                }
            }
        }
    }

    class Factory(val bluetoothAdapter: BluetoothAdapter?) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DiscoveryViewModel(bluetoothAdapter) as T
        }
    }

    enum class State {
        STATE_TURNING_ON,
        STATE_ON,
        STATE_TURNING_OFF,
        STATE_OFF
    }
}
