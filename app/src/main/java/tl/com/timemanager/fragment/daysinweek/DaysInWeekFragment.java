package tl.com.timemanager.fragment.daysinweek;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tl.com.timemanager.R;
import tl.com.timemanager.adapter.DaysInWeekAdapter;
import tl.com.timemanager.base.BaseFragment;
import tl.com.timemanager.dialog.calendar.BaseCalendarDialog;
import tl.com.timemanager.fragment.actionsDay.ActionsInDayFragment;
import tl.com.timemanager.service.TimeService;

public class DaysInWeekFragment extends BaseFragment implements BaseCalendarDialog.IDateChangedListener, View.OnClickListener, TimeService.IUpdateUI {

    private static final String TITLE_WEEK_OF_YEAR = "Hoạt động trong tuần ";
    private TabLayout tab;
    private ViewPager pager;
    private DaysInWeekAdapter adapter;
    private TimeService timeService;
    private TextView tvWeekOfYear;
    private int weekOfYear;
    private int year;
    // các nút thêm hoạt động, thêm tuỳ chọn, đồng bộ, mở lịch
    private FloatingActionButton fabInsert, fabMore, fabSync, fabOpenCalendar;
    private boolean fabMoreIsOpen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_days_in_week, container, false);
        initView(view);
        return view;
    }

    /**
     * xét service và khởi tạo service
     *
     * @param timeService
     */
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
        updateTimeService();
    }

    public void updateTimeService() {
        timeService.setActionsInCurrentWeek();
        timeService.setiUpdateUI(this);
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    private void initView(View view) {


        tvWeekOfYear = view.findViewById(R.id.tv_weekOfYear);
        tab = view.findViewById(R.id.tab);
        pager = view.findViewById(R.id.viewpager);

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        year = calendar.get(Calendar.YEAR);

        tvWeekOfYear.setText(TITLE_WEEK_OF_YEAR + weekOfYear);

        adapter = new DaysInWeekAdapter(getChildFragmentManager(), timeService, weekOfYear, year);
        pager.setAdapter(adapter);
        tab.setupWithViewPager(pager);
        setCurrentFragment(dayOfWeek);

        fabInsert = view.findViewById(R.id.fab_insert);
        fabOpenCalendar = view.findViewById(R.id.fab_open_calendar);
        fabMore = view.findViewById(R.id.fab_more);
        fabSync = view.findViewById(R.id.fab_sync);

        fabMore.setOnClickListener(this);
        fabInsert.setOnClickListener(this);
        fabOpenCalendar.setOnClickListener(this);
        fabSync.setOnClickListener(this);


        updateFloatingActionButton();
    }

    /**
     * xét fragment hiện tại
     *
     * @param dayOfWeek ngày
     */
    @Override
    public void setCurrentItemFragment(int dayOfWeek) {

        setCurrentFragment(dayOfWeek);
    }

    /*
 Cập nhật lại tình trạng các nút
  */
    public void updateFloatingActionButton() {
        if (fabMoreIsOpen) {
            fabMore.setImageResource(R.drawable.ic_close_white_24dp);
            fabInsert.setClickable(true);
            fabInsert.setVisibility(View.VISIBLE);
            fabOpenCalendar.setClickable(true);
            fabOpenCalendar.setVisibility(View.VISIBLE);
            fabSync.setClickable(true);
            fabSync.setVisibility(View.VISIBLE);
        } else {
            fabMore.setImageResource(R.drawable.ic_more_horiz_white_24dp);
            fabInsert.setClickable(false);
            fabInsert.setVisibility(View.INVISIBLE);
            fabOpenCalendar.setClickable(false);
            fabOpenCalendar.setVisibility(View.INVISIBLE);
            fabSync.setClickable(false);
            fabSync.setVisibility(View.INVISIBLE);
        }
    }

    /*
   khi nhấn vào nút
    */
    @Override
    public void onClick(View v) {
        int position = pager.getCurrentItem();
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        switch (v.getId()) {
            case R.id.fab_insert:
                // thêm hoạt động
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment.isVisible() && fragment != null && ((ActionsInDayFragment) fragment).getDayOfWeek() == position) {
                            ((ActionsInDayFragment) fragment).displayInsertActionDialog();
                        }
                    }
                }
                break;
            case R.id.fab_open_calendar:
                // mở lịch
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment.isVisible() && fragment != null && ((ActionsInDayFragment) fragment).getDayOfWeek() == position) {
                            ((ActionsInDayFragment) fragment).displayCalendarDialog();
                        }
                    }
                }
                break;
            case R.id.fab_more:
                // mở hoặc đóng hiển thị các nút
                fabMoreIsOpen = !fabMoreIsOpen;
                updateFloatingActionButton();
                break;
            case R.id.fab_sync:
                int dayOfWeek = position;
                // đồng bộ dữ liệu hiển thị
                timeService.updateActionsInWeekFromTimeTable(dayOfWeek);
                timeService.checkActionsDone(dayOfWeek);
                timeService.updateActionsInWeek(weekOfYear,year);
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment.isVisible() && fragment != null ) {
                            ((ActionsInDayFragment) fragment).changedActionItem();
                        }
                    }
                }
                Toast.makeText(getActivity(),"Đồng bộ thành công",Toast.LENGTH_LONG).show();
                break;
        }

    }

    /**
     * cập nhât lại hoạt động trong tuần
     *
     * @param dayOfWeek
     * @param weekOfYear
     * @param year
     */
    @Override
    public void updateActionsInWeek(int dayOfWeek, int weekOfYear, int year) {
        timeService.updateActionsInWeek(weekOfYear, year);
        tvWeekOfYear.setText(TITLE_WEEK_OF_YEAR + weekOfYear);
        this.year = year;
        this.weekOfYear = weekOfYear;
        adapter.setWeekOfYear(weekOfYear);
        adapter.setYear(year);
        changedDateInChildFragment();
        adapter.notifyDataSetChanged();
    }

//    public void notifyDataSetChangedInAdapter(){
//        adapter.notifyDataSetChanged();
//    }

    @Override
    public void updateActionStatisticFragment(int day) {

    }

    /**
     * thay đổi ngày tại các fragment con
     */
    public void changedDateInChildFragment() {

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                ((ActionsInDayFragment) fragment).setYear(year);
                ((ActionsInDayFragment) fragment).setWeekOfYear(weekOfYear);
                ((ActionsInDayFragment) fragment).changedActionItem();
            }
        }
    }

    /**
     * mở fragment hoạt động trong ngày
     *
     * @param dayOfWeek ngày
     */
    public void setCurrentFragment(int dayOfWeek) {
        pager.setCurrentItem(dayOfWeek);
    }


    /**
     * cập nhật lại giao diện
     */
    @Override
    public void updateUI() {
        changedDateInChildFragment();
    }


}
