package com.fant.insapp.app;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

public class MyDatabase {  

        SQLiteDatabase mDb;
        DbHelper mDbHelper;
        Context mContext;
        DropboxAPI<AndroidAuthSession> mApiPar;
        String parDBName;
        boolean downloadifcreate = false;
    	
        
        
        // file di default
        private static final String DB_DEFAULT_NAME= myGlobal.getStorageFantDir().getPath() + java.io.File.separator +  "INSbase.sqlite";//nome del db
        private static final int DB_VERSION=1; //numero di versione del nostro db

        private static final String TYPE_DB_STRING = " TEXT COLLATE RTRIM";
        
        public MyDatabase(Context ctx){
                mContext=ctx;
                mDbHelper=new DbHelper(ctx, DB_DEFAULT_NAME, null, DB_VERSION);   //quando istanziamo questa classe, istanziamo anche l'helper (vedi sotto)
                downloadifcreate = false;
        }

        // Costruttore con parametro DB_NAME
        public MyDatabase(Context ctx, String strDBNAME){
        	parDBName=strDBNAME;
            mContext=ctx;
            mDbHelper=new DbHelper(ctx, strDBNAME, null, DB_VERSION);   //quando istanziamo questa classe, istanziamo anche l'helper (vedi sotto)
            downloadifcreate = false;
        }

  
        public void open(){  //il database su cui agiamo è leggibile/scrivibile
        	// Richiamare questo metodo significa rendere scrivibile il database
        	// Se non esiste automaticamente scatena onCreate di SQLiteOpenHelper
        	mDb=mDbHelper.getWritableDatabase();                
        }
        
        public void close(){ //chiudiamo il database su cui agiamo
                mDb.close();
        }

        static class DataINStable {  // i metadati della tabella, accessibili ovunque
            static final String TABELLA_INSDATA = "myINSData";
            static final String TABELLA_CATEGORIE = "Definizioni_Categoria";
            static final String TABELLA_ADA = "Definizioni_ADa";
            static final String TABELLA_CPERSONALI = "Definizioni_CPers";
            static final String TABELLA_CHIFA = "Definizioni_ChiFa";
            static final String TABELLA_TIPOOPERAZIONE = "Definizioni_TipoOperazione";
            
            static final String ID = "_id";
            static final String DATA_OPERAZIONE_KEY = "DataOperazione";                
            static final String TIPO_OPERAZIONE_KEY = "TipoOperazione";
            static final String CHI_FA_KEY = "ChiFa";
            static final String A_DA_KEY = "ADa";
            static final String C_PERS_KEY = "CPers";
            static final String VALORE_KEY = "Valore";
            static final String CATEGORIA_KEY = "Categoria";
            static final String GENERICA_KEY = "Generica";
            static final String DESCRIZIONE_KEY = "Descrizione";
            static final String NOTE_KEY = "Note";
            static final String SPECIAL_NOTE_KEY = "SpecialNote";
            
    }
        
        private static final String TABELLA_INSDATA_CREATE = "CREATE TABLE IF NOT EXISTS "  //codice sql di creazione della tabella
                + DataINStable.TABELLA_INSDATA + " ("
                + DataINStable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
                + DataINStable.DATA_OPERAZIONE_KEY + TYPE_DB_STRING + ", "
                + DataINStable.TIPO_OPERAZIONE_KEY + TYPE_DB_STRING + ", "
                + DataINStable.CHI_FA_KEY + TYPE_DB_STRING + ", "
                + DataINStable.A_DA_KEY + TYPE_DB_STRING + ", "
                + DataINStable.C_PERS_KEY + TYPE_DB_STRING + ", "
                + DataINStable.VALORE_KEY + TYPE_DB_STRING + ","
                + DataINStable.CATEGORIA_KEY + TYPE_DB_STRING + ", "
                + DataINStable.GENERICA_KEY + TYPE_DB_STRING + ", "
                + DataINStable.DESCRIZIONE_KEY + TYPE_DB_STRING + ", "                
                + DataINStable.NOTE_KEY + TYPE_DB_STRING + ", "
                + DataINStable.SPECIAL_NOTE_KEY + TYPE_DB_STRING + ");" ;
                
                
        
        //i seguenti metodi servono per la lettura/scrittura del db. aggiungete e modificate a discrezione
        
