import('deathrun_utils', 'timer', 'entity_box');
import('deathrun_spectator', 'become_spectator');

global_map = system_variable_get('deathrun:map_data', {});

__on_tick() -> (
    if (query(player(), 'has_scoreboard_tag', 'dr_runner'),
        modify(player(), 'hunger', 20);
        modify(player(), 'saturation', 20);
        modify(player(), 'portal_cooldown', 1000);

        checkpoint();
        speed_pad();
        jump_pad();

        if (_should_player_be_dead(player()),
            _send_player_to_checkpoint_or_start_location(player());
        );
    );
);

__on_player_drops_item(player) -> (
    if (query(player(), 'has_scoreboard_tag', 'dr_runner'),
        return('cancel');
    );
);

__on_player_drops_stack(player) -> ( if (query(player(), 'has_scoreboard_tag', 'dr_runner'), return('cancel'); ); );

setup_runners(player) -> (
    modify(player, 'tag', 'dr_runner');
    inventory_set(player, 0, 1, 'feather', '{display:{Name:\'[{"text":"Leap ","italic":"false","color":"yellow"},{"text":"[Use]","color":"gray"}]\'}}');
);

__on_player_uses_item(player, item_tuple, hand) -> (
    task_thread('__on_player_uses_item', _(player, item_tuple, hand) -> (
        if (get(item_tuple, 0) == 'feather',
            if (get(item_tuple, 2) == '{display:{Name:\'[{"text":"Leap ","italic":"false","color":"yellow"},{"text":"[Use]","color":"gray"}]\'}}',
                facing = query(player, 'look');
                modify(player, 'motion', get(facing, 0) * 1.8, 0.34, get(facing, 2) * 1.8);
                sound('minecraft:entity.ender_dragon.flap', pos(player), 1.0, 1.0, 'hostile');
                scoreboard('leap', player, 1);
                inventory_set(player(), 0, 0);

                timer();
                scoreboard('recharge', player, scoreboard('second', '.cooldownTime'));
                while (scoreboard('second', player) <= scoreboard('second', '.cooldownTime') + 1, scoreboard('second', '.cooldownTime') + 1,
                    if (query(player, 'has_scoreboard_tag', 'dr_runner'),
                        display_title(player, 'actionbar', format(['l Leap Recharging... ','w '+scoreboard('recharge', player)]));
                        if (scoreboard('recharge', player) == 0,
                            setup_runners(player);
                            scoreboard('second', player, 0);
                            scoreboard('leap', player, 0);
                        );
                        sleep(1000);
                    );
                );
            );
        );
    ), player, item_tuple, hand);
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if (query(player, 'has_scoreboard_tag', 'dr_runner'),
        return('cancel');
    );
);

// Up arrow: ▲
// Down arrow: ▼
// Right arrow: ▶
// Left arrow: ◀
checkpoint() -> (
    checkpoints = global_map:'checkpoints';
    id = _get_checkpoint_id();
    name = checkpoints:id:'name';
    if (_is_player_in_checkpoint() && scoreboard('lastCheckpointId', player()) < _get_checkpoint_id() && scoreboard('lastCheckpointId', player()) == (_get_checkpoint_id() - 1),
        scoreboard('lastCheckpointId', player(), _get_checkpoint_id());
        if (scoreboard('lastCheckpointId', player()) == (length(checkpoints) - 1),
            players = player('*');
            delete(players, (players ~ player()));
            finish_time = time();
            print(player(), format('l ▶ ','y You finished in ','w '+_ordinal(for (player('*'), scoreboard('lastCheckpointId', _) == length(global_map:'checkpoints') - 1))+' ','y place! ','ig '+str('[%02d:%06.3f]', floor((finish_time - scoreboard('deathrun_variables', '#start_time')) / 1000 / 60), (finish_time - scoreboard('deathrun_variables', '#start_time')) / 1000 % 60)));
            for (players,
                print(_, format('l ▶ ','y '+str(player())+' has finished in ','w '+_ordinal(for (player('*'), scoreboard('lastCheckpointId', _) == length(global_map:'checkpoints') - 1))+' ','y place! ','ig '+str('[%02d:%06.3f]', floor((finish_time - scoreboard('deathrun_variables', '#start_time')) / 1000 / 60), (finish_time - scoreboard('deathrun_variables', '#start_time')) / 1000 % 60)));
            );
            become_spectator();
            sound('minecraft:entity.player.levelup', pos(player()), 1, 1, 'player');
            run('script in deathrun_main run global_places:\''+player()+'\' = \''+str('[%02d:%06.3f]', floor((finish_time - scoreboard('deathrun_variables', '#start_time')) / 1000 / 60), (finish_time - scoreboard('deathrun_variables', '#start_time')) / 1000 % 60)+'\'');
            if (scoreboard('deathrun_variables', '#dr_timer_reduced') == 0,
                for (player('*'),
                    sound('minecraft:entity.elder_guardian.curse', pos(_), 1, 1, 'hostile');
                    print(_, format('d ▶ ','y A player has finished! ','g Timer shortened to 60 seconds.'));
                    scoreboard('deathrun_variables', '#dr_timer_minutes', 1);
                    scoreboard('deathrun_variables', '#dr_timer_seconds', 0);
                );
                scoreboard('deathrun_variables', '#dr_timer_reduced', 1);
            );
        ,
            print(player(), format('l ▶ Checkpoint Reached: ','w '+name));
            if (scoreboard('leap', player()) == 0,
                display_title(player(), 'actionbar', format('l Checkpoint! ','g '+name));
            );
            sound('minecraft:entity.player.levelup', pos(player()), 1, 1, 'player');
        );
    );
);

