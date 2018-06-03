import java.util.*;
import java.net.*;
import java.io.*;
import javax.json.*;
import javax.swing.SwingWorker;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
//javac -cp ".:./*" SwingWorkerRealTime.java
//java -cp ".:./*" SwingWorkerRealTime
/** Creates a real-time chart using SwingWorker */
public class SwingWorkerRealTime {
private static HttpURLConnection httpConn;
private static double lastPrice;
//private static final DrawingPanel panel = new DrawingPanel(3000,3000);
//private static final Graphics g = panel.getGraphics();
private static double[] marketData = new double[1000]; //Creates the temp array for each refresh of data
private static ArrayList<ArrayList<Double>> plotPoints = new ArrayList<>(); //Records each average market value and stores it
  MySwingWorker mySwingWorker;
  SwingWrapper<XYChart> sw;
  XYChart chart;
  //ArrayList<ArrayList<Double>> plotPoints = new ArrayList<>();
/*
  public static void main(String[] args) throws Exception {

    SwingWorkerRealTime swingWorkerRealTime = new SwingWorkerRealTime();
    swingWorkerRealTime.go();
  }
*/

  public void go() {

    // Create Chart
    chart =
        QuickChart.getChart(
            "Market Data Live",
            "Time",
            "Value_USD",
            "randomWalk",
            new double[] {0},
            new double[] {0});
    chart.getStyler().setLegendVisible(false);
    chart.getStyler().setXAxisTicksVisible(false);

    // Show it
    sw = new SwingWrapper<XYChart>(chart);
    sw.displayChart();

    mySwingWorker = new MySwingWorker();
    mySwingWorker.execute();
  }

  private class MySwingWorker extends SwingWorker<Boolean, double[]> {

    final ArrayList<ArrayList<Double>> plotPoints = new ArrayList<>();

    public MySwingWorker() {
      ArrayList<Double> topBollinger = new ArrayList<Double>();
      ArrayList<Double> SMA = new ArrayList<Double>();
      ArrayList<Double> bottomBollinger = new ArrayList<Double>();
      plotPoints.add(topBollinger);//Top Bollinger Band
      plotPoints.add(SMA);//SMA
      plotPoints.add(bottomBollinger);//Bottom Bollinger Band
    }
    protected String generateUrl(){
      //Let's create an 20 - period SMA where each period is 10 minutes long

      long currentUnixTime = System.currentTimeMillis() / 1000L;
      // 12000 = 10 minutes period * 60s/period * 20 periods
      long startUnixTime = currentUnixTime - 12000;
      String url = "https://poloniex.com/public?command=returnTradeHistory&currencyPair=USDT_BTC&start=" + startUnixTime + "&end=" + currentUnixTime;
      //System.out.println(url);
      return url;
    }
    protected HttpURLConnection sendGetRequest(String requestURL){
      try{

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoInput(true); // true if we want to read server's response
        httpConn.setDoOutput(false); // false indicates this is a GET request
        filter(); //Moves rates from json into array

        double currentAverage = movingAverage();
        //System.out.print(currentAverage);//Calculates and prints the moving average
        //System.out.println();
        //httpConn.disconnect();
        //tesBuy(currentAverage);
        return httpConn;
      }catch (IOException e){
        System.err.println("IOException: "+e.getMessage());
      }
      return httpConn;
    }
    protected void filter() throws IOException{
      JsonReader reader = Json.createReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
      JsonArray jo = reader.readArray();
      for (int i = 0; i < 1000; i++){
        JsonObject jop = jo.getJsonObject(i);
        marketData[i] = (Double.parseDouble(jop.getString("rate")));

      }
      lastPrice = marketData[999];
    }

    protected double movingAverage(){
      double sum = 0;
      for (double num : marketData){
        sum = sum + num;
      }
      double average = sum / 1000;
      double std = 0;

      for (double num : marketData){
        std = std + Math.pow(num - average , 2);
      }

      std = Math.sqrt(std / 1000);

      plotPoints.get(0).add(2 * std + average);
      plotPoints.get(1).add(average);
      plotPoints.get(2).add(-1 * 2 * std + average);
      //System.out.println("std: " + std);

      return average;
    }

    @Override
    protected Boolean doInBackground() throws Exception {

      while (!isCancelled()) {
        //System.out.println("hi");
        sendGetRequest(generateUrl());
        //System.out.println("hi");
        double[] array0 = new double[plotPoints.get(0).size()];
        double[] array1 = new double[plotPoints.get(1).size()];
        double[] array2 = new double[plotPoints.get(2).size()];
        for (int i = 0; i < plotPoints.get(0).size(); i++) {
          array0[i] = plotPoints.get(0).get(i);
        }
        for (int j = 0; j < plotPoints.get(1).size(); j++) {
          array1[j] = plotPoints.get(1).get(j);
        }
        for (int k = 0; k < plotPoints.get(2).size(); k++) {
          array2[k] = plotPoints.get(2).get(k);
        }
        System.out.println(lastPrice);
        //publish(array0);
        //XYSeries series0 = chart.addSeries("Top Band", array0);
        System.out.println("hi");
        publish(array1);
        //publish(array2);
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) {
          // eat it. caught when interrupt is called
          System.out.println("MySwingWorker shut down.");
        }
      }

      return true;
    }

    @Override
    protected void process(List<double[]> chunks) {

      //System.out.println("number of chunks: " + chunks.size());

      double[] mostRecentDataSet = chunks.get(chunks.size() - 1);

      chart.updateXYSeries("randomWalk", null, mostRecentDataSet, null);
      //chart.updateXYSeries("Top Band", null, mostRecentDataSet, null);
      sw.repaintChart();

      long start = System.currentTimeMillis();
      long duration = System.currentTimeMillis() - start;
      try {
        Thread.sleep(40 - duration); // 40 ms ==> 25fps
        // Thread.sleep(400 - duration); // 40 ms ==> 2.5fps
      } catch (InterruptedException e) {
        System.out.println("InterruptedException occurred.");
      }
    }
  }
}
