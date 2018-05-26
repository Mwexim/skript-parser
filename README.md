# skript-parser
###### (name subject to changes)

skript-parser is a project that aims to develop a new parser for [Skript](https://github.com/bensku/Skript). 

The project is currently separated into two branches :
  * master : a bare-bones pattern precompiler, this is the is the essence of this project, all other modules are based off of it. 
    Development on it is halted, changes may be made. I will refer to this as "parser-base"
  * java-implementation : a standalone Java implementation and extension of parser-base
    
## Motivations and Objectives

*'Skript' refers to the Spigot plugin here*

I am making this project because Skript's current parser, on top of being a giant and slow spaghetti mess, is not portable, as it depends on parts that are eventually tighly coupled with the Spigot API.

Then, I have a few goals for this parser :
  * Port Skript over to Sponge, the details of how that might happen are unspecified
  * A more personal goal of mine is to make Skript into a simple, easy to learn language, that proves useful for prototyping or explaining code to a wide audience, with its very English-friendly grammar.
  
## Technical details

*'Skript' refers to the language as a whole here*

Skript is a scripting language (thus, it is interpreted) made so that code written in it resembles plain English sentences as much as possible. The language is indentation-sensitive, and pretty much structures like Python, e.g :
```python
section:
  line of code # comments are also marked with a '#', like in Python
  another section:
    code inside the section
  code outside of the section
```

Even though the Java implementation is not advanced enough in order to prove it *yet*, and no proofs have been written using the Skript plugin *yet*, Skript is most certainly Turing-complete.

### Skript patterns explanation

Nearly every element of the language is defined by a syntax, of which I will detail the meaning here :
 
 `(first|second|third)` defines a choice : the user can input any of the words inside the brackets, separated by pipes
It is possible to retrieve information over what the choice is, through *parse marks*. They consist of an integer and a broken pipe symbol (`¦`), written after the opening bracket, or a pipe `|`. If not specified, the default parse mark is 0. Example : `(1¦one|2¦two)`. All successive parse marks are XORed together, e.g the syntax `(one|1¦1) (2¦two|2)` matched against `1 two`, will output a parse mark of `1 XOR 2 == 3`.

`[optional]` makes a part of the syntax optional. New to skript-parser is the ability to write `[1¦one]` or `[1¦one|the number one]`. Note that these are just syntax sugar for `[(1¦one)]` and `[(1¦one|the number one)]` respectively.

`%type%` specifies that this part of the syntax accepts a value of the type `type`. This value can be anything by default, but a constraint, called "acceptance" can be specified to limit this :
  * `%*type%` will only accept literals of the type `type`
  * `%~type%` is the opposite of the above syntax, it won't accept literals.
  * `%^type%` only accepts *variables* of type `type`
  * `%-type%` will make it so the expression will be null when opted out (e.g the %% part is inside an optional group), rather than defaulting to the default expression, specified when defining `type`.
  
Now, boolean expressions have a special treatment. Some boolean expressions, called "conditional", can only be used :
  * In a condition
  * Alternatively, boolean types can be marked with `=` in syntax (i.e `%=boolean%`) to say that they accept conditional expressions. The easiest way to use a conditional expression as a regular boolean one is to use the "whether" expression : `whether %~=boolean%`. Note that the while loop uses `%=boolean%`
