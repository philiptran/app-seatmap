package cl.seatmap.widget;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

/**
 * 
 * @author philiptrannp
 * 
 */
public class FindContactAutoCompleteTextView extends RelativeLayout {
	final int DRAWABLE_LEFT = 0;
	final int DRAWABLE_TOP = 1;
	final int DRAWABLE_RIGHT = 2;
	final int DRAWABLE_BOTTOM = 3;
	//
	private AutoCompleteTextView textView;
	private FindContactAutoCompleteAdapter findContactAutoCompleteAdapter;
	private ExchangeContactService exchangeContactService;
	private Drawable searchIcon;
	private Drawable removeIcon;
	private TransitionDrawable backgroundTransition;
	//
	private ExchangeContact currentContact;
	private View currentContactView;
	private ImageView homeIcon;

	//
	public FindContactAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackground(getResources().getDrawable(R.drawable.bg_transition));
		searchIcon = getResources().getDrawable(R.drawable.ic_action_search);
		removeIcon = getResources().getDrawable(R.drawable.ic_action_remove);
		searchIcon.setBounds(0, 0, searchIcon.getIntrinsicWidth(),
				searchIcon.getIntrinsicHeight());
		removeIcon.setBounds(0, 0, removeIcon.getIntrinsicWidth(),
				removeIcon.getIntrinsicHeight());

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
		//
		textView = new AutoCompleteTextView(context);
		textView.setId(R.id.findcontact_textview);
		textView.setLayoutParams(params);
		textView.setSingleLine(true);
		textView.setLines(1);
		textView.setCursorVisible(false);
		textView.setTextColor(Color.BLACK);
		textView.setTextSize(18);
		textView.setBackground(getResources().getDrawable(
				R.drawable.bg_findcontact_textview));
		textView.setCompoundDrawables(searchIcon, null, null, null);
		textView.setCompoundDrawablePadding(5);
		textView.setPadding(10, 10, 10, 10);
		textView.setDropDownWidth(LayoutParams.MATCH_PARENT);
		textView.setDropDownVerticalOffset(0);
		textView.setDropDownHorizontalOffset(0);

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

	public void setCurrentContact(ExchangeContact contact) {
		this.currentContact = contact;

		// update the current contact view
		TextView nameText = (TextView) currentContactView
				.findViewById(R.id.name);
		nameText.setText(currentContact.getName());
		//
		TextView phoneText = (TextView) currentContactView
				.findViewById(R.id.phone);
		phoneText.setText(currentContact.getPhone());
		//
		TextView locationText = (TextView) currentContactView
				.findViewById(R.id.location);
		locationText.setText(currentContact.getOfficeLocation());
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
		departmentText.setText(currentContact.getDepartment());
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
		this.textView.setText(null);
	}

	// /**
	// * Set click listener for the Nearby text of the
	// * <code>currentContactView</code>
	// *
	// * @param listener
	// */
	// public void setNearbyTextOnClickListener(View.OnClickListener listener) {
	// TextView nearbyText = (TextView) currentContactView
	// .findViewById(R.id.nearby);
	// nearbyText.setOnClickListener(listener);
	// }

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
				if (event.getX() <= searchIcon.getBounds().width()) {
					Log.d(MainActivity.TAG,
							"Search icon is touched. Do nothing.");
					return true;
				} else if (event.getX() >= textView.getWidth()
						- removeIcon.getBounds().width()) {
					textView.setText("");
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
	private Debouncer<String> findContactDebouncer = new Debouncer<String>(
			new Debouncer.Function<String>() {
				@Override
				public void call(String text) {
					new FindContactAsyncTask(text).execute();
				}
			}, 300);
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
				textView.setCompoundDrawables(searchIcon, null, removeIcon,
						null);
				// debounce the search
				findContactDebouncer.call(text);
			} else {
				textView.setCompoundDrawables(searchIcon, null, null, null);
				// destroy pending task
				findContactDebouncer.destroy();
			}
		}
	};

	private class FindContactAsyncTask extends
			AsyncTask<String, Void, List<ExchangeContact>> {
		private String text;

		public FindContactAsyncTask(String text) {
			this.text = text;
		}

		@Override
		protected List<ExchangeContact> doInBackground(String... params) {
			try {
				return exchangeContactService.findContact(text);
			} catch (Exception e) {
				Log.e("RetrieveContactAsyncTask", "Fail to find contact '"
						+ text + "'", e);
				return null; // to tell onPostExecute that an error has
								// occurred.
			}
		}

		@Override
		protected void onPostExecute(List<ExchangeContact> contacts) {
			if (this.text.equalsIgnoreCase(textView.getText().toString())) {
				if (contacts == null) {
					UIUtils.showAlert(getContext(), "Error",
							"Exchang service is not available.\r\nPlease try again later.");
				} else if (contacts.isEmpty()) {
					UIUtils.showAlert(getContext(), "Not Found",
							"Sorry, the name could not be found.");
				} else {
					findContactAutoCompleteAdapter.clear();
					findContactAutoCompleteAdapter.setSearchText(this.text);
					findContactAutoCompleteAdapter.addAll(contacts);
					findContactAutoCompleteAdapter.notifyDataSetChanged();
					// hide the home icon
					homeIcon.setVisibility(View.INVISIBLE);
				}
			}
		}
	}
}
