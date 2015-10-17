package cl.seatmap;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import cl.seatmap.dao.ContactLocationDAO;
import cl.seatmap.domain.ContactLocation;
import cl.seatmap.domain.ExchangeContact;
import cl.seatmap.widget.DetailFloorView;
import cl.seatmap.widget.FindContactAutoCompleteTextView;
import cl.seatmap.widget.FindContactAutoCompleteTextView.OnSpeakToMeListener;

import com.qozix.tileview.TileView;

/**
 * 
 * @author philiptrannp
 * 
 */
public class MainActivity extends Activity {
	public static final String TAG = "CL-SEAT-MAP";
	protected static final int TRANSITION_DURATION = 500;
	private static final int NEARBY_DISTANCE = 400;
	private static final int REQUEST_VOICE_RECOGNIZER = 1234;
	//
	private AppConfig appConfig;
	private FindContactAutoCompleteTextView findContactAutocomplete;
	private DetailFloorView detailFloorView;
	private ContactLocationDAO contactLocationDAO;
	private boolean detailMarkerSwitchOn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		//
		appConfig = new AppConfig(this);
		// Force reload database from app's assets when we upgrade the app
		if (appConfig.getDBVer() < ContactLocationDAO.DATABASE_VERSION) {
			ContactLocationDAO.forceDatabaseReload(this);
			appConfig.setDBVer(ContactLocationDAO.DATABASE_VERSION);
		}
		contactLocationDAO = new ContactLocationDAO(this);
		//
		findContactAutocomplete = (FindContactAutoCompleteTextView) findViewById(R.id.findcontact_autocomplete);
		findContactAutocomplete
				.setOnItemClickListener(findContactOnItemClickListener);
		findContactAutocomplete
				.setLocationTextOnClickListener(currentLocationTextOnClickListener);
		findContactAutocomplete
				.setOnSpeakToMeListener(new OnSpeakToMeListener() {
					@Override
					public void startVoiceRecognitionActivity() {
						Intent intent = new Intent(
								RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
						intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
								RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
						intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
								"uFind: Please speak slowly and enunciate clearly.");
						startActivityForResult(intent, REQUEST_VOICE_RECOGNIZER);
					}
				});
		//
		detailFloorView = new DetailFloorView(this, contactLocationDAO);
		detailFloorView.addTileViewEventListener(detailFloorViewEventListener);
		detailFloorView.setMarkerOnClickListener(detailMarkerOnClickListener);
		detailFloorView.setVisibility(View.INVISIBLE);
		//
		RelativeLayout main = (RelativeLayout) findViewById(R.id.activity_main);
		main.addView(detailFloorView);
		main.bringChildToFront(findContactAutocomplete);
		main.refreshDrawableState();

		//
		findContactAutocomplete.maximize();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_VOICE_RECOGNIZER && resultCode == RESULT_OK) {
			List<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			String firstMatch = matches.get(0);
			findContactAutocomplete.setText(firstMatch);
		}
	}

	private AdapterView.OnItemClickListener findContactOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ExchangeContact contact = (ExchangeContact) parent.getAdapter()
					.getItem(position);
			// default to officeLocation
			String location = contact.getOfficeLocation();
			if (contact.getTitle().toLowerCase().contains("meeting room")) {
				// extract the meeting room number from the name
				Pattern pattern = Pattern.compile("^(\\d-.*):");
				Matcher m = pattern.matcher(contact.getName());
				if (m.find()) {
					location = m.group(1);
				} else {
					// Room that not follow convention -> use the name to look
					// up
					location = contact.getName();
				}
			}
			// retrieve contact location from embedded DB
			ContactLocation cl = contactLocationDAO.get(location);
			if (cl == null || (cl.getX() == 0 && cl.getY() == 0)
					|| cl.getX() >= DetailFloorView.WIDTH
					|| cl.getY() >= DetailFloorView.HEIGHT) {
				String errorMessage = "Could not find location '" + location
						+ "'";
				if (cl != null) {
					errorMessage = "Invalid mapping (" + cl.getX() + ", "
							+ cl.getY() + ") for location '" + location + "'";
				}
				//
				Log.e(TAG, errorMessage);
				final View fView = view;
				UIUtils.showAlert(parent.getContext(), "Not Found",
						errorMessage, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface paramDialogInterface,
									int paramInt) {
								UIUtils.showSoftInput(fView);
								findContactAutocomplete.maximize();
							}
						});
				return;
			}
			//
			contact.setContactLocation(cl);
			// find neighbors in the background
			new FindNearbyAsyncTask().execute(contact);
			//
			findContactAutocomplete.setCurrentContact(contact);
			findContactAutocomplete.showCurrentContactView();
			findContactAutocomplete.minimize();
			//
			detailMarkerSwitchOn = true;
			detailFloorView.setScale(1f);
			detailFloorView.setCurrentContact(contact);
			detailFloorView.setVisibility(View.VISIBLE);
		}
	};

	private class FindNearbyAsyncTask extends
			AsyncTask<ExchangeContact, Void, Void> {
		@Override
		protected Void doInBackground(ExchangeContact... params) {
			ExchangeContact contact = params[0];
			ContactLocation cl = contact.getContactLocation();
			// update cached name if required
			if (cl != null && !contact.getName().equalsIgnoreCase(cl.getName())) {
				cl.setName(contact.getName());
				contactLocationDAO.update(cl);
			}
			// find nearby contacts from embedded DB
			List<ContactLocation> orderedNearby = contactLocationDAO
					.findNearby(contact.getContactLocation(), NEARBY_DISTANCE);

			// only show the first 4 neighbors
			orderedNearby = orderedNearby.subList(0,
					orderedNearby.size() < 4 ? orderedNearby.size() : 4);
			contact.setNearby(orderedNearby);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			detailFloorView.updateNeighbors();
		}

		private List<ContactLocation> avoidOverlapping(
				List<ContactLocation> orderedNearby) {
			if (orderedNearby.size() < 2) {
				return orderedNearby;
			}
			//
			int MIN_X = 200; // px
			int MIN_Y = 40; // px
			ContactLocation lastRef = orderedNearby.get(0);
			for (int i = 1; i < orderedNearby.size(); i++) {
				ContactLocation p = orderedNearby.get(i);
				System.out.println("Process p " + p.toString() + ", lastRef "
						+ lastRef.toString());
				int dX = p.getX() - lastRef.getX();
				int mX = Math.abs(dX);
				int dY = p.getY() - lastRef.getY();
				int mY = Math.abs(dY);
				if ((mY < MIN_Y && mX < MIN_X) || dY <= 0) {
					p.setY(p.getY() + MIN_Y - dY);
					System.out.println("\t-> Shifted location " + p.toString());
				} else {
					System.out.println("\t->No shift!");
				}
				//
				lastRef = p;
			}
			//
			return orderedNearby;
		}
	}

	private TileView.TileViewEventListener detailFloorViewEventListener = new TileView.TileViewEventListenerImplementation() {
		@Override
		public void onScaleChanged(double scale) {
			Log.d(MainActivity.TAG, "Detailed Floor - scale " + scale);
		}
	};
	private View.OnClickListener detailMarkerOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			detailMarkerSwitchOn = !detailMarkerSwitchOn;
			if (detailMarkerSwitchOn) {
				detailFloorView.showNeighbors();
				findContactAutocomplete.showCurrentContactView();
			} else {
				detailFloorView.hideNeighbors();
				findContactAutocomplete.hideCurrentContactView();
			}
		}
	};

	private OnClickListener currentLocationTextOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View paramView) {
			detailFloorView.moveToContact();
		}
	};
}
