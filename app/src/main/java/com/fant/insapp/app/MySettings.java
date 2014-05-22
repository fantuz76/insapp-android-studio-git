package com.fant.insapp.app;


import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


public class MySettings extends PreferenceActivity {

	static Context mycontext;
public static String versionName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        mycontext = this;

  		try {
			versionName = this.getPackageManager()
            	    .getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			versionName = "Errore versione non rilevata";
		}

  		
  		
  		
  		
        // Add a button to the header list.
        if (hasHeaders()) {
            Button button = new Button(this);
            button.setText("Pulsante");
            setListFooter(button);
        }
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
    	
        loadHeadersFromResource(R.xml.preference_headers, target);
		        
    }
    
    
    public static void CancellaDatabase() {

		//Put up the Yes/No message box
		AlertDialog.Builder buildererase = new AlertDialog.Builder(mycontext);
		buildererase
		.setTitle("Cancellazione dei file database locali")
		.setMessage("Sicuro di cancellare i file Database?" + System.getProperty("line.separator") +
				"Verranno persi tutti dati inseriti e non sincronizzati (database locale) "+ System.getProperty("line.separator") +
				"e eventuali modifiche non committate del database completo (database completo locale)")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {    	    	    	    	    	    	

				try {
					java.io.File locFileDB = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_DB_FILENAME);
					if(!locFileDB.exists()) {
						
						showToast("File inesistente: " + locFileDB.getName());
						myGlobal.statoDBLocal = false;
					} else if (locFileDB.delete()) {
						showToast("Cancellato: " + locFileDB.getName());
						myGlobal.statoDBLocal = false;
					}
				} catch (Exception e) {
					showToast("Errore" + e.getMessage());
				}

				try {
					java.io.File locFileFullDB = new java.io.File(myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator +  myGlobal.LOCAL_FULL_DB_FILE);
					if(!locFileFullDB.exists()) {
						showToast("File inesistente: " + locFileFullDB.getName());
						myGlobal.statoDBLocal = false;
					} else if (locFileFullDB.delete()) {
						showToast("Cancellato: " + locFileFullDB.getName());
						myGlobal.statoDBLocalFull = false;
					}
				} catch (Exception e) {
					showToast("Errore" + e.getMessage());
				}

			}
		})
		.setNegativeButton("No", null)						//Do nothing on no
		.show();   
    }


    
    public static class DeleteAllDatabase extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CancellaDatabase();
        }
    }    

    public static class DownloadAllDatabase extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

			// Prima di sincronizzare chiedo se voglio fare la copia in locale
            // l'info viene poi passata tramite booleano fino alla procedura di download da dropbox

			AlertDialog.Builder buildersync = new AlertDialog.Builder(mycontext);
			buildersync    	    	
			.setTitle("Backup file")
			.setMessage("Creare una copia nel telefono dei file prima di scaricare?" + System.getProperty("line.separator") +
					"(consigliato anche se occupa memoria)")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
				            if (!myGlobal.prepDBfilesisOK(mycontext,true,true,true))
				            	showToast("Errore nel download database");
						}    				    	
					})
					.setNegativeButton("No",  new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Solo addesso faccio iniziare il download
				            if (!myGlobal.prepDBfilesisOK(mycontext,true,true,false))
				            	showToast("Errore nel download database");
						}
					})						
					.show();

             	         
        }
    }    

    
    
    /**
     * This fragment shows the preferences for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            //PreferenceManager.setDefaultValues(getActivity(),
            //        R.xml.advanced_preferences, false);

            // Load the preferences from an XML resource

            addPreferencesFromResource(R.xml.fragmented_preferences);
        }
    }

    /**
     * This fragment contains a second-level set of preference that you
     * can get to by tapping an item in the first preferences fragment.
     */
    public static class Prefs1FragmentInner extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from preference XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
            //addPreferencesFromResource(R.xml.fragmented_preferences_inner);
        }
    }

    /**
     * This fragment shows the preferences for the second header.
     */
    public static class Prefs2Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from headers XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_fanins);
            
            

    		PreferenceCategory prefCat=(PreferenceCategory)findPreference("chiaveVersione");
    		prefCat.setTitle("ver: " + versionName);
    		
            
        }
    }

    public static Handler UIHandler;

    static 
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    // *************************************************************************
    // Mostra messaggio toast 
    // *************************************************************************
    public static void showToast(final String toast) {
    	MySettings.runOnUI(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(mycontext , toast, Toast.LENGTH_LONG).show();
          }
        });
      }
    
    
}