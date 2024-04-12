package org.tobi;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;

import java.util.List;

public class LineChart extends ApplicationFrame {

    private LineChart(String chartTitle, XYDataset dataset, String xLabel, String yLabel) {
        super(chartTitle);
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                chartTitle,
                xLabel,yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,true,false);

        ChartPanel chartPanel = new ChartPanel( lineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        setContentPane( chartPanel );
    }

    private static XYDataset createDataset(List<Double> x, List<Double> y, String seriesName) {
        final XYSeries series = new XYSeries(seriesName);
        int size = x.size();

        for (int i = 0; i < size; i++) {
            series.add(x.get(i), y.get(i));
        }

        final XYSeriesCollection dataset = new XYSeriesCollection( );
        dataset.addSeries(series);
        return dataset;
    }

    public static void createLineChart(String chartTitle, String seriesName, List<Double> x, List<Double> y, String xLabel, String yLabel) {
        LineChart chart = new LineChart(chartTitle, createDataset(x, y, seriesName), xLabel, yLabel);

        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }
}
