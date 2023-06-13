package com.example.uptechapp.dao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uptechapp.R;

import java.util.List;

public class GifPagerAdapter extends RecyclerView.Adapter<GifPagerAdapter.GifViewHolder> {
    private final List<GifImage> gifImages;

    public GifPagerAdapter(List<GifImage> gifImages) {
        this.gifImages = gifImages;
    }

    @NonNull
    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lea, parent, false);
        return new GifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GifViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .asGif()
                .load(gifImages.get(position).getResourceId())
                .into(holder.gifView);
    }

    @Override
    public int getItemCount() {
        return gifImages.size();
    }

    public static class GifViewHolder extends RecyclerView.ViewHolder {
        public final ImageView gifView;

        public GifViewHolder(View itemView) {
            super(itemView);
            gifView = itemView.findViewById(R.id.gif);
        }
    }
}
