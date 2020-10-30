package mani.itachi.memories.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import mani.itachi.memories.uicomponents.SingleMediaScanner;

/**
 * Created by ManikantaInugurthi on 03-02-2017.
 */

public class Utils {

    public static int getRandomId() {
        return new Random().nextInt();
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();

        if(view == null) {
            Log.w(Utils.class.getCanonicalName(), "Hide keyboard called on null view");
            return;
        }

        InputMethodManager methodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        methodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int getDisplayHeight(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 50;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static boolean isTypePresent(String type) {
        String[] strings = {"birthday", "party", "food", "classroom"};
        for (String s : strings) {
            if (s.equals(type.trim())) {
                Log.d("True:string recieved:", type);
                return true;
            }
        }
        Log.d("False:string recieved", type);
        return false;
    }

    public static void saveFile(Context context, Bitmap bitmap, int id) {
        File mFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Memories");
        File file = new File(mFolder.getAbsolutePath() + File.separator + id + ".png");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("path:", file.getAbsolutePath());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    Toast.makeText(context, "Saved to gallery!", Toast.LENGTH_SHORT).show();
                    new SingleMediaScanner(context, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
