package com.onecoder.device.adpater;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.onecoder.device.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/6/5.
 */

public class FileListAdapter extends BaseAdapter {
    public static final String KEY_FILE_NAME = "fileName";
    public static final String KEY_FILE_SIZE = "fileSize";
    public static final String KEY_CHECK_DELETE_HISTORY_STATUS = "deleteHistory";
    public static final String KEY_CHECK_DELETE_FILE_STATUS = "deleteFile";
    public static final String KEY_FILE_DOWNLOAD_STATUS = "fileDownloadStatus";
    public static final String KEY_CHECK_SELECT_STATUS = "checkSelectStatus";
    public static final String KEY_CHECK_HAS_FOCUS = "checkHasFocus";

    public static final String VAL_BOOLEAN_FALSE = "0";
    public static final String VAL_BOOLEAN_TRUE = "1";

    private boolean debugBatchDeleting = false;
    private Context context;
    private List<Map<String, String>> mFileInfoList;
    private int focusOnPosition = -1;

    private OnItemClickListener onItemClickListener;

    /**
     * 点击Item的回调
     */
    public interface OnItemClickListener {
        void onItemClick(int position, ViewHolder viewHolder, Map<String, String> map, boolean deleteHistory, boolean deleteFile, boolean selected);
    }

    public FileListAdapter(Context context, List<Map<String, String>> mFileInfoList) {
        this.context = context;
        this.mFileInfoList = mFileInfoList;
    }

    public boolean isDebugBatchDeleting() {
        return debugBatchDeleting;
    }

    public void setDebugBatchDeleting(boolean debugBatchDeleting) {
        this.debugBatchDeleting = debugBatchDeleting;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(List<Map<String, String>> mFileInfoList) {
        this.mFileInfoList = mFileInfoList;
        notifyDataSetChanged();
    }

    public boolean focusOn(int position) {
        if (focusOnPosition == position) {
            return false;
        }

        Object o = getItem(position);
        if (!(o instanceof Map)) {
            return false;
        }
        Map<String, String> map = (Map<String, String>) o;
        map.put(KEY_CHECK_HAS_FOCUS, VAL_BOOLEAN_TRUE);

        o = getItem(focusOnPosition);
        if (o instanceof Map) {
            map = (Map<String, String>) o;
            map.put(KEY_CHECK_HAS_FOCUS, VAL_BOOLEAN_FALSE);
        }

        focusOnPosition = position;
        notifyDataSetChanged();
        return true;
    }

    @Override
    public int getCount() {
        return mFileInfoList != null ? mFileInfoList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mFileInfoList != null && position >= 0 && position < mFileInfoList.size()
                ? mFileInfoList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mFileInfoList == null || mFileInfoList.size() <= position || mFileInfoList.get(position) == null) {
            return convertView;
        }

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.fit_file_list_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.root_layout = convertView.findViewById(R.id.root_layout);
            viewHolder.tv_file_name = convertView.findViewById(R.id.tv_file_name);
            viewHolder.tv_file_size = convertView.findViewById(R.id.tv_file_size);
            viewHolder.tv_delete_history_hint = convertView.findViewById(R.id.tv_delete_history_hint);
            viewHolder.tv_delete_file_hint = convertView.findViewById(R.id.tv_delete_file_hint);
            viewHolder.tv_file_download_status = convertView.findViewById(R.id.tv_file_download_status);
            viewHolder.tv_select_status_hint = convertView.findViewById(R.id.tv_select_status_hint);
            viewHolder.checkbox_delete_history = convertView.findViewById(R.id.checkbox_delete_history);
            viewHolder.checkbox_delete_file = convertView.findViewById(R.id.checkbox_delete_file);
            viewHolder.checkbox_select_status = convertView.findViewById(R.id.checkbox_select_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Map<String, String> map = mFileInfoList.get(position);

        viewHolder.root_layout.setBackgroundColor(VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_HAS_FOCUS))
                ? Color.parseColor("#33ee0000") : Color.TRANSPARENT);

