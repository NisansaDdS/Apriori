import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AprioriFlat {

    ArrayList<ItemSet> transactions=new ArrayList<ItemSet>();

    HashMap<ItemSet,Integer> MapOutput=new HashMap<ItemSet,Integer>();
    ArrayList<HashMap<ItemSet,Integer>> reduceOutputs=new ArrayList<HashMap<ItemSet,Integer>>();



    public static void main(String[] args) {
	// write your code here


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

    }




}
