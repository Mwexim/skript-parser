# skript-parser
## Presentation

### What is Skript ?
Skript is a Spigot plugin for Minecraft servers. It enables its users to write very simple code in plaintext files in order to make "mini-plugins". The syntax is meant to be very lenient and close to English. The following code is valid Skript :
```vba
on right click on dirt:
    if the player is holding seeds:
        set the clicked block to grass
    else if the player is holding any mushroom:
        set the clicked block to mycelium
```
Very close to English, isn't it ?
### What's the problem with Skript ?
It's quite a mess. The code is filled with bad programming practices, spaghetti code and confusing architectures. But most importantly, this code is tightly-coupled with the Spigot API ; the core parsing and executing mechanisms need Spigot in order to work. Combine that with the previous flaws, and you have code that is practically impossible to use with another API.
### What's the point of skript-parser then ?
Fix this. Skript-parser provides a better, easier to understand and more flexible implementation of Skript. A standalone, command line  implementation of skript-parser is being worked on, as a proof of concept.
## Contributing 
If you want to start contributing, simply clone the Git repository and start working from here. I recommend using IntelliJ IDEA, because it's what I use, and some annotations are only understood by it.
Pull requests are welcome, although please make a separate branch for them.

## I have other questions, where do I ask them ?
You can contact me on Discord, where I'm Syst3ms#9959. 
I rarely check Github, so if you want to contact me here, don't expect a swift response from me.
