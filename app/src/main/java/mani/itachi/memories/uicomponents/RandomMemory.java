package mani.itachi.memories.uicomponents;

import android.app.Application;
import android.util.Log;

import java.util.List;
import java.util.Random;

import mani.itachi.memories.database.DbHelper;
import mani.itachi.memories.database.MemoryDto;

/**
 * Created by ManikantaInugurthi on 03-02-2017.
 */

public class RandomMemory extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        dbHelper.setMyCardsList();
    }

}