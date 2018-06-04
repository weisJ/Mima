# Mima
Simple environment for writing Mima-Code (see http://gbi.ira.uka.de/ chapter 10)

This mima runs on 24bit machine words in memory and 20 bits addresses / constants
(Note: mimaX programs also run 24bit addresses/constants;

# Supports:
- simple syntax highlighting
- memory address variables
- instruction address variables
- constant value references

# Instructions:

__<a\>__ denotes the value in memory at address __a__

__c__ is a constant value

__accu__ is the accumulation register

__iar__ is the instruction address register

__sp__ is the stack pointer

__rs__ is the return stack

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

- __CALL__ __c__ : rs.push(iar) and c → iar
- __RET__ : rs.pop() → iar

- __ADC__ __c__ : accu + c → accu

- __LDSP__ : sp → accu
-__STSP__ : accu → sp

- __STVR__ __disp(SP)__ : accu → <<sp> + disp>
- __LDVR__ __disp(SP)__ : <<sp> + disp> → accu

-all instructions from Mima

#Language details

- Comments: __#...__ or __#...#__
- Definitions: - __§define__ __"def"__ __:__ __"value"__ (use with e.g. STV "def")
               - __§define__ __const__ __"def"__ : __"value"__ (use with e.g. LDC "def")
               - __"def"__ __:__ __...___ (use with e.g. JMP "def")

