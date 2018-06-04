# Mima
Simple environment for writing Mima-Code (see http://gbi.ira.uka.de/ chapter 10)

This mima runs on 24bit machine words in memory and 20 bits addresses / constants

# Supports:
- simple syntax highlighting
- memory address variables
- instruction address variables

# Instructions:

__<__ __a__ __>__ denotes the value in memory at address __a__

__c__ is a constant value

__accu__ is the default register

__iar__ is the instruction pointer register

__sp__ is the stack pointer

Mima:

- __LDC__ __c__ : c  → accu
- __LDV__ __a__ : <a\> → accu
- __STV__ __a__ : accu → <a\>
- __LDIV__ __a__ : <<a\>\> → accu
- __STIV__ __a__ : accu → <<a\>\>

- __NOT__ : invert all bits in accu
- __RAR__ : rotate bits in accu one place to the right

- __ADD__ __a__ : accu + <a\> → accu
- __AND__ : accu AND <a\> → accu (bitwise)
- __OR__ : accu OR <a\> → accu (bitwise)
- __XOR__ : accu XOR <a\> → accu (bitwise)
- __EQL__ __a__ : if <a\> = accu then -1 → accu else 0 → accu

- __JMP__ __c__ : c → iar
- __JMN__ __c__ : if msb of accu = 1 then c → iar

MimaX:

- Todo
