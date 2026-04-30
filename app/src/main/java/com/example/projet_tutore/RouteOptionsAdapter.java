package com.example.projet_tutore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RouteOptionsAdapter extends RecyclerView.Adapter<RouteOptionsAdapter.ViewHolder> {

    private List<RouteOption> routes;
    private OnRouteClickListener listener;

    public interface OnRouteClickListener {
        void onRouteClick(RouteOption route);
    }

    public RouteOptionsAdapter(List<RouteOption> routes, OnRouteClickListener listener) {
        this.routes = routes;
        this.listener = listener;
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
        RouteOption route = routes.get(position);

        // En-tête
        holder.tvModeIcon.setText(route.getModeIcon());
        holder.tvDurationMain.setText(route.getDuration());
        holder.tvModeName.setText(route.getModeName());
        holder.tvArrivalTime.setText("Arrivée: " + route.getArrivalTime());
        holder.tvDistance.setText(route.getDistance());

        // Étapes détaillées
        if (route.getSteps() != null && !route.getSteps().isEmpty()) {
            holder.rvStepsTimeline.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            RouteStepsAdapter stepsAdapter = new RouteStepsAdapter(route.getSteps());
            holder.rvStepsTimeline.setAdapter(stepsAdapter);
            holder.layoutRouteDetails.setVisibility(View.VISIBLE);
        } else {
            holder.layoutRouteDetails.setVisibility(View.GONE);
        }

        // Informations supplémentaires
        String extraInfo = route.getExtraInfo();
        if (!extraInfo.isEmpty()) {
            holder.layoutExtraInfo.setVisibility(View.VISIBLE);
            holder.tvExtraInfo.setText(extraInfo);
        } else {
            holder.layoutExtraInfo.setVisibility(View.GONE);
        }

        // Prix
        if (route.getPrice() != null && !route.getPrice().isEmpty()) {
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText(route.getPrice());
        } else {
            holder.tvPrice.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRouteClick(route);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvModeIcon;
        TextView tvDurationMain;
        TextView tvModeName;
        TextView tvArrivalTime;
        TextView tvDistance;
        RecyclerView rvStepsTimeline;
        LinearLayout layoutRouteDetails;
        LinearLayout layoutExtraInfo;
        TextView tvExtraInfo;
        TextView tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModeIcon = itemView.findViewById(R.id.tvModeIcon);
            tvDurationMain = itemView.findViewById(R.id.tvDurationMain);
            tvModeName = itemView.findViewById(R.id.tvModeName);
            tvArrivalTime = itemView.findViewById(R.id.tvArrivalTime);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            rvStepsTimeline = itemView.findViewById(R.id.rvStepsTimeline);
            layoutRouteDetails = itemView.findViewById(R.id.layoutRouteDetails);
            layoutExtraInfo = itemView.findViewById(R.id.layoutExtraInfo);
            tvExtraInfo = itemView.findViewById(R.id.tvExtraInfo);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}