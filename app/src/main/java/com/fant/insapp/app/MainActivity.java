package com.fant.insapp.app;


import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
/*
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
*/




public class MainActivity extends FragmentActivity {


	///////////////////////////////////////////////////////////////////////////
	//                          DROPBOX.                      				 //
	///////////////////////////////////////////////////////////////////////////
	// Replace this with your app key and secret assigned by Dropbox.
	// Note that this is a really insecure way to do this, and you shouldn't
	// ship code which contains your key & secret in such an obvious way.
	// Obfuscation is good.
	final static private String APP_KEY = "f7s1t7zzvannleh";
	final static private String APP_SECRET = "imxtgys6mew61n2";
	///////////////////////////////////////////////////////////////////////////
	//                      End app-specific settings.                       //
	///////////////////////////////////////////////////////////////////////////
	// You don't need to change these, leave them alone.
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	private static final boolean USE_OAUTH1 = false;



	///////////////////////////////////////////////////////////////////////////
	//                                 End DROPBOX                           //
	///////////////////////////////////////////////////////////////////////////

	private boolean mDropboxLoggedIn;
	java.io.File retFileDropbox;







	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;

	static final int UPLOAD_GDRIVE = 1;	
	static int actAfterAccountPicker;

	static final String SPREADSHEET_INS_TEMP_NAME = "INS_temp";



	com.google.api.services.drive.model.File fileOnGoogleDrive = null;



	private static Drive service;
	private GoogleAccountCredential credential;	  

	private String valData, valTipoOper, valChiFa, valADa, valPersonale, valValore, valCategoria, valDescrizione, valNote;
	private boolean fileAccessOK;

	Menu myMainMenu;

	public static String fileName, fileNameFull;
	Spinner spinCategoria;

	AutoCompleteTextView textCategoria;
	ArrayAdapter<String> adapterCat;
	ArrayAdapter<String> adapterCatTxt;
	Spinner spinADa;

	AutoCompleteTextView textADa;
	ArrayAdapter<CharSequence> adapterADa;
	ArrayAdapter<CharSequence> adapterADaTxt;

	private ProgressDialog progDia = null;
	TextView textTitle;

    private GestureDetector gestureDetector;

	// *************************************************************************
	// OnCreate
	// *************************************************************************
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


        gestureDetector = new GestureDetector(
                new SwipeGestureDetector());

		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
		myGlobal.mApiDropbox = new DropboxAPI<AndroidAuthSession>(session);

		checkDropboxAppKeySetup();


		// prepara file
		fileAccessOK = prepFileisOK();
		if (!fileAccessOK) 
			showToast("Errore creazione file: " + fileNameFull);

		if (!myGlobal.prepDBfilesisOK(this,false,false,false))
			showToast("Errore nel check file Database");

		// Display the proper UI state if logged in or not
		setDropboxLoggedIn(myGlobal.mApiDropbox.getSession().isLinked());

        Spinner spinner;
		ArrayAdapter<CharSequence> adapter;

		textTitle = (TextView) findViewById(R.id.textViewTitle);
		textTitle.setText(R.string.title_activity_InsertData);
		textTitle.setTextColor(getResources().getColor(R.color.TitleYellow));

		EditText editTextData = (EditText) findViewById(R.id.TextData);
		editTextData.setOnTouchListener(new ClickDataButton());

		if (myGlobal.statoDBLocal) {
			// Se Database tutto a posto inizializzo array con valori 
			myGlobal.DBINSlocal.open();
            myGlobal.DBINSLocalFull.open();
			List<String> mylistr; 
			mylistr = myGlobal.DBINSLocalFull.fetchListValori(MyDatabase.DataINStable.TABELLA_TIPOOPERAZIONE);
			myGlobal.arrTipoOperazione = mylistr.toArray(new String[0]);
			mylistr = myGlobal.DBINSLocalFull.fetchListValori(MyDatabase.DataINStable.TABELLA_CHIFA);
			myGlobal.arrChiFa = mylistr.toArray(new String[0]);
			mylistr = myGlobal.DBINSLocalFull.fetchListValori(MyDatabase.DataINStable.TABELLA_CPERSONALI);
			myGlobal.arrCPersonale = mylistr.toArray(new String[0]);
			mylistr = myGlobal.DBINSLocalFull.fetchListValori(MyDatabase.DataINStable.TABELLA_CATEGORIE);
			myGlobal.arrCategoria = mylistr.toArray(new String[0]);
			mylistr = myGlobal.DBINSLocalFull.fetchListValori(MyDatabase.DataINStable.TABELLA_ADA);
			myGlobal.arrADa = mylistr.toArray(new String[0]);
            mylistr = myGlobal.DBINSLocalFull.fetchListValoriFromQuery("SELECT Generica FROM " + MyDatabase.DataINStable.TABELLA_CATEGORIE + " GROUP BY Generica ORDER BY Generica");
            myGlobal.arrGenerica = mylistr.toArray(new String[0]);

			myGlobal.DBINSlocal.close();
            myGlobal.DBINSLocalFull.close();
		} else {
			// in mancanza del DB metto tutti List vuoti
			List<String> mylistr =  new ArrayList<String>();
			mylistr.add("");        	
			myGlobal.arrTipoOperazione = mylistr.toArray(new String[0]);        	
			myGlobal.arrChiFa = mylistr.toArray(new String[0]);        	
			myGlobal.arrCPersonale = mylistr.toArray(new String[0]);        	
			myGlobal.arrCategoria = mylistr.toArray(new String[0]);        	
			myGlobal.arrADa = mylistr.toArray(new String[0]);
            myGlobal.arrGenerica = mylistr.toArray(new String[0]);
        }


