package tl.com.timemanager.dialog.insert;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import tl.com.timemanager.R;
import tl.com.timemanager.service.TimeService;

import static tl.com.timemanager.Constant.AMUSING_ACTION;
import static tl.com.timemanager.Constant.AT_HOME_ACTION;
import static tl.com.timemanager.Constant.FREE_TIME;
import static tl.com.timemanager.Constant.OUTSIDE_ACTION;
import static tl.com.timemanager.Constant.RELAX_ACTION;

public class BaseInsertDialog extends BottomSheetDialog implements AdapterView.OnItemSelectedListener, View.OnClickListener, TextWatcher {


    //ảnh hoạt động
    protected ImageView ivAction;
    protected TextView tvTimeDoIt;
    protected ImageView btnMinusTime;
    protected ImageView btnPlusTime;
    // box chọn loại hoạt động
    protected Spinner spin_action;
    // tên hoạt động
    protected EditText edtTitleAction;
    //Thời gian bắt đầu
    protected EditText edtTimeStart;
    //nút tắt
    protected ImageView ivClose;
    // nút lưu hoạt động
    protected Button btnSave;
    // bật thông báo hay k
    protected Switch swNotification;
    // bật chế độ DND hay k
    protected Switch swDoNotDisturb;
    // Loại hoạt động
    protected int kindOfAction;
    // đếm giờ
    protected int count = 1;
    protected TimeService service;

    // Thông báo lỗi
    protected TextView tvErrorTime;
    protected TextView tvErrorTitle;
    protected TextView tvErrorTimeStart;

    // đánh dấu xem có phải là sửa hoạt động không.
    protected boolean isModify;
    // lắng nghe dữ liệu thay đổi
    protected IDataChangedListener iListener;

    public BaseInsertDialog(@NonNull Context context) {
        super(context, R.style.StyleDialogBottom);
        setContentView(R.layout.dialog_insert_action);
    }

    public void setService(TimeService service) {
        this.service = service;
    }

    public void setiListener(IDataChangedListener iListener) {
        this.iListener = iListener;
    }

    public void initView() {
        ivAction = findViewById(R.id.iv_img_action);
        //       ArrayAdapter<CharSequence> adapter_time = ArrayAdapter.createFromResource(getContext(), R.array.hour, android.R.layout.simple_spinner_item);
//        adapter_time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spin_time.setAdapter(adapter_time);
//        spin_time.setOnItemSelectedListener(this);

        tvTimeDoIt = findViewById(R.id.tv_time_do);
        btnMinusTime = findViewById(R.id.btn_minus_time);
        btnPlusTime = findViewById(R.id.btn_plus_time);
        btnMinusTime.setOnClickListener(this);
        btnPlusTime.setOnClickListener(this);

        spin_action = findViewById(R.id.spinner_kind_of_action);
        ArrayAdapter<CharSequence> adapter_action = ArrayAdapter.createFromResource(getContext(), R.array.kind_of_action, android.R.layout.simple_spinner_item);
        adapter_action.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_action.setAdapter(adapter_action);
        spin_action.setOnItemSelectedListener(this);

        ivAction = findViewById(R.id.iv_img_action);
        ivClose = findViewById(R.id.iv_close);
        btnSave = findViewById(R.id.btn_save);
        edtTitleAction = findViewById(R.id.edt_name_action);
        edtTimeStart = findViewById(R.id.edt_time_start);

        swNotification = findViewById(R.id.sw_notification);
        swDoNotDisturb = findViewById(R.id.sw_do_not_disturb);
        tvErrorTime = findViewById(R.id.tv_error_time);
        tvErrorTitle = findViewById(R.id.tv_error_title);
        tvErrorTimeStart = findViewById(R.id.tv_error_time_start);

        ivClose.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        edtTitleAction.addTextChangedListener(this);
        edtTimeStart.addTextChangedListener(this);
        setData();
    }

    protected void setData() {

    }

    protected void setModifyingData(boolean b) {

    }

    /**
     * khi chọn spinner
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_kind_of_action:
                kindOfAction = position;
                ivAction.setImageResource(service.getResImageByKindOfActionId(kindOfAction));
//                switch (position) {
//                    case FREE_TIME:
//                        ivAction.setImageResource(R.drawable.free_time);
//                        //colorId = R.color.colorNoAction;
//                        break;
//                    case OUTSIDE_ACTION:
//                        ivAction.setImageResource(R.drawable.school);
//                        //colorId = R.color.colorOutSideAction;
//                        break;
//                    case AT_HOME_ACTION:
//                        ivAction.setImageResource(R.drawable.homework);
//                        //colorId = R.color.colorHomework;
//                        break;
//                    case AMUSING_ACTION:
//                        ivAction.setImageResource(R.drawable.giaitri);
//                        //colorId = R.color.colorEntertainment;
//                        break;
//                    case RELAX_ACTION:
//                        ivAction.setImageResource(R.drawable.sleep);
//                        //colorId = R.color.colorRelax;
//                        break;
//                }
                break;
        }

    }

    protected void checkSameTime() {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {

    }

    /**
     * kiểm tra xem tiêu đề có hợp lệ hay k
     */

    protected void checkInvalidTitle() {
        String title = String.valueOf(edtTitleAction.getText());
        if (title.trim().length() > 0) {
            tvErrorTitle.setVisibility(View.GONE);
        } else {
            tvErrorTitle.setVisibility(View.VISIBLE);
        }
    }


    protected void updateData() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * sau khi thay đổi edit text
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (edtTitleAction.getText().hashCode() == s.hashCode()) {
            checkInvalidTitle();
        } else if (edtTimeStart.getText().hashCode() == s.hashCode()) {
            checkInvalidTimeStart();
        }
    }

    protected void checkInvalidTimeStart() {

    }


    public interface IDataChangedListener {
        void changedDataItem();

        void changedActionItem();
    }

}
