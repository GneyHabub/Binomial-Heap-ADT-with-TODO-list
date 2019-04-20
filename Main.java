//Alexander Krivonosov. Group -6

import java.util.Scanner;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Proof of complexity.
 * Since we were required to implement a mergeable heap which performs its main operations in log(n), I decided to implement binomial heap.
 *
 * We know that if the height of the binomial tree is k, then number of nodes in this tree is 2^k.
 * So, if we say that if n is the number of nodes, then the height of the tree is log(n).
 * Now we can say, that the AuxiliaryMerge function takes st most log(n) + 1 steps which is O(log(n)). It can be seen as follows.
 * Let H1 contain n1 nodes and H2 contain n2 nodes, so that n = n1 + n2. Then H1 contains at most log(n1) + 1 roots and H2 contains
 * at most log(n2) + 1 roots, and so H contains at most log(n1)+log(n2) + 2 ≤ 2 * log(n)+2 = O(log(n)) right after the call of AuxiliaryMerge.
 * In the Merge function the 'main' while loop has complexity of O(1) and there are at most floor(log(n1)) + floor(log(n2)) + 2 iterations, since we get
 * this number of nodes to restructure. So the total time is O(log(n)).
 *
 * The Insertion is simply creating a new heap with only one node anf then merging it with the rest of the tree. So it is O(log(n)) too.
 *
 * The Max function simply searches for the largest root in the heap, so its complexity is O(r), where r is a number of trees in the heap, which at most
 * can be equal to floor(log(n)) + 1. So the running complexity of Max is O(log(n)).
 *
 * The RemoveMax function works as follows. It finds the maximum node in the heap, deletes it and creates the linked list out of its children in reverse order
 * which are then merged with function Merge. So the overall time complexity is still O(log(n)).
 */

/**
 * A few words about testing.
 * First version of my program for TODO list used array to store objects of class date. Finding particular task in particular days required O(n^2) operation.
 * After some tests with significantly large n, it was clear that it takes too much time, so I changed the implementation.
 * I started using TreeMap instead of Array because it performed much better in terms of time complexity.
 */
public class Main {
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        int n = reader.nextInt();
        reader.nextLine();

        String[] datesTemp = new String[5000];
        int numOfDates = 0; //number of dates
        ComparatorOfDates comp = new ComparatorOfDates();
        Map < String, Date > dates = new TreeMap < > (comp); //TreeMap for dates

        for (int i = 0; i < n; i++) {
            String[] inp = reader.nextLine().split("\\s"); //read line and split by space
            if (inp[0].equals("DO")) { //if it is delete operation, delete task from the day immediately
                dates.get(inp[1]).removeTask();
            } else {
                if (!(Arrays.asList(datesTemp).contains(inp[0]))) { //if it is the first time we meet this date, add it to TreeMap
                    datesTemp[numOfDates] = inp[0];
                    dates.put(inp[0], new Date(inp[0]));
                    numOfDates++;
                }
                Task temp = new Task(inp[1], Integer.parseInt(inp[2]));
                dates.get(inp[0]).addTask(temp);
            }
        }

        MergeableHeap < Task > entireHeap = new MergeableHeap < > (); //heap for final output

        for (String date: dates.keySet()) { //go along all dates, pop element, output it and put back to heap for final output
            System.out.println("TODOList " + dates.get(date).name);
            while (!dates.get(date).heap.isEmpty()) {
                Task temp = dates.get(date).removeTask();
                System.out.println("    " + temp.name);
                entireHeap.insert(temp.priority, temp);

            }
        }

