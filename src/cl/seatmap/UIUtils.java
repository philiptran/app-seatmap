package cl.seatmap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * 
 * @author philiptrannp
 * 
 */
public abstract class UIUtils {
	public static void showAlert(Context context, String title, String message) {
		showAlert(context, title, message, null, null);
	}

	public static void showAlert(Context context, String title, String message,
			String okBtnTitle) {
		showAlert(context, title, message, okBtnTitle, null);
	}

	public static void showAlert(Context context, String title, String message,
			DialogInterface.OnClickListener okClickListener) {
		showAlert(context, title, message, null, okClickListener);
	}

	public static void showAlert(Context context, String title, String message,
			String okBtnTitle, DialogInterface.OnClickListener okClickListener) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		alertBuilder.setTitle(title);
		alertBuilder.setMessage(message);
		alertBuilder.setPositiveButton(okBtnTitle != null ? okBtnTitle
				: "  OK  ", okClickListener);
		alertBuilder.create();
		//
		alertBuilder.show();
	}

	public static void showToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public static void hideSoftInput(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(view.getWindowToken(),
		// InputMethodManager.HIDE_IMPLICIT_ONLY);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static void showSoftInput(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	/**
	 * To also set android.permission.ACCESS_NETWORK_STATE in
	 * AndroidManifest.xml
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnected();
	}

	/**
	 * Return the current display size
	 * 
	 * @param context
	 * @return
	 */
	public static Point getScreenSize(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenOrientation(Context context) {
		final int rotation = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
		// .getOrientation();
				.getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			return Configuration.ORIENTATION_PORTRAIT;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			return Configuration.ORIENTATION_LANDSCAPE;
		default:
			return Configuration.ORIENTATION_UNDEFINED;
		}
	}

	public static int getFlagResource(String country) {
		String c = country != null ? country.toUpperCase() : "";
		if ("SINGAPORE".equals(c)) {
			return R.drawable.flag_sg;
		} else if ("INDIA".equals(c)) {
			return R.drawable.flag_india;
		} else if ("PHILIPPINES".equals(c)) {
			return R.drawable.flag_phil;
		} else if ("CANADA".equals(c)) {
			return R.drawable.flag_canada;
		} else {
			return R.drawable.flag_default;
		}
	}

	public static String nullSafe(String s) {
		return s == null || s.isEmpty() ? "NIL" : s;
	}

	public static String shortenName(String name) {
		return shortenName(name, 15);
	}

	public static String shortenName(String name, int maxLen) {
		if (name.length() <= maxLen) {
			return name;
		}
		return name.substring(0, maxLen - 4) + " ...";
	}
}
