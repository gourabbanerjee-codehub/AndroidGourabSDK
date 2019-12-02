package com.example.ezypayaepssdk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.finopaytech.finosdk.activity.MainTransactionActivity;
import com.finopaytech.finosdk.encryption.AES_BC;
import com.finopaytech.finosdk.helpers.Utils;
import com.finopaytech.finosdk.models.ErrorSingletone;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

;

class TestActivity extends AppCompatActivity {


    private TextView clickMe;
    private EditText refid,amount;
    private String RefID="",Amount="",TYPEID="",timeStamp="";
    private RadioGroup ItemtypeGroup;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_aeps);

        timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

        refid=findViewById(R.id.editText2);
        amount=findViewById(R.id.editText3);
        refid.setText(timeStamp);

        RefID=refid.getText().toString();
        Amount=amount.getText().toString();



        ItemtypeGroup = (RadioGroup) findViewById(R.id.radiogroup);
        ItemtypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged (RadioGroup group,int checkedId){

                Log.d("chk", "id" + checkedId);

                if (checkedId == R.id.cash_withdraw) {

                    TYPEID=Constants.SERVICE_AEPS_CW;
                    amount.setText("100");
                   // refid.setText(timeStamp);

                } else if (checkedId == R.id.balance_enquiry) {
                    TYPEID=Constants.SERVICE_AEPS_BE;
                    amount.setText("0");
                   // refid.setText(timeStamp);

                }
            }
        });

        clickMe=findViewById(R.id.textView19);
        clickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!refid.getText().toString().equals("")&&!amount.getText().toString().equals("")&&!TYPEID.equals("")){

                    Intent intent = new Intent(TestActivity.this, MainTransactionActivity.class);
                    intent.putExtra("RequestData", getEncryptedRequest());
                    intent.putExtra("HeaderData", getEncryptedHeader());
                    intent.putExtra("ReturnTime", 5);// Application return time in second
                    startActivityForResult(intent, 1);
                    Toast.makeText(TestActivity.this,refid.getText().toString()+"\n\n"+amount.getText().toString()+"\n\n"+TYPEID,Toast.LENGTH_LONG).show();

                }
                else {

                    Toast.makeText(TestActivity.this,"Enter Amount",Toast.LENGTH_SHORT).show();

                }


            }
        });

   /*    // String requestData= Utils.replaceNewLine(AES_BC.getInstance().encryptEncode(getRequestDataJson().toString(), Constants.CLIENT_REQUEST_ENCRYPTION_KEY));
        Intent intent = new Intent(this, MainTransactionActivity.class);
        intent.putExtra("RequestData", getEncryptedRequest());
        intent.putExtra("HeaderData", getEncryptedHeader());
        intent.putExtra("ReturnTime", 5);// Application return time in second
        startActivityForResult(intent, 1);*/
    }


    public String getEncryptedRequest()
    {
        String strRequestData = "";
        JSONObject jsonRequestDataObj = new JSONObject(); // inner object request
        try {
            jsonRequestDataObj.put("MerchantId", Constants.MERCHANT_ID);
            jsonRequestDataObj.put("SERVICEID", TYPEID);
            jsonRequestDataObj.put("RETURNURL", Constants.RETURN_URL);
            jsonRequestDataObj.put("Version", Constants.VERSION);
            jsonRequestDataObj.put("Amount", amount.getText().toString());
            jsonRequestDataObj.put("ClientRefID", refid.getText().toString());
           /* if(getServiceID().equals(Constants.SERVICE_AEPS_TS)||getServiceID().equals(Constants.SERVICE_MICRO_TS)){
                jsonRequestDataObj.put("Amount", "0");
                jsonRequestDataObj.put("ClientRefID", "20191014134");
            } else {
                jsonRequestDataObj.put("Amount", "100");
                jsonRequestDataObj.put("ClientRefID",Utils.generateRefID(Constants.MERCHANT_ID));
            }*/
            strRequestData= Utils.replaceNewLine(AES_BC.getInstance().encryptEncode(jsonRequestDataObj.toString(), Constants.CLIENT_REQUEST_ENCRYPTION_KEY));
        } catch (Exception e) {
        }
        return strRequestData;
    }


    private static String getEncryptedHeader()
    {
        String strHeader = "";
        JSONObject header = new JSONObject();
        try {
            header.put("AuthKey", Constants.AUTHKEY);
            header.put("ClientId", Constants.CLIENTID);
            strHeader = Utils.replaceNewLine(AES_BC.getInstance().encryptEncode(header.toString(), Constants.CLIENT_HEADER_ENCRYPTION_KEY));
        } catch (JSONException ex)
        {
            ex.printStackTrace();
        }
        return strHeader;
    }


/*    public String getServiceID() {
        String clientRefID = "";
        if (cbMicroATM.isChecked()) {
            if (rbLts.isChecked())
                clientRefID = Constants.SERVICE_MICRO_TS;
            if (rbCashWithdrawal.isChecked())
                clientRefID = Constants.SERVICE_MICRO_CW;
            if (rbBalanceEnquiry.isChecked())
                clientRefID = Constants.SERVICE_MICRO_BE;
        } else if (cbAEPS.isChecked()) {
            if (rbLts.isChecked())
                clientRefID = Constants.SERVICE_AEPS_TS;
            if (rbCashWithdrawal.isChecked())
                clientRefID = Constants.SERVICE_AEPS_CW;
            if (rbBalanceEnquiry.isChecked())
                clientRefID = Constants.SERVICE_AEPS_BE;
        }
        return clientRefID;
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null & resultCode == RESULT_OK && requestCode == 1) {
            String response;
            if (data.hasExtra("ClientResponse")) {
                response = data.getStringExtra("ClientResponse");
                String strDecryptResponse = AES_BC.getInstance().decryptDecode(Utils.replaceNewLine(response), Constants.CLIENT_REQUEST_ENCRYPTION_KEY);
                Utils.showOneBtnDialog(this, getString(R.string.STR_INFO), strDecryptResponse, false);
            } else if (data.hasExtra("ErrorDtls")) {
                response = data.getStringExtra("ErrorDtls");
                if (!response.equalsIgnoreCase("")) {
                    try {
                        String[] error_dtls = response.split("\\|");
                        String errorMsg = error_dtls[0];
                        String errorDtlsMsg = error_dtls[1];
                        Utils.showOneBtnDialog(this, getString(R.string.STR_INFO), "Error Message : " + errorMsg + "\n" + " Error Details : " + errorDtlsMsg, false);
                    }
                    catch (ArrayIndexOutOfBoundsException exp)
                    { }
                }
            }
            ErrorSingletone.getFreshInstance();
        }
    }


}
