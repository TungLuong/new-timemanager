package tl.com.timemanager.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import tl.com.timemanager.MainActivity;
import tl.com.timemanager.R;
import tl.com.timemanager.database.Data;
import tl.com.timemanager.item.ItemAction;
import tl.com.timemanager.item.ItemDataInTimeTable;
import tl.com.timemanager.MyBinder;
import tl.com.timemanager.item.ItemKindOfAction;

import static tl.com.timemanager.Constant.AMUSING_ACTION;
import static tl.com.timemanager.Constant.AT_HOME_ACTION;
import static tl.com.timemanager.Constant.CHANNEL_ID_NOTIFICATION;
import static tl.com.timemanager.Constant.CHANNEL_ID_RUNNING_IN_BACKGROUND;
import static tl.com.timemanager.Constant.COUNT_DAY;
import static tl.com.timemanager.Constant.COUNT_TIME;
import static tl.com.timemanager.Constant.DELAY_MINUTE;
import static tl.com.timemanager.Constant.FREE_TIME;
import static tl.com.timemanager.Constant.OUTSIDE_ACTION;
import static tl.com.timemanager.Constant.RELAX_ACTION;
import static tl.com.timemanager.Constant.START_ALARM;
import static tl.com.timemanager.Constant.TIME_MAX;
import static tl.com.timemanager.Constant.TIME_MIN;

public class TimeService extends Service {

    private static final int NOTIFICATION_ACTION_ID = 3;
    String TAG = TimeService.class.getSimpleName();
    private static final int NOTIFICATION_RUNNING_ID = 4;
    private NotificationManager notificationManager;

    // quản lí thông báo
    //private NotificationManager notificationManager;
    // danh sách item data
    private List<ItemDataInTimeTable> itemDatas = new ArrayList<>();
    // danh sách các hoạt động
    private List<List<ItemAction>> actionsInWeek;
    // danh sách hoạt động hiện tại
    private List<ItemAction> currAction;

    //danh sách loại hoạt động
    private List<ItemKindOfAction> kindOfActions = new ArrayList<>();
    // quản lí báo thức
    private AlarmManager alarmManager;
    private int currDay = 0;
    private int weekOfYear = 0;
    private int year = 0;

    private MyBroadcastReceiver broadcast;
    private RemoteViews remoteViews;
    //    private RealmAsyncTask transaction;
    // dữ liệu
    private Data data;
    private IUpdateUI iUpdateUI;

    //private boolean beenRepeated;
    // biến đêm
    private PendingIntent pIntent;
    private PendingIntent pBntStartAction;
    private PendingIntent pBntEndAction;
    private Intent intent;
    private int idCurrAction = -1;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationRunningInBackground();
        }
        data = new Data();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        registerBroadcast();
        initIntent();
        createNotificationManager();
        initData();
        setAlarm((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) % 24, 0);
        checkNotificationAndDND();
    }

    private void createNotificationRunningInBackground() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_running_background_black_24dp)
                .setContentText("Ứng dụng chạy ngầm")
                .setChannelId(CHANNEL_ID_RUNNING_IN_BACKGROUND);
