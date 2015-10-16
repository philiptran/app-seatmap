package cl.seatmap.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import cl.seatmap.R;
import cl.seatmap.dao.ContactLocationDAO;
import cl.seatmap.domain.ContactLocation;
import cl.seatmap.domain.ExchangeContact;

import com.qozix.tileview.TileView;

/**
 * 
 * @author philiptrannp
 * 
 */
public class DetailFloorView extends BaseFloorView {
	public static final int WIDTH = 6511;
	public static final int HEIGHT = 5198;
	private ContactLocationDAO contactLocationDAO = null;
	private TextOverlayView<OverlayItem> tapView;

	public DetailFloorView(Context context,
			ContactLocationDAO contactLocationDAO) {
		this(context, contactLocationDAO, null);
		//
		this.tapView = new TextOverlayView<OverlayItem>(context);
		this.tapView.setVisibility(View.INVISIBLE);
		this.tapView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.INVISIBLE);
			}
		});
		addMarker(this.tapView, 0, 0);
	}

	public DetailFloorView(Context context,
			ContactLocationDAO contactLocationDAO, AttributeSet attrs) {
		super(context);
		//
		this.contactLocationDAO = contactLocationDAO;
		//
		setId(R.id.detail_floor_view);
		//
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		//
		setSize(WIDTH, HEIGHT);
		setScale(1f);
		setMarkerAnchorPoints(-0.5f, -1.0f);
		//
		// addDetailLevel(1f, "tiles/l/l3_1000_%col%_%row%.png",
		// "samples/l3-sample.png");
		// addDetailLevel(0.75f, "tiles/l/l3_750_%col%_%row%.png",
		// "samples/l3-sample.png");
		// addDetailLevel(0.5f, "tiles/l/l3_500_%col%_%row%.png",
		// "samples/l3-sample.png");
		// addDetailLevel(0.25f, "tiles/l/l3_250_%col%_%row%.png",
		// "samples/l3-sample.png");
		addDetailLevel(1f, "tiles/l/l3_1000_%col%_%row%.png",
				"samples/l3-sample.png");
		addDetailLevel(0.75f, "tiles/l/l3_750_%col%_%row%.png");
		addDetailLevel(0.5f, "tiles/l/l3_500_%col%_%row%.png");
		addDetailLevel(0.25f, "tiles/l/l3_250_%col%_%row%.png");
		//
		addTileViewEventListener(new DetailFloorViewEventListener());
	}

	@Override
	public void setCurrentContact(ExchangeContact contact) {
		super.setCurrentContact(contact);
		//
		this.tapView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void showNeighbors() {
		super.showNeighbors();
		this.tapView.setVisibility(View.INVISIBLE);
	}

	private class DetailFloorViewEventListener extends
			TileView.TileViewEventListenerImplementation {
		// private final WeakReference<TileView> tileView;
		public DetailFloorViewEventListener() {
			// this.tileView = new WeakReference<TileView>(tileView);
		}

		@Override
		public void onTap(int x, int y) {
			double scale = getScale();
			// lets center the screen to that coordinate
			Point p = getPositionManager().translate(x / scale, y / scale);
			//
			ContactLocation cl = contactLocationDAO.findContactAt(p.x, p.y);
			if (cl != null) {
				hideNeighbors();
				String name = cl.getName();
				name = name != null && !name.isEmpty() ? name : "Not Occupied";
				tapView.setData(new OverlayItem(name));
				moveMarker(tapView, cl.getX(), cl.getY());
				tapView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onScaleChanged(double scale) {
			if (scale != 1.0) {
				hideNeighbors();
			} else {
				showNeighbors();
			}
		}
	}
}
