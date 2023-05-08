global_pos1 = null;
global_pos2 = null;
global_death_pos1 = null;
global_death_pos2 = null;
global_render_locations = [];
global_render_death_locations = [];
global_locations = [];
global_death_locations = [];
global_traps = ['REMOVE_BLOCKS', 'FIRE', 'GIANT', 'FLOOD', 'POSION', 'TNT', 'WALL', 'FANGS', 'FIRE_SNAKE', 'GLASS_FLOOR', 'ARROWS'];

// Trap format: [{'activate_args' -> ['glass_floor', 'east', [pos1, pos2]], deathsarea -> {'area' -> [], 'rotation' -> []}}]
global_map_output = {'map_name' -> '', 'starting_glass' -> [], 'spawn_locations' -> {'runners' -> {'locations' -> [], 'rotation' -> []}, 'deaths' -> {'locations' -> [], 'rotation' -> []}}, 'checkpoints' -> [], 'traps' -> []};

// Commands

__config() -> {
    'commands' -> {
        '' -> 'help',
        'add checkpoint <respawnDirection>' -> ['add_checkpoint', null],
        'add checkpoint <respawnDirection> <name>' -> 'add_checkpoint',
        'add spawn <playerType> <rotation>' -> 'add_spawn',
        'add selection' -> 'add_selection',
        'define trap REMOVE_BLOCKS <deathsRotation> <blocks>' -> ['define_trap_remove_blocks', 'air', null],
        'define trap REMOVE_BLOCKS <deathsRotation> <blocks> <replaceWith>' -> ['define_trap_remove_blocks', null],
        'define trap REMOVE_BLOCKS <deathsRotation> <blocks> <replaceWith> <extraSound>' -> 'define_trap_remove_blocks',
        'define trap FIRE <deathsRotation> <fireType>' -> 'define_trap_fire',
        'define trap GIANT <deathsRotation>' -> 'define_trap_giant',
        'define trap FLOOD <deathsRotation> <fluid>' -> 'define_trap_fire',
        'define trap POSION <deathsRotation>' -> 'define_trap_posion',
        'define trap TNT <deathsRotation>' -> 'define_trap_tnt',
        'define trap WALL <deathsRotation> <block>' -> 'define_trap_wall',
        'define trap FANGS <deathsRotation>' -> 'define_trap_fangs',
        'define trap FIRE_SNAKE <deathsRotation> <direction>' -> 'define_trap_fire_snake',
        'define trap GLASS_FLOOR <deathsRotation> <direction>' -> 'define_trap_glass_floor',
        'define trap ARROWS <deathsRotation>' -> 'define_trap_arrows',
        'save' -> 'save_map',
        'load <filename>' -> 'load_map',
        'set mapname <name>' -> 'set_name',
        'set startglass' -> 'set_start_glass',
        'wand' -> 'give_wand',
        'wand deselect' -> 'deselect',
        'help' -> 'help',
        'start' -> _() -> (run('script in deathrun_main run start();')),
        'end' -> _() -> (run('script in deathrun_main run end();'))
    },
    'arguments' -> {
        'playerType' -> { 'type' -> 'string', 'options' -> ['death', 'runner'] },
        'type' -> { 'type' -> 'string', 'options' -> ['FIRE', 'GIANT', 'LAVA', 'POSION', 'TNT', 'WATER', 'FANGS'] },
        'name' -> { 'type' -> 'text', 'suggest' -> [] },
        'block' -> { 'type' -> 'block' },
        'fireType' -> { 'type' -> 'term', 'options' -> [block('fire'), block('soul_fire')] },
        'fluid' -> { 'type' -> 'term', 'options' -> [block('lava'), block('water')]},
        'blocks' -> { 'type' -> 'blockpredicate' },
        'replaceWith' -> { 'type' -> 'block' },
        'extraSound' -> { 'type' -> 'sound' },
        'direction' -> { 'type' -> 'string', 'options' -> ['north','south','east','west'] },
        'respawnDirection' -> { 'type' -> 'string', 'options' -> ['north','south','east','west'] },
        'rotation' -> { 'type' -> 'rotation', 'suggest' -> ['0.0 0.0','90.0 0.0','-180.0 0.0','-90.0 0.0'] },
        'deathsRotation' -> { 'type' -> 'rotation', 'suggest' -> ['0.0 0.0','90.0 0.0','-180.0 0.0','-90.0 0.0']},
        'filename' -> { 'type' -> 'string', 'options' -> list_files('./', 'shared_json') }
    },
    'command_permission' -> 'ops';
};

