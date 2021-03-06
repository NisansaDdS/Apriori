import java.io.*;
import java.util.*;

public class AprioriFlat implements Serializable  {

    ArrayList<ItemSet> transactions=new ArrayList<ItemSet>();

    HashMap<ItemSet,Integer> supportMapOutput =new HashMap<ItemSet,Integer>();
    ArrayList<HashMap<ItemSet,Integer>> supportReduceOutputs =new ArrayList<HashMap<ItemSet,Integer>>();
    HashMap<ItemSet,Integer> supportReduceOutputsLastGen =new HashMap<ItemSet,Integer>();
    double supportThresholdPer =60;//1;
    int supportThreashold =0;
    boolean findNegativePatterns=false;

    HashMap<ItemSet,HashMap<ItemSet,Integer>> confidanceMapOutput =new HashMap<ItemSet,HashMap<ItemSet,Integer>>();
    HashMap<ItemSet,HashMap<ItemSet,Double>> confidanceReduceOutput1=new HashMap<ItemSet,HashMap<ItemSet,Double>>();
    ArrayList<Rule> confidanceReduceOutput2=new ArrayList<Rule>();
    double confidanceThresholdPer =75;
    static boolean test1=false;
    static boolean test2=false;



    public static void main(String[] args) {
	// write your code here
        if(args.length!=1){
            test1=false;
            test2=false;
        }
        else{
            try {
                int num = Integer.valueOf(args[0]);
                if(num==1){
                    test1=true;
                    test2=false;
                }
                else if(num==2){
                    test1=false;
                    test2=true;
                }
                else{
                    test1=false;
                    test2=false;
                }
            }
            catch(Exception e){
                test1=false;
                test2=false;
            }
        }

        AprioriFlat af=new AprioriFlat();
    }


    public AprioriFlat() {

       loadTransactions();
        supportThreashold =(int)((supportThresholdPer *transactions.size())/100);
        System.out.println("Creating frequent itemsets");
        Map1();
        Reduce1();
        HashMap<ItemSet,Integer> newFrequentItems= supportReduceOutputs.get(supportReduceOutputs.size()-1);
        int i=1;
        while(newFrequentItems.size()!=0){
            System.out.println("Running loop "+i);
            Map21(newFrequentItems);
            Reduce1();
            newFrequentItems= supportReduceOutputs.get(supportReduceOutputs.size()-1);
            i++;
        }
       // Serialize();
       // print();

       // deSerialize();
     /*   Map3();
        Reduce2();
        Map4();
        Reduce3();*/
        Map31();

        print();
    }



