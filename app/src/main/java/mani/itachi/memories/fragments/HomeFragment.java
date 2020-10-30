package mani.itachi.memories.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Random;

import mani.itachi.memories.MainActivity;
import mani.itachi.memories.R;
import mani.itachi.memories.database.DbHelper;
import mani.itachi.memories.database.MemoryDto;
import mani.itachi.memories.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    TextView memoryName;
    TextView memoryType;
    TextView memoryDate;
    TextView memoryDesc;
    ImageView memoryImage;
    TextView nomemories;
    View memoryScreen;
    int randmem;
    MemoryDto memoryDto;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<MemoryDto> mem = DbHelper.getInstance().getAllMyCards();
        if (mem.size() != 0) {
            randmem = new Random().nextInt(mem.size());
        } else {
            memoryDto = new MemoryDto(-1, "mani", "type", "date", "desc", "-1");
            return;
        }
        Log.v("Size", String.valueOf(mem.size()));
        Log.v("ID=", String.valueOf(randmem));
        memoryDto = mem.get(randmem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Memories");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        return view;
    }

    private void initializeViews(View view) {
        memoryName = view.findViewById(R.id.homememory_name);
        memoryDate = view.findViewById(R.id.homememory_date);
        memoryDesc = view.findViewById(R.id.homememory_desc);
        memoryType = view.findViewById(R.id.homememory_type);
        memoryImage = view.findViewById(R.id.homememory_image);
        nomemories = view.findViewById(R.id.no_memories_home);
        memoryScreen = view.findViewById(R.id.memoryScreen);
        if (memoryDto.getId() == -1) {
            Log.v("GetID", String.valueOf(memoryDto.getId()));
            memoryScreen.setVisibility(View.INVISIBLE);
            return;
        } else {
            memoryScreen.setVisibility(View.VISIBLE);
            nomemories.setVisibility(View.INVISIBLE);
        }
        memoryName.setText(memoryDto.getName());
        memoryType.setText(memoryDto.getType());
        memoryDate.setText(memoryDto.getDate());
        memoryDesc.setText(memoryDto.getDesc());
        memoryImage.getLayoutParams().height = (int) (Utils.getDisplayHeight(getActivity()) * 0.40);
        Bitmap bitmap = null;
        if (memoryDto.getImagePath().equals("-1")) {
            Log.d("file path stored is", memoryDto.getBitmapPath());
            bitmap = BitmapFactory.decodeFile(memoryDto.getBitmapPath());
            if (bitmap != null) {
                bitmap = Utils.getRoundedCornerBitmap(bitmap);
            } else {
                Toast.makeText(getActivity(), "Something went wrong.Please set your pokemon again." +
                        "We're really sorry.", Toast.LENGTH_LONG).show();
                bitmap = Utils.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), getResources()
                        .getIdentifier("exeggutor", "drawable", getActivity().getPackageName())));
            }
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), getResources()
                    .getIdentifier(memoryDto.getImagePath(), "drawable", getActivity().getPackageName()));
        }
        memoryImage.setImageBitmap(bitmap);
        if (Utils.isTypePresent(memoryDto.getType().toLowerCase())) {
            memoryScreen.setBackground(new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
                    getResources().getIdentifier(memoryDto.getType().toLowerCase(), "drawable",
                            getActivity().getPackageName()))));
        } else {
            memoryScreen.setBackground(getResources().getDrawable(R.drawable.mainbackground));
        }
    }

    public void shareMyMemory() {
        memoryScreen.setDrawingCacheEnabled(true);
        memoryScreen.setDrawingCacheBackgroundColor(getResources().getColor(R.color.WHITE));
        Bitmap bitmap = Bitmap.createBitmap(memoryScreen.getDrawingCache());
        memoryScreen.setDrawingCacheEnabled(false);
        ((MainActivity) getActivity()).shareImage(bitmap);
    }

    public void saveMyMemory() {
        memoryScreen.setDrawingCacheEnabled(true);
        memoryScreen.setDrawingCacheBackgroundColor(getResources().getColor(R.color.WHITE));
        Bitmap bitmap = Bitmap.createBitmap(memoryScreen.getDrawingCache());
        memoryScreen.destroyDrawingCache();
        Utils.saveFile(getActivity(), bitmap, memoryDto.getId());
    }

}
