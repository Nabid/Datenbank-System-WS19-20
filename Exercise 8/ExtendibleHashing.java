import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ExtendibleHashing {

    /**
     * Compile the code with:
     *
     *     javac ExtendibleHashing.java
     *
     * To execute the program you then have to use:
     *
     *     java ExtendibleHashing <Max Bucket Size> <Data File>
     *
     * For the exercise write the code of the methods Directory.addEntry and Directory.toString().
     * The Hashing class contains some helper functions to work with the byte datatype.
     *
     */
    public static void main (String[] args)  throws IOException  {
        if (args.length < 2) {
            System.out.println("Error: 2 parameters expected: Bucketsize, data-file");
            System.exit(-1);
        }

        int maxBucketSize = 0;
        try {
            maxBucketSize = Integer.parseInt(args[0]);
        }
        catch (Exception e) {
            System.out.println("The first argument must be an integer.");
            System.exit(1);
        }
        Directory d = new Directory(maxBucketSize);
        hashFileToDirectory(args[1],d);

    }

    
    /**
     * Reads a file; calculates the hash of each line and adds it to the directory
     * 
     * @param file the path to the file to read
     * @param directory the directory to store the hashes in
     * @throws IOException
     */
    private static void hashFileToDirectory(String file, Directory directory) throws IOException {
        System.out.println("Reading file: " + file);
        List<String> lines = Files.readAllLines(Paths.get(file));
        for(String line : lines) {
            directory.addEntry(Integer.valueOf(line.hashCode()%255).byteValue(),line);
        }

    }

    //////////////////////////////////////////////////////////////////////////////
    //Below are some helper functions that can be used to modify/display bits
    //////////////////////////////////////////////////////////////////////////////


    /**
     * Returns only the first X significant bits of the byte value
     * @param value The value to be modified.
     * @param x How many bits to return
     * @return A byte representing only the first x significant bits of value.
     */
    static byte getXSignificantBits(byte value, int x){
        //Create mask
        byte mask = 0;
        for (int i = 0; i < 8; ++i) {
            if (i >= 8-x){
                mask |= (1<<i);
            }
        }
        String maskStr = ExtendibleHashing.getBooleanString(mask);
        String valuexMask = ExtendibleHashing.getBooleanString((byte) (value & mask));
        return (byte) (value & mask);
    }

    /**
     * Flips the byte order between little endian and big endian.
     * @param value The byte to reverse
     * @return The reversed byte
     */
    static byte reverseByteOrder(byte value){
        byte b = 0;
        for (int i = 0; i < 8; ++i) {
            b<<=1;
            b|=( value &1);
            value>>=1;
        }
        return b;
    }

    /**
     * Returns the value as Binary string
     * @param value The value to transform
     * @return The binary string
     */
    static String getBooleanString(int value){
        return String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    /**
     * Returns the value as Binary string
     * @param value The value to transform
     * @return The binary string
     */
    static String getBooleanString(byte value){
        return getBooleanString(value & 0xFF);
    }

    /**
     * Returns the boolean value of index in values.
     * For example: 3 => 00000011:  getBoolIndex(0, 3) = True; getBoolIndex(2, 3) = False
     *                i: 76543210
     * @param index The index to look at (Starting from the least significant bit)
     * @param value The value to get the boolean from
     * @return The boolean value at index in value.
     */
    static Boolean getBoolIndex(int index, byte value){
        byte mask = 0b1;
        mask <<= index;
        return (mask&value) != 0;
    }
}


/**
 * The Directory class should represent the hashing directory.
 * You have to implement the addEntry and toString() methods.
 * How you actually store and represent the Buckets is up to you.
 *
 * There are relatively simple implementations with a vector of buckets,
 * where each entry in the vector represents a hash prefix.
 */
class Directory{

    private int maxBucketSize; //Maximum Bucket size
    private int d = 0; //Global Depth

    Directory(int maxBucketSize) {
        System.out.println("Initialized Directory with maximum bucket size: " + maxBucketSize);
        this.maxBucketSize = maxBucketSize;
    }



    /**
     * This function will be called with the hashed integer value.
     * You may assume that only binary hash values <= 8 bits are inserted.
     * @param hash The value to be used for hashing.
     * @param data The data point to be inserted.
     *
     */
    void addEntry(byte hash, String data){
        System.out.println("Inserting '" + data + "' with hash value " + ExtendibleHashing.getBooleanString(hash));

        /////////////////////////////////////////////////////////////////////
        //TODO:  Your code here!
        /////////////////////////////////////////////////////////////////////
        System.out.println("ERROR: Directory.addEntry not implemented!");

        System.out.println("======================================");
        System.out.println(toString());
        System.out.println("======================================");
    }

    /**
     * This function should present the following:
     *  Global Depth
     *  All buckets with:
     *      Their contained data points
     *      Local depth
     *      Hash prefixes pointing to them
     * @return A String representing the above content.
     */
    public String toString(){

        /////////////////////////////////////////////////////////////////////
        //TODO:  Your code here!
        /////////////////////////////////////////////////////////////////////
        System.out.println("ERROR: Directory.toString not implemented!");


        return "";
    }
}