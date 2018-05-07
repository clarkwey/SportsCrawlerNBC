import java.io.*;
import java.util.*;
import java.awt.*;
public class DigitalDerivative{
	public static final DrawingPanel panel = new DrawingPanel(800,400);
	public static final Graphics g = panel.getGraphics();
	public static final int[] years = {1960,1962,1964,1966,1968,1970,1972,1974,1976,1978,1980,1982,1984,1986,1988,1990,1992,1994,1996,1998,2000,2002,2004,2006,2008,2010,2012,2014,2016,2018,2020,2022,2024};
	public static void main(String[] args) throws FileNotFoundException{
		Scanner console = new Scanner(System.in);
		System.out.println("Input file name");
		Scanner sc = new Scanner(new File(console.next()));
		ArrayList<Double> list = new ArrayList<>();
		while (sc.hasNextLine()){
			
			list.add(sc.nextDouble());
		}
		for (int i =0; i<800; i++){
			int u = (int) Math.round(list.get(i));

			g.fillOval(i,400-u,1,1);
		}
		System.out.println("select Savitsky-Golay filter: ");
		System.out.println("smoothing");
		System.out.println(" quadratic or cubic");
		System.out.println("  0   {  0, 0,-3,12,17,12,-3, 0,  0}");
		System.out.println("  1   {  0,-2, 3, 6, 7, 6, 3,-2,  0}");
		System.out.println("  2   {-21,14,39,54,59,54,39,14,-21}");
		System.out.println(" quartic or quintic");
		System.out.println("  3   { 0,  5,-30, 75,131, 75,-30,  5, 0}");
		System.out.println("  4   {15,-55, 30,135,179,135, 30,-55,15}");
		System.out.println();
		System.out.println("1st derivative");
		System.out.println(" linear or quadratic");
		System.out.println("  5   { 0, 0, 0,-1,0,1,0,0,0}");
		System.out.println("  6   { 0, 0,-2,-1,0,1,2,0,0}");
		System.out.println("  7   { 0,-3,-2,-1,0,1,2,3,0}");
		System.out.println("  8   {-4,-3,-2,-1,0,1,2,3,4}");		
		System.out.println(" cubic or quartic");
		System.out.println("  9   { 0,   0,   1,  -8,0,  8, -1,  0,  0}");
		System.out.println("  10  { 0,  22, -67, -58,0, 58, 67,-22,  0}");
		System.out.println("  11  {86,-142,-193,-126,0,126,193,142,-86}");
		System.out.print("Enter an integer 0 - 11 corresponding to desired filter: ");
		int filterKey = console.nextInt();
        System.out.println();
        int[] a0 = { 0, 0,-3,12,17,12,-3, 0,  0};
        int[] a1 = {0,-2, 3, 6, 7, 6, 3,-2,  0};
        int[] a2 = {-21,14,39,54,59,54,39,14,-21};
        int[] a3 = {0,  5,-30, 75,131, 75,-30,  5, 0};
        int[] a4 = {15,-55, 30,135,179,135, 30,-55,15};
        int[] a5 = {0, 0, 0,-1,0,1,0,0,0};
        int[] a6 = {0, 0,-2,-1,0,1,2,0,0};
        int[] a7 = {0,-3,-2,-1,0,1,2,3,0};
        int[] a8 = {-4,-3,-2,-1,0,1,2,3,4};
        int[] a9 = {0,0,1,-8,0,8,-1,0,0};
        int[] a10 = {0,22,-67,-58,0,58,67,-22,0};
        int[] a11 = {86,-142,-193,-126,0,126,193,142,-86};
        int[][] array = new int[12][9];
        for (int i =0; i<9; i++){
        	array[0][i]=a0[i];
        }
        for (int i =0; i<9; i++){
        	array[1][i]=a1[i];
        }
        for (int i =0; i<9; i++){
        	array[2][i]=a2[i];
        }
        for (int i =0; i<9; i++){
        	array[3][i]=a3[i];
        }
        for (int i =0; i<9; i++){
        	array[4][i]=a4[i];
        }
        for (int i =0; i<9; i++){
        	array[5][i]=a5[i];
        }
        for (int i =0; i<9; i++){
        	array[6][i]=a6[i];
        }
        for (int i =0; i<9; i++){
        	array[7][i]=a7[i];
        }
        for (int i =0; i<9; i++){
        	array[8][i]=a8[i];
        }
        for (int i =0; i<9; i++){
        	array[9][i]=a9[i];
        }
        for (int i =0; i<9; i++){
        	array[10][i]=a10[i];
        }
        for (int i =0; i<9; i++){
        	array[11][i]=a11[i];
        }
        
        derivative(list, array,filterKey);
	}
	public static void derivative(ArrayList<Double> list, int[][] array, int row){
		int r =0;
		for (int i =5; i<795; i++){
			double tempSum = 0;
			if ((i)%24==0 && r<34){
				drawAxis(i,r);
				r++;
			}			
			for (int k =-4; k<5; k++){
				tempSum = tempSum + list.get(i+k-1)*array[row][k+4];
			}
			g.fillOval(i,400-(int)(4*tempSum),1,1);
		}
	}	
	public static void drawAxis(int x,int counter){
		g.setFont(new Font("SanSerif", Font.PLAIN,8));
		g.drawLine(x,380,x,370);
		String year = Integer.toString(years[counter]);
		g.drawString(year,x-8, 390);
	}

}