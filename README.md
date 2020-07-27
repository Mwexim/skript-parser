# skript-parser

### What is Skript?
Skript is a Spigot plugin for Minecraft servers. Created by Njol back in 2011 and now powered by the community. The purpose of Skript is to enable anyone to modify their Minecraft server and create unique innovations for their server. Plugin development often involves a lot of time and skill or paying someone to do it. Skript uses plaintext files, English like syntax and has easy in-game commands to manage scripts. There's no compiling and other messy Java stuff. 

Valid Skript code looks like this:
```vba
on right click on dirt:
    if the player is holding seeds:
        set the clicked block to grass
    else if the player is holding any mushroom:
        set the clicked block to mycelium
```
It's simple and easy to understand. Learning Skript is a fun process that isn't time-consuming. There aren't any complicated methods, standards or weird syntax. Everything is in a sentence like structure.

### What's the problem with Skript?
It's quite a mess. Skript was created by Njol as a small hobby project that grew massively. Fulfilling a need that no one knew existed. The code that runs Skript is filled with bad programming practices, spaghetti code and confusing architectures. As Skript grew and the more features were added, the worse it got. The code got more tangled and less understandable. One of the worst parts of Skript is that it is tightly-coupled with the Spigot API. Skript can only be used on a Minecraft server running Spigot. This makes Skript essentially useless outside of a Spigot Minecraft server. 
### What's the point of skript-parser then?
To fix the flaws of Skript. Skript-parser provides a better, easier to understand and more flexible implementation of Skript. A standalone, command-line implementation of skript-parser is being worked on, as a proof of concept. This would allow Skript to progress onto other platforms and also improve the Skript plugin itself.
## Repository structure
This repository currently contains two main branches :
 * **master** : this branch contains a proof-of-concept implementation of the parser, that compiles to an executable JAR file.
 * **skript-library** : this branch contains no implementations, and its purpose is to be used as a base library for any skript-parser-related projects.

## Contributing 
If you want to start contributing, simply clone the Git repository and start working from here. I recommend using IntelliJ IDEA, because it's what I use, and some annotations are only understood by it.
Pull requests are welcome, although please make a separate branch for them.

## I have other questions, where do I ask them?
You can contact me on Discord, where I'm Syst3ms#9959. 
I rarely check Github so contacting me here isn't the best! If you do, don't expect a quick response.
