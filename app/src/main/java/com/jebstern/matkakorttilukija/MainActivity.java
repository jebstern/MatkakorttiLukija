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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AlertDialog levelDialog;
    private Locale myLocale;
    private String[] langs;
    public static final String MY_PREFS_NAME = "MyPrefsFile";


    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    // MATKAKORTTI VARIABLES
    private TextView tv_content_history, tv_content_kausi, tv_content_arvo, tv_eticket_validity;
    private NfcAdapter mNfcAdapter;
    private static byte[] selectHslCommand = {(byte) 0x90, (byte) 0x5A, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x11, (byte) 0x20, (byte) 0xEF, (byte) 0x00};
    private static byte[] readAppinfoCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static byte[] readPeriodpassCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static byte[] readStoredvalueCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static byte[] readETicketCommand = {(byte) 0x90, (byte) 0xBD, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1A, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static byte[] readHistoryCommand = {(byte) 0x90, (byte) 0xBB, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static byte[] readNextCommand = {(byte) 0x90, (byte) 0xAF, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static byte[] OK = {(byte) 0x91, (byte) 0x00};
    private static byte[] MORE_DATA = {(byte) 0x91, (byte) 0xAF};

    Tag tag;
    String content_kausi, content_history, content_arvo, eticket_validity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set appropriate language
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString("language", null);
        if (language != null) {
            myLocale = new Locale(language);
        } else {
            myLocale = new Locale("fi");  // If firtst time use. default to Finnish
        }

        // Snippet to display correct language
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        langs = getResources().getStringArray(R.array.languages_array);
        setContentView(R.layout.activity_main);

        tv_content_kausi = (TextView) findViewById(R.id.tv_content_kausi);
        tv_content_history = (TextView) findViewById(R.id.tv_content_history);
        tv_content_arvo = (TextView) findViewById(R.id.tv_content_arvo);
        tv_eticket_validity = (TextView) findViewById(R.id.tv_eticket_validity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // NFC stuff below
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (mNfcAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }
        if (!mNfcAdapter.isEnabled()) {

            AlertDialog.Builder alertbox = new AlertDialog.Builder(MainActivity.this);
            alertbox.setTitle(getApplicationContext().getString(R.string.error_dialog_nfc_title));
            alertbox.setMessage(getApplicationContext().getString(R.string.error_dialog_nfc_message));
            alertbox.setPositiveButton(getApplicationContext().getString(R.string.error_dialog_nfc_btn_yes), new DialogInterface.OnClickListener() {
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
            alertbox.setNegativeButton(getApplicationContext().getString(R.string.error_dialog_nfc_btn_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertbox.show();
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
        builder.setSingleChoiceItems(langs, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case 0:
                        setLocale("en");
                        break;
                    case 1:
                        setLocale("sv");
                        break;
                    case 2:
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
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString("language", null);
        if (language != null) {
            myLocale = new Locale(language);
        } else {
            myLocale = new Locale("fi");

        }
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }


    private class ReadHSLCard extends AsyncTask<Void, Void, Void> {

        private IsoDep ISOCard;

        public ReadHSLCard(IsoDep ISOCard) {
            super();
            this.ISOCard = ISOCard;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            byte[] appInfo, periodPass, storedValue, eTicket, history, selection;
            byte[] hist1, hist2 = new byte[2];

            if (ISOCard != null) {
                try {
                    ISOCard.connect();
                } catch (IOException e) {
                    Log.e("doInBackground", "ISOCard connect() FAILED!!!");
                    e.printStackTrace();
                }

                try {
                    TravelCard card;
                    selection = ISOCard.transceive(selectHslCommand);
                    if (Arrays.equals(selection, OK)) {
                        appInfo = ISOCard.transceive(readAppinfoCommand);
                        periodPass = ISOCard.transceive(readPeriodpassCommand);
                        storedValue = ISOCard.transceive(readStoredvalueCommand);
                        eTicket = ISOCard.transceive(readETicketCommand);
                        hist1 = ISOCard.transceive(readHistoryCommand);

                        if (Arrays.equals(Arrays.copyOfRange(hist1, hist1.length - 2, hist1.length), MORE_DATA)) {
                            hist2 = ISOCard.transceive(readNextCommand);
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
                            content_kausi = helpperi.getPeriod1Status() + daysleft1 + getApplicationContext().getResources().getString(R.string.daysLeft) + helpperi.getPeriod1Zone() + "\n" + helpperi.getPeriod1Date();
                        } else {
                            if (daysleft2 > 0) {
                                content_kausi = helpperi.getPeriod2Status() + daysleft2 + getApplicationContext().getResources().getString(R.string.daysLeft) + helpperi.getPeriod2Zone() + "\n" + helpperi.getPeriod2Date();
                            } else if (daysleft2 > daysleft1) {
                                content_kausi = helpperi.getPeriod2Status() + "\n" + helpperi.getPeriod2Zone() + "\n" + helpperi.getPeriod2Date();
                            } else {
                                content_kausi = helpperi.getPeriod1Status() + "\n" + helpperi.getPeriod1Zone() + "\n" + helpperi.getPeriod1Date();
                            }
                        }


                        content_history = helpperi.getHistory();
                        content_arvo = getApplicationContext().getResources().getString(R.string.ticketWorth) + helpperi.getCardValue();
                        eticket_validity = helpperi.getETicketValidity();


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    ISOCard.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            tv_content_kausi.setText(content_kausi);
            tv_content_history.setText(content_history);
            tv_content_arvo.setText(content_arvo);
            tv_eticket_validity.setText(eticket_validity);
        }
    }


    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    public void onNewIntent(Intent intent) {
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
