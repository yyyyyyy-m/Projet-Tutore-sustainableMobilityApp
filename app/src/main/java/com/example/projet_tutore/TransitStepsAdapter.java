package com.example.projet_tutore;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransitStepsAdapter extends RecyclerView.Adapter<TransitStepsAdapter.ViewHolder> {
    private List<TDB> transitSteps;

    public TransitStepsAdapter(List<TDB> transitSteps) {
        this.transitSteps = transitSteps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tdb, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TDB step = transitSteps.get(position);

        // Badge de la ligne
        holder.tvLigneBadge.setText(step.getLigne());
        try {
            holder.tvLigneBadge.setBackgroundColor(Color.parseColor(step.getCouleur()));
        } catch (Exception e) {
            holder.tvLigneBadge.setBackgroundColor(Color.parseColor("#8ED38A"));
        }

        // Direction
        String vehicleIcon = getVehicleIcon(step.getType());
        holder.tvDirection.setText(vehicleIcon + " " + step.getSignal());

        // Nombre d'arrêts
        holder.tvStops.setText(step.getNumStops() + " arrêts");

        // Départ et arrivée
        holder.tvDepartArrivee.setText(step.getDepart() + " → " + step.getArrivee());

        // Durée
        holder.tvDureeSegment.setText(step.getDuree());
    }

    private String getVehicleIcon(String vehicleType) {
        switch (vehicleType.toLowerCase()) {
            case "subway":
            case "metro":
                return "🚇";
            case "bus":
                return "🚌";
            case "tram":
                return "🚊";
            case "train":
                return "🚆";
            case "walking":
                return "🚶";
            default:
                return "🚍";
        }
    }

    @Override
    public int getItemCount() {
        return transitSteps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLigneBadge;
        TextView tvDirection;
        TextView tvStops;
        TextView tvDepartArrivee;
        TextView tvDureeSegment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLigneBadge = itemView.findViewById(R.id.tvLigneBadge);
            tvDirection = itemView.findViewById(R.id.tvDirection);
            tvStops = itemView.findViewById(R.id.tvStops);
            tvDepartArrivee = itemView.findViewById(R.id.tvDepartArrivee);
            tvDureeSegment = itemView.findViewById(R.id.tvDureeSegment);
        }
    }
}