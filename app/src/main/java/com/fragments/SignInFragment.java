package com.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zphr.kiosk.AppLoignRegisterActivity;
import com.zphr.kiosk.ContactUsActivity;
import com.zphr.kiosk.ForgotPasswordActivity;
import com.zphr.kiosk.R;
import com.zphr.kiosk.KioskLandingScreenActivity;
import com.zphr.kiosk.SelectCountryActivity;
import com.general.files.BounceAnimation;
import com.general.files.GeneralFunctions;
import com.general.files.OpenMainProfile;
import com.general.files.SetOnTouchList;
import com.general.files.ConfigureMemberData;
import com.general.files.ActUtils;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment {


    MaterialEditText emailBox;
    MaterialEditText passwordBox;
    MButton btn_type2;

    AppLoignRegisterActivity appLoginAct;
    GeneralFunctions generalFunc;

    int submitBtnId;
    MTextView forgetPassTxt;

    View view;

    String required_str = "";
    String error_email_str = "";

    MTextView registerTxt;

    static MaterialEditText countryBox;
    static String vCountryCode = "";
    static String vPhoneCode = "";
    static boolean isCountrySelected = false;
    FrameLayout countryArea;
    ImageView countrydropimage, countrydropimagerror;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(Utils.IS_KIOSK_APP ? R.layout.kiosk_fragment_sign_in : R.layout.fragment_sign_in, container, false);

        appLoginAct = (AppLoignRegisterActivity) getActivity();
        generalFunc = appLoginAct.generalFunc;

        countryArea = (FrameLayout) view.findViewById(R.id.countryArea);
        countryBox = (MaterialEditText) view.findViewById(R.id.countryBox);
        emailBox = (MaterialEditText) view.findViewById(R.id.emailBox);
        emailBox.setHelperTextAlwaysShown(true);
        passwordBox = (MaterialEditText) view.findViewById(R.id.passwordBox);
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        forgetPassTxt = (MTextView) view.findViewById(R.id.forgetPassTxt);

        registerTxt = (MTextView) view.findViewById(R.id.registerTxt);
        registerTxt.setOnClickListener(new setOnClickList());


        passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordBox.setTypeface(generalFunc.getDefaultFont(getActContext()));


        emailBox.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);

        emailBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passwordBox.setImeOptions(EditorInfo.IME_ACTION_DONE);


        countrydropimage = (ImageView) view.findViewById(R.id.countrydropimage);
        countrydropimagerror = (ImageView) view.findViewById(R.id.countrydropimagerror);

        vCountryCode = generalFunc.retrieveValue(Utils.DefaultCountryCode);
        vPhoneCode = generalFunc.retrieveValue(Utils.DefaultPhoneCode);

        int paddingValStart = (int) getResources().getDimension(R.dimen._35sdp);
        int paddingValEnd = (int) getResources().getDimension(R.dimen._12sdp);
        if (generalFunc.isRTLmode()) {
            countryBox.setPaddings(paddingValEnd, 0, paddingValStart, 0);

        } else {
            countryBox.setPaddings(paddingValStart, 0, paddingValEnd, 0);

        }

        if (!vPhoneCode.equalsIgnoreCase("")) {
            countryBox.setText("+" + generalFunc.convertNumberWithRTL(vPhoneCode));
            isCountrySelected = true;
        }

        countryBox.setShowClearButton(false);


        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());
        forgetPassTxt.setOnClickListener(new setOnClickList());
        setLabels();
        if (generalFunc.retrieveValue("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD").equalsIgnoreCase("Yes")) {
            removeInput();
            emailBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    if (charSequence.length() > 3 && TextUtils.isDigitsOnly(emailBox.getText())) {
                        countryArea.setVisibility(View.VISIBLE);
                    } else {
                        countryArea.setVisibility(View.GONE);
                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
        return view;
    }

    public void removeInput() {
        Utils.removeInput(countryBox);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {
            countryBox.setOnTouchListener(new SetOnTouchList());

            countryBox.setOnClickListener(new setOnClickList());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        emailBox.requestFocus();
    }

    public void setLabels() {
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_PHONE_EMAIL"));
        emailBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_SIGN_IN_MOBILE_EMAIL_HELPER"));
        emailBox.setHelperTextAlwaysShown(true);
        passwordBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_PASSWORD_LBL_TXT"));
        countryBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COUNTRY_TXT"));
        registerTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DONT_HAVE_AN_ACCOUNT"));

        forgetPassTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FORGET_PASS_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_LOGIN"));

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");
    }

    public void checkValues() {
        Utils.hideKeyboard(getActContext());
        String noWhiteSpace = generalFunc.retrieveLangLBl("Password should not contain whitespace.", "LBL_ERROR_NO_SPACE_IN_PASS");
        String pass_length = generalFunc.retrieveLangLBl("Password must be", "LBL_ERROR_PASS_LENGTH_PREFIX")
                + " " + Utils.minPasswordLength + " " + generalFunc.retrieveLangLBl("or more character long.", "LBL_ERROR_PASS_LENGTH_SUFFIX");

        boolean countryEntered = false;
        boolean emailEntered = Utils.checkText(emailBox.getText().toString().replace("+", "")) ? true
                : Utils.setErrorFields(emailBox, required_str);

        boolean passwordEntered = Utils.checkText(passwordBox) ?
                (Utils.getText(passwordBox).contains(" ") ? Utils.setErrorFields(passwordBox, noWhiteSpace)
                        : (Utils.getText(passwordBox).length() >= Utils.minPasswordLength ? true : Utils.setErrorFields(passwordBox, pass_length)))
                : Utils.setErrorFields(passwordBox, required_str);


        String regexStr = "^[0-9]*$";

        if (emailBox.getText().toString().trim().replace("+", "").matches(regexStr)) {
            if (emailEntered) {
                emailEntered = emailBox.length() >= 3 ? true : Utils.setErrorFields(emailBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
            }
            if (generalFunc.retrieveValue("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD").equalsIgnoreCase("Yes")) {
                countryEntered = isCountrySelected ? true : false;
                if (countryBox.getText().length() == 0) {
                    countryEntered = false;
                }

                if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {

                    if (!countryEntered) {
                        Utils.setErrorFields(countryBox, required_str);
                        countrydropimagerror.setVisibility(View.VISIBLE);
                        countrydropimage.setVisibility(View.GONE);
                    } else {
                        countrydropimage.setVisibility(View.VISIBLE);
                        countrydropimagerror.setVisibility(View.GONE);

                    }
                }
            }

        } else {
            emailEntered = Utils.checkText(emailBox) ?
                    (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                    : Utils.setErrorFields(emailBox, required_str);

            if (emailEntered == false) {
                return;
            }
        }

        if (emailEntered == false || passwordEntered == false) {
            return;
        }

        btn_type2.setEnabled(false);
        signInUser();
    }

    public void signInUser() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "signIn");
        parameters.put("vEmail", Utils.getText(emailBox));
        parameters.put("vPassword", Utils.getText(passwordBox));
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.IS_KIOSK_APP ? Utils.eSystem_Type_KIOSK : Utils.app_type);
        parameters.put("CountryCode", vCountryCode);
        parameters.put("PhoneCode", vPhoneCode);

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.DEFAULT_CURRENCY_VALUE, "");
        data.put(Utils.LANGUAGE_CODE_KEY, "");
        data = generalFunc.retrieveValue(data);
        parameters.put("vCurrency", data.get(Utils.DEFAULT_CURRENCY_VALUE));
        parameters.put("vLang", data.get(Utils.LANGUAGE_CODE_KEY));

        ApiHandler.execute(getActContext(), parameters, true, true, generalFunc, responseString -> {

            btn_type2.setEnabled(true);
            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    new ConfigureMemberData(responseString, generalFunc, getActContext(), false);

                    generalFunc.storeData(Utils.IS_KIOSK_APP ? Utils.HOTEL_PROFILE_JSON : Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));


                    if (Utils.IS_KIOSK_APP) {
                        JSONObject jsonObject = generalFunc.getJsonObject(responseString);
                        JSONObject jo = generalFunc.getJsonObject(Utils.message_str, jsonObject);
                        String iHotelId = generalFunc.getJsonValueStr("iHotelId", jo);
                        System.out.println("ihotelid : " + iHotelId);
                        generalFunc.storeHotelData(iHotelId);

                        HashMap<String, String> storeData = new HashMap<>();

                        storeData.put(Utils.PUBNUB_PUB_KEY, generalFunc.getJsonValueStr("PUBNUB_PUBLISH_KEY", jo));
                        storeData.put(Utils.PUBNUB_SUB_KEY, generalFunc.getJsonValueStr("PUBNUB_SUBSCRIBE_KEY", jo));
                        storeData.put(Utils.PUBNUB_SEC_KEY, generalFunc.getJsonValueStr("PUBNUB_SECRET_KEY", jo));
                        storeData.put(Utils.RIDER_REQUEST_ACCEPT_TIME_KEY, generalFunc.getJsonValueStr("RIDER_REQUEST_ACCEPT_TIME", jo));
                        storeData.put("showCountryList", generalFunc.getJsonValueStr("showCountryList", jo));

                        storeData.put(Utils.DEVICE_SESSION_ID_KEY, generalFunc.getJsonValueStr("tDeviceSessionId", jo));
                        storeData.put(Utils.SESSION_ID_KEY, generalFunc.getJsonValueStr("tSessionId", jo));

                        storeData.put(Utils.PUBNUB_DISABLED_KEY, generalFunc.getJsonValueStr("PUBNUB_DISABLED", jo));
                        storeData.put(Utils.ENABLE_SOCKET_CLUSTER_KEY, generalFunc.getJsonValueStr("ENABLE_SOCKET_CLUSTER", jo));
                        storeData.put(Utils.SC_CONNECT_URL_KEY, generalFunc.getJsonValueStr("SC_CONNECT_URL", jo));
                        storeData.put(Utils.GOOGLE_SERVER_KEY, generalFunc.getJsonValueStr("GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY", jo));

                        storeData.put("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", generalFunc.getJsonValue("ENABLE_PHONE_LOGIN_VIA_COUNTRY_SELECTION_METHOD", responseString));
                        storeData.put("ENABLE_EMAIL_OPTIONAL", generalFunc.getJsonValue("ENABLE_EMAIL_OPTIONAL", responseString));
                        if (generalFunc.getJsonValue("Visit_Locations", jo) != null && !generalFunc.getJsonValue("Visit_Locations", jo).equals("")) {
                            storeData.put(Utils.VisitLocationsKey, generalFunc.getJsonValueStr("Visit_Locations", jo));
                        }
                        generalFunc.storeData(storeData);

                        Bundle bn = new Bundle();
                        new ActUtils(getActContext()).startActWithData(KioskLandingScreenActivity.class, bn);
                        ActivityCompat.finishAffinity((Activity) getActContext());

                    } else {
                        new OpenMainProfile(getActContext(),
                                generalFunc.getJsonValue(Utils.message_str, responseString), false, generalFunc).startProcess();
                    }

                } else {
                    passwordBox.setText("");
                    if (generalFunc.getJsonValue("eStatus", responseString).equalsIgnoreCase("Deleted")) {
                        openContactUsDialog(responseString);
                    } else if (generalFunc.getJsonValue("eStatus", responseString).equalsIgnoreCase("Inactive")) {
                        openContactUsDialog(responseString);
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    public void openContactUsDialog(String responseString) {
        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
        alertBox.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {

                alertBox.closeAlertBox();
                if (btn_id == 0) {
                    new ActUtils(getActContext()).startAct(ContactUsActivity.class);
                }
            }
        });
        alertBox.showAlertBox();
    }

    public Context getActContext() {
        return appLoginAct.getActContext();
    }

    public static void setdata(int requestCode, int resultCode, Intent data) {

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && data != null) {

            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;

            countryBox.setText("+" + vPhoneCode);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == appLoginAct.RESULT_OK && data != null) {

            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");

            isCountrySelected = true;
            countryBox.setTextColor(getResources().getColor(R.color.black));
            countryBox.setText("+" + generalFunc.convertNumberWithRTL(vPhoneCode));

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }

    public class setOnClickList implements View.OnClickListener, BounceAnimation.BounceAnimListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActivity());
            if (i == submitBtnId) {
                BounceAnimation.setBounceAnimation(getActContext(), view);
                BounceAnimation.setBounceAnimListener(this);
            } else if (i == forgetPassTxt.getId()) {
                new ActUtils(getActContext()).startAct(ForgotPasswordActivity.class);
            } else if (i == R.id.countryBox) {
                Intent intent = new Intent(getActivity(), SelectCountryActivity.class);
                getActivity().startActivityForResult(intent, Utils.SELECT_COUNTRY_REQ_CODE);
            }

        }

        @Override
        public void onAnimationFinished(View view) {
            if (view.getId() == submitBtnId) {
                checkValues();
            }
        }
    }
}
