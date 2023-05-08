global_map = system_variable_get('deathrun:map_data', {});

__on_tick() -> (
    if (query(player(), 'has_scoreboard_tag', 'dr_spectator'),
        modify(player(), 'hunger', 20);
        modify(player(), 'saturation', 20);
        modify(player(), 'portal_cooldown', 1000);

        run('title '+player()+' actionbar {"text":"You\'ve finished! Yay! ","color":"aqua"}');
    );
);

__on_player_uses_item(player, item_tuple, hand) -> (
    if (hand == 'mainhand' && get(item_tuple, 0) == 'compass' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"Teleporter ","italic":"false","color":"aqua"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        teleporter();
        return('cancel');
    );
    if (hand == 'mainhand' && get(item_tuple, 0) == 'blaze_powder' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"Report Player ","italic":"false","color":"red"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        report_player();
    );
    if (hand == 'mainhand' && get(item_tuple, 0) == 'heart_of_the_sea' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"New Game ","italic":"false","color":"green"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        new_game();
    );
    if (hand == 'mainhand' && get(item_tuple, 0) == 'dragon_breath' && get(item_tuple, 2) == '{display:{Name:\'[{"text":"Leave Game ","italic":"false","color":"red"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}',
        leave_game();
    );
);

init() -> (
    inventory_set(player(), 0, 1, 'compass', '{display:{Name:\'[{"text":"Teleporter ","italic":"false","color":"aqua"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
    // inventory_set(player(), 1, 1, 'blaze_powder', '{display:{Name:\'[{"text":"Report Player ","italic":"false","color":"red"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
    // inventory_set(player(), 7, 1, 'heart_of_the_sea', '{display:{Name:\'[{"text":"New Game ","italic":"false","color":"green"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
    // inventory_set(player(), 8, 1, 'dragon_breath', '{display:{Name:\'[{"text":"Leave Game ","italic":"false","color":"red"},{"text":"[Right-Click]","italic":"false","color":"gray"}]\'}}');
);

become_spectator() -> (
    if (!query(player(), 'has_scoreboard_tag', 'dr_spectator'),
        modify(player(), 'clear_tag', 'dr_runner');
        modify(player(), 'tag', 'dr_spectator');
        modify(player(), 'effect', 'invisibility', 9999, 0, false, false);
        modify(player(), 'may_fly', true);

        modify(player(), 'effect', 'blindness', 20 * 2, 0, false, true);
        display_title(player(), 'title', format('r You\'re a spectator!'), 5, 35, 10);
        display_title(player(), 'subtitle', format('y Now spectating.'), 5, 35, 10);
        display_title(player(), 'actionbar', '');

        location = _get_random_runner_start_location();
        rotation = global_map:'spawn_locations':'runners':'rotation';
        modify(player(), 'pos', [location:0 + 0.5, location:1, location:2 + 0.5]);
        modify(player(), 'yaw', rotation:1);
        modify(player(), 'pitch', rotation:0);

        bossbar(lower(player())+':display_runner', 'players', null);
        bossbar('deathrun:display_spectator', 'players', player());

        init();
    );
);

leave_spectator(player) -> (
    if (query(player, 'has_scoreboard_tag', 'dr_spectator'),
        modify(player, 'clear_tag', 'dr_spectator');
        modify(player, 'effect', 'invisibility', 0, 0, false, false);
        if (query(player, 'gamemode') != 'creative' && query(player, 'gamemode') != 'spectator',
            modify(player, 'may_fly', false);
        );
    );
);

teleporter() -> (
    runners = player('*');
    for (runners,
        if (query(player(_), 'has_scoreboard_tag', 'dr_spectator'),
            delete(runners, (runners ~ player(_)));
        );
    );

    screen = create_screen(player(), 'generic_9x3', 'Teleport to Player', _(screen, player, action, data) -> (
        if (action == 'pickup',
            item_tuple = inventory_get(screen, data:'slot');
            if (get(item_tuple, 0) == 'light_blue_concrete',
                runner = split('"', item_tuple:2:'display':'Name'):3;
                modify(player(), 'pos', pos(player(runner)));
                close_screen(screen);
            );
        );
        return('cancel');
    ));
    sound('minecraft:entity.item.pickup', pos(player()), 1, 0, 'neutral');

    for (runners,
        inventory_set(screen, _i, 1, 'light_blue_concrete', '{display:{Name:\'{"text":"'+_+'","italic":"false","color":"aqua"}\'}}');
    );
);

report_player() -> (
    print(player(), format('r Not implemented yet. Contact the server admin to set this up.'));
);

new_game() -> (
    print(player(), format('r Not implemented yet. Contact the server admin to set this up.'));
);

leave_game() -> (
    print(player(), format('r Not implemented yet. Contact the server admin to set this up.'));
);

// Helper functions

_get_random_runner_start_location() -> (
    start_locations = global_map:'spawn_locations':'runners':'locations';
    location_index = rand(length(start_locations) - 1);
    location = start_locations:location_index;
    return(location);
);