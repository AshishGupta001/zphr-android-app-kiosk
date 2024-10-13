package com.adapter.files.kiosk;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.zphr.kiosk.R;
import com.general.files.GeneralFunctions;
import com.model.Hotel;
import com.view.MTextView;

import java.util.ArrayList;

/**
 * Created by Admin on 17-10-17.
 */
public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.MyViewHolder> {

    ArrayList<Hotel> list;
    Context mContext;
    String required_str = "";
    public GeneralFunctions generalFunc;
    OnItemClickList onItemClickList;
    public String selectedDestAddress="-1";

    public HotelAdapter(Context mContext, ArrayList<Hotel> list) {
        this.mContext = mContext;
        this.list = list;
        generalFunc = new GeneralFunctions(mContext);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        MTextView txtAddress;
        LinearLayout relDestinationList;

        public MyViewHolder(View view) {
            super(view);
            txtAddress = (MTextView) view.findViewById(R.id.txtAddress);
            relDestinationList = (LinearLayout) view.findViewById(R.id.relDestinationList);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kiosk_design_destinationlist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Hotel item = list.get(position);
        String names = item.gettDestName();

        String[] namesList = names.split(",");

        String name1 = namesList[0];

        holder.txtAddress.setText(name1);


        if (item.getiVisitId().equalsIgnoreCase(selectedDestAddress))
        {
            holder.relDestinationList.setBackground(mContext.getResources().getDrawable(R.drawable.layout_square_bg_filled));
            holder.txtAddress.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
        }
        else
        {
            holder.relDestinationList.setBackground(mContext.getResources().getDrawable(R.drawable.layout_square_bg));
            holder.txtAddress.setTextColor(mContext.getResources().getColor(R.color.white));
        }

        holder.relDestinationList.setOnClickListener(v -> onItemClickList.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public interface OnItemClickList {
        void onItemClick(int position);
    }
}


