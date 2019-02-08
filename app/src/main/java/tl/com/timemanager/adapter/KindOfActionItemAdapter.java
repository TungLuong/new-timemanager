package tl.com.timemanager.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tl.com.timemanager.R;
import tl.com.timemanager.item.ItemKindOfAction;

public class KindOfActionItemAdapter extends RecyclerView.Adapter<KindOfActionItemAdapter.ViewHolder> {

    private IKindOfActionItem iKindOfActionItem;

    public KindOfActionItemAdapter(IKindOfActionItem iKindOfActionItem) {
        this.iKindOfActionItem = iKindOfActionItem;
    }

    @NonNull
    @Override
    public KindOfActionItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_kind_of_action, parent, false);
        final KindOfActionItemAdapter.ViewHolder viewHolder = new KindOfActionItemAdapter.ViewHolder(itemView);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iKindOfActionItem.onClickItem(viewHolder.getAdapterPosition());
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemKindOfAction item = iKindOfActionItem.getItem(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvCountDone.setText(iKindOfActionItem.getCountDone(position));
        holder.imvLogo.setImageResource(item.getIdResImage());
        holder.background.setBackgroundResource(item.getIdResColor());
    }

    @Override
    public int getItemCount() {
        return iKindOfActionItem.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private TextView tvCountDone;
        private ImageView imvLogo;
        public LinearLayout background;
        private ImageView imv_detail;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_kind_of_action_title);
            tvCountDone = itemView.findViewById(R.id.tv_action_done);
            imvLogo = itemView.findViewById(R.id.imv_logo);
            background = itemView.findViewById(R.id.background);
            imv_detail = itemView.findViewById(R.id.imv_detail);
        }
    }

    public interface IKindOfActionItem {

        int getCount();

        ItemKindOfAction getItem(int position);

        void onClickItem(int position);

        String getCountDone(int position);

    }
}
