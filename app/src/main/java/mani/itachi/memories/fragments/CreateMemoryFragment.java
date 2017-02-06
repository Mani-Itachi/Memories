package mani.itachi.memories.fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
public class CreateMemoryFragment extends Fragment {

    MemoryDto memoryDto;
    TextView memoryName,memoryDate,memoryType,memorydesc;
    ImageView memoryImage;
    Bitmap memoryImageBitmap;
    View detailScreen;
    Bitmap savedScreen;
    DialogCreator dialogCreator;
    ProgressDialog progressDialog;

    public CreateMemoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        memoryDto =(MemoryDto) getArguments().getSerializable("MemoryDto");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Create Card");
        View view = inflater.inflate(R.layout.fragment_create_memory, container, false);
        initializeViews(view);
        return view;
    }

    public void initializeViews(View view){
        memoryName = (TextView) view.findViewById(R.id.create_memory_name);
        memoryDate = (TextView) view.findViewById(R.id.create_memory_date);
        memorydesc = (TextView) view.findViewById(R.id.create_memory_desc);
        memoryType = (TextView) view.findViewById(R.id.create_memory_type);
        memoryImage = (ImageView) view.findViewById(R.id.create_memory_image);
        detailScreen = view.findViewById(R.id.detailScreen);
        memoryImage.getLayoutParams().height = (int) (Utils.getDisplayHeight(getActivity()) * 0.40);
        setMemoryData();
        setOnClick();
    }

    private void setMemoryData(){
        memoryName.setText(memoryDto.getName());
        memoryType.setText(memoryDto.getType());
        memoryDate.setText(memoryDto.getDate());
        memorydesc.setText(memoryDto.getDesc());
        Bitmap bitmap = null;
        if(memoryDto.getImagePath().equals("-1")){
            bitmap = BitmapFactory.decodeFile(memoryDto.getBitmapPath());
            bitmap = Utils.getRoundedCornerBitmap(bitmap);
        }else {
            bitmap = BitmapFactory.decodeResource(getResources(), getResources()
                    .getIdentifier(memoryDto.getImagePath(), "drawable", getActivity().getPackageName()));
        }
        memoryImageBitmap = bitmap;
        memoryImage.setImageBitmap(memoryImageBitmap);
        setMyScreenBackGround();
    }

    private void setMyScreenBackGround() {
        if (Utils.isTypePresent(memoryType.getText().toString().toLowerCase())) {
            detailScreen.setBackground(new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
                    getResources().getIdentifier(memoryType.getText().toString().toLowerCase(), "drawable",
                            getActivity().getPackageName()))));
        } else {
            detailScreen.setBackground(getResources().getDrawable(R.drawable.mainbackkground));
        }
    }

    private void initMaterial() {
        dialogCreator = new DialogCreator(getActivity(), new DialogCreator.OnClickCallBack() {
            @Override
            public void onPress(View v, String s) {
                switch (v.getId()) {
                    case R.id.create_memory_name:
                        memoryName.setText(s);
                        break;
                    case R.id.create_memory_type:
                        memoryType.setText(s);
                        setMyScreenBackGround();
                        break;
                    case R.id.create_memory_date:
                        memoryDate.setText(s);
                        break;
                    case R.id.create_memory_desc:
                        memorydesc.setText(s);
                        break;
                }
            }
        });
        dialogCreator.setShouldEdit();
    }

    private void setOnClick() {
        initMaterial();
        memoryName.setOnClickListener(dialogCreator);
        memoryType.setOnClickListener(dialogCreator);
        memoryDate.setOnClickListener(dialogCreator);
        memorydesc.setOnClickListener(dialogCreator);
        final MainActivity mainActivity = (MainActivity) getActivity();
        memoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogCreator.getShouldEdit()) {
                    mainActivity.startMemImagePicker();
                }
            }
        });
    }

    public void setMemoryImage(Bitmap bitmap) {
        memoryImageBitmap = bitmap;
        memoryImage.setImageBitmap(memoryImageBitmap);
    }

    public void shareThismemory() {
        detailScreen.setDrawingCacheEnabled(true);
        savedScreen = Bitmap.createBitmap(detailScreen.getDrawingCache());
        detailScreen.destroyDrawingCache();
        ((MainActivity) getActivity()).shareImage(savedScreen);
    }

    public void saveMyCard(final Fragment fragment) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Saving the memory");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TAG", "time start:" + System.currentTimeMillis() + "");
                        if (DbHelper.getInstance().saveMyCard(getDtoOfScreen())) {
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

    private MemoryDto getDtoOfScreen() {
        memoryDto = new MemoryDto();
        memoryDto.setId(Utils.getRandomId());
        memoryDto.setName(memoryName.getText().toString());
        memoryDto.setType(memoryType.getText().toString());
        memoryDto.setDate(memoryDate.getText().toString());
        memoryDto.setDesc(memorydesc.getText().toString());
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
}
