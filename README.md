# Mima
Simple environment for writing Mima-Code (see http://gbi.ira.uka.de/ chapter 10)

This mima runs on 24bit machine words in memory and 20 bits addresses / constants

Supports:
- simple syntax highlighting
- memory address variables
- instruction address variables

Instructions:

__<__ __a__ __>__ denotes the value in memory at address __a__

__c__ is a constant value

__akku__ is the default register
__iar__ is the instruction pointer register

- __LDC__ __c__ : c  → akku
- __LDV__ __a__ : <a\> → akku
- __STV__ __a__ : akku → <a>
- __LDIV__ __a__ : <<a\>\> → akku
- __STIV__ __a__ : akku → <<a\>\>

- __NOT__ : invert all bits in akku
- __RAR__ : rotate bits in akku one place to the right

- __ADD__ __a__ : akku + <a\> → akku
- __AND__ : akku AND <a\> → akku (bitwise)
- __OR__ : akku OR <a\> → akku (bitwise)
- __XOR__ : akku XOR <a\> → akku (bitwise)
- __EQL__ __a__ : if <a\> = akku -1 → akku else 0 → akku

- __JMP__ __c__ : c → iar
- __JMN__ __c__ : if msb of akku = 1 then c → iar
