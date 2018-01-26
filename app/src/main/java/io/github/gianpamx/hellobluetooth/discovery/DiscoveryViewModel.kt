package io.github.gianpamx.hellobluetooth.discovery

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DiscoveryViewModel : ViewModel {
    val error = MutableLiveData<String>()
    val status = MutableLiveData<State>()
    val devices = MutableLiveData<List<Device>>()

    private val bluetoothAdapter: BluetoothAdapter?

    constructor(bluetoothAdapter: BluetoothAdapter?) {
        this.bluetoothAdapter = bluetoothAdapter

        if (this.bluetoothAdapter == null) {
            error.value = "Devices do not support bluetooth"
            return
        }

        checkStatus()
    }

    fun checkStatus() {
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                status.postValue(State.STATE_OFF)
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
                    status.postValue(DiscoveryViewModel.State.STATE_OFF)
                } else if (it.extras.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_ON) {
                    status.postValue(DiscoveryViewModel.State.STATE_ON)
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
