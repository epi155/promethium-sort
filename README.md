# promethium-sort

Utility for sorting large positional files, without having to load them entirely into memory.
The options are similar to the IBM DFSORT.


The simplest way to invoke sort is

~~~java
public class Main {
    public static void main(String[] args) {
        File sourceFile = new File(args[0]);    // unsorted file
        File targetFile = new File(args[1]);    // sorted file
        
        SortEngine.using(256)       // Max Records in Memory
            .sortIn(sourceFile)
            .sort()                 // Comparator<String> can be added
            .sortOut(targetFile);
    }
}
~~~


The utility uses a DSL style, the available options are:
**sortIn**, *skipRecord*, *include*, *stopAfter*, *inRec*, **sort**, *allDups*, *first*, *firstDup*, *last*, *lastDup* , *noDups*, *sum*, _reduce_, *outRec*, **sortOut**.

Options allow you to manipulate records before and after sorting.

The file is split into small, sorted files.
Small files are taken in pairs and balanced, resulting in a sorted file with the same records as the pair.
This operation is repeated until a single ordered file is obtained.
The generated temporary files are deleted at each cycle.
Free disk space of at least twice the size of the original file is required.


#### Option detail

**`sortIn`**
: Set the file to be sorted.

`skipRecord`
: Sets the number of records to skip from the beginning of the file

`include`
: Set the condition to include (or discard) records

`stopAfter`
:  Can be used to specify the maximum number of records you want the subtask for the input file to accept for sorting (accepted means read from the input file and not deleted by INCLUDE

`inRec`
:  Edit the input record before sorting

**`sort`**
:  Sort the records using the given comparator or natural order

`allDups`
:  Limits the records selected to those with KEY-SORT values that occur more than once.

`first`
:  Limits the records selected to those with KEY-SORT values that occur only once and the first record of those with KEY-SORT values that occur more than once.

`firstDup`
: Limits the records selected to the first record of those with KEY-SORT values that occur more than once.

`last`
: Limits the records selected to those with KEY-SORT values that occur only once and the last record of those with KEY-SORT values that occur more than once.

`lastDup`
: Limits the records selected to the last record of those with KEY-SORT values that occur more than once.

`noDups`
: Limits the records selected to those with KEY-SORT values that occur only once.

`sum`
: This control statement maps each group of records with equal KEY-SORT in their summary

`outRec`
: Edit the output record after sorting

**`sortOut`** 
: Set the sorted file
