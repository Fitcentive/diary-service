-- # -- !Ups

-- Constraint tables
create table meal_entry_types (
    name varchar not null constraint pk_meal_entry_types primary key,
    description varchar not null
);

insert into meal_entry_types (name, description) values ('Breakfast', 'First meal of the day');
insert into meal_entry_types (name, description) values ('Lunch',     'Mid day meal');
insert into meal_entry_types (name, description) values ('Dinner',    'Last meal of the day');
insert into meal_entry_types (name, description) values ('Snack',     'Any other meal');

create table food_entries (
    id uuid not null constraint pk_food_entries primary key,
    user_id uuid not null,
    food_id integer not null,
    serving_id integer not null,
    number_of_servings decimal not null,
    meal_entry varchar not null constraint fk_meal_entry references meal_entry_types (name),
    entry_date timestamp not null default now(),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

-- # -- !Downs