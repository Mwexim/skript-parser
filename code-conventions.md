# Code Conventions
This document outlines code styling conventions to follow when contributing to this project.

## Security

Obviously, all of the following are strictly forbidden :
 - Malicious code.
 - Code exposing secrets/passwords.

Third party code may be allowed if and only if its licensing matches that of this project (currently MIT).

## Formatting :
IntelliJ IDEA is the recommended IDE to use for this project.
 * Every statement (separated with a semicolon) belongs on a separate line.
 * Lines should stay under 120 characters long. This rule is strict when it comes to comments/javadoc, but otherwise it is acceptable to have lines about 10 characters longer.
 * The brace style used is the K&R variant sometimes called 1TBS/OTBS (One True Brace Style) :
	 * No line break before the opening brace, only a single space.
	 * Lines inside the block are indented once (1 tab or 4 spaces)
	 * The closing brace is vertically aligned with the start of the statement that opened the code block (i.e the `if` statement, the `while` loop, etc)
	 * There may be no code following a closing brace, with the exception of `else if`/`else` after an `if`/`else if` statement, `catch`/`finally` after a `try` block and `while (condition)` at the end of a `do ... while` loop, in which case it is separated by a space.

Example : 
```java
public static void main(String[] args) {
	while (someCondition) {
		var someInt = 2;
		var result = computation(someInt);
		if (matchesCondition(result)) {
			success();
		} else {
			failure();
		}
	}
}
```

 * Braces may only be omitted with `if`, `for`, `while` containing a single statement if it doesn't hurt the readability of the code. In the case of `if`, there must be no attached `else if`/`else` blocks, in which case all blocks must have braces. When braces are removed, the statement inside must still be indented and on its own line.

Example :
```java
if (simpleCondition) {
	simpleStatement();
}
```
may become
```java
if (simpleCondition)
	simpleStatement();
```

## Naming conventions

* Classes use PascalCase
* Constant fields (enums included) use SCREAMING_SNAKE_CASE
* Methods, fields and local variables use camelCase
* Packages are all lowercase with no spaces or delimiters.
* Classes extending `ConditionalExpression` should start with the `CondExpr` prefix
* Classes extending `Expression` that behave as literals (i.e that don't take any expression as input, and whose values do not depend on the placement in the code) should start with the `Lit` prefix
* Other classes extending `Expression` should start with the `Expr` prefix.
* Classes extending `Effect` should start with the `Eff` prefix.
* Classes extending `CodeSection` should start with the `Sec` prefix.
* Classes extending `SkriptEvent` should start with the `Evt` prefix.
* Classes extending `TriggerContext` should *end* with the `Context` *suffix*.

## Other language features

* This project uses Java 11, all features introduced thereafter cannot be used.
* The `var` keyword should be used as often as possible when it doesn't hamper readability.
* Fields may be marked `final` if and only if they are effectively final. Parameters should never be, and local variables should only be marked `final` when required by a lambda. 
* Methods should only be marked `final` to disallow overriding them, and for this `static` methods should absolutely never be marked `final`.
* Classes shouldn't be marked `final`.
* The `@Override` annotation should be added when applicable.

## Project-specific conventions

* When implementing any `SyntaxElement`, `init` must come first and `toString` should come last.

## Nullity

* Every parameter, field, and return value from the project is presumed to be not null unless explicitly marked so as such using the `org.jetbrains.annotations.Nullable` annotation.
* When creating a new package, create a `package-info.java` file, and add the `javax.annotation.ParametersAreNonnullByDefault`. 
* If you are absolutely certain that a variable is non-null despite it being marked as nullable, use an assertion.

Important note : in the future, the project is planned to instead use the `java.util.Optional` API instead.


