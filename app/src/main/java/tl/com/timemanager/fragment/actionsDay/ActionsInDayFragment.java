package tl.com.timemanager.fragment.actionsDay;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tl.com.timemanager.item.ItemAction;
import tl.com.timemanager.R;
import tl.com.timemanager.adapter.ActionItemAdapter;
import tl.com.timemanager.base.BaseFragment;
import tl.com.timemanager.dialog.calendar.BaseCalendarDialog;
import tl.com.timemanager.dialog.calendar.CalendarActionInDayDialog;
import tl.com.timemanager.dialog.insert.BaseInsertDialog;
import tl.com.timemanager.dialog.insert.InsertActionsInDayDialog;
import tl.com.timemanager.dialog.seen.SeenActionsInDayDialog;
import tl.com.timemanager.service.TimeService;

@SuppressLint("ValidFragment")
public class ActionsInDayFragment extends BaseFragment implements ActionItemAdapter.IActionItem, BaseInsertDialog.IDataChangedListener {

    private RecyclerView rcvAction;
    private TimeService service;
    private ActionItemAdapter actionItemAdapter;
    // ngày tháng năm của các hoạt động hiển thị
    private int dayOfWeek;
    private int weekOfYear;
    private int year;
    private TextView tvMess;

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actions_in_one_day, container, false);
        init(view);
        return view;
    }

    @SuppressLint("ValidFragment")
    public ActionsInDayFragment(TimeService service, int dayOfWeek, int weekOfYear, int year) {
        this.service = service;
        this.dayOfWeek = dayOfWeek;
        this.weekOfYear = weekOfYear;
        this.year = year;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public void setYear(int year) {
        this.year = year;
    }


    private void init(View view) {

        rcvAction = view.findViewById(R.id.rcv_actions);
        rcvAction.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvAction.setHasFixedSize(true);
        actionItemAdapter = new ActionItemAdapter(this);
        rcvAction.setAdapter(actionItemAdapter);
        tvMess = view.findViewById(R.id.tv_message);


    }

    @Override
    public int getCount() {
        if (service.getActionsInDay(dayOfWeek) == null) return 0;
        if (service.getActionsInDay(dayOfWeek).size() == 0) {
            tvMess.setVisibility(View.VISIBLE);
        } else {
            tvMess.setVisibility(View.GONE);
        }
        return service.getActionsInDay(dayOfWeek).size();
    }

    @Override
    public ItemAction getItemAction(int position) {
        return service.getActionsInDay(dayOfWeek).get(position);
    }

    @Override
    public void onClickItem(int position) {
        displaySeenActionsInDayDialog(dayOfWeek, position);
    }

    @Override
    public void setCompleteForAction(int adapterPosition) {
        service.setCompleteForAction(dayOfWeek, adapterPosition);
    }

    @Override
    public int getResImageByKindOfActionID(int kindOfActionID) {
        return service.getResImageByKindOfActionId(kindOfActionID);
    }

    @Override
    public int getResBackgroundByKindOfActionID(int kindOfActionID) {
        return service.getResBackgroundByKindOfActionId(kindOfActionID);
    }

//    @Override
//    public String getTitleByKindOfActionId(int idKindOfAction) {
//        return service.getTitleByKindOfActionId(idKindOfAction);
//    }

//    @Override
//    public void removeItemAction(int position) {
//        service.getActionsInDay(dayOfWeek).remove(position);
//    }

//    @Override
//    public void deleteAction(int day,int position) {
//
//        service.deleteActionByPositionItemAction(day,position);
//    }


    /**
     * Mở dialog lịch để xem hoạt động các ngày khác nhau trong lịch
     */
    public void displayCalendarDialog() {
        CalendarActionInDayDialog dialog = new CalendarActionInDayDialog(getActivity());
        dialog.setIDateChangedListener((BaseCalendarDialog.IDateChangedListener) getParentFragment());
        dialog.setDayOfWeek(dayOfWeek);
        dialog.setWeekOfYear(weekOfYear);
        dialog.setYear(year);
        dialog.initCalendar();
        dialog.show();
    }


    public void displayInsertActionDialog() {
        InsertActionsInDayDialog dialog = new InsertActionsInDayDialog(getActivity());
        dialog.setItemAction(createNewItemAction());
        dialog.setDayOfWeek(dayOfWeek);
        dialog.setService(service);
        dialog.initView();
        dialog.setiListener(this);
        dialog.show();
    }

    private ItemAction createNewItemAction() {
        int size = service.getCountActionsInDay(dayOfWeek);
        ItemAction action = new ItemAction();
        action.setTimeDoIt(1);
        action.setDayOfWeek(dayOfWeek);
        action.setWeekOfYear(weekOfYear);
        action.setYear(year);
        if (size > 0) {
            int time = service.setTimeForAction(dayOfWeek, 1);
            action.setHourOfDay(time);
        }
        return action;
    }

    /**
     * mở ra dialog để xem hoạt động
     *
     * @param day      ngày
     * @param position vị trí của hoạt động
     */
    private void displaySeenActionsInDayDialog(int day, int position) {
        SeenActionsInDayDialog dialog = new SeenActionsInDayDialog(getActivity());
        dialog.setPositionItemAction(position);
        dialog.setDayOfWeek(day);
        dialog.setService(service);
        dialog.initView();
        dialog.setiListener(this);
        dialog.show();
    }


    @Override
    public void changedDataItem() {

    }

    /**
     * cập nhật lại hoạt động trong ngày
     */
    @Override
    public void changedActionItem() {
        if (actionItemAdapter != null) {
            actionItemAdapter.notifyDataSetChanged();
        }
    }
}
