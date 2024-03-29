package com.fant.insapp.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class LineGraph{
	TimeSeries series;
	String Titolo;
	Context mycontext;
	
	
	public LineGraph(){
		int[] values = { 1, 2, 3, 4, 5 };
		int[] x = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }; // x values!
		int[] y =  { 30, 34, 45, 57, 77, 89, 100, 111 ,123 ,145 }; // y values!

		int k = 0;
		series = new TimeSeries("Linea 2"); 
		for( int i = 0; i < x.length; i++)
		{
			series.add(x[i], y[i]);
		}
		Titolo = "Demo";

	}

	public LineGraph(Context _context, String _title, TimeSeries _cat){
		Titolo = _title;
		series = _cat;
		mycontext = _context;
	}

	public Intent getIntent(Context context) {
		


		
		// Our second data
		int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }; // x values!
		int[] y2 =  { 145, 123, 111, 100, 89, 77, 57, 45, 34, 30}; // y values!
		TimeSeries series2 = new TimeSeries("Line2"); 
		for( int i = 0; i < x2.length; i++)
		{
			series2.add(x2[i], y2[i]);
		}
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series);
		//dataset.addSeries(series2);
		
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph
		XYSeriesRenderer renderer = new XYSeriesRenderer(); // This will be used to customize line 1
		//XYSeriesRenderer renderer2 = new XYSeriesRenderer(); // This will be used to customize line 2
		mRenderer.addSeriesRenderer(renderer);
		//mRenderer.addSeriesRenderer(renderer2);
		mRenderer.setBackgroundColor(Color.BLACK);
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setXLabels(25);
	    mRenderer.setYLabels(20);
	    mRenderer.setXLabelsAlign(Align.RIGHT);
	    mRenderer.setYLabelsAlign(Align.RIGHT);
	    mRenderer.setZoomButtonsVisible(true);

	    mRenderer.setChartTitle(Titolo);
	    mRenderer.setXTitle("Date");
	    mRenderer.setYTitle("Valori");
	    mRenderer.setAntialiasing(true);
	    
	    
	    
	    
	    
		// Customization time for line 1!
		renderer.setColor(mycontext.getResources().getColor(R.color.LightGreen));
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setFillPoints(true);
		renderer.setChartValuesTextSize(20);	
		renderer.setLineWidth(3);
		// Customization time for line 2!
		//renderer2.setColor(Color.YELLOW);
		//renderer2.setPointStyle(PointStyle.DIAMOND);
		//renderer2.setFillPoints(true);
		
		Intent intent = ChartFactory.getLineChartIntent(context, dataset, mRenderer, "Line Graph Title");
		return intent;
		
	}

}
