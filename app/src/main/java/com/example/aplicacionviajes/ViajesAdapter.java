package com.example.aplicacionviajes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aplicacionviajes.entity.Trip;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViewHolder> {

    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_GRID = 1;

    private final ArrayList<Trip> trips;
    private final OnItemClickListener listener;
    private boolean isGridMode = false;

    public interface OnItemClickListener {
        void onItemClick(Trip trip);
    }

    public ViajesAdapter(ArrayList<Trip> trips, OnItemClickListener listener) {
        this.trips = trips;
        this.listener = listener;
    }

    public void setGridMode(boolean gridMode) {
        this.isGridMode = gridMode;
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == VIEW_TYPE_GRID) ? R.layout.trip_item_grid : R.layout.trip_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.bind(trip, listener);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewCodigo, textViewCiudad;
        ImageView ivFavorite, ivTripImage;
        ImageView btnBuyItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewCodigo = itemView.findViewById(R.id.textViewCodigo);
            textViewCiudad = itemView.findViewById(R.id.textViewCiudad);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            btnBuyItem = itemView.findViewById(R.id.btnBuyItem);
            ivTripImage = itemView.findViewById(R.id.ivTripImage);
        }

        public void bind(Trip trip, OnItemClickListener listener) {
            textViewTitle.setText(trip.getTitulo());
            textViewCodigo.setText(trip.getCodigo());
            textViewCiudad.setText(trip.getCiudad());

            updateFavoriteIcon(trip.isFavorite());

            Glide.with(itemView.getContext())
                    .load(trip.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivTripImage);

            ivFavorite.setOnClickListener(v -> {
                trip.setFavorite(!trip.isFavorite());
                updateFavoriteIcon(trip.isFavorite());
                FavoritesManager.saveFavoriteCode(itemView.getContext(), trip.getCodigo(), trip.isFavorite());
            });

            btnBuyItem.setOnClickListener(v -> {
                String message = "¡Que tengas un buen viaje en " + trip.getCiudad() + "!";
                Snackbar.make(itemView, message, Snackbar.LENGTH_LONG).show();
            });

            itemView.setOnClickListener(v -> listener.onItemClick(trip));
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }
}
