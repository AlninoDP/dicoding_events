package com.example.dicodingevents.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dicodingevents.data.local.entity.EventEntity
import com.example.dicodingevents.databinding.CarouselItemBinding
import com.example.dicodingevents.ui.eventdetail.EventDetailActivity
import com.example.dicodingevents.utils.EventDiffCallback

class CarouselAdapter :
    ListAdapter<EventEntity, CarouselAdapter.CarouselViewHolder>(EventDiffCallback()) {

    class CarouselViewHolder(private val binding: CarouselItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: EventEntity) {
            Glide.with(itemView.context)
                .load(event.mediaCover)
                .into(binding.carouselImage)

            binding.eventName.text = event.name

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, EventDetailActivity::class.java)
                intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.eventId)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding =
            CarouselItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarouselViewHolder(binding)
    }


    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
}