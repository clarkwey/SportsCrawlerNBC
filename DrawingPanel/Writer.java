import java.util.*;
import java.io.*;
import java.net.*;
import javax.json.*;

//javac -cp ".:./*" Writer.java
      //java -cp ".:./*" Writer
public class Writer{
  private static final String POLONIEX_SECRET_KEY = "c251d9e12a35f8fe4cc04c2399ca5069d01205f3145d77097c3a38716362be19727420825c6df759e39441cd7c14f0a4f530eb9024a18f495e743dee49d79427"; //KEY
  private static final String POLONIEX_API_KEY = "KZABKMRE-G0ECEYCO-HR2AB5BS-PYTKNRJX"; // TODO API KEY
  public static void main(String[] args) throws  InterruptedException{
    //Choose the choice of crypto to analyze
    Scanner console = new Scanner(System.in);
    System.out.println("Enter cryptocurrency: BTC, ETH, BCH, XRP, REP, LTC, ZEC, ETC, STR, XMR, NXT, DASH");
    String crypto = console.next();
    //For now keep it at BTC
    //String cryptocurrency = "BTC";
    //How many seconds per refresh... should be around 600
    SwingWorkerRealTime swingWorkerRealTime = new SwingWorkerRealTime();
    swingWorkerRealTime.go();
  }
}
