# -- !Ups

-- Constraint tables
create table activity_levels (
    name varchar not null constraint pk_activity_levels primary key,
    description varchar not null
);

insert into activity_levels (name, description) values ('Not very active',   'Little no exercise per week');
insert into activity_levels (name, description) values ('Lightly active',    '1-3 days of exercise per week');
insert into activity_levels (name, description) values ('Moderately active', '3-5 days of exercise per week');
insert into activity_levels (name, description) values ('Active',            '6-7 days per week');
insert into activity_levels (name, description) values ('Very active',       'Hard exercise 6-7 days per week');

create table goals (
    name varchar not null constraint pk_goals primary key,
    description varchar not null
);

insert into goals (name, description) values ('Lose 0.5 lbs per week',  'Caloric deficit needed to lose 0.5 lbs per week');
insert into goals (name, description) values ('Lose 1 lbs per week',    'Caloric deficit needed to lose 1 lbs per week');
insert into goals (name, description) values ('Lose 1.5 lbs per week',  'Caloric deficit needed to lose 1.5 lbs per week');
insert into goals (name, description) values ('Lose 2 lbs per week',    'Caloric deficit needed to lose 2 lbs per week');
insert into goals (name, description) values ('Maintain weight',        'Calories needed to maintain body weight');
insert into goals (name, description) values ('Gain 0.5 lbs per week',  'Caloric surplus needed to gain 2 lbs per week');
insert into goals (name, description) values ('Gain 1 lbs per week',    'Caloric surplus needed to gain 1 lbs per week');
insert into goals (name, description) values ('Gain 1.5 lbs per week',  'Caloric surplus needed to gain 1.5 lbs per week');
insert into goals (name, description) values ('Gain 2 lbs per week',    'Caloric surplus needed to gain 2 lbs per week');

create table steps_taken (
    id uuid not null constraint pk_steps_taken primary key,
    user_id uuid not null,
    steps int not null,
    calories_burned decimal not null,
    entry_date varchar not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint unique_user_date unique (user_id, entry_date)
);

alter table fitness_user_profile
    add column activity_level varchar not null default 'Lightly active' constraint fk_activity_levels references activity_levels (name),
    add column goal varchar not null default 'Maintain weight' constraint fk_goals references goals (name),
    add column step_goal_per_day int;


# -- !Downs