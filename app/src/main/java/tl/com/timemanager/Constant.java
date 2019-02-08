package tl.com.timemanager;

public interface Constant {
    int FREE_TIME = 0;
    int OUTSIDE_ACTION = 1;
    int AT_HOME_ACTION = 2;
    int AMUSING_ACTION= 3;
    int RELAX_ACTION = 4;

    int TIME_MIN = 5;
    int TIME_MAX = 22;
    int COUNT_DAY = 7;
    int COUNT_TIME = TIME_MAX - TIME_MIN + 1;
    String START_ALARM="action.start.alarm";
    String CHANNEL_ID_NOTIFICATION = "my_channel_notification";
    String CHANNEL_ID_RUNNING_IN_BACKGROUND = "my_channel_running_in_background";
    int DELAY_MINUTE = 1;

}
