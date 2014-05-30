package com.fant.insapp.app;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ReportActivity extends ListActivity {

	static final int MY_REQUEST_MODIFY_DATA = 1;


	private MyDatabase DBINStoread;

	public static String versionName = "";
	static ListView myListActivity;

	private static SimpleCursorAdapter dataAdapter;

	private Cursor mycursor = null;
	private String querystr = "";

	private ListAdapter myadapter;

	EditText editTextDateInizio, editTextDateFine, editTextClicked;

	Context mycontext;
	Bundle mySavedInstance;	
	CategorySeries seriePie;
	TimeSeries serieLine;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Operation title bar
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);		 
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.read_title_actionbar);

		//Remove notification bar
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_report);

		mycontext = this;
		mySavedInstance = savedInstanceState;


		initializeActivity();

		if (myGlobal.statoDBLocalFull == false) {
			showToast("Errore di presenza file DB: " + myGlobal.LOCAL_FULL_DB_FILE);
			finish();
			return;
		}

		calcoloTotale();






	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.report_actions, menu);
		return super.onCreateOptionsMenu(menu);		

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.action_graph:
			LineGraph line = new LineGraph(this, "",serieLine);
			Intent lineIntent = line.getIntent(this);
			startActivity(lineIntent);			
			return true;

		case R.id.action_graph_pie:			
			PieGraph pie = new PieGraph(this, "Distribuzione Spese", seriePie);
			Intent pieIntent = pie.getIntent(this);
			startActivity(pieIntent);
			return true;
		
		case R.id.action_cat_simple:		
			calcoloTotaleCategorie();
			return true;
			
		case R.id.action_cat_gen:
            calcoloTotaleCategorieGeneriche();
            return true;
			
		case R.id.action_conti:	
			calcoloTotale();
			return true;	
		default:
			return super.onOptionsItemSelected(item);
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






	private void calcoloTotale () {

		DBINStoread = new MyDatabase(
				getApplicationContext(), 
				myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_FULL_DB_FILE);


		ArrayList<String> columnArray1 = new ArrayList<String>();
		ArrayList<String> columnArray2 = new ArrayList<String>();
		String DataInizio = "2007-01-11";
		String DataFine = "2050-12-31";

		DataInizio = editTextDateInizio.getText().toString();
		DataFine = editTextDateFine.getText().toString();


		DBINStoread.open();


		String tmpchifa;

		float ComuniIW = getResultSpesa(DataInizio, DataFine, "C", "IWBank");
		float ComuniJB = getResultSpesa(DataInizio, DataFine, "C", "JB") + getResultSpesa(DataInizio, DataFine, "C", "Fineco") + getResultSpesa(DataInizio, DataFine, "C", "LaBanque");
		float ComuniSF = getResultSpesa(DataInizio, DataFine, "C", "SF") + getResultSpesa(DataInizio, DataFine, "C", "MPS");

		float PersJBIW = getResultSpesa(DataInizio, DataFine, "JB", "IWBank");
		float PersJBJB = getResultSpesa(DataInizio, DataFine, "JB", "JB") + getResultSpesa(DataInizio, DataFine, "JB", "Fineco") + getResultSpesa(DataInizio, DataFine, "JB", "LaBanque");
		float PersJBSF = getResultSpesa(DataInizio, DataFine, "JB", "SF") + getResultSpesa(DataInizio, DataFine, "JB", "MPS");

		float PersSFIW = getResultSpesa(DataInizio, DataFine, "SF", "IWBank");
		float PersSFJB = getResultSpesa(DataInizio, DataFine, "SF", "JB") + getResultSpesa(DataInizio, DataFine, "SF", "Fineco") + getResultSpesa(DataInizio, DataFine, "SF", "LaBanque");
		float PersSFSF = getResultSpesa(DataInizio, DataFine, "SF", "SF") + getResultSpesa(DataInizio, DataFine, "SF", "MPS");


		tmpchifa = "IWBank";
		float SpostdaIWaIW = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "IWBank");
		float SpostdaIWaJB = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "JB") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "Fineco") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "LaBanque");
		float SpostdaIWaSF = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "SF") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "MPS");


		tmpchifa = "JB";
		float SpostdaJBaIW = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "IWBank");
		float SpostdaJBaJB = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "JB") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "Fineco") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "LaBanque");
		float SpostdaJBaSF = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "SF") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "MPS");

		tmpchifa = "Fineco";
		float SpostdaFinecoaIW = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "IWBank");
		float SpostdaFinecoaJB = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "JB") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "Fineco") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "LaBanque");
		float SpostdaFinecoaSF = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "SF") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "MPS");

		tmpchifa = "LaBanque";
		float SpostdaLabanqueaIW = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "IWBank");
		float SpostdaLabanqueaJB = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "JB") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "Fineco") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "LaBanque");
		float SpostdaLabanqueaSF = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "SF") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "MPS");


		float SpostdaJulieAIW = SpostdaJBaIW + SpostdaFinecoaIW + SpostdaLabanqueaIW;
		float SpostdaJulieAJulie = SpostdaJBaJB + SpostdaFinecoaJB + SpostdaLabanqueaJB;
		float SpostdaJulieASimone = SpostdaJBaSF + SpostdaFinecoaSF + SpostdaLabanqueaSF;


		tmpchifa = "SF";
		float SpostdaSFaIW = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "IWBank");
		float SpostdaSFaJB = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "JB") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "Fineco") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "LaBanque");
		float SpostdaSFaSF = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "SF") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "MPS");


		tmpchifa = "MPS";
		float SpostdaMPSaIW = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "IWBank");
		float SpostdaMPSaJB = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "JB") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "Fineco") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "LaBanque");
		float SpostdaMPSaSF = getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "SF") + getResultSpostamento(DataInizio, DataFine, "C", tmpchifa, "MPS");


		float SpostdaSimoneAIW = SpostdaSFaIW + SpostdaMPSaIW;
		float SpostdaSimoneAJulie = SpostdaSFaJB + SpostdaMPSaJB + SpostdaLabanqueaJB;
		float SpostdaSimoneASimone = SpostdaSFaSF + SpostdaMPSaSF;		


		//Spese o movimenti di soldi riguardanti spese comuni				
		//JB sp comuni  	ComuniJB 
		//JB dato a IW 		SpostdaJulieAIW
		//JB preso da IW 		SpostdaIWaJB
		// Tot messi da JB 		ComuniJB + SpostdaJulieAIW - SpostdaIWaJB;

		// SF sp comuni		
		// SF dato a IW	
		// SF preso da IW	
		// Tot messi da SF	ComuniSF + SpostdaSimoneAIW - SpostdaIWaSF


		float SFdovrebbeJB1 = ((ComuniJB + SpostdaJulieAIW - SpostdaIWaJB) - (ComuniSF + SpostdaSimoneAIW - SpostdaIWaSF)) / 2;

		// Spese o passaggi di soldi non riguardanti le spese comuni				
		// SF ha dato a JB		SpostdaSimoneAJulie	
		// SF ha pagato a JB	PersJBSF
		// Tot da SF a JB	

		// JB ha dato a SF		SpostdaJulieASimone
		// JB ha pagato a SF	PersSFJB
		// Tot da JB a SF	

		float SFdovrebbeJB2 = (SpostdaJulieASimone + PersSFJB) - (SpostdaSimoneAJulie + PersJBSF);

		float SFdeve = SFdovrebbeJB1+ SFdovrebbeJB2;
		float SFversaIW = SFdeve * 2;

		showToast("SF deve versare su IW:" + SFversaIW);

		ArrayList<ReportObject> myReportList = new ArrayList<ReportObject>();

		myReportList.add(new ReportObject("Casa" , "0"));
		myReportList.add(new ReportObject("Peso" , "1"));

		myReportList.clear();


		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("Il calcolo del dovuto",""));
		myReportList.add(new ReportObject("SF DEVE= " , myGlobal.FloatToStr(SFdeve)));
		myReportList.add(new ReportObject("SF DEVE versare su IW=" , myGlobal.FloatToStr(SFversaIW)));
		myReportList.add(new ReportObject("(valore *2)",""));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("SF deve a JB per spese comuni o IW=	" , myGlobal.FloatToStr(SFdovrebbeJB1)));
		myReportList.add(new ReportObject("calcolati facendo [Tot messi da JB]-[Tot messi da SF] e poi diviso 2 (tutte spese comuni):",""));  //((ComuniJB + SpostdaJulieAIW - SpostdaIWaJB) - (ComuniSF + SpostdaSimoneAIW - SpostdaIWaSF)) / 2;
		myReportList.add(new ReportObject("+" + "\t\tJB sp comuni",myGlobal.FloatToStr(ComuniJB)));
		myReportList.add(new ReportObject("+" + "\t\tJB dato a IW",myGlobal.FloatToStr(SpostdaJulieAIW)));
		myReportList.add(new ReportObject("-" + "\t\tJB preso da IW",myGlobal.FloatToStr(SpostdaIWaJB)));
		myReportList.add(new ReportObject("[Tot messi da JB]= " , myGlobal.FloatToStr((ComuniJB + SpostdaJulieAIW - SpostdaIWaJB))));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("+" + "\t\tSF sp comuni",myGlobal.FloatToStr(ComuniSF)));
		myReportList.add(new ReportObject("+" +  "\t\tSF dato a IW",myGlobal.FloatToStr(SpostdaSimoneAIW)));		
		myReportList.add(new ReportObject("-"  + "\t\tSF preso da IW",myGlobal.FloatToStr(SpostdaIWaSF)));
		myReportList.add(new ReportObject("[Tot messi da SF]= 	" , myGlobal.FloatToStr((ComuniSF + SpostdaSimoneAIW - SpostdaIWaSF))));		
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("SF dovrebbe a JB per scambi diretti o personali=	" , myGlobal.FloatToStr(SFdovrebbeJB2)));
		myReportList.add(new ReportObject("calcolati facendo [Spost JB->SF] - [Spost SF->JB] :",""));  //(SpostdaJulieASimone + PersSFJB) - (SpostdaSimoneAJulie + PersJBSF);
		myReportList.add(new ReportObject("+" +  "\t\tJB ha dato a SF",myGlobal.FloatToStr(SpostdaJulieASimone)));
		myReportList.add(new ReportObject("+"  + "\t\tJB ha pagato pers SF",myGlobal.FloatToStr(PersSFJB)));
		myReportList.add(new ReportObject("[Spost SF->JB]= 			" , myGlobal.FloatToStr((SpostdaJulieASimone + PersSFJB))));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("+"  + "\t\tSF ha dato a JB",myGlobal.FloatToStr(SpostdaSimoneAJulie)));
		myReportList.add(new ReportObject("+" + "\t\tSF ha pagato pers JB ",myGlobal.FloatToStr(PersJBSF) ));
		myReportList.add(new ReportObject("[Spost JB->SF]= 			" , myGlobal.FloatToStr((SpostdaSimoneAJulie + PersJBSF))));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("PARZIALI",""));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("Sp Comuni fatte da:",""));
		myReportList.add(new ReportObject("IW",myGlobal.FloatToStr(ComuniIW)));
		myReportList.add(new ReportObject("JB",myGlobal.FloatToStr(ComuniJB)));
		myReportList.add(new ReportObject("SF",myGlobal.FloatToStr(ComuniSF)));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("Pers JB    fatte da:",""));
		myReportList.add(new ReportObject("IW",myGlobal.FloatToStr(PersJBIW)));
		myReportList.add(new ReportObject("JB",myGlobal.FloatToStr(PersJBJB)));
		myReportList.add(new ReportObject("SF",myGlobal.FloatToStr(PersJBSF)));
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("Pers SF    fatte da:",""));
		myReportList.add(new ReportObject("IW",myGlobal.FloatToStr(PersSFIW)));
		myReportList.add(new ReportObject("JB",myGlobal.FloatToStr(PersSFJB)));
		myReportList.add(new ReportObject("SF",myGlobal.FloatToStr(PersSFSF)));			
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("Spost da IW   verso:",""));
		myReportList.add(new ReportObject("IW",myGlobal.FloatToStr(SpostdaIWaIW)));
		myReportList.add(new ReportObject("JB",myGlobal.FloatToStr(SpostdaIWaJB)));
		myReportList.add(new ReportObject("SF",myGlobal.FloatToStr(SpostdaIWaSF)));		
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("Spost da JB  verso:",""));
		myReportList.add(new ReportObject("IW",myGlobal.FloatToStr(SpostdaJulieAIW)));
		myReportList.add(new ReportObject("JB",myGlobal.FloatToStr(SpostdaJulieAJulie)));
		myReportList.add(new ReportObject("SF",myGlobal.FloatToStr(SpostdaJulieASimone)));		
		myReportList.add(new ReportObject("",""));
		myReportList.add(new ReportObject("Spost da SF  verso:",""));
		myReportList.add(new ReportObject("IW",myGlobal.FloatToStr(SpostdaSimoneAIW)));
		myReportList.add(new ReportObject("JB",myGlobal.FloatToStr(SpostdaSimoneAJulie)));
		myReportList.add(new ReportObject("SF",myGlobal.FloatToStr(SpostdaSimoneASimone)));		

		/*		myadapter = new ArrayAdapter<String>(
				this,
				R.layout.list_report,
				R.id.descrizioneReport,
				columnArray1
				);
		setListAdapter(myadapter);*/



		ReportAdapter myReportAdapter = new ReportAdapter(this, myReportList);
		setListAdapter(myReportAdapter);





		/// Impostazione delle serie per i grafici
		seriePie = new CategorySeries("Spese Comuni fatte dai soggetti");
		seriePie.add("Spese IW", ComuniIW);
		seriePie.add("Spese JB", ComuniJB);
		seriePie.add("Spese SF", ComuniSF);

		//getResultSpesa(DataInizio, DataFine, "C", "IWBank");

		String[] datestr = DataInizio.toString().split("-");
		int yearInizio = Integer.valueOf(datestr[0]);
		int monthInizio = Integer.valueOf(datestr[1]);

		datestr = DataFine.toString().split("-");
		int yearFine = Integer.valueOf(datestr[0]);
		int monthFine = Integer.valueOf(datestr[1]);

		serieLine = new TimeSeries("Andamento spese comuni");

		int cntColonna = 1;                
		for (int cntanno = yearInizio; cntanno <= yearFine; cntanno++)
		{            
			double _totAnno = 0;
			for (int i = 1; i <= 12; i++)
			{
				Calendar cal = new GregorianCalendar(cntanno, i, 1);
				Date datepoint = cal.getTime();
				double _totmese = 0;                        
				_totmese += getTotMeseCatGenerica(cntanno, i, "C", "");
				_totAnno += _totmese;

				serieLine.add(cntColonna,(double)_totmese);
				serieLine.addAnnotation(String.valueOf(cntanno) + "/" + String.valueOf(i) + System.getProperty("line.separator")+ myGlobal.FloatToStr((float)_totmese) ,cntColonna,(double)_totmese);

				cntColonna++;
			}


		}


		DBINStoread.close();

	}


	private void calcoloTotaleCategorie () {

		DBINStoread = new MyDatabase(
				getApplicationContext(), 
				myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_FULL_DB_FILE);


		String DataInizio = "2007-01-11";
		String DataFine = "2050-12-31";

		DataInizio = editTextDateInizio.getText().toString();
		DataFine = editTextDateFine.getText().toString();


		DBINStoread.open();


		ArrayList<ReportObject> myReportList = new ArrayList<ReportObject>();



		myReportList.clear();
		myReportList.add(new ReportObject("",""));


		String[] datestr = DataInizio.toString().split("-");
		int yearInizio = Integer.valueOf(datestr[0]);
		int monthInizio = Integer.valueOf(datestr[1]);

		datestr = DataFine.toString().split("-");
		int yearFine = Integer.valueOf(datestr[0]);
		int monthFine = Integer.valueOf(datestr[1]);

		for (int i = 0; i < myGlobal.arrCategoria.length; i++)			
		{
			double _totAnno = getTotPeriodoCategoria(yearInizio, 1, yearFine, 12, "", myGlobal.arrCategoria[i]);
			myReportList.add(new ReportObject(String.valueOf(yearInizio) + "-" + String.valueOf(yearFine) + " " + myGlobal.arrCategoria[i], myGlobal.FloatToStr((float)_totAnno)));
		}

		myReportList.add(new ReportObject("",""));

		ReportAdapter myReportAdapter = new ReportAdapter(this, myReportList);
		setListAdapter(myReportAdapter);

		DBINStoread.close();

	}


    private void calcoloTotaleCategorieGeneriche () {

        DBINStoread = new MyDatabase(
                getApplicationContext(),
                myGlobal.getStorageDatabaseFantDir().getPath() + java.io.File.separator + myGlobal.LOCAL_FULL_DB_FILE);


        String DataInizio = "2007-01-11";
        String DataFine = "2050-12-31";

        DataInizio = editTextDateInizio.getText().toString();
        DataFine = editTextDateFine.getText().toString();


        DBINStoread.open();


        ArrayList<ReportObject> myReportList = new ArrayList<ReportObject>();



        myReportList.clear();
        myReportList.add(new ReportObject("",""));


        String[] datestr = DataInizio.toString().split("-");
        int yearInizio = Integer.valueOf(datestr[0]);
        int monthInizio = Integer.valueOf(datestr[1]);

        datestr = DataFine.toString().split("-");
        int yearFine = Integer.valueOf(datestr[0]);
        int monthFine = Integer.valueOf(datestr[1]);

        for (int i = 0; i < myGlobal.arrGenerica.length; i++)
        {
            double _totAnno = getTotPeriodoCategoriaGenerica(yearInizio, 1, yearFine, 12, "", myGlobal.arrGenerica[i]);
            myReportList.add(new ReportObject(String.valueOf(yearInizio) + "-" + String.valueOf(yearFine) + " " + myGlobal.arrGenerica[i], myGlobal.FloatToStr((float)_totAnno)));
        }

        myReportList.add(new ReportObject("",""));

        ReportAdapter myReportAdapter = new ReportAdapter(this, myReportList);
        setListAdapter(myReportAdapter);

        DBINStoread.close();

    }

    protected float getResultSpesa(String _DataInizio, String _DataFine, String _queryCPers, String _queryChiFa){
		String _queryTipoOperazione = "Spesa";

		querystr = "SELECT SUM(Valore) AS Total FROM " + MyDatabase.DataINStable.TABELLA_INSDATA + " WHERE " + 
				" (" + MyDatabase.DataINStable.DATA_OPERAZIONE_KEY + ">='" + _DataInizio + "' AND " + MyDatabase.DataINStable.DATA_OPERAZIONE_KEY + "<='"+ _DataFine + "') AND " +
				" " + MyDatabase.DataINStable.TIPO_OPERAZIONE_KEY + "='" + _queryTipoOperazione+"' COLLATE NOCASE AND " +
				MyDatabase.DataINStable.C_PERS_KEY + "='" + _queryCPers + "' COLLATE NOCASE AND "+
				MyDatabase.DataINStable.CHI_FA_KEY + "='" + _queryChiFa + "' COLLATE NOCASE " ; 
		mycursor = DBINStoread.rawQuery(querystr,  null );
		if (mycursor.getCount() == 0) {
			return 0;
		} else {
			mycursor.moveToFirst();
			if (mycursor.getString(mycursor.getColumnIndex("Total")) == null) 
				return 0;
			String str = mycursor.getString(mycursor.getColumnIndex("Total"));
			float retval;

			retval = Float.valueOf(str);
			return retval;			
		}
	}


	private float getResultSpostamento(String _DataInizio, String _DataFine, String _queryCPers, String _queryChiFa, String _queryAda){
		String _queryTipoOperazione = "Spostamento";
		querystr = "SELECT SUM(Valore) AS Total FROM " + MyDatabase.DataINStable.TABELLA_INSDATA + " WHERE " + 
				" (" + MyDatabase.DataINStable.DATA_OPERAZIONE_KEY + ">='" + _DataInizio + "' AND " + MyDatabase.DataINStable.DATA_OPERAZIONE_KEY + "<='"+ _DataFine + "') AND " +
				" " + MyDatabase.DataINStable.TIPO_OPERAZIONE_KEY + "='" + _queryTipoOperazione+"' COLLATE NOCASE AND " + 
				MyDatabase.DataINStable.C_PERS_KEY + "='" + _queryCPers + "' COLLATE NOCASE AND "+
				MyDatabase.DataINStable.CHI_FA_KEY + "='" + _queryChiFa + "' COLLATE NOCASE AND "+ 
				MyDatabase.DataINStable.A_DA_KEY + "='" + _queryAda + "' COLLATE NOCASE ";
		mycursor = DBINStoread.rawQuery(querystr,  null );
		if (mycursor.getCount() == 0) {
			return 0;
		} else {
			mycursor.moveToFirst();
			if (mycursor.getString(mycursor.getColumnIndex("Total")) == null) 
				return 0;			
			String str = mycursor.getString(mycursor.getColumnIndex("Total"));
			float retval;

			retval = Float.valueOf(str);
			return retval;			
		}
	}


	
	
	
	
	

	double getTotAnnoCategoria(int _anno, String _CatGenerica)
	{
		double _res = 0;

		for (int i = 1; i <= 12; i++)
		{
			_res += getTotMeseCatGenerica(_anno, i, "C", _CatGenerica);
		}
		return _res;
	}


	double getTotMeseCategoria(int _anno, int _mese, String _CPers, String _Categoria)
	{        
		int _annoend = _anno, _meseend = _mese;


		if (_mese == 12) {
			_annoend = _anno + 1;
			_meseend = 1;
		} else {
			_annoend = _anno;
			_meseend = _mese + 1;
		}


		String querystr = "SELECT SUM(CAST(Valore AS REAL)) AS Total FROM myINSData WHERE " +
				" (DataOperazione>='" + myGlobal.intToString(_anno,4) + "-" + myGlobal.intToString(_mese,2) + "-01' AND DataOperazione<'" + myGlobal.intToString(_annoend,2) + "-" + myGlobal.intToString(_meseend,2) + "-01') AND " +
				"TipoOperazione='Spesa' " ;
		if (_CPers != "")
		{
			querystr += " AND CPers='" + _CPers + "'";
		}
		if (_Categoria != "")
		{
			querystr += " AND Categoria='" + _Categoria + "'";
		}

		mycursor = DBINStoread.rawQuery(querystr,  null );
		if (mycursor.getCount() == 0) {
			return 0;
		} else {
			mycursor.moveToFirst();
			if (mycursor.getString(mycursor.getColumnIndex("Total")) == null) 
				return 0;			
			String str = mycursor.getString(mycursor.getColumnIndex("Total"));
			double retval;

			retval = Float.valueOf(str);
			return retval;			
		}

	}
	
	

	double getTotPeriodoCategoria(int _annoStart, int _meseStart, int _annoStop, int _meseStop, String _CPers, String _Categoria)
	{        
		int _annoend = _annoStop, _meseend = _meseStop;


		if (_meseend == 12) {
			_annoend = _annoend + 1;
			_meseend = 1;
		} 
		String querystr = "SELECT SUM(CAST(Valore AS REAL)) AS Total FROM myINSData WHERE " +
				" (DataOperazione>='" + myGlobal.intToString(_annoStart,4) + "-" + myGlobal.intToString(_meseStart,2) + "-01' AND DataOperazione<'" + myGlobal.intToString(_annoend,2) + "-" + myGlobal.intToString(_meseend,2) + "-01') AND " +
				"TipoOperazione='Spesa' " ;
		if (_CPers != "")
		{
			querystr += " AND CPers='" + _CPers + "'";
		}
		if (_Categoria != "")
		{
			querystr += " AND Categoria='" + _Categoria + "'";
		}

		mycursor = DBINStoread.rawQuery(querystr,  null );
		if (mycursor.getCount() == 0) {
			return 0;
		} else {
			mycursor.moveToFirst();
			if (mycursor.getString(mycursor.getColumnIndex("Total")) == null) 
				return 0;			
			String str = mycursor.getString(mycursor.getColumnIndex("Total"));
			double retval;

			retval = Float.valueOf(str);
			return retval;			
		}

	}



    double getTotPeriodoCategoriaGenerica(int _annoStart, int _meseStart, int _annoStop, int _meseStop, String _CPers, String _CatGenerica)
    {
        int _annoend = _annoStop, _meseend = _meseStop;


        if (_meseend == 12) {
            _annoend = _annoend + 1;
            _meseend = 1;
        }
        String querystr = "SELECT SUM(CAST(Valore AS REAL)) AS Total FROM myINSData WHERE " +
                " (DataOperazione>='" + myGlobal.intToString(_annoStart,4) + "-" + myGlobal.intToString(_meseStart,2) + "-01' AND DataOperazione<'" + myGlobal.intToString(_annoend,2) + "-" + myGlobal.intToString(_meseend,2) + "-01') AND " +
                "TipoOperazione='Spesa' " ;
        if (_CPers != "")
        {
            querystr += " AND CPers='" + _CPers + "'";
        }
        if (_CatGenerica != "")
        {
            querystr += " AND Generica='" + _CatGenerica + "'";
        }


        mycursor = DBINStoread.rawQuery(querystr,  null );
        if (mycursor.getCount() == 0) {
            return 0;
        } else {
            mycursor.moveToFirst();
            if (mycursor.getString(mycursor.getColumnIndex("Total")) == null)
                return 0;
            String str = mycursor.getString(mycursor.getColumnIndex("Total"));
            double retval;

            retval = Float.valueOf(str);
            return retval;
        }

    }

	double getTotAnnoCatGenerica(int _anno, String _CatGenerica)
	{
		double _res = 0;

		for (int i = 1; i <= 12; i++)
		{
			_res += getTotMeseCatGenerica(_anno, i, "C", _CatGenerica);
		}
		return _res;
	}


	double getTotMeseCatGenerica(int _anno, int _mese, String _CPers, String _CatGenerica)
	{        
		int _annoend = _anno, _meseend = _mese;


		if (_mese == 12) {
			_annoend = _anno + 1;
			_meseend = 1;
		} else {
			_annoend = _anno;
			_meseend = _mese + 1;
		}

		String querystr = "SELECT SUM(CAST(Valore AS REAL)) AS Total FROM myINSData WHERE " +
				" (DataOperazione>='" + myGlobal.intToString(_anno,4) + "-" + myGlobal.intToString(_mese,2) + "-01' AND DataOperazione<'" + myGlobal.intToString(_annoend,2) + "-" + myGlobal.intToString(_meseend,2) + "-01') AND " +
				"TipoOperazione='Spesa' " ;
		if (_CPers != "")
		{
			querystr += " AND CPers='" + _CPers + "'";
		}
		if (_CatGenerica != "")
		{
			querystr += " AND Generica='" + _CatGenerica + "'";
		}

		mycursor = DBINStoread.rawQuery(querystr,  null );
		if (mycursor.getCount() == 0) {
			return 0;
		} else {
			mycursor.moveToFirst();
			if (mycursor.getString(mycursor.getColumnIndex("Total")) == null) 
				return 0;			
			String str = mycursor.getString(mycursor.getColumnIndex("Total"));
			double retval;

			retval = Float.valueOf(str);
			return retval;			
		}

	}



	class ClickDataButtonInizio implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {    		
			if (MotionEvent.ACTION_UP == event.getAction()) {
				editTextClicked = editTextDateInizio;	// oggetto da impostare in callback
				selezionaData();
			}
			return false;
		}
	};

	class ClickDataButtonFine implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {    		
			if (MotionEvent.ACTION_UP == event.getAction()) {
				editTextClicked = editTextDateFine;	// oggetto da impostare in callback
				selezionaData();
			}
			return false;
		}
	};

	private void initializeActivity() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
		String formattedDateOnly = df.format( c.getTime());

		editTextDateInizio = (EditText) findViewById(R.id.DataInizioReport);
		editTextDateFine = (EditText) findViewById(R.id.DataFineReport);

		editTextDateFine.setText(formattedDateOnly);


		c.set(Calendar.DAY_OF_YEAR, 1);
		c.set(Calendar.YEAR, 2007);

		formattedDateOnly = df.format( c.getTime());	    
		editTextDateInizio.setText(formattedDateOnly);

		editTextDateInizio.setOnTouchListener(new ClickDataButtonInizio());
		editTextDateFine.setOnTouchListener(new ClickDataButtonFine());

	}


	private void selezionaData(){
		int year, month, day;

		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

		String[] datestr = editTextClicked.getText().toString().split("-");

		year = Integer.valueOf(datestr[0]);
		month = Integer.valueOf(datestr[1])-1;        // Calendar di Java ha il mese che parte da 0 e non da 1
		day = Integer.valueOf(datestr[2]);


		Dialog dd = new DatePickerDialog(mycontext, myDateSetListener, year, month, day);
		dd.show();
	}


	// questa è la Callback che indica che l'utente ha finito di scegliere la data
	OnDateSetListener myDateSetListener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker datePicker, int year, int month, int day) {

			//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);	    	
			//String formattedDate = df.format(new Date(year-1900, month, day));

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.DAY_OF_MONTH, day);
			cal.set(Calendar.MONTH, month);
			String format = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(cal.getTime());

			editTextClicked.setText(format);

			calcoloTotale();
		}
	};

}


