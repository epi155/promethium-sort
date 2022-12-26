# promethium-sort

Utility for sorting large positional files, without having to load them entirely into memory.
The options are similar to the IBM DFSORT.


The simplest way to invoke sort is

~~~java
import java.io.File;

class Main {
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
