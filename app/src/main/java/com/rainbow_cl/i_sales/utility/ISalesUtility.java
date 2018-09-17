package com.rainbow_cl.i_sales.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by netserve on 30/08/2018.
 */

public final class ISalesUtility {
    private static final String TAG = ISalesUtility.class.getSimpleName();
    private static String ENCODE_IMG = "&img";
    private static String ENCODE_CAROUSEL = "&amp;carousel;";

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 156);
        return noOfColumns;
    }

    // Renvoi le nom de l'image du produit a partir de la description
    public static String getImgProduit(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }

//        Log.e(TAG, "getImgProduit: description="+description);
        // extraction de la chaine apres code encodage de la photo
        String[] descriptionTab = description.split(ENCODE_IMG);
//        Log.e(TAG, "getImgProduit: descriptionTab before length="+descriptionTab.length);
        // S'il n'ya pas d'encode de img, on renvoi null
        if (descriptionTab.length > 2) {
            return null;
        }
//        Log.e(TAG, "getImgProduit: descriptionTab after length="+descriptionTab.length);
        // extraction de la chaine avant code ':&'
        /*String[] imgTab = descriptionTab[1].split(":&");
        if (imgTab.length <= 1) {
            return null;
        }*/
        // console.log(this.TAG, "descriptionTab:getImgProduit ", descriptionTab);
        // console.log(this.TAG, "descriptionTab:imgTab ", imgTab);
        return descriptionTab[1];
    }

    // Renvoi le nom les images carousel du produit a partir de la description
    public static String[] getCarouselProduit(String description) {

        String[] carousel;
        // extraction de la chaine apres code encodage de la photo
        String[] descriptionTab = description.split(ENCODE_CAROUSEL);
        Log.e(TAG, "descriptionTab:getCarouselProduit "+descriptionTab.toString());
        // S'il n'ya pas d'encode de img, on renvoi null
        if (descriptionTab.length <= 1) {
            return null;
        }
        // extraction de la chaine avant code ':'
        String[] carouselTab = descriptionTab[1].split(":&");
        Log.e(TAG, "descriptionTab:carouselTab "+carouselTab.toString());
        if (carouselTab.length <= 1) {
            return null;
        }
        carousel = carouselTab[0].split(":");
        return carousel;
    }

//    Arromdi les bord d'une image bitmap
    public static Bitmap getRoundedCornerBitmap(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }
}