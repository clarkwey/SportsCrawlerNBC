import java.util.*;
import java.net.*;
import java.io.*;
import javax.json.*;
import java.text.*;
import javax.swing.SwingWorker;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
//javac -cp ".:./*" SwingWorkerRealTime.java
//java -cp ".:./*" SwingWorkerRealTime
/** Creates a real-time chart using SwingWorker */
public class SwingWorkerRealTime {
public static boolean HOLD = false;
private static HttpURLConnection httpConn;
private static double lastPrice;
private static double buyingPrice;
private static double profit;
private static double[] marketData = new double[1000]; //Creates the temp array for each refresh of data
private static ArrayList<ArrayList<Double>> plotPoints = new ArrayList<>(); //Records each average market value and stores it
  MySwingWorker mySwingWorker;
  SwingWrapper<XYChart> sw;
  XYChart chart;
  public void go() {
    // Create the live Chart
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
    //Establishes connection to the api
    protected HttpURLConnection sendGetRequest(String requestURL){
      try{
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoInput(true); // true if we want to read server's response
        httpConn.setDoOutput(false); // false indicates this is a GET request
        filter(); //Moves rates from json into array
        double currentAverage = movingAverage();
        return httpConn;
      }catch (IOException e){
        System.err.println("IOException: "+e.getMessage());
      }
      return httpConn;
    }
    //Filters through the api and pulls out the rates
    protected void filter() throws IOException{
      JsonReader reader = Json.createReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
      JsonArray jo = reader.readArray();
      for (int i = 0; i < 1000; i++){
        JsonObject jop = jo.getJsonObject(i);
        marketData[i] = (Double.parseDouble(jop.getString("rate")));

      }
      lastPrice = marketData[999];
    }
    //Calculates moving averages
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
      return average;
    }
    @Override
    protected Boolean doInBackground() throws Exception {
      int whenToPrint = 0;
      while (!isCancelled()) {
        sendGetRequest(generateUrl());
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
        if (plotPoints.get(0).size() > 2){
          double previousTop = array0[plotPoints.get(0).size()-2];
          double previousMiddle = array1[plotPoints.get(1).size()-2];
          double previousBottom = array2[plotPoints.get(2).size()-2];
          double lastTop = array0[plotPoints.get(0).size()-1];
          double middle = array1[plotPoints.get(1).size()-1];
          double lastBottom = array2[plotPoints.get(2).size()-1];
          //System.out.println("Distance from top/bottom: " + (lastTop - lastPrice) + " & " + (lastPrice - lastBottom) + " last price: "+ lastPrice);
          if (!HOLD){
            buy(lastTop, previousTop, middle, previousMiddle, lastBottom, previousBottom);
          }else if(HOLD){
            System.out.println("HOLD COIN");
            sell(lastTop, previousTop, middle, previousMiddle, lastBottom, previousBottom);
          }
        }
        publish(array1);
        if (whenToPrint == 10){
          double middle = array1[plotPoints.get(1).size()-1];
          SimpleDateFormat ft = new SimpleDateFormat ("MM/dd/YY hh:mm:ss a zzz");
          System.out.print("Current Date/Time: " + ft.format(new Date())+ " Last Price: " + lastPrice + " MA: " + middle + "\n");
          whenToPrint = 0;
        }
        whenToPrint++;
        if (plotPoints.get(2).size()-1 > 200){
          plotPoints.get(0).remove(0);
          plotPoints.get(1).remove(0);
          plotPoints.get(2).remove(0);
        }
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) {

          System.out.println("MySwingWorker shut down.");
        }
      }
      return true;
    }
    @Override
    protected void process(List<double[]> chunks) {
      double[] mostRecentDataSet = chunks.get(chunks.size() - 1);
      chart.updateXYSeries("randomWalk", null, mostRecentDataSet, null);
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
    protected void buy(double top, double prevTop, double middle, double prevMiddle, double bottom, double prevBottom){
        double distanceToTop = top - lastPrice;
        double distanceToBottom = lastPrice - bottom;
        double prevDistanceToTop = prevTop - lastPrice;
        if(distanceToTop - prevDistanceToTop < -1 && distanceToTop > 5){
          HOLD = true; //If top band difference drops, sign that value is increasing.
          System.out.println("BUY: value increasing");
          System.out.println("Buying Price: $" + lastPrice);
          buyingPrice = lastPrice;
        }else if (distanceToBottom < 3){
          HOLD = true; // If price crosses the bottom bound or approaches it, price is likely to rise.We should BUY
          System.out.println("BUY: Approaching bottom band");
          System.out.println("Buying Price: $" +lastPrice);
          buyingPrice = lastPrice;
        }
    }
    protected void sell(double top, double prevTop, double middle, double prevMiddle, double bottom, double prevBottom){
      double distanceToTop = top - lastPrice;
      double distanceToBottom = lastPrice - bottom;
      double prevDistanceToTop = prevTop - lastPrice;
      if (distanceToTop < 3){
        HOLD = false;
        System.out.println("SELL: Approaching top band");
        System.out.println("Selling Price: $" + lastPrice);
        System.out.println("Profit: " + (lastPrice - buyingPrice));
      }else if(lastPrice - buyingPrice >10 ){
        HOLD = false;
        System.out.println("SELL: Target profit");
        System.out.println("Selling Price: $"+lastPrice);
        System.out.println("Profit: " + (lastPrice - buyingPrice));
      }else if(buyingPrice - lastPrice > 10){
        HOLD = false;
        System.out.println("CUTOFF SELL");
        System.out.println("Selling Price: $"+lastPrice);
        System.out.println("Loss: " + (lastPrice - buyingPrice));
      }
    }
  }
}
