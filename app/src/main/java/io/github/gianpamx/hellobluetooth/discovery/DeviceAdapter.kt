package io.github.gianpamx.hellobluetooth.discovery

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.gianpamx.hellobluetooth.R
import kotlinx.android.synthetic.main.discovery_device.view.*


class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
    private val devices = ArrayList<Device>()

    fun updateDevices(devices: List<Device>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(this.devices, devices))

        this.devices.clear()
        this.devices.addAll(devices)

        diffResult.dispatchUpdatesTo(this);
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.discovery_device, parent, false))
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(device: Device) {
            view.nameTextView.text = device.name
            view.addressTextView.text = device.address
            view.pairedTextView.text = if(device.isPaired) "Paired" else "Not paired"
        }
    }

    class DiffCallback(val oldList: List<Device>, val newList: List<Device>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].address == newList[newItemPosition].address

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]
    }
}
