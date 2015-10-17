package cl.seatmap.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cl.seatmap.R;
import cl.seatmap.UIUtils;
import cl.seatmap.domain.ExchangeContact;

/**
 * 
 * @author philiptrannp
 * 
 */
public class FindContactAutoCompleteAdapter extends
		ArrayAdapter<ExchangeContact> {
	private List<ExchangeContact> contacts;
	private Bitmap defaultPhoto;
	private String searchText = "";

	/**
	 * @param context
	 * @param objects
	 */
	public FindContactAutoCompleteAdapter(Context context,
			ExchangeContact[] objects) {
		super(context, R.layout.findcontact_item_layout);
		this.contacts = objects != null ? Arrays.asList(objects)
				: new ArrayList<ExchangeContact>();
		// defaultPhoto = BitmapFactory.decodeResource(context.getResources(),
		// R.drawable.ic_user);
	}

	/**
	 * 
	 * @param text
	 */
	public void setSearchText(String text) {
		this.searchText = text != null ? text : "";
	}

	@Override
	public void clear() {
		super.clear();
		this.contacts.clear();
	}

	@Override
	public void addAll(Collection<? extends ExchangeContact> collection) {
		this.contacts.clear();
		this.contacts.addAll(collection);
	}

	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public ExchangeContact getItem(int position) {
		return contacts.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		LayoutInflater inflater = LayoutInflater.from(getContext());
		if (row == null) {
			row = inflater.inflate(R.layout.findcontact_item_layout, parent,
					false);
		}
		//
		ExchangeContact contact = getItem(position);

		TextView nameText = (TextView) row.findViewById(R.id.name);
		nameText.setText(UIUtils.highlight(contact.getName(), searchText));

		TextView titleText = (TextView) row.findViewById(R.id.title);
		titleText.setText(UIUtils.highlight(contact.getTitle(), searchText));

		TextView phoneText = (TextView) row.findViewById(R.id.phone);
		phoneText.setText(UIUtils.highlight(contact.getPhone(), searchText));

		// optional mobile number
		String mobile = contact.getMobile();
		if (mobile == null || mobile.isEmpty()) {
			ViewGroup vg = (ViewGroup) row.findViewById(R.id.layout_mobile);
			vg.setVisibility(View.GONE);
		} else {
			TextView mobileText = (TextView) row.findViewById(R.id.mobile);
			mobileText.setText(UIUtils.highlight(mobile, searchText));
		}
		TextView locationText = (TextView) row.findViewById(R.id.location);
		locationText.setText(UIUtils.highlight(contact.getOfficeLocation(),
				searchText));

		ImageView flagView = (ImageView) row.findViewById(R.id.flag_icon);
		flagView.setImageResource(UIUtils.getFlagResource(contact.getCountry()));
		//
		// ImageView photo = (ImageView) row.findViewById(R.id.photo);
		// photo.setImageBitmap(defaultPhoto);
		//
		row.setTag(contact);
		return row;
	}
}