    public void Serialize(){
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream("Backup.dat");
            out = new ObjectOutputStream(fos);
            out.writeObject(supportReduceOutputs);

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void deSerialize(){
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream("Backup.dat");
            in = new ObjectInputStream(fis);
            supportReduceOutputs = (ArrayList<HashMap<ItemSet,Integer>>)in.readObject();
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadTransactions() {

        if (test1) {
            transactions.add(new ItemSet(new String[]{"A", "B", "C", "D", "E", "F"}));
            transactions.add(new ItemSet(new String[]{"B", "H", "S", "C", "F", "T"}));
            transactions.add(new ItemSet(new String[]{"A", "U", "O", "F", "W", "D"}));
            transactions.add(new ItemSet(new String[]{"O", "A", "E", "C", "F", "X"}));
            transactions.add(new ItemSet(new String[]{"G", "A", "C", "D", "E", "F"}));
        } else if (test2) {
            transactions.add(new ItemSet(new String[]{"J", "B", "C", "D", "E", "F"}));
            transactions.add(new ItemSet(new String[]{"B", "H", "S", "C", "F", "T"}));
            transactions.add(new ItemSet(new String[]{"J", "U", "F", "W", "D"}));
            transactions.add(new ItemSet(new String[]{"J", "E", "C", "F", "X"}));
            transactions.add(new ItemSet(new String[]{"G", "J", "C", "D", "E", "F"}));
        } else {
            String[][] data = readFile("./Data/FoodMart.csv");
            for (int i = 0; i < data.length; i++) {
                transactions.add(new ItemSet(data[i]));
            }
        }

    }


    public String[][] readFile(String filename) {
        String[][] data=new String[0][0];
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            String[] titles=line.split(",");//dt;ID;Age;RA;PS;PID;Am;As;SP -> //date and time;Customer ID;Age;Residence Area;Product subclass;Product ID;Amount;Asset;Sales price
            line = br.readLine();
            ArrayList<String[]> dataLines=new ArrayList<String[]>();
            while (line != null) {
                String[] lineParts=line.split(",");
                ArrayList<String> items=new ArrayList<String>();
                for (int i = 0; i <lineParts.length ; i++) {
                    int isBought=Integer.parseInt(lineParts[i].trim());

                    if(findNegativePatterns) {
                        if (isBought == 1) {
                            items.add(titles[i] + "_T");
                        } else {
                            items.add(titles[i] + "_F");
                        }
                    }
                    else{
                        if (isBought == 1) {
                            items.add(titles[i]);
                        }
                    }

                }
                lineParts=new String[items.size()];
                for (int i = 0; i <items.size() ; i++) {
                    lineParts[i]=items.get(i);
                }
                dataLines.add(lineParts);
                line = br.readLine();
            }

            data=new String[dataLines.size()][];
            for (int i = 0; i <dataLines.size() ; i++) {
                data[i]=dataLines.get(i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


    public void print(){
        System.out.println("Frequent items\n");
        for (int i = 0; i < supportReduceOutputs.size(); i++) {
            HashMap<ItemSet,Integer> output= supportReduceOutputs.get(i);
            if(!output.isEmpty()){
                System.out.println("Generation " + i);
            }
            Iterator<ItemSet> itr=output.keySet().iterator();
            while(itr.hasNext()) {
                ItemSet is = itr.next();
                System.out.println(is.toString() + " " + output.get(is));
            }
            System.out.print("\n");
        }
        System.out.println("Rules\n");
        Collections.sort(confidanceReduceOutput2);
        for (int i = 0; i < confidanceReduceOutput2.size(); i++) {
            System.out.println(confidanceReduceOutput2.get(i));
        }
    }


    public void Map1(){
        supportMapOutput =new HashMap<ItemSet,Integer>();

        for (int i = 0; i < transactions.size(); i++) {
            String[] tr=transactions.get(i).items;
            for (int j = 0; j <tr.length ; j++) {
                AddToSupportMapWithCombine(new ItemSet(tr[j]));
            }
        }
    }



    public void Map21(HashMap<ItemSet,Integer> frequentItems) {
        System.out.println("Mapping");
        supportMapOutput = new HashMap<ItemSet, Integer>();
        ArrayList<ItemSet> transCands=new ArrayList<ItemSet>();
        Iterator<ItemSet> itr=frequentItems.keySet().iterator();
        System.out.println(frequentItems.keySet().size());
        while(itr.hasNext()){
            ItemSet source=itr.next();
            Iterator<ItemSet> itr1=frequentItems.keySet().iterator();
            while(itr1.hasNext()){
                ItemSet source1=itr1.next();
                ItemSet union=source.takeUnion(source1);
                if(union!=null){
                    if (!transCands.contains(union)) {
                        transCands.add(union);
                        ArrayList<ItemSet> subSets = union.createKmin1Subsets();
                        if (frequentItems.keySet().containsAll(subSets)) {
                            for (int i = 0; i < transactions.size(); i++) {
                                ItemSet transac = transactions.get(i);
                                if (union.IsThisSetAsubstOf(transac)) { //Candidate is in the sentence
                                    AddToSupportMapWithCombine(union);
                                }
                            }
                        }
                    }
                }
            }
        }
    }




    public void Map2(HashMap<ItemSet,Integer> frequentItems){
        supportMapOutput =new HashMap<ItemSet,Integer>();

        for (int i = 0; i < transactions.size(); i++) {
            System.out.println((i+1)+"out of "+transactions.size()+" trans" );
            ItemSet transac=transactions.get(i);
            ArrayList<ItemSet> transCands=new ArrayList<ItemSet>();

            for (int j = 0; j < transac.items.length; j++) {
              //  System.out.println("***********************************************");
             //   System.out.println((j+1)+"out of "+transac.items.length+" trans items");
                Iterator<ItemSet> itr=frequentItems.keySet().iterator();
                int count=frequentItems.keySet().size();
                int l=0;
                while(itr.hasNext()){
                //    System.out.println("."+(l+1)+"out of "+count+" freq items");
                    l++;
                    ItemSet source=itr.next();
                    Boolean pass=false;
                    for (int k = 0; k <source.items.length ; k++) {
                        if(source.items[k].endsWith("_T")){
                            pass=true;
                            break;
                        }
                    }

                    if(!source.contains(transac.items[j])) {
                        if(!pass){
                            if(transac.items[j].endsWith("_T")){
                                pass=true;
                            }
                        }

                   //    if(pass) {
                            ItemSet candidate = new ItemSet(source, transac.items[j]);
                            if (!transCands.contains(candidate)) {
                                ArrayList<ItemSet> subSets = candidate.createKmin1Subsets();
                                if (frequentItems.keySet().containsAll(subSets)) {
                                    if (candidate.IsThisSetAsubstOf(transac)) { //Candidate is in the sentence
                                        AddToSupportMapWithCombine(candidate);
                                    }
                                }
                                transCands.add(candidate);
                            }
                       // }

                    }
                }
            }
        }
    }





    private void AddToSupportMapWithCombine(ItemSet candidate) {
        Integer currentCandidateCount= supportMapOutput.get(candidate);
        if(currentCandidateCount==null) {
            currentCandidateCount=1;
        }
        else{
            currentCandidateCount++;
            supportMapOutput.remove(candidate);
        }
        supportMapOutput.put(candidate, currentCandidateCount);
    }


    public void Reduce1(){ //Nothing much to do here in this case because combiner has already done all the work
        System.out.println("Reducing");
        HashMap<ItemSet,Integer> reducerOutput=new HashMap<ItemSet,Integer>();
        Iterator<ItemSet> itr= supportMapOutput.keySet().iterator();
        while(itr.hasNext()) {
            ItemSet source = itr.next();
            int count= supportMapOutput.get(source);
            if(count>= supportThreashold) {
                reducerOutput.put(source, count);
            }
        }

        supportReduceOutputs.add(reducerOutput);
        supportReduceOutputsLastGen=reducerOutput;
    }


    HashMap<ItemSet,Integer> allSupportOutputs =new HashMap<ItemSet,Integer>();

    public void Map31() {

        //Unroll
        for (int i = 0; i < supportReduceOutputs.size(); i++) {
            HashMap<ItemSet,Integer> current=supportReduceOutputs.get(i);
            allSupportOutputs.putAll(current);
            if(current.size()>0){
                supportReduceOutputsLastGen=current;
            }
        }
        System.out.println(supportReduceOutputsLastGen.size());
        Iterator<ItemSet> itr = supportReduceOutputsLastGen.keySet().iterator();
        while (itr.hasNext()) {
            ItemSet source = itr.next();
            ruleGenerator(source,source);

        }
    }


        public void ruleGenerator(ItemSet set,ItemSet original){
            if(set.items.length>1){
                ArrayList<ItemSet> subsets = set.createKmin1Subsets();
                for (int i = 0; i < subsets.size(); i++) {
                    ItemSet subSet=subsets.get(i);
                    int supportL=allSupportOutputs.get(original);
                    int supportX=allSupportOutputs.get(subSet);
                    double confidence=(supportL*100)/supportX;
                    if(confidence>=confidanceThresholdPer){
                        confidanceReduceOutput2.add(new Rule(subSet,original.getSetDifference(subSet),confidence));
                        ruleGenerator(subSet,original);
                    }
                }
            }
        }

    public void Map3(){
        System.out.println("Mapping");
        for (int i = 0; i < supportReduceOutputs.size(); i++) {
            HashMap<ItemSet,Integer> reduGeneration= supportReduceOutputs.get(i);
            Iterator<ItemSet> itr=reduGeneration.keySet().iterator();
            while(itr.hasNext()) {
                ItemSet source = itr.next();
                ArrayList<ItemSet> subsets=  source.createSubsets();
                for (int j = 0; j < subsets.size(); j++) {
                    addToConfidanceMapWithCombine(source,reduGeneration.get(source),subsets.get(j));
                }
            }
        }
    }

    public void Map4(){

    }

    public void Reduce2() {
        System.out.println("Reducing");
        Iterator<ItemSet> itr=confidanceMapOutput.keySet().iterator();
        while(itr.hasNext()) {
            ItemSet subset = itr.next();
           // System.out.println(subset);

            HashMap<ItemSet,Integer> superSets=confidanceMapOutput.get(subset);
            Integer denominator=superSets.get(subset);


            Iterator<ItemSet> itr1=superSets.keySet().iterator();
            while(itr1.hasNext()) {
                ItemSet superSet = itr1.next();
                if(!superSet.equals(subset)) {
                    Integer numerator=superSets.get(superSet);
                    double confidence=((double)(numerator*100))/denominator;
                    if(confidence>=confidanceThresholdPer){
                        ItemSet q=superSet.getSetDifference(subset); //These lines must go to mapper 4
                        HashMap<ItemSet,Double> Ps=confidanceReduceOutput1.get(q);
                        if(Ps==null){
                            Ps=new HashMap<ItemSet,Double>();
                        }
                        Ps.put(subset, confidence);
                        confidanceReduceOutput1.put(q,Ps) ;  //These lines must go to mapper 4
                    }
                }
            }
            //System.out.println(confidanceReduceOutput1.size());
        }
    }






    public void Reduce3() {
        System.out.println("Reducing");
        Iterator<ItemSet> itr=confidanceReduceOutput1.keySet().iterator();
        while(itr.hasNext()) {
            ItemSet q = itr.next();
            HashMap<ItemSet,Double> Ps=confidanceReduceOutput1.get(q);

            //Find Super sets
            ArrayList<ItemSet> superSets=new ArrayList<ItemSet>();
            Iterator<ItemSet> itr1=Ps.keySet().iterator();
            while(itr1.hasNext()){
                ItemSet a=itr1.next();
                Iterator<ItemSet> itr2=Ps.keySet().iterator();
                while(itr2.hasNext()){
                    ItemSet b=itr2.next();
                    if(!a.equals(b)){
                        if(a.IsThisSetAsubstOf(b)){
                            superSets.add(b);
                        }
                    }
                }
            }

            //Remove supersets
            for (int i = 0; i <superSets.size() ; i++) {
            Ps.remove(superSets.get(i));
            }

            //Create rules
            itr1=Ps.keySet().iterator();
            while(itr1.hasNext()) {
                ItemSet p = itr1.next();
                confidanceReduceOutput2.add(new Rule(p,q,Ps.get(p)));
            }

        }
    }

    public void addToConfidanceMapWithCombine(ItemSet source,Integer sourceVal,ItemSet subset){
        HashMap<ItemSet,Integer> sources=confidanceMapOutput.get(subset);
        if(sources==null){
            sources=new HashMap<ItemSet,Integer>();
        }
        else{
            confidanceMapOutput.remove(subset);
        }
        sources.remove(source); //Logically impossible to happen in this case. But leaving here for thread safety.
        sources.put(source, sourceVal);
        confidanceMapOutput.put(subset,sources);
    }


    public class Rule implements Serializable,Comparable {
        ItemSet p,q;



        double confidence =0;

        public Rule(ItemSet p, ItemSet q, double confidence) {
            this.p = p;
            this.q = q;
            this.confidence = confidence;
        }

        public String toString(){
            StringBuilder sb=new StringBuilder();
            sb.append(p);
            sb.append(" => ");
            sb.append(q);
            sb.append(" : ");
            sb.append(confidence);
            sb.append("%");
            return sb.toString();
        }

        @Override
        public int compareTo(Object o) {
            Rule r=(Rule)o;
            if(confidence>r.confidence){
                return -1;
            }
            else if(confidence<r.confidence){
                return 1;
            }
            return 0;
        }
    }


    public class ItemSet implements Serializable {
        String[] items;

        //Create itemset with a single given strings
        public ItemSet(String item) {
            this(new String[]{item});
        }

        /**
         * Create itemset with given strings
         * @param items
         */
        public ItemSet(String[] items) {
            this.items = items;
        }

        public ItemSet(ArrayList<String> itemsL) {
           items =new String[itemsL.size()];
            for (int i = 0; i <itemsL.size() ; i++) {
                items[i]=itemsL.get(i);
            }
        }

        public ItemSet takeUnion(ItemSet is){
            HashSet<String> allIitems=new HashSet<String>();
            for (int i = 0; i <items.length ; i++) {
                allIitems.add(items[i]);
            }
            for (int i = 0; i < is.items.length; i++) {
                allIitems.add(is.items[i]);
            }
            if(allIitems.size()!=(items.length+1)){
                return null;
            }
            String[] finItems=new String[allIitems.size()];
            Iterator<String> itr=allIitems.iterator();
            int i=0;
            while(itr.hasNext()){
                finItems[i]=itr.next();
                i++;
            }
            return (new ItemSet(finItems));
        }


        //Create k+1 generation itemset with the given k generation itemset by adding the new item
        public ItemSet(ItemSet i,String newItem) {
            this(i.items,newItem);
        }

        //Create itemset with given strings
        public ItemSet(String[] oldItems,String newItem) {
            items =new String[oldItems.length+1];
            int i = 0;
            for (; i <oldItems.length ; i++) {
                items[i]=oldItems[i];
            }
            items[i]=newItem;

           Arrays.sort(items);
        }

        public ItemSet getSetDifference(ItemSet b){
            ArrayList<String> passed=new ArrayList<String>();
            for (int i = 0; i <items.length ; i++) {
                boolean pass=true;
                for (int j = 0; j < b.items.length; j++) {
                    if(items[i].equalsIgnoreCase(b.items[j])){
                        pass=false;
                        break;
                    }
                }
                if(pass){
                    passed.add(items[i]);
                }
            }
            return new ItemSet(passed);
        }

        public boolean contains(String s){
            for (int j = 0; j <items.length ; j++) {
                if(items[j].equalsIgnoreCase(s)){
                   return true;
                }
            }
            return false;
        }

        //Create subset itemset without the iten with the given index
        public ItemSet(ItemSet it,int i) {
            items=new String[it.items.length-1];
            int k=0;
            for (int j = 0; j < it.items.length; j++) {
                if(j!=i){
                    items[k]=it.items[j];
                    k++;
                }
            }
        }


        public ArrayList<ItemSet> createSubsets() {
            ArrayList<ItemSet> subsets = new ArrayList<ItemSet>();

            ArrayList<ItemSet> toGenSubsets = new ArrayList<ItemSet>();
            toGenSubsets.add(this);
            while (!toGenSubsets.isEmpty()) {
                ItemSet candidate = toGenSubsets.get(0);
                toGenSubsets.remove(0);
                ArrayList<ItemSet> kminSubs = candidate.createKmin1Subsets();
                toGenSubsets.addAll(kminSubs);
                subsets.add(candidate);
            }
            return subsets;
        }


        /**
         * Create the subsets of genration K-1
         * @return
         */
        public ArrayList<ItemSet> createKmin1Subsets(){
            ArrayList<ItemSet> subsets=new ArrayList<ItemSet>();
            if(items.length==1){ //Do not make empty itemsets
                return subsets;
            }
            for (int i = 0; i <items.length; i++) {
                subsets.add(new ItemSet(this,i));
            }
            return subsets;
        }


        public boolean equals(Object obj){
            ItemSet i2=(ItemSet)obj;
            //System.out.println("Equals called!");
            if(i2.items.length!=items.length){
                return false;
            }
            return IsThisSetAsubstOf(i2);
        }


        public int hashCode(){
            int result=17;
            result=37*result+toString().hashCode();
            return(result);
        }

        public boolean IsThisSetAsubstOf(ItemSet i2) {
            for (int i = 0; i <items.length ; i++) {
                boolean match=false;
                for (int j = 0; j <i2.items.length ; j++) {
                    if(i2.items[j].equalsIgnoreCase(items[i])){
                        match=true;
                        break;
                    }
                }
                if(!match){
                    return false;
                }
            }
            return true;
        }


        public String toString(){
            StringBuilder sb=new StringBuilder();
            sb.append("[");
            for (int i = 0; i <items.length; i++) {
                sb.append(items[i]);
                if(i!=items.length-1){
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }




}
