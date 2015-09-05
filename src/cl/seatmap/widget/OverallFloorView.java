package cl.seatmap.widget;

import android.content.Context;
import android.util.AttributeSet;
import cl.seatmap.R;
import cl.seatmap.UIUtils;
import cl.seatmap.domain.ContactLocation;
import cl.seatmap.domain.ExchangeContact;

/**
 * 
 * @author philiptrannp
 *
 */
public class OverallFloorView extends BaseFloorView {
	public OverallFloorView(Context context) {
		this(context, null);
	}

	public OverallFloorView(Context context, AttributeSet attrs) {
		super(context);
		//
		width = 1535;
		height = 1260;
		//
		setId(R.id.floor_view);
		//
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		//
		setSize(width, height);
		setScaleToFit(true);
		setMarkerAnchorPoints(-0.5f, -1.0f);
		//
		addDetailLevel(1f, "tiles/h/h_1000_%col%_%row%.png");
		addDetailLevel(0.75f, "tiles/h/h_750_%col%_%row%.png");
		addDetailLevel(0.5f, "tiles/h/h_500_%col%_%row%.png");
	}

	@Override
	public void moveToContact() {
		ExchangeContact contact = getContact();
		if (contact != null && contact.getContactLocation() != null) {
			ContactLocation cl = contact.getContactLocation();
			moveToAndCenter(cl.getHx(), cl.getHy());
		}
	}

	@Override
	public void setCurrentContact(ExchangeContact contact) {
		ContactLocation cl = contact.getContactLocation();
		if (cl == null) {
			UIUtils.showAlert(getContext(), "Error", "Application Error");
			return;
		}
		contactMarker.setTag(contact);
		moveMarker(contactMarker, cl.getHx(), cl.getHy());
		slideToAndCenter(cl.getHx(), cl.getHy());
	}
}
