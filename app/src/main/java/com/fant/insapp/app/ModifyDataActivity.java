package com.fant.insapp.app;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyDataActivity extends FragmentActivity {


    

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

	    				
	private String valData, valTipoOper, valChiFa, valADa, valPersonale, valValore, valCategoria, valDescrizione, valNote;
	
	
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
	TextView textTitle;
	
	
	

    // *************************************************************************
	// OnCreate
    // *************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        //Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_main);

        Spinner spinner;
		ArrayAdapter<CharSequence> adapter;
		
		textTitle = (TextView) findViewById(R.id.textViewTitle);
		textTitle.setText("Modifica Dati");

		
		EditText editTextData = (EditText) findViewById(R.id.TextData);
		editTextData.setOnTouchListener(new ClickDataButton());
					
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
        adapterCat = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, myGlobal.arrCategoria);		

        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategoria.setAdapter(adapterCat);
        spinCategoria.setOnItemSelectedListener(new SelectSpinAutocomplete());

        adapterCatTxt = new ArrayAdapter<String>(this,   android.R.layout.simple_expandable_list_item_1, myGlobal.arrCategoria);
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
        spinner.setOnTouchListener(spinnerOnTouch);
        // Imposto Listener solo se clicco sullo Spinner per non resettare valore in ingresso
        // spinADa.setOnItemSelectedListener(new SelectSpinAutocomplete());

        textADa = (AutoCompleteTextView) findViewById(R.id.TextAutocompleteADa);        	
        adapterADaTxt = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, myGlobal.arrADa);
        textADa.setAdapter(adapterADaTxt);
        textADa.setOnFocusChangeListener(new ChangeFocusAutoComplete());
        textADa.setValidator(new ValidateADa());



        final ImageButton buttonOK = (ImageButton) findViewById(R.id.imgbtnOK);		
        buttonOK.setOnClickListener(new ClickOKButton());

        final ImageButton buttonReset = (ImageButton)  findViewById(R.id.imgbtnReset);
        buttonReset.setOnClickListener(new ClickResetButton());

        initTextValue();
    


    }




	private View.OnTouchListener spinnerOnTouch = new View.OnTouchListener() {
	    public boolean onTouch(View v, MotionEvent event) {
	        if (event.getAction() == MotionEvent.ACTION_UP) {
	            // solo alla pressione dello Spinner attivo l'autocompletamento
	        	// altrimenti già all'avvio rischio di farlo partire cancellando il valore nella textView
	        	spinADa.setOnItemSelectedListener(new SelectSpinAutocomplete());
	        }
	        return false;
	    }
	};


	 


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


    	case R.id.action_uploadDB:        	
    		return true;

	
    		


    	case R.id.action_sync_INS_temp:

    		return true;


    	case R.id.action_authDropbox:        
    		return true;

    	case R.id.action_settings:
    		showToast("Menu setting not available");
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
    		showToast("Valore è Sbagliato");
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
            // Perform action on click
        	final EditText editTextData = (EditText) findViewById(R.id.TextData);
        	valData = editTextData.getText().toString();

        	final Spinner editTextTipoOper = (Spinner) findViewById(R.id.SpinnerTipoOper);    				
        	valTipoOper = editTextTipoOper.getSelectedItem().toString();

        	final Spinner editTextChiFa = (Spinner) findViewById(R.id.SpinnerChiFa);    				
        	valChiFa = editTextChiFa.getSelectedItem().toString();

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
        		
        		Intent resultDataIntent = new Intent();
        		resultDataIntent.putExtra("valueName", "valueData");
        		
        		
	    		// Passo parametri a nuova activity, con lo stesso nome della colonna DataBase
        		resultDataIntent.putExtra(MyDatabase.DataINStable.DATA_OPERAZIONE_KEY,  valData);	    				
        		resultDataIntent.putExtra(MyDatabase.DataINStable.CHI_FA_KEY, valChiFa);
        		resultDataIntent.putExtra(MyDatabase.DataINStable.TIPO_OPERAZIONE_KEY, valTipoOper);
        		resultDataIntent.putExtra(MyDatabase.DataINStable.A_DA_KEY,  valADa);
        		resultDataIntent.putExtra(MyDatabase.DataINStable.C_PERS_KEY, valPersonale); 
        		resultDataIntent.putExtra(MyDatabase.DataINStable.VALORE_KEY, valValore);
        		resultDataIntent.putExtra(MyDatabase.DataINStable.CATEGORIA_KEY, valCategoria);
        		resultDataIntent.putExtra(MyDatabase.DataINStable.DESCRIZIONE_KEY, valDescrizione);
        		resultDataIntent.putExtra(MyDatabase.DataINStable.NOTE_KEY, valNote); 

        		setResult(ModifyDataActivity.RESULT_OK, resultDataIntent);
        		finish();
        		
        	} else {
        		showToast("Dati non corretti nessun file caricato");
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
					while (!charCmpIsDifferent && (posch<myGlobal.arrCategoria[numline].length()) && (posch<invalidText.length())) {
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
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
			//Date selDate = new Date(year, month, day);
			
			String formattedDate = df.format(new Date(year-1900, month, day));
			
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

    
    // *************************************************************************
    // Inizializza valori con quelli passati dall'activity
    // *************************************************************************  
    public void initTextValue() {
 
		Spinner spinner;
		EditText myeditText;

		// recupero info extra e decido qual DB usare
		Bundle bun = getIntent().getExtras();
		
		myeditText = (EditText) findViewById(R.id.TextData);
		myeditText.setText(bun.getString(MyDatabase.DataINStable.DATA_OPERAZIONE_KEY));
		
		myeditText = (EditText) findViewById(R.id.TextDescrizione);
		myeditText.setText(bun.getString(MyDatabase.DataINStable.DESCRIZIONE_KEY));
	
		myeditText = (EditText) findViewById(R.id.TextValore);
		myeditText.setText(bun.getString(MyDatabase.DataINStable.VALORE_KEY));

		myeditText = (EditText) findViewById(R.id.TextNote);
		myeditText.setText(bun.getString(MyDatabase.DataINStable.NOTE_KEY));
		
	
		ArrayAdapter<String> myAdapStr;
		ArrayAdapter<CharSequence> myAdapChseq;
		int spinnerPosition;

		spinner = (Spinner) findViewById(R.id.SpinnerTipoOper);
		myAdapStr = (ArrayAdapter<String>) spinner.getAdapter(); 
		spinnerPosition = myAdapStr.getPosition(bun.getString(MyDatabase.DataINStable.TIPO_OPERAZIONE_KEY));
		spinner.setSelection(spinnerPosition);
			
				 
		spinner = (Spinner) findViewById(R.id.SpinnerChiFa);
		myAdapChseq = (ArrayAdapter<CharSequence>) spinner.getAdapter();
		spinnerPosition = myAdapChseq.getPosition(bun.getString(MyDatabase.DataINStable.CHI_FA_KEY));
		spinner.setSelection(spinnerPosition);
		

		spinner = (Spinner) findViewById(R.id.SpinnerPersonale);
		myAdapChseq = (ArrayAdapter<CharSequence>) spinner.getAdapter();
		spinnerPosition = myAdapChseq.getPosition(bun.getString(MyDatabase.DataINStable.C_PERS_KEY));
		spinner.setSelection(spinnerPosition);


		// Categoria 
		spinCategoria  = (Spinner) findViewById(R.id.SpinnerCategoria);
		spinnerPosition = adapterCat.getPosition(bun.getString(MyDatabase.DataINStable.CATEGORIA_KEY));
		spinCategoria.setSelection(spinnerPosition);


		// A/Da
		textADa.setText(bun.getString(MyDatabase.DataINStable.A_DA_KEY));
		//myeditText = (EditText) findViewById(R.id.TextAutocompleteADa);
		//myeditText.setText(bun.getString(MyDatabase.DataINStable.A_DA_KEY));
		
		
		//spinADa.setOnItemSelectedListener(new SelectSpinAutocomplete());
		
		/*
		spinADa = (Spinner) findViewById(R.id.SpinnerADa);
		myAdapChseq = (ArrayAdapter<CharSequence>) spinner.getAdapter();
		spinnerPosition = myAdapChseq.getPosition(bun.getString(MyDatabase.DataINStable.A_DA_KEY));
		spinner.setSelection(spinnerPosition);
		*/
		
		

    }





}
