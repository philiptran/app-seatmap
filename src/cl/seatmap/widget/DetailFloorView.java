package cl.seatmap.widget;

import android.content.Context;
import android.util.AttributeSet;

import cl.seatmap.R;

/**
 * 
 * @author philiptrannp
 *
 */
public class DetailFloorView extends BaseFloorView {
	public DetailFloorView(Context context) {
		this(context, null);
	}

	public DetailFloorView(Context context, AttributeSet attrs) {
		super(context);
		//
		width = 6400;
		height = 3840;
		//
		setId(R.id.detail_floor_view);
		//
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		//
		setSize(width, height);
		setScale(1f);
		setMarkerAnchorPoints(-0.5f, -1.0f);
		//
		addDetailLevel(1f, "tiles/l/l1_1000_%col%_%row%.png");
		addDetailLevel(0.75f, "tiles/l/l1_750_%col%_%row%.png");
		addDetailLevel(0.5f, "tiles/l/l1_500_%col%_%row%.png");
		addDetailLevel(0.25f, "tiles/l/l1_250_%col%_%row%.png");
	}
}
