

package com.fant.insapp.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

/**
 * Here we show getting metadata for a directory and downloading a file in a
 * background thread, trying to show typical exception handling and flow of
 * control for an app that downloads a file from Dropbox.
 */

public class DownloadFromDropbox extends AsyncTask<Void, Long, Boolean> {

	//java.io.File myFileDropbox;


	private Context mContext;
	private ProgressDialog mDialog;
	private DropboxAPI<?> mApi;
	private String mPath;    


	private boolean mCanceled;
	private Long mFileLen;
	private String mErrorMsg;

	// Note that, since we use a single file name here for simplicity, you
	// won't be able to use this code for two simultaneous downloads.
	private static String local_file_name;
	private static String dropbox_file_name;
	private static boolean backupLocalFile;



	public DownloadFromDropbox(Context context, DropboxAPI<?> api,
			String dropboxPath, String dropboxFile, String localFile, boolean createBackup) {
		// We set the context this way so we don't accidentally leak activities
		mContext = context.getApplicationContext();

		mApi = api;
		mPath = dropboxPath;
		local_file_name = localFile;
		dropbox_file_name = dropboxFile;
		backupLocalFile = createBackup;
		
		
		mDialog = new ProgressDialog(context);
		mDialog.setMax(100);
		mDialog.setMessage("Downloading " + local_file_name);
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

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			if (mCanceled) {
				return false;
			}

			// Get the metadata for a directory
			Entry dirent = mApi.metadata(mPath, 1000, null, true, null);

			if (!dirent.isDir || dirent.contents == null) {
				// It's not a directory, or there's nothing in it
				mErrorMsg = "File or empty directory";
				return false;
			}

			// Make a list of everything in it that we can get a thumbnail for
			ArrayList<Entry> thumbs = new ArrayList<Entry>();
			for (Entry ent: dirent.contents) {

				if (new String(ent.fileName()).equals(dropbox_file_name)) {
					// Add it to the list of thumbs we can choose from
					thumbs.add(ent);
				}
			}

			if (mCanceled) {
				return false;
			}

			if (thumbs.size() == 0) {
				// No thumbs in that directory
				mErrorMsg = "Not find: " + dropbox_file_name ;
				return false;
			}

			// Eseguo backup locale se richiesto
			if (backupLocalFile) {
				if (!(new File(local_file_name).exists())) {
					showToast("File " + local_file_name + " non esiste, non si può fare backup ");
				} else {
					try {

						// backup file													
						java.io.File oldFile = new java.io.File(local_file_name);
						java.io.File newFile = new java.io.File(local_file_name + "."  + myGlobal.formattedDate() + ".bkup" );
						myGlobal.copyFiles(oldFile, newFile);
					} catch (IOException e) {				
						e.printStackTrace();
						showToast("Error IOException: " + e.getMessage());
					}	
				}
			}


			
			// Now pick the first one
			Entry ent = thumbs.get(0);
			String path = ent.path;
			mFileLen = ent.bytes;

			try {
				File file = new File(local_file_name);
				FileOutputStream outputStream;
				outputStream = new FileOutputStream(file);
				DropboxFileInfo info = mApi.getFile(ent.path, null, outputStream, 
						new ProgressListener() {
					@Override
					public long progressInterval() {
						// Update the progress bar every half-second or so
						return 100;
					}

					@Override
					public void onProgress(long bytes, long total) {
						publishProgress(bytes);
					}
				});
				Log.i(myGlobal.TAG, "The file's rev is: " + info.getMetadata().rev);
			} catch (FileNotFoundException e1) {
				mErrorMsg = "Couldn't create a local file to store the image";
				return false;
			}


			return true;

		} catch (DropboxUnlinkedException e) {
			// The AuthSession wasn't properly authenticated or user unlinked.
			mErrorMsg = "Dropbox session not properly authenticated or user unlinked";
		} catch (DropboxPartialFileException e) {
			// We canceled the operation
			mErrorMsg = "Download canceled";
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
				// path not found (or if it was the thumbnail, can't be
				// thumbnailed)
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
		} catch (DropboxParseException e) {
			// Probably due to Dropbox server restarting, should retry
			mErrorMsg = "Dropbox error.  Try again.";
		} catch (DropboxException e) {
			// Unknown error
			mErrorMsg = "Unknown error.  Try again.";
		}
		return false;
	}

	@Override
	protected void onProgressUpdate(Long... progress) {
		int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
		mDialog.setProgress(percent);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// Chiusa mDialog posso fare qualcos ain funzione di result
		mDialog.dismiss();        
		if (result) {
			// result OK
			showToast("File successfully downloaded");
			
			// aggiorno booleani di presenza DB
			if (new File(local_file_name).getName().equals(myGlobal.LOCAL_DB_FILENAME))
				myGlobal.statoDBLocal = true;
			if (new File(local_file_name).getName().equals(myGlobal.LOCAL_FULL_DB_FILE))
				myGlobal.statoDBLocalFull = true;
			
		} else {
			// Couldn't download it, so show an error
			showToast(mErrorMsg);
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		error.show();
	}


}
