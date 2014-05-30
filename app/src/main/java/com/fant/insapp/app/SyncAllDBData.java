package com.fant.insapp.app;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SyncAllDBData extends AsyncTask<Void, Long, Boolean> {

	//private DropboxAPI<?> mApi;

	private UploadRequest mRequest;
	private boolean mCanceled;
	private String mPath;
	private ProgressDialog mDialog;
	private Long mFileLen;
	private Long totProgressLen;
	private String mErrorMsg;
	Cursor mycursor;
	private int faseTask;
	private MyDatabase DBINSlocal, DBINSdownloaded;
	private Context mycontext;
	
	private int datiInseriti;

	// *******************************
	// Costruttore, prepara tuttoe mostra dialog
	// *******************************        
	public SyncAllDBData(Context _context) {

		datiInseriti = 0;
		mycontext = _context;
		// riferimento totale progress
		totProgressLen = Long.valueOf(500);


		mPath = myGlobal.DROPBOX_INS_DIR;
		faseTask = 0;


		mDialog = new ProgressDialog(mycontext);
		mDialog.setMax(100);
		mDialog.setMessage("Sincronizzazione Database ");
		mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDialog.setProgress(0);

		mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// This will cancel the putFile operation
				//mRequest.abort();
				mCanceled = true;
				mErrorMsg = "Canceled";
			}
		});

		mDialog.show();
	}


	// *******************************
	// Operazioni da eseguire in background
	// *******************************
	@Override
	protected Boolean doInBackground(Void... params) {
		String path;



		try {
			if (mCanceled) return false;


			// *******************************
			// *-*-*-*  Innanzi tutto scarico ultima versione del file da DropBox

			// Get the metadata for a directory
			DropboxAPI.Entry dirent = myGlobal.mApiDropbox.metadata(mPath, 1000, null, true, null);

			if (!dirent.isDir || dirent.contents == null) {
				// It's not a directory, or there's nothing in it
				mErrorMsg = "File or empty directory";
				return false;
			}

			// Make a list of everything in it that we can get a thumbnail for
			ArrayList<DropboxAPI.Entry> thumbs = new ArrayList<DropboxAPI.Entry>();
			for (DropboxAPI.Entry ent: dirent.contents) {                	
				if (new String(ent.fileName()).equals(myGlobal.REMOTE_DB_FILENAME)) {
					// Add it to the list of thumbs we can choose from
					thumbs.add(ent);
				}
			}

			if (mCanceled) return false;

			if (thumbs.size() == 0) {
				// Nessun file remoto
				mErrorMsg = "Non trovato: " + myGlobal.REMOTE_DB_FILENAME ;
				return false;
			}

			// Now pick the first one
			DropboxAPI.Entry ent = thumbs.get(0);
			path = ent.path;
			mFileLen = ent.bytes;

			publishProgress((totProgressLen/20));
			faseTask++;

			try {
				File file = new File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_DOWNLOADED_DB_FILE);
				FileOutputStream outputStream;
				outputStream = new FileOutputStream(file);
				DropboxFileInfo info = myGlobal.mApiDropbox.getFile(ent.path, null, outputStream, 
						new ProgressListener() {
					@Override
					public long progressInterval() {
						// Update the progress bar every half-second or so
						return 100;
					}

					@Override
					public void onProgress(long bytes, long total) {    	                	
						publishProgress((totProgressLen/20) + ((bytes * (totProgressLen/2))/mFileLen));
					}
				});
				Log.i(myGlobal.TAG, "The file's rev is: " + info.getMetadata().rev);
			} catch (FileNotFoundException e1) {
				mErrorMsg = "Impossibile creare file locale";
				return false;
			}



			publishProgress((totProgressLen/2));
			faseTask++;



			File filechk = new File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_DOWNLOADED_DB_FILE);
			if(!filechk.exists()) {
				mErrorMsg = "File scaricato non trovato: " + myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_DOWNLOADED_DB_FILE;    			
				return false;
			}


			// *******************************
			// *-*-*-* Apertura Database locale e remoto scaricato
			DBINSlocal = new MyDatabase(
					mycontext, 
					myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_DB_FILENAME);

			DBINSdownloaded = new MyDatabase(
					mycontext, 
					myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_DOWNLOADED_DB_FILE);



			DBINSlocal.open();
			DBINSdownloaded.open();

			// *******************************
			// *-*-*-*  Attacco DB Locale al DB scaricato
			DBINSdownloaded.execSQLsimple("attach database \"" + myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_DB_FILENAME + "\" as locdbatt");
			// Faccio intersezione tra DB scaricato e DB locale su alcune Colonne
			mycursor = DBINSdownloaded.rawQuery("SELECT DataOperazione,TipoOperazione,ChiFa,ADa,CPers,Valore,Categoria,Descrizione FROM  myINSData" 
					+ " INTERSECT " +
					"SELECT DataOperazione,TipoOperazione,ChiFa,ADa,CPers,Valore,Categoria,Descrizione  FROM  locdbatt.myINSData ", null);

			// se getCount=0 vuol dire che non ci sono righe doppie
			if (mycursor.getCount() != 0) {
				// TODO Ci sono delle righe doppie 

			}

			// *******************************
			// *-*-*-* Aggiungo al DB scaricato i valori del DB locale che vengono cancellati man mano
			Cursor cursorLocal = DBINSlocal.fetchDati();		
			//showToast("Inserimento di " + cursorLocal.getCount() + " dati!");
			datiInseriti = cursorLocal.getCount();
			if (cursorLocal.getCount() != 0) {
				while ( cursorLocal.moveToNext() ) {
					DBINSdownloaded.insertRecordDataIns(
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.DATA_OPERAZIONE_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.TIPO_OPERAZIONE_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.CHI_FA_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.A_DA_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.C_PERS_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.VALORE_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.CATEGORIA_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.DESCRIZIONE_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.NOTE_KEY) ), 
							cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.SPECIAL_NOTE_KEY) ));

					// elimino dal database la riga corrispondente, guardando solo il codice ID univoco 
					String actualID = cursorLocal.getString( cursorLocal.getColumnIndex(MyDatabase.DataINStable.ID) );
					DBINSlocal.deleteDatabyID(actualID);
				}
			}





			publishProgress((totProgressLen*3/5));
			faseTask++;

			// *******************************
			// *-*-*-* Tramite query aggiorno categoria Generica nel file completo
			DBINSdownloaded.execSQLsimple("UPDATE " + MyDatabase.DataINStable.TABELLA_INSDATA + " SET Generica = (SELECT  " + MyDatabase.DataINStable.GENERICA_KEY + " FROM  " + MyDatabase.DataINStable.TABELLA_CATEGORIE + "  WHERE  " + MyDatabase.DataINStable.TABELLA_CATEGORIE + ".Valori =  " + MyDatabase.DataINStable.TABELLA_INSDATA + "." + MyDatabase.DataINStable.CATEGORIA_KEY + " )");

			DBINSlocal.close();
			DBINSdownloaded.close();


			// *******************************
			// *-*-*-* Salvo il DB downloaded aggiornato rinominandolo come LOCAL_FULL_DB_FILE
			java.io.File oldFile = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_DOWNLOADED_DB_FILE);				
			oldFile.renameTo(new java.io.File( myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_FULL_DB_FILE));		            	    	
			//showToast("aggiornato file DB full locale: " + myGlobal.LOCAL_FULL_DB_FILE);


			// *******************************
			// *-*-*-* Preparo l'upload creando in locale una copia del file LOCAL_FULL_DB_FILE con nome REMOTE_DB_FILENAME
			java.io.File oldFileDB = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE);        		
			java.io.File newFileDB2 = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.REMOTE_DB_FILENAME);
			myGlobal.copyFiles(oldFileDB, newFileDB2);
			//myGlobal.copyFiles2(oldFileDB, newFileDB);   	    	
			//Files.copy(oldFileDB, newFileDB);




			// *******************************
			// *-*-*-* Upload del file REMOTE_DB_FILENAME
			mFileLen = newFileDB2.length();
			FileInputStream fis = new FileInputStream(newFileDB2);
			path = mPath + newFileDB2.getName();    
			if (true) {
				// prima di fare upload tento di farne una copia di backup in remoto
				try {
					DropboxAPI.Entry newEntry = myGlobal.mApiDropbox.copy(path, path + "."  + myGlobal.formattedDate() + ".bkup");
				} catch (DropboxUnlinkedException e) {
					Log.e(myGlobal.TAG, "User has unlinked." + e.getMessage());
				} catch (DropboxException e) {
					Log.e(myGlobal.TAG, "Something went wrong while copying."  + e.getMessage());
				}
			}
			// By creating a request, we get a handle to the putFile operation, so we can cancel it later if we want to
			mRequest = myGlobal.mApiDropbox.putFileOverwriteRequest(path, fis, newFileDB2.length(), 
					new ProgressListener() {
				@Override
				public long progressInterval() {
					// Update the progress bar every ...
					return 100;
				}

				@Override
				public void onProgress(long bytes, long total) {
					publishProgress(((totProgressLen * 3/5)) + ((bytes * ((totProgressLen * 2) / 5))/mFileLen) );
				}
			});

			// *******************************
			// *-*-*-* Operazione terminata
			if (mRequest != null) {
				mRequest.upload();
				return true;
			}
			return true;

		} catch (DropboxUnlinkedException e) {
			// The AuthSession wasn't properly authenticated or user unlinked.
			mErrorMsg = "Dropbox session not properly authenticated or user unlinked";
			return (false);
		} catch (DropboxPartialFileException e) {
			// We canceled the operation
			mErrorMsg = "Download canceled";
			return (false);
		} catch (DropboxServerException e) {
			// Server-side exception.  These are examples of what could happen, 
			// but we don't do anything special with them here.
			if (e.error == DropboxServerException._304_NOT_MODIFIED) {
				// won't happen since we don't pass in revision with metadata
			} else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
				// Unauthorized, so we should unlink them.  You may want to
				// automatically log the user out in this case.
			} else if (e.error == DropboxServerException._403_FORBIDDEN) {
				// Not allowed to access this
			} else if (e.error == DropboxServerException._404_NOT_FOUND) {
				// path not found (or if it was the thumbnail, can't be thumbnailed)
			} else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
				// too many entries to return
			} else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
				// can't be thumbnailed
			} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
				// user is over quota
			} else {
				// Something else
			}
			// This gets the Dropbox error, translated into the user's language
			mErrorMsg = e.body.userError;
			if (mErrorMsg == null) {
				mErrorMsg = e.body.error;
			}
		} catch (DropboxIOException e) {
			// Happens all the time, probably want to retry automatically.
			mErrorMsg = "Network error.  Try again.";
			return (false);
		} catch (DropboxParseException e) {
			// Probably due to Dropbox server restarting, should retry
			mErrorMsg = "Dropbox error.  Try again.";
			return (false);
		} catch (DropboxException e) {
			// Unknown error
			mErrorMsg = "Unknown error.  Try again.";
			return (false);
		} catch (FileNotFoundException e1) {
			mErrorMsg = "File not found Exception. " + e1.getMessage();
		} catch (IOException e) {            	
			mErrorMsg = "Error IOException: " + e.getMessage();
			return (false);
		}




		return true;
	}      

	@Override
	protected void onPostExecute(Boolean result) {
		AlertDialog.Builder builder;

		//showToast("aggiornato file DB full locale: " + myGlobal.LOCAL_FULL_DB_FILE);			datiInseriti
		
		mDialog.dismiss();
		String textToShow;			
		if (result) {
			// result OK
			//showToast("Sincronizzazione terminata correttamente");
			textToShow = ("Sincronizzazione terminata correttamente "+ System.getProperty("line.separator") +
					"Aggiunti " + String.valueOf(datiInseriti) + " dati! " + System.getProperty("line.separator") +
					"" + System.getProperty("line.separator") +
					"Scaricato il database dal cloud e integrato con i dati inseriti nel file locale" + System.getProperty("line.separator") +
					"Il file locale è stato svuotato. Ricaricato nel cloud il database completo: " + myGlobal.REMOTE_DB_FILENAME + System.getProperty("line.separator") +
					System.getProperty("line.separator") +					
					"Aggiornate la copia del database completo in locale: "+ myGlobal.LOCAL_FULL_DB_FILE + System.getProperty("line.separator") +
					"Attenzione che eventuali precedenti modifiche al database completo locale non committate sul cloud sono perse." + System.getProperty("line.separator") +
					"" + System.getProperty("line.separator") +
					"");

			builder=new AlertDialog.Builder(mycontext);
			builder.setTitle("Sincronizzazione DB");
			builder.setMessage(textToShow);
			builder.setCancelable(false);
			builder.setPositiveButton("Chiudi",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.dismiss();
					//finish();
				}
			});
			builder.show();
		} else {
			// Couldn't download it, so show an error
			//showToast(mErrorMsg);
			textToShow = ("Errore rilevato durante la sincronizzazione:" + System.getProperty("line.separator") +
					mErrorMsg + System.getProperty("line.separator") +
					"" + System.getProperty("line.separator") +
					"" + System.getProperty("line.separator") +
					"Conviene rieffettuare l'operazione. Nel caso peggiore cancellare i file locali o forzare un nuovo download" + System.getProperty("line.separator") +
					"" + System.getProperty("line.separator") +
					"");

			builder=new AlertDialog.Builder(mycontext);
			builder.setTitle("Sincronizzazione DB");
			builder.setMessage(textToShow);
			builder.setCancelable(false);
			builder.setPositiveButton("Chiudi",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.dismiss();
					//finish();
				}
			});	
			builder.show();
		}

	}

	@Override
	protected void onPreExecute() {        	
	}

	@Override
	protected void onProgressUpdate(Long... progress) {
		int percent = (int)(100.0*(double)progress[0]/totProgressLen + 0.5);
		mDialog.setProgress(percent);
		if (faseTask == 1) {
			mDialog.setMessage("Downloading " + myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_DOWNLOADED_DB_FILE);
		} else if (faseTask == 2) {
			mDialog.setMessage("Aggiornamento dati database.. ");
		} else if (faseTask == 3) {
			mDialog.setMessage("Uploading " + myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.REMOTE_DB_FILENAME);
		}

	}        



}