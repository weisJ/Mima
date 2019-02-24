# Mima
Simple environment for writing Mima-Code (see http://gbi.ira.uka.de/ chapter 10)

<p align="center"> 
<img src="https://user-images.githubusercontent.com/31143295/40985489-8630f3be-68e4-11e8-8eae-c1adc6bf9ade.png">
</p>

This mima runs on 24bit machine words in memory and 20 bits addresses / constants

__Note:__ mimaX programs also run 24bit addresses/constants

# Supports:
- simple syntax highlighting
- memory address variables
- instruction address variables
- constant value references

# How to use
You can run either the jar or executable from .\build. No further installation is needed.

# Instructions:

__<a\>__ denotes the value in memory at address __a__

__c__ is a constant value

__accu__ is the accumulation register

__iar__ is the instruction address register

__sp__ is the stack pointer

__rs__ is the return stack

__Mima:__

- __LDC__ __c__ : c  ⟶  accu
- __LDV__ __a__ : <a\> ⟶ accu
- __STV__ __a__ : accu ⟶ <a\>
- __LDIV__ __a__ : <<a\>\> ⟶ accu
- __STIV__ __a__ : accu ⟶ <<a\>\>

- __NOT__ : invert all bits in accu
- __RAR__ : rotate bits in accu one place to the right

- __ADD__ __a__ : accu + <a\> ⟶ accu
- __AND__ : accu AND <a\> ⟶ accu (bitwise)
- __OR__ : accu OR <a\> ⟶ accu (bitwise)
- __XOR__ : accu XOR <a\> ⟶ accu (bitwise)
- __EQL__ __a__ : if <a\> = accu then -1 ⟶ accu else 0 ⟶ accu

- __JMP__ __c__ : c ⟶ iar
- __JMN__ __c__ : if msb of accu = 1 then c ⟶ iar

__MimaX:__

- __CALL__ __c__ : rs.push(iar) and c ⟶ iar
- __RET__ : rs.pop() ⟶ iar

- __ADC__ __c__ : accu + c ⟶ accu

- __LDSP__ : sp ⟶ accu
- __STSP__ : accu ⟶ sp

- __STVR__ __disp,SP__ : accu ⟶ <<sp> + disp>
- __LDVR__ __disp,SP__ : <<sp\> + disp\> ⟶ accu

- all instructions from Mima

# Language details

- Comments: __#...__ or __#...#__
- Definitions:
    - __§define__ __"def"__ __=__ __"value"__ (use with e.g. STV "def")
    - __§define__ __const__ __"ref"__ __=__ __"value"__ (use with e.g. LDC("ref"))
    - __"ref"__ __:__ __...__ (use with e.g. JMP "ref")
- Statements must be terminated with __;__
- Arguments are passed within parenthesis (e.g. STV(5), LDVR(1,SP())
- __SP__ is a function call, so it has to be used by typing __"SP()"__ and **not** __"SP"__
- You can use scopes "__{ ... }__" for variable shadowing.
  Variables are lost after the scope except they explicitly define a memory cell.
  Constants are lost indefinitely
- Variables must be defined before they can be used. Jumps are the only exception for this.
- You can **not** jump into an inner scope. Jumps can only occur within the same or an outer scope.

