package io.github.gianpamx.hellobluetooth.discovery

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import io.github.gianpamx.hellobluetooth.R
import kotlinx.android.synthetic.main.discovery_activity.*


class DiscoveryActivity : AppCompatActivity() {

    companion object {
        val TAG = "DiscoveryActivity"
        val REQUEST_ENABLE_BT = 1
        val REQUEST_LOCATION_PERMISSION = 2
    }

    lateinit var statusSnackbar: Snackbar

    lateinit var adapter: DeviceAdapter

    lateinit var discoveryViewModel: DiscoveryViewModel

    var isDiscovering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.discovery_activity)

        adapter = DeviceAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        statusSnackbar = Snackbar.make(findViewById(android.R.id.content), "", LENGTH_INDEFINITE)

        discoveryViewModel = ViewModelProviders.of(this, DiscoveryViewModel.Factory(BluetoothAdapter.getDefaultAdapter())).get(DiscoveryViewModel::class.java)
        discoveryViewModel.error.observe(this, errorObserver)
        discoveryViewModel.status.observe(this, statusObserver)
        discoveryViewModel.devices.observe(this, devicesObserver)
        discoveryViewModel.discovering.observe(this, discoveringObserver)
    }

    val errorObserver = Observer<String> {
        it?.let {
            Snackbar.make(findViewById(android.R.id.content), it, LENGTH_INDEFINITE).show()
        }
    }

    val statusObserver = Observer<DiscoveryViewModel.State> {
        it?.let {
            if (it == DiscoveryViewModel.State.STATE_OFF) {
                statusSnackbar.setText("Bluetooth OFF")
                statusSnackbar.setAction("Turn ON", {
                    startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
                })
                statusSnackbar.show()
            } else if (it == DiscoveryViewModel.State.STATE_ON) {
                statusSnackbar.dismiss()
            }
        }
    }

    val devicesObserver = Observer<List<Device>> {
        it?.let {
            adapter.updateDevices(it)
        }
    }

    val discoveringObserver = Observer<Boolean> {
        it?.let {
            isDiscovering = it
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.discovery_menu, menu)

        menu?.findItem(R.id.searchItem)?.setVisible(!isDiscovering)
        menu?.findItem(R.id.stopItem)?.setVisible(isDiscovering)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.searchItem) {
            if (PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                discoveryViewModel.discover()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
        } else if (item?.itemId == R.id.stopItem) {
            discoveryViewModel.cancelDiscover()
        } else {
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            discoveryViewModel.checkStatus()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && PERMISSION_GRANTED == grantResults[0]) {
            discoveryViewModel.discover()
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(discoveryViewModel.stateChangeBroadcastReceiver, IntentFilter(ACTION_STATE_CHANGED))
        registerReceiver(discoveryViewModel.actionBroadcastReceiver, IntentFilter(ACTION_FOUND))
        registerReceiver(discoveryViewModel.discoveryReceiver, IntentFilter(ACTION_DISCOVERY_STARTED))
        registerReceiver(discoveryViewModel.discoveryReceiver, IntentFilter(ACTION_DISCOVERY_FINISHED))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(discoveryViewModel.stateChangeBroadcastReceiver)
        unregisterReceiver(discoveryViewModel.actionBroadcastReceiver)
        unregisterReceiver(discoveryViewModel.discoveryReceiver)
    }
}
