package com.jebstern.matkakorttilukija;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AlertDialog levelDialog;
    private String[] mLanguagesArray;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    static final String LOG_TAG = "MainActivity";

    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    private TextView tvTitleHistory;
    private TextView tvContentHistory;
    private TextView tvTitlePeriod;
    private TextView tvContentPeriod;
    private TextView tvTitleValue;
    private TextView tvContentValue;
    private TextView tvTitleEticket;
    private TextView tvEticketValidity;
    private TextView tvHelpText;

    private NfcAdapter mNfcAdapter;

    Tag tag;

    String mContentPeriod;
    String mContentHistory;
    String mContentValue;
    String mEticketValidity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set appropriate language
        setNewLanguage();


        mLanguagesArray = getResources().getStringArray(R.array.languages_array);
        setContentView(R.layout.activity_main);

        tvHelpText = (TextView) findViewById(R.id.tv_help_text);
        tvTitleHistory = (TextView) findViewById(R.id.tv_title_history);
        tvTitlePeriod = (TextView) findViewById(R.id.tv_title_period);
        tvTitleValue = (TextView) findViewById(R.id.tv_title_value);
        tvTitleEticket = (TextView) findViewById(R.id.tv_title_eticket);
        tvContentPeriod = (TextView) findViewById(R.id.tv_content_period);
        tvContentHistory = (TextView) findViewById(R.id.tv_content_history);
        tvContentValue = (TextView) findViewById(R.id.tv_content_value);
        tvEticketValidity = (TextView) findViewById(R.id.tv_eticket_validity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // NFC stuff below
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (mNfcAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }
        if (!mNfcAdapter.isEnabled()) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(getApplicationContext().getString(R.string.error_dialog_nfc_title));
            dialog.setMessage(getApplicationContext().getString(R.string.error_dialog_nfc_message));
            dialog.setPositiveButton(getApplicationContext().getString(R.string.error_dialog_nfc_btn_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(intent);
                    }
                }
            });
            dialog.setNegativeButton(getApplicationContext().getString(R.string.error_dialog_nfc_btn_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }

        // For foreground intentions, e.g if the user reads the HSL card again while the app is active --> Dont start new activity, refresh current
        handleIntent(getIntent());


        // Foreground Dispatch System: allows this activity to intercept an intent and claim priority over other activities that handle the same intent.
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        String mimeType = "text/plain";
        try {
            ndef.addDataType(mimeType);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("ndef.addDataType(" + mimeType + ") failed", e);
        }
        IntentFilter td = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mFilters = new IntentFilter[]{ndef, td};

        mTechLists = new String[][]{new String[]{
                NfcA.class.getName(),
                IsoDep.class.getName()

        }};
    }


    private void handleIntent(Intent intent) {
        boolean languageChanged = intent.getBooleanExtra("languageChanged", false);
        if (!languageChanged) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
                if (intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) == null) {
                    ReadHSLCard asyncReader = new ReadHSLCard(IsoDep.get(tag));
                    asyncReader.execute();
                } else {
                    tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    ReadHSLCard asyncReader = new ReadHSLCard(IsoDep.get(tag));
                    asyncReader.execute();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_settings:
                showLanguageDialog();
                return true;
            case R.id.ab_help:
                openAlert(getApplicationContext().getString(R.string.dialog_help_title), getApplicationContext().getString(R.string.dialog_help_msg));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    public void openAlert(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getApplicationContext().getString(R.string.dialog_language_title));
        builder.setSingleChoiceItems(mLanguagesArray, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        setLocale("en");
                        break;
                    case 1:
                        setLocale("sv");
                        break;
                    default:
                        setLocale("fi");
                        break;
                }
                levelDialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.show();
    }


    public void setLocale(String lang) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("language", lang);
        editor.apply();
        setNewLanguage();  // refresh UI with new language
        Intent intent = getIntent();  // Restart activity with new language
        intent.putExtra("languageChanged", true);
        finish();
        startActivity(intent);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setNewLanguage();
    }

    public void setNewLanguage() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString("language", null);
        Locale locale;
        if (language != null) {
            locale = new Locale(language);
        } else {
            locale = new Locale("fi");
        }
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }


    private class ReadHSLCard extends AsyncTask<Void, Void, Void> {

        private IsoDep ISOCard;

        ReadHSLCard(IsoDep ISOCard) {
            super();
            this.ISOCard = ISOCard;
        }

        @Override
        protected Void doInBackground(Void... params) {

            byte[] appInfo;
            byte[] periodPass;
            byte[] storedValue;
            byte[] eTicket;
            byte[] history;
            byte[] selection;
            byte[] hist1;
            byte[] hist2 = new byte[2];

            if (ISOCard != null) {
                try {
                    ISOCard.connect();
                } catch (IOException e) {
                    Log.e("doInBackground", "ISOCard connect() FAILED!!!");
                    e.printStackTrace();
                }

                try {
                    TravelCard card;
                    selection = ISOCard.transceive(Utilities.selectHslCommand);
                    if (Arrays.equals(selection, Utilities.OK)) {
                        appInfo = ISOCard.transceive(Utilities.readAppinfoCommand);
                        periodPass = ISOCard.transceive(Utilities.readPeriodpassCommand);
                        storedValue = ISOCard.transceive(Utilities.readStoredvalueCommand);
                        eTicket = ISOCard.transceive(Utilities.readETicketCommand);
                        hist1 = ISOCard.transceive(Utilities.readHistoryCommand);

                        if (Arrays.equals(Arrays.copyOfRange(hist1, hist1.length - 2, hist1.length), Utilities.MORE_DATA)) {
                            hist2 = ISOCard.transceive(Utilities.readNextCommand);
                        }

                        history = new byte[hist1.length - 2 + hist2.length - 2];
                        System.arraycopy(hist1, 0, history, 0, hist1.length - 2);
                        System.arraycopy(hist2, 0, history, hist1.length - 2, hist2.length - 2);

                        card = new TravelCard(appInfo, periodPass, storedValue, eTicket, history);
                        Helpperi helpperi = new Helpperi(card, card.getValueTicket(), getApplicationContext());
                        helpperi.setup();


                        long daysleft1 = helpperi.getDaysLeftValid1();
                        long daysleft2 = helpperi.getDaysLeftValid2();

                        if (daysleft1 > 0) {
                            mContentPeriod = helpperi.getPeriod1Status() + daysleft1 + getApplicationContext().getResources().getString(R.string.daysLeft) + helpperi.getPeriod1Zone() + "\n" + helpperi.getPeriod1Date();
                        } else {
                            if (daysleft2 > 0) {
                                mContentPeriod = helpperi.getPeriod2Status() + daysleft2 + getApplicationContext().getResources().getString(R.string.daysLeft) + helpperi.getPeriod2Zone() + "\n" + helpperi.getPeriod2Date();
                            } else if (daysleft2 > daysleft1) {
                                mContentPeriod = helpperi.getPeriod2Status() + "\n" + helpperi.getPeriod2Zone() + "\n" + helpperi.getPeriod2Date();
                            } else {
                                mContentPeriod = helpperi.getPeriod1Status() + "\n" + helpperi.getPeriod1Zone() + "\n" + helpperi.getPeriod1Date();
                            }
                        }


                        mContentHistory = helpperi.getHistory();
                        mContentValue = getApplicationContext().getResources().getString(R.string.ticketWorth) + helpperi.getCardValue();
                        mEticketValidity = helpperi.geteTicketValidity();


                    }

                } catch (IOException e) {
                    Log.e("doInBackground", "IOException e");
                }


                try {
                    ISOCard.close();
                } catch (IOException e) {
                    Log.e("doInBackground", "ISOCard.close(), IOException e");
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            tvHelpText.setVisibility(View.GONE);
            tvTitlePeriod.setVisibility(View.VISIBLE);
            tvTitleValue.setVisibility(View.VISIBLE);
            tvTitleEticket.setVisibility(View.VISIBLE);
            tvTitleHistory.setVisibility(View.VISIBLE);
            tvContentPeriod.setText(mContentPeriod);
            tvContentHistory.setText(mContentHistory);
            tvContentValue.setText(mContentValue);
            tvEticketValidity.setText(mEticketValidity);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    public void onNewIntent(Intent intent) {
        Log.e("onNewIntent", "new intent");
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            if (intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) == null) {
                ReadHSLCard asyncReader = new ReadHSLCard(IsoDep.get(tag));
                asyncReader.execute();
            } else {
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                ReadHSLCard asyncReader = new ReadHSLCard(IsoDep.get(tag));
                asyncReader.execute();
            }
        }
    }


}
