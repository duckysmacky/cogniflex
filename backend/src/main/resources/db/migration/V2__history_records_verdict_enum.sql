alter table history_records
    drop constraint if exists history_records_kind_check;

alter table history_records
    rename column kind to verdict;

create type analysis_verdict as enum ('human', 'ai');

alter table history_records
    alter column verdict drop default,
    alter column verdict type analysis_verdict
    using case verdict
        when 0 then 'human'::analysis_verdict
        when 1 then 'ai'::analysis_verdict
        else null
    end;