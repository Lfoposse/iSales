package com.iSales.utility;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

/**
 * Created by netserve on 28/12/2018.
 */

public class InputFilterMinMax implements InputFilter {
    private static final String TAG = com.iSales.utility.InputFilterMinMax.class.getSimpleName();

    private int min, max;
    int input;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {

            if ((dest.toString() + source.toString()).equals("-")) {
                source = "-1";
            }

            input = Integer.parseInt(dest.toString() + source.toString());
            Log.e(TAG, "filter: start="+start+" end="+end+" dest="+dest+" source="+source+" input="+input);
//            if (source.equals("") && dest.length() == 1) {
//                return "0";
//            }
            if (isInRange(min, max, input))
                return null;

        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
//        return b > a ? c >= a && c <= b : c >= b && c <= a;
        return c >= a ;
    }
}