        public void insertRecordDataIns(String _valData, String _valTipoOper, String _valChiFa, String _valADa, 
        		String _valPersonale, String _valValore, String _valCategoria, String _valDescrizione, String _valNote, String _valspecialNote){ //metodo per inserire i dati
                ContentValues cv=new ContentValues();
                cv.put(DataINStable.DATA_OPERAZIONE_KEY, _valData);
                cv.put(DataINStable.TIPO_OPERAZIONE_KEY, _valTipoOper);
                cv.put(DataINStable.CHI_FA_KEY, _valChiFa);
                cv.put(DataINStable.A_DA_KEY, _valADa);
                cv.put(DataINStable.C_PERS_KEY, _valPersonale);
                cv.put(DataINStable.VALORE_KEY, _valValore);
                cv.put(DataINStable.CATEGORIA_KEY, _valCategoria);
                cv.put(DataINStable.GENERICA_KEY, "");
                cv.put(DataINStable.DESCRIZIONE_KEY, _valDescrizione);
                cv.put(DataINStable.NOTE_KEY, _valNote);
                cv.put(DataINStable.SPECIAL_NOTE_KEY, _valspecialNote);

                mDb.insert(DataINStable.TABELLA_INSDATA, null, cv);
        }

        
        public int updateRecordDataIns(String _IDval, String _valData, String _valTipoOper, String _valChiFa, String _valADa, 
        		String _valPersonale, String _valValore, String _valCategoria, String _valDescrizione, String _valNote, String _valspecialNote){ //metodo per inserire i dati
                ContentValues cv=new ContentValues();
                cv.put(DataINStable.DATA_OPERAZIONE_KEY, _valData);
                cv.put(DataINStable.TIPO_OPERAZIONE_KEY, _valTipoOper);
                cv.put(DataINStable.CHI_FA_KEY, _valChiFa);
                cv.put(DataINStable.A_DA_KEY, _valADa);
                cv.put(DataINStable.C_PERS_KEY, _valPersonale);
                cv.put(DataINStable.VALORE_KEY, _valValore);
                cv.put(DataINStable.CATEGORIA_KEY, _valCategoria);
                cv.put(DataINStable.GENERICA_KEY, "");
                cv.put(DataINStable.DESCRIZIONE_KEY, _valDescrizione);
                cv.put(DataINStable.NOTE_KEY, _valNote);
                cv.put(DataINStable.SPECIAL_NOTE_KEY, _valspecialNote);

                //
                return mDb.update(DataINStable.TABELLA_INSDATA, cv, DataINStable.ID + "=" + _IDval, null);
        }
        
        //---deletes a particular title---
        public boolean deleteDatabyID(String _IDval) 
        {
            return mDb.delete(DataINStable.TABELLA_INSDATA,
            		DataINStable.ID + "=" + _IDval
            		, null) > 0;
        }      
        
        public Cursor fetchDati(){ //metodo per fare la query di tutti i dati
                return mDb.query(DataINStable.TABELLA_INSDATA, null,null,null,null,null,null);                
        }
        
    
        public Cursor rawQuery(String _sqlQuery, String[] _args){ //metodo per fare la query di tutti i dati
            return mDb.rawQuery(_sqlQuery, _args);
        }

        //Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
        public void execSQLsimple(String _sqlStr) throws SQLException {        	
        	mDb.execSQL(_sqlStr);        	
        }
        
        //Execute a single SQL statement that is NOT a SELECT/INSERT/UPDATE/DELETE.
        public void execSQLarg(String _sqlStr, String[] _selectionArgs){ 
            mDb.execSQL(_sqlStr, _selectionArgs);                
        } 
        
        public List<String> fetchValori(String _tabella){ //metodo per fare la query di tutti i dati
            List<String> _myls = new ArrayList<String>();
        	/*
            static final String TABELLA_CATEGORIE = "Definizioni_Categoria";
            static final String TABELLA_ADA = "Definizioni_ADa";
            static final String TABELLA_CPERSONALI = "Definizioni_CPers";
            static final String TABELLA_CHIFA = "Definizioni_ChiFa";
            static final String TABELLA_TIPOOPERAZIONE = "Definizioni_TipoOperazione";
            */
        	Cursor mycurs = mDb.query(_tabella, null,null,null,null,null,null);
        	
        	if (mycurs.moveToFirst()) {
        	    do {
        	    	_myls.add(mycurs.getString(mycurs.getColumnIndex("Valori")));
        	    } while (mycurs.moveToNext());               
        	}
        	return _myls;
        }
        
        
        

        private class DbHelper extends SQLiteOpenHelper { //classe che ci aiuta nella creazione del db

                public DbHelper(Context context, String name, CursorFactory factory,int version) {
                        super(context, name, factory, version);
                }

                @Override
                public void onCreate(SQLiteDatabase _db) { //solo quando il db viene creato, creiamo la tabella

                	
                	//_db.execSQL(TABELLA_INSDATA_CREATE);
                	
                }

                @Override
                public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
                        //qui mettiamo eventuali modifiche al db, se nella nostra nuova versione della app, il db cambia numero di versione

                }
                
        
        }
                

}