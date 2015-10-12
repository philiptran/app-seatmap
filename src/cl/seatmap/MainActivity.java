package cl.seatmap;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.AsyncTask;
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
	protected static final int TRANSITION_DURATION = 500;
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
			//
			findContactAutocomplete.setCurrentContact(contact);
			findContactAutocomplete.showCurrentContactView();
			findContactAutocomplete.minimize();
			//
			detailFloorView.setScale(1f);
			detailFloorView.setCurrentContact(contact);
			detailFloorView.setVisibility(View.INVISIBLE);

			//
			overallFloorView.setCurrentContact(contact);
			overallFloorView.setVisibility(View.VISIBLE);
			// create a new instance as a task is only executed once.
			new FindNearbyAsyncTask().execute(contact);
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
			contact.setNearby(avoidOverlapping(orderedNearby));
			return null;
		}

		private List<ContactLocation> avoidOverlapping(
				List<ContactLocation> orderedNearby) {
			if (orderedNearby.size() < 2) {
				return orderedNearby;
			}
			//
			int MIN_X = 200; // px
			int MIN_Y = 25; // px
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
			// findContactAutocomplete.hideCurrentContactView();
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
		overallFloorView.animate().alpha(0f).setDuration(TRANSITION_DURATION)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						overallFloorView.setVisibility(View.INVISIBLE);
						overallFloorView.setAlpha(1f);
					}
				});
		detailFloorView.removeCallouts();
		detailFloorView.setScale(1f);
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
