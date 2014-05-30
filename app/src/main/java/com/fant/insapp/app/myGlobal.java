package com.fant.insapp.app;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class myGlobal extends Application
{

	public static final String TAG = "FANTUZ_Activity";

	public static final String LOCAL_DB_FILENAME = "INSbase_loc.sqlite";	
	public static final String REMOTE_DB_FILENAME = "INSbase.sqlite";
	public static final String REMOTE_DB_FILENAME_EMPTY = "INSbase_loc_empty.sqlite";
	public static final String LOCAL_DOWNLOADED_DB_FILE = "INSbase_download.sqlite";
	public static final String LOCAL_FULL_DB_FILE = "INSbase_full.sqlite";
	public static final String DROPBOX_INS_DIR = "/INS/";
	public static boolean statoDBLocal;
	public static boolean statoDBLocalFull;
	public static DropboxAPI<AndroidAuthSession> mApiDropbox;

	public static boolean ReadTxtActivityLoaded;

	public static String[] arrTipoOperazione;
	public static String[] arrChiFa;
	public static String[] arrCPersonale;
	public static String[] arrCategoria;	
	public static String[] arrADa;

    public static String[] arrGenerica;

	public static MyDatabase DBINSlocal;

	Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		// Cose da fare una volta sola all'avvio dell'applicazione
	}

	// constructor
	public myGlobal(Context context){
		this.mContext = context;
	}

	// *************************************************************************
	// Ritorno il percorso dove vado a salvare i file, se non esiste lo crea anche
	// *************************************************************************
	public static java.io.File getStorageFantDir(){
		// controllo presenza dir e se non c'è la creo
		String storageDir = Environment.getExternalStorageDirectory().getPath() + java.io.File.separator + "FanINS";
		java.io.File myfolder = new java.io.File(storageDir);
		if (!myfolder.exists())
			myfolder.mkdir();        
		return (myfolder);

	}

	public static java.io.File getStorageDatabaseFantDir(){
		// controllo presenza dir e se non c'è la creo
		String storageDir = Environment.getExternalStorageDirectory().getPath() + java.io.File.separator + "FanINS" + java.io.File.separator + "DB";
		java.io.File myfolder = new java.io.File(storageDir);
		if (!myfolder.exists())
			myfolder.mkdir();        
		return (myfolder);
	}


	// TODO 
	public String convToSQLiteDate(String _dataDaConvertire) throws ParseException {
		String tmpstr = _dataDaConvertire;

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YYY");
		Date myDate = dateFormat.parse(_dataDaConvertire);

		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.ITALY);


		return (tmpstr);
	}

	public static void copyFiles(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();

	}

	public static void copyFiles2(File src, File dst) throws IOException {

		Files.copy(src, dst);

	}     


	public static boolean checkData(String _valData)  {
		String dataStr = _valData;    		
		String[] splitData = {""};
		int giorno, mese, anno;
		boolean changed = false, errData = false;

		if (dataStr.contains("/")) {
			splitData = dataStr.split("/");
		} else if (dataStr.contains(".")) {
			splitData = dataStr.split(".");        				
		} else if (dataStr.contains(" ")) {
			splitData = dataStr.split(" ");        				
		} else if (dataStr.contains("-")) {
			splitData = dataStr.split("-");        				
		}
		if (splitData.length > 2 ) {
			anno = (int) Integer.parseInt(splitData[0]);
			if (anno<0 || anno >2299) {
				anno = 2014;
				errData = changed = true;				
			}
		}		
		if (splitData.length > 0 ) {
			mese = (int) Integer.parseInt(splitData[1]);
			if (mese<0 || mese >12) {
				mese = 1;
				errData = changed = true;
			}
		}
		if (splitData.length > 1 ) {
			giorno = (int) Integer.parseInt(splitData[2]);
			if (giorno<0 || giorno >31) {
				giorno = 1;
				errData = changed = true;
			}
		}



		// reg exp per MM-dd-yyyy o MM/dd/yyyy o MM.dd.yyyy
		//String regEx = "^(0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])[- /.](19|20)\\d\\d$";

		// reg exp per yyyy-MM-dd o yyyy/MM/dd ecc...		
		String regEx = "^(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])$";
		if (_valData.matches(regEx) && !errData) 
			return(true);
		else
			return(false);    	
	}

	public static boolean checkValore(String _valValore)  {
		float myFloat;
		String regEx = "^(-)?\\d*(\\.\\d*)?$";


		if (_valValore == "") return false;

		try {
			myFloat = Float.parseFloat(_valValore);
		} catch (Exception e) {
			return false;
		}

		if (myFloat == 0) return false;

		if (_valValore.matches(regEx)) 
			return(true);
		else
			return(false);

	}


	public static String formattedDate() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd@HH'h'mm'm'ss's'", Locale.ITALY);
		String _formattedDate = df.format(c.getTime());
		return _formattedDate;
	}


	// *************************************************************************
	// Preparo file database, se non ci sono li crea 
	// *************************************************************************    
	public static  boolean prepDBfilesisOK(final Context ctx, boolean _forceDownladlocalEmpty, boolean _forceDownladlocalFull, final boolean backupIfPossible){
		MyDatabase DBINStmp;
		String tmpmsg;


		final File _local = new File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_DB_FILENAME);
		final File _full = new File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE);

		try  {
			// controllo presenza dei file Database locali
			if(!_local.exists() || _forceDownladlocalEmpty) {
				if(!_local.exists())
					tmpmsg = "File non trovato: " + myGlobal.LOCAL_DB_FILENAME;
				else 
					tmpmsg = "Scaricamento " + myGlobal.LOCAL_DB_FILENAME;
				// file non esiste devo scaricarlo?
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder
				.setTitle(tmpmsg)
				.setMessage("Scaricarlo da DropBox?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {			      	
						DownloadFromDropbox download2 = new DownloadFromDropbox(ctx, myGlobal.mApiDropbox, myGlobal.DROPBOX_INS_DIR, myGlobal.REMOTE_DB_FILENAME_EMPTY,
								myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_DB_FILENAME, backupIfPossible);
						download2.execute();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {		
						Toast.makeText(ctx, "File inesistente e non scaricato possibili errori nel programma!", Toast.LENGTH_LONG).show();    		    		
						if (!_local.exists())
							myGlobal.statoDBLocal = false;
					}
				})
				.show();    			

			} else {

				DBINSlocal = new MyDatabase(
						ctx, 
						myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_DB_FILENAME);

				DBINSlocal.open();
				DBINSlocal.close();
				myGlobal.statoDBLocal = true;
			}



			if(!_full.exists() || _forceDownladlocalFull) {
				if(!_local.exists())
					tmpmsg = "File non trovato: " + myGlobal.LOCAL_FULL_DB_FILE;
				else 
					tmpmsg = "Scaricamento " + myGlobal.LOCAL_FULL_DB_FILE;				
				// file non esiste devo scaricarlo?
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder
				.setTitle(tmpmsg)
				.setMessage("Scaricarlo da DropBox?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {			      	
						DownloadFromDropbox download2 = new DownloadFromDropbox(ctx, myGlobal.mApiDropbox, myGlobal.DROPBOX_INS_DIR, myGlobal.REMOTE_DB_FILENAME,
								myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_FULL_DB_FILE, backupIfPossible);
						download2.execute();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {		
						Toast.makeText(ctx, "File inesistente e non scaricato possibili errori nel programma!", Toast.LENGTH_LONG).show();
						if (!_full.exists())
							myGlobal.statoDBLocalFull = false;
					}
				})
				.show();    			

			} else {
				DBINStmp = new MyDatabase(
						ctx, 
						myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE);

				DBINStmp.open();
				DBINStmp.close();
				myGlobal.statoDBLocalFull = true;
			}


			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(ctx, "Error Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}

	}
	
	public static String FloatToStr(float _num){
		String _tmp;
		
		DecimalFormat formatter;// = (DecimalFormat) NumberFormat.getInstance(Locale.ITALY);
		formatter = new DecimalFormat("###,###.##€");
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

		symbols.setGroupingSeparator(' ');
		_tmp = formatter.format(_num);
				
		return _tmp;
	}
	
	 public static String intToString(int num, int digits) {
		    assert digits > 0 : "Invalid number of digits";

		    // create variable length array of zeros
		    char[] zeros = new char[digits];
		    Arrays.fill(zeros, '0');
		    // format number as String
		    DecimalFormat df = new DecimalFormat(String.valueOf(zeros));

		    return df.format(num);
		}
	
}