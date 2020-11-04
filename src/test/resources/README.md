# Skript-parser Testing System
All files that are part of the internal testing structure of skript-parser can be found in here.

## Contributing to Tests
We're not very strict about what can be added to our test suite.
Tests don't have to be good code - often, testing edge cases pretty
much requires weird code. That being said, there are a couple of things
to keep in mind:

* Use tabs for indentation
* Always test each possible case of your syntax.
* Write descriptive assert messages and write comments
* Ensure your tests pass for the latest stable version and the latest beta version

Furthermore, we want to keep consistency across all tests:
* Syntax tests should be put in their respective folder 
(expressions go into the `expressions/` folder, and so forth).
    * Their name should be equal to their Java class. `ExprLength.java`'s test for example,
    will be saved at the following path: `expressions/ExprLength.txt`.
    * When you make changes to existing syntax, their tests need to be modified as well
    to handle that new syntax (make sure to demarcate this part with the correct version
    the change was made in).
* Tests about literals obviously go into the `literals/` folder. These can be tests about 
types, lists, literal strings and many other things. However, expressions like `LitMathConstants`
should still be preserved for the `expressions/` folder.
* All other tests go into `general/`.
* If you don't want your test to be parsed like a normal script, you can put it inside the `misc/`
folder. All tests there will not even be touched unless someone specifically calls them in another 
test class.

It is obvious that workarounds to existing bugs may not be used in tests. Tests that involve
asynchronous code (like `EffAsync`, `EffWait` or `EffShutdown`) are not allowed as they break
the test environment and will yield uncertain results.

Contributors are free to modify any test at any time to improve their quality and coverage
of the actual syntax.

## Syntax inside Tests
Test scripts have all normal Skript syntaxes available, except for the events. In addition to that,
some syntaxes for test development are available.

* Test cases are events: <code>test [when \<condition\>]</code>
  * Contents of tests will not be parsed when the conditions aren't met
* Assertions are available as effects: <code>assert \<condition\> [with "failure message"]</code>
* Reach syntax is available to check if a given code block reaches a certain statement. This
is particularly useful for effects like `EffEscape`, because sometimes the code might be escaped
too much, resulting in the test not running entirely. We refer to `misc/dummy.sk` for its documentation.
* Take a look at existing tests for examples; in particular,
  <code>misc/dummy.sk</code> is useful for beginners

## Test Development
When executing the `gradle build` task, the tests normally are run by themselves. To exclusively
run tests, run the `gradle test` task.