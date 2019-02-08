package tl.com.timemanager.fragment.actionstatistics;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tl.com.timemanager.MainActivity;
import tl.com.timemanager.R;
import tl.com.timemanager.adapter.KindOfActionItemDetailAdapter;
import tl.com.timemanager.base.BaseFragment;
import tl.com.timemanager.item.ItemAction;
import tl.com.timemanager.service.TimeService;

@SuppressLint("ValidFragment")
public class ActionStatisticsDetailFragment extends BaseFragment implements KindOfActionItemDetailAdapter.IKindOfActionItemDetail {
    private RecyclerView rcvAction;
    private TimeService service;
    private KindOfActionItemDetailAdapter actionItemAdapter;
    private int kindOfActionID;
    private RelativeLayout background;
    private TextView tvTitle;
    private ImageView imvLogo;
    private List<ItemAction> actions = new ArrayList<>();
    private int dayOfWeek;
    private int year;
    private int weekOfYear;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action_statistic_detail, container, false);
        initView(view);
        initData();
        return view;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public void setKindOfActionID(int kindOfActionID) {
        this.kindOfActionID = kindOfActionID;
    }

    public void initData() {
        actions = service.getActionsByKindOfActionId(kindOfActionID);
        background.setBackgroundResource(service.getResColorByKindOfActionId(kindOfActionID));
        tvTitle.setText(service.getKindOfActionItem(kindOfActionID).getTitle());
        imvLogo.setImageResource(service.getResImageByKindOfActionId(kindOfActionID));
    }

    public ActionStatisticsDetailFragment (TimeService service) {
        this.service = service;
    }


    private void initView(View view) {

        rcvAction = view.findViewById(R.id.rcv_actions_statistic_detail);
        rcvAction.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvAction.setHasFixedSize(true);
        actionItemAdapter = new KindOfActionItemDetailAdapter(this);
        rcvAction.setAdapter(actionItemAdapter);
        background = view.findViewById(R.id.ll_background);
        tvTitle = view.findViewById(R.id.tv_kind_of_action_title);
        imvLogo = view.findViewById(R.id.imv_logo);

    }

    @Override
    public int getCount() {
        if (actions == null)return 0;
        return actions.size();
    }

    @Override
    public ItemAction getIemAction(int pos) {
        return actions.get(pos);
    }

    @Override
    public void onBackPressed() {
        ((MainActivity)getActivity()).openActionStatisticsFragmentTwo(dayOfWeek,weekOfYear,year);
    }
}
