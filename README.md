<img src="https://raw.githubusercontent.com/weisJ/Mima/master/mima-app/src/main/resources/images/mima.png" width="100" height="100" align="right">

# Mima IDE
Simple environment for writing Mima-Code (see <http://gbi.ira.uka.de/> chapter 10)
<img src="https://raw.githubusercontent.com/weisJ/Mima/master/images/Mima_Preview.png" align="center">

This is a simple IDE developed for writing code targeted at the Mima (Minimal Computing Machine) taught at KIT (Karlsruher Intitute for Technology).

# How to use
You can run either the jar or executable from .\build or the release page.
No further installation is needed.
You can also compile it yourself using maven.

# What is a Mima
The Mima or minimal Machine is an idealized Prozessor taught at the KIT. It uses, as the name implies, a minimal set of prozessor instruction to achieve full computational power.

--Diagramm--

The Mima uses registers that are 24bit long and store numbers in twos complement. To encode the instructions the first four bytes of the register are reserved and the remaining 20 bits are used for the argument. This implies that arguments can only be positive numbers as negative numbers need to be stored in 24 bits.

An extended version of the Mima called MimaX adds some further capabilities to support callstacks and 24bit values in adresses and argument constants.

## Instructions of the Mima(X)

__<a\>__ denotes the value in memory at address __a__

__c__ is a constant value

__accu__ is the accumulation register

__iar__ is the instruction address register

__sp__ is the stack pointer

__rs__ is the return stack

__Mima Instructions:__

| Instruction    | Description                                   |
|----------------|----------------------------------------------:|  
| __LDC__ __c__  | c  ⟶  accu                                   |
| __LDV__ __a__  | <a\> ⟶ accu                                  |
| __STV__ __a__  | accu ⟶ <a\>                                  |
| __LDIV__ __a__ | <<a\>\> ⟶ accu                               |
| __STIV__ __a__ | accu ⟶ <<a\>\>                               |
| __NOT__        | invert all bits in accu                       |
| __RAR__        | rotate bits in accu one place to the right    |
| __ADD__ __a__  | accu + <a\> ⟶ accu                           |
| __AND__        | accu AND <a\> ⟶ accu (bitwise)               |
| __OR__         | accu OR <a\> ⟶ accu (bitwise)                |
| __XOR__        | accu XOR <a\> ⟶ accu (bitwise)               |
| __EQL__ __a__  | if <a\> = accu then -1 ⟶ accu else 0 ⟶ accu |
| __JMP__ __c__  | c ⟶ iar                                      |
| __JMN__ __c__  | if msb of accu = 1 then c ⟶ iar              |

__Additional MimaX Instructions:__

| Instruction          | Description                             |
|----------------------|----------------------------------------:| 
| __CALL__ __c__       | rs.push(iar) and c ⟶ iar               |
| __RET__              | rs.pop() ⟶ iar                         |
| __ADC__ __c__        | accu + c ⟶ accu                        | 
| __LDSP__             | sp ⟶ accu                              |
| __STSP__             | accu ⟶ sp                              |
| __STVR__ __disp,SP__ | accu ⟶ <<sp> + disp>                   |
| __LDVR__ __disp,SP__ | <<sp\> + disp\> ⟶ accu                 |


# Language implementation details

The IDE utilizes an internal Mima language with makes it easier to produce working code.

- Comments: ```#...``` or ```#...#```.
- Definitions:
    - ```§define "def" = "value"``` (use with e.g. STV "def").
    - ```§define const "ref" = "value"``` (use with e.g. LDC("ref")).
    - ```"ref" : ...``` (use with e.g. JMP "ref").
- Statements must be terminated by ```;```.
- Arguments are passed within parenthesis (e.g. ```STV(5)```, ```LDVR(1,SP()```)
- ```SP``` is a function call, so it has to be used by typing ```SP()``` and **not** ```SP```.
- You can use scopes ```{ ... }``` for variable shadowing.
  Variables are lost outside the scope except they explicitly define a memory cell.
  Constants are lost indefinitely.
- Variables must be defined before they can be used. Jumps are the only exception for this.
- You can **not** jump into an inner scope. Jumps can only occur within the same or an outer scope.

