package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.zphr.kiosk.R;
import com.view.pinnedListView.CountryListItem;

import java.util.ArrayList;

public class PinnedSectionListAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {

    Context mContext;
    ArrayList<CountryListItem> countryListItems;

    CountryClick countryClickList;
    boolean isStateList = false;
    private CountryListItem[] sections;
    private LayoutInflater inflater;


    public PinnedSectionListAdapter(Context mContext, ArrayList<CountryListItem> countryListItems, CountryListItem[] sections) {
        // TODO Auto-generated constructor stub
        this.mContext = mContext;
        this.countryListItems = countryListItems;
        this.sections = sections;
    }

    public void isStateList(boolean value) {
        this.isStateList = value;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.country_list_item, null);

        TextView txt_view = (TextView) convertView.findViewById(R.id.txt);
        TextView txt_count = (TextView) convertView.findViewById(R.id.txt_count);

        txt_view.setTextColor(Color.BLACK);
        txt_view.setTag("" + position);
        final CountryListItem countryListItem = countryListItems.get(position);

        txt_view.setText(countryListItem.text);
        if (countryListItem.type == CountryListItem.SECTION) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            txt_view.setClickable(false);
            txt_view.setEnabled(false);

            txt_view.setTextColor(Color.parseColor("#FFFFFF"));
            txt_count.setTextColor(Color.parseColor("#FFFFFF"));
            txt_count.setText("" + countryListItem.CountSubItems);
            txt_count.setVisibility(View.VISIBLE);
        } else {
            txt_view.setClickable(true);
            txt_view.setEnabled(true);

            if (isStateList == false) {
                txt_count.setVisibility(View.VISIBLE);
                txt_count.setText(countryListItem.getvPhoneCode());
            } else {
                txt_count.setVisibility(View.GONE);
            }

        }

        txt_view.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            if (countryClickList != null) {
                countryClickList.countryClickList(countryListItem);
            }
        });

        return convertView;
    }

    public void setCountryClickListener(CountryClick countryClickList) {
        this.countryClickList = countryClickList;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public CountryListItem[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        if (section >= sections.length) {
            section = sections.length - 1;
        }
        return sections[section].listPosition;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position >= getCount()) {
            position = getCount() - 1;
        }
        return countryListItems.get(position).sectionPosition;
    }

    @Override
    public int getItemViewType(int position) {
        return countryListItems.get(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == CountryListItem.SECTION;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return countryListItems.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return countryListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }





    public interface CountryClick {
        void countryClickList(CountryListItem countryListItem);
    }

}
