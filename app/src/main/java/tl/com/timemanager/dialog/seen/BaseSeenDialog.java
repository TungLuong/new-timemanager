package tl.com.timemanager.dialog.seen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import tl.com.timemanager.R;
import tl.com.timemanager.service.TimeService;
import tl.com.timemanager.dialog.insert.InsertItemDataInTimeTableDialog;

public class BaseSeenDialog extends BottomSheetDialog implements View.OnClickListener {

    protected static final String TAG = InsertItemDataInTimeTableDialog.class.getSimpleName();
    //Ảnh hoạt động
    protected ImageView ivAction;
    //Tên hoạt động
    protected TextView tvTitle;
    //nút close
    protected ImageView ivClose;
    //nút sửa hoạt động
    protected Button btnModify;
    //nút xoá hoạt động
    protected Button btnDelete;
    //thời gian bắt đầu
    protected TextView tvTimeStart;
    //thời gian kết thúc
    protected TextView tvTimeEnd;
    //loại hoạt động
    protected TextView tvKindOfAction;

    protected ImageView ivNotification;
    protected ImageView ivDoNotDisturb;

    protected TimeService service;
    protected InsertItemDataInTimeTableDialog.IDataChangedListener iListener;

    public BaseSeenDialog(@NonNull Context context) {
        super(context, R.style.StyleDialogBottom);
        setContentView(R.layout.dialog_seen_action);
    }


    public void setService(TimeService service) {
        this.service = service;
    }


    public void setiListener(InsertItemDataInTimeTableDialog.IDataChangedListener listener) {
        this.iListener = listener;
    }

    public void initView() {

        ivAction = findViewById(R.id.iv_img_action);
        ivClose = findViewById(R.id.iv_close);
        ivNotification = findViewById(R.id.iv_notifi);
        ivDoNotDisturb = findViewById(R.id.iv_do_not_disturb);
        btnModify = findViewById(R.id.btn_modify);
        btnDelete = findViewById(R.id.btn_delete);
        tvTitle = findViewById(R.id.tv_name_action_);
        tvKindOfAction = findViewById(R.id.tv_kind_of_action_);
        tvTimeStart = findViewById(R.id.tv_time_start);
        tvTimeEnd = findViewById(R.id.tv_time_end);

        ivClose.setOnClickListener(this);
        btnModify.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        setData();

    }

    protected void setData() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.btn_modify:
                showDialogModifyAction();
                dismiss();
                break;
            case R.id.btn_delete:
                showDialogDelete();
                break;
        }

    }

    protected void showDialogModifyAction() {

    }

    /**
     * hiển thị dialog xoá
     */
    protected void showDialogDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xoá hoạt động");
        builder.setMessage("Bạn có chắc chắn xoá hoạt động này đi không");
        builder.setCancelable(false);
        builder.setPositiveButton("Quay lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAction();
                dismiss();
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    protected void deleteAction() {
    }

}
