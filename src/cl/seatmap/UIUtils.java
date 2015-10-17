package cl.seatmap;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.text.Spanned;
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

	public static boolean isSpeechAvailable(Context context) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		return activities.size() > 0;
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
		if (c.contains("SINGAPORE")) {
			return R.drawable.flag_sg;
		} else if (c.contains("INDIA")) {
			return R.drawable.flag_india;
		} else if (c.contains("PHILIPPINES")) {
			return R.drawable.flag_phil;
		} else if (c.contains("CANADA")) {
			return R.drawable.flag_canada;
		} else if (c.contains("OMAN")) {
			return R.drawable.flag_oman;
		} else if (c.contains("PANAMA")) {
			return R.drawable.flag_panama;
		} else if (c.contains("UAE")) {
			return R.drawable.flag_uae;
		} else if (c.contains("USA")) {
			return R.drawable.flag_us;
		} else if (c.contains("MAURITIUS")) {
			return R.drawable.flag_mauritius;
		} else {
			return R.drawable.flag_default;
		}
	}

	public static String nullSafe(String s) {
		return s == null || s.isEmpty() ? "NIL" : s;
	}

	public static Spanned highlight(String source, String text) {
		source = UIUtils.nullSafe(source);
		source = source.replaceAll("(?i)(" + text + ")",
				"<font color='#19A3A3'><b>$1</b></font>");
		return Html.fromHtml(source);
	}
}
