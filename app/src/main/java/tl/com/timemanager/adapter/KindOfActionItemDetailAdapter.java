package tl.com.timemanager.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tl.com.timemanager.R;
import tl.com.timemanager.fragment.actionstatistics.ActionStatisticsDetailFragment;
import tl.com.timemanager.item.ItemAction;

public class KindOfActionItemDetailAdapter extends RecyclerView.Adapter<KindOfActionItemDetailAdapter.ViewHolder> {

    private IKindOfActionItemDetail iKindOfActionItemDetail;

    public KindOfActionItemDetailAdapter(IKindOfActionItemDetail iKindOfActionItemDetail) {
        this.iKindOfActionItemDetail = iKindOfActionItemDetail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_kind_of_action_detail, parent, false);
        KindOfActionItemDetailAdapter.ViewHolder viewHolder = new KindOfActionItemDetailAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemAction action = iKindOfActionItemDetail.getIemAction(position);
        if (action.getDayOfWeek() == 0) {
            holder.tvDayOfWeek.setText("CN : ");
        } else {
            holder.tvDayOfWeek.setText("T" + (action.getDayOfWeek() + 1) + " : ");
        }
        holder.tvTitle.setText(action.getTitle());
        holder.tvTime.setText(action.getHourOfDay() + " h - " + (action.getHourOfDay() + action.getTimeDoIt()) + " h");
        if (action.isComplete()) {
            holder.imvComplete.setImageResource(R.drawable.ic_complete_24dp);
        } else {
            holder.imvComplete.setImageResource(R.drawable.ic_close_black_24dp);
        }

    }

    @Override
    public int getItemCount() {
        return iKindOfActionItemDetail.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDayOfWeek;
        private TextView tvTitle;
        private TextView tvTime;
        private ImageView imvComplete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            imvComplete = itemView.findViewById(R.id.imv_complete);
        }
    }

    public interface IKindOfActionItemDetail {
        int getCount();

        ItemAction getIemAction(int pos);
    }
}
