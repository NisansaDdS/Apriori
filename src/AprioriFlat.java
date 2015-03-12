import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class AprioriFlat implements Serializable  {

    ArrayList<ItemSet> transactions=new ArrayList<ItemSet>();

    HashMap<ItemSet,Integer> supportMapOutput =new HashMap<ItemSet,Integer>();
    ArrayList<HashMap<ItemSet,Integer>> supportReduceOutputs =new ArrayList<HashMap<ItemSet,Integer>>();
    double supportThresholdPer =5.0;
    int supportThreashold =0;


    HashMap<ItemSet,HashMap<ItemSet,Integer>> confidanceMapOutput =new HashMap<ItemSet,HashMap<ItemSet,Integer>>();
    HashMap<ItemSet,HashMap<ItemSet,Double>> confidanceReduceOutput1=new HashMap<ItemSet,HashMap<ItemSet,Double>>();
    ArrayList<Rule> confidanceReduceOutput2=new ArrayList<Rule>();
    double confidanceThresholdPer =20.0;


    public static void main(String[] args) {
	// write your code here
        AprioriFlat af=new AprioriFlat();
    }


    public AprioriFlat() {

     /*  loadTransactions();
        supportThreashold =(int)((supportThresholdPer *transactions.size())/100);
        Map1();
        Reduce1();
        HashMap<ItemSet,Integer> newFrequentItems= supportReduceOutputs.get(supportReduceOutputs.size()-1);
        int i=1;
        while(newFrequentItems.size()!=0){
            System.out.println("Running loop "+i);
            Map2(newFrequentItems);
            Reduce1();
            newFrequentItems= supportReduceOutputs.get(supportReduceOutputs.size()-1);
            i++;
        }
       // Serialize();

       // deSerialize();
        Map3();
        Reduce2();
        Map4();
        Reduce3();

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

    public void loadTransactions(){
       String[][] data=readFile("./Data/D01.csv");
        for (int i = 0; i < data.length; i++) {
            transactions.add(new ItemSet(data[i]));
        }

      /*  transactions.add(new ItemSet(new String[]{"A","B","C","D","E","F"}));
        transactions.add(new ItemSet(new String[]{"B","H","S","C","F","T"}));
        transactions.add(new ItemSet(new String[]{"A","U","O","F","W","D"}));
        transactions.add(new ItemSet(new String[]{"O","A","B","C","F","X"}));
        transactions.add(new ItemSet(new String[]{"O","A","C","D","F","Y"}));
        transactions.add(new ItemSet(new String[]{"B","C","X","E","W","Z"}));*/

    }


    public String[][] readFile(String filename) {
        String[][] data=new String[0][0];
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            String[] titles=line.split(";");//dt;ID;Age;RA;PS;PID;Am;As;SP -> //date and time;Customer ID;Age;Residence Area;Product subclass;Product ID;Amount;Asset;Sales price
            ArrayList<String[]> dataLines=new ArrayList<String[]>();
            while (line != null) {
                String[] lineParts=line.split(";");
                for (int i = 0; i <lineParts.length ; i++) {
                    lineParts[i]=titles[i]+"_"+lineParts[i].trim();
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

    public void Map2(HashMap<ItemSet,Integer> frequentItems){
        supportMapOutput =new HashMap<ItemSet,Integer>();

        for (int i = 0; i < transactions.size(); i++) {
            ItemSet transac=transactions.get(i);
            ArrayList<ItemSet> transCands=new ArrayList<ItemSet>();

            for (int j = 0; j < transac.items.length; j++) {
                Iterator<ItemSet> itr=frequentItems.keySet().iterator();
                while(itr.hasNext()){
                    ItemSet source=itr.next();
                    if(!source.contains(transac.items[j])) {
                       ItemSet candidate = new ItemSet(source, transac.items[j]);
                       if(!transCands.contains(candidate)) {
                           ArrayList<ItemSet> subSets = candidate.createKmin1Subsets();
                           if (frequentItems.keySet().containsAll(subSets)) {
                               if (candidate.IsThisSetAsubstOf(transac)) { //Candidate is in the sentence
                                   AddToSupportMapWithCombine(candidate);
                               }
                           }
                           transCands.add(candidate);
                       }
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
        HashMap<ItemSet,Integer> reducerOutput=new HashMap<ItemSet,Integer>();
        Iterator<ItemSet> itr= supportMapOutput.keySet().iterator();
        while(itr.hasNext()) {
            ItemSet source = itr.next();
            int count= supportMapOutput.get(source);
            if(count> supportThreashold) {
                reducerOutput.put(source, count);
            }
        }

        supportReduceOutputs.add(reducerOutput);
    }




    public void Map3(){
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
            sources.put(source,sourceVal);
        confidanceMapOutput.put(subset,sources);
    }


    public class Rule implements Serializable {
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
