You are tasked with writing a Java program that will calculate the sum
of values stored in a user-specified binary file. The sum must be limited
to 64-bit precision.

------[ Input Specification

The program will be provided only one argument, the path to the binary file
containing the list of integers. The binary file consists of an array of
little-endian 32-bit unsigned integers. The examples directory contains a
file "simple.txt" that contains five integers: 1, 2, 3, 4 and 5. Malformed
input is not possible and does not need to be handled.

------[ Output Specification

The output of the program must be a single line consisting of a human-readable
base-ten form of the sum of values provided in the input. The sum may only
support 64-bit bits of precision.

    Example usage for "simple.txt":
    ,-------------------------------------,
    | shell$ ./sum ../examples/simple.txt |
    | 15                                  |
    |                                     |
    '-------------------------------------'

------[ Additional Constraints

The program must be able to handle input files consisting of more than one billion
integers. Please make an effort to make your sum program perform well for larger
inputs.

###Solution
The approach is for SSD where parallel reads can have a place.  
After some measurements found out that work of arithmetic sum operations two times faster than reading data from disk for the same batch of bytes. This relation can be expressed like  `1 : 2` or `0.33 : 0.67` based on 1. 
During startup program checks available number of cores and distribute work among them according percentage above. 
Pool of Readers reads data by chunks from different parts of file in parallel manner and place chunks in a queue. 
Pool of Adders sums numbers from chunks taken from the queue.
Plain `int` is enough to store unsigned integer from input.
Resulting sum of numbers can be stored in `long` because 64-bit unsigned integer also fits in plain `long`.
Actual result is interpreted as 64-bit unsigned integer.
Also added check for overflow of result.  
For HDD with sequential access will be enough to have one worker for reading and one worker for calculations because calculations faster that reading and only one reading thread can be served by OS.

To package in distributive zip with executable script
```
shell$ gradlew distZip
```