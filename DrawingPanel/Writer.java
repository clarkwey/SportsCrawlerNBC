import java.util.*;
import java.text.*;
import java.io.*;
import java.net.*;
import javax.json.*;

//javac -cp ".:./*" Writer.java
//java -cp ".:./*" Writer
public class Writer{
  private static ArrayList<Double> marketData = new ArrayList<Double>();
  private static HttpURLConnection httpConn;
  public static final String POLONIEX_SECRET_KEY = "c251d9e12a35f8fe4cc04c2399ca5069d01205f3145d77097c3a38716362be19727420825c6df759e39441cd7c14f0a4f530eb9024a18f495e743dee49d79427"; //KEY
  public static final String POLONIEX_API_KEY = "KZABKMRE-G0ECEYCO-HR2AB5BS-PYTKNRJX"; // TODO API KEY
  public static void main(String[] args) throws  InterruptedException{

    String url = "https://poloniex.com/public?command=returnTradeHistory&currencyPair=USDT_BTC";
    while (true){
      SimpleDateFormat ft = new SimpleDateFormat ("MM:dd:YY hh:mm:ss a zzz");
      System.out.println("Current Date/Time: " + ft.format(new Date())+" : Period 5s");
      sendGetRequest(url);
      Thread.sleep(10000);
      }
  }
  private static HttpURLConnection sendGetRequest(String requestURL){
    try{
      URL url = new URL(requestURL);
      httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setUseCaches(false);
      httpConn.setDoInput(true); // true if we want to read server's response
      httpConn.setDoOutput(false); // false indicates this is a GET request
      filter();
      System.out.println("Moving average: " + movingAverage());
      return httpConn;
    }catch (IOException e){
      System.err.println("IOException: "+e.getMessage());
    }
    return httpConn;
  }
  private static void filter() throws IOException{
    JsonReader reader = Json.createReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
    JsonArray jo = reader.readArray();
    for (int i = 0; i < 200; i++){
      JsonObject jop = jo.getJsonObject(i);
      marketData.add(Double.parseDouble(jop.getString("rate")));
    }
  }
  private static double movingAverage(){
    double average = 0;
    for (int i = 0; i < 200; i++){
      average = average + marketData.get(i);
    }
    return average / 200;
  }
}