//                        .setCustomBigContentView(remoteViews);
        startForeground(NOTIFICATION_RUNNING_ID, builder.build());
    }

    private void initIntent() {
        Intent intent = new Intent(this, TimeService.class);
        intent.setAction(START_ALARM);
        pIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setiUpdateUI(IUpdateUI iUpdateUI) {
        this.iUpdateUI = iUpdateUI;
    }

    public List<List<ItemAction>> getActionsInWeek() {
        return actionsInWeek;
    }

    public List<ItemAction> getActionsInDay(int day) {
        return actionsInWeek.get(day);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initData() {
        List<ItemDataInTimeTable> list = data.getAllItemData();
        if (list.size() == 0) {
            for (int i = 0; i < COUNT_TIME; i++) {
                for (int j = 0; j < COUNT_DAY; j++) {
                    ItemDataInTimeTable itemData = new ItemDataInTimeTable(j, (i + TIME_MIN));
                    data.insertItemData(itemData);
                    Log.d(TAG, "time =    " + itemData.getHourOfDay());
                }
            }
        }
        list = data.getAllItemData();
        itemDatas.addAll(list);
        setActionsInCurrentWeek();
        //Calendar calendar = Calendar.getInstance();
        // checkActionsDone();
        setCurrAction();
        //setAlarm((calendar.get(Calendar.HOUR_OF_DAY)) % 24, 0);
        kindOfActions = data.getAllKindOfAction();
        if (kindOfActions.size() == 0) {
            ItemKindOfAction freeTime = new ItemKindOfAction(FREE_TIME, "Hoạt động tự do", R.drawable.free_time, R.drawable.background_free_time, R.color.colorFreeTime);
            ItemKindOfAction homeWork = new ItemKindOfAction(AT_HOME_ACTION, "Hoạt động tại nhà", R.drawable.homework, R.drawable.background_action_at_home, R.color.colorHomework);
            ItemKindOfAction outSide = new ItemKindOfAction(OUTSIDE_ACTION, "Hoạt động bên ngoài", R.drawable.school, R.drawable.background_action_outside, R.color.colorOutSideAction);
            ItemKindOfAction amusing = new ItemKindOfAction(AMUSING_ACTION, "Hoạt động giải trí", R.drawable.giaitri, R.drawable.background_action_entertainment, R.color.colorEntertainment);
            ItemKindOfAction relax = new ItemKindOfAction(RELAX_ACTION, "Hoạt động nghỉ ngơi", R.drawable.sleep, R.drawable.background_action_relax, R.color.colorRelax);
            data.insertKindOfAction(freeTime);
            data.insertKindOfAction(homeWork);
            data.insertKindOfAction(outSide);
            data.insertKindOfAction(amusing);
            data.insertKindOfAction(relax);
            kindOfActions = data.getAllKindOfAction();
        }
    }

    /**
     * xét các danh sách theo tuần hiện tại
     */
    public void setActionsInCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        updateActionsInWeek(weekOfYear, year);

    }

    /**
     * xét thuộc tính modify cho item data
     *
     * @param isModify
     * @param item
     */
    public void setModifyForItemData(Boolean isModify, ItemDataInTimeTable item) {
        data.setModifyForItemData(isModify, item);
    }

    /**
     * cập nhật danh sách các hoạt động theo tuần bất kì
     *
     * @param weekOfYear tuần cập nhật
     * @param year       năm
     */
    public void updateActionsInWeek(int weekOfYear, int year) {
        this.weekOfYear = weekOfYear;
        this.year = year;
        List<ItemAction> actions = data.getActionsInWeek(weekOfYear, year);


        // Tạo dữ liệu mặc định
//        if (actions.size() == 0) {
//            ItemAction action = new ItemAction();
//            action.setDayOfWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
//            action.setYear(year);
//            action.setWeekOfYear(weekOfYear);
//            action.setTitle("Hoạt động 1 ");
//            insertItemAction(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1, action);
//
//            actions = data.getActionsInWeek(weekOfYear, year);
//        }

        actionsInWeek = new ArrayList<>();
        for (int i = 0; i < COUNT_DAY; i++) {
            actionsInWeek.add(new ArrayList<ItemAction>());
        }
        if (actions.size() != 0) {
            for (ItemAction action : actions) {
                if (action.getTitle() != null) {
                    int dayOfWeek = action.getDayOfWeek();
                    actionsInWeek.get(dayOfWeek).add(action);
                } else {
                    data.deleteItemAction(action);
                }
            }
            for (int i = 0; i < COUNT_DAY; i++) {
                sortActionByTime(i);
            }
        }
        checkActionsDone();
    }

    /**
     * thêm hoạt động vào lưu trữ
     *
     * @param dayOfWeek
     * @param action
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void insertItemAction(int dayOfWeek, ItemAction action) {
        int id = data.insertItemAction(action);
        actionsInWeek.get(dayOfWeek).add(data.getActionFromDBById(id));
        setCurrAction();
        checkNotificationAndDND();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case START_ALARM:
                        checkActionsDone();
                        setAlarm((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) % 24, 0);
                        checkNotificationAndDND();
                        if (iUpdateUI != null) {
                            try {
                                iUpdateUI.updateUI();
                            } catch (Exception e) {
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkNotificationAndDND() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        if (currDay != day) {
            currDay = day;
            if (currDay == 0) {
                Calendar cal = Calendar.getInstance();
                //clean action in week
                updateActionsInWeek(cal.get(Calendar.WEEK_OF_YEAR), cal.get(Calendar.YEAR));
                //update action
                updateActionsInWeekFromTimeTable(currDay);
                if (iUpdateUI != null) {
                    iUpdateUI.updateActionsInWeek(currDay, cal.get(Calendar.WEEK_OF_YEAR), cal.get(Calendar.YEAR));
                }
            }
            setCurrAction();
            if (iUpdateUI != null) {
                iUpdateUI.setCurrentItemFragment(currDay);
            }
        }

        if (currAction != null && currAction.size() > 0) {
            for (int i = 0; i < currAction.size(); i++) {
                ItemAction action = currAction.get(i);
                Calendar cal = Calendar.getInstance();
                int currHour = cal.get(Calendar.HOUR_OF_DAY);
                if (currHour >= action.getHourOfDay() && currHour < action.getHourOfDay() + action.getTimeDoIt()) {
                    if (action.isDoNotDisturb()) {
                        turnOnDoNotDisturb();
                    } else {
                        turnOffDoNotDisturb();
                    }
                    int currMinute = cal.get(Calendar.MINUTE);
                    if (currHour == action.getHourOfDay() && currMinute <= DELAY_MINUTE && action.isNotification()) {
                        showNotification(action);
                    } else {
                        notificationManager.cancel(NOTIFICATION_ACTION_ID);
                    }
                    break;
                } else {
                    turnOffDoNotDisturb();
                }
            }

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Binder binder = new MyBinder(this);
        return binder;
    }

    public int getCountItemData() {
        Log.d(TAG, "size.........." + itemDatas.size() + "");
        if (itemDatas == null) return 0;
        else return itemDatas.size();
    }

    public ItemDataInTimeTable getItemDataInTimeTable(int position) {
        return itemDatas.get(position);
    }

    public int getCountActionsInDay(int day) {
        if (actionsInWeek.get(day) == null) return 0;
        return actionsInWeek.get(day).size();
    }

    public ItemAction getItemAction(int day, int position) {
        return actionsInWeek.get(day).get(position);
    }


    /**
     * sắp xếp hoạt động trong ngày theo thời gian
     *
     * @param day
     */
    public void sortActionByTime(int day) {
        Collections.sort(actionsInWeek.get(day));
    }

    /**
     * xoá hoạt động trong ngày day ở vị trí pos
     *
     * @param day
     * @param posItemAction
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void deleteActionByPositionItemAction(int day, int posItemAction) {
        ItemAction action = actionsInWeek.get(day).remove(posItemAction);
        ItemAction itemAction = data.getActionFromDBById(action.getId());
        data.deleteItemAction(itemAction);
        setCurrAction();
        checkNotificationAndDND();
    }

    /**
     * xoá item data trong bảng thời gian biểu
     *
     * @param posItemData
     */
    public void deleteItemDataFromTimeTable(int posItemData) {

        int i = posItemData - getItemDataInTimeTable(posItemData).getFlag() * COUNT_DAY;
        ItemDataInTimeTable item = getItemDataInTimeTable(i);
        int count = item.getTimeDoIt();

        int j = 0;
        while (j < count && i < getCountItemData()) {
            item = getItemDataInTimeTable(i);
            ItemDataInTimeTable newItem = new ItemDataInTimeTable();
            newItem.setId(item.getId());
            newItem.setHourOfDay(item.getHourOfDay());
            newItem.setDayOfWeek(item.getDayOfWeek());
            updateItemData(newItem);
            i = i + COUNT_DAY;
            j++;
        }
    }

    /**
     * cập nhật lại thời gian biểu
     */
    public void updateTimeTable() {
        data.updateTimeTable(itemDatas);
    }

    /**
     * tạo ra thời gian bắt đầu phù hợp cho hoạt động
     *
     * @param day
     * @param timeDoIt
     * @return
     */
    public int setTimeForAction(int day, int timeDoIt) {
        List<ItemAction> itemActions = actionsInWeek.get(day);
        List<ItemAction> actions = new ArrayList<>();
        actions.addAll(itemActions);
        if (actions.size() > 0) {
            int timeStart;
            int timeEnd;
            for (int i = 0; i < actions.size() - 1; i++) {
                ItemAction actionOne = actions.get(i);
                ItemAction actionTwo = actions.get(i + 1);
                timeStart = actionOne.getHourOfDay() + actionOne.getTimeDoIt();
                timeEnd = actionTwo.getHourOfDay();
                if (timeEnd - timeStart >= timeDoIt) {
                    return timeStart;
                }
            }
            ItemAction actionOne = actions.get(actions.size() - 1);
            timeStart = actionOne.getHourOfDay() + actionOne.getTimeDoIt();
            timeEnd = TIME_MAX;
            if (timeEnd - timeStart >= timeDoIt) {
                return timeStart;
            }
        }
        return TIME_MAX;
    }

    /**
     * cập nhật itemdata
     *
     * @param item
     */
    public void updateItemData(ItemDataInTimeTable item) {
        data.updateItemData(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //true will remove notification
        }
        unRegisterBroadcast();
        cancelAlarm();

    }


    /**
     * cập nhật hoạt động
     *
     * @param action
     */
    public void updateItemAction(ItemAction action) {
        checkActionDone(action);
        data.updateItemAction(action);
    }


    /**
     * thêm hoạt động từ bảng thời gian biểu
     *
     * @param dayOfWeek
     * @param action
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void insertItemActionFromTimeTable(int dayOfWeek, ItemAction action) {
        if (checkValidInsert(dayOfWeek, action)) {
            action.setWeekOfYear(weekOfYear);
            action.setYear(year);
            insertItemAction(dayOfWeek, action);
        }
    }

    /**
     * cập nhật các hoạt động trong ngày từ thời gian biểu
     *
     * @param dayOfWeek
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void updateActionsInWeekFromTimeTable(int dayOfWeek) {
        for (int i = dayOfWeek; i < COUNT_DAY; i++) {
            for (int j = 0; j < COUNT_TIME; j++) {
                int index = i + COUNT_DAY * j;
                ItemDataInTimeTable itemData = itemDatas.get(index);
                if (itemData.isActive() && itemData.getFlag() == 0) {
                    ItemAction action = new ItemAction();
                    action.setTitle(itemData.getTitle());
                    action.setAction(itemData.getAction());
                    action.setDayOfWeek(itemData.getDayOfWeek());
                    action.setHourOfDay(itemData.getHourOfDay());
                    action.setTimeDoIt(itemData.getTimeDoIt());
                    action.setNotification(itemData.isNotification());
                    action.setDoNotDisturb(itemData.isDoNotDisturb());
                    int day = itemData.getDayOfWeek();
                    insertItemActionFromTimeTable(day, action);
                }
            }
            sortActionByTime(i);
        }
        setCurrAction();
    }

    /**
     * kiểm tra xem hoạt động thêm vào có thoả mãn phù hợp không
     *
     * @param day
     * @param itemAction
     * @return
     */
    public boolean checkValidInsert(int day, ItemAction itemAction) {
        List<ItemAction> actions = getActionsInDay(day);
        int timeStart = itemAction.getHourOfDay();
        int timeEnd = timeStart + itemAction.getTimeDoIt();
        boolean valid = true;
        if (actions.size() > 0) {
            for (ItemAction action : actions) {
                int start = action.getHourOfDay();
                int end = action.getHourOfDay() + action.getTimeDoIt();
                if (end <= timeStart || start >= timeEnd) {

                } else {
                    return false;
                }
            }
        }
        return valid;
    }


    /**
     * Đặt hẹn giờ
     *
     * @param currHour
     * @param currMinute
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setAlarm(int currHour, int currMinute) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, currHour + 1);
        calendar.set(Calendar.MINUTE, currMinute);
        calendar.set(Calendar.SECOND, 0);
        long timeInMillis = calendar.getTimeInMillis();
        Intent intent = new Intent(this, TimeService.class);
        intent.setAction(START_ALARM);
        pIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }


    }

    /**
     * tắt chế độ không làm phiền
     */
    private void turnOffDoNotDisturb() {
        changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
    }

    /**
     * bật chế độ k làm phiền
     */
    private void turnOnDoNotDisturb() {
        changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_NONE);
    }

    public void cancelAlarm() {
        alarmManager.cancel(pIntent);
    }


