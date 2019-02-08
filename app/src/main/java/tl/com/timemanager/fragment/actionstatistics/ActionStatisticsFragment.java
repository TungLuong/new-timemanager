package tl.com.timemanager.fragment.actionstatistics;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tl.com.timemanager.MainActivity;
import tl.com.timemanager.adapter.KindOfActionItemAdapter;
import tl.com.timemanager.fragment.actionsDay.ActionsInDayFragment;
import tl.com.timemanager.item.ItemAction;
import tl.com.timemanager.R;
import tl.com.timemanager.base.BaseFragment;
import tl.com.timemanager.dialog.calendar.BaseCalendarDialog;
import tl.com.timemanager.dialog.calendar.CalendarActionStatisticsDialog;
import tl.com.timemanager.item.ItemKindOfAction;
import tl.com.timemanager.service.TimeService;

@SuppressLint("ValidFragment")
public class ActionStatisticsFragment extends BaseFragment implements View.OnClickListener, BaseCalendarDialog.IDateChangedListener, KindOfActionItemAdapter.IKindOfActionItem {
    // biểu đồ tròn thể hiện tỉ lệ các hoạt động
    private TimeService service;
    private KindOfActionItemAdapter adapter;
    private RecyclerView rcvKindOfAction;
    private List<CountHour> countHour;
    // ngày tháng năm của hoạt động đang xét
    private int dayOfWeek;
    private int year;
    private int weekOfYear;
    private int countKindOfAction;
    // nút mở ra lịch để xem thống kê các ngày khác
    private FloatingActionButton btOpenCalendar;
    private TextView tvWeekOfYear;

    @SuppressLint("ValidFragment")
    public ActionStatisticsFragment(TimeService timeService) {
        super();
        this.service = timeService;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action_statistics, container, false);
        initView(view);
        initData();
        return view;
    }


    private void initView(View view) {
        btOpenCalendar = view.findViewById(R.id.fab_open_calendar);
        btOpenCalendar.setOnClickListener(this);
        tvWeekOfYear = view.findViewById(R.id.tv_week_of_year);
        rcvKindOfAction = view.findViewById(R.id.rcv_kind_of_action);
        rcvKindOfAction.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvKindOfAction.setHasFixedSize(true);
        adapter = new KindOfActionItemAdapter(this);
        rcvKindOfAction.setAdapter(adapter);

    }

    private void initData() {
        tvWeekOfYear.setText("Thống kê tuần " + weekOfYear);
        countHour = new ArrayList<>();
        countKindOfAction = service.getCountItemKindOfAction();
        for (int i = 0; i < countKindOfAction; i++) {
            countHour.add(new CountHour(0, 0));
        }
        List<ItemAction> actions = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            List<ItemAction> actionsInDay = service.getActionsInDay(day);
            for (ItemAction action : actionsInDay) {
                actions.add(action);
            }
        }
        for (ItemAction action : actions) {
            int kindOfAction = action.getAction();
            countHour.get(kindOfAction).setCountHourTotal(countHour.get(kindOfAction).getCountHourTotal() + action.getTimeDoIt());
            if (action.isComplete()) {
                countHour.get(kindOfAction).setCountHourComplete(countHour.get(kindOfAction).getCountHourComplete() + action.getTimeDoIt());
            }
        }

    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    /**
     * mở ra dialog lịch để xem thống kê các ngày khác
     */
    private void displayCalendarDialog() {
        CalendarActionStatisticsDialog dialog = new CalendarActionStatisticsDialog(getActivity());
        dialog.setIDateChangedListener(this);
        dialog.setDayOfWeek(dayOfWeek);
        dialog.setWeekOfYear(weekOfYear);
        dialog.setYear(year);
        dialog.initCalendar();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_open_calendar:
                displayCalendarDialog();
                break;
        }
    }

    @Override
    public void setCurrentItemFragment(int day) {

    }

    /**
     * cập nhật lại hoạt động trong tuần
     *
     * @param dayOfWeek
     * @param weekOfYear
     * @param year
     */
    @Override
    public void updateActionsInWeek(int dayOfWeek, int weekOfYear, int year) {
        service.updateActionsInWeek(weekOfYear, year);
        this.dayOfWeek = dayOfWeek;
        this.weekOfYear = weekOfYear;
        this.year = year;
    }

    /**
     * cập nhật lại biểu đồ thống kê
     *
     * @param day
     */
    @Override
    public void updateActionStatisticFragment(int day) {
        dayOfWeek = day;
        initData();
        adapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return service.getCountItemKindOfAction();
    }

    @Override
    public ItemKindOfAction getItem(int position) {
        return service.getKindOfActionItem(position);
    }

    @Override
    public void onClickItem(int position) {
        ((MainActivity) getActivity()).openActionStatisticsDetailFragment(position,dayOfWeek,weekOfYear,year);
    }


    @Override
    public String getCountDone(int position) {
        return countHour.get(position).getCountHourComplete() + " / " + countHour.get(position).getCountHourTotal();
    }

    private class CountHour {
        private int countHourComplete;
        private int countHourTotal;

        public CountHour(int countHourComplete, int countHourTotal) {
            this.countHourComplete = countHourComplete;
            this.countHourTotal = countHourTotal;
        }

        public int getCountHourComplete() {
            return countHourComplete;
        }

        public void setCountHourComplete(int countHourComplete) {
            this.countHourComplete = countHourComplete;
        }

        public int getCountHourTotal() {
            return countHourTotal;
        }

        public void setCountHourTotal(int countHourTotal) {
            this.countHourTotal = countHourTotal;
        }
    }
}
