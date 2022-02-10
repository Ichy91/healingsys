insert into day_of_week(day)
values ('MONDAY'),
        ('TUESDAY'),
        ('WEDNESDAY'),
        ('THURSDAY'),
        ('FRIDAY'),
        ('SATURDAY'),
        ('SUNDAY');

insert into operation_details(open, closed, max_generated_days, slot_length_in_hour, slot_max_capacity)
values ('08:00', '17:00', 10, 1.0, 3);
