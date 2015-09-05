package cl.seatmap.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cl.seatmap.R;
import cl.seatmap.domain.ExchangeContact;

/**
 * 
 * @author philiptrannp
 *
 */
public class FindContactAutoCompleteAdapter extends ArrayAdapter<ExchangeContact> {
    private List<ExchangeContact> contacts;
    private Bitmap defaultPhoto;
    /**
     * @param context
     * @param objects
     */
    public FindContactAutoCompleteAdapter(Context context, ExchangeContact[] objects) {
        super(context, R.layout.findcontact_item_layout);
        contacts = objects != null ? Arrays.asList(objects) : new ArrayList<ExchangeContact>();
        //defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_user);
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
            row = inflater.inflate(R.layout.findcontact_item_layout, parent, false);
        }
        ExchangeContact contact = getItem(position);
        TextView nameText = (TextView) row.findViewById(R.id.name);
        nameText.setText(contact.getName());
        TextView phoneText = (TextView) row.findViewById(R.id.phone);
        phoneText.setText(contact.getPhone());
        TextView locationText = (TextView) row.findViewById(R.id.location);
        locationText.setText(contact.getOfficeLocation());

        //
//        ImageView photo = (ImageView) row.findViewById(R.id.photo);
//        photo.setImageBitmap(defaultPhoto);
        //
        row.setTag(contact);
        return row;
    }
}
