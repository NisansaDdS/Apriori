import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AprioriFlat {

    ArrayList<ItemSet> transactions=new ArrayList<ItemSet>();

    HashMap<ItemSet,Integer> MapOutput=new HashMap<ItemSet,Integer>();
    ArrayList<HashMap<ItemSet,Integer>> reduceOutputs=new ArrayList<HashMap<ItemSet,Integer>>();
    double thresholdPer=2.0;
    int threashold=0;



    public static void main(String[] args) {
	// write your code here
        AprioriFlat af=new AprioriFlat();
    }


    public AprioriFlat() {
        threashold=(int)(thresholdPer*transactions.size());
        Map1();
        Reduce();
        HashMap<ItemSet,Integer> newFrequentItems=reduceOutputs.get(reduceOutputs.size()-1);
        while(newFrequentItems.size()!=0){
            Map2(newFrequentItems);
            Reduce();
            newFrequentItems=reduceOutputs.get(reduceOutputs.size()-1);
        }
    }

    public void print(){
        for (int i = 0; i < reduceOutputs.size(); i++) {
            System.out.print("Generation "+i);
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
            for (int j = 0; j < transac.items.length; j++) {
                Iterator<ItemSet> itr=frequentItems.keySet().iterator();
                while(itr.hasNext()){
                    ItemSet source=itr.next();
                    if(!source.contains(transac.items[j])) {
                        ItemSet candidate = new ItemSet(source, transac.items[j]);
                        ArrayList<ItemSet> subSets=candidate.createSubsets();
                        if(frequentItems.keySet().containsAll(subSets)){ //checkWhetherSubsetsAreFrequent Will work?
                            if(candidate.IsThisSetAsubstOf(transac)){ //Candidate is in the sentence
                                AddToMapWithCombine(candidate);
                            }
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
            MapOutput.remove(candidate);
        }
        else{
            currentCandidateCount++;
        }
        MapOutput.put(candidate, currentCandidateCount);
    }


    public class ItemSet{
        String[] items;

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
        }

        //Create itemset with a single given strings
        public ItemSet(String item) {
            this(new String[]{item});
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
            if(i2.items.length!=items.length){
                return false;
            }

            return IsThisSetAsubstOf(i2);
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
