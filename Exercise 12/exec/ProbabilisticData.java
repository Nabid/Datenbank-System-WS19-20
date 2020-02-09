import java.util.*;
import java.util.stream.Collectors;


  /**
   * Compile the code with:
   *
   * javac ProbabilisticData.java
   *
   * To execute the program you then have to use:
   *
   * java ProbabilisticData
   * 
   * The program already implements:
   *  - A normal mock database table for persons, having the attributes "FID, Name, ZIP, Date of Birth".
   *  - A Probabilistic database table, which can be used to represent all possible worlds with the set representation.
   * 
   * Your task is to
   *  - Implement the "calculatePossibleWorldSets" function which takes the persons table, sets of FID that were deemed the same person, 
   *    and correctness values for every FID.
   *    The function has to return a filled probabilistic database table "ProbabilisticPersonTable" with all possible worlds. 
   *  - Implement the ProbabilisticPersonTable::query<Name/Zip/Dob> functions.
   *    They take the remaining attributes as parameter and return the results and their probability.
   * 
   * You may of course also create new classes/functions and modify existing ones (except for the main function).
   *
   */
public class ProbabilisticData {

  /**
   * Creates sample data and tests the implementation.
   * Do not change!
   * 
   */
  public static void main(String[] args) {
    //Create base data table
    PersonTable personsTable = new PersonTable();
    personsTable.insertTuple(1,"Hoffman","66133","12.11.89");
    personsTable.insertTuple(2,"Mayer","67663","04.01.93");
    personsTable.insertTuple(3,"Hofmann","66123","11.11.89");
    personsTable.insertTuple(4,"Mayer","67653","01.04.1993");
    personsTable.insertTuple(5,"Meyer","67663","01.04.1993");
    System.out.println(personsTable);

    //Create similar tuple sets
    ArrayList<HashSet<Integer>> similarTuples = new ArrayList<HashSet<Integer>>();
    HashSet<Integer> similar1 = new HashSet<>();
    similar1.add(1);
    similar1.add(3);
    similarTuples.add(similar1);
    HashSet<Integer> similar2 = new HashSet<>();
    similar2.add(2);
    similar2.add(4);
    similar2.add(5);
    similarTuples.add(similar2);

    //Create correctness table
    HashMap<Integer, Double> correctness = new HashMap<>();
    correctness.put(1, 0.6);
    correctness.put(2, 0.3);
    correctness.put(3, 0.4);
    correctness.put(4, 0.2);
    correctness.put(5, 0.5);

    /*
    * Check results
    */
    ProbabilisticPersonTable ppt = calculatePossibleWorldSets(personsTable, similarTuples, correctness);
    System.out.println(ppt);
    if(ppt.getEntries().size() != 2){
      System.err.println("Wrong number of worlds");
      System.exit(-1);
    }
    for (ProbabilisticPersonTuple pp : ppt.getEntries()) {
      HashMap<String,Double> names = pp.getNames();
      HashMap<String,Double> zips = pp.getZips();
      HashMap<String,Double> dobs = pp.getDobs();
      if (pp.getFIDs().equals(similar1)){
        if(names.getOrDefault("Hoffman", -1.) != 0.6 || names.getOrDefault("Hofmann", -1.) != 0.4 || zips.getOrDefault("66133", -1.) != 0.6
        || zips.getOrDefault("66123", -1.) != 0.4 || dobs.getOrDefault("12.11.89", -1.) != 0.6 || dobs.getOrDefault("11.11.89", -1.) != 0.4){
          System.err.println("World merged wrong content");
          System.exit(-1);
        }
      }else if(pp.getFIDs().equals(similar2)){
        if(names.getOrDefault("Mayer", -1.) != 0.5 || names.getOrDefault("Meyer", -1.) != 0.5 || zips.getOrDefault("67663", -1.) != 0.8
        || zips.getOrDefault("67653", -1.) != 0.2 || dobs.getOrDefault("01.04.1993", -1.) != 0.7 || dobs.getOrDefault("04.01.93", -1.) != 0.3){
          System.err.println("World merged wrong content");
          System.exit(-1);
        }
      }else{
        System.err.println("World merged wrong FID sets: " + pp.getFIDs().toString());
        System.exit(-1);
      }
    }

    //Check Queries
    final double THRESHOLD = .0001;
    HashMap<String,Double> q1 = ppt.queryZIP("", "01.04.1993");
    if(q1.size() != 2 || Math.abs(q1.getOrDefault("67663", -1.)-0.56) >= THRESHOLD || Math.abs(q1.getOrDefault("67653", -1.) -  0.14) >= THRESHOLD){
      System.err.println("Wrong query result for query 1");
      System.exit(-1);
    }

    HashMap<String,Double> q2 = ppt.queryDOB("Hofmann", "66133");
    if(q2.size() != 2 || Math.abs(q2.getOrDefault("12.11.89", -1.)-0.144) >= THRESHOLD || Math.abs(q2.getOrDefault("11.11.89", -1.) -  0.096) >= THRESHOLD){
      System.err.println("Wrong query result for query 2");
      System.exit(-1);
    }

    System.out.println("All checks passed, solution probably correct.");
    System.exit(0);
  }



