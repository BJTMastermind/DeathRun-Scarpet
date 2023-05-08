import('deathrun_runner', 'setup_runners');
import('deathrun_death', 'setup_deaths');
import('deathrun_spectator', 'leave_spectator');
import('deathrun_utils', 'clock');

global_map = system_variable_get('deathrun:map_data', {});
global_starting_glass_backup = {};
global_places = {};

__config() -> {
    'scope' -> 'global';
};

__on_tick() -> (
    for (player('*'),
        run('bossbar set '+lower(_)+':display_runner name [{"text":"Checkpoints: '+(scoreboard('lastCheckpointId', _) + 1)+'"},{"text":"/'+(length(global_map:'checkpoints'))+' ","color":"gray"},{"text":"Deaths: '+(scoreboard('deathCount', _))+' "},{"text":"Time: '+str('%02d:%02d', scoreboard('deathrun_variables', '#dr_timer_minutes'), scoreboard('deathrun_variables', '#dr_timer_seconds'))+'"}]');
        run('bossbar set '+lower(_)+':display_death name [{"text":"Kills: '+(scoreboard('killCount', _))+' "},{"text":"Time: '+str('%02d:%02d', scoreboard('deathrun_variables', '#dr_timer_minutes'), scoreboard('deathrun_variables', '#dr_timer_seconds'))+'"}]');
        run('bossbar set deathrun:display_spectator name [{"text":"Checkpoints: '+(for (player('*'), scoreboard('lastCheckpointId', _) == length(global_map:'checkpoints') - 1))+' "},{"text":"Time: '+str('%02d:%02d', scoreboard('deathrun_variables', '#dr_timer_minutes'), scoreboard('deathrun_variables', '#dr_timer_seconds'))+'"}]');
    );

    if (scoreboard('deathrun_variables', '#dr_is_running') == 1 && _have_all_players_finished(),
        end();
    );

    if (scoreboard('deathrun_variables', '#dr_is_running') == 1 && _has_time_ran_out(),
        end();
    );
);

__on_start() -> (
    init();

    run('script load deathrun_death');
    run('script load deathrun_runner');
    run('script load deathrun_spectator');
    run('script load deathrun_traps');
    run('script load deathrun_utils');
    run('script load dr');
);

init() -> (
    run('gamerule commandBlockOutput false');
    run('gamerule logAdminCommands false');

    if (scoreboard('leap') == null,
        scoreboard_add('leap');
    );
    if (scoreboard('second') == null,
        scoreboard_add('second');
    );
    if (scoreboard('recharge') == null,
        scoreboard_add('recharge');
    );
    if (scoreboard('lastTrapId') == null,
        scoreboard_add('lastTrapId');
    );
    if (scoreboard('lastCheckpointId') == null,
        scoreboard_add('lastCheckpointId');
    );
    if (scoreboard('deathCount') == null,
        scoreboard_add('deathCount');
    );
    if (scoreboard('killCount') == null,
        scoreboard_add('killCount');
    );
    if (scoreboard('deathrun_variables') == null,
        scoreboard_add('deathrun_variables');
    );

    scoreboard('second', '.cooldownTime', 15);
    scoreboard('deathrun_variables', '#dr_is_running', 0);
    scoreboard('deathrun_variables', '#dr_timer_minutes', 5);
    scoreboard('deathrun_variables', '#dr_timer_seconds', 0);
    scoreboard('deathrun_variables', '#dr_timer_reduced', 0);

    for (player('*'),
        scoreboard('leap', _, 0);
        scoreboard('second', _, 0);
        scoreboard('recharge', _, 0);
        scoreboard('lastTrapId', _, 0);
        scoreboard('lastCheckpointId', _, -1);
        scoreboard('deathCount', _, 0);
        scoreboard('killCount', _, 0);
    );

    global_places = {};
);

start() -> (
    task_thread('start', _() -> (
        _randomize_players();
        _select_random_deaths();
        c_for (i = 10, i > 0, i += -1,
            display_title(player('*'), 'actionbar', format('y Game Start ▶ ',if (i > 3, 'y', if (i >= 1, 'r', 'g'))+' ▋',if (i > 3, 'y', if (i >= 2, 'r', 'g'))+' ▋',if (i > 3, 'y', if (i >= 3, 'r', 'g'))+' ▋',if (i >= 4, 'y', 'g')+' ▋',if (i >= 5, 'y', 'g')+' ▋',if (i >= 6, 'y', 'g')+' ▋',if (i >= 7, 'y', 'g')+' ▋',if (i >= 8, 'y', 'g')+' ▋',if (i >= 9, 'y', 'g')+' ▋',if (i == 10, 'y', 'g')+' ▋','w  '+i));
            if (i >= 1 && i <= 3,
                sound('minecraft:block.dispenser.dispense', pos(player), 1, 1, 'block');
            );
            sleep(1000);
        );
        scoreboard('deathrun_variables', '#dr_is_running', 1);
        schedule(20, 'clock');
        sound('minecraft:entity.zombie_villager.cure', pos(player()), 1, 1, 'hostile');
        print(player('*'), format('l ▶ ','c The game has started! Run!'));
        _drop_starting_glass(global_map:'starting_glass');
        for (player('*'),
            bossbar(lower(_)+':display_runner');
            bossbar(lower(_)+':display_death');
            bossbar('deathrun:display_spectator');
            run('bossbar set '+lower(_)+':display_runner name [{"text":"Checkpoints: 0"},{"text":"/'+(length(global_map:'checkpoints'))+' ","color":"gray"},{"text":"Deaths: 0 "},{"text":"Time: 05:00"}]');
            run('bossbar set '+lower(_)+':display_death name [{"text":"Kills: 0 "},{"text":"Time: 05:00"}]');
            run('bossbar set deathrun:display_spectator name [{"text":"Checkpoints: 0 "},{"text":"Time: 05:00"}]');
            if (query(_, 'has_scoreboard_tag', 'dr_runner'),
                bossbar(lower(_)+':display_runner', 'players', _);
            ,
                bossbar(lower(_)+':display_death', 'players', _);
            );
        );
        scoreboard('deathrun_variables', '#start_time', time());
    ));
);

