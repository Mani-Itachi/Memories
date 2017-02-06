package mani.itachi.memories.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mani.itachi.memories.MainActivity;
import mani.itachi.memories.R;
import mani.itachi.memories.database.DbHelper;
import mani.itachi.memories.database.MemoryDto;
import mani.itachi.memories.uicomponents.DialogCreator;
import mani.itachi.memories.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemoryDetailFragment extends Fragment {


    MemoryDto memoryDto;
    TextView memoryName;
    TextView memoryType;
    TextView memoryDesc;
    TextView memoryDate;
    ImageView memoryImage;
    Bitmap memoryImageBitmap;
    View detailScreen;
    Bitmap savedScreen;
    DialogCreator dialogCreator;
    Boolean preEdit = false;
    int temp = 0;
    ProgressDialog progressDialog;

    public MemoryDetailFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        memoryDto = (MemoryDto) getArguments().getSerializable("MemoryDto");
        Log.d("TAG", "id is:" +  memoryDto.getId() + "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory_detail, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View v) {
        memoryName = (TextView) v.findViewById(R.id.memory_detail_name);
        memoryType = (TextView) v.findViewById(R.id.memory_detail_type);
        memoryDesc = (TextView) v.findViewById(R.id.memory_detail_desc);
        memoryDate = (TextView) v.findViewById(R.id.memory_detail_date);
        memoryImage = (ImageView) v.findViewById(R.id.memory_detail_image);
        detailScreen = v.findViewById(R.id.detailScreen);
        memoryImage.getLayoutParams().height = (int) (Utils.getDisplayHeight(getActivity()) * 0.40);
        setmemoryData();
        setOnClick();
    }

    public DialogCreator getdialogCreator() {
        return dialogCreator;
    }

    private void setOnClick() {
        dialogCreator = new DialogCreator(getActivity(), new DialogCreator.OnClickCallBack() {
            @Override
            public void onPress(View v, String s) {
                switch (v.getId()) {
                    case R.id.memory_detail_name:
                        memoryName.setText(s);
                        break;
                    case R.id.memory_detail_date:
                        memoryDate.setText(s);
                        break;
                    case R.id.memory_detail_type:
                        memoryType.setText(s);
                        setMyScreenBackGround();
                        break;
                    case R.id.memory_detail_desc:
                        memoryDesc.setText(s);
                        break;
                }
            }
        });
        memoryName.setOnClickListener(dialogCreator);
        memoryDate.setOnClickListener(dialogCreator);
        memoryType.setOnClickListener(dialogCreator);
        memoryDesc.setOnClickListener(dialogCreator);
        final MainActivity mainActivity = (MainActivity) getActivity();
        memoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogCreator.getShouldEdit()) {
                    mainActivity.startMemImagePicker();
                }
            }
        });
        if (preEdit) {
            dialogCreator.setShouldEdit();
        }
    }

    public void setmemoryImage(Bitmap bitmap) {
        memoryImageBitmap = bitmap;
        memoryImage.setImageBitmap(memoryImageBitmap);
    }

    private void setmemoryData() {
        getActivity().setTitle(memoryDto.getName());
        memoryName.setText(memoryDto.getName());
        memoryType.setText(memoryDto.getType());
        memoryDesc.setText(memoryDto.getDesc());
        memoryDate.setText(memoryDto.getDate());
        Bitmap bitmap = null;
        if (memoryDto.getImagePath().equals("-1")) {
            bitmap = BitmapFactory.decodeFile(memoryDto.getBitmapPath());
            bitmap = Utils.getRoundedCornerBitmap(bitmap);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), getResources()
                    .getIdentifier(memoryDto.getImagePath(), "drawable", getActivity().getPackageName()));
        }
        memoryImageBitmap = bitmap;
        memoryImage.setImageBitmap(memoryImageBitmap);
        memoryImage.setImageBitmap(bitmap);
        setMyScreenBackGround();
    }


    private void setMyScreenBackGround() {
        Log.d("Type:", memoryType.getText().toString().toLowerCase());
        if (Utils.isTypePresent(memoryType.getText().toString().toLowerCase())) {
            detailScreen.setBackground(new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
                    getResources().getIdentifier(memoryType.getText().toString().toLowerCase(), "drawable",
                            getActivity().getPackageName()))));
        } else {
            detailScreen.setBackground(getResources().getDrawable(R.drawable.mainbackkground));
        }
    }

    public void shareThismemory() {
        detailScreen.setDrawingCacheEnabled(true);
        detailScreen.setDrawingCacheBackgroundColor(getResources().getColor(R.color.WHITE));
        savedScreen = Bitmap.createBitmap(detailScreen.getDrawingCache());
        detailScreen.destroyDrawingCache();
        detailScreen.setDrawingCacheEnabled(false);
        ((MainActivity) getActivity()).shareImage(savedScreen);
    }

    public void saveInGallery() {
        detailScreen.setDrawingCacheEnabled(true);
        savedScreen = Bitmap.createBitmap(detailScreen.getDrawingCache());
        detailScreen.destroyDrawingCache();
        Utils.saveFile(getActivity(), savedScreen, memoryDto.getId());
    }

    private void saveMyCard(final Fragment fragment) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("How do you want to save the card?");
        builder.setPositiveButton("New", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startProgressDialog("Saving your memory");
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("TAG", "time start:" + System.currentTimeMillis() + "");
                                if (DbHelper.getInstance().saveMyCard(getDtoOfScreenData())) {
                                    toggleShouldEdit();
                                    getActivity().supportInvalidateOptionsMenu();
                                    progressDialog.dismiss();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fragment != null) {
                                                ((MainActivity) getActivity()).setFragment(fragment);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        executorService.shutdown();
                    }
                });
                executorService.shutdown();
            }
        });
        builder.setNegativeButton("Existing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startProgressDialog("Saving your memory");
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (DbHelper.getInstance().updateMyCard(getDtoOfScreenDataWithId(memoryDto.getId()))) {
                                    toggleShouldEdit();
                                    getActivity().supportInvalidateOptionsMenu();
                                    progressDialog.dismiss();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fragment != null) {
                                                ((MainActivity) getActivity()).doFragChange(fragment, 0);
                                            }
                                        }
                                    });
                                }

                            }
                        });
                        executorService.shutdown();
                    }
                });
                executorService.shutdown();
            }
        });
        builder.setNeutralButton("Don't Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (fragment != null) {
                    ((MainActivity) getActivity()).doFragChange(fragment, 0);
                }
            }
        });
        builder.show();
    }

    private MemoryDto getDtoOfScreenDataWithId(int id) {
        memoryDto = new MemoryDto();
        memoryDto.setId(id);
        memoryDto.setName(memoryName.getText().toString());
        memoryDto.setType(memoryType.getText().toString());
        memoryDto.setDesc(memoryDesc.getText().toString());
        memoryDto.setDate(memoryDate.getText().toString());
        memoryDto.setBitmapPath(writeToFile(memoryImageBitmap, memoryDto.getId()).getAbsolutePath());
        memoryDto.setImagePath("-1");
        return memoryDto;
    }

    private MemoryDto getDtoOfScreenData() {
        memoryDto = new MemoryDto();
        memoryDto.setId(Utils.getRandomId());
        memoryDto.setName(memoryName.getText().toString());
        memoryDto.setType(memoryType.getText().toString());
        memoryDto.setDesc(memoryDesc.getText().toString());
        memoryDto.setDate(memoryDate.getText().toString());
        memoryDto.setBitmapPath(writeToFile(memoryImageBitmap, memoryDto.getId()).getAbsolutePath());
        memoryDto.setImagePath("-1");
        return memoryDto;
    }

    private File writeToFile(Bitmap bitmap, long id) {
        OutputStream outStream = null;

        File file = new File(getActivity().getFilesDir() + File.separator + id + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(getActivity().getFilesDir() + File.separator + id + ".png");
            Log.e("file exist", "" + file + ",Bitmap= " + file.getAbsolutePath());
        }
        try {
            // make a new bitmap from your file
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("file", "" + file);
        File thumbnail = new File(getActivity().getFilesDir() + File.separator + id + "thumb.png");
        if (thumbnail.exists()) {
            thumbnail.delete();
            thumbnail = new File(getActivity().getFilesDir() + File.separator + id + "thumb.png");
            Log.e("thumbnail exist", "" + file + ",Bitmap= " + file.getAbsolutePath());
        }
        try {
            // make a new bitmap from your file
            outStream = new FileOutputStream(thumbnail);
            if (id % 2 == 0) {
                bitmap=Bitmap.createScaledBitmap(bitmap,320,250,false);
            } else {
                bitmap=Bitmap.createScaledBitmap(bitmap,360,285,false);
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("thumbnail", "" + thumbnail);
        return file;
    }

    public void deleteCard() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure??");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startProgressDialog("Deleting the card");
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        dismissListener();
                                    }
                                });
                                temp = DbHelper.getInstance().deleteCard(memoryDto.getId());
                                Log.d("TAG", "temp" + temp + "");
                                progressDialog.dismiss();
                            }
                        });
                        executorService.shutdown();
                    }
                });
                executorService.shutdown();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();

    }

    private void dismissListener() {
        if (temp == 0) {
            Toast.makeText(getActivity(), "Oops we couldn't delete the memory.", Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).doFragChange(new MyMemoriesFragment(), 0);
        } else {
            ((MainActivity) getActivity()).doFragChange(new MyMemoriesFragment(), 0);
        }
    }

    private void startProgressDialog(String title) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(title);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void saveAndToggle() {
        saveMyCard(null);
    }

    public void saveAndToggleAndChange(Fragment fragment) {
        saveMyCard(fragment);
    }

    public void toggleShouldEdit() {
        dialogCreator.setShouldEdit();
    }

    public Boolean getEditing() {
        return dialogCreator.getShouldEdit();
    }

}