  /**
   * Creates all possible worlds in a set representation.
   * @param personsTable A table containing normal person tuples
   * @param similarTuples A list of sets. Each set describes which person tuples (represented by their FID) were deemed to be the same person.
   * @param correctness A Map of FID->Double, which specifies the correctness of every person tuple.
   * @return A ProbabilisticPersonTable, representing all possible worlds
   */
  public static ProbabilisticPersonTable calculatePossibleWorldSets(PersonTable personsTable,List<HashSet<Integer>> similarTuples, Map<Integer, Double> correctness){
    ProbabilisticPersonTable probabilisticPersonTable = new ProbabilisticPersonTable();

    for (HashSet<Integer> similarTuple : similarTuples) {
      HashSet<Integer> fids = new HashSet<Integer>();
      HashMap<String, Double> names = new HashMap<String, Double>();
      HashMap<String, Double> zips = new HashMap<String, Double>();
      HashMap<String, Double> dobs = new HashMap<String, Double>();

      for (int FID : similarTuple) {
        Optional<PersonTuple> personOptional = personsTable.getByFID(FID);
        if(!personOptional.isPresent()) continue;

        double probability = correctness.get(FID);
        PersonTuple personTuple = personOptional.get();
        fids.add(FID);

        // add previously existing probability with self probability
        double existingProbability = 0.0;

        existingProbability = (names.get(personTuple.getName()) != null ? names.get(personTuple.getName()) : 0.0) + probability;
        names.put(personTuple.getName(), existingProbability);

        existingProbability = (zips.get(personTuple.getZip()) != null ? zips.get(personTuple.getZip()) : 0.0) + probability;
        zips.put(personTuple.getZip(), existingProbability);

        existingProbability = (dobs.get(personTuple.getDob()) != null ? dobs.get(personTuple.getDob()) : 0.0) + probability;
        dobs.put(personTuple.getDob(), existingProbability);
      }

      ProbabilisticPersonTuple probabilisticPersonTuple = new ProbabilisticPersonTuple(fids, names, zips, dobs);
      probabilisticPersonTable.addTuple(probabilisticPersonTuple);
    }

    return probabilisticPersonTable;
  }
}

/**
 * Represents a probabilistic person table with all possible worlds as set representation.
 */
class ProbabilisticPersonTable{
  List<ProbabilisticPersonTuple> entries;

  /**
   * Creates an empty ProbabilisticPersonTable
   */
  public ProbabilisticPersonTable(){
    entries = new ArrayList<>();
  }

  /**
   * 
   * @return the list of probabilistic person tuples
   */
  public List<ProbabilisticPersonTuple> getEntries(){
    return entries;
  }

