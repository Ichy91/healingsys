drop table  department_closed_day,
    department_closed_day,
    appointment,
    authentication,
    closed_time,
    day_of_week,
    users,
    address,
    department,
    department;

insert into department(created_date, name, opening, closing, max_generated_days, slot_length_in_hour, slot_max_capacity, status)
values (now(), 'Registration', '08:00', '17:00', 10, 0.5, 1, 'ACTIVE');

insert into day_of_week(day)
values ('MONDAY'),
       ('TUESDAY'),
       ('WEDNESDAY'),
       ('THURSDAY'),
       ('FRIDAY'),
       ('SATURDAY'),
       ('SUNDAY');

insert into department_closed_day(department_id, closed_day_id)
values (1, 6),
       (1, 7);

insert into users(id, created_date, name, idnumber, phone_number, email, status, role)
values ('b210fd75-0230-4617-ae89-f50675a0fff8', now(), 'Istvan Farago', 'Y3472944G', '+34603233448', 'faragoistvan91@gmail.com', 'ACTIVE', 'PATIENT'),
       ('873fdcc0-0d22-497a-9791-f4dab756cbd5', now(), 'Gergely Staszkiv', 'CE586214', '+36704751213', 'staszkiv.gergely@gmail.com', 'ACTIVE', 'PATIENT');
