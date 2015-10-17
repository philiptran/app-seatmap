package cl.seatmap.widget;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cl.seatmap.MainActivity;
import cl.seatmap.R;
import cl.seatmap.UIUtils;
import cl.seatmap.domain.ExchangeContact;
import cl.seatmap.service.ExchangeContactService;
import cl.seatmap.utils.Debouncer;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

/**
 * 
 * @author philiptrannp
 * 
 */
public class FindContactAutoCompleteTextView extends RelativeLayout {
	private static final int DEBOUNCER_DELAY_MS = 200;
	//
	private AutoCompleteTextView textView;
	private FindContactAutoCompleteAdapter findContactAutoCompleteAdapter;
	private ExchangeContactService exchangeContactService;
	private Drawable menuIcon;
	private Drawable removeIcon;
	private Drawable micIcon;
	private TransitionDrawable backgroundTransition;
	private OnSpeakToMeListener onSpeakToItListener;
	//
	private ExchangeContact currentContact;
	private View currentContactView;
	private ImageView homeIcon;

	//
	public FindContactAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackground(getResources().getDrawable(R.drawable.bg_transition));
		menuIcon = getResources().getDrawable(R.drawable.ic_menu);
		menuIcon.setBounds(0, 0, 35, 35);
		removeIcon = getResources().getDrawable(R.drawable.ic_remove);
		removeIcon.setBounds(0, 0, 40, 40);
		micIcon = getResources().getDrawable(R.drawable.ic_mic);
		micIcon.setBounds(0, 0, 40, 40);
		// Main Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setLayoutParams(params);

		// These two properties kills the softInput
		// setFocusable(true);
		// setFocusableInTouchMode(true);

		// Create Home Image
		Point displaySize = UIUtils.getScreenSize(context);
		double w = displaySize.x;
		double h = 0.8 * displaySize.y;
		params = new RelativeLayout.LayoutParams((int) w, (int) h);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		homeIcon = new ImageView(context);
		homeIcon.setId(R.id.home_icon);
		homeIcon.setImageResource(R.drawable.home);
		homeIcon.setOnClickListener(homeIconOnClickListener);
		addView(homeIcon, params);

		// Create AutoCompleteTextView
		params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.setMargins(20, 20, 20, 20);
		//
		textView = new AutoCompleteTextView(context);
		textView.setId(R.id.findcontact_textview);
		textView.setLayoutParams(params);
		textView.setSingleLine(true);
		textView.setLines(1);
		textView.setCursorVisible(false);
		textView.setTextColor(Color.BLACK);
		textView.setTextSize(16);
		textView.setBackground(getResources().getDrawable(
				R.drawable.bg_findcontact_textview));
		//
		textView.setCompoundDrawables(menuIcon, null, micIcon, null);
		textView.setCompoundDrawablePadding(30);
		textView.setPadding(25, 25, 25, 25);
		textView.setDropDownWidth(LayoutParams.MATCH_PARENT);
		textView.setDropDownHeight(LayoutParams.MATCH_PARENT);
		textView.setDropDownVerticalOffset(0);
		textView.setDropDownHorizontalOffset(-20);
		// overwrite default 3d background for dropdown list
		textView.setDropDownBackgroundDrawable(getResources().getDrawable(
				R.drawable.background_dropdown));
		textView.setHint(R.string.hint_search);

		// no fullscreen mode in landscape
		textView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		addView(textView);

		// Create CurrentContactView and hide it
		currentContactView = LayoutInflater.from(context).inflate(
				R.layout.current_contact_layout, null);
		params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		currentContactView.setLayoutParams(params);
		//
		addView(currentContactView);
		currentContactView.setVisibility(INVISIBLE);
		//
		backgroundTransition = (TransitionDrawable) getResources().getDrawable(
				R.drawable.bg_transition);
		//
		TextView phoneText = (TextView) currentContactView
				.findViewById(R.id.phone);
		phoneText.setOnClickListener(phoneTextOnClickListener);

		TextView mobileText = (TextView) currentContactView
				.findViewById(R.id.mobile);
		mobileText.setOnClickListener(mobileTextOnClickListener);

		View closeIcon = (View) currentContactView
				.findViewById(R.id.close_icon);
		closeIcon.setOnClickListener(currentContactCloseIconOnClickListener);

