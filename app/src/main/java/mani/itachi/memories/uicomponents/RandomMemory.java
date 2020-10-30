package mani.itachi.memories.uicomponents;

import android.app.Application;

import mani.itachi.memories.database.DbHelper;

/**
 * Created by ManikantaInugurthi on 03-02-2017.
 */

public class RandomMemory extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DbHelper dbHelper = DbHelper.getInstance();
        dbHelper.refreshAllCardsCache();
    }

}