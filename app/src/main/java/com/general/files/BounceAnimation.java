package com.general.files;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.zphr.kiosk.R;

public class BounceAnimation {

   public static BounceAnimListener bounceAnimListener;

    public static void setBounceAnimation(Context context,View view) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.bounce_interpolator);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (bounceAnimListener != null) {
                    bounceAnimListener.onAnimationFinished(view);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    public interface BounceAnimListener {
        void onAnimationFinished(View view);
    }

    public static void setBounceAnimListener(BounceAnimListener bounceAnimLis) {
        bounceAnimListener = bounceAnimLis;
    }
}
