import java.io.IOException;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;
import java.util.*;

/**
 * Compile the code with:
 *
 * javac ZCurveExercise.java
 *
 * To execute the program you then have to use:
 *
 * java ZCurveExercise <k> <points.txt> <queries.txt>
 *
 * For the exercise write the code of the methods:
 * - ZCurveExercise::getKNN
 * - ZCurveExercise::getKNNZCurve
 * - Point::distance
 * - Point::calculateZValue
 */
public class ZCurveExercise {

	public static void main(String... args) throws IOException {
		System.out.println("ZCurve exercise:");
		System.out.println("============================================");
		if (args.length < 3) {
			System.out.println("Error: 3 parameters expected: k, data-file, query-file");
			System.exit(-1);
		}
		List<Point> points = readFile(args[1]);
		List<Point> queries = readFile(args[2]);
		int k = Integer.parseInt(args[0]);

		System.out.println("============================================");
		System.out.println("Points:");
		for (Point p : points) {
			System.out.println(p.toString());
		}
		System.out.println("============================================");
		System.out.println("Queries:");
		for (Point q : queries) {
			System.out.println(q.toString());
		}

		System.out.println("============================================");
		System.out.println("KNN (k = "+k+"):");
		for (Point q : queries) {
			List<Point> knn = getKNN(k,points,q);
			System.out.println(q.toString()+": " + knn.toString());
		}
		System.out.println("============================================");
		System.out.println("KNN ZCurve (k = "+k+"):");
		for (Point q : queries) {
			List<Point> knn = getKNNZCurve(k,points,q);
			System.out.println(q.toString()+": " + knn.toString());
		}
		System.out.println("============================================");
	}

	/**
	 * Reads a file and returns the points in it as a List
	 * 
	 * @param file the path to the file to read
	 * @return A list of points contained in the file.
	 * @throws IOException
	 */
	private static List<Point> readFile(String file) throws IOException {
		System.out.println("Reading file: " + file);
		List<String> lines = Files.readAllLines(Paths.get(file));
		
		return lines.stream().map(s -> s.split(" ")).map(
			e -> new Point(Integer.parseInt(e[0]),Integer.parseInt(e[1]))
			).collect(Collectors.toList());
	}

	/**
	 * Computes the k NN of query in the list of points
	 * @param k How many nearest neighbors to return
	 * @param points The list of points in which to search for the NN
	 * @param query The query point, for which the NN are searched
	 * @return A list of k NN, of the query point, in the list of points
	 */
	private static List<Point> getKNN(int k, List<Point> points, Point query){
		/////////////////////////////////////////
        /////////// Your Code Here        ///////
        /////////////////////////////////////////
		//System.out.println("getKNN function not implemented!");
		List<Pair<Point, Double>> hashNNList = new ArrayList<Pair<Point, Double>> ();

		for(Point p : points) {
			hashNNList.add(new Pair<>(p, p.distance(query)));
		}

        // sort according to distance
        Collections.sort(hashNNList, new Comparator<Pair<Point, Double>>() {
            @Override
            public int compare(final Pair<Point, Double> o1, final Pair<Point, Double> o2) {
				Double a = o1.getValue();
				Double b = o2.getValue();
                return a.compareTo(b);
            }
		});
		
		//hashNNList.subList(0, k).getKeys();
		List<Point> result = new ArrayList<Point>();
		for(int i=0; i<k; i++) {
			result.add(hashNNList.get(i).getKey());
		}

		return result;
	}

	/**
	 * Computes the k NN of query in the list of points, based on the ZCurve value
	 * @param k How many nearest neighbors to return
	 * @param points The list of points in which to search for the NN
	 * @param query The query point, for which the NN are searched
	 * @return A list of k NN, of the query point, in the list of points
	 */
	private static List<Point> getKNNZCurve(int k, List<Point> points, Point query){
		/////////////////////////////////////////
        /////////// Your Code Here        ///////
        /////////////////////////////////////////
		// System.out.println("getKNNZCurve function not implemented!");
		List<Pair<Point, Long>> hashNNList = new ArrayList<Pair<Point, Long>> ();

		for(Point p : points) {
			long diff = Math.abs(p.calculateZValue() - query.calculateZValue());
			hashNNList.add(new Pair<>(p, diff));
		}

        // sort according to distance
        Collections.sort(hashNNList, new Comparator<Pair<Point, Long>>() {
            @Override
            public int compare(final Pair<Point, Long> o1, final Pair<Point, Long> o2) {
				Long a = o1.getValue();
				Long b = o2.getValue();
                return a.compareTo(b);
            }
		});
		
		//hashNNList.subList(0, k).getKeys();
		List<Point> result = new ArrayList<Point>();
		for(int i=0; i<k; i++) {
			result.add(hashNNList.get(i).getKey());
		}

		return result;
	}

	static class Pair<K,V> extends AbstractMap.SimpleEntry<K,V> {
		public Pair(K k, V v) {
			super(k,v);
		}
	}

	/**
	 * Represents a 2D point
	 */
	static class Point {
		private final int x;
		private final int y;
		
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		/**
		 * @return the String representation of the given point
		 */
		public String toString() {
			return "("+x+","+y+")"+" {"+calculateZValue()+"}";
		}

		/**
		 * Calculates the distance to a given Point
		 * @param o the other Point to calculate the distance to
		 * @return The distance between this Point and Point o
		 * Eucledean distance
		 */
		public double distance(Point o){
			/////////////////////////////////////////
	        /////////// calculateZValue        ///////
	        /////////////////////////////////////////
			//System.out.println("Point::distance function not implemented!");
			Double result = 0.0;
			result = Math.sqrt((double)(this.x - o.x)*(this.x - o.x) + (this.y - o.y)*(this.y - o.y)); 
			return result;
		}

		/**
		 * @return the ZCurve value of the given point
		 * Z Value from byte string of x and y
		 */
		public long calculateZValue(){
			/////////////////////////////////////////
	        /////////// calculateZValue        ///////
	        /////////////////////////////////////////
			//System.out.println("Point::getKNNZCurve function not implemented!");
			String strX = Integer.toBinaryString(this.x);
			String strY = Integer.toBinaryString(this.y);
			String strZ = "";

			int lenDiff = Math.abs(strX.length() - strY.length());

			// System.out.println("X = " + strX + ", Y = " + strY);
			// if(this.x == 4 && this.y == 8) {
			// 	System.out.println("X = " + strX + ", Y = " + strY + ", Diff = " + lenDiff);
			// }

			if(strX.length() < strY.length()) {
				while(lenDiff-- > 0) {
					strX = "0" + strX;
				}
				// if(this.x == 4 && this.y == 8) {
				// 	System.out.println("->>>X = " + strX + ", Y = " + strY);
				// }
			} else {
				while(lenDiff-- > 0) {
					strY = "0" + strY;
				}
			}

			// if(this.x == 4 && this.y == 8) {
			// 	System.out.println("X = " + strX + ", Y = " + strY);
			// }

			int len = strX.length();

			for(int i=0; i<len; i++) {
				strZ = strZ + "" + strY.charAt(i) + strX.charAt(i);
			}

			// if(this.x == 4 && this.y == 8) {
			// 	System.out.println("Z = " + strZ);
			// }

			long result = Long.parseLong(strZ, 2);

			return result;
		}
	}


}


