package mani.itachi.memories.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mani.itachi.memories.utils.CollectionUtils;


/**
 * Created by ManikantaInugurthi on 02-02-2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final class DBHelperConstants {

        public static final String KEY_NAME = "name";
        public static final String KEY_DATE = "hp";
        public static final String KEY_BITMAP_PATH = "image";
        public static final String KEY_IMAGE_PATH = "imagePath";
        public static final String KEY_TYPE = "type";
        public static final String KEY_DESC = "desc";
        private static final int DATABASE_VERSION = 11;

        private static final String DATABASE_NAME = "Itachi";
        private static final String TABLE_MY_CARDS = "myMemories";

        private static final String KEY_ID = "id";
        private static final String INT_DATA_TYPE = "INTEGER";
        private static final String VARCHAR_DATA_TYPE = "VARCHAR";
        private static final String COLUMN_FORMAT = "%s %s %s";
        private static final String CREATE_TABLE_MY_CARD = "CREATE TABLE "
                + TABLE_MY_CARDS + "(" +
                String.format(COLUMN_FORMAT, KEY_ID, INT_DATA_TYPE, "PRIMARY KEY,") +
                String.format(COLUMN_FORMAT, KEY_NAME, VARCHAR_DATA_TYPE, ",") +
                String.format(COLUMN_FORMAT, KEY_TYPE, VARCHAR_DATA_TYPE, ",") +
                String.format(COLUMN_FORMAT, KEY_DATE, VARCHAR_DATA_TYPE, ",") +
                String.format(COLUMN_FORMAT, KEY_DESC, VARCHAR_DATA_TYPE, ",") +
                String.format(COLUMN_FORMAT, KEY_IMAGE_PATH, VARCHAR_DATA_TYPE, ",") +
                String.format(COLUMN_FORMAT, KEY_BITMAP_PATH, VARCHAR_DATA_TYPE, "") +
                ")";

    }

    @SuppressLint("StaticFieldLeak")
    public static DbHelper dbHelper;
    private final Context context;
    private MemoryDto mMemoryDto = null;
    private static final List<MemoryDto> cardsListCache = new ArrayList<>();
    private final List<String> cardNamesCache = new ArrayList<>();
    private Runnable setMyCardsListRunnable;

    private DbHelper(Context context) {
        super(context, DBHelperConstants.DATABASE_NAME, null, DBHelperConstants.DATABASE_VERSION);
        this.context = context;
    }

    public static DbHelper getInstance(Context context) {
        if (dbHelper == null)
            dbHelper = new DbHelper(context);
        return dbHelper;
    }

    public static DbHelper getInstance() {
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBHelperConstants.CREATE_TABLE_MY_CARD);
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
        db.execSQL("DROP TABLE IF EXISTS " + DBHelperConstants.TABLE_MY_CARDS);
        onCreate(db);
    }

    public boolean saveMyCard(MemoryDto cardsDto) {
        cardsListCache.add(cardsDto);
        mMemoryDto = new MemoryDto();
        mMemoryDto = cardsDto;
        Runnable runnable = () -> {
            SQLiteDatabase db = DbHelper.this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBHelperConstants.KEY_ID, mMemoryDto.getId());
            values.put(DBHelperConstants.KEY_NAME, mMemoryDto.getName());
            values.put(DBHelperConstants.KEY_TYPE, mMemoryDto.getType());
            values.put(DBHelperConstants.KEY_DATE, mMemoryDto.getDate());
            values.put(DBHelperConstants.KEY_DESC, mMemoryDto.getDesc());
            values.put(DBHelperConstants.KEY_IMAGE_PATH, mMemoryDto.getImagePath());
            values.put(DBHelperConstants.KEY_BITMAP_PATH, mMemoryDto.getBitmapPath());
            db.insert(DBHelperConstants.TABLE_MY_CARDS, null, values);
        };
        return execInThread(runnable);
    }

    public boolean updateMyCard(MemoryDto cardsDto) {
        MemoryDto temp = null;
        for (MemoryDto dto : cardsListCache) {
            if (dto.getId() == cardsDto.getId()) {
                temp = dto;
                break;
            }
        }
        if (temp != null) {
            cardsListCache.remove(temp);
        }
        cardsListCache.add(cardsDto);
        mMemoryDto = new MemoryDto();
        mMemoryDto = cardsDto;
        Runnable runnable = () -> {
            SQLiteDatabase db = DbHelper.this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBHelperConstants.KEY_ID, mMemoryDto.getId());
            values.put(DBHelperConstants.KEY_NAME, mMemoryDto.getName());
            values.put(DBHelperConstants.KEY_TYPE, mMemoryDto.getType());
            values.put(DBHelperConstants.KEY_DATE, mMemoryDto.getDate());
            values.put(DBHelperConstants.KEY_DESC, mMemoryDto.getDesc());
            values.put(DBHelperConstants.KEY_IMAGE_PATH, mMemoryDto.getImagePath());
            values.put(DBHelperConstants.KEY_BITMAP_PATH, mMemoryDto.getBitmapPath());

            db.update(DBHelperConstants.TABLE_MY_CARDS, values, "id = ?", new String[]{mMemoryDto.getId() + ""});
        };
        return execInThread(runnable);
    }

    public List<MemoryDto> getAllCards() {
        if (CollectionUtils.isEmpty(cardsListCache)) {
            List<MemoryDto> mem = refreshAllCardsCache();
            Log.v("tag", String.valueOf(mem.size()));
            return mem;
        } else {
            return cardsListCache;
        }
    }

    public List<MemoryDto> refreshAllCardsCache() {
        setMyCardsListRunnable = () -> {
            cardsListCache.clear();
            String selectQuery = "SELECT  * FROM " + DBHelperConstants.TABLE_MY_CARDS;

            SQLiteDatabase db = DbHelper.this.getReadableDatabase();
            @SuppressLint("Recycle") Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    MemoryDto dto = new MemoryDto();
                    dto.setId(c.getInt(c.getColumnIndex(DBHelperConstants.KEY_ID)));
                    dto.setName(c.getString(c.getColumnIndex(DBHelperConstants.KEY_NAME)));
                    dto.setBitmapPath(c.getString(c.getColumnIndex(DBHelperConstants.KEY_BITMAP_PATH)));
                    dto.setType(c.getString(c.getColumnIndex(DBHelperConstants.KEY_TYPE)));
                    dto.setDesc(c.getString(c.getColumnIndex(DBHelperConstants.KEY_DESC)));
                    dto.setDate(c.getString(c.getColumnIndex(DBHelperConstants.KEY_DATE)));
                    cardsListCache.add(dto);
                    Log.v("added", dto.getName());
                    cardNamesCache.add(c.getString(c.getColumnIndex(DBHelperConstants.KEY_NAME)));
                } while (c.moveToNext());
            }
        };
        execInThread(setMyCardsListRunnable);
        return cardsListCache;
    }

    public int deleteCard(long id) {
        Log.d("TAG", "deleting" + id + "");
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            File imageFile = new File(context.getFilesDir() + File.separator + id + ".png");
            File thumbNailFile = new File(context.getFilesDir() + File.separator + id + "thumb.png");
            Files.delete(imageFile.toPath());
            Files.delete(thumbNailFile.toPath());
        } catch (Exception e) {
            Log.e("Error occurred when deleting the card", String.valueOf(e));
            Toast.makeText(context, "Error occurred", Toast.LENGTH_LONG).show();
            return -1;
        }
        int response = db.delete(DBHelperConstants.TABLE_MY_CARDS, DBHelperConstants.KEY_ID + " = ?", new String[]{String.valueOf(id)});
        if (response != 0) {
            return response;
        }
        execInThread(setMyCardsListRunnable);
        return 0;
    }

}
