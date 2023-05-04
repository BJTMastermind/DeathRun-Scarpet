# Death Run Scarpet

Allows you to easily setup a deathrun map using just a few custom commands, and Carpet Mod!

## Prerequisites
[Fabric](https://fabricmc.net/use/installer/) (1.19.3)<br>
[Carpet Mod](https://www.curseforge.com/minecraft/mc-mods/carpet/files?version=1.19.3) (1.19.3)

## How To Install
1. Download or clone the repository and drag and drop all of the .sc files into your worlds `scripts` folder. If the folder doesn't exist, create it.
2. Open the world, If the world is already opened run `/reload`.
3. Run the following commands:<br>
`/script load deathrun_main`<br>
`/script in deathrun_main run setup()`

## How To Use
1. Open chat and type `/dr wand` to give your self the Death Run wand.
2. Use the wand to select 2 corners (Similar to WorldEdit).
3. Open chat and type `/dr add selection` to add the selection. You should now see a red box rendering showing your selected area(s).

More Info Coming Soon...

## Planned / To Do
1. Rewrite glass floor trap to support sloped glass floors. (Currently only supports a flat floor)
2. Clean up / Optimize code.
3. Add a Death Bot for solo play that will automaticly activate traps. (Will try to not make the bot over powered at that)
