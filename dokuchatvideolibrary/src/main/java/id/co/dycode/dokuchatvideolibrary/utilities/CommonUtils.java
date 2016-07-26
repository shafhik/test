package id.co.dycode.dokuchatvideolibrary.utilities;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by fahmi on 07/07/2016.
 */
public class CommonUtils {
    public static void hideKeyboard(final View editText) {
        editText.post(new Runnable() {
            @Override
            public void run() {
                try {
                    InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                } catch (Exception ignored) {
                }
            }
        });

    }

    public static void showKeyboard(final View editText) {
        editText.requestFocus();
        editText.post(new Runnable() {
            @Override
            public void run() {
                try {
                    InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, 0);
                } catch (Exception ignored) {
                }
            }
        });

    }
}
