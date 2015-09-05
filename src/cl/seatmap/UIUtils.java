package cl.seatmap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
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
		// imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		// SHOW_IMPLICIT does not work in landscape mode
		imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
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
}
