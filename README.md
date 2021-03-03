# Pancake Plugin <img src="https://pancake.gg/_nuxt/img/large-logo.2e8edcb.png" width=25 height=25>

A spigot plugin which adds a currency system that uses pancakes as the currency (yes, like the discord [pancake bot](https://pancake.gg/)) 

## All Commands: 

```yml
 - withdraw:
     aliases: wd
     usage: /<command> <amount of pancakes>
     
 - deposit:
     aliases: dep
     usage: /<command> <amount of pancakes or all>
     
 - balance:
     aliases: bal
     usage: /<command> <player (optional)>
     
 - newuser:
     permission: op
     usage: /<command> <player> <discord username (optional)> <initial pancakes (optional) default 500>
```

To get it working, you first need to add users to the database using the `newuser` command, 

The release also consists of a resource pack which is necessary, but the it also prompts the download link, which is hosted on my dropbox, when a player joins.

An example of how the pancakes look:

![screen shot](https://media.discordapp.net/attachments/744475414075801670/796449356970328124/2021-01-06_23.png?width=884&height=506)

P.S. you can also eat the pancakes, which gives a special effect