// Selection & Rendering Selections

__on_tick() -> (
    render_selection();
);

__on_player_breaks_block(player, block) -> (
    item_tuple = query(player, 'holds', 'mainhand');
    if (get(item_tuple, 0) == 'golden_axe' && get(item_tuple, 2) == '{Damage:0,display:{Lore:[\'{"text":"Main wand used for selecting the area of:","italic":"false","color":"gray"}\',\'{"text":"Traps","italic":"false","color":"gray"}\',\'{"text":"Checkpoints","italic":"false","color":"gray"}\',\'{"text":"Starting locations","italic":"false","color":"gray"}\',\'{"text":"Starting Glass","italic":"false","color":"gray"}\'],Name:\'{"text":"Deathrun Main Selection Wand","italic":"false","color":"yellow"}\'}}',
        if (pos(block) != global_pos1,
            global_pos1 = pos(block);
            print(format('y 1st position set to ('+global_pos1:0+', '+global_pos1:1+', '+global_pos1:2+').'));
        );
        return('cancel');
    , if (get(item_tuple, 0) == 'diamond_axe' && get(item_tuple, 2) == '{Damage:0,display:{Lore:[\'{"text":"Used for selecting the area of:","italic":"false","color":"gray"}\',\'{"text":"Deaths Trap Activation Area","italic":"false","color":"gray"}\'],Name:\'{"text":"Deathrun Trap Trigger Area Selection Wand","italic":"false","color":"green"}\'}}',
        if (pos(block) != global_death_pos1,
            global_death_pos1 = pos(block);
            print(format('y 1st death area position set to ('+global_death_pos1:0+', '+global_death_pos1:1+', '+global_death_pos1:2+').'));
        );
        return('cancel');
    ));
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if (hand == 'mainhand' && get(item_tuple, 0) == 'golden_axe' && get(item_tuple, 2) == '{Damage:0,display:{Lore:[\'{"text":"Main wand used for selecting the area of:","italic":"false","color":"gray"}\',\'{"text":"Traps","italic":"false","color":"gray"}\',\'{"text":"Checkpoints","italic":"false","color":"gray"}\',\'{"text":"Starting locations","italic":"false","color":"gray"}\',\'{"text":"Starting Glass","italic":"false","color":"gray"}\'],Name:\'{"text":"Deathrun Main Selection Wand","italic":"false","color":"yellow"}\'}}',
        if (pos(block) != global_pos2,
            global_pos2 = pos(block);
            print(format('y 2nd position set to ('+global_pos2:0+', '+global_pos2:1+', '+global_pos2:2+').'));
            return('cancel');
        );
    , if  (hand == 'mainhand' && get(item_tuple, 0) == 'diamond_axe' && get(item_tuple, 2) == '{Damage:0,display:{Lore:[\'{"text":"Used for selecting the area of:","italic":"false","color":"gray"}\',\'{"text":"Deaths Trap Activation Area","italic":"false","color":"gray"}\'],Name:\'{"text":"Deathrun Trap Trigger Area Selection Wand","italic":"false","color":"green"}\'}}',
        if (pos(block) != global_death_pos2,
            global_death_pos2 = pos(block);
            print(format('y 2nd death area position set to ('+global_death_pos2:0+', '+global_death_pos2:1+', '+global_death_pos2:2+').'));
            return('cancel');
        );
    ));
);

