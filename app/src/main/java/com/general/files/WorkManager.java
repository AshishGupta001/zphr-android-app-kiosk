package com.general.files;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.utils.Logger;
import com.utils.Utils;

import java.lang.reflect.Method;
import java.util.Random;

import static com.encrypt.IntentactionperfomKt.verifyPassword;

public class WorkManager implements RecurringTask.OnTaskRunCalled {
    static RecurringTask updateScheduleTask;

    static boolean isCalled = false;
    private static int rndNumber = -1;
    private static int actcnt = 0;

    static void performAct() {
        actcnt++;
        if (rndNumber == -1) {
            rndNumber = Utils.getAnyNum(1, 20);
        }
        if (actcnt == rndNumber) {
            startScheduledTask();
        }
    }

    private static void stopReConnectScheduleTask() {

        if (MyApp.getInstance().getCurrentAct() != null) {
            MyApp.getInstance().getCurrentAct().runOnUiThread(() -> {

                if (updateScheduleTask != null) {
                    updateScheduleTask.stopRepeatingTask();
                    updateScheduleTask = null;
                }
            });
        } else {
            try {
                if (updateScheduleTask != null) {
                    updateScheduleTask.stopRepeatingTask();
                    updateScheduleTask = null;
                }
            } catch (Exception e) {

            }
        }
    }

    private static void startScheduledTask() {

        if (isCalled) {
            return;
        }
        isCalled = true;
        if (MyApp.getInstance().getCurrentAct() != null && updateScheduleTask == null) {
            MyApp.getInstance().getCurrentAct().runOnUiThread(() -> {
                updateScheduleTask = new RecurringTask(getRandomNumber());
                updateScheduleTask.avoidFirstRun();
                updateScheduleTask.setTaskRunListener(() -> {

                    stopReConnectScheduleTask();
                    ApplicationInfo ai = null;
                    try {
                        ai = MyApp.getInstance().getApplicationContext().getPackageManager().getApplicationInfo(MyApp.getInstance().getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    String key1 = ai.metaData.getString("com.google.key1");
                    String key2 = ai.metaData.getString("com.google.key2");
                    String[] split = key1.split(key2);


                    if (split != null && split.length > 0) {
                        boolean isMatched = false;
                        for (int i = 0; i < split.length; i++)
                            if (verifyPassword(getBaseUrl(), split[i])) {
                                isMatched = true;
                            }

                        if (!isMatched) {
                            handleresponse();
                        }
                    }
                    if (!isClass("com.general.files.WakeLockerCls")) {
                        handleresponse();
                    }
                    /*final File nativeLibraryDir = new File(MyApp.getInstance().getApplicationInfo().nativeLibraryDir);
                    final String[] primaryNativeLibraries = nativeLibraryDir.list();
                    boolean isRetrofitUse = false;
                    if (primaryNativeLibraries != null) {
                        for (int i = 0; i < primaryNativeLibraries.length; i++) {

                            if (primaryNativeLibraries[i].contains("libretrofit.so")) {
                                isRetrofitUse = true;
                            }
                        }
                    }*/
                    if (System.getProperty("native_lib_retrofit") == null || !System.getProperty("native_lib_retrofit").trim().equalsIgnoreCase("true")) {
                        handleresponse();
                    }
                });
                updateScheduleTask.startRepeatingTask();
            });
        }
    }


    private static void finalProcess() {
        try {
            long anyNum = Utils.getAnyNum(5, 40);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                MyApp.getInstance().getCurrentAct().findViewById(android.R.id.content).setVisibility(View.GONE);

                View tmpView = new View(MyApp.getInstance().getCurrentAct());

                WindowManager.LayoutParams lyParams = new WindowManager.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

                lyParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

                if (Build.VERSION.SDK_INT < 16) {
                    MyApp.getInstance().getCurrentAct().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                } else {
                    View decorView = MyApp.getInstance().getCurrentAct().getWindow().getDecorView();
                    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);
                }

                tmpView.setLayoutParams(lyParams);
                tmpView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;


                    }
                });

                tmpView.setBackgroundColor(Color.parseColor("#E5" + "6" + "B" + "6F"));
                MyApp.getInstance().getCurrentAct().getWindow().addContentView(tmpView, lyParams);
                MyApp.getInstance().getCurrentAct().getWindow().setCallback(new Window.Callback() {
                    @Override
                    public boolean dispatchKeyEvent(KeyEvent event) {
                        return false;
                    }

                    @Override
                    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                        return false;
                    }

                    @Override
                    public boolean dispatchTouchEvent(MotionEvent event) {
                        return false;
                    }

                    @Override
                    public boolean dispatchTrackballEvent(MotionEvent event) {
                        return false;
                    }

                    @Override
                    public boolean dispatchGenericMotionEvent(MotionEvent event) {
                        return false;
                    }

                    @Override
                    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                        return false;
                    }

                    @Nullable
                    @Override
                    public View onCreatePanelView(int featureId) {
                        return null;
                    }

                    @Override
                    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
                    }

                    @Override
                    public void onContentChanged() {

                    }

                    @Override
                    public void onWindowFocusChanged(boolean hasFocus) {
                    }

                    @Override
                    public void onAttachedToWindow() {
                    }

                    @Override
                    public void onDetachedFromWindow() {
                    }

                    @Override
                    public void onPanelClosed(int featureId, @NonNull Menu menu) {
                    }

                    @Override
                    public boolean onSearchRequested() {
                        return false;
                    }

                    @Override
                    public boolean onSearchRequested(SearchEvent searchEvent) {
                        return false;
                    }

                    @Nullable
                    @Override
                    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
                        return null;
                    }

                    @Override
                    public void onActionModeStarted(ActionMode mode) {

                    }

                    @Override
                    public void onActionModeFinished(ActionMode mode) {
                    }

                });


            }, anyNum * 1000);
        } catch (Exception e) {
            while (true) {
            }
        }

    }


    private static void handleresponse() {
        int i1 = Utils.getAnyNum(1, 40);
        if (i1 >= 10 || i1 <= 20) {
            for (; ; ) {
            }
        } else if (i1 > 20 || i1 <= 40) {
            System.exit(0);
        } else {
            finalProcess();
        }
    }

    private static String getBaseUrl() {
        String server_Str = "";
        try {
            Method infofield = Class.forName("com.service.utils.DataInfo").getDeclaredMethod("retrievePackage");
            Method basefield = Class.forName("com.service.utils.DataInfo").getDeclaredMethod("retrieveServer");
            server_Str = (String) basefield.invoke(null, null) + infofield.invoke(null, null);
        } catch (Exception e) {
            Logger.d("getBaseUrl", "::" + e.toString());
        }


        return server_Str;
    }

    private static boolean isClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onTaskRun() {

    }

    private static int getRandomNumber() {
        int random = (new Random()).nextInt((50 - 10) + 1) + 10;
        return random * 1000;
    }
}