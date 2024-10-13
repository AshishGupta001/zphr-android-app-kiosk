package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.general.files.GeneralFunctions;
import com.zphr.kiosk.R;
import com.utils.CommonUtilities;
import com.utils.LoadImage;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.anim.loader.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 04-07-2016.
 */
public class CabTypeAdapter extends RecyclerView.Adapter<CabTypeAdapter.ViewHolder> {

    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list_item;
    Context mContext;
    String vehicleIconPath = CommonUtilities.SERVER_URL + "webimages/icons/VehicleType/";
    String vehicleDefaultIconPath = CommonUtilities.SERVER_URL + "webimages/icons/DefaultImg/";

    OnItemClickList onItemClickList;
    ViewHolder viewHolder;

    String selectedVehicleTypeId = "";
    boolean isMultiDelivery = false;
    private boolean isDestinationAdded = false;


    public CabTypeAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
    }


    @Override
    public CabTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(Utils.IS_KIOSK_APP ? R.layout.kiosk_item_design_cab_type : R.layout.item_design_cab_type, parent, false);

        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public void setSelectedVehicleTypeId(String selectedVehicleTypeId) {
        this.selectedVehicleTypeId = selectedVehicleTypeId;
    }

    public void isDestinationAdded(boolean isDestinationAdded) {
        this.isDestinationAdded = isDestinationAdded;
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        setData(viewHolder, position, false);
    }

    public void setData(CabTypeAdapter.ViewHolder viewHolder, final int position, boolean isHoverOLD) {
        HashMap<String, String> item = list_item.get(position);

        String vVehicleType = item.get("vVehicleType");
        String iVehicleTypeId = item.get("iVehicleTypeId");


        if (!item.get("eRental").equals("") && item.get("eRental").equals("Yes")) {

            viewHolder.carTypeTitle.setText(item.get("vRentalVehicleTypeName"));

        } else {
            viewHolder.carTypeTitle.setText(vVehicleType);
        }

        boolean isHover = selectedVehicleTypeId.equals(iVehicleTypeId) ? true : false;

        if (item.get("total_fare") != null && !item.get("total_fare").equals("")) {
            viewHolder.totalfare.setText(generalFunc.convertNumberWithRTL(item.get("total_fare")));
        } else if (!Utils.IS_KIOSK_APP) {
            viewHolder.infoimage.setVisibility(View.GONE);
            viewHolder.totalfare.setText("");
        }

        if (Utils.IS_KIOSK_APP) {
            if (isDestinationAdded == false && item.get("eUnitTxt") != null && !item.get("eUnitTxt").equals("")) {
                viewHolder.totalMiles.setText(" /" + item.get("eUnitTxt"));
            } else {
                viewHolder.totalMiles.setText("");
            }
        }


        String imgUrl = "";
        String imgName = "";
        if (isHover) {
            imgName = generalFunc.getImageName(item.get("vLogo1"), mContext);
        } else {
            imgName = generalFunc.getImageName(item.get("vLogo"), mContext);
        }
        if (imgName.equals("")) {
            if (isHover) {
                imgUrl = vehicleDefaultIconPath + "hover_ic_car.png";
            } else {
                imgUrl = vehicleDefaultIconPath + "ic_car.png";
            }
        } else {
            imgUrl = vehicleIconPath + item.get("iVehicleTypeId") + "/android/" + imgName;
        }
        loadImage(viewHolder, imgUrl);


        if (!Utils.IS_KIOSK_APP) {
            if (position == 0) {
                viewHolder.leftSeperationLine.setVisibility(View.INVISIBLE);
                viewHolder.leftSeperationLine2.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.leftSeperationLine.setVisibility(View.VISIBLE);
                viewHolder.leftSeperationLine2.setVisibility(View.VISIBLE);
            }

            if (position == list_item.size() - 1) {
                viewHolder.rightSeperationLine.setVisibility(View.INVISIBLE);
                viewHolder.rightSeperationLine2.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.rightSeperationLine.setVisibility(View.VISIBLE);
                viewHolder.rightSeperationLine2.setVisibility(View.VISIBLE);
            }
        }

        viewHolder.contentArea.setOnClickListener(view -> {
            if (onItemClickList != null) {
                onItemClickList.onItemClick(position, "");
            }
        });


        viewHolder.infoimage.setOnClickListener(view -> {
            if (onItemClickList != null) {
                onItemClickList.onItemClick(position, "info");
            }
        });

        if (isHover == true) {
            viewHolder.imagareaselcted.setVisibility(View.VISIBLE);
            viewHolder.imagarea.setVisibility(View.GONE);

            if (!Utils.IS_KIOSK_APP) {
                if (viewHolder.totalfare.getText().toString().length() > 0) {
                    viewHolder.infoimage.setVisibility(View.VISIBLE);
                }
                viewHolder.totalfare.setTextColor(Color.parseColor("#777b82"));
                viewHolder.carTypeTitle.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_2));
                new CreateRoundedView(mContext.getResources().getColor(R.color.white), Utils.dipToPixels(mContext, 28), 2,
                        mContext.getResources().getColor(R.color.appThemeColor_2), viewHolder.carTypeImgViewselcted);
            } else {
                viewHolder.totalfare.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
                viewHolder.carTypeTitle.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            }
            viewHolder.carTypeImgViewselcted.setBorderColor(mContext.getResources().getColor(R.color.appThemeColor_2));

        } else {
            viewHolder.imagareaselcted.setVisibility(View.GONE);
            viewHolder.imagarea.setVisibility(View.VISIBLE);

            if (!Utils.IS_KIOSK_APP) {
                viewHolder.infoimage.setVisibility(View.GONE);
                new CreateRoundedView(Color.parseColor("#ffffff"), Utils.dipToPixels(mContext, 28), 2,
                        Color.parseColor("#cbcbcb"), viewHolder.carTypeImgView);
                viewHolder.totalfare.setTextColor(Color.parseColor("#BABABA"));
                viewHolder.carTypeTitle.setTextColor(mContext.getResources().getColor(R.color.black));

            } else {
                viewHolder.totalfare.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
                viewHolder.carTypeTitle.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_2));
            }
            viewHolder.carTypeImgView.setBorderColor(Color.parseColor("#cbcbcb"));

        }

        if (isMultiDelivery) {
            viewHolder.totalFareArea.setVisibility(View.GONE);
        } else {
            viewHolder.totalFareArea.setVisibility(View.VISIBLE);
        }

    }


    private void loadImage(final CabTypeAdapter.ViewHolder holder, String imageUrl) {
        new LoadImage.builder(LoadImage.bind(imageUrl), holder.carTypeImgView)
                .setPicassoListener(new LoadImage.PicassoListener() {
                    @Override
                    public void onSuccess() {
                        holder.loaderView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        holder.loaderView.setVisibility(View.VISIBLE);
                    }
                }).build();
        new LoadImage.builder(LoadImage.bind(imageUrl), holder.carTypeImgViewselcted)
                .setPicassoListener(new LoadImage.PicassoListener() {
                    @Override
                    public void onSuccess() {
                        holder.loaderView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        holder.loaderView.setVisibility(View.VISIBLE);
                    }
                }).build();
    }

    @Override
    public int getItemCount() {
        if (list_item == null) {
            return 0;
        }
        return list_item.size();
    }

    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public void clickOnItem(int position, String selected) {
        if (onItemClickList != null) {
            onItemClickList.onItemClick(position, selected);
        }
    }

    public interface OnItemClickList {
        void onItemClick(int position, String selected);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public SelectableRoundedImageView carTypeImgView, carTypeImgViewselcted;
        public MTextView carTypeTitle;
        public View leftSeperationLine;
        public View rightSeperationLine;
        public View leftSeperationLine2;
        public View rightSeperationLine2;
        public LinearLayout contentArea;
        public AVLoadingIndicatorView loaderView, loaderViewselected;
        public MTextView totalfare, totalMiles;
        public ImageView infoimage;
        public LinearLayout totalFareArea;

        public FrameLayout imagarea, imagareaselcted;

        public ViewHolder(View view) {
            super(view);

            carTypeImgView = (SelectableRoundedImageView) view.findViewById(R.id.carTypeImgView);
            carTypeImgViewselcted = (SelectableRoundedImageView) view.findViewById(R.id.carTypeImgViewselcted);
            carTypeTitle = (MTextView) view.findViewById(R.id.carTypeTitle);
            totalFareArea = (LinearLayout) view.findViewById(R.id.totalFareArea);
            if (!Utils.IS_KIOSK_APP) {
                leftSeperationLine = view.findViewById(R.id.leftSeperationLine);
                rightSeperationLine = view.findViewById(R.id.rightSeperationLine);
                leftSeperationLine2 = view.findViewById(R.id.leftSeperationLine2);
                rightSeperationLine2 = view.findViewById(R.id.rightSeperationLine2);
            }
            contentArea = (LinearLayout) view.findViewById(R.id.contentArea);
            loaderView = (AVLoadingIndicatorView) view.findViewById(R.id.loaderView);
            loaderViewselected = (AVLoadingIndicatorView) view.findViewById(R.id.loaderViewselected);
            if (Utils.IS_KIOSK_APP) {
                totalMiles = (MTextView) view.findViewById(R.id.totalMiles);
            }
            totalfare = (MTextView) view.findViewById(R.id.totalfare);
            imagarea = (FrameLayout) view.findViewById(R.id.imagarea);
            imagareaselcted = (FrameLayout) view.findViewById(R.id.imagareaselcted);
            infoimage = (ImageView) view.findViewById(R.id.infoimage);

        }
    }
}