render_selection() -> (
    if (global_render_locations != [],
        for (global_render_locations,
            x1 = min(_:0:0, _:1:0);
            y1 = min(_:0:1, _:1:1);
            z1 = min(_:0:2, _:1:2);
            x2 = max(_:0:0, _:1:0) + 1;
            y2 = max(_:0:1, _:1:1) + 1;
            z2 = max(_:0:2, _:1:2) + 1;
            draw_shape('box', 2, 'color', 0xFF0000F0, 'fill', 0xF0000050, 'from', [x1, y1, z1], 'to', [x2, y2, z2]);
        );
    );
    if (global_render_death_locations != [],
        for (global_render_death_locations,
            x1 = min(_:0:0, _:1:0);
            y1 = min(_:0:1, _:1:1);
            z1 = min(_:0:2, _:1:2);
            x2 = max(_:0:0, _:1:0) + 1;
            y2 = max(_:0:1, _:1:1) + 1;
            z2 = max(_:0:2, _:1:2) + 1;
            draw_shape('box', 2, 'color', 0x00FF00F0, 'fill', 0x00F00050, 'from', [x1, y1, z1], 'to', [x2, y2, z2]);
        );
    );
);

// Functions for Commands

add_checkpoint(direction, name) -> (
    if (global_locations == [],
        print(format('r You must have 1 selection for this.\nRun "/dr add selection" to add 1 selection then try again.'));
        return();
    );
    if (length(global_locations) > 1,
        print(format('r You must only have 1 selection for this.\nRun "/dr wand deselect" and try again with 1 selection.'));
        return();
    );
    global_map_output:'checkpoints':null = {'name' -> name, 'direction' -> direction, 'location' -> global_locations:0};
    print('Added checkpoint.');
    deselect();
);

add_spawn(who, rotation) -> (
    if (global_locations == [],
        print(format('r You must have at least 1 selection for this.\nRun "/dr add selection" to add a selection then try again.'));
        return();
    );

    if (who == 'runner',
        global_map_output:'spawn_locations':'runners':'locations' = global_locations;
        global_map_output:'spawn_locations':'runners':'rotation' = rotation;
        print('Added spawn location(s) for runners.');
    ,
        global_map_output:'spawn_locations':'deaths':'locations' = global_locations;
        global_map_output:'spawn_locations':'deaths':'rotation' = rotation;
        print('Added spawn location(s) for deaths.');
    );
    deselect();
);

add_selection() -> (
    if (global_pos1 != null && global_pos2 == null,
        put(global_locations, null, global_pos1);
        put(global_render_locations, null, [global_pos1, global_pos1]);
        print('Added selected block location to list.');
    , if (global_pos1 != null && global_pos2 != null,
        put(global_locations, null, [global_pos1, global_pos2]);
        put(global_render_locations, null, [global_pos1, global_pos2]);
        print('Added current selection to list.');
    ));

    if (global_death_pos1 != null && global_death_pos2 != null,
        put(global_death_locations, null, [global_death_pos1, global_death_pos2]);
        put(global_render_death_locations, null, [global_death_pos1, global_death_pos2]);
        print('Added current death area selection to list.');
    );
    global_pos1 = null;
    global_pos2 = null;
    global_death_pos1 = null;
    global_death_pos2 = null;
);