        viewHolder.tv_file_name.setText("File name:" + map.get(KEY_FILE_NAME));
        viewHolder.tv_file_size.setText("File size:" + map.get(KEY_FILE_SIZE) + "KB");

        boolean downloaded = VAL_BOOLEAN_TRUE.equals(map.get(KEY_FILE_DOWNLOAD_STATUS));
        viewHolder.tv_file_download_status.setText(downloaded ? "Downloaded" : "Not download");
        viewHolder.tv_file_download_status.setTextColor(downloaded ? Color.GREEN : Color.BLACK);
        viewHolder.tv_delete_history_hint.setVisibility(debugBatchDeleting ? View.VISIBLE : View.GONE);
        viewHolder.tv_delete_file_hint.setVisibility(debugBatchDeleting ? View.VISIBLE : View.GONE);
        viewHolder.checkbox_delete_history.setVisibility(debugBatchDeleting ? View.VISIBLE : View.GONE);
        viewHolder.checkbox_delete_file.setVisibility(debugBatchDeleting ? View.VISIBLE : View.GONE);
        viewHolder.tv_select_status_hint.setVisibility(debugBatchDeleting ? View.VISIBLE : View.GONE);
        viewHolder.checkbox_select_status.setVisibility(debugBatchDeleting ? View.VISIBLE : View.GONE);
        viewHolder.checkbox_delete_history.setChecked(VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_DELETE_HISTORY_STATUS)));
        viewHolder.checkbox_delete_file.setChecked(VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_DELETE_FILE_STATUS)));
        viewHolder.checkbox_select_status.setChecked(VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_SELECT_STATUS)));

        setItemClickEvent(position, viewHolder);
        return convertView;
    }

    private void setItemClickEvent(final int position, final ViewHolder viewHolder) {
        if (getItem(position) == null) {
            return;
        }
        final boolean deleteHistory = viewHolder.checkbox_delete_history.isChecked();
        final boolean deleteFile = viewHolder.checkbox_delete_file.isChecked();
        final boolean selected = viewHolder.checkbox_select_status.isChecked();
        final Map<String, String> map = (Map<String, String>) getItem(position);

        viewHolder.root_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position, viewHolder, map, deleteHistory, deleteFile, selected);
                }
            }
        });

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (map == null) {
                    return;
                }
                switch (buttonView.getId()) {
                    case R.id.checkbox_delete_history:
                        map.put(KEY_CHECK_DELETE_HISTORY_STATUS, isChecked ? VAL_BOOLEAN_TRUE : VAL_BOOLEAN_FALSE);
                        break;

                    case R.id.checkbox_delete_file:
                        map.put(KEY_CHECK_DELETE_FILE_STATUS, isChecked ? VAL_BOOLEAN_TRUE : VAL_BOOLEAN_FALSE);
                        break;

                    case R.id.checkbox_select_status:
                        map.put(KEY_CHECK_SELECT_STATUS, isChecked ? VAL_BOOLEAN_TRUE : VAL_BOOLEAN_FALSE);
                        break;

                    default:
                        break;
                }
            }
        };

        viewHolder.checkbox_delete_history.setOnCheckedChangeListener(onCheckedChangeListener);
        viewHolder.checkbox_delete_file.setOnCheckedChangeListener(onCheckedChangeListener);
        viewHolder.checkbox_select_status.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    public class ViewHolder {

        public LinearLayout root_layout = null;
        public TextView tv_file_name = null;
        public TextView tv_file_size = null;
        public TextView tv_delete_history_hint = null;
        public TextView tv_delete_file_hint = null;
        public TextView tv_file_download_status = null;
        public TextView tv_select_status_hint = null;
        public CheckBox checkbox_delete_history = null;
        public CheckBox checkbox_delete_file = null;
        public CheckBox checkbox_select_status = null;
    }
}
