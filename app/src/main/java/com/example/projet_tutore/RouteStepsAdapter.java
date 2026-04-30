package com.example.projet_tutore;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RouteStepsAdapter extends RecyclerView.Adapter<RouteStepsAdapter.ViewHolder> {

    private List<RouteStep> steps;

    public RouteStepsAdapter(List<RouteStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_itineraires_steps, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RouteStep step = steps.get(position);

        holder.tvStepIcon.setText(step.getStepIcon());
        holder.tvStepTitle.setText(step.getTitle());
        holder.tvStepDetails.setText(step.getDetails());
        holder.tvStepDuration.setText(step.getDuration());

        // Gérer la couleur de ligne pour le transit
        if (step.getType().equals("TRANSIT") && step.getLineColor() != null) {
            try {
                int color = Color.parseColor("#" + step.getLineColor());
                holder.tvStepIcon.setBackgroundColor(color);
            } catch (Exception e) {
                // Couleur par défaut
            }
        }

        // Masquer les lignes pour le premier et dernier élément
        if (position == 0) {
            holder.lineTop.setVisibility(View.INVISIBLE);
        } else {
            holder.lineTop.setVisibility(View.VISIBLE);
        }

        if (position == steps.size() - 1) {
            holder.lineBottom.setVisibility(View.INVISIBLE);
        } else {
            holder.lineBottom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepIcon;
        TextView tvStepTitle;
        TextView tvStepDetails;
        TextView tvStepDuration;
        View lineTop;
        View lineBottom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepIcon = itemView.findViewById(R.id.tvStepIcon);
            tvStepTitle = itemView.findViewById(R.id.tvStepTitle);
            tvStepDetails = itemView.findViewById(R.id.tvStepDetails);
            tvStepDuration = itemView.findViewById(R.id.tvStepDuration);
            lineTop = itemView.findViewById(R.id.lineTop);
            lineBottom = itemView.findViewById(R.id.lineBottom);
        }
    }
}