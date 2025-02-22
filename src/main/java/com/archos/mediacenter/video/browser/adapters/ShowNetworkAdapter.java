package com.archos.mediacenter.video.browser.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.archos.mediacenter.video.R;
import com.bumptech.glide.Glide;
import java.util.List;

public class ShowNetworkAdapter extends RecyclerView.Adapter<ShowNetworkAdapter.ShowNetworkViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(String item);
    }
    private List<String> NetworkLogoPaths;
    private String path;
    private OnItemClickListener listener;
    public ShowNetworkAdapter(List<String> NetworkLogoPaths, OnItemClickListener listener) {
        this.NetworkLogoPaths = (List<String>) NetworkLogoPaths;
        this.listener = listener;
    }
    @Override
    public ShowNetworkAdapter.ShowNetworkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.logo, parent, false);
        ShowNetworkViewHolder vh = new ShowNetworkViewHolder(v);
        return vh;
    }
    @Override
    public void onBindViewHolder( ShowNetworkAdapter.ShowNetworkViewHolder vh, int position) {
        path = NetworkLogoPaths.get(position);
        Glide.with(vh.itemView.getContext()).load(path).into(vh.logoImage);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
    @Override
    public int getItemCount() {
        return NetworkLogoPaths.size();
    }
    public class ShowNetworkViewHolder extends RecyclerView.ViewHolder {
        protected ImageView logoImage;

        public ShowNetworkViewHolder(View itemView) {
            super(itemView);
            logoImage = itemView.findViewById(R.id.network_logo);
        }
    }
}