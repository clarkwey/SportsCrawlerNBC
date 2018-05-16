import java.util.*;
import java.text.*;


public class Writer{
  public static void main(String[] args) throws  InterruptedException{
    while (true){

      SimpleDateFormat ft =
      new SimpleDateFormat ("hh:mm:ss a zzz");
      System.out.println("Current Date: " + ft.format(new Date())+" : Period 5s");
      Thread.sleep(5000);
      }
  }
}