speed_pad() -> (
    top_loc = pos_offset(pos(player()), 'down', 1);

    if (block(top_loc) == 'redstone_block',
        bottom_loc = pos_offset(pos(player()), 'down', 2);

        if (block(bottom_loc) == 'oak_sign' || block(bottom_loc) == 'oak_wall_sign',
            if (!query(player(), 'has_scoreboard_tag', 'has_speed'),
                sign_data = block_data(bottom_loc);

                text1 = get(split('"', get(sign_data, 'Text1')), -2);
                text2 = get(split('"', get(sign_data, 'Text2')), -2);

                modify(player(), 'effect', 'speed', 20 * number(text2), number(text1));
                modify(player(), 'tag', 'has_speed');
            );
        );
    ,
        modify(player(), 'clear_tag', 'has_speed');
    );
);

jump_pad() -> (
    top_loc = pos_offset(pos(player()), 'down', 1);

    if (block(top_loc) == 'emerald_block',
        bottom_loc = pos_offset(pos(player()), 'down', 2);

        if (block(bottom_loc) == 'oak_sign' || block(bottom_loc) == 'oak_wall_sign',
            if (!query(player(), 'has_scoreboard_tag', 'has_jump'),
                sign_data = block_data(bottom_loc);

                text1 = get(split('"', get(sign_data, 'Text1')), -2);
                text2 = get(split('"', get(sign_data, 'Text2')), -2);

                modify(player(), 'effect', 'jump_boost', 20 * number(text2), number(text1));
                modify(player(), 'tag', 'has_jump');
            );
        );
    ,
        modify(player(), 'clear_tag', 'has_jump');
    );
);

// Helper functions

_is_player_in_checkpoint() -> (
    checkpoints = global_map:'checkpoints';
    for (checkpoints,
        location = _:'location';
        pos1 = location:0;
        pos2 = location:1;

        players_in_area = entity_box('player', pos1, pos2);
        if ((players_in_area ~ player()) >= 0,
            return(true);
        );
    );
    return(false);
);

_get_checkpoint_id() -> (
    checkpoints = global_map:'checkpoints';
    for (checkpoints,
        location = _:'location';
        pos1 = location:0;
        pos2 = location:1;

        players_in_area = entity_box('player', pos1, pos2);
        if ((players_in_area ~ player()) >= 0,
            return(_i);
        );
    );
    return(-1);
);

_should_player_be_dead(player) -> (
    standing_in_block = block(pos(player));
    if (standing_in_block == 'water' || standing_in_block == 'lava' || standing_in_block == 'fire' || standing_in_block == 'soul_fire' || run('data get entity '+player+' HurtTime'):0 > 0,
        return(true);
    );
    return(false);
);

_send_player_to_checkpoint_or_start_location(player) -> (
    checkpoint = global_map:'checkpoints':scoreboard('lastCheckpointId', player);
    start_locations = global_map:'spawn_locations':'runners':'locations';
    start_rotation = global_map:'spawn_locations':'runners':'rotation';
    if (scoreboard('lastCheckpointId', player) == -1,
        location_index = rand(length(start_locations) - 1);
        location = start_locations:location_index;
        modify(player, 'pos', [location:0 + 0.5, location:1, location:2 + 0.5]);
        modify(player, 'yaw', start_rotation:1);
        modify(player, 'pitch', start_rotation:0);
    ,
        location = checkpoint:'location';
        pos1 = location:0;
        pos2 = location:1;

        checkpoint_ground_center = [floor((pos1:0 + pos2:0) / 2),min(pos1:1, pos2:1),floor((pos1:2 + pos2:2) / 2)];
        checkpoint_exit_direction = checkpoint:'direction';
        rotation = [0];
        if (checkpoint_exit_direction == 'north',
            checkpoint_ground_center:2 += -2;
            rotation:null = 180;
        , if (checkpoint_exit_direction == 'south',
            checkpoint_ground_center:2 += 2;
            rotation:null = 0;
        , if (checkpoint_exit_direction == 'east',
            checkpoint_ground_center:0 += 2;
            rotation:null = -90;
        , if (checkpoint_exit_direction == 'west',
            checkpoint_ground_center:0 += -2;
            rotation:null = 90;
        ))));
        modify(player, 'pos', [checkpoint_ground_center:0 + 0.5, checkpoint_ground_center:1, checkpoint_ground_center:2 + 0.5]);
        modify(player, 'yaw', rotation:1);
        modify(player, 'pitch', rotation:0);
    );
    scoreboard('deathCount', player, (scoreboard('deathCount', player) + 1));
    sound('minecraft:entity.zombie.hurt', pos(player), 1, 1, 'hostile');
);

_ordinal(num) -> (
    if (11 <= (num % 100) <= 13,
        suffix = 'th';
    ,
        suffix = ['th','st','nd','rd','th']:min(num % 10, 4);
    );
    return(str(num) + suffix);
);