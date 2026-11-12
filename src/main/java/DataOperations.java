import java.util.*;

public class DataOperations 
{
    public static void printWithIterator(Collection<Client> collection) 
    {
        Iterator<Client> iterator = collection.iterator();
        while (iterator.hasNext()) 
        {
            System.out.println(iterator.next());
        }
    }

    public static void sortByBudgetLambda(List<Client> list) 
    {
        list.sort((c1, c2) -> Double.compare(c1.getSearchBudget(), c2.getSearchBudget()));
    }

    public static void sortByNameLambda(List<Client> list) 
    {
        list.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
    }

    public static void printSortedByMap(Map<Integer, Client> baseMap) 
    {
        TreeMap<Integer, Client> sortedMap = new TreeMap<>(baseMap);
        for (Map.Entry<Integer, Client> entry : sortedMap.entrySet()) 
        {
            System.out.println("Key (ID): " + entry.getKey() + " -> " + entry.getValue());
        }
    }
}
