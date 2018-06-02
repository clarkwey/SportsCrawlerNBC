import java.util.*;
import java.text.*;
import java.io.*;
import java.net.*;
import javax.json.*;
import javafx.stage.*;
import java.awt.*;
//javac -cp ".:./*" Writer.java
//java -cp ".:./*" Writer
public class Writer{
  public static boolean HOLD = false;
  private static double lastPrice;
  //private static final DrawingPanel panel = new DrawingPanel(3000,3000);
  //private static final Graphics g = panel.getGraphics();
  private static double[] marketData = new double[1000]; //Creates the temp array for each refresh of data
  private static ArrayList<ArrayList<Double>> plotPoints = new ArrayList<>(); //Records each average market value and stores it
  private static HttpURLConnection httpConn; //Universal HttpURLConnection
  private static final String POLONIEX_SECRET_KEY = "c251d9e12a35f8fe4cc04c2399ca5069d01205f3145d77097c3a38716362be19727420825c6df759e39441cd7c14f0a4f530eb9024a18f495e743dee49d79427"; //KEY
  private static final String POLONIEX_API_KEY = "KZABKMRE-G0ECEYCO-HR2AB5BS-PYTKNRJX"; // TODO API KEY
  public static void main(String[] args) throws  InterruptedException{
    //Choose the choice of crypto to analyze
    /*
    Scanner console = new Scanner(System.in);
    System.out.println("Enter cryptocurrency: BTC, ETH, BCH, XRP, REP, LTC, ZEC, ETC, STR, XMR, NXT, DASH");
    String crypto = console.next();
    */
    //For now keep it at BTC
    String cryptocurrency = "BTC";
    //1526666054
    //1526654054
    //test string https://poloniex.com/public?command=returnTradeHistory&currencyPair=BTC_NXT&start=1526654054&end=1526666054
    //String url = "https://poloniex.com/public?command=returnTradeHistory&currencyPair=USDT_"+cryptocurrency;
    //How many seconds per refresh... should be around 600
    int seconds = 1;
    int whenToPrint = 0;
    //The infinite loop
    initializeList();
    SwingWorkerRealTime swingWorkerRealTime = new SwingWorkerRealTime();
    swingWorkerRealTime.go();
    while (true){
      SimpleDateFormat ft = new SimpleDateFormat ("MM/dd/YY hh:mm:ss a zzz");
      //sendGetRequest(generateUrl());
      //We want to make a request every second BUT only print out the results every 10 seconds
      //plotPoints();
      if (whenToPrint == 10){
        //System.out.print("Current Date/Time: " + ft.format(new Date())+" : " + plotPoints.get(1).get(plotPoints.get(1).size()-1) + "\n");
        System.out.println("Last Price: " + lastPrice);
        //System.out.println(plotPoints.get(0).get(plotPoints.get(0).size()-1) + " " + plotPoints.get(2).get(plotPoints.get(2).size()-1));
        whenToPrint = 0;
      }
      whenToPrint++;
      Thread.sleep(seconds*1000);
    }
  }
  private static void initializeList(){
    ArrayList<Double> topBollinger = new ArrayList<Double>();
    ArrayList<Double> SMA = new ArrayList<Double>();
    ArrayList<Double> bottomBollinger = new ArrayList<Double>();
    plotPoints.add(topBollinger);//Top Bollinger Band
    plotPoints.add(SMA);//SMA
    plotPoints.add(bottomBollinger);//Bottom Bollinger Band
  }
  private static String generateUrl(){
    //Let's create an 20 - period SMA where each period is 10 minutes long
    long currentUnixTime = System.currentTimeMillis() / 1000L;
    // 12000 = 10 minutes period * 60s/period * 20 periods
    long startUnixTime = currentUnixTime - 12000;
    String url = "https://poloniex.com/public?command=returnTradeHistory&currencyPair=USDT_BTC&start=" + startUnixTime + "&end=" + currentUnixTime;
    //System.out.println(url);
    return url;
  }
  private static HttpURLConnection sendGetRequest(String requestURL){
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
  //Method reads JSON and inserts rates into array
  private static void filter() throws IOException{
    JsonReader reader = Json.createReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
    JsonArray jo = reader.readArray();
    for (int i = 0; i < 1000; i++){
      JsonObject jop = jo.getJsonObject(i);
      marketData[i] = (Double.parseDouble(jop.getString("rate")));
    }
    lastPrice = marketData[999];
  }
  //Method calculates moving average and standard deviations for bollinger bands
  private static double movingAverage(){
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
  //TODO create an alert to buy or testSell
  /*
  The use of Bollinger Bands varies widely among traders. Some traders buy when
  price touches the lower Bollinger Band and exit when price touches the moving
  average in the center of the bands. Other traders buy when price breaks above
  the upper Bollinger Band or sell when price falls below the lower Bollinger
  Band.[4] Moreover, the use of Bollinger Bands is not confined to stock traders;
  options traders, most notably implied volatility traders, often sell options
  when Bollinger Bands are historically far apart or buy options when the Bollinger
  Bands are historically close together, in both instances, expecting volatility
  to revert towards the average historical volatility level for the stock.
  */
  /*
  private static boolean testSell(double currentAverage){
    for (double average : averageList){
      if (currentAverage )
    }
  }
  */
  //private static final DrawingPanel panel = new DrawingPanel(1000,3000);
  /*
  public static void plotPoints(){
    for (int i = 0; i < plotPoints.get(1).size(); i++){
      double tempTop = 3000 - (plotPoints.get(0).get(i) - 6000);
      double tempMiddle = 3000 - (plotPoints.get(1).get(i) - 6000);
      double tempBottom = 3000 - (plotPoints.get(2).get(i) - 6000);
      System.out.println(tempTop + " " + tempMiddle + " "+ " " + tempBottom);
      g.fillOval(i, (int) tempTop, 1, 1);
      g.fillOval(i, (int) tempMiddle, 1, 1);
      g.fillOval(i, (int) tempBottom, 1, 1);
      g.fillOval(i, (int) lastPrice, 1, 1);
    }
  }
  */
}
