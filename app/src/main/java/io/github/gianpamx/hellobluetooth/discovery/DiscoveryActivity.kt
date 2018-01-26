package io.github.gianpamx.hellobluetooth.discovery

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.github.gianpamx.hellobluetooth.R


class DiscoveryActivity : AppCompatActivity() {

    companion object {
        val TAG = "DiscoveryActivity"
        val REQUEST_ENABLE_BT = 1
    }

    lateinit var statusSnackbar: Snackbar

    lateinit var discoveryViewModel: DiscoveryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.discovery_activity)

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        discoveryViewModel = ViewModelProviders.of(this, DiscoveryViewModel.Factory(bluetoothAdapter)).get(DiscoveryViewModel::class.java)

        discoveryViewModel.error.observe(this, Observer {
            it?.let {
                Snackbar.make(findViewById(android.R.id.content), it, LENGTH_INDEFINITE).show()
            }
        })

        statusSnackbar = Snackbar.make(findViewById(android.R.id.content), "", LENGTH_INDEFINITE)
        discoveryViewModel.status.observe(this, Observer {
            it?.let {
                if (it == DiscoveryViewModel.State.STATE_OFF) {
                    statusSnackbar.setText("Bluetooth OFF")
                    statusSnackbar.setAction("Turn ON", View.OnClickListener {
                        startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
                    })
                    statusSnackbar.show()
                } else if (it == DiscoveryViewModel.State.STATE_ON) {
                    statusSnackbar.dismiss()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            discoveryViewModel.checkStatus()
        }
    }

    override fun onResume() {
        super.onResume()

        registerReceiver(discoveryViewModel.stateChangeBroadcastReceiver, IntentFilter(ACTION_STATE_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(discoveryViewModel.stateChangeBroadcastReceiver)
    }
}