  /**
   * Adds a new ProbabilisticPersonTuple to the table
   * @param p
   */
  public void addTuple(ProbabilisticPersonTuple p){
    entries.add(p);
  }

  
/**
 * Queries the probabilistic table for all ZIP values adhering to the query, and their probabilities
 * @param name The name to be queried or empty string ("") if every name is allowed
 * @param dob The DOB to be queried or empty string ("") if every DOB is allowed
 * @return A map Result->Probability
 */
  public HashMap<String, Double> queryZIP(String name, String dob){
    HashMap<String, Double> result = new HashMap<>();

    for (ProbabilisticPersonTuple pp : getEntries()) {
      HashMap<String, Double> names = pp.getNames();
      HashMap<String, Double> dobs = pp.getDobs();
      HashMap<String, Double> zips = pp.getZips();

      Double nameProbability = names.get(name);
      Double dobProbability = dobs.get(dob);

      for(Map.Entry<String, Double> nEntry : names.entrySet()) {
        for(Map.Entry<String, Double> dEntry : dobs.entrySet()) {
          for(Map.Entry<String, Double> zEntry : zips.entrySet()) {
            String zipKey = zEntry.getKey().toString();
            Double zipProbability = Double.parseDouble(zEntry.getValue().toString());

            if(name == "") {
              // consider only dob
              if(dEntry.getKey() == dob) {
                result.put(zipKey, zipProbability * dobProbability);
              }
            } else if(dob == "") {
              // consider only name
              if(nEntry.getKey() == name) {
                result.put(zipKey, zipProbability * nameProbability);
              }
            } else {
              // consider name and dob
              if(nEntry.getKey() == name && dEntry.getKey() == dob) {
                result.put(zipKey, zipProbability * nameProbability * dobProbability);
              }
            }
          }
        }
      }
    }
    return result;
  }

/**
 * Queries the probabilistic table for all name values adhering to the query, and their probabilities
 * @param zip The zip to be queried or empty string ("") if every zip is allowed
 * @param dob The DOB to be queried or empty string ("") if every DOB is allowed
 * @return A map Result->Probability
 */
  public HashMap<String, Double> queryName(String zip, String dob){
    HashMap<String, Double> result = new HashMap<>();

    for (ProbabilisticPersonTuple pp : getEntries()) {
      HashMap<String, Double> names = pp.getNames();
      HashMap<String, Double> dobs = pp.getDobs();
      HashMap<String, Double> zips = pp.getZips();

      Double zipProbability = zips.get(zip);
      Double dobProbability = dobs.get(dob);

      for(Map.Entry<String, Double> nEntry : names.entrySet()) {
        for(Map.Entry<String, Double> dEntry : dobs.entrySet()) {
          for(Map.Entry<String, Double> zEntry : zips.entrySet()) {
            String nameKey = nEntry.getKey().toString();
            Double nameProbability = Double.parseDouble(nEntry.getValue().toString());

            if(zip == "") {
              // consider only dob
              if(dEntry.getKey() == dob) {
                result.put(nameKey, nameProbability * dobProbability);
              }
            } else if(dob == "") {
              // consider only zip
              if(zEntry.getKey() == zip) {
                result.put(nameKey, nameProbability * zipProbability);
              }
            } else {
              // consider name and dob
              if(zEntry.getKey() == zip && dEntry.getKey() == dob) {
                result.put(nameKey, nameProbability * zipProbability * dobProbability);
              }
            }
          }
        }
      }
    }

    return result;
  }

