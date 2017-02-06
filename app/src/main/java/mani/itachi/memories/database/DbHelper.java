package mani.itachi.memories.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.media.MediaFormat.KEY_HEIGHT;
import static android.media.MediaFormat.KEY_LEVEL;

/**
 * Created by ManikantaInugurthi on 02-02-2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final String KEY_NAME = "name";
    public static final String KEY_DATE = "hp";
    public static final String KEY_BITMAP_PATH = "image";
    public static final String KEY_IMAGE_PATH = "imagePath";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DESC = "desc";
    private static final int DATABASE_VERSION = 11;

    // Database Name
    private static final String DATABASE_NAME = "Itachi";
    private static final String TABLE_MYCARDS = "myMemories";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String CREATE_TABLE_MYCARD = "CREATE TABLE "
            + TABLE_MYCARDS + "(" +
            KEY_ID + " INTEGER PRIMARY KEY," +
            KEY_NAME + " VARCHAR," +
            KEY_TYPE + " VARCHAR," +
            KEY_DATE + " VARCHAR," +
            KEY_DESC + " VARCHAR," +
            KEY_IMAGE_PATH + " VARCHAR," +
            KEY_BITMAP_PATH + " VARCHAR" +
            ")";

    public static DbHelper mDbHelper = null;
    private Context mContext;
    private MemoryDto mMemoryDto = null;
    private static List<MemoryDto> myCardsList = new ArrayList<MemoryDto>();
    private List<String> myCardsNameList = new ArrayList<String>();
    private Runnable setMyCardsListRunnable;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mDbHelper = this;
    }

    public static DbHelper getInstance() {
        return mDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_MYCARD);
    }

    private boolean execInThread(Runnable r) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(r);
        service.shutdown();
        Log.d("TAG", "end time" + System.currentTimeMillis() + "");
        return true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYCARDS);
        // create new tables
        onCreate(db);
    }

    public boolean saveMyCard(MemoryDto cardsDto) {
        myCardsList.add(cardsDto);
        mMemoryDto = new MemoryDto();
        mMemoryDto = cardsDto;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DbHelper.this.getWritableDatabase();
                
                ContentValues values = new ContentValues();
                values.put(KEY_ID, mMemoryDto.getId());
                values.put(KEY_NAME, mMemoryDto.getName());
                values.put(KEY_TYPE, mMemoryDto.getType());
                values.put(KEY_DATE, mMemoryDto.getDate());
                values.put(KEY_DESC, mMemoryDto.getDesc());
                values.put(KEY_IMAGE_PATH, mMemoryDto.getImagePath());
                values.put(KEY_BITMAP_PATH, mMemoryDto.getBitmapPath());
                db.insert(TABLE_MYCARDS, null, values);
            }
        };
        return execInThread(runnable);
    }

    public boolean updateMyCard(MemoryDto cardsDto) {
        MemoryDto temp = null;
        for (MemoryDto dto : myCardsList) {
            if (dto.getId() == cardsDto.getId()) {
                temp = dto;
                break;
            }
        }
        if (temp != null) {
            myCardsList.remove(temp);
        }
        myCardsList.add(cardsDto);
        mMemoryDto = new MemoryDto();
        mMemoryDto = cardsDto;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DbHelper.this.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_ID, mMemoryDto.getId());
                values.put(KEY_NAME, mMemoryDto.getName());
                values.put(KEY_TYPE, mMemoryDto.getType());
                values.put(KEY_DATE, mMemoryDto.getDate());
                values.put(KEY_DESC, mMemoryDto.getDesc());
                values.put(KEY_IMAGE_PATH, mMemoryDto.getImagePath());
                values.put(KEY_BITMAP_PATH, mMemoryDto.getBitmapPath());
                db.update(TABLE_MYCARDS, values, "id = ?", new String[]{mMemoryDto.getId() + ""});
            }
        };
        return execInThread(runnable);
    }

    public List<MemoryDto> getAllMyCards() {
        if (myCardsList == null) {
            List<MemoryDto> mem= setMyCardsList();
            Log.v("tag",String.valueOf(mem.size()));
            return mem;
        } else {
            return myCardsList;
        }
    }

    public List<String> getMyCardsNameList() {
        return myCardsNameList;
    }

    public List<MemoryDto> setMyCardsList() {
        setMyCardsListRunnable = new Runnable() {
            @Override
            public void run() {
                myCardsList.clear();
                String selectQuery = "SELECT  * FROM " + TABLE_MYCARDS;

                SQLiteDatabase db = DbHelper.this.getReadableDatabase();
                Cursor c = db.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                if (c.moveToFirst()) {
                    do {
                        MemoryDto dto = new MemoryDto();
                        dto.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                        dto.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                        dto.setBitmapPath(c.getString(c.getColumnIndex(KEY_BITMAP_PATH)));
                        dto.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
                        dto.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));
                        dto.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                        myCardsList.add(dto);
                        Log.v("added",dto.getName());
                        myCardsNameList.add(c.getString(c.getColumnIndex(KEY_NAME)));
                    } while (c.moveToNext());
                }
            }
        };
        execInThread(setMyCardsListRunnable);
        return myCardsList;
    }

    public int deleteCard(long id) {
        Log.d("TAG", "deleting" + id + "");
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            File file = new File(mContext.getFilesDir() + File.separator + id + ".png");
            if (file.exists()) {
                file.delete();
            }
            file = new File(mContext.getFilesDir() + File.separator + id + "thumb.png");
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int temp = db.delete(TABLE_MYCARDS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        if (temp != 0) {
            if (execInThread(setMyCardsListRunnable)) {
                return temp;
            }
        }
        return 0;
    }

}
