package com.zphr.kiosk;

import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.general.files.GeneralFunctions;
import com.utils.KioskMode;
import com.utils.MyDeviceAdminReceiver;
import com.utils.Utils;

public class BaseActivity extends AppCompatActivity {

    protected Context mContext = this;
    protected View mDecorView;
    protected DevicePolicyManager mDevicePolicyManager;
    protected ComponentName mAdminComponentName;
    private boolean isAdmin;
    private boolean isKiosk;
    private GeneralFunctions generalFunc;
    private static final String TAG = "KIOSK";
    private KioskMode kioskMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kioskMode = KioskMode.getKioskMode();
        mDecorView = getWindow().getDecorView();
        generalFunc = new GeneralFunctions(this);
        mDecorView = getWindow().getDecorView();
        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

    // KIOSK MODE & SCREEN PINNING IMPLEMENTATION START
     /*
        setImmersiveMode(true);

       if (this instanceof LauncherActivity || (this instanceof AppLoignRegisterActivity && Utils.checkText(generalFunc.getHotelId())))
        Log.i(TAG, "ISLOCKED"+!MyApp.ISLOCKED);
        setUpScreenPin();
        */
    // KIOSK MODE & SCREEN PINNING IMPLEMENTATION END
    }

    protected void setUpScreenPin() {

        isAdmin = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                isAdmin = true;
            } else {
            }
        }
        enableKioskMode(true);
        //TODO : for clear device Owner
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setKioskPolicies(true, isAdmin);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void removeScreenPin() {
        try {
            setKioskPolicies(false, isAdmin);
            enableKioskMode(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void enableKioskMode(boolean enabled) {
        try {
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())) {
                        isKiosk = true;
                        mDevicePolicyManager.lockNow();
                        startLockTask();
                    } else {
                        isKiosk = false;
                        ComponentName compName = new ComponentName(this, MyDeviceAdminReceiver.class);
                        mDevicePolicyManager.removeActiveAdmin(compName);
                        Log.e("Kiosk Mode Error", getString(R.string.kiosk_not_permitted));
                    }
                }
            } else {
                isKiosk = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    stopLockTask();
                }
            }
        } catch (Exception e) {
            isKiosk = false;
            // TODO: Log and handle appropriately
            Log.e("Kiosk Mode Error", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setKioskPolicies(boolean enable, boolean isAdmin) {
        if (isAdmin) {
            setRestrictions(enable);
            enableStayOnWhilePluggedIn(enable);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setUpdatePolicy(enable);
                setKeyGuardEnabled(enable);
            }
            setAsHomeApp(enable);
        }
        setLockTask(enable, isAdmin);
        setImmersiveMode(enable);
    }

    // region restrictions
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setRestrictions(boolean disallow) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
        }
    }
    // endregion

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void enableStayOnWhilePluggedIn(boolean active) {
        if (active) {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "0");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setLockTask(boolean start, boolean isAdmin) {
        if (isAdmin) {
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, new String[]{getPackageName()});
        }
        if (start) {
            startLockTask();
            kioskMode.lockUnlock(this, true);
        } else {
            kioskMode.lockUnlock(this, false);
            stopLockTask();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setUpdatePolicy(boolean enable) {
        if (enable) {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                    SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setAsHomeApp(boolean enable) {
        Log.i(TAG, "enable");
        if (enable) {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
            intentFilter.addCategory(Intent.CATEGORY_HOME);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            mDevicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName, intentFilter, new ComponentName(getPackageName(), LauncherActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                    mAdminComponentName, getPackageName());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setKeyGuardEnabled(boolean enable) {
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, !enable);
    }

    private void setImmersiveMode(boolean enable) {
        Log.i(TAG, "enable");
        if (enable) {
            int flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN  // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            mDecorView.setSystemUiVisibility(flags);
        } else {
            int flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            mDecorView.setSystemUiVisibility(flags);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        //move current activity to front if required
        boolean isHotelId = Utils.checkText(generalFunc.getHotelId());
        Log.i(TAG, "onUserLeaveHint()..." + isHotelId);

     //Enable KIOSK MODE RELATED PERMISSION START -
       /*
       if (isHotelId)
       {
         kioskMode.moveTaskToFront(this);
       }
      */
    }

    // override back,home ,recent & settings
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isClickable = Utils.checkText(generalFunc.getHotelId()) ? false : true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i(TAG, "KEYCODE_BACK..." + isClickable);

            return isClickable;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.i(TAG, "KEYCODE_HOME..." + isClickable);

            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_SETTINGS) {

            Log.i(TAG, "KEYCODE_SETTINGS..." + isClickable);
            return isClickable;
        }

        return super.onKeyDown(keyCode, event);
    }
}
