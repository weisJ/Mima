# Mima
Simple environment for writing Mima-Code (see http://gbi.ira.uka.de/ chapter 10)
Supports simple syntax highlighting.

#Instructions:


<a> denotes the value at memory address a
c is a constant value
_________________________________________________________
LDC   c |   c → akku
LDV   a |   <a> → akku
STV   a |   akku → <a>
LDIV  a |   <<a>> → akku
STIV  a |   akku → <<a>>
________|_______________________________________________
RAR     |   rotate akku one place to the right
NOT     |   bitwise invert akku
________|_______________________________________________
ADD   a |   akku + <a> → akku
AND   a |   akku AND <a> (bitwise) → akku
OR    a |   akku OR <a> (bitwise) → akku
XOR   a |   akku XOR <a> (bitwise) → akku
EQL   a |   if akku = <a>  -1 → akku, else 0 → akku
        |
HALT    |   stop the program
JMP   a |   move instruction pointer to a
JMN   a |   JMP a if most significant bit in akku is 1
________|_______________________________________________

#Language details:

comments:                   #"comment" (only full line comments possible)
binary values:              0b...
memory address references:  $define "reference" : "address value"
instruction reference:      "reference" : "insctruction"
                            JMP "reference"
