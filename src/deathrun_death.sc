import('deathrun_utils', 'entity_box', 'clamp');

global_map = system_variable_get('deathrun:map_data', {});

__on_tick() -> (
    if (query(player(), 'has_scoreboard_tag', 'dr_death'),
        modify(player(), 'hunger', 20);
        modify(player(), 'saturation', 20);
        modify(player(), 'portal_cooldown', 1000);

        modify(player(), 'effect', 'speed', 20 * 1, 2, false, true);

        give_activator_items();
    );
);

__on_player_uses_item(player, item_tuple, hand) -> (
    if (hand == 'mainhand' && get(item_tuple, 0) == 'stick' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"Last Trap ","italic":"false","color":"red"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        teleport_to_last_trap();
    );
    if (hand == 'mainhand' && get(item_tuple, 0) == 'arrow' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"Next Trap ","italic":"false","color":"green"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        teleport_to_next_trap();
    );
    if (hand == 'mainhand' && get(item_tuple, 0) == 'slime_ball' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"Activate Trap ","italic":"false","color":"green"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        activate_trap();
    );
    if (hand == 'mainhand' && get(item_tuple, 0) == 'compass' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"Teleporter ","italic":"false","color":"light_purple"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        teleport_to_trap();
        return('cancel');
    );
);

setup_deaths(player) -> (
    modify(player, 'tag', 'dr_death');
    inventory_set(player, 0, 1, 'stick', '{display:{Name:\'[{"text":"Last Trap ","italic":"false","color":"red"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
    inventory_set(player, 1, 1, 'arrow', '{display:{Name:\'[{"text":"Next Trap ","italic":"false","color":"green"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
    inventory_set(player, 8, 1, 'compass', '{display:{Name:\'[{"text":"Teleporter ","italic":"false","color":"light_purple"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
);

give_activator_items() -> (
    if (_is_player_in_deaths_area(),
        c_for (i = 2, i <= 7, i += 1,
            inventory_set(player(), i, 1, 'slime_ball', '{display:{Name:\'[{"text":"Activate Trap ","italic":"false","color":"green"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
        );
        scoreboard('lastTrapId', player(), _get_trap_id());
    ,
        c_for (i = 2, i <= 7, i += 1,
            inventory_set(player(), i, 0, 'air');
        );
    );
);

// Death item triggers

teleport_to_last_trap() -> (
    traps = global_map:'traps';
    if (scoreboard('lastTrapId', player()) == 0,
        print(format('r You are at the first trap. no more before this.'));
        return();
    );
    deathsarea = traps:(scoreboard('lastTrapId', player()) - 1):'deathsarea':'area';
    rotation = traps:(scoreboard('lastTrapId', player()) - 1):'deathsarea':'rotation';
    pos1 = deathsarea:0;
    pos2 = deathsarea:1;
    center_point = [floor((pos1:0 + pos2:0) / 2) + 0.5, min(pos1:1, pos2:1), floor((pos1:2 + pos2:2) / 2) + 0.5];
    modify(player(), 'pos', center_point);
    modify(player(), 'yaw', rotation:1);
    modify(player(), 'pitch', rotation:0);
);

teleport_to_next_trap() -> (
    traps = global_map:'traps';
    if (scoreboard('lastTrapId', player()) == (length(traps) - 1),
        print(format('r You are at the last trap. no more after this.'));
        return();
    );
    deathsarea = traps:(scoreboard('lastTrapId', player()) + 1):'deathsarea':'area';
    rotation = traps:(scoreboard('lastTrapId', player()) + 1):'deathsarea':'rotation';
    pos1 = deathsarea:0;
    pos2 = deathsarea:1;
    center_point = [floor((pos1:0 + pos2:0) / 2) + 0.5, min(pos1:1, pos2:1), floor((pos1:2 + pos2:2) / 2) + 0.5];
    modify(player(), 'pos', center_point);
    modify(player(), 'yaw', rotation:1);
    modify(player(), 'pitch', rotation:0);
);

activate_trap() -> (
    traps = global_map:'traps';
    id = _get_trap_id();
    activate_args = traps:id:'activate_args';
    trap_function = '';
    // function builder
    c_for (i = 0, i < length(activate_args), i += 1,
        if (i == 0,
            trap_function += str(activate_args:i) + '(';
        , if (i < length(activate_args) - 1,
            if (type(activate_args:i) == 'string',
                trap_function += '\''+str(activate_args:i)+'\'' + ', ';
            ,
                trap_function += str(activate_args:i) + ', ';
            );
        , if (i < length(activate_args),
            if (type(activate_args:i) == 'string',
                trap_function += '\''+str(activate_args:i)+'\'' + ');';
            ,
                trap_function += str(activate_args:i) + ');';
            );
        ,
            if (type(activate_args:i) == 'string',
                trap_function += '\''+str(activate_args:i)+'\'';
            ,
                trap_function += str(activate_args:i);
            );
        )));
    );
    run('script in deathrun_traps run '+trap_function);
);

teleport_to_trap() -> (
    traps = global_map:'traps';
    size = if (length(traps) <= 27,
        'generic_9x3';
    , if (length(traps) > 27 && length(traps) <= 54,
        'generic_9x6';
    ));

    screen = create_screen(player(), size, 'Teleport Locations', _(screen, player, action, data) -> (
        traps = global_map:'traps';
        if (action == 'pickup',
            item_tuple = inventory_get(screen, data:'slot');
            if (get(item_tuple, 0) == 'red_concrete',
                trap_id = data:'slot';
                deathsarea = traps:trap_id:'deathsarea':'area';
                rotation = traps:trap_id:'deathsarea':'rotation';
                pos1 = deathsarea:0;
                pos2 = deathsarea:1;
                center_point = [floor((pos1:0 + pos2:0) / 2) + 0.5, min(pos1:1, pos2:1), floor((pos1:2 + pos2:2) / 2) + 0.5];
                modify(player, 'pos', center_point);
                modify(player, 'yaw', rotation:1);
                modify(player, 'pitch', rotation:0);
                print('Teleported to: Trap '+data:'slot');
                close_screen(screen);
            );
        );
        return('cancel');
    ));
    sound('minecraft:entity.item.pickup', pos(player()), 1, 0, 'neutral');

    for (traps,
        inventory_set(screen, _i, 1, 'red_concrete', '{display:{Name:\'{"text":"Trap '+_i+'","italic":"false","bold":"true","color":"red"}\'}}');
    );
);

// Helper functions

_is_player_in_deaths_area() -> (
    traps = global_map:'traps';
    for (traps,
        deathsarea = _:'deathsarea':'area';
        pos1 = deathsarea:0;
        pos2 = deathsarea:1;

        players_in_area = entity_box('player', pos1, pos2);
        if ((players_in_area ~ player()) >= 0,
            return(true);
        );
    );
    return(false);
);

_get_trap_id() -> (
    traps = global_map:'traps';
    for (traps,
        deathsarea = _:'deathsarea':'area';
        pos1 = deathsarea:0;
        pos2 = deathsarea:1;

        players_in_area = entity_box('player', pos1, pos2);
        if ((players_in_area ~ player()) >= 0,
            return(_i);
        );
    );
    return(-1);
);