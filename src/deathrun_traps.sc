import('deathrun_utils', 'fill', 'entity_box');
import('deathrun_runner', '_send_player_to_checkpoint_or_start_location');

__config() -> (
    {
        'scope' -> 'global';
    }
);

__on_tick() -> (
    run('execute as @e[type=falling_block] at @s unless block ~ ~-0.5 ~ air run kill @s');
);

glass_floor(direction, area) -> (
    task_thread('glass_floor', _(direction, area) -> (
        backup = {};

        volume(area:0, area:1, put(backup, pos(_), block(_)));

        top_left = [max(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), max(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), min(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];
        top_right = [max(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), max(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), max(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];
        bottom_left = [min(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), min(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), min(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];
        bottom_right = [min(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), min(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), max(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];

        loop_count = if (direction == 'north' || direction == 'south', (max(area:0:2, area:1:2) - min(area:0:2, area:1:2) + 1), (max(area:0:0, area:1:0) - min(area:0:0, area:1:0) + 1));
        iter = 0;
        loop(loop_count,
            if (direction == 'north',
                c_for(x = bottom_left:0, x <= top_left:0, x += 1,
                    glass_type = block([x + 0.5, bottom_left:1, (bottom_left:2 + 0.5) + iter]);
                    if (block_tags(glass_type, 'impermeable'),
                        spawn('falling_block', [x + 0.5, bottom_left:1, (bottom_left:2 + 0.5) + iter], '{Time:1,BlockState:{Name:"minecraft:'+glass_type+'"}}');
                        set([x + 0.5, bottom_left:1, (bottom_left:2 + 0.5) + iter], 'air');
                        sound('minecraft:block.note_block.harp', [x + 0.5, bottom_left:1, (bottom_left:2 + 0.5) + iter], 1, (2 / (max(area:0:2, area:1:2) - min(area:0:2, area:1:2) + 1)) * iter, 'record');
                    );
                );
            , if (direction == 'south',
                c_for(x = bottom_right:0, x <= top_right:0, x += 1,
                    glass_type = block([x + 0.5, bottom_right:1, (bottom_right:2 + 0.5) - iter]);
                    if (block_tags(glass_type, 'impermeable'),
                        spawn('falling_block', [x + 0.5, bottom_right:1, (bottom_right:2 + 0.5) - iter], '{Time:1,BlockState:{Name:"minecraft:'+glass_type+'"}}');
                        set([x + 0.5, bottom_right:1, (bottom_right:2 + 0.5) - iter], 'air');
                        sound('minecraft:block.note_block.harp', [x + 0.5, bottom_right:1, (bottom_right:2 + 0.5) - iter], 1, (2 / (max(area:0:2, area:1:2) - min(area:0:2, area:1:2) + 1)) * iter, 'record');
                    );
                );
            , if (direction == 'east',
                c_for(z = top_left:2, z <= top_right:2, z += 1,
                    glass_type = block([(top_left:0 + 0.5) - iter, top_left:1, z + 0.5]);
                    if (block_tags(glass_type, 'impermeable'),
                        spawn('falling_block', [(top_left:0 + 0.5) - iter, top_left:1, z + 0.5], '{Time:1,BlockState:{Name:"minecraft:'+glass_type+'"}}');
                        set([(top_left:0 + 0.5) - iter, top_left:1, z + 0.5], 'air');
                        sound('minecraft:block.note_block.harp', [(top_left:0 + 0.5) - iter, top_left:1, z + 0.5], 1, (2 / (max(area:0:0, area:1:0) - min(area:0:0, area:1:0) + 1)) * iter, 'record');
                    );
                );
            , if (direction == 'west',
                c_for(z = bottom_left:2, z <= bottom_right:2, z += 1,
                    glass_type = block([(bottom_left:0 + 0.5) + iter, bottom_left:1, z + 0.5]);
                    if (block_tags(glass_type, 'impermeable'),
                        spawn('falling_block', [(bottom_left:0 + 0.5) + iter, bottom_left:1, z + 0.5], '{Time:1,BlockState:{Name:"minecraft:'+glass_type+'"}}');
                        set([(bottom_left:0 + 0.5) + iter, bottom_left:1, z + 0.5], 'air');
                        sound('minecraft:block.note_block.harp', [(bottom_left:0 + 0.5) + iter, bottom_left:1, z + 0.5], 1, (2 / (max(area:0:0, area:1:0) - min(area:0:0, area:1:0) + 1)) * iter, 'record');
                    );
                );
            ))));
            iter += 1;
            sleep(100); // 2 game tick delay
        );
        schedule(20 * 3, 'restore_blocks', backup);
    ), direction, area);
);

fire_snake(direction, area) -> (
    task_thread('fire_snake', _(direction, area) -> (
        center_point = [floor((area:0:0 + area:1:0) / 2), floor((area:0:1 + area:1:1) / 2), floor((area:0:2 + area:1:2) / 2)];

        top_left = [max(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), max(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), min(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];
        top_right = [max(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), max(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), max(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];
        bottom_left = [min(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), min(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), min(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];
        bottom_right = [min(min(area:0:0, area:1:0), max(area:0:0, area:1:0)), min(min(area:0:1, area:1:1), max(area:0:1, area:1:1)), max(min(area:0:2, area:1:2), max(area:0:2, area:1:2))];

        loop_count = if (direction == 'north' || direction == 'south', (max(area:0:2, area:1:2) - min(area:0:2, area:1:2) + 1), (max(area:0:0, area:1:0) - min(area:0:0, area:1:0) + 1));
        iter = 0;
        sound('minecraft:entity.zombie.break_wooden_door', center_point, 1, 1, 'hostile');
        loop(loop_count,
            if (direction == 'north',
                particle_line('minecraft:flame', [top_left:0 + 0.5, top_left:1 + 0.5, top_left:2 + 0.5 + iter], [bottom_left:0 + 0.5, bottom_left:1 + 0.5, bottom_left:2 + 0.5 + iter], 3);
                players_in_area = entity_box('player', [top_left:0, top_left:1, top_left:2 + iter], [bottom_left:0, bottom_left:1, bottom_left:2 + iter]);
                for (players_in_area,
                    _send_player_to_checkpoint_or_start_location(_);
                );
            , if (direction == 'south',
                particle_line('minecraft:flame', [top_right:0 + 0.5, top_right:1 + 0.5, top_right:2 + 0.5 - iter], [bottom_right:0 + 0.5, bottom_right:1 + 0.5, bottom_right:2 + 0.5 - iter], 3);
                players_in_area = entity_box('player', [top_left:0, top_left:1, top_left:2 - iter], [bottom_left:0, bottom_left:1, bottom_left:2 - iter]);
                for (players_in_area,
                    _send_player_to_checkpoint_or_start_location(_);
                );
            , if (direction == 'east',
                particle_line('minecraft:flame', [top_left:0 + 0.5 - iter, top_left:1 + 0.5, top_left:2 + 0.5], [top_right:0 + 0.5 - iter, top_right:1 + 0.5, top_right:2 + 0.5], 3);
                players_in_area = entity_box('player', [top_left:0 - iter, top_left:1, top_left:2], [top_right:0 - iter, top_right:1, top_right:2]);
                for (players_in_area,
                    _send_player_to_checkpoint_or_start_location(_);
                );
            , if (direction == 'west',
                particle_line('minecraft:flame', [bottom_left:0 + 0.5 + iter, bottom_left:1 + 0.5, bottom_left:2 + 0.5], [bottom_right:0 + 0.5 + iter, bottom_right:1 + 0.5, bottom_right:2 + 0.5], 3);
                players_in_area = entity_box('player', [top_left:0 + iter, top_left:1, top_left:2], [top_right:0 + iter, top_right:1, top_right:2]);
                for (players_in_area,
                    _send_player_to_checkpoint_or_start_location(_);
                );
            ,
                print('Not a vaild direction. Must be [north, south, east, west]');
            ))));
            iter += 1;
            sleep(200); // 4 game tick delay
        );
    ), direction, area);
);

remove_blocks(remove_block_type, replace_with, extra_sound, area) -> (
    backup = {};

    volume(area:0, area:1, put(backup, pos(_), block(_)));

    c_for(i = 0, i < length(backup), i += 1,
        key = keys(backup):i;
        value = backup:key;
        if (value == remove_block_type,
            set(key, replace_with);
            sound('minecraft:entity.item.pickup', key, 1, 1, 'neutral');
            if (extra_sound != null,
                sound(extra_sound, key, 1, 1, 'block');
            );
        );
    );
    schedule(20 * 3, 'restore_blocks', backup);
);

tnt(area) -> (
    center_point = [floor((area:0:0 + area:1:0) / 2), floor((area:0:1 + area:1:1) / 2), floor((area:0:2 + area:1:2) / 2)];

    particle_box('minecraft:explosion', area:0, area:1, 20);
    sound('minecraft:entity.generic.explode', center_point, 1, 1, 'block');

    players_in_area = entity_box('player', area:0, area:1);
    for (players_in_area,
        _send_player_to_checkpoint_or_start_location(_);
    );
);

arrows(area) -> (
    dispensers = {};

    volume(area:0, area:1, if (_ == 'dispenser', put(dispensers, pos(_), str(block_state(_):'facing'))));

    c_for(i = 0, i < length(dispensers), i += 1,
        location = keys(dispensers):i;
        direction = dispensers:location;

        if (direction == 'north',
            sound('minecraft:block.dispenser.launch', location, 1, 1, 'block');
            location = [location:0 + 0.5, location:1 + 0.4, location:2 - 1 + 0.5];
            spawn('arrow', location, '{Motion:[-0.02d,-0.02d,-2.0d],Rotation:[181.0f,0.0f],PierceLevel:25b}');
        , if (direction == 'south',
            sound('minecraft:block.dispenser.launch', location, 1, 1, 'block');
            location = [location:0 + 0.5, location:1 + 0.4, location:2 + 1 + 0.5];
            spawn('arrow', location, '{Motion:[-0.02d,-0.02d,2.0d],Rotation:[1.0f,0.0f],PierceLevel:25b}');
        , if (direction == 'east',
            sound('minecraft:block.dispenser.launch', location, 1, 1, 'block');
            location = [location:0 + 1 + 0.5, location:1 + 0.4, location:2 + 0.5];
            spawn('arrow', location, '{Motion:[2.0d,-0.02d,-0.02d],Rotation:[91.0f,0.0f],PierceLevel:25b}');
        , if (direction == 'west',
            sound('minecraft:block.dispenser.launch', location, 1, 1, 'block');
            location = [location:0 - 1 + 0.5, location:1 + 0.4, location:2 + 0.5];
            spawn('arrow', location, '{Motion:[-2.0d,-0.02d,-0.02d],Rotation:[-91.0f,0.0f],PierceLevel:25b}');
        , if (direction == 'up',
            sound('minecraft:block.dispenser.launch', location, 1, 1, 'block');
            location = [location:0 + 0.5, location:1 + 1, location:2 + 0.5];
            spawn('arrow', location, '{Motion:[-0.01d,2.0d,-0.01d],Rotation:[0.0f,-271.0f],PierceLevel:25b}');
        , if (direction == 'down',
            sound('minecraft:block.dispenser.launch', location, 1, 1, 'block');
            location = [location:0 + 0.5, location:1 - 1, location:2 + 0.5];
            spawn('arrow', location, '{Motion:[-0.01d,-2.0d,-0.01d],Rotation:[0.0f,271.0f],PierceLevel:25b}');
        ,
            print('Something went vary wrong here!');
        ))))));
    );
    schedule(10, _() -> (run('kill @e[type=arrow]')));
);

wall(block_type, area) -> (
    backup = {};

    center_point = [floor((area:0:0 + area:1:0) / 2), floor((area:0:1 + area:1:1) / 2), floor((area:0:2 + area:1:2) / 2)];

    volume(area:0, area:1, put(backup, pos(_) , block(_)));

    fill([area:0:0,area:0:1,area:0:2], [area:1:0,area:1:1,area:1:2], block_type, 'air');
    sound('minecraft:entity.item.pickup', center_point, 1, 1, 'neutral');

    schedule(20 * 3, 'restore_blocks', backup);
);

fire(fire_type, area) -> (
    backup = {};

    center_point = [floor((area:0:0 + area:1:0) / 2), floor((area:0:1 + area:0:1) / 2), floor((area:0:2 + area:1:2) / 2)];

    volume(area:0, area:1, put(backup, pos(_), block(_)));

    carpets = [];
    volume(area:0, area:1,
        if (_ == 'red_carpet',
            put(carpets, null, _);
        );
        if (_ == 'orange_carpet',
            put(carpets, null, _);
        );
        if (_ == 'yellow_carpet',
            put(carpets, null, _);
        );
        if (_ == 'gray_carpet',
            put(carpets, null, _);
        );
    );
    contains_carpets = (length(carpets) > 0);

    sound('minecraft:entity.item.pickup', center_point, 1, 1, 'neutral');
    if (contains_carpets,
        c_for (i = 0, i < length(backup), i += 1,
            location = keys(backup):i;
            block = backup:location;
            if (block == 'red_carpet' || block == 'orange_carpet' || block == 'yellow_carpet' || block == 'gray_carpet',
                set(location, fire_type);
            );
        );
    ,
        fill([area:0:0,area:0:1,area:0:2], [area:1:0,area:1:1,area:1:2], fire_type);
    );
    schedule(20 * 3, 'restore_blocks', backup);
);

flood(fluid, area) -> (
    task_thread('flood', _(fluid, area) -> (
        center_point = [floor((area:0:0 + area:1:0) / 2), floor((area:0:1 + area:1:1) / 2), floor((area:0:2 + area:1:2) / 2)];

        sound('minecraft:entity.item.pickup', center_point, 1, 1, 'neutral');
        without_updates(
            volume(area:0, area:1, if (_ == 'air', set(pos(_), fluid)));
        );

        sleep(3000);
        without_updates(
            volume(area:0, area:1, if (_ == fluid, set(pos(_), 'air')));
        );
    ), fluid, area);
);

giant(area) -> (
    task_thread('giant', _(area) -> (
        center_point = [floor((area:0:0 + area:1:0) / 2), floor((area:0:1 + area:1:1) / 2), floor((area:0:2 + area:1:2) / 2)];

        giant = spawn('giant', [center_point:0 + 0.5, center_point:1 + 5, center_point:2 + 0.5], '{Rotation:[0.0f,0.0f],Silent:1b}');

        onGround = false;
        loop (10,
            if (query(giant, 'on_ground') == true && onGround == false,
                particle_box('minecraft:explosion', [pos(giant):0 + 2, pos(giant):1, pos(giant):2 + 2], [pos(giant):0 - 2, pos(giant):1 + 2, pos(giant):2 - 2], 20);
                sound('minecraft:entity.iron_golem.hurt', center_point, 1, 1, 'neutral');
                modify(giant, 'remove');

                players_in_area = entity_box('player', area:0, area:1);
                for (players_in_area,
                    _send_player_to_checkpoint_or_start_location(_);
                );
                onGround = true;
            );
            sleep(100);
        );
    ), area);
);

poison(area) -> (
    task_thread('poison', _(area) -> (
        center_point = [floor((area:0:0 + area:1:0) / 2), floor((area:0:1 + area:1:1) / 2), floor((area:0:2 + area:1:2) / 2)];

        loop(50,
            c_for (x = min(area:0:0, area:1:0), x <= max(area:0:0, area:1:0), x += 1,
                c_for (z = min(area:0:2, area:1:2), z <= max(area:0:2, area:1:2), z += 1,
                    spawn_particle = floor(rand(5, 1030));
                    color = floor(rand(3, 1030));
                    if (spawn_particle == 3,
                        if (color == 0,
                            run('particle entity_effect '+(x + 0.5)+' '+area:0:1+' '+(z + 0.5)+' 0.5 1 0 1 0 force');
                        , if (color == 1,
                            run('particle entity_effect '+(x + 0.5)+' '+area:0:1+' '+(z + 0.5)+' 1 0.7 0 1 0 force');
                        , if (color == 2,
                            run('particle entity_effect '+(x + 0.5)+' '+area:0:1+' '+(z + 0.5)+' 1 0.85 0 1 0 force');
                        )));
                    );
                );
            );
            sound('minecraft:entity.guardian.attack', center_point, 1, 1, 'hostile');

            players_in_area = entity_box('player', area:0, area:1);
            for (players_in_area,
                _send_player_to_checkpoint_or_start_location(_);
            );

            sleep(100);
        );
        reset_seed(1030);
    ), area);
);

fangs(area) -> (
    task_thread('fangs', _(area) -> (
        c_for (x = min(area:0:0, area:1:0), x <= max(area:0:0, area:1:0), x += 1,
            c_for (z = min(area:0:2, area:1:2), z <= max(area:0:2, area:1:2), z += 1,
                spawn_fangs = floor(rand(5, 1031));
                if (spawn_fangs == 3,
                    spawn('evoker_fangs', [x + 0.5, area:0:1, z + 0.5]);
                );
            );
        );
        reset_seed(1031);

        sleep(500); // 10 ticks delay

        players_in_area = entity_box('player', area:0, area:1);
        for (players_in_area,
            _send_player_to_checkpoint_or_start_location(_);
        );
    ), area);
);

restore_blocks(backup) -> (
    c_for (i = 0, i < length(backup), i += 1,
        key = keys(backup):i;
        value = backup:key;
        set(key, value);
    );
);