define_trap_remove_blocks(deathsRotation, blocks, replaceWith, extraSound) -> (
    global_map_output:'traps':null = {'activate_args' -> ['remove_blocks', if (blocks:0 == null, blocks:1, blocks:0), replaceWith, extraSound, global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_fire(deathsRotation, fire_type) -> (
    global_map_output:'traps':null = {'activate_args' -> ['fire', fire_type, global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_giant(deathsRotation) -> (
    global_map_output:'traps':null = {'activate_args' -> ['giant', global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_flood(deathsRotation, fluid) -> (
    global_map_output:'traps':null = {'activate_args' -> ['flood', fluid, global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_posion(deathsRotation) -> (
    global_map_output:'traps':null = {'activate_args' -> ['poison', global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_tnt(deathsRotation) -> (
    global_map_output:'traps':null = {'activate_args' -> ['tnt', global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_wall(deathsRotation, block) -> (
    global_map_output:'traps':null = {'activate_args' -> ['wall', block, global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_fangs(deathsRotation) -> (
    global_map_output:'traps':null = {'activate_args' -> ['fangs', global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_fire_snake(deathsRotation, direction) -> (
    global_map_output:'traps':null = {'activate_args' -> ['fire_snake', direction, global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_glass_floor(deathsRotation, direction) -> (
    global_map_output:'traps':null = {'activate_args' -> ['glass_floor', direction, global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

define_trap_arrows(deathsRotation) -> (
    global_map_output:'traps':null = {'activate_args' -> ['arrows', global_locations:0], 'deathsarea' -> {'area' -> global_death_locations:0, 'rotation' -> deathsRotation}};
    deselect();
);

save_map() -> (
    if (global_map_output:'map_name' == '',
        print(format('r You must set a name for this deathrun map.\nRun: /dr set mapname <name>'));
        return();
    );
    if (global_map_output:'starting_glass' == [],
        print(format('r You must set the starting glass location for this deathrun map.\nRun: /dr set startglass'));
        return();
    );
    if (global_map_output:'spawn_locations':'runners':'locations' == [],
        print(format('r You must set at least 1 starting location for runners for this deathrun map.\nRun: /dr add spawn runner'));
        return();
    );
    if (global_map_output:'spawn_locations':'deaths':'locations' == [],
        print(format('r You must set at least 1 starting location for deaths for this deathrun map.\nRun: /dr add spawn death'));
        return();
    );
    if (global_map_output:'traps' == [],
        print(format('r You must set at least 1 trap location for this deathrun map.\nRun: /dr define trap <trap> [<options>]'));
        return();
    );
    if (global_map_output:'checkpoints' == [],
        print(format('r You must set at least 1 checkpoint location for this deathrun map.\nRun: /dr add checkpoint'));
        return();
    );
    write_file(global_map_output:'map_name', 'shared_json', global_map_output);
    print('Wrote map to file: scripts/shared/'+global_map_output:'map_name'+'.json');
);

load_map(name) -> (
    global_map_output = read_file(name, 'shared_json');
    system_variable_set('deathrun:map_data', global_map_output);
    print('Loaded map file from: scripts/shared/'+name+'.json');
);

set_name(name) -> (
    put(global_map_output, 'map_name', name);
    print('set map name to: '+name);
);

set_start_glass() -> (
    if (global_locations == [],
        print(format('r You must have 1 selection for this.\nRun "/dr add selection" to add 1 selection then try again.'));
        return();
    );
    if (length(global_locations) > 1,
        print(format('r You must only have 1 selection for this.\nRun "/dr wand deselect" and try again with 1 selection.'));
        return();
    );
    print('Added starting glass located at pos: '+str(global_locations:0));
    put(global_map_output, 'starting_glass', global_locations:0);
    print(str(global_map_output));
    deselect();
);

give_wand() -> (
    run('give @s minecraft:golden_axe{display:{Name:\'{"text":"Deathrun Main Selection Wand","italic":"false","color":"yellow"}\',Lore:[\'{"text":"Main wand used for selecting the area of:","italic":"false","color":"gray"}\',\'{"text":"Traps","italic":"false","color":"gray"}\',\'{"text":"Checkpoints","italic":"false","color":"gray"}\',\'{"text":"Starting locations","italic":"false","color":"gray"}\',\'{"text":"Starting Glass","italic":"false","color":"gray"}\']}} 1');
    run('give @s minecraft:diamond_axe{display:{Name:\'{"text":"Deathrun Trap Trigger Area Selection Wand","italic":"false","color":"green"}\',Lore:[\'{"text":"Used for selecting the area of:","italic":"false","color":"gray"}\',\'{"text":"Deaths Trap Activation Area","italic":"false","color":"gray"}\']}} 1');
    print(format('y Left click: select pos #1; Right click: select pos #2'));
);

help() -> (
    print('');
    print('Death Run :: Available Commands:');
    print('');
    print('/dr add checkpoint <name>');
    print('/dr add spawn <playerType>');
    print('/dr define trap <type>');
    print('/dr save');
    print('/dr set mapname <name>');
    print('/dr set startglass');
    print('/dr wand');
    print('/dr wand deselect');
    print('');
    print('Trap Types:');
    c_for(i = 0, i < length(global_traps), i += 1,
        print('    '+global_traps:i);
    );
    print('');
);

deselect() -> (
    global_pos1 = null;
    global_pos2 = null;
    global_death_pos1 = null;
    global_death_pos2 = null;
    global_locations = [];
    global_death_locations = [];
    global_render_locations = [];
    global_render_death_locations = [];
);