  /**
 * Queries the probabilistic table for all DOB values adhering to the query, and their probabilities
 * @param name The name to be queried or empty string ("") if every name is allowed
 * @param zip The ZIP to be queried or empty string ("") if every ZIP is allowed
 * @return A map Result->Probability
 */
  public HashMap<String, Double> queryDOB(String name, String zip){
    HashMap<String, Double> result = new HashMap<>();

    for (ProbabilisticPersonTuple pp : getEntries()) {
      HashMap<String, Double> names = pp.getNames();
      HashMap<String, Double> dobs = pp.getDobs();
      HashMap<String, Double> zips = pp.getZips();

      Double nameProbability = names.get(name);
      Double zipProbability = zips.get(zip);

      for(Map.Entry<String, Double> nEntry : names.entrySet()) {
        for(Map.Entry<String, Double> dEntry : dobs.entrySet()) {
          for(Map.Entry<String, Double> zEntry : zips.entrySet()) {
            String dobKey = dEntry.getKey().toString();
            Double dobProbability = Double.parseDouble(dEntry.getValue().toString());

            if(zip == "") {
              // consider only name
              if(nEntry.getKey() == name) {
                result.put(dobKey, nameProbability * dobProbability);
              }
            } else if(name == "") {
              // consider only zip
              if(zEntry.getKey() == zip) {
                result.put(dobKey, dobProbability * zipProbability);
              }
            } else {
              // consider name and dob
              if(zEntry.getKey() == zip && nEntry.getKey() == name) {
                result.put(dobKey, nameProbability * zipProbability * dobProbability);
              }
            }
          }
        }
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return "---ProbabilisticPersonTable---\n" + entries.stream().map(x->x.toString()).collect(Collectors.joining("\n")) + "\n-----------------\n";
  }

}

/**
 * Represents one person in the probabilistic persons table
 */
class ProbabilisticPersonTuple{
  // A set of all combined FIDs
  HashSet<Integer> fids;
  // Maps of all attributes and their probabilities
  HashMap<String, Double> names;
  HashMap<String, Double> zips;
  HashMap<String, Double> dobs;

  /**
   * Creates a new ProbabilisticPersonTuple with the possible world sets
   * @param fids
   * @param names
   * @param zips
   * @param dobs
   */
  public ProbabilisticPersonTuple(HashSet<Integer> fids, HashMap<String, Double> names, HashMap<String, Double> zips, HashMap<String, Double> dobs) {
    this.fids = fids;
    this.names = names;
    this.zips = zips;
    this.dobs = dobs;
  }

  /**
   * 
   * @return the FID set
   */
  public HashSet<Integer> getFIDs() {
    return fids;
  }
  /**
   * @return the Name->Probability map
   */
  public HashMap<String, Double> getNames() {
    return names;
  }
  /**
   * @return the ZIP->Probability map
   */  
  public HashMap<String, Double> getZips() {
    return zips;
  }
  /**
   * @return the DOB->Probability map
   */
  public HashMap<String, Double> getDobs() {
    return dobs;
  }

  @Override
  public String toString() {
    return "[dobs=" + dobs + ", fids=" + fids + ", names=" + names + ", zips=" + zips + "]";
  }

}


/**
 * A mock person database table
 */
class PersonTable{
  List<PersonTuple> entries;

  /**
   * Creates an empty PersonTable
   */
  public PersonTable(){
    entries = new ArrayList<>();
  }

  /**
   * Adds a new person tuple
   * @param fID The FID of the person
   * @param name The name of the person
   * @param zip The ZIP of the person
   * @param dob The date of birth of the person
   */
  public void insertTuple(int fID, String name, String zip, String dob){
    entries.add(new PersonTuple(fID, name, zip, dob));
  }

  /**
   * Returns a person by its FID if it exists
   * @param FID The FID of the queried person
   * @return An optional PersonTuple
   */
  public Optional<PersonTuple> getByFID(int FID){
    return entries.stream().filter(x -> x.FID == FID).findAny();
  }

  /**
   * 
   * @return All person tuples in the table
   */
  public List<PersonTuple> getEntries(){
    return entries;
  }

  @Override
  public String toString() {
    return "---PersonTable---\n" + entries.stream().map(x->x.toString()).collect(Collectors.joining("\n")) + "\n-----------------\n";
  }

}

/**
 * Represents one person in the mock database table
 */
class PersonTuple{
  int FID;
  String name;
  String zip;
  String dob;

  /**
   * Creates a new Person Tuple from its arguments
   * @param fID The FID of the person
   * @param name The name of the person
   * @param zip The ZIP of the person
   * @param dob The date of birth of the person
   */
  public PersonTuple(int fID, String name, String zip, String dob) {
    FID = fID;
    this.name = name;
    this.zip = zip;
    this.dob = dob;
  }

  public int getFID() {
    return FID;
  }
  public String getName() {
    return name;
  }
  public String getZip() {
    return zip;
  }
  public String getDob() {
    return dob;
  }

  @Override
  public String toString() {
    return "[FID=" + FID + ", dob=" + dob + ", name=" + name + ", zip=" + zip + "]";
  }
}