		if (!isInEditMode()) {
			// Initialization code that is not letting the Visual Editor draw
			// properly to be placed in this block.

			exchangeContactService = new ExchangeContactService(context);
			//
			findContactAutoCompleteAdapter = new FindContactAutoCompleteAdapter(
					context, null);
			//
			textView.setOnTouchListener(findContactOnTouchListener);
			textView.addTextChangedListener(findContactTextWatcher);
			textView.setOnEditorActionListener(findContactOnEditorActionListener);
			textView.setAdapter(findContactAutoCompleteAdapter);
		}
	}

	private View.OnClickListener homeIconOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			textView.setText("");
			UIUtils.showSoftInput(textView);
		}
	};
	private View.OnClickListener currentContactCloseIconOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			hideCurrentContactView();
		}
	};
	private View.OnClickListener phoneTextOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String number = "tel:" + currentContact.getPhone();
			Intent callIntent = new Intent(Intent.ACTION_CALL,
					Uri.parse(number));
			v.getContext().startActivity(callIntent);
		}
	};
	private View.OnClickListener mobileTextOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String number = "tel:" + currentContact.getMobile();
			Intent callIntent = new Intent(Intent.ACTION_CALL,
					Uri.parse(number));
			v.getContext().startActivity(callIntent);
		}
	};

	public void setCurrentContact(ExchangeContact contact) {
		this.currentContact = contact;

		// update the current contact view
		TextView nameText = (TextView) currentContactView
				.findViewById(R.id.name);
		nameText.setText(currentContact.getName());
		//
		TextView titleText = (TextView) currentContactView
				.findViewById(R.id.title);
		titleText.setText(UIUtils.nullSafe(currentContact.getTitle()));
		//
		TextView phoneText = (TextView) currentContactView
				.findViewById(R.id.phone);
		phoneText.setText(UIUtils.nullSafe(currentContact.getPhone()));

		// optional mobile number
		ViewGroup vg = (ViewGroup) currentContactView
				.findViewById(R.id.layout_mobile);
		String mobile = contact.getMobile();
		if (mobile == null || mobile.isEmpty()) {
			vg.setVisibility(View.GONE);
		} else {
			TextView mobileText = (TextView) currentContactView
					.findViewById(R.id.mobile);
			mobileText.setText(mobile);
			vg.setVisibility(View.VISIBLE);
		}
		//
		TextView locationText = (TextView) currentContactView
				.findViewById(R.id.location);
		locationText.setText(UIUtils.nullSafe(currentContact
				.getOfficeLocation()));
		//
		ImageView flagView = (ImageView) currentContactView
				.findViewById(R.id.flag_icon);
		flagView.setImageResource(UIUtils.getFlagResource(contact.getCountry()));
		//
		TextView levelText = (TextView) currentContactView
				.findViewById(R.id.level);
		levelText.setText("L" + currentContact.getContactLocation().getLevel());
		//
		TextView departmentText = (TextView) currentContactView
				.findViewById(R.id.department);
		departmentText
				.setText(UIUtils.nullSafe(currentContact.getDepartment()));
	}

	public void setOnItemClickListener(
			AdapterView.OnItemClickListener clickListener) {
		this.textView.setOnItemClickListener(clickListener);
	}

	/**
	 * Set click listener for the Location text of the
	 * <code>currentContactView</code>
	 * 
	 * @param listener
	 */
	public void setLocationTextOnClickListener(View.OnClickListener listener) {
		TextView locationText = (TextView) currentContactView
				.findViewById(R.id.location);
		locationText.setOnClickListener(listener);
	}

	public void clearText() {
		setText(null);
	}

	public void setText(String text) {
		this.textView.setText(text);
	}

	public void showCurrentContactView() {
		currentContactView.setVisibility(VISIBLE);
	}

	public void hideCurrentContactView() {
		currentContactView.setVisibility(INVISIBLE);
	}

	public void maximize() {
		textView.setText("");
		hideCurrentContactView();
		homeIcon.setVisibility(View.VISIBLE);
		//
		setBackground(backgroundTransition);
		backgroundTransition.reverseTransition(1000);
	}

	public void minimize() {
		homeIcon.setVisibility(View.INVISIBLE);
		textView.setCursorVisible(false);
		textView.setText(null);
		//
		if (!TransitionDrawable.class.isInstance(getBackground())) {
			setBackground(backgroundTransition);
		}
		((TransitionDrawable) getBackground()).startTransition(1000);
		// Background transition can not set to completely transparent.
		setBackgroundColor(getResources().getColor(android.R.color.transparent));

		// hide soft input
		UIUtils.hideSoftInput(textView);
	}

	private OnEditorActionListener findContactOnEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			int result = actionId & EditorInfo.IME_MASK_ACTION;
			switch (result) {
			case EditorInfo.IME_ACTION_DONE:
			case EditorInfo.IME_ACTION_NEXT:
				// hide soft input
				UIUtils.hideSoftInput(v);
				//
				String text = textView.getText().toString().trim();
				if (text.length() != 0
						&& findContactAutoCompleteAdapter.getCount() == 0) {
					textView.setText(null);
					// UIUtils.showSoftInput(v);
					return true;
				}
				break;
			}
			//
			return false;
		}
	};
	private View.OnTouchListener findContactOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.performClick(); // WHY?
			//
			if (!UIUtils.isNetworkAvailable(getContext())) {
				UIUtils.showAlert(
						getContext(),
						"Network Error",
						"Network is not available. Turn on your WiFi or Data and try again.",
						"Try Again");
				return true;
			}
			//
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (event.getX() <= menuIcon.getBounds().width() + 50) {
					Log.d(MainActivity.TAG, "Menu icon is touched. Do nothing.");
					UIUtils.showAlert(getContext(), "Test",
							"Menu Icon is touched");
					return true;
				} else if (event.getX() >= textView.getWidth()
						- micIcon.getBounds().width() - 50) {
					if (textView.getText().toString().isEmpty()) {
						// micIcon is touched.
						if (onSpeakToItListener != null) {
							onSpeakToItListener.startVoiceRecognitionActivity();
						}
						return true;
					} else {
						// removeIcon is touched.
						textView.setText(null);
					}
				}
				//
				UIUtils.showSoftInput(textView);
				maximize();
				return true;
			}
			//
			return false;
		}
	};

	//
	private class FindContactResponseListener implements
			Listener<List<ExchangeContact>>, ErrorListener {
		private String mText;

		public FindContactResponseListener(final String text) {
			this.mText = text;
		}

		private boolean discardResponse() {
			// should discard the response as user has changed the text
			return !mText.equals(textView.getText().toString().trim());
		}

		@Override
		public void onResponse(List<ExchangeContact> contacts) {
			if (!discardResponse()) {
				if (contacts.isEmpty()) {
					UIUtils.showAlert(getContext(), "Contact Not Found",
							"Sorry, we could not find the name.");
				} else {
					findContactAutoCompleteAdapter.clear();
					findContactAutoCompleteAdapter.setSearchText(mText);
					findContactAutoCompleteAdapter.addAll(contacts);
					findContactAutoCompleteAdapter.notifyDataSetChanged();
					// hide the home icon
					homeIcon.setVisibility(View.INVISIBLE);
				}
			}
		}

		@Override
		public void onErrorResponse(VolleyError error) {
			if (!discardResponse()) {
				UIUtils.showAlert(getContext(), "Error",
						"Exchang service is not available."
								+ "\r\nPlease try again later." + "\r\nCause: "
								+ error.getMessage());
			}
		}
	}

	private Debouncer<String> findContactDebouncer = new Debouncer<String>(
			new Debouncer.Function<String>() {
				@Override
				public void call(String text) {
					FindContactResponseListener listener = new FindContactResponseListener(
							text);
					exchangeContactService.findContact(text, listener, listener);
				}
			}, DEBOUNCER_DELAY_MS);
	//
	private TextWatcher findContactTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// do nothing
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// do nothing
		}

		@Override
		public void afterTextChanged(Editable s) {
			// display remove text icon only if there is text
			String text = textView.getText().toString().trim();
			if (!text.isEmpty()) {
				textView.setCompoundDrawables(menuIcon, null, removeIcon, null);
				// debounce the search
				findContactDebouncer.call(text);
			} else {
				textView.setCompoundDrawables(menuIcon, null, micIcon, null);
				// destroy pending task
				findContactDebouncer.destroy();
			}
		}
	};

	public void setOnSpeakToMeListener(OnSpeakToMeListener listener) {
		this.onSpeakToItListener = listener;
	}

	public interface OnSpeakToMeListener {
		void startVoiceRecognitionActivity();
	}
}
