package tl.com.timemanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import tl.com.timemanager.base.BaseActivity;
import tl.com.timemanager.base.BaseFragment;
import tl.com.timemanager.fragment.actionstatistics.ActionStatisticsDetailFragment;
import tl.com.timemanager.fragment.actionstatistics.ActionStatisticsFragment;
import tl.com.timemanager.fragment.daysinweek.DaysInWeekFragment;
import tl.com.timemanager.fragment.timetable.TimeTableFragment;
import tl.com.timemanager.service.TimeService;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private ActionBar actionBar;
    private ServiceConnection conn;
    private TimeService timeService;
    //private Animation anim;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm realm = Realm.getDefaultInstance(); // opens "myrealm.realm"
        try {
            setContentView(R.layout.activity_main);
            startService();
            connectedService();
            init();
        } finally {
            realm.close();
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
//
//        int orientation = display.getOrientation();
//
//        if(orientation == Configuration.ORIENTATION_PORTRAIT){
//
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
//
//        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout
                , R.string.open, R.string.close);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        drawerToggle.syncState();
        NavigationView navigationView = findViewById(R.id.nv_drawer);
        navigationView.setNavigationItemSelectedListener(this);
        //anim = AnimationUtils.loadAnimation(this, R.anim.exit_right_to_left);
    }

    /**
     * Bắt đầu service
     */
    private void startService() {
        Intent intent = new Intent();
        intent.setClass(this, TimeService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    /**
     * Kết nối service
     */
    private void connectedService() {
        conn = new ServiceConnection() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyBinder myBinder = (MyBinder) service;
                timeService = myBinder.getTimeService();
                openDaysInWeekFragment();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "Error");
            }
        };
        Intent intent = new Intent();
        intent.setClass(this, TimeService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

//    public void addActionsInDayFragment(){
//        ActionsInDayFragment fragment = new ActionsInDayFragment();
//        FragmentManager manager = getSupportFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        transaction.replace(R.id.content,fragment,ActionsInDayFragment.class.getName());
//        Toast.makeText(this,"AAAAAAAAAAA",Toast.LENGTH_SHORT).show();
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }

    /**
     * Mở fragment DaysInWeekFragment
     */
    public void openDaysInWeekFragment() {
        DaysInWeekFragment fragment = new DaysInWeekFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_right_to_left,
                R.anim.exit_right_to_left,
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right
        );
        transaction.replace(R.id.content, fragment, DaysInWeekFragment.class.getName());
        fragment.setTimeService(timeService);
        transaction.commit();
    }

    /**
     * Mở fragment TimeTableFragment
     */
    public void openTimeTableFragment() {
        TimeTableFragment fragment = new TimeTableFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_right_to_left,
                R.anim.exit_right_to_left,
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right
        );
        transaction.replace(R.id.content, fragment, TimeTableFragment.class.getName());
        fragment.setTimeService(timeService);
        transaction.commit();
    }

    /**
     * Mở fragment ActionStatisticsFragment
     */
    public void openActionStatisticsFragment() {
        ActionStatisticsFragment fragment = new ActionStatisticsFragment(timeService);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        fragment.setDayOfWeek(dayOfWeek);
        fragment.setWeekOfYear(weekOfYear);
        fragment.setYear(year);
        fragment.updateActionsInWeek(dayOfWeek,weekOfYear,year);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_right_to_left,
                R.anim.exit_right_to_left,
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right
        );
        transaction.replace(R.id.content, fragment, ActionStatisticsFragment.class.getName());
        transaction.commit();
    }

    public void openActionStatisticsFragmentTwo(int dayOfWeek, int weekOfYear, int year) {
        ActionStatisticsFragment fragment = new ActionStatisticsFragment(timeService);
        fragment.setDayOfWeek(dayOfWeek);
        fragment.setWeekOfYear(weekOfYear);
        fragment.setYear(year);
        fragment.updateActionsInWeek(dayOfWeek,weekOfYear,year);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right,
                R.anim.enter_right_to_left,
                R.anim.exit_right_to_left
        );
        transaction.replace(R.id.content, fragment, ActionStatisticsFragment.class.getName());
        transaction.commit();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // mDrawerLayout.setAnimation(anim);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        boolean fragmentIsVisible = false;
        switch (item.getItemId()) {
            case R.id.it_actions_in_day:
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment instanceof DaysInWeekFragment && fragment.isVisible()) {
                            fragmentIsVisible = true;
                            break;
                        }
                    }
                }
                if (!fragmentIsVisible) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT, false);
                    openDaysInWeekFragment();
                } else mDrawerLayout.closeDrawer(Gravity.LEFT, true);
                return true;
            case R.id.it_time_table:
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment instanceof TimeTableFragment && fragment.isVisible()) {
                            fragmentIsVisible = true;
                            break;
                        }
                    }
                }
                if (!fragmentIsVisible) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT, false);
                    openTimeTableFragment();
                } else {
                    mDrawerLayout.closeDrawer(Gravity.LEFT, true);
                }
                return true;
            case R.id.it_statistics:
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment instanceof ActionStatisticsFragment && fragment.isVisible()) {
                            fragmentIsVisible = true;
                            break;
                        }
                    }
                }
                if (!fragmentIsVisible) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT, false);
                    openActionStatisticsFragment();
                } else mDrawerLayout.closeDrawer(Gravity.LEFT, true);
                return true;
            case R.id.it_exit:
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment.isVisible()) {
                            ((BaseFragment) fragment).onBackPressed();
                            break;
                        }
                    }
                }
                return true;

        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void openActionStatisticsDetailFragment(int kindOfActionID,int dayOfWeek,int weekOfYear,int year) {
        ActionStatisticsDetailFragment fragment = new ActionStatisticsDetailFragment(timeService);
        fragment.setKindOfActionID(kindOfActionID);
        fragment.setDayOfWeek(dayOfWeek);
        fragment.setWeekOfYear(weekOfYear);
        fragment.setYear(year);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_right_to_left,
                R.anim.exit_right_to_left,
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right
        );
        transaction.replace(R.id.content, fragment, ActionStatisticsDetailFragment.class.getName());
        transaction.commit();

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        if (fragments != null) {
//            for (Fragment fragment : fragments) {
//                if (fragment != null && fragment instanceof DaysInWeekFragment && fragment.isVisible()) {
//                    Calendar cal = Calendar.getInstance();
//                    if(((DaysInWeekFragment) fragment).getWeekOfYear() != cal.get(Calendar.WEEK_OF_YEAR)) {
//                        ((DaysInWeekFragment) fragment).updateActionsInWeek(cal.get(Calendar.DAY_OF_WEEK)-1,cal.get(Calendar.WEEK_OF_YEAR),cal.get(Calendar.YEAR));
//                        timeService.updateActionsInWeekFromTimeTable(cal.get(Calendar.DAY_OF_WEEK) -1 );
//                    }
//                    ((DaysInWeekFragment) fragment).setCurrentItemFragment(cal.get(Calendar.DAY_OF_WEEK)-1);
//                    ((DaysInWeekFragment) fragment).updateUI();
//                    break;
//                }
//                if (fragment != null && fragment instanceof ActionStatisticsFragment && fragment.isVisible()) {
//                    Calendar cal = Calendar.getInstance();
//                    if(((ActionStatisticsFragment) fragment).getWeekOfYear() != cal.get(Calendar.WEEK_OF_YEAR)) {
//                        ((ActionStatisticsFragment) fragment).updateActionsInWeek(cal.get(Calendar.DAY_OF_WEEK)-1,cal.get(Calendar.WEEK_OF_YEAR),cal.get(Calendar.YEAR));
//                        timeService.updateActionsInWeekFromTimeTable(cal.get(Calendar.DAY_OF_WEEK) -1 );
//                    }
//                    ((ActionStatisticsFragment) fragment).updateActionStatisticFragment(cal.get(Calendar.DAY_OF_WEEK)-1);
//                }
//            }
//        }
//
//    }


}