		spinner = (Spinner) findViewById(R.id.SpinnerTipoOper);
		adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, myGlobal.arrTipoOperazione );
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		// Specify the layout to use when the list of choices appears
		spinner.setAdapter(adapter);	// Apply the adapter to the spinner

		spinner = (Spinner) findViewById(R.id.SpinnerChiFa);
		adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, myGlobal.arrChiFa );
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner = (Spinner) findViewById(R.id.SpinnerPersonale);
		adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, myGlobal.arrCPersonale );
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);


		// Categoria 
		spinCategoria  = (Spinner) findViewById(R.id.SpinnerCategoria);
		adapterCat = new ArrayAdapter<String>(MainActivity.this,   android.R.layout.simple_spinner_item, myGlobal.arrCategoria);		

		adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinCategoria.setAdapter(adapterCat);
		spinCategoria.setOnItemSelectedListener(new SelectSpinAutocomplete());

		adapterCatTxt = new ArrayAdapter<String>(MainActivity.this,   android.R.layout.simple_expandable_list_item_1, myGlobal.arrCategoria);
		textCategoria = (AutoCompleteTextView) findViewById(R.id.TextAutocompleteCategoria);
		textCategoria.setAdapter(adapterCatTxt);
		//textCategoria.setCompletionHint("Selezionare o scrivere categoria");
		textCategoria.setOnFocusChangeListener(new ChangeFocusAutoComplete());
		textCategoria.setValidator(new ValidateCategoria());


		// A/Da
		spinADa = (Spinner) findViewById(R.id.SpinnerADa);

		adapterADa =  new ArrayAdapter<CharSequence>   (this, android.R.layout.simple_spinner_item, myGlobal.arrADa );
		adapterADa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinADa.setAdapter(adapterADa);
		spinADa.setOnItemSelectedListener(new SelectSpinAutocomplete());

		textADa = (AutoCompleteTextView) findViewById(R.id.TextAutocompleteADa);        	
		adapterADaTxt = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, myGlobal.arrADa);
		textADa.setAdapter(adapterADaTxt);
		textADa.setOnFocusChangeListener(new ChangeFocusAutoComplete());
		textADa.setValidator(new ValidateADa());



		final ImageButton buttonOK = (ImageButton) findViewById(R.id.imgbtnOK);		
		buttonOK.setOnClickListener(new ClickOKButton());

		final ImageButton buttonReset = (ImageButton)  findViewById(R.id.imgbtnReset);
		buttonReset.setOnClickListener(new ClickResetButton());

        final Button myButtonProve = (Button) findViewById(R.id.btnProve);
        myButtonProve.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Prova

                new Thread(new Runnable() {
                    //Thread to stop network calls on the UI thread
                    public void run() {
                        //Request the HTML
                        // JSON Node names
                        final String TAG_SUCCESS = "success";
                        final String TAG_MESSAGE = "message";
                        final String TAG_SQL = "sql";
                        final String TAG_NUMROWS = "numrows";
                        final String TAG_RESULT_ARRAY = "res";
                        final String TAG_PID = "_id";
                        final String TAG_VALORE = "Valore";

/*
                        webJSONParser jParser = new webJSONParser();
                        // datiweb JSONArray
                        JSONArray datiweb = null;

                        String url_all_datiweb = "http://simonefantuzzi.altervista.org/phpDB/get_querysql.php/";
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("_querysql", "SELECT * FROM myINSData"));

                        // getting JSON string from URL
                        JSONObject json = jParser.makeHttpRequest(url_all_datiweb, "GET", params);

                        try {

                            if (json.getInt(TAG_SUCCESS) == 1) {
                                // datiweb found
                                // Getting Array of datiweb
                                datiweb = json.getJSONArray(TAG_RESULT_ARRAY);

                                // I'm assuming that the JSONArray will contain only JSONObjects with the same propertties
                                MatrixCursor mc = new MatrixCursor(new String[] {MyDatabase.DataINStable.ID, MyDatabase.DataINStable.DATA_OPERAZIONE_KEY, MyDatabase.DataINStable.CHI_FA_KEY, MyDatabase.DataINStable.A_DA_KEY, MyDatabase.DataINStable.C_PERS_KEY, MyDatabase.DataINStable.VALORE_KEY, MyDatabase.DataINStable.CATEGORIA_KEY, MyDatabase.DataINStable.GENERICA_KEY, MyDatabase.DataINStable.DESCRIZIONE_KEY, MyDatabase.DataINStable.NOTE_KEY, MyDatabase.DataINStable.SPECIAL_NOTE_KEY});
                                for (int i = 0; i < datiweb.length(); i++) {
                                    JSONObject jo = datiweb.getJSONObject(i);
                                    // extract the properties from the JSONObject and use it with the addRow() method below
                                    mc.addRow(new Object[] {MyDatabase.DataINStable.ID, MyDatabase.DataINStable.DATA_OPERAZIONE_KEY, MyDatabase.DataINStable.CHI_FA_KEY, MyDatabase.DataINStable.A_DA_KEY, MyDatabase.DataINStable.C_PERS_KEY, MyDatabase.DataINStable.VALORE_KEY, MyDatabase.DataINStable.CATEGORIA_KEY, MyDatabase.DataINStable.GENERICA_KEY, MyDatabase.DataINStable.DESCRIZIONE_KEY, MyDatabase.DataINStable.NOTE_KEY, MyDatabase.DataINStable.SPECIAL_NOTE_KEY});
                                    Log.e("WEB-FANT", mc.getString(mc.getColumnIndex(MyDatabase.DataINStable.DATA_OPERAZIONE_KEY)));
                                }

                                // looping through All datiweb
                                for (int i = 0; i < datiweb.length(); i++) {
                                    JSONObject c = datiweb.getJSONObject(i);

                                    // Storing each json item in variable
                                    String id = c.getString(TAG_PID);
                                    String val = c.getString(TAG_VALORE);



                                    // adding HashList to ArrayList
                                    //datiwebList.add(map);
                                }
                            } else {
                                // no datiweb found
                                // Launch Add New product Activity
                                //
                                //Intent i = new Intent(getApplicationContext(),
                                //        NewProductActivity.class);
                                //// Closing all previous activities
                                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                //startActivity(i);

                                int i = 0;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        */
/*
                            HttpParams httpParameters = new BasicHttpParams();
                            HttpConnectionParams.setConnectionTimeout(httpParameters, 1000);
                            HttpConnectionParams.setSoTimeout(httpParameters, 1000);

                            HttpClient client = new DefaultHttpClient(httpParameters);
                            HttpGet request = new HttpGet("http://simonefantuzzi.altervista.org/provasqlite.php");
                            HttpResponse response = client.execute(request);

                            //Do something with the response
                            // Get the response
                            BufferedReader rd = new BufferedReader
                                    (new InputStreamReader(response.getEntity().getContent()));

                            String line = "";
                            while ((line = rd.readLine()) != null) {
                                Log.e("Result",line);
                            }
                            */
                    }
                }).start();

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://www.google.it");
                HttpGet request = new HttpGet("http://http://simonefantuzzi.altervista.org/provasqlite.php");

            }
        });


		initTextValue();



	}



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void onLeftSwipe() {
        showToast("Left swipe");
    }

    private void onRightSwipe() {
        // Do something
        showToast("Right swipe");
    }

	// *************************************************************************
	// Gestione Activity result (chiamata Activity con result per scelta credenziali Google)
	// *************************************************************************
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:    	
			if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

				if (accountName != null) {
					credential.setSelectedAccountName(accountName);

					service = getDriveService(credential);

					if (actAfterAccountPicker == UPLOAD_GDRIVE){
						this.progDia = ProgressDialog.show(this, "INS..", "Uploading Data...", true);
						new uploadFileToGDrive().execute("");            	
					} 
				}
			}
			break;

		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				if (actAfterAccountPicker == UPLOAD_GDRIVE){
					this.progDia = ProgressDialog.show(this, "INS..", "Uploading Data...", true);
					new uploadFileToGDrive().execute("");            	
				} 
			} else {
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);          
			}
			break;

		default:
			break;

		}
	}    


	// *************************************************************************
	// Carico fisicamente il file su Google Drive. Parametri: Directory, NomeFile, Metadata
	// *************************************************************************
	private boolean uploadSingleFile(String _pathName, String _fileName, String _metaData) {

		Uri fileUriDataBase;

		fileUriDataBase = Uri.fromFile(new java.io.File(_pathName + java.io.File.separator + _fileName));
		// File's binary content
		java.io.File fileContent = new java.io.File(fileUriDataBase.getPath());
		FileContent mediaContent = new FileContent(_metaData, fileContent);

		// File's metadata.
		com.google.api.services.drive.model.File body;
		body = new com.google.api.services.drive.model.File();
		body.setTitle(fileContent.getName());
		body.setMimeType(_metaData);

		try {

			//file = service.files().insert(body, mediaContent).execute();
			Drive.Files.Insert insert = service.files().insert(body, mediaContent);
			MediaHttpUploader uploader = insert.getMediaHttpUploader();
			uploader.setDirectUploadEnabled(true);
			fileOnGoogleDrive = insert.execute();
		} catch (UserRecoverableAuthIOException e) {
			startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
			showToast("Error UserRecoverableAuthIOException: " );
			return (false);
		} catch (IOException e) {
			e.printStackTrace();
			showToast("Error IOException: " + e.getMessage());
			return (false);
		}

		return (true);

	}


	// *************************************************************************
	// Upload file di testo in google drive, Task asincrono
	// *************************************************************************
	private class uploadFileToGDrive  extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			if (uploadSingleFile(myGlobal.getStorageFantDir().getPath() , fileName, "text/plain"))
				showToast("Uploaded: " + fileName);

			if (uploadSingleFile(myGlobal.getStorageDatabaseFantDir().getPath() , myGlobal.LOCAL_DB_FILENAME, "application/octet-stream"))
				showToast("Uploaded: " + myGlobal.LOCAL_DB_FILENAME);

			return "Executed";
		}      


		@Override
		protected void onPostExecute(String result) {
			if (MainActivity.this.progDia != null) {
				MainActivity.this.progDia.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {        	
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}        

	}




    // Private class for gestures
    private class SwipeGestureDetector
            extends GestureDetector.SimpleOnGestureListener {
        // Swipe properties, you can change it to make the swipe
        // longer or shorter and speed
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 200;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            try {
                float diffAbs = Math.abs(e1.getY() - e2.getY());
                float diff = e1.getX() - e2.getX();

                if (diffAbs > SWIPE_MAX_OFF_PATH)
                    return false;

                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    MainActivity.this.onLeftSwipe();

                    // Right swipe
                } else if (-diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    MainActivity.this.onRightSwipe();
                }
            } catch (Exception e) {
                Log.e("YourActivity", "Error on gestures");
            }
            return false;
        }
    }


	// *************************************************************************
	// Upload file di testo in google drive, Task asincrono
	// *************************************************************************
 /*
	private class updateINS2  extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			SpreadsheetService myService = new SpreadsheetService("MySpreadsheetIntegration-v1");
			myService.setProtocolVersion(SpreadsheetService.Versions.V3);
			try {
				// TODO. Selezionare account diverso
				myService.setUserCredentials("fantuz76@gmail.com", "Ramarro1");


				// Define the URL to request.  This should never change.
				URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");

				// Make a request to the API and get all spreadsheets.
				SpreadsheetFeed feed = myService.getFeed(SPREADSHEET_FEED_URL,SpreadsheetFeed.class);
				List<SpreadsheetEntry> spreadsheetList = feed.getEntries();

				if (spreadsheetList.size() == 0) {
					showToast("No sheets found");
				} else {

					// cerco posizione di SPREADSHEET_INS_TEMP_NAME
					int i, pos_sheet=0;
					for (i=0; i<spreadsheetList.size(); i++) {
						//showToast(spreadsheetList.get(i).getTitle().toString());
						//showToast(spreadsheetList.get(i).getPlainTextContent());
						if (spreadsheetList.get(i).getTitle().getPlainText().equals(SPREADSHEET_INS_TEMP_NAME)) {	            	    		
							pos_sheet = i;
						}
					}	            	    
					SpreadsheetEntry spreadsheet = spreadsheetList.get(pos_sheet);	            	    
					if (spreadsheet.getTitle().getPlainText().equals("INS_temp")) {

						showToast("Inizio scrittura spreadsheet: " + spreadsheet.getTitle().getPlainText());
						//spreadsheet.getTitle().getId()	// questa ritorna il link all'xml con tutte le informazioni

						// Get the first worksheet of the first spreadsheet.
						WorksheetFeed worksheetFeed = myService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
						List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
						WorksheetEntry worksheet = worksheets.get(0);


						int row = 0;
						int col = 0;

						// Fetch the cell feed of the worksheet.
						URL cellFeedUrl = worksheet.getCellFeedUrl();
						CellFeed cellFeed = myService.getFeed(cellFeedUrl, CellFeed.class);

						// Guardo ultima cella libera
						for (CellEntry cell : cellFeed.getEntries()) {
							row = cell.getCell().getRow();
							col = cell.getCell().getCol();
						}
						if (row < 11) row = 11;		// minimo riga 11


						// Se file txt OK carico tutto nel fiel excel 
						if (fileAccessOK) {		            	    	

							CellEntry newCell;
							String ValToWrite = "";
							int rowCnt;
							int colCnt;
							FileReader fr = new FileReader(fileNameFull);
							int intread = 0;
							char chread = 0;

							rowCnt = row+1;
							colCnt = 1;

							// leggo file con campi separati da tab
							while (intread != -1) {		            	    	
								intread = fr.read();
								chread = (char) intread;
								if (chread == -1) {
									// fine file
								} else if (chread == '\n') {
									// a capo, nuova riga
									if (colCnt == 1) {			            	    			
										newCell = new CellEntry(rowCnt, colCnt, myGlobal.formattedDate());
										cellFeed.insert(newCell);
										colCnt++;
									}
									newCell = new CellEntry(rowCnt, colCnt, ValToWrite);
									cellFeed.insert(newCell);		            	    		
									ValToWrite = "";
									rowCnt++;
									colCnt = 1;
								} else if (chread == '\t') {
									// nuovo valore
									if (colCnt == 1) {			            	    			
										newCell = new CellEntry(rowCnt, colCnt, myGlobal.formattedDate());
										cellFeed.insert(newCell);
										colCnt++;
									}
									newCell = new CellEntry(rowCnt, colCnt, ValToWrite);
									cellFeed.insert(newCell);		            	    		
									ValToWrite = "";
									colCnt++;
								} else {
									// carattere normale, concatena
									ValToWrite += chread; 
								}
							}
							fr.close();
							showToast("Scrittura spreadsheet completata");

							// adesso, una volta caricato lo rinomino così resta nella SD del telefono come backup
							java.io.File oldFile = new java.io.File(fileNameFull);
							//Now invoke the renameTo() method on the reference, oldFile in this case
							oldFile.renameTo(new java.io.File(fileNameFull.replace(".txt", "_" + myGlobal.formattedDate() + ".txt")));		            	    	
							showToast("rinominato file txt: " + fileNameFull.replace(".txt", "_" + myGlobal.formattedDate() + ".txt"));

							// prepara file
							fileAccessOK = prepFileisOK();
						}


					} else {
						showToast("Non trovato spreadsheet " + SPREADSHEET_INS_TEMP_NAME);
					}
				}
			} catch (Exception e) {
				showToast("Exception " + e.getMessage());
			}
			return "Executed";
		}


		@Override
		protected void onPostExecute(String result) {

			if (MainActivity.this.progDia != null) {
				MainActivity.this.progDia.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {        	
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}        

	}
    */



	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName(
				"Google-DriveSample/1.0")
				.build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.myMainMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;

		switch(item.getItemId())
		{

            case R.id.action_readfileDBlocal:
                if (myGlobal.statoDBLocal == false) {
                    showToast("Errore presenza file DB locale! Impossibile procedere.");
                } else {
                    myGlobal.ReadTxtActivityLoaded = false;
                    intent = new Intent(this, ReadTxtActivity.class);
                    // passo delle informazioni all'Activity
                    intent.putExtra("readDBtype","local");
                    startActivity(intent);
                }
                return true;



            case R.id.action_readfileDBfull:
                if (myGlobal.statoDBLocalFull == false) {
                    showToast("Errore presenza file DB locale! Impossibile procedere.");
                } else {
                    myGlobal.ReadTxtActivityLoaded = false;
                    intent = new Intent(this, ReadTxtActivity.class);
                    // passo delle informazioni all'Activity
                    intent.putExtra("readDBtype","full");
                    startActivity(intent);
                }
                return true;


            case R.id.action_sync_db:

                AlertDialog.Builder buildersync = new AlertDialog.Builder(MainActivity.this);
                buildersync
                        .setTitle("Sincronizzazione nuovi dati locali con il DB cloud")
                        .setMessage("Sicuro di sincronizzare? " + System.getProperty("line.separator") +
                                "(possibile perdita di eventuali modifiche al file completo locale)")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if ((myGlobal.statoDBLocal == false) || (myGlobal.statoDBLocalFull == false)) {
                                    showToast("Errore presenza file DB locale! Impossibile procedere.");
                                } else {
                                    // Prima di sincronizzare chiedo se voglio fare la copia in locale

                                    AlertDialog.Builder buildersync = new AlertDialog.Builder(MainActivity.this);
                                    buildersync
                                            .setTitle("Backup file")
                                            .setMessage("Creare una copia nel telefono dei file prima di sincronizzare?" + System.getProperty("line.separator") +
                                                    "(consigliato anche se occupa memoria)")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        // backup DB Local
                                                        java.io.File oldFile = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_DB_FILENAME);
                                                        java.io.File newFile = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_DB_FILENAME + "."  + myGlobal.formattedDate() + ".bkup");

                                                        // backup DB full Local
                                                        java.io.File oldFileFull = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE);
                                                        java.io.File newFileFull = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE + "."  + myGlobal.formattedDate() + ".bkup");

                                                        myGlobal.copyFiles(oldFile, newFile);
                                                        myGlobal.copyFiles(oldFileFull, newFileFull);
                                                        //myGlobal.copyFiles2(oldFile, newFile);
                                                        //Files.copy(oldFile, newFile);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                        showToast("Error IOException: " + e.getMessage());
                                                    }

                                                    // Solo addesso faccio iniziare la sincronizzazione
                                                    //Intent intent = new Intent(MainActivity.this, SyncDBActivity.class);
                                                    //startActivity(intent);
                                                    SyncAllDBData sync = new SyncAllDBData(MainActivity.this);
                                                    sync.execute();
                                                }
                                            })
                                            .setNegativeButton("No",  new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // Solo addesso faccio iniziare la sincronizzazione
                                                    //Intent intent = new Intent(MainActivity.this, SyncDBActivity.class);
                                                    //startActivity(intent);
                                                    SyncAllDBData sync = new SyncAllDBData(MainActivity.this);
                                                    sync.execute();

                                                }
                                            })
                                            .show();

                                    // l'inizio della intent SyncDBActivity non posso farlo qua altrimenti partirebbe senza aver atteso la risposta
                                    // alla domanda di backup

                                }
                            }
                        })
                        .setNegativeButton("No", null)						//Do nothing on no
                        .show();
                return true;







            case R.id.action_uploadDB:
			// adesso, una volta caricato lo rinomino così resta nella SD del telefono come backup
			java.io.File oldFile = new java.io.File(fileNameFull);
			java.io.File newFile = new java.io.File(fileNameFull.replace(".txt", "_" + myGlobal.formattedDate() + ".txt"));    	    	

			// copia
			try {
				myGlobal.copyFiles(oldFile, newFile);
				//myGlobal.copyFiles2(oldFile, newFile);   	    	
				//Files.copy(oldFile, newFile);
			} catch (IOException e) {				
				e.printStackTrace();
				showToast("Error IOException: " + e.getMessage());
			}

			// stessa cosa con il file database
			// adesso, una volta caricato lo rinomino così resta nella SD del telefono come backup
			java.io.File oldFileDB = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE);
			java.io.File newFileDB = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE.replace(".sqlite", "_" + myGlobal.formattedDate() + ".sqlite"));
			java.io.File newFileDB2 = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.REMOTE_DB_FILENAME);

			// copia
			try {
				myGlobal.copyFiles(oldFileDB, newFileDB);
				myGlobal.copyFiles(oldFileDB, newFileDB2);
				//myGlobal.copyFiles2(oldFileDB, newFileDB);   	    	
				//Files.copy(oldFileDB, newFileDB);
			} catch (IOException e) {
				e.printStackTrace();
				showToast("Error IOException: " + e.getMessage());
			}

			UploadToDropbox upload = new UploadToDropbox(this, myGlobal.mApiDropbox, myGlobal.DROPBOX_INS_DIR, newFile, false, false);
			upload.execute();

			// il file REMOTE_DB_FILENAME lo cancello dalla SD dopo upload e tento anche un backup del file remoto
			UploadToDropbox uploadDB = new UploadToDropbox(this, myGlobal.mApiDropbox, myGlobal.DROPBOX_INS_DIR, newFileDB2, true, true);
			uploadDB.execute();

			// adesso Cancello il file newFile
			// non farlo qua se no noon upload niente
			//newFile.delete();
			return true;





		case R.id.action_authDropbox:        		
			if (mDropboxLoggedIn) {
				logOutDropbox();
			} else {
				// Start the remote authentication
				if (USE_OAUTH1) {
					myGlobal.mApiDropbox.getSession().startAuthentication(MainActivity.this);
				} else {
					myGlobal.mApiDropbox.getSession().startOAuth2Authentication(MainActivity.this);
				}
			}
			return true;



		case R.id.action_settings:
			//Intent intentSettings = new Intent(this, SettingsActivity.class);                
			Intent intentSettings = new Intent(this, MySettings.class);
			startActivity(intentSettings);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Funzione richiamata ogni volta che viene presentato il MENU
		MenuItem myMenuitem = myMainMenu.findItem(R.id.action_authDropbox);

		if (mDropboxLoggedIn) {
			myMenuitem.setTitle(R.string.action_authDropbox_logout);
		} else {
			myMenuitem.setTitle(R.string.action_authDropbox_login);
		}

		return super.onPrepareOptionsMenu(menu);
	}
	// *************************************************************************
	// Controllo valori inseriti
	// *************************************************************************
	public boolean checkAllValues()  {
		if (!(myGlobal.checkData(valData))) {
			showToast("Data Sbagliata");    		
			return false;
		}

		if (!myGlobal.checkValore(valValore)) {
			showToast("Valore Sbagliato");
			return false;
		} 

		if (!checkCategoria()) {
			showToast("Categoria Sbagliata");
			return false;
		}

		if (!checkADa()) {
			showToast("A/Da Sbagliato");
			return false;
		}
		return true;
	}






	public boolean checkCategoria()  {
		List<String> mylistr = Arrays.asList(myGlobal.arrCategoria);    	
		if (mylistr.contains(valCategoria)) 
			return(true);
		else
			return(false);    	
	}

	public boolean checkADa()  {
		List<String> mylistr = Arrays.asList(myGlobal.arrADa);    
		// non ammissibile
		if  ( (valTipoOper.equalsIgnoreCase("Spostamento")) && (valADa.equals("")) ) {
			return false;
		}

		// tutti valori ammessi, anche cose nuove
		if (mylistr.contains(valADa))  {
			return(true);
		} else {
			return(true);
		}

	}




	// *************************************************************************
	// Preparo file di testo, controllo consistenza 
	// *************************************************************************
	public boolean prepFileisOK() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;


		// definisco nome file usando IMEI telefono per avere file diversi da tel diversi
		TelephonyManager tMgr =(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		final String mIMEIstr;
		if (tMgr.getDeviceId() != null) {
			mIMEIstr = tMgr.getDeviceId();
		} else {
			mIMEIstr = "unknownIMEI";
		}


		// inizializzo nome directory nome file        
		fileName = "FanINS_" + mIMEIstr.trim() +".txt";        
		fileNameFull = myGlobal.getStorageFantDir().getPath() + java.io.File.separator + fileName;


		// Verifico presenza SD esterna per salvare dati
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}




		if (mExternalStorageWriteable && mExternalStorageAvailable)  {
			try  {

				// Se non esiste creo il file
				java.io.File checkFile = new java.io.File(fileNameFull);
				if (!checkFile.exists()) {
					if (!checkFile.createNewFile())
						return false;
				}    			    			
				return true;
			} catch (Exception ioe) {
				ioe.printStackTrace();
				return false;
			}

		}

		return false;

	}


	// *************************************************************************
	// Salvo dati su file di testo
	// *************************************************************************
	public void saveDataOnFile()  {
		if (!fileAccessOK) {
			showToast("Error file create: " + fileNameFull);
		} else {    	
			//Put up the Yes/No message box
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
			.setTitle("Salvare Dati")
			.setMessage("Sicuro?")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {			      	

					// Scrivo dati nel file, modalità append
					try  {
						FileWriter fw = new FileWriter(fileNameFull, true);

						// Scrivo separando i campi da TAB
						fw.append(valData + '\t');
						fw.append(valTipoOper + '\t');
						fw.append(valChiFa + '\t');
						fw.append(valADa + '\t');
						fw.append(valPersonale + '\t');
						fw.append(valValore + '\t');
						fw.append(valCategoria + '\t');
						fw.append('\t');					// Categoria Generica è vuota la calcola poi file excel
						fw.append(valDescrizione + '\t');
						fw.append(valNote + '\t');

						fw.append('\n');

						fw.flush();
						fw.close();

						// Lo metto nel DB (converto anche Valore come float)
						//Float myFloatValore = (float) 0;
						//myFloatValore = Float.parseFloat(valValore);

						if (myGlobal.statoDBLocal == false) {
							showToast("Errore presenza file DB locale! Impossibile procedere.");
						} else {
							myGlobal.DBINSlocal.open();
							myGlobal.DBINSlocal.insertRecordDataIns(valData, valTipoOper, valChiFa, valADa, valPersonale, valValore, valCategoria, valDescrizione, valNote, "");
							myGlobal.DBINSlocal.close();

							textTitle.setTextColor(getResources().getColor(R.color.TitleGreen));
							showToast("Dati Salvati");
						}
					} catch (IOException ioe) {    					
						showToast("Error IOException: " + ioe.getMessage());
						ioe.printStackTrace();
					} catch (Exception e){
						showToast("Error Exception: " + e.getMessage());
						e.printStackTrace();    					
					}

				}
			})
			.setNegativeButton("No", null)						//Do nothing on no
			.show();

		}
	}



	// *************************************************************************
	// Mostra messaggio toast 
	// *************************************************************************
	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
			}
		});
	}



	// *************************************************************************
	// classe  click button ON
	// *************************************************************************    
	class ClickOKButton implements View.OnClickListener {
		@Override
		public void onClick(View v) {
            SharedPreferences myAppSettings  = getSharedPreferences(myGlobal.PREFERENCE_APP_FILE, 0);

			// Perform action on click
			final EditText editTextData = (EditText) findViewById(R.id.TextData);
			valData = editTextData.getText().toString();

			final Spinner editTextTipoOper = (Spinner) findViewById(R.id.SpinnerTipoOper);    				
			valTipoOper = editTextTipoOper.getSelectedItem().toString();

			final Spinner editTextChiFa = (Spinner) findViewById(R.id.SpinnerChiFa);    				
			valChiFa = editTextChiFa.getSelectedItem().toString();
            int posChiFa = editTextChiFa.getSelectedItemPosition();

			//final Spinner editTextADa = (Spinner) findViewById(R.id.SpinnerADa);    				
			//valADa = editTextADa.getSelectedItem().toString();
			valADa = textADa.getText().toString();

			final Spinner editTextPersonale = (Spinner) findViewById(R.id.SpinnerPersonale);    				
			valPersonale = editTextPersonale.getSelectedItem().toString();

			final EditText editTextValore = (EditText) findViewById(R.id.TextValore);    				
			valValore = editTextValore.getText().toString();

			final Spinner editTextCategoria = (Spinner) findViewById(R.id.SpinnerCategoria);    				
			valCategoria = editTextCategoria.getSelectedItem().toString();

			final EditText editTextDescrizione = (EditText) findViewById(R.id.TextDescrizione);    				
			valDescrizione = editTextDescrizione.getText().toString();

			final EditText editTextNote = (EditText) findViewById(R.id.TextNote);    				
			valNote = editTextNote.getText().toString();

			if (checkAllValues()) {
				saveDataOnFile() ;

                //salvo alcune preferenze nei settings
                SharedPreferences.Editor editor = myAppSettings.edit();
                editor.putInt(myGlobal.PREFERENCE_APP_LASTCHIFA, posChiFa);
                editor.commit();
			} else {
				showToast("Dati non corretti nessun dato aggiunto");
			}

		}
	};


	// *************************************************************************
	// classe  Spinner Selected item
	// *************************************************************************        
	class SelectSpinAutocomplete implements Spinner.OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			textTitle.setTextColor(getResources().getColor(R.color.TitleYellow));	    	
			if (parentView.getId() == R.id.SpinnerCategoria) {
				AutoCompleteTextView textViewCat = (AutoCompleteTextView) findViewById(R.id.TextAutocompleteCategoria);
				textViewCat.setText(spinCategoria.getSelectedItem().toString().trim());
			} else if (parentView.getId() == R.id.SpinnerADa) {
				AutoCompleteTextView textViewCat = (AutoCompleteTextView) findViewById(R.id.TextAutocompleteADa);
				textViewCat.setText(spinADa.getSelectedItem().toString().trim());
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			// your code here

		}

	};



	// *************************************************************************
	// classe  per getione Focus Change Categoria
	// *************************************************************************            
	class ChangeFocusAutoComplete implements View.OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {        	
			if ((v.getId() == R.id.TextAutocompleteCategoria && !hasFocus) || (v.getId() == R.id.TextAutocompleteADa && !hasFocus)) {
				showToast("Performing validation");
				((AutoCompleteTextView)v).performValidation();
				textTitle.setTextColor(getResources().getColor(R.color.TitleYellow));
			} 
		}
	}


	// *************************************************************************
	// classe  per validazione AutoComplete
	// *************************************************************************  
	class ValidateCategoria implements AutoCompleteTextView.Validator {			
		@Override
		public boolean isValid(CharSequence text) {
			List<String> mylistr = Arrays.asList(myGlobal.arrCategoria);
			if (mylistr.contains(text.toString())) {

				int spinnerPosition = adapterCat.getPosition(text.toString());
				spinCategoria.setSelection(spinnerPosition);

				return true;
			}

			return false;			
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {
			String fxTxt;
			int numline=0, posch=0, maxposch=0, memoline=0;
			// Whatever value you return here must be in the list of valid words.

			List<String> mylistr = Arrays.asList(myGlobal.arrCategoria);
			if (mylistr.contains(invalidText.toString())) {
				return invalidText;
			} else {
				while (numline < myGlobal.arrCategoria.length) {
					posch = 0;
					boolean charCmpIsDifferent = false;
					char cmp1, cmp2;
					while (!charCmpIsDifferent && (posch < myGlobal.arrCategoria[numline].length()) && (posch<invalidText.length())) {
						// confronto carattere per carattere
						cmp1 = myGlobal.arrCategoria[numline].charAt(posch);
						cmp2 = invalidText.charAt(posch);						

						// se sono lettere faccio Upcase
						if (Character.isLetter(myGlobal.arrCategoria[numline].charAt(posch))) {
							cmp1 = Character.toUpperCase(cmp1);
							cmp2 = Character.toUpperCase(cmp2);						
						}

						// Se confronto OK proseguo altrimenti mi fermo
						if (cmp1 == cmp2) {
							posch++;
						} else {
							charCmpIsDifferent = true;
						}

					}

					if (posch > maxposch) {
						maxposch = posch;
						memoline = numline;
					}

					numline++;
				}
				fxTxt = myGlobal.arrCategoria[memoline];
				showToast("Text Categoria Fixed: " + fxTxt);

				int spinnerPosition = adapterCat.getPosition(fxTxt);
				spinCategoria.setSelection(spinnerPosition);

				return fxTxt;
			}

		}
	};



	class ValidateADa implements AutoCompleteTextView.Validator {			
		@Override
		public boolean isValid(CharSequence text) {

			List<String> mylistr = Arrays.asList(myGlobal.arrADa);
			if (mylistr.contains(text.toString())) {

				int spinnerPosition = adapterADa.getPosition(text.toString());
				spinADa.setSelection(spinnerPosition);

				return true;
			}

			return false;			
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {

			// Whatever value you return here must be in the list of valid words.
			List<String> mylistr = Arrays.asList(myGlobal.arrADa);
			if (mylistr.contains(invalidText.toString())) {
				return invalidText;
			} else {				
				return invalidText;
			}

		}
	};	

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user						
			//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
			//String formattedDate = df.format(new Date(year-1900, month, day));


	        Calendar cal = Calendar.getInstance();
	        cal.set(Calendar.YEAR, year);
	        cal.set(Calendar.DAY_OF_MONTH, day);
	        cal.set(Calendar.MONTH, month);
	    	String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(cal.getTime());
	    	
			EditText editTextData = (EditText) getActivity().findViewById(R.id.TextData);
			editTextData.setText(formattedDate);


		}
	}



	class ClickDataButton implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {    		
			if (MotionEvent.ACTION_UP == event.getAction()) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(getSupportFragmentManager(), "datePicker");
				textTitle.setTextColor(getResources().getColor(R.color.TitleYellow));
			}
			return false;
		}
	};



	// *************************************************************************
	// classe  click button ON
	// *************************************************************************    
	class ClickResetButton implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// Perform action on click
			initTextValue();
		}
	};



	public void initTextValue() {
        SharedPreferences myAppSettings  = getSharedPreferences(myGlobal.PREFERENCE_APP_FILE, 0);

		textTitle.setTextColor(getResources().getColor(R.color.TitleYellow));

		Spinner spinner;
		EditText myeditText;

		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
		String formattedDateOnly = df.format(c.getTime());

		myeditText = (EditText) findViewById(R.id.TextData);
		myeditText.setText(formattedDateOnly);

		myeditText = (EditText) findViewById(R.id.TextDescrizione);
		myeditText.setText("");

		myeditText = (EditText) findViewById(R.id.TextValore);
		myeditText.setText("0");
        myeditText.requestFocus();

		myeditText = (EditText) findViewById(R.id.TextNote);
		myeditText.setText("");


		spinner = (Spinner) findViewById(R.id.SpinnerTipoOper);
		spinner.setSelection(0, true);

        // prendo la posizione da selezionare nei settings, se non esiste prendo la 0
        int numLastChiFa = myAppSettings.getInt(myGlobal.PREFERENCE_APP_LASTCHIFA,0);
        spinner = (Spinner) findViewById(R.id.SpinnerChiFa);
        if ((numLastChiFa < spinner.getAdapter().getCount()) && (numLastChiFa >= 0))
		    spinner.setSelection(numLastChiFa, true);
        else
            spinner.setSelection(0, true);


		spinner = (Spinner) findViewById(R.id.SpinnerPersonale);
		spinner.setSelection(0, true);	

		// Categoria 
		spinCategoria  = (Spinner) findViewById(R.id.SpinnerCategoria);
		int spinnerPosition = adapterCat.getPosition("Spesa");
		spinCategoria.setSelection(spinnerPosition);


		// A/Da
		spinADa = (Spinner) findViewById(R.id.SpinnerADa);
        spinADa.setSelection(0, true);

	}






	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 */
	private void loadAuth(AndroidAuthSession session) {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

		if (key.equals("oauth2:")) {
			// If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
			session.setOAuth2AccessToken(secret);
		} else {
			// Still support using old OAuth 1 tokens.
			session.setAccessTokenPair(new AccessTokenPair(key, secret));
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 */
	private void storeAuth(AndroidAuthSession session) {
		// Store the OAuth 2 access token, if there is one.
		String oauth2AccessToken = session.getOAuth2AccessToken();
		if (oauth2AccessToken != null) {
			SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
			Editor edit = prefs.edit();
			edit.putString(ACCESS_KEY_NAME, "oauth2:");
			edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
			edit.commit();
			return;
		}
		// Store the OAuth 1 access token, if there is one.  This is only necessary if
		// you're still using OAuth 1.
		AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
		if (oauth1AccessToken != null) {
			SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
			Editor edit = prefs.edit();
			edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
			edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
			edit.commit();
			return;
		}
	}


	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		loadAuth(session);
		return session;
	}   

	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = myGlobal.mApiDropbox.getSession();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				storeAuth(session);
				setDropboxLoggedIn(true);
			} catch (IllegalStateException e) {
				showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
				Log.i(myGlobal.TAG, "Error authenticating", e);
			}
		}
	}

	private void logOutDropbox() {
		// Remove credentials from the session
		myGlobal.mApiDropbox.getSession().unlink();
		// Clear our stored keys
		clearKeys();
		// Change UI state to display logged out version
		setDropboxLoggedIn(false);
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}
	/**
	 * Convenience function to change UI state based on being logged in
	 */
	private void setDropboxLoggedIn(boolean loggedIn) {

		mDropboxLoggedIn = loggedIn;
		if (loggedIn) {
			//mSubmit.setText("Unlink from Dropbox");
			//mDisplay.setVisibility(View.VISIBLE);
		} else {
			//mSubmit.setText("Link with Dropbox");
			//mDisplay.setVisibility(View.GONE);            
		}
	}

	private void checkDropboxAppKeySetup() {
		// Check to make sure that we have a valid app key
		if (APP_KEY.startsWith("CHANGE") ||
				APP_SECRET.startsWith("CHANGE")) {
			showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
			finish();
			return;
		}

		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			showToast("URL scheme in your app's " +
					"manifest is not set up correctly. You should have a " +
					"com.dropbox.client2.android.AuthActivity with the " +
					"scheme: " + scheme);
			finish();
		}
	}

}