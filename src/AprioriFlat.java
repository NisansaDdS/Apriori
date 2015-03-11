import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class AprioriFlat {

    ArrayList<ItemSet> transactions=new ArrayList<ItemSet>();

    HashMap<ItemSet,Integer> MapOutput=new HashMap<ItemSet,Integer>();
    ArrayList<HashMap<ItemSet,Integer>> reduceOutputs=new ArrayList<HashMap<ItemSet,Integer>>();
    double thresholdPer=66.0;
    int threashold=0;



    public static void main(String[] args) {
	// write your code here
        AprioriFlat af=new AprioriFlat();
    }


    public AprioriFlat() {

        loadTransactions();
        threashold=(int)((thresholdPer*transactions.size())/100);
        Map1();
        Reduce();
        HashMap<ItemSet,Integer> newFrequentItems=reduceOutputs.get(reduceOutputs.size()-1);
        //System.out.println(newFrequentItems.size());
        while(newFrequentItems.size()!=0){
            Map2(newFrequentItems);
            Reduce();
            newFrequentItems=reduceOutputs.get(reduceOutputs.size()-1);
        }
        print();
    }

    public void loadTransactions(){
        transactions.add(new ItemSet(new String[]{"A","B","C","D","E","F"}));
        transactions.add(new ItemSet(new String[]{"B","H","S","C","F","T"}));
        transactions.add(new ItemSet(new String[]{"A","U","O","F","W","D"}));
        transactions.add(new ItemSet(new String[]{"O","A","B","C","F","X"}));
        transactions.add(new ItemSet(new String[]{"O","A","C","D","F","Y"}));
        transactions.add(new ItemSet(new String[]{"B","C","X","E","W","Z"}));

    }


    public void print(){
        for (int i = 0; i < reduceOutputs.size(); i++) {
            System.out.println("Generation " + i);
            HashMap<ItemSet,Integer> output=reduceOutputs.get(i);
            Iterator<ItemSet> itr=output.keySet().iterator();
            while(itr.hasNext()) {
                ItemSet is = itr.next();
                System.out.println(is.toString() + " " + output.get(is));
            }
            System.out.print("\n");
        }
    }


    public void Map1(){
        MapOutput=new HashMap<ItemSet,Integer>();

        for (int i = 0; i < transactions.size(); i++) {
            String[] tr=transactions.get(i).items;
            for (int j = 0; j <tr.length ; j++) {
                AddToMapWithCombine(new ItemSet(tr[j]));
            }
        }
    }

    public void Map2(HashMap<ItemSet,Integer> frequentItems){
        MapOutput=new HashMap<ItemSet,Integer>();

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
                           ArrayList<ItemSet> subSets = candidate.createSubsets();
                           if (frequentItems.keySet().containsAll(subSets)) { //checkWhetherSubsetsAreFrequent Will work?
                               if (candidate.IsThisSetAsubstOf(transac)) { //Candidate is in the sentence
                                   AddToMapWithCombine(candidate);
                               }
                           }
                           transCands.add(candidate);
                       }
                    }
                }
            }
        }
    }

    public void Reduce(){ //Nothing much to do here in this case because combiner has already done all the work
        HashMap<ItemSet,Integer> reducerOutput=new HashMap<ItemSet,Integer>();
        Iterator<ItemSet> itr=MapOutput.keySet().iterator();
        while(itr.hasNext()) {
            ItemSet source = itr.next();
            int count=MapOutput.get(source);
            if(count>threashold) {
                reducerOutput.put(source, count);
            }
        }

        reduceOutputs.add(reducerOutput);
    }

    private void AddToMapWithCombine(ItemSet candidate) {
        Integer currentCandidateCount=MapOutput.get(candidate);
        if(currentCandidateCount==null) {
            currentCandidateCount=1;
        }
        else{
            currentCandidateCount++;
            MapOutput.remove(candidate);
        }
        MapOutput.put(candidate, currentCandidateCount);
    }


    public class ItemSet{
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


        public ArrayList<ItemSet> createSubsets(){
            ArrayList<ItemSet> subsets=new ArrayList<ItemSet>();
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
