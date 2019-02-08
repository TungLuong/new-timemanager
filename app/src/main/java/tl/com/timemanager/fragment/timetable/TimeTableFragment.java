package tl.com.timemanager.fragment.timetable;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import tl.com.timemanager.item.ItemDataInTimeTable;
import tl.com.timemanager.R;
import tl.com.timemanager.adapter.DataItemInTimeTableAdapter;
import tl.com.timemanager.adapter.TimeItemAdapter;
import tl.com.timemanager.base.BaseFragment;
import tl.com.timemanager.dialog.insert.InsertItemDataInTimeTableDialog;
import tl.com.timemanager.dialog.seen.SeenActionInTimeTableDialog;
import tl.com.timemanager.service.TimeService;

import static tl.com.timemanager.Constant.COUNT_DAY;

public class TimeTableFragment extends BaseFragment implements DataItemInTimeTableAdapter.IDataItem, InsertItemDataInTimeTableDialog.IDataChangedListener {

    private static final String TAG = TimeTableFragment.class.getSimpleName() ;
    private RecyclerView rcvTime;
    private RecyclerView rcvData;
    private TimeService timeService;
    private DataItemInTimeTableAdapter dataItemInTimeTableAdapter;
    private int currPos;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_table,container,false);
        initRecyclerView(view);
        controlRecyclerViewAndScroll(view);
        return view;
    }


    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
        this.timeService.setActionsInCurrentWeek();
    }


    /**
     * Khởi tạo recyclerView
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initRecyclerView(View view) {
        rcvTime = view.findViewById(R.id.recycler_view_time);
        rcvTime.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        TimeItemAdapter timeItemAdapter = new TimeItemAdapter();
        rcvTime.setAdapter(timeItemAdapter);

        rcvData = view.findViewById(R.id.recycler_view_data);
        rcvData.setHasFixedSize(false);
        rcvData.setLayoutManager(new StaggeredGridLayoutManager(COUNT_DAY, StaggeredGridLayoutManager.VERTICAL));
        dataItemInTimeTableAdapter = new DataItemInTimeTableAdapter(this);
        rcvData.setAdapter(dataItemInTimeTableAdapter);


    }

    /**
     * điều khuyển thao tác vuốt của recyclerView và Scroll
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void controlRecyclerViewAndScroll(View view){

        final HorizontalScrollView scrollViewB = view.findViewById(R.id.horizontal_scroll_view_B);
        final HorizontalScrollView scrollViewD = view.findViewById(R.id.horizontal_scroll_view_D);

        scrollViewB.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                scrollViewD.scrollTo(scrollX,scrollY);
            }
        });

        scrollViewD.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                scrollViewB.scrollTo(scrollX,scrollY);
            }
        });

        final boolean[] firstIsTouched = {false};
        final boolean[] secondIsTouched = {false};



        rcvData.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(firstIsTouched[0]) {
                    secondIsTouched[0] = false;
                    super.onScrolled(recyclerView, dx, dy);
                    rcvTime.scrollBy(dx, dy);
                }
            }
        });

        rcvData.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                firstIsTouched[0] = true;
                return false;
            }
        });


        rcvTime.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(secondIsTouched[0]) {
                    firstIsTouched[0] = false;
                    super.onScrolled(recyclerView, dx, dy);
                    rcvData.scrollBy(dx, dy);
                }
            }
        });

        rcvTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                secondIsTouched[0] = true;
                return false;
            }
        });

    }

    /**
     * Lấy số lượng item
     * @return số lượng item
     */
    @Override
    public int getCount() {
        if(timeService == null) return 0;
        return timeService.getCountItemData();
    }

    /**
     * Lấy giá trị item
     * @param position vị trí item
     * @return giá trị item
     */
    @Override
    public ItemDataInTimeTable getData(int position) {
        return timeService.getItemDataInTimeTable(position);
    }

    /**
     * Khi click vào item
     * @param position vị trí item
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClickItem(int position) {
        ItemDataInTimeTable itemDataInTimeTable = timeService.getItemDataInTimeTable(position);
        if(itemDataInTimeTable.isActive()){
            displaySeenActionDialog(position);
        }
        else {
            if (currPos == position) {
                displayInsertActionDialog(position);
            } else {
                currPos = position;
            }
            dataItemInTimeTableAdapter.updatePositionFocus(position);
        }

    }

    @Override
    public int getResColorByKindOfActionId(int kindOfActionId) {
        return timeService.getResColorByKindOfActionId(kindOfActionId);
    }

    /**
     * Mở dialog SeenActionDialog
     * @param position vị trí item click vào
     */
    private void displaySeenActionDialog(int position) {
        SeenActionInTimeTableDialog dialog = new SeenActionInTimeTableDialog(getActivity());
        dialog.setPositionItemData(position);
        dialog.setService(timeService);
        dialog.setiListener(this);
        dialog.initView();
        dialog.show();
    }

    /**
     * Mở dialog InsertActionDialog
     * @param position vị trí item chọn
     */
    private void displayInsertActionDialog(int position) {
        InsertItemDataInTimeTableDialog dialog = new InsertItemDataInTimeTableDialog(getActivity());
        dialog.setPositionItemData(position);
        dialog.setService(timeService);
        dialog.setiListener(this);
        dialog.initView();
        dialog.show();
    }

    /**
     * Thay đổi dữ liệu của thời gian biểu
     */
    @Override
    public void changedDataItem() {
        dataItemInTimeTableAdapter.notifyDataSetChanged();
    }

    @Override
    public void changedActionItem() {

    }


}
