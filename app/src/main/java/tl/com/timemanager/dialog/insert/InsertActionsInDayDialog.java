package tl.com.timemanager.dialog.insert;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import tl.com.timemanager.item.ItemAction;
import tl.com.timemanager.R;

import static tl.com.timemanager.Constant.AMUSING_ACTION;
import static tl.com.timemanager.Constant.AT_HOME_ACTION;
import static tl.com.timemanager.Constant.FREE_TIME;
import static tl.com.timemanager.Constant.OUTSIDE_ACTION;
import static tl.com.timemanager.Constant.RELAX_ACTION;
import static tl.com.timemanager.Constant.TIME_MAX;
import static tl.com.timemanager.Constant.TIME_MIN;

public class InsertActionsInDayDialog extends BaseInsertDialog {

    // Ngày trong tuần
    private int dayOfWeek;

    private ItemAction itemAction;

    public InsertActionsInDayDialog(@NonNull Context context) {
        super(context);
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * Nạp dữ liệu hiển thị
     */
    @Override
    protected void setData() {
        // Nếu hoạt động đã có tiêu đề -> sửa hoạt động chứ kp thêm mới
        if (itemAction.getTitle() != null) {
            isModify = true;
            edtTitleAction.setText(itemAction.getTitle());
//            spin_time.setSelection(itemAction.getTimeDoIt() - 1);
            count = itemAction.getTimeDoIt();
            spin_action.setSelection(itemAction.getAction());

            if (itemAction.isNotification()) {
                swNotification.setChecked(true);
            } else swNotification.setChecked(false);

            if (itemAction.isDoNotDisturb()) {
                swDoNotDisturb.setChecked(true);
            } else swDoNotDisturb.setChecked(false);

            ivAction.setImageResource(service.getResImageByKindOfActionId(itemAction.getAction()));
//            switch (itemAction.getAction()) {
//                case FREE_TIME:
//                    ivAction.setImageResource(R.drawable.no_action);
//                    break;
//                case OUTSIDE_ACTION:
//                    ivAction.setImageResource(R.drawable.school);
//                    break;
//                case AT_HOME_ACTION:
//                    ivAction.setImageResource(R.drawable.homework);
//                    break;
//                case AMUSING_ACTION:
//                    ivAction.setImageResource(R.drawable.giaitri);
//                    break;
//                case RELAX_ACTION:
//                    ivAction.setImageResource(R.drawable.sleep);
//                    break;
//            }
        } else isModify = false;

        edtTimeStart.setText(itemAction.getHourOfDay() + "");
        tvTimeDoIt.setText(count + " giờ ");
        checkInvalidTimeStart();

    }

    /**
     * kiểm tra trùng thời gian
     */
    protected void checkSameTime() {
        List<ItemAction> itemActions = service.getActionsInWeek().get(dayOfWeek);
        List<ItemAction> actions = new ArrayList<>();
        for (ItemAction action : itemActions) {
            actions.add(action);
        }
        actions.remove(itemAction);
        try {
            int timeStart = Integer.valueOf(String.valueOf(edtTimeStart.getText() + ""));
            int timeEnd = timeStart + count;
            if (timeEnd > TIME_MAX + 1) {
                tvErrorTime.setVisibility(View.VISIBLE);
                return;
            }
            if (actions.size() > 0) {
                for (ItemAction action : actions) {
                    int start = action.getHourOfDay();
                    int end = action.getHourOfDay() + action.getTimeDoIt();
                    if (end <= timeStart || start >= timeEnd) {
                        tvErrorTime.setVisibility(View.GONE);
                    } else {
                        tvErrorTime.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            }
            tvErrorTime.setVisibility(View.GONE);
        } catch (Exception e) {
            tvErrorTime.setVisibility(View.VISIBLE);
        }

    }

    /**
     * cập nhật dữ liệu
     */
    protected void updateData() {
        String title = String.valueOf(edtTitleAction.getText());
        int time = Integer.valueOf(String.valueOf(edtTimeStart.getText() + ""));
        ItemAction action = new ItemAction();
        action.setId(itemAction.getId());
        action.setDayOfWeek(itemAction.getDayOfWeek());
        action.setYear(itemAction.getYear());
        action.setWeekOfYear(itemAction.getWeekOfYear());
        action.setTitle(title);
        action.setAction(kindOfAction);
        action.setHourOfDay(time);
        action.setTimeDoIt(count);
        action.setNotification(swNotification.isChecked());
        action.setDoNotDisturb(swDoNotDisturb.isChecked());
        action.setComplete(itemAction.isComplete());
        service.updateItemAction(action);

    }

    /**
     * kiểm tra thời gian bắt đầu có phù hợp không
     */
    protected void checkInvalidTimeStart() {
        if (edtTimeStart.getText().toString().trim().length() > 0 && edtTimeStart.getText().toString().trim().length() < 3) {
            int time = Integer.valueOf(edtTimeStart.getText().toString());
            if (time >= TIME_MIN && time <= TIME_MAX) {
                tvErrorTimeStart.setVisibility(View.GONE);
                checkSameTime();
            } else {
                tvErrorTimeStart.setVisibility(View.VISIBLE);
            }
        } else {
            tvErrorTimeStart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.btn_save:
                checkInvalidTitle();
                if (tvErrorTime.getVisibility() == View.GONE
                        && tvErrorTitle.getVisibility() == View.GONE
                        && tvErrorTimeStart.getVisibility() == View.GONE) {
                    if (!isModify) {
                        service.insertItemAction(dayOfWeek, itemAction);
                    }
                    updateData();

                    service.sortActionByTime(dayOfWeek);
                    iListener.changedActionItem();
                    dismiss();
                }
                break;
            case R.id.btn_minus_time:
                if (count > 1) {
                    count--;
                }
                tvTimeDoIt.setText(count + " giờ ");
                checkSameTime();
                break;
            case R.id.btn_plus_time:
                count++;
                tvTimeDoIt.setText(count + " giờ ");
                checkSameTime();
                break;
        }
    }


    public void setItemAction(ItemAction itemAction) {
        this.itemAction = itemAction;
    }
}
