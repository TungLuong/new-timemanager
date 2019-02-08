package tl.com.timemanager.dialog.calendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;

import java.util.Calendar;

import tl.com.timemanager.R;

public class BaseCalendarDialog extends BottomSheetDialog implements View.OnClickListener, CalendarView.OnDateChangeListener {

    protected CalendarView calendarView;
    //nut close
    protected ImageView ivClose;
    protected IDateChangedListener iDateChangedListener;
    // ngày, tuần, năm
    protected int dayOfWeek;
    protected int weekOfYear;
    protected int year;


    public BaseCalendarDialog(@NonNull Context context) {
        super(context, R.style.Theme_Design_Light_BottomSheetDialog);
        setContentView(R.layout.dialog_calender);
        initView();
    }

    public void setIDateChangedListener(IDateChangedListener iDateChangedListener) {
        this.iDateChangedListener = iDateChangedListener;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public void setYear(int year) {
        this.year = year;
    }

    protected void initView() {
        calendarView = findViewById(R.id.calendar);
        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(this);
        calendarView.setOnDateChangeListener(this);

    }

    public void initCalendar() {
        int day = dayOfWeek + 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        calendar.set(Calendar.DAY_OF_WEEK, day);
        long milliTime = calendar.getTimeInMillis();
        calendarView.setDate(milliTime, true, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
    }

    public interface IDateChangedListener {
        void setCurrentItemFragment(int day);

        void updateActionsInWeek(int dayOfWeek, int weekOfYear, int year);

        void updateActionStatisticFragment(int day);
    }
}
