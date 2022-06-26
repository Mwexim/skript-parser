# skript-parser

### What is Skript?

Skript is a Spigot plugin for Minecraft servers, created by Njol back in 2011. The aim of Skript has been to act as an easy hands-on solution for Minecraft server modification. Plugin development often requires a lot of time to learn and become proficient with Java, that is if you don't just pay someone else to do it for you. Skript code is interpreted from plaintext side, and can be loaded and reloaded on the fly in-game, without any need to compile and build. 

Valid Skript code looks like this:
```vba
on right click on dirt:
    if the player is holding seeds:
        set the clicked block to grass
    else if the player is holding any mushroom:
        set the clicked block to mycelium
```
Even if you have never written a single line of code in your entire life, what this small script does should be very clear to you. Such is the Skript language's purpose. This is especially valuable in a Minecraft environment because Java is a notoriously verbose and bulky language to program in. However, the Skript *plugin* has some glaring flaws.

### The problem with the Skript plugin

To put it bluntly, it's quite the mess. Skript was created by Njol as a small hobby project that grew massively thereafter. Unfortunately as more and more features were tacked on the existing ones, some general design issues persisted and even worsened. As it is, Skript is riddled with some bad programming design, such as, but not limited to, spaghetti code, inelegant control flow, a wasteful parsing process, and so on and so forth. It is also tightly-coupled with the Spigot API, meaning that not only can Skript only be used on a Minecraft server running Spigot, but it's very hard (and not really worth it, anyway) to remove all the Spigot-related elements from the codebase. This makes Skript's code essentially useless outside of a Spigot Minecraft server.

### The aim of this project

The main aim is to provide a better, easier to understand and more flexible implementation of the Skript language. This implies, to a yet indeterminate extent, an overhaul of the API present in the Skript plugin. A standalone CLI implementation can be found at [skript-cli](https://github.com/SkriptLang/skript-cli). It works not only as a simple, bare-bones implementation of the language, but also as a proof-of-concept that demonstrates how this project's code can be repurposed for many different environments, not just Minecraft.

## Repository structure

This repository currently contains two main branches :

 * **master** : this branch contains a proof-of-concept implementation of the parser, that compiles to an executable JAR file.
 * **skript-library** : this branch contains no implementations, and its purpose is to be used as a base library for any skript-parser-related projects.

## Contributing 

External contributions are extremely welcome, as I (Syst3ms, owner of the repository) and other contributors don't have enough time to contribute to the repository regularly. Adding features is obviously amazing, but smaller things such as cleaning up the code and adding documentation can go a long way for the project. If you want to start contributing, simply clone the Git repository and start working from here. I recommend using IntelliJ IDEA, it's my IDE of choice, and some annotations are only properly understood and handled by it.
