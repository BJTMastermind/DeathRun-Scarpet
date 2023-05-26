# Death Run Scarpet

Allows you to easily setup a deathrun map using just a few custom commands, and Carpet Mod!

## NOTE
This script currently only supports/works in Minecraft 1.19.3. if you attempt to use this script in 1.19.4+ versions expect it to be unplayable and vary broken. Older versions below 1.19.3 have not been tested. 

## Prerequisites
[Fabric](https://fabricmc.net/use/installer/) (1.19.3)<br>
[Carpet Mod](https://www.curseforge.com/minecraft/mc-mods/carpet/files?version=1.19.3) (1.19.3)

## How To Install
1. Download or clone the repository and drag and drop all of the .sc files into your worlds `scripts` folder from the `src` folder. If the folder doesn't exist, create it.
2. (Singleplayer) Copy/Cut and paste `resources.zip` into the root of your world folder from the same `src` folder from the download. (Technically optional, Makes empty white bossbar invisible for stats at the top of the screen)
3. (Multiplayer) Paste the link to the resources.zip file into `resource-pack` in server.properties, and set `require-resource-pack` to **true**
3. Open the world, If the world is already opened run `/reload`.
4. Run the following commands:<br>
`/carpet setDefault commandScript ops`<br>
`/script load deathrun_main` (This will automaticly load all the other scripts as well).
5. Done!

## How To Use
1. Open chat and type `/dr wand` to give your self the Death Run wand.
2. Use the wand to select 2 corners (Similar to WorldEdit).
3. Open chat and type `/dr add selection` to add the selection. You should now see a red box rendering showing your selected area(s). (Currently Traps only support a single area + the death area selection)<br>
4. There are 2 wands for setting up a deathrun map to be playable, the `Main Selection Wand` and the `Deaths Trap Trigger Area Selection Wand` (Names are W.I.P). Details of how to setup a map to be playable coming soon<br>

More Info Coming Soon...

## Planned / To Do
1. Rewrite glass floor trap to support sloped glass floors. (Currently only supports a flat floor)
2. Major Rewrite to clean up and optimize the code.
3. Add a Death Bot for solo play that will automaticly activate traps. (Will try to not make the bot over powered at that)