        //final output
        System.out.println("TODOList");
        while (!(entireHeap.size == 1)) { //final output
            Task temp = entireHeap.removeMax().value;
            System.out.println("    " + temp.name);
        }
        Task temp = entireHeap.removeMax().value;
        System.out.print("    " + temp.name);
    }

    interface MergeableHeapADT<G>{
        boolean isEmpty();
        int getSize();
        void merge(MergeableHeap < G > heapToMerge);
        void insert(int key, G value);
        Pair < G > max();
        HeapNode < G > removeMax();
    }

    //class of node of a binomial heap
    static class HeapNode < G > { //class for the nodes of the heap
        int key,
                degree;
        G value;
        HeapNode < G > parent;
        HeapNode < G > sibling;
        HeapNode < G > child;

        public HeapNode(int key, G value) {
            this.key = key;
            this.degree = 0;
            this.value = value;
            parent = null;
            sibling = null;
            child = null;
        }

        public void link(HeapNode < G > node) { //linker of two trees
            this.parent = node;
            this.sibling = node.child;
            node.child = this;
            node.degree++;

        }

        public HeapNode < G > findMaxNode() { //find max in heap
            HeapNode < G > x = this;
            HeapNode < G > res = this;
            int max = x.key;
            while (x != null) {
                if (x.key > max) {
                    res = x;
                    max = x.key;
                }
                x = x.sibling;
            }
            return res;
        }

        public HeapNode < G > findNode(int key) { //function for finding a node with particular key
            HeapNode < G > temp = this;
            HeapNode < G > ans = null;
            while (temp != null) {
                if (temp.key == key) {
                    ans = temp;
                    break;
                }
                if (temp.child == null) {
                    temp = temp.sibling;
                } else {
                    ans = temp.child.findNode(key);
                    if (ans == null) {
                        temp = temp.sibling;
                    } else {
                        break;
                    }
                }
            }
            return ans;
        }

        public HeapNode < G > reverse(HeapNode < G > s) { //function which reverses order of trees in a heap (needed in merging)
            HeapNode < G > ans;
            if (this.sibling != null) {
                ans = this.sibling.reverse(this);
            } else {
                ans = this;
            }
            this.sibling = s;
            return ans;
        }

        public int getSize() { //function for getting size of a heap
            return (1 + ((child == null) ? 0 : child.getSize()) + ((sibling == null) ? 0 : sibling.getSize()));
        }
    }

    static class MergeableHeap < G > implements MergeableHeapADT<G>{
        private HeapNode < G > Heap;
        public int size;

        public MergeableHeap() {
            Heap = null;
            size = 0;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int getSize() {
            return size;
        }

        //merge function from Section 2.3.1. of Cormen's book
        //it is used to get a linked list of trees in the heap. It is not a correct heap, but it is needed for merging
        private HeapNode < G > auxiliaryMerge(HeapNode < G > heapToMerge) {
            int len = 0;
            HeapNode < G > x = Heap;
            while (x != null) {
                len++;
                x = x.sibling;
            }
            x = heapToMerge;
            while (x != null) {
                len++;
                x = x.sibling;
            }

            HeapNode < G > [] ans = new HeapNode[len];
            HeapNode < G > i = Heap;
            HeapNode < G > j = heapToMerge;
            int k = 0;
            while (i != null && j != null) {
                if (i.degree <= j.degree) {
                    ans[k] = i;
                    i = i.sibling;
                } else {
                    ans[k] = j;
                    j = j.sibling;
                }
                k++;
            }
            if (i == null) {
                while (j != null) {
                    ans[k] = j;
                    j = j.sibling;
                    k++;
                }
            } else {
                while (i != null) {
                    ans[k] = i;
                    i = i.sibling;
                    k++;
                }
            }

            for (int iter = 0; iter < ans.length - 1; iter++) {
                ans[iter].sibling = ans[iter + 1];
            }

            return ans[0];
        }

        //the merge function itself from 19th chapter of second edition of Cormen's book
        //it restructures the linked list we got after auxiliaryMerge to a correct binomial heap
        public void merge(MergeableHeap < G > heapToMerge) {
            Heap = auxiliaryMerge(heapToMerge.Heap);
            size += heapToMerge.size;
            if (Heap == null) {
                return;
            }
            HeapNode < G > prev = null;
            HeapNode < G > x = Heap;
            HeapNode < G > next = Heap.sibling;
            while (next != null) {
                if (x.degree != next.degree || (next.sibling != null && next.sibling.degree == x.degree)) {
                    prev = x; //case 1 and 2
                    x = next; //case 1 and 2
                } else {
                    if (x.key >= next.key) {
                        x.sibling = next.sibling; //case 3
                        next.link(x); //case 3
                    } else {
                        if (prev == null) { //case 4
                            Heap = next; //case 4
                        } else {
                            prev.sibling = next; //case 4
                        }
                        x.link(next); //case 4
                        x = next; //case 4
                    }
                }
                next = x.sibling;
            }
        }

        //insertion function which simply creates a new heap with one node and which then merged with the rest of the heap
        public void insert(int key, G value) {
            HeapNode < G > tmp = new HeapNode < > (key, value);
            MergeableHeap < G > tempH = new MergeableHeap < > ();
            tempH.Heap = tmp;
            tempH.size = 1;
            if (Heap == null) {
                Heap = tmp;
                size = 1;
            } else {
                merge(tempH);
                size++;
            }
        }

        public Pair < G > max() {
            Pair<G> res = new Pair<>(Heap.findMaxNode().key, Heap.findMaxNode().value);
            return res;
        }

        //the function which removes the top of the heap, makes a linked list with reversed order out of its children and merges with th rest of the heap
        public HeapNode < G > removeMax() {
            if (Heap == null) {
                return null;
            }
            HeapNode < G > temp, prev, max, node;
            temp = Heap;
            prev = null;
            max = Heap.findMaxNode();

            while (temp.key != max.key) {
                prev = temp;
                temp = temp.sibling;
            }
            if (prev == null) {
                Heap = temp.sibling;
            } else {
                prev.sibling = temp.sibling;
            }
            temp = temp.child;
            node = temp;

            while (temp != null) {
                temp.parent = null;
                temp = temp.sibling;
            }
            if ((Heap == null) && (node == null)) {
                size = 0;
            } else {
                if (Heap == null) {
                    Heap = node.reverse(null);
                    size = Heap.getSize();
                } else {
                    if (node == null) {
                        size = Heap.getSize();
                    } else {
                        HeapNode < G > tmp = node.reverse(null);
                        MergeableHeap < G > tempH = new MergeableHeap < > ();
                        tempH.Heap = tmp;
                        tempH.size = tmp.getSize();
                        merge(tempH);
                        size = Heap.getSize();
                    }
                }
            }

            return max;
        }

    }

    //auxiliary class Task
    static class Task {
        String name;
        int priority;

        public Task(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }
    }

    //class Date which keeps the heap of tasks
    static class Date {
        String name;
        MergeableHeap < Task > heap = new MergeableHeap < > ();

        public Date(String name) {
            this.name = name;
        }

        public void addTask(Task t) {
            heap.insert(t.priority, t);
        }

        public Task removeTask() {
            return heap.removeMax().value;
        }
    }

    //comparator which is used in the TreeMap of Heaps
    static class ComparatorOfDates implements Comparator < String > {
        public int compare(String d1, String d2) {
            String[] temp1;
            String[] temp2;
            temp1 = d1.split("\\.");
            temp2 = d2.split("\\.");

            int[] tInt1 = new int[4];
            int[] tInt2 = new int[4];
            for (int i = 0; i < 3; i++) {
                tInt1[i] = Integer.parseInt(temp1[i]);
                tInt2[i] = Integer.parseInt(temp2[i]);
            }

            if (tInt1[2] > tInt2[2]) {
                return 1;
            } else if (tInt1[2] < tInt2[2]) {
                return -1;
            } else {
                if (tInt1[1] > tInt2[1]) {
                    return 1;
                } else if (tInt1[1] < tInt2[1]) {
                    return -1;
                } else {
                    if (tInt1[0] > tInt2[0]) {
                        return 1;
                    } else if (tInt1[0] < tInt2[0]) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }

        }
    }

    //auxiliary class pair which is used to return key-value pair in max() function
    static class Pair<G>{
        private int key;
        private G value;

        public Pair(int key, G value) {
            this.key = key;
            this.value = value;
        }

        public int getKey() {
            return key;
        }

        public G getValue() {
            return value;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public void setValue(G value) {
            this.value = value;
        }
    }
}