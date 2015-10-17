package cl.seatmap.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;
import cl.seatmap.R;
import cl.seatmap.UIUtils;
import cl.seatmap.domain.ContactLocation;
import cl.seatmap.domain.ExchangeContact;

import com.qozix.tileview.TileView;

/**
 * 
 * @author philiptrannp
 * 
 */
public abstract class BaseFloorView extends TileView {
	protected ImageView contactMarker;
	protected List<View> neighborViews; // should we use ViewGroup?

	protected BaseFloorView(Context context) {
		super(context);
		setCacheEnabled(true);
		//
		contactMarker = new ImageView(context);
		contactMarker.setImageResource(R.drawable.map_pin_red2);
		//
		// addTileViewEventListener(new FloorViewEventListener(this));

		neighborViews = new ArrayList<View>();
	}

	public ExchangeContact getContact() {
		return (ExchangeContact) contactMarker.getTag();
	}

	public void moveToContact() {
		ExchangeContact contact = getContact();
		if (contact != null && contact.getContactLocation() != null) {
			ContactLocation cl = contact.getContactLocation();
			moveToAndCenter(cl.getX(), cl.getY());
		}
	}

	public void setCurrentContact(ExchangeContact contact) {
		ContactLocation cl = contact.getContactLocation();
		if (cl == null) {
			// should not be here
			UIUtils.showAlert(getContext(), "Error", "Application Error");
			return;
		}
		contactMarker.setTag(contact);
		moveMarker(contactMarker, cl.getX(), cl.getY());
		// defer updateNeighbors to the asyncTask in MainActivity
		//
		slideToAndCenter(cl.getX(), cl.getY());
	}
	
	public void setMarkerOnClickListener(OnClickListener listener) {
		contactMarker.setOnClickListener(listener);
	}

	public void calloutContact(ExchangeContact contact) {
		ContactLocation cl = contact.getContactLocation();
		BalloonOverlayView<OverlayItem> callout = new BalloonOverlayView<OverlayItem>(
				getContext(), 0);
		callout.setData(new OverlayItem(contact.getName(), contact
				.getOfficeLocation()));
		//
		addCallout(callout, cl.getX(), cl.getY(), -0.5f, -1f);
		// contactMarker.setVisibility(View.INVISIBLE);
	}

	protected void removeNeighbors() {
		for (View v : neighborViews) {
			removeMarker(v);
		}
		neighborViews.clear();
	}

	public void updateNeighbors() {
		removeNeighbors();
		//
		ExchangeContact contact = getContact();
		List<ContactLocation> neighbors = contact.getNearby();
		for (ContactLocation cl : neighbors) {
			String name = cl.getName();
			if (name == null || name.isEmpty()) {
				// should not be here
				continue;
			}
			//
			View view = createNeighborView(cl);
			addMarker(view, cl.getX(), cl.getY());
			neighborViews.add(view);
		}
		// need to remove and add contactMarker again as bringChildToFront is
		// not supported
		// bringChildToFront(contactMarker);
		removeMarker(contactMarker);
		ContactLocation cl = contact.getContactLocation();
		addMarker(contactMarker, cl.getX(), cl.getY());
	}

	private View createNeighborView(ContactLocation cl) {
		// BalloonOverlayView<OverlayItem> view = new
		// BalloonOverlayView<OverlayItem>(
		// getContext());
		// view.setData(new OverlayItem(shortenName(cl.getName()), null));

		// Use simple text overlay
		TextOverlayView<OverlayItem> view = new TextOverlayView<OverlayItem>(
				getContext(), 0);
		view.setData(new OverlayItem(cl.getName()));
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.INVISIBLE);
			}
		});
		return view;
	}

	public void hideNeighbors() {
		for (View view : neighborViews) {
			view.setVisibility(View.INVISIBLE);
		}
	}

	public void showNeighbors() {
		for (View view : neighborViews) {
			view.setVisibility(View.VISIBLE);
		}
	}

	/**
     *
     */
	protected class FloorViewEventListener extends
			TileView.TileViewEventListenerImplementation {
		// create a simple callout
		BalloonOverlayView<OverlayItem> callout;
		TileView tileView;

		public FloorViewEventListener(TileView tileView) {
			this.tileView = tileView;
			this.callout = new BalloonOverlayView<OverlayItem>(
					tileView.getContext(), 0);
		}

		@Override
		public void onTap(int x, int y) {
			double scale = tileView.getScale();
			// lets center the screen to that coordinate
			Point p = tileView.getPositionManager().translate(x / scale,
					y / scale);
			//
			UIUtils.showToast(getContext(), "Tap at " + x + ", " + y
					+ "; Translated: " + p.x + ", " + p.y);
			//
			tileView.slideToAndCenter(p.x, p.y);
			//
			// tileView.addCallout(callout, p.x, p.y, -0.5f, -1f);
			// a little sugar
		}
	}
}
