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
	protected int width;
	protected int height;
	protected ImageView contactMarker;
	protected List<View> callouts;
	
	protected BaseFloorView(Context context) {
		super(context);
		//
		contactMarker = new ImageView(context);
		contactMarker.setImageResource(R.drawable.map_pin_red2);
		//
		addMarker(contactMarker, 0, 0);

		// addTileViewEventListener(new FloorViewEventListener(this));

		// setTransitionsEnabled(true);
		
		callouts = new ArrayList<View>();
		
		setCacheEnabled(true);
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
			UIUtils.showAlert(getContext(), "Error", "Application Error");
			return;
		}
		contactMarker.setTag(contact);
		moveMarker(contactMarker, cl.getX(), cl.getY());
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

	public void calloutNearby() {
		callouts.clear();
		List<ContactLocation> nearby = getContact().getNearby();
		for (ContactLocation cl : nearby) {
			TextOverlayView<OverlayItem> callout = new TextOverlayView<OverlayItem>(
					getContext(), 0);
			callout.setData(new OverlayItem(cl.getName(), null));
			callouts.add(callout);
			//
			//addCallout(callout, cl.getX(), cl.getY(), -0.5f, -1f);
			addCallout(callout, cl.getX(), cl.getY(), -0.5f, -1f);
		}
	}
	public void removeCallouts() {
		for(View callout: callouts) {
			removeCallout(callout);
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
			// UIUtils.showToast(getContext(), "Tap at " + x + ", " + y
			// + "; Translated: " + p.x + ", " + p.y);
			//
			tileView.slideToAndCenter(p.x, p.y);
			//
			// tileView.addCallout(callout, p.x, p.y, -0.5f, -1f);
			// a little sugar
		}
	}
}
