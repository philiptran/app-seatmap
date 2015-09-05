package cl.seatmap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
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
import cl.seatmap.widget.OverallFloorView;

import com.qozix.tileview.TileView;

/**
 * 
 * @author philiptrannp
 *
 */
public class MainActivity extends Activity {
	public static final String TAG = "CL-SEAT-MAP";
	protected static final int TRANSITION_DURATION = 1000;
	private static final int NEARBY_DISTANCE = 1000; // px
	//
	private FindContactAutoCompleteTextView findContactAutocomplete;
	private OverallFloorView overallFloorView;
	private DetailFloorView detailFloorView;
	private ContactLocationDAO contactLocationDAO;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		//
		findContactAutocomplete = (FindContactAutoCompleteTextView) findViewById(R.id.findcontact_autocomplete);
		findContactAutocomplete
				.setOnItemClickListener(findContactOnItemClickListener);
		findContactAutocomplete
				.setLocationTextOnClickListener(currentLocationTextOnClickListener);
		findContactAutocomplete
				.setNearbyTextOnClickListener(nearbyTextOnClickListener);
		//
		overallFloorView = new OverallFloorView(this);
		overallFloorView
				.addTileViewEventListener(overallFloorViewEventListener);
		overallFloorView.setMarkerOnClickListener(markerOnClickListener);
		//
		detailFloorView = new DetailFloorView(this);
		detailFloorView.addTileViewEventListener(detailFloorViewEventListener);
		detailFloorView.setMarkerOnClickListener(detailMarkerOnClickListener);
		detailFloorView.setVisibility(View.INVISIBLE);
		//
		RelativeLayout main = (RelativeLayout) findViewById(R.id.activity_main);
		main.addView(overallFloorView);
		main.addView(detailFloorView);
		main.bringChildToFront(findContactAutocomplete);
		main.refreshDrawableState();
		//
		contactLocationDAO = new ContactLocationDAO(this);
	}

	private AdapterView.OnItemClickListener findContactOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ExchangeContact contact = (ExchangeContact) parent.getAdapter()
					.getItem(position);

			// retrieve contact location from embedded DB
			ContactLocation cl = contactLocationDAO.get(contact
					.getOfficeLocation());

			if (cl == null) {
				String errorMessage = "No location information for "
						+ contact.getOfficeLocation();
				//
				Log.e(TAG, errorMessage);
				UIUtils.showAlert(parent.getContext(), "Missing Location Info",
						errorMessage);
				return;
			}
			//
			contact.setContactLocation(cl);
			// find nearby contacts from embedded DB
			contact.setNearby(contactLocationDAO.findNearby(cl, NEARBY_DISTANCE));
			//
			findContactAutocomplete.setCurrentContact(contact);
			findContactAutocomplete.showCurrentContactView();
			findContactAutocomplete.minimize();
			//
			detailFloorView.setCurrentContact(contact);
			detailFloorView.setVisibility(View.INVISIBLE);
			
			//
			overallFloorView.setCurrentContact(contact);
			overallFloorView.setVisibility(View.VISIBLE);
		}
	};
	private View.OnClickListener markerOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			findContactAutocomplete.toggleCurrentContactView();
		}
	};

	private View.OnClickListener detailMarkerOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			findContactAutocomplete.toggleCurrentContactView();
		}
	};
	private TileView.TileViewEventListener detailFloorViewEventListener = new TileView.TileViewEventListenerImplementation() {
		@Override
		public void onScaleChanged(double scale) {
			Log.d(MainActivity.TAG, "Detailed Floor - scale " + scale);
			if (scale < 0.35f) {
				transitionToOverviewMap();
			}
		}
	};
	private TileView.TileViewEventListener overallFloorViewEventListener = new TileView.TileViewEventListenerImplementation() {
		@Override
		public void onTap(int x, int y) {
			findContactAutocomplete.hideCurrentContactView();
		}

		@Override
		public void onDoubleTap(int x, int y) {
			transitionToDetailedMap();
		}

		@Override
		public void onZoomStart(double scale) {
			transitionToDetailedMap();
		}
	};
	private OnClickListener currentLocationTextOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View paramView) {
			if (overallFloorView.getVisibility() == View.INVISIBLE) {
				transitionToOverviewMap();
			}
		}
	};
	private View.OnClickListener nearbyTextOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (detailFloorView.getVisibility() == View.INVISIBLE) {
				transitionToDetailedMap();
			}
			//
			detailFloorView.calloutNearby();
		}
	};

	private void transitionToOverviewMap() {
		detailFloorView.animate().alpha(0f).setDuration(TRANSITION_DURATION)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						detailFloorView.setVisibility(View.INVISIBLE);
						detailFloorView.setAlpha(1f);
					}
				});
		overallFloorView.setAlpha(0f);
		overallFloorView.setScale(1f);
		overallFloorView.setVisibility(View.VISIBLE);
		overallFloorView.animate().alpha(1f).setDuration(TRANSITION_DURATION)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						overallFloorView.moveToContact();
					}
				});
	}

	private void transitionToDetailedMap() {
		findContactAutocomplete.hideCurrentContactView();
		overallFloorView.animate().alpha(0f).setDuration(TRANSITION_DURATION)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						overallFloorView.setVisibility(View.INVISIBLE);
						overallFloorView.setAlpha(1f);
					}
				});
		detailFloorView.setAlpha(0f);
		detailFloorView.setVisibility(View.VISIBLE);
		detailFloorView.animate().alpha(1f).setDuration(TRANSITION_DURATION)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						detailFloorView.moveToContact();
					}
				});
	}
}
