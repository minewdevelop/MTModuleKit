package com.minew.mtmoduledemo;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.minew.modulekit.MTModule;

import java.util.List;


public class RecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<MTModule> mData;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        viewHolder = new ViewHolder(View.inflate(parent.getContext(), R.layout.item, null));
        return viewHolder;
    }

    public MTModule getData(int position) {
        return mData.get(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }


    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setDataAndUi(mData.get(position));

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setData(List<MTModule> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView data;
        TextView rssi;

        public ViewHolder(View itemView) {
            super(itemView);
            data = (TextView) itemView.findViewById(R.id.data);
            rssi = (TextView) itemView.findViewById(R.id.rssi);
        }

        public void setDataAndUi(MTModule mtModule) {
            data.setText(mtModule.getMacAddress());
            rssi.setText("" + mtModule.getRssi());
        }

    }
}
