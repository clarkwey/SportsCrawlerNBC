public class Market{
  private String type;
  private double rate;
  private double amount;
  private double total;
  public Market(String type, double rate, double amount, double total){
    this.type = type;
    this.rate = rate;
    this.amount = amount;
    this.total = total;
  }
  public String getType(){
    return this.type;
  }
  public double getRate(){
    return this.rate;
  }
  public double getAmount(){
    return this.amount;
  }
  public double getTotal(){
    return this.total;
  }
  public String toString(){
    return type + ", " + rate + ", "+ amount + ", " + ", " + total;
  }
}
