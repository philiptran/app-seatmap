package cl.seatmap.widget;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cl.seatmap.MainActivity;
import cl.seatmap.R;
import cl.seatmap.UIUtils;
import cl.seatmap.domain.ExchangeContact;
import cl.seatmap.service.ExchangeContactService;

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
		//
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		//
		params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		//
		textView = new AutoCompleteTextView(context);
		textView.setId(R.id.findcontact_textview);
		textView.setLayoutParams(params);
		textView.setSingleLine(true);
		textView.setLines(1);
		textView.setTextIsSelectable(true);
		textView.setTextColor(Color.BLACK);
		textView.setTextSize(18);
		textView.setBackground(getResources().getDrawable(
				R.drawable.text_border));
		textView.setCompoundDrawables(searchIcon, null, null, null);
		textView.setCompoundDrawablePadding(5);
		textView.setPadding(10, 10, 10, 10);
		textView.setDropDownWidth(LayoutParams.MATCH_PARENT);
		textView.setDropDownVerticalOffset(0);
		textView.setDropDownHorizontalOffset(0);
		textView.setDropDownBackgroundDrawable(getResources().getDrawable(
				R.drawable.background_dropdown));
		textView.setHint(R.string.hint_search);
		textView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		//
		addView(textView);
		//
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

		if (!isInEditMode()) {
			// Initialization code that is not letting the Visual Editor draw
			// properly to be placed in this block.

			exchangeContactService = new ExchangeContactService(context);

			textView.setOnTouchListener(findContactOnTouchListener);
			textView.addTextChangedListener(findContactTextWatcher);
			textView.setOnEditorActionListener(findContactOnEditorActionListener);
			//
			findContactAutoCompleteAdapter = new FindContactAutoCompleteAdapter(
					context, null);
			textView.setAdapter(findContactAutoCompleteAdapter);
		}
	}

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

	/**
	 * Set click listener for the Nearby text of the
	 * <code>currentContactView</code>
	 * 
	 * @param listener
	 */
	public void setNearbyTextOnClickListener(View.OnClickListener listener) {
		TextView nearbyText = (TextView) currentContactView
				.findViewById(R.id.nearby);
		nearbyText.setOnClickListener(listener);
	}

	public void minimize() {
		textView.setCursorVisible(false);
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

	public void toggleCurrentContactView() {
		if (currentContactView.getVisibility() == VISIBLE) {
			currentContactView.setVisibility(INVISIBLE);
		} else {
			currentContactView.setVisibility(VISIBLE);
		}
	}

	public void showCurrentContactView() {
		currentContactView.setVisibility(VISIBLE);
	}

	public void hideCurrentContactView() {
		currentContactView.setVisibility(INVISIBLE);
	}

	public void maximize() {
		textView.setCursorVisible(true);
		//
		setBackground(backgroundTransition);
		backgroundTransition.reverseTransition(1000);
		//
		currentContactView.setVisibility(INVISIBLE);
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
				String name = v.getText().toString().trim();
				if (name.length() == 0
						|| findContactAutoCompleteAdapter.getCount() != 0) {
					break;
				}

				//
				new AsyncTask<String, Void, List<ExchangeContact>>() {
					/**
					 * Return NULL if there is error.
					 */
					@Override
					protected List<ExchangeContact> doInBackground(
							String... params) {
						String name = params[0];
						try {
							return exchangeContactService.findContact(name);
						} catch (Exception e) {
							Log.e("RetrieveContactAsyncTask",
									"Fail to find contact '" + name + "'", e);
							return null;
						}
					}

					@Override
					protected void onPostExecute(List<ExchangeContact> contacts) {
						if (contacts == null) {
							UIUtils.showAlert(getContext(), "Error",
									"Exchang service is not available.\r\nPlease try again later.");
						} else if (contacts.isEmpty()) {
							UIUtils.showAlert(getContext(), "No Records",
									"There is no such contact.");
						} else {
							findContactAutoCompleteAdapter.clear();
							findContactAutoCompleteAdapter.addAll(contacts);
							findContactAutoCompleteAdapter
									.notifyDataSetChanged();
							maximize();
						}
					}
				}.execute(name);
				break;
			}
			//
			return true;
		}
	};
	private View.OnTouchListener findContactOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (event.getX() <= searchIcon.getBounds().width()) {
					Log.d(MainActivity.TAG,
							"Search icon is touched. Do nothing.");
					return true;
				} else if (event.getX() >= textView.getWidth()
						- removeIcon.getBounds().width()) {
					textView.setText("");
					return true;
				} else {
					if (!UIUtils.isNetworkAvailable(getContext())) {
						UIUtils.showAlert(
								getContext(),
								"Network Error",
								"Network is not available. Turn on your WiFi or Data and try again.",
								"Try Again");
					} else {
						AutoCompleteTextView textView = (AutoCompleteTextView) v;
						if (textView.hasFocus()) {
							UIUtils.showSoftInput(textView);
							maximize();
						}
					}
					return true;
				}
			}
			return false;
		}
	};
	private TextWatcher findContactTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// do nothing
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (start - before > 1) {
				new AsyncTask<String, Void, List<ExchangeContact>>() {
					/**
					 * Return NULL if there is error.
					 */
					@Override
					protected List<ExchangeContact> doInBackground(
							String... params) {
						String name = params[0];
						try {
							return exchangeContactService.findContact(name);
						} catch (Exception e) {
							Log.e("RetrieveContactAsyncTask",
									"Fail to find contact '" + name + "'", e);
							return null;
						}
					}

					@Override
					protected void onPostExecute(List<ExchangeContact> contacts) {
						if (contacts != null) {
							findContactAutoCompleteAdapter.clear();
							findContactAutoCompleteAdapter.addAll(contacts);
							findContactAutoCompleteAdapter
									.notifyDataSetChanged();
							maximize();
						}
					}
				}.execute(s.toString());
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// display remove text icon only if there is text
			if (s != null && s.length() > 1) {
				textView.setCompoundDrawables(searchIcon, null, removeIcon,
						null);
			} else {
				textView.setCompoundDrawables(searchIcon, null, null, null);
			}
		}
	};
}
