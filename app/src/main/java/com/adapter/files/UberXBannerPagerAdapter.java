package com.adapter.files;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.zphr.kiosk.R;
import com.utils.LoadImage;
import com.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by Admin on 02-03-2017.
 */
public class UberXBannerPagerAdapter extends PagerAdapter {

    private final ArrayList<String> IMAGES;
    private final LayoutInflater inflater;
    private final Context context;

    private int bannerWidth;
    private final int bannerHeight;

    public UberXBannerPagerAdapter(Context context, ArrayList<String> IMAGES) {
        this.context = context;
        this.IMAGES = IMAGES;
        inflater = LayoutInflater.from(context);

        bannerWidth = (int) (Utils.getScreenPixelWidth(context));
        bannerWidth = ((bannerWidth / 100) * 70);
        bannerHeight = (int) (bannerWidth / 2.0);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup view, int position) {

        View imageLayout = inflater.inflate(R.layout.item_uber_x_banner_design, view, false);
        assert imageLayout != null;

        final ImageView bannerImgView = imageLayout.findViewById(R.id.bannerImgView);

        position = position % IMAGES.size();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bannerImgView.getLayoutParams();
        layoutParams.width = bannerWidth;
        layoutParams.height = bannerHeight;
        bannerImgView.setLayoutParams(layoutParams);

        String imageURL = Utils.getResizeImgURL(context, IMAGES.get(position), bannerWidth, bannerHeight);

        new LoadImage.builder(LoadImage.bind(imageURL), bannerImgView).build();

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, @NotNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}