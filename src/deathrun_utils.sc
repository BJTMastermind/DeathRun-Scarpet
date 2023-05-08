timer() -> (
    if (scoreboard('leap', player()) == 1,
        scoreboard('second', player(), (scoreboard('second', player()) + 1));
        scoreboard('recharge', player(), (scoreboard('recharge', player()) - 1));
        if (scoreboard('second', player()) >= scoreboard('second', '.cooldownTime') + 1,
            return();
        );
        schedule(20, 'timer');
    );
);

clock() -> (
    if (scoreboard('deathrun_variables', '#dr_timer_seconds') == 0,
        scoreboard('deathrun_variables', '#dr_timer_minutes', scoreboard('deathrun_variables', '#dr_timer_minutes') - 1);
        scoreboard('deathrun_variables', '#dr_timer_seconds', 59);
    ,
        scoreboard('deathrun_variables', '#dr_timer_seconds', scoreboard('deathrun_variables', '#dr_timer_seconds') - 1);
    );
    if (scoreboard('deathrun_variables', '#dr_is_running') == 1,
        schedule(20, 'clock');
    );
);

fill(pos1, pos2, block_type, ... replace) -> (
    if (length(replace) > 1,
        throw('Function fill(pos1, pos2, block_type, replace?) requires 3 to 4 arguments, but '+(3 + length(replace))+' were given.');
    );
    replace_type = if (length(replace) == 1, replace:0, null);

    task_thread('fill', _(pos1, pos2, block_type, replace_type) -> (
        c_for (x = min(pos1:0, pos2:0), x <= max(pos1:0, pos2:0), x += 1,
            c_for (y = min(pos1:1, pos2:1), y <= max(pos1:1, pos2:1), y += 1,
                c_for (z = min(pos1:2, pos2:2), z <= max(pos1:2, pos2:2), z += 1,
                    location = [x,y,z];
                    without_updates(
                        if (replace_type != null,
                            if (block(location) == replace_type,
                                set(location, block_type);
                            );
                        ,
                            set(location, block_type);
                        );
                    );
                );
            );
        );
    ), pos1, pos2, block_type, replace_type);
);

clamp(value, lower, upper) -> (
    return(max(lower, min(value, upper)));
);

entity_box(type, from_pos, to_pos) -> (
    x = min(from_pos:0, to_pos:0);
    y = min(from_pos:1, to_pos:1);
    z = min(from_pos:2, to_pos:2);
    dx = max(from_pos:0, to_pos:0) - x;
    dy = max(from_pos:1, to_pos:1) - y;
    dz = max(from_pos:2, to_pos:2) - z;
    return(entity_selector('@e[type='+type+',x='+x+',y='+y+',z='+z+',dx='+dx+',dy='+dy+',dz='+dz+']'));
);