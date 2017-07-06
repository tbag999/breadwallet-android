package com.breadwallet.presenter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.breadwallet.R;
import com.breadwallet.presenter.entities.TransactionListItem;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.animation.SlideDetector;
import com.breadwallet.tools.manager.SharedPreferencesManager;
import com.breadwallet.tools.util.BRCurrency;
import com.breadwallet.tools.util.BRExchange;
import com.breadwallet.wallet.BRPeerManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class FragmentTransactionItem extends Fragment {
    private static final String TAG = FragmentTransactionItem.class.getName();

    public TextView mTitle;
    private TextView mDescriptionText;
    private TextView mSubHeader;
    private TextView mConfirmationText;
    private TextView mAvailableSpend;
    private TextView mCommentText;
    private TextView mAmountText;
    private TextView mAddressText;
    private TextView mDateText;
    private TextView mToFromBottom;
    private TextView mTxHash;
    private TransactionListItem item;
    private LinearLayout signalLayout;
    private ImageButton close;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.

        View rootView = inflater.inflate(R.layout.transaction_details_item, container, false);
        signalLayout = (LinearLayout) rootView.findViewById(R.id.signal_layout);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mDescriptionText = (TextView) rootView.findViewById(R.id.description_text);
        mSubHeader = (TextView) rootView.findViewById(R.id.sub_header);
        mCommentText = (TextView) rootView.findViewById(R.id.comment_text);
        mAmountText = (TextView) rootView.findViewById(R.id.amount_text);
        mAddressText = (TextView) rootView.findViewById(R.id.address_text);
        mDateText = (TextView) rootView.findViewById(R.id.date_text);
        mToFromBottom = (TextView) rootView.findViewById(R.id.to_from);
        mConfirmationText = (TextView) rootView.findViewById(R.id.confirmation_text);
        mAvailableSpend = (TextView) rootView.findViewById(R.id.available_spend);
        mTxHash = (TextView) rootView.findViewById(R.id.tx_hash);
        close = (ImageButton) rootView.findViewById(R.id.close_button);

        signalLayout.setOnTouchListener(new SlideDetector(getContext(), signalLayout));

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                getActivity().onBackPressed();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity app = getActivity();
                if (app != null)
                    app.getFragmentManager().popBackStack();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillTexts();

    }

    private void fillTexts() {
//        Log.e(TAG, "fillTexts fee: " + item.getFee());
//        Log.e(TAG, "fillTexts hash: " + item.getHexId());
        //get the current iso
        String iso = SharedPreferencesManager.getPreferredBTC(getActivity()) ? "BTC" : SharedPreferencesManager.getIso(getContext());
        //get the tx amount
        BigDecimal txAmount = new BigDecimal(item.getReceived() - item.getSent()).abs();
        //see if it was sent
        boolean sent = item.getReceived() - item.getSent() < 0;

        int blockHeight = item.getBlockHeight();
        int confirms = blockHeight == Integer.MAX_VALUE ? 0 : SharedPreferencesManager.getLastBlockHeight(getContext()) - blockHeight + 1;

        //calculated and formatted amount for iso
        String amountWithFee = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, txAmount));
        String amount = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, item.getFee() == -1 ? txAmount : txAmount.subtract(new BigDecimal(item.getFee()))));
        //calculated and formatted fee for iso
        String fee = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, new BigDecimal(item.getFee())));
        //description (Sent $24.32 ....)
        Spannable descriptionString = sent ? new SpannableString("Sent " + amountWithFee) : new SpannableString("Received " + amountWithFee);

        String startingBalance = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, new BigDecimal(sent ? item.getBalanceAfterTx() + txAmount.longValue() : item.getBalanceAfterTx() - txAmount.longValue())));
        String endingBalance = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromSatoshis(getActivity(), iso, new BigDecimal(item.getBalanceAfterTx())));
        String commentString = "For Love";
        String amountString = String.format("%s %s\n\nStarting Balance: %s\nEnding Balance:  %s", amount, item.getFee() == -1 ? "" : String.format("(%s fee)", fee), startingBalance, endingBalance);


        SpannableString addr = sent ? new SpannableString(item.getTo()[0]) : new SpannableString(item.getFrom()[0]);
        SpannableString toFrom = sent ? new SpannableString("to") : new SpannableString("from");
        toFrom.setSpan(new RelativeSizeSpan(1f), 0, toFrom.length(), 0);
        //span a piece of text to be smaller size (the address)
        final StyleSpan norm = new StyleSpan(Typeface.NORMAL);
        addr.setSpan(norm, 0, addr.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        addr.setSpan(new RelativeSizeSpan(0.8f), 0, addr.length(), 0);

        mTxHash.setText(item.getHexId());

        int relayCount = BRPeerManager.getRelayCount(item.getHexId());

        int level = 0;
        Log.e(TAG, "setTexts: confirms: " + confirms);
        Log.e(TAG, "setTexts: relayCount: " + relayCount);

        if (confirms <= 0) {
            if (relayCount <= 0)
                level = 0;
            else if (relayCount == 1)
                level = 1;
            else
                level = 2;
        } else {
            if (confirms == 1)
                level = 3;
            else if (confirms == 2)
                level = 4;
            else if (confirms == 3)
                level = 5;
            else
                level = 6;
        }
        boolean availableForSpend = false;
        String sentReceived = !sent ? "Receiving" : "Sending";
        String percentage = "";
        switch (level) {
            case 0:
                percentage = "0%";
                break;
            case 1:
                percentage = "20%";
                break;
            case 2:
                percentage = "40%";
                availableForSpend = true;
                break;
            case 3:
                percentage = "60%";
                availableForSpend = true;
                break;
            case 4:
                percentage = "80%";
                availableForSpend = true;
                break;
            case 5:
                percentage = "100%";
                availableForSpend = true;
                break;
        }

        if (availableForSpend) {
            mAvailableSpend.setText("Available to Spend");
        } else {
            signalLayout.removeView(mAvailableSpend);
        }

        if (level == 6) {
            mConfirmationText.setText(getString(R.string.Transaction_complete));
        } else {
            mConfirmationText.setText(String.format("%s - %s", sentReceived, percentage));
        }

        if (!item.isValid())
            mConfirmationText.setText("INVALID");

        mToFromBottom.setText(sent ? "From" : "To");
        mDateText.setText(getFormattedDate(item.getTimeStamp()));
        mDescriptionText.setText(TextUtils.concat(descriptionString));
        mSubHeader.setText(TextUtils.concat(toFrom, " ", addr));
        mCommentText.setText(commentString);
        mAmountText.setText(amountString);
        mAddressText.setText(sent ? item.getFrom()[0] : item.getTo()[0]);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static FragmentTransactionItem newInstance(TransactionListItem item) {

        FragmentTransactionItem f = new FragmentTransactionItem();
        f.setItem(item);

        return f;
    }

    public void setItem(TransactionListItem item) {
        this.item = item;

    }

    private String getFormattedDate(long timeStamp) {

        Date currentLocalTime = new Date(timeStamp == 0 ? System.currentTimeMillis() : timeStamp * 1000);

        SimpleDateFormat date1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat date2 = new SimpleDateFormat("HH:mm a", Locale.getDefault());

        String str1 = date1.format(currentLocalTime);
        String str2 = date2.format(currentLocalTime);

        return str1 + " at " + str2;
    }


}