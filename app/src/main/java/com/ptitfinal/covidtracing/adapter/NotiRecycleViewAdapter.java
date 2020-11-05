package com.ptitfinal.covidtracing.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptitfinal.covidtracing.R;
import com.ptitfinal.covidtracing.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotiRecycleViewAdapter extends RecyclerView.Adapter<NotiRecycleViewAdapter.ViewHolder> {
    private List<Notification> listNoti = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mListener;

    public NotiRecycleViewAdapter(Context context, List<Notification> list) {
        this.listNoti = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = listNoti.get(position);
        holder.container.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_animation));
        holder.tvContentNoti.setText(notification.getContent());
        holder.tvTitleNoti.setText(notification.getTitle());
    }
    public void setClasses(List<Notification> listNoti) {
        if(this.listNoti.size()==0)
        {
            this.listNoti = listNoti;
            notifyDataSetChanged();
        }

    }
    @Override
    public int getItemCount() {
        return listNoti.size();
    }

    private interface OnItemClickListener {
        public void onItemClick(int position, String ID);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvContentNoti;
        private TextView tvTitleNoti;
        private RelativeLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position, listNoti.get(position).getID());
                        }
                    }
                }
            });
            tvContentNoti = itemView.findViewById(R.id.m_content_noti);
            container = itemView.findViewById(R.id.container_row);
            tvTitleNoti = itemView.findViewById(R.id.m_title);
        }
    }
}
