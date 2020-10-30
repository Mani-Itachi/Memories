package mani.itachi.memories.uicomponents;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import mani.itachi.memories.R;
import mani.itachi.memories.utils.AllStrings;

/**
 * Created by ManikantaInugurthi on 03-02-2017.
 */

public class DialogCreator implements View.OnClickListener {

    Context mContext;
    OnClickCallBack mOnClickCallBack;
    Boolean shouldEdit = false;
    List<String> mStringList;

    public DialogCreator(Context context, OnClickCallBack onClickCallBack) {
        mContext = context;
        mOnClickCallBack = onClickCallBack;
        initListData();
    }

    private void initListData() {
        mStringList = new ArrayList<>();
        AllStrings strings = new AllStrings();
        for (String s : strings.typeString) {
            mStringList.add(s);
        }
    }

    @Override
    public void onClick(final View view) {
        if (shouldEdit) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            AlertDialog.Builder alerttype = new AlertDialog.Builder(mContext);
            final EditText edittext = new EditText(mContext);
            TextView temp = (TextView) view;
            switch (view.getId()) {
                case R.id.create_memory_name:
                case R.id.memory_detail_name:
                    alert.setTitle("Name Your Memory");
                    edittext.setHint(temp.getText().toString());
                    edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
                    break;
                case R.id.create_memory_type:
                case R.id.memory_detail_type:
                    alerttype.setTitle("What does this memory belongs to");
                    View typeListView = LayoutInflater.from(mContext).inflate(R.layout.type_listview, null);
                    CommonAdapter<String> commonAdapter = new CommonAdapter<>(new CommonAdapter.OnGetViewListener<String>() {
                        @Override
                        public View getView(View convertView, String item, int position) {
                            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_imagepicker, null);
                            ((TextView) convertView.findViewById(R.id.dialogListText)).setText(item);
                            ((ImageView) convertView.findViewById(R.id.dialogListImage)).
                                    setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                                            mContext.getResources().getIdentifier("type_" + item.toLowerCase(), "drawable",
                                                    mContext.getPackageName()))
                                    );
                            return convertView;
                        }
                    });
                    commonAdapter.setList(mStringList);
                    alerttype.setAdapter(commonAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mOnClickCallBack.onPress(view, mStringList.get(i).toLowerCase());
                        }
                    });
                    break;
                case R.id.create_memory_date:
                    //todo date
                    break;
                case R.id.memory_detail_date:
                    //todo date
                    break;
                case R.id.create_memory_desc:
                case R.id.memory_detail_desc:
                    alert.setTitle("Why so Special?");
                    edittext.setSingleLine(false);
                    edittext.setHint(temp.getText().toString());
                    edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
                    break;
            }
            alert.setView(edittext);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (!edittext.getText().toString().isEmpty()) {
                        mOnClickCallBack.onPress(view, edittext.getText().toString());
                    }
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            if (view.getId() != R.id.create_memory_type && view.getId() != R.id.create_memory_date && view.getId() != R.id.memory_detail_date
                    && view.getId() != R.id.memory_detail_type) {
                alert.show();
            } else if (view.getId() != R.id.create_memory_date && view.getId() != R.id.memory_detail_date) {
                alerttype.show();
            }
        }
    }

    public Boolean getShouldEdit() {
        return shouldEdit;
    }

    public void setShouldEdit() {
        this.shouldEdit = !shouldEdit;
    }

    public interface OnClickCallBack {
        void onPress(View v, String s);
    }
}
