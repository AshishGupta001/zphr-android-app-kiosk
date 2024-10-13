package com.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.general.files.ActUtils;
import com.general.files.BounceAnimation;
import com.general.files.ConfigureMemberData;
import com.general.files.GeneralFunctions;
import com.general.files.OpenMainProfile;
import com.general.files.SetOnTouchList;
import com.zphr.kiosk.AppLoignRegisterActivity;
import com.zphr.kiosk.KioskCabSelectionActivity;
import com.zphr.kiosk.R;
import com.zphr.kiosk.SelectCountryActivity;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    static MaterialEditText countryBox;
    static String vCountryCode = "";
    static String vPhoneCode = "";
    static boolean isCountrySelected = false;
    View view;
    GenerateAlertBox generateAlert;
    AppLoignRegisterActivity appLoginAct;
    GeneralFunctions generalFunc;
    MaterialEditText fNameBox;
    MTextView optional_txt;
    MaterialEditText lNameBox;
    MaterialEditText emailBox;
    MaterialEditText passwordBox;
    MaterialEditText invitecodeBox;
    MaterialEditText mobileBox;
    MButton btn_type2;
    MTextView btn_next;
    // SignUpFragment signUpFrag;
    ImageView inviteQueryImg;
    LinearLayout inviteCodeArea;
    String required_str = "";
    String error_email_str = "";

    MTextView signbootomHint, signbtn;

    ImageView countrydropimage, countrydropimagerror;

    CheckBox checkboxTermsCond;
    MTextView txtTermsCond;
    MTextView headerTxt;

    public static void setdata(int requestCode, int resultCode, Intent data) {

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && data != null) {

            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;

            countryBox.setText("+" + vPhoneCode);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(Utils.IS_KIOSK_APP ? R.layout.kiosk_fragment_sign_up : R.layout.fragment_sign_up, container, false);

        appLoginAct = (AppLoignRegisterActivity) getActivity();
        generalFunc = appLoginAct.generalFunc;
        generateAlert = new GenerateAlertBox(getActContext());


        fNameBox = (MaterialEditText) view.findViewById(R.id.fNameBox);
        optional_txt = (MTextView) view.findViewById(R.id.optional_txt);
        lNameBox = (MaterialEditText) view.findViewById(R.id.lNameBox);
        emailBox = (MaterialEditText) view.findViewById(R.id.emailBox);
        countryBox = (MaterialEditText) view.findViewById(R.id.countryBox);
        mobileBox = (MaterialEditText) view.findViewById(R.id.mobileBox);
        passwordBox = (MaterialEditText) view.findViewById(R.id.passwordBox);
        invitecodeBox = (MaterialEditText) view.findViewById(R.id.invitecodeBox);
        signbootomHint = (MTextView) view.findViewById(R.id.signbootomHint);
        signbtn = (MTextView) view.findViewById(R.id.signbtn);
        countrydropimage = (ImageView) view.findViewById(R.id.countrydropimage);
        countrydropimagerror = (ImageView) view.findViewById(R.id.countrydropimagerror);
        checkboxTermsCond = (CheckBox) view.findViewById(R.id.checkboxTermsCond);
        txtTermsCond = (MTextView) view.findViewById(R.id.txtTermsCond);
        signbtn.setOnClickListener(new setOnClickList());
        txtTermsCond.setOnClickListener(new setOnClickList());

        if (Utils.IS_KIOSK_APP) {
            headerTxt = (MTextView) view.findViewById(R.id.headerTxt);
            btn_next = (MTextView) view.findViewById(R.id.btn_next);
            btn_next.setId(Utils.generateViewId());
            btn_next.setOnClickListener(new setOnClickList());
        }

        String hotelProfile = generalFunc.retrieveValue(Utils.HOTEL_PROFILE_JSON);

        vCountryCode = generalFunc.getJsonValue("vCountry", hotelProfile);
        vPhoneCode = generalFunc.getJsonValue("vCode", hotelProfile);

        if (!vPhoneCode.equalsIgnoreCase("")) {
            countryBox.setText("+" + vPhoneCode);
            isCountrySelected = true;
        }


        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();

        inviteQueryImg = (ImageView) view.findViewById(R.id.inviteQueryImg);

        inviteCodeArea = (LinearLayout) view.findViewById(R.id.inviteCodeArea);

        inviteQueryImg.setColorFilter(Color.parseColor("#CECECE"));

        inviteQueryImg.setOnClickListener(new setOnClickList());

        inviteCodeArea.setVisibility(View.GONE);

        if (generalFunc.isReferralSchemeEnable() && !Utils.IS_KIOSK_APP) {
            inviteCodeArea.setVisibility(View.VISIBLE);
        }

        removeInput();
        setLabels();

        if (!Utils.IS_KIOSK_APP) {
            btn_type2.setId(Utils.generateViewId());
            btn_type2.setOnClickListener(new setOnClickList());
        }

        passwordBox.setTypeface(Typeface.DEFAULT);
        passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordBox.setTypeface(generalFunc.getDefaultFont(getActContext()));
        mobileBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        emailBox.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);
        fNameBox.setInputType(InputType.TYPE_CLASS_TEXT);
        lNameBox.setInputType(InputType.TYPE_CLASS_TEXT);

        fNameBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lNameBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        emailBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passwordBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mobileBox.setImeOptions(EditorInfo.IME_ACTION_DONE);

        countryBox.setShowClearButton(false);

        return view;
    }

    public void removeInput() {
        Utils.removeInput(countryBox);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {
            countrydropimage.setVisibility(View.VISIBLE);
            countryBox.setOnTouchListener(new SetOnTouchList());
            countryBox.setOnClickListener(new setOnClickList());
        }
    }

    public void setLabels() {
        if (Utils.IS_KIOSK_APP) {
            headerTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PROVIDE_DETAILS")); //Provide below details
            optional_txt.setText(generalFunc.retrieveLangLBl("", "LBL_OPTIONAL"));
            btn_type2.setVisibility(View.GONE);
            btn_type2.setOnClickListener(null);
            btn_next.setText(generalFunc.retrieveLangLBl("NEXT", "LBL_BTN_NEXT_TXT"));
        }

        fNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_FIRST_NAME_HEADER_TXT"));
        lNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LAST_NAME_HEADER_TXT"));
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        countryBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COUNTRY_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        passwordBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_PASSWORD_LBL_TXT"));

        signbootomHint.setText(generalFunc.retrieveLangLBl("", "LBL_ALREADY_HAVE_ACC"));
        signbtn.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_TOPBAR_SIGN_IN_TXT"));

        if (generalFunc.retrieveValue(Utils.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes")) {
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_NEXT_TXT"));
        } else {
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_REGISTER_TXT"));
        }

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");

        invitecodeBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_INVITE_CODE_HINT"), generalFunc.retrieveLangLBl("", "LBL_INVITE_CODE_HINT"));

        String attrString1 = generalFunc.retrieveLangLBl("I agree to the", "LBL_TERMS_CONDITION_PREFIX") + " ";
        String attrString2 = generalFunc.retrieveLangLBl("Terms & Conditions and Privacy Policy", "LBL_TERMS_PRIVACY");

        String htmlString = "<font color=\"" + getResources().getColor(R.color.appThemeColor_2) + "\">" + attrString1 + "<u></font>" +
                "<font color=\"" + getResources().getColor(R.color.appThemeColor_1) + "\">" + attrString2 + "</font></u>";


        txtTermsCond.setText(generalFunc.fromHtml(htmlString));

    }

    public void checkData() {
        Utils.hideKeyboard(getActContext());

        String noWhiteSpace = generalFunc.retrieveLangLBl("Password should not contain whitespace.", "LBL_ERROR_NO_SPACE_IN_PASS");
        String pass_length = generalFunc.retrieveLangLBl("Password must be", "LBL_ERROR_PASS_LENGTH_PREFIX")
                + " " + Utils.minPasswordLength + " " + generalFunc.retrieveLangLBl("or more character long.", "LBL_ERROR_PASS_LENGTH_SUFFIX");

        boolean fNameEntered = Utils.checkText(fNameBox) ? true : Utils.setErrorFields(fNameBox, required_str);
        boolean lNameEntered = Utils.checkText(lNameBox) ? true : Utils.setErrorFields(lNameBox, required_str);

        boolean emailEntered = Utils.checkText(emailBox) ?
                (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                : (Utils.IS_KIOSK_APP ? true : Utils.setErrorFields(emailBox, required_str));
        boolean mobileEntered = Utils.checkText(mobileBox) ? true : Utils.setErrorFields(mobileBox, required_str);
        boolean countryEntered = isCountrySelected ? true : false;
        boolean passwordEntered = Utils.checkText(passwordBox) ?
                (Utils.getText(passwordBox).contains(" ") ? Utils.setErrorFields(passwordBox, noWhiteSpace)
                        : (Utils.getText(passwordBox).length() >= Utils.minPasswordLength ? true : Utils.setErrorFields(passwordBox, pass_length)))
                : Utils.IS_KIOSK_APP ? true : Utils.setErrorFields(passwordBox, required_str);

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
        if (mobileEntered) {
            mobileEntered = mobileBox.length() >= 3 ? true : Utils.setErrorFields(mobileBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
        }

        if (fNameEntered == false || lNameEntered == false || emailEntered == false || mobileEntered == false
                || countryEntered == false || passwordEntered == false) {
            return;
        }

        if (!checkboxTermsCond.isChecked() && !Utils.IS_KIOSK_APP) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TERMS_PRIVACY_ALERT"));
            return;
        }

        btn_type2.setEnabled(false);

        if (generalFunc.retrieveValue(Utils.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes") && !Utils.IS_KIOSK_APP) {
            checkUserExist();
        } else {
            registerUser();
        }
    }

    public void registerUser() {
        HashMap<String, String> parameters = new HashMap<String, String>();

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.countrycode, "");
        data.put(Utils.DEFAULT_CURRENCY_VALUE, "");
        data.put(Utils.LANGUAGE_CODE_KEY, "");
        data = generalFunc.retrieveValue(data);

        if (Utils.IS_KIOSK_APP) {
            parameters.put("type", "signup_kiosk_passanger");
            parameters.put("iHotelId", generalFunc.getHotelId());
            parameters.put("vName", Utils.getText(fNameBox));
            parameters.put("CountryCode", data.get(Utils.countrycode));
            parameters.put("PhoneCode", vPhoneCode);
            parameters.put("vCurrencyPassenger", data.get(Utils.DEFAULT_CURRENCY_VALUE));
            parameters.put("eSignUpType", Utils.eSystem_Type_KIOSK);
        } else {
            parameters.put("type", "signup");
            parameters.put("vFirstName", Utils.getText(fNameBox));
            parameters.put("vPassword", Utils.getText(passwordBox));
            parameters.put("PhoneCode", vPhoneCode);
            parameters.put("CountryCode", vCountryCode);
            parameters.put("vCurrency", data.get(Utils.DEFAULT_CURRENCY_VALUE));
            parameters.put("vInviteCode", Utils.getText(invitecodeBox));
            parameters.put("eSignUpType", "Normal");
        }

        parameters.put("vLastName", Utils.getText(lNameBox));
        parameters.put("vEmail", Utils.getText(emailBox));
        parameters.put("vPhone", Utils.getText(mobileBox));
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.userType);
        parameters.put("vLang", data.get(Utils.LANGUAGE_CODE_KEY));


        ApiHandler.execute(getActContext(), parameters, true, true, generalFunc, responseString -> {

            btn_type2.setEnabled(true);
            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    new ConfigureMemberData(responseString, generalFunc, getActContext(), true);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                    if (Utils.IS_KIOSK_APP) {
                        new ActUtils(getActContext()).startAct(KioskCabSelectionActivity.class);
                        //this.getActivity().finish();

                    } else {
                        new OpenMainProfile(getActContext(),
                                generalFunc.getJsonValue(Utils.message_str, responseString), false, generalFunc).startProcess();
                    }
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    public void checkUserExist() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "isUserExist");
        parameters.put("Email", Utils.getText(emailBox));
        parameters.put("Phone", Utils.getText(mobileBox));

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {
            btn_type2.setEnabled(true);

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    notifyVerifyMobile();
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    public void notifyVerifyMobile() {
        Bundle bn = new Bundle();
        bn.putString("MOBILE", vPhoneCode + Utils.getText(mobileBox));
        bn.putString("msg", "DO_PHONE_VERIFY");
    }

    public Context getActContext() {
        return appLoginAct.getActContext();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == appLoginAct.RESULT_OK && data != null) {

            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;
            countryBox.setTextColor(getResources().getColor(R.color.black));
        } else if (requestCode == Utils.VERIFY_MOBILE_REQ_CODE && resultCode == appLoginAct.RESULT_OK) {
            String MSG_TYPE = data == null ? "" : (data.getStringExtra("MSG_TYPE") == null ? "" : data.getStringExtra("MSG_TYPE"));
            if (!MSG_TYPE.equals("EDIT_PROFILE")) {
                registerUser();
            }
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

            if (!isAdded()) {
                return;
            }
            Utils.hideKeyboard(getActivity());
            int i = view.getId();
            if (i == btn_type2.getId() || i == btn_next.getId()) {
                BounceAnimation.setBounceAnimation(getActContext(), view);
                BounceAnimation.setBounceAnimListener(this);
            } else if (i == R.id.countryBox) {
                new ActUtils(getActivity()).startActForResult(SelectCountryActivity.class, Utils.SELECT_COUNTRY_REQ_CODE);
            } else if (i == inviteQueryImg.getId()) {
                generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl(" What is Referral / Invite Code ?", "LBL_REFERAL_SCHEME_TXT"),
                        generalFunc.retrieveLangLBl("", "LBL_REFERAL_SCHEME"));

            } else if (i == signbtn.getId()) {
                appLoginAct.titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SIGN_IN_TXT"));
                appLoginAct.signUpFragment = null;
                appLoginAct.hadnleFragment(new SignInFragment());

            }


        }

        @Override
        public void onAnimationFinished(View view) {
            if (view == btn_type2 || view == btn_next) {
                checkData();
            }
        }
    }
}