end() -> (
    scoreboard('deathrun_variables', '#dr_is_running', 0);
    for (bossbar(),
        bossbar(_, 'players', null);
        bossbar(_, 'remove');
    );
    display_title(player('*'), 'title', format('d ▶ ','r Game Over ','d ◀'));
    display_title(player('*'), 'subtitle', '');
    display_title(player('*'), 'actionbar', '');
    for (player('*'),
        sound('minecraft:entity.blaze.shoot', pos(_), 1, 1, 'hostile');
        leave_spectator(_);
        modify(_, 'clear_tag', 'dr_runner');
        modify(_, 'clear_tag', 'dr_death');
        modify(_, 'clear_tag', 'dr_spectator');
        c_for (i = 0, i <= 8, i += 1,
            inventory_set(_, i, 0);
        );
        c_for (i = 0, i < min(length(global_places), 3), i += 1,
            print(_, format('y ▶ ',str('l %s Place: ', _ordinal(i + 1)),str('y %s ', keys(global_places):i),str('ig %s', values(global_places):i)));
        );
    );
    init();
    schedule(20 * 3, 'reset');
);

reset() -> (
    c_for (i = 0, i < length(global_starting_glass_backup), i += 1,
        key = keys(global_starting_glass_backup):i;
        value = global_starting_glass_backup:key;
        set(key, value);
    );
);

// Helper functions

_randomize_players() -> (
    players = player('*');
    start_locations = copy(global_map:'spawn_locations':'runners':'locations');
    rotation = global_map:'spawn_locations':'runners':'rotation';
    for (players,
        location_index = floor(rand(length(start_locations)));
        location = start_locations:location_index;
        delete(start_locations, location_index);

        modify(_, 'pos', [location:0 + 0.5, location:1, location:2 + 0.5]);
        modify(_, 'yaw', rotation:1);
        modify(_, 'pitch', rotation:0);
        modify(_, 'tag', 'dr_runner');
        setup_runners(_);
    );
);

_select_random_deaths() -> (
    start_locations = global_map:'spawn_locations':'deaths':'locations';
    rotation = global_map:'spawn_locations':'deaths':'rotation';
    death_count = if (length(player('*')) == 1, 0, if (length(player('*')) == 2 || length(player('*')) == 3, 1, 2));
    if (length(player('*')) > 1,
        for (range(death_count),
            death = entity_selector('@r[tag=dr_runner]'):0;
            location = start_locations:_;

            modify(death, 'pos', [location:0 + 0.5, location:1, location:2 + 0.5]);
            modify(death, 'yaw', rotation:1);
            modify(death, 'pitch', rotation:0);
            modify(death, 'clear_tag', 'dr_runner');
            modify(death, 'tag', 'dr_death');
            setup_deaths(death);
        );
    );
);

_drop_starting_glass(area) -> (
    volume(area:0, area:1, put(global_starting_glass_backup, pos(_), block(_)));
    volume(area:0, area:1,
        if (block_tags(_, 'impermeable'),
            set([_x,_y,_z], 'air');
            if (_y > area:0:1,
                spawn('falling_block', [_x + 0.5, _y, _z + 0.5], '{Time:1,BlockState:{Name:"minecraft:'+global_starting_glass_backup:[_x,_y,_z]+'"}}');
            );
        );
    );
);

_have_all_players_finished() -> (
    runners = 0;
    for (player('*'),
        if (query(_, 'has_scoreboard_tag', 'dr_runner'),
            runners += 1;
        );
    );
    return(runners == 0);
);

_has_time_ran_out() -> (
    return(scoreboard('deathrun_variables', '#dr_timer_minutes') == 0 && scoreboard('deathrun_variables', '#dr_timer_seconds') == 0);
);

_ordinal(num) -> (
    if (11 <= (num % 100) <= 13,
        suffix = 'th';
    ,
        suffix = ['th','st','nd','rd','th']:min(num % 10, 4);
    );
    return(str(num) + suffix);
);