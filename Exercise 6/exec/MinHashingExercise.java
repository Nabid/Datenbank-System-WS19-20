import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Compile the code with:
 *
 * javac MinHashingExercise.java
 *
 * To execute the program you then have to use:
 *
 * java MinHashingExercise <File1> <File2>
 *
 * For the exercise write the code of the methods jaccard, similaritykHash, and
 * similaritykValues.
 *
 * Author: PRIOM BISWAS AND MD NABID IMTEAJ
 * 
 */
public class MinHashingExercise {

    public static void main(String... args) throws IOException {
        System.out.println("Min Hashing exercise:");
        System.out.println("============================================");
        if (args.length < 2) {
            System.out.println("Error: Two files required as input!");
            System.exit(-1);
        }
        List<String> file1 = readFile(args[0]);
        List<String> file2 = readFile(args[1]);

        System.out.println("============================================");
        System.out.println("Calculating Jaccard similarity:");
        System.out.println(jaccard(file1, file2));
        System.out.println("============================================");
        for (int i = 1; i <= 6; i++) {
            System.out.println("Calculating similarity for k = " + i + " hash functions:");
            System.out.println(similaritykHash(i, file1, file2));
        }
        System.out.println("============================================");
        for (int i = 1; i <= 6; i++) {
            System.out.println("Calculating similarity for k = " + i + " hash values:");
            System.out.println(similaritykValues(i, file1, file2));
        }
        System.out.println("============================================");
    }

    private static double jaccard(List<String> lhs, List<String> rhs) {
        /////////////////////////////////////////
        /////////// Your Code Here ///////
        /////////////////////////////////////////
        //System.out.println("Jaccard function not implemented!");
        // CONVERTING ALL ENTRY TO LOWERCASE
        // lhs = lhs.stream().map(String::toLowerCase).collect(Collectors.toList());
        // rhs = rhs.stream().map(String::toLowerCase).collect(Collectors.toList());

        Set<String> union = new HashSet<String>(lhs);
        union.addAll(rhs);

        //System.out.println(union.size()); 

        Set<String> intersection = new HashSet<String>(lhs);
        intersection.retainAll(rhs);

        //System.out.println(intersection.size()); 

        double result = (double)intersection.size() / union.size();
        return result;
    }

    private static double similaritykHash(int k, List<String> lhs, List<String> rhs) {
        /////////////////////////////////////////
        /////////// Your Code Here ///////
        /////////////////////////////////////////
        //System.out.println("similaritykHash function not implemented!");

        // APPROACH
        // iterate k times
        // get min values for each k
        // if the min value for terms in both list are same, increase counter
        int sim = 0;
        int iterator = k - 1;
        Random rand = new Random();
        while (iterator >= 0) {
            int lMin = 999999;
            int rMin = 999999;
            int randomNumber;
            try {
                randomNumber = rand.nextInt(147) % 6;
            } catch(Exception e) {
                randomNumber = 0;
            }
            //System.out.println("random number: " + randomNumber);
            int tmpHash;
            for (String lString :  lhs) {
                tmpHash = hash(randomNumber, lString);
                if (tmpHash < lMin) {
                    lMin = tmpHash;
                }
            }
            for (String rString:  rhs) {
                tmpHash = hash(randomNumber, rString);
                if (tmpHash < rMin) {
                    rMin = tmpHash;
                }
            }
            if (lMin == rMin) {
                sim++;
            }

            iterator--;
        }
        double result = (double) sim / k;
        return result;
    }

    private static double similaritykValues(int k, List<String> lhs, List<String> rhs) {
        /////////////////////////////////////////
        /////////// Your Code Here ///////
        /////////////////////////////////////////
        //System.out.println("similaritykValues function not implemented!");

        // APPROACH:
        // get hashfunction for both list: lhs, rhs
        // find minimum for each list
        // get jaccard similarity for the sets of all terms with min hash in both list

        // CONVERTING ALL ENTRY TO LOWERCASE
        // lhs = lhs.stream().map(String::toLowerCase).collect(Collectors.toList());
        // rhs = rhs.stream().map(String::toLowerCase).collect(Collectors.toList());

        Map<Integer, ArrayList<String>> lhsHash = new HashMap<Integer, ArrayList<String>>();
        Map<Integer, ArrayList<String>> rhsHash = new HashMap<Integer, ArrayList<String>>();

        ArrayList<String> lArrayList, rArrayList;
        for(String itemL : lhs) {
            int hashFunction = hash(k, itemL);
            // check if the arraylist is null
            if(lhsHash.get(hashFunction) == null) {
                lArrayList = new ArrayList<String>();
            } else {
                lArrayList = lhsHash.get(hashFunction);
            }
            lArrayList.add(itemL);
            lhsHash.put(hashFunction, lArrayList);
        }

        for(String itemR : rhs) {
            int hashFunction = hash(k, itemR);
            // check if the arraylist is null
            if(rhsHash.get(hashFunction) == null) {
                rArrayList = new ArrayList<String>();
            } else {
                rArrayList = rhsHash.get(hashFunction);
            }
            rArrayList.add(itemR);
            rhsHash.put(hashFunction, rArrayList);
        }

        List<Integer> sortedKeys = new ArrayList<Integer>(lhsHash.keySet());
        int hashMinKey = Collections.min(sortedKeys);
        lArrayList = lhsHash.get(hashMinKey);
        //System.out.println(lArrayList); 

        sortedKeys = new ArrayList<Integer>(rhsHash.keySet());
        hashMinKey = Collections.min(sortedKeys);
        rArrayList = rhsHash.get(hashMinKey);
        //System.out.println(rArrayList); 

        return jaccard(lArrayList, rArrayList);
    }

    /**
     * Reads a file and returns the words in it as a List
     * 
     * @param file the path to the file to read
     * @return A list of words contained in the file.
     * @throws IOException
     */
    private static List<String> readFile(String file) throws IOException {
        System.out.println("Reading file: " + file);
        String contents = new String(Files.readAllBytes(Paths.get(file)));
        System.out.println(contents);
        return Arrays.asList(contents.split(" "));
    }

    /**
     * Calculates the k-th hash value of str
     * 
     * @param k   The index of the hash function in [0,5]
     * @param str The string to hash
     * @return The k-th hash value of str
     */
    static int hash(int k, String str) {
        int hash = str.hashCode();
        switch (k) {
        case 0:
            return hash % 2012;
        case 1:
            return hash % 1024;
        case 2:
            return hash % 4273;
        case 3:
            return hash % 582;
        case 4:
            return hash % 8362;
        case 5:
            return hash % 2743;
        default:
            return -1;
        }
    }
}