//    private Bitmap getBitMap(int action) {
//        Bitmap bm = null;
//        switch (action) {
//            case FREE_TIME:
//                bm = BitmapFactory.decodeResource(getResources(), R.drawable.free_time);
//                break;
//            case OUTSIDE_ACTION:
//                bm = BitmapFactory.decodeResource(getResources(), R.drawable.school);
//                break;
//            case AT_HOME_ACTION:
//                bm = BitmapFactory.decodeResource(getResources(), R.drawable.homework);
//                break;
//            case AMUSING_ACTION:
//                bm = BitmapFactory.decodeResource(getResources(), R.drawable.giaitri);
//                break;
//            case RELAX_ACTION:
//                bm = BitmapFactory.decodeResource(getResources(), R.drawable.sleep);
//                break;
//            default:
//                break;
//        }
//        return bm;
//    }

    public void setCompleteForAction(int dayOfWeek, int adapterPosition) {
        data.setCompleteForAction(actionsInWeek.get(dayOfWeek).get(adapterPosition));
    }

    public int getCountItemKindOfAction() {
        if (kindOfActions == null) return 0;
        return kindOfActions.size();
    }

    public ItemKindOfAction getKindOfActionItem(int position) {
        return kindOfActions.get(position);
    }

    public List<ItemAction> getActionsByKindOfActionId(int kindOfActionId) {
        List<ItemAction> actions = new ArrayList<>();
        for (int day = 0; day < COUNT_DAY; day++) {
            List<ItemAction> actionsInDay = getActionsInDay(day);
            for (ItemAction action : actionsInDay) {
                if (action.getAction() == kindOfActionId) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action != null) {
                ItemAction itemAction = data.getActionFromDBById(idCurrAction);
                switch (action) {
                    case Intent.ACTION_TIME_CHANGED:
                        cancelAlarm();
                        setAlarm((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) % 24, 0);
                        checkNotificationAndDND();
                        checkActionsDone();
                        break;
                    case Intent.ACTION_TIMEZONE_CHANGED:
                        cancelAlarm();
                        setAlarm((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) % 24, 0);
                        checkNotificationAndDND();
                        break;
                    case "LATTER_ACTION":
                        if (itemAction != null) {
                            if (itemAction.isComplete()) {
                                data.setCompleteForAction(itemAction);
                                if (iUpdateUI != null) {
                                    iUpdateUI.updateUI();
                                }
                            }
                        }
                        notificationManager.cancel(NOTIFICATION_ACTION_ID);
                        turnOffDoNotDisturb();
                        break;
                    case "START_ACTION":
                        if (itemAction != null) {
                            if (!itemAction.isComplete()) {
                                data.setCompleteForAction(itemAction);
                                if (iUpdateUI != null) {
                                    iUpdateUI.updateUI();
                                }
                            }
                        }
                        notificationManager.cancel(NOTIFICATION_ACTION_ID);
                        break;
                    default:
                        break;
                }
            }
        }
    }


    /**
     * kiểm tra xem các hoạt động đã qua thời gian hiện tại chưa
     */
    public void checkActionsDone() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        checkActionsDone(dayOfWeek);
    }

    public void checkActionsDone(int dayOfWeek) {
        for (int i = dayOfWeek; i < COUNT_DAY; i++) {
            List<ItemAction> actionsInDay = actionsInWeek.get(i);
            for (ItemAction action : actionsInDay) {
                if (action.isDone() == false
                        || action.getHourOfDay() >= Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        || action.getWeekOfYear() >= (Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) - 1)) {
                    checkActionDone(action);
                }
            }

        }
    }

    /**
     * kiểm tra hoạt động đã qua thời gian hiện tại chưa
     *
     * @param action
     */
    public void checkActionDone(ItemAction action) {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        boolean done;
        if (year <= action.getYear()) {
            if (year == action.getYear()) {
                if (weekOfYear <= action.getWeekOfYear()) {
                    if (weekOfYear == action.getWeekOfYear()) {
                        if (dayOfWeek <= action.getDayOfWeek()) {
                            if (dayOfWeek == action.getDayOfWeek()) {
                                if (hourOfDay < action.getHourOfDay()) {
                                    done = false;
                                } else {
                                    done = true;
                                }
                            } else done = false;
                        } else {
                            done = true;
                        }
                    } else done = false;
                } else {
                    done = true;
                }
            } else done = false;
        } else {
            done = true;
        }
        data.setDoneForItemAction(action, done);
    }


    /**
     * đăng kí broadcast
     */
    private void registerBroadcast() {
        broadcast = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //      intentFilter.addAction("CHECK_ACTION_DONE_AND_COMPLETE");
        intentFilter.addAction("START_ACTION");
        intentFilter.addAction("LATTER_ACTION");
        //  intentFilter.addAction("SET_NEW_TIME_FOR_ACTION");
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(broadcast, intentFilter);
    }

    /**
     * huỷ đăng ký
     */
    private void unRegisterBroadcast() {
        unregisterReceiver(broadcast);
    }

    public interface IUpdateUI {
        void updateUI();

        void setCurrentItemFragment(int dayOfWeek);

        void updateActionsInWeek(int dayOfWeek, int weekOfYear, int year);
    }

    /**
     * xét hoạt động hiện tại
     */
    public void setCurrAction() {
        currAction = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
//        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        List<ItemAction> list = data.getActionsInDay(dayOfWeek, weekOfYear, year);
        if (list == null) return;
        List<ItemAction> actions = new ArrayList<>();
        actions.addAll(list);
        Collections.sort(actions);
        currAction = new ArrayList<>();
        for (ItemAction item : actions) {
            currAction.add(item);
        }
    }

    /**
     * hiển thị thông báo
     *
     * @param action
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(ItemAction action) {

        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_action);
        remoteViews.setTextViewText(R.id.tv_title_notifi, action.getTitle());
        remoteViews.setTextViewText(R.id.tv_set_time_start, String.valueOf(action.getHourOfDay()) + " giờ");
        remoteViews.setTextViewText(R.id.tv_set_time_finish, String.valueOf(action.getHourOfDay() + action.getTimeDoIt()) + " giờ");
//        Bitmap bm = getBitMap(action.getAction());
//        remoteViews.setImageViewBitmap(R.id.iv_notification, bm);
        remoteViews.setImageViewResource(R.id.iv_notification, getResImageByKindOfActionId(action.getAction()));
        Intent bntStartAction = new Intent("START_ACTION");
        idCurrAction = action.getId();
        pBntStartAction = PendingIntent.getBroadcast(this, 111, bntStartAction, 0);
        remoteViews.setOnClickPendingIntent(R.id.btn_start, pBntStartAction);

        Intent bntEndAction = new Intent("LATTER_ACTION");
        pBntEndAction = PendingIntent.getBroadcast(this, 222, bntEndAction, 0);
        remoteViews.setOnClickPendingIntent(R.id.btn_latter, pBntEndAction);


        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
                .setChannelId(CHANNEL_ID_NOTIFICATION)
                .setContentIntent(pendingIntent)
                .setCustomBigContentView(remoteViews);
        notificationManager.notify(NOTIFICATION_ACTION_ID, builder.build());
    }

    /**
     * tạo notification manager
     */
    private void createNotificationManager() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * thay đổi trạng thái
     *
     * @param interruptionFilter
     */
    protected void changeInterruptionFiler(int interruptionFilter) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                notificationManager.setInterruptionFilter(interruptionFilter);
            } else {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
    }

    public int getResImageByKindOfActionId(int idKindOfAction) {
        return kindOfActions.get(idKindOfAction).getIdResImage();
    }

    public int getResColorByKindOfActionId(int idKindOfAction) {
        return kindOfActions.get(idKindOfAction).getIdResColor();
    }

    public int getResBackgroundByKindOfActionId(int idKindOfAction) {
        return kindOfActions.get(idKindOfAction).getIdResBackground();
    }

    public String getTitleByKindOfActionId(int idKindOfAction) {
        return kindOfActions.get(idKindOfAction).getTitle();
    }
}
