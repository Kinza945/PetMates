-- PetMates release foundation для Supabase/PostgreSQL.
-- Этот файл нужен backend-разработчику: Android уже вызывает эти RPC-методы
-- в real-режиме, а RLS ниже фиксирует правила доступа на стороне сервера.
-- Перед применением проверьте имена enum-типов в вашей базе.

begin;

-- 1. RLS: включаем защиту на ключевых таблицах.
alter table public.users enable row level security;
alter table public.projects enable row level security;
alter table public.project_members enable row level security;
alter table public.vacancies enable row level security;
alter table public.responses enable row level security;
alter table public.invites enable row level security;
alter table public.notifications enable row level security;

-- 2. USERS
drop policy if exists "users_read_public_profiles" on public.users;
create policy "users_read_public_profiles"
on public.users for select
using (true);

drop policy if exists "users_update_own_profile" on public.users;
create policy "users_update_own_profile"
on public.users for update
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

-- 3. PROJECTS
drop policy if exists "projects_read_all" on public.projects;
create policy "projects_read_all"
on public.projects for select
using (true);

drop policy if exists "projects_create_authorized" on public.projects;
create policy "projects_create_authorized"
on public.projects for insert
with check (auth.uid() = owner_id);

drop policy if exists "projects_update_owner" on public.projects;
create policy "projects_update_owner"
on public.projects for update
using (auth.uid() = owner_id)
with check (auth.uid() = owner_id);

drop policy if exists "projects_delete_owner" on public.projects;
create policy "projects_delete_owner"
on public.projects for delete
using (auth.uid() = owner_id);

-- 4. PROJECT MEMBERS
drop policy if exists "project_members_read_all" on public.project_members;
create policy "project_members_read_all"
on public.project_members for select
using (true);

-- 5. VACANCIES
drop policy if exists "vacancies_read_all" on public.vacancies;
create policy "vacancies_read_all"
on public.vacancies for select
using (true);

drop policy if exists "vacancies_insert_project_owner" on public.vacancies;
create policy "vacancies_insert_project_owner"
on public.vacancies for insert
with check (
  exists (
    select 1 from public.projects p
    where p.project_id = project_id and p.owner_id = auth.uid()
  )
);

drop policy if exists "vacancies_update_project_owner" on public.vacancies;
create policy "vacancies_update_project_owner"
on public.vacancies for update
using (
  exists (
    select 1 from public.projects p
    where p.project_id = vacancies.project_id and p.owner_id = auth.uid()
  )
)
with check (
  exists (
    select 1 from public.projects p
    where p.project_id = vacancies.project_id and p.owner_id = auth.uid()
  )
);

drop policy if exists "vacancies_delete_project_owner" on public.vacancies;
create policy "vacancies_delete_project_owner"
on public.vacancies for delete
using (
  exists (
    select 1 from public.projects p
    where p.project_id = vacancies.project_id and p.owner_id = auth.uid()
  )
);

-- 6. RESPONSES
drop policy if exists "responses_read_own_or_project_owner" on public.responses;
create policy "responses_read_own_or_project_owner"
on public.responses for select
using (
  user_id = auth.uid()
  or exists (
    select 1
    from public.vacancies v
    join public.projects p on p.project_id = v.project_id
    where v.vacancy_id = responses.vacancy_id and p.owner_id = auth.uid()
  )
);

drop policy if exists "responses_insert_own" on public.responses;
create policy "responses_insert_own"
on public.responses for insert
with check (user_id = auth.uid());

drop policy if exists "responses_update_own_or_project_owner" on public.responses;
create policy "responses_update_own_or_project_owner"
on public.responses for update
using (
  user_id = auth.uid()
  or exists (
    select 1
    from public.vacancies v
    join public.projects p on p.project_id = v.project_id
    where v.vacancy_id = responses.vacancy_id and p.owner_id = auth.uid()
  )
)
with check (
  user_id = auth.uid()
  or exists (
    select 1
    from public.vacancies v
    join public.projects p on p.project_id = v.project_id
    where v.vacancy_id = responses.vacancy_id and p.owner_id = auth.uid()
  )
);

-- 7. INVITES
drop policy if exists "invites_read_invited_or_project_owner" on public.invites;
create policy "invites_read_invited_or_project_owner"
on public.invites for select
using (
  user_id = auth.uid()
  or exists (
    select 1 from public.projects p
    where p.project_id = invites.project_id and p.owner_id = auth.uid()
  )
);

drop policy if exists "invites_insert_project_owner" on public.invites;
create policy "invites_insert_project_owner"
on public.invites for insert
with check (
  exists (
    select 1 from public.projects p
    where p.project_id = project_id and p.owner_id = auth.uid()
  )
);

drop policy if exists "invites_update_invited_or_project_owner" on public.invites;
create policy "invites_update_invited_or_project_owner"
on public.invites for update
using (
  user_id = auth.uid()
  or exists (
    select 1 from public.projects p
    where p.project_id = invites.project_id and p.owner_id = auth.uid()
  )
)
with check (
  user_id = auth.uid()
  or exists (
    select 1 from public.projects p
    where p.project_id = invites.project_id and p.owner_id = auth.uid()
  )
);

-- 8. NOTIFICATIONS
drop policy if exists "notifications_own_read" on public.notifications;
create policy "notifications_own_read"
on public.notifications for select
using (user_id = auth.uid());

drop policy if exists "notifications_own_update" on public.notifications;
create policy "notifications_own_update"
on public.notifications for update
using (user_id = auth.uid())
with check (user_id = auth.uid());

drop policy if exists "notifications_own_delete" on public.notifications;
create policy "notifications_own_delete"
on public.notifications for delete
using (user_id = auth.uid());

-- 9. PROJECT RATINGS: атомарная защита от повторной оценки.
create table if not exists public.project_ratings (
  rating_id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.projects(project_id) on delete cascade,
  user_id uuid not null references public.users(user_id) on delete cascade,
  score smallint not null check (score >= 1 and score <= 5),
  comment text,
  created_at timestamptz not null default now(),
  unique (project_id, user_id)
);

alter table public.project_ratings enable row level security;

drop policy if exists "project_ratings_read_all" on public.project_ratings;
create policy "project_ratings_read_all"
on public.project_ratings for select
using (true);

drop policy if exists "project_ratings_insert_own" on public.project_ratings;
create policy "project_ratings_insert_own"
on public.project_ratings for insert
with check (user_id = auth.uid());

-- 10. RPC: обновление профиля текущего пользователя.
create or replace function public.update_my_profile(
  real_name text default null,
  age smallint default null,
  gender text default 'unspecified',
  country text default null,
  city text default null,
  workplace text default null,
  profile_role text default null,
  description text default null,
  hard_skills jsonb default '[]'::jsonb,
  soft_skills jsonb default '[]'::jsonb,
  contacts jsonb default '[]'::jsonb
)
returns public.users
language plpgsql
security invoker
as $$
declare
  updated_user public.users;
begin
  update public.users
  set
    real_name = update_my_profile.real_name,
    age = update_my_profile.age,
    gender = update_my_profile.gender::gender,
    country = update_my_profile.country,
    city = update_my_profile.city,
    workplace = update_my_profile.workplace,
    profile_role = update_my_profile.profile_role,
    description = update_my_profile.description,
    hard_skills = update_my_profile.hard_skills,
    soft_skills = update_my_profile.soft_skills,
    contacts = update_my_profile.contacts
  where user_id = auth.uid()
  returning * into updated_user;

  if updated_user.user_id is null then
    raise exception 'Profile not found for current user';
  end if;

  return updated_user;
end;
$$;

-- 11. RPC: отклики.
create or replace function public.respond_to_vacancy(p_vacancy_id uuid)
returns public.responses
language plpgsql
security definer
set search_path = public
as $$
declare
  current_user_id uuid := auth.uid();
  project_owner_id uuid;
  project_id_value uuid;
  created_response public.responses;
begin
  if current_user_id is null then
    raise exception 'Unauthorized';
  end if;

  select p.owner_id, p.project_id into project_owner_id, project_id_value
  from public.vacancies v
  join public.projects p on p.project_id = v.project_id
  where v.vacancy_id = p_vacancy_id and v.is_open = true;

  if project_owner_id is null then
    raise exception 'Vacancy not found or closed';
  end if;
  if project_owner_id = current_user_id then
    raise exception 'Project owner cannot respond to own vacancy';
  end if;
  if exists (
    select 1 from public.project_members pm
    where pm.project_id = project_id_value and pm.user_id = current_user_id
  ) then
    raise exception 'Project member cannot respond to project vacancy';
  end if;
  if exists (
    select 1 from public.responses r
    where r.vacancy_id = p_vacancy_id and r.user_id = current_user_id and r.status = 'pending'
  ) then
    raise exception 'Pending response already exists';
  end if;

  insert into public.responses(response_id, user_id, vacancy_id, status, created_at)
  values (gen_random_uuid(), current_user_id, p_vacancy_id, 'pending', now())
  returning * into created_response;

  insert into public.notifications(notification_id, user_id, category, event_type, reference_type, reference_id, context_data, is_read, created_at)
  values (gen_random_uuid(), project_owner_id, 'response', 'response.created', 'response', created_response.response_id, jsonb_build_object('vacancy_id', p_vacancy_id), false, now());

  return created_response;
end;
$$;

create or replace function public.update_response_status(p_response_id uuid, p_status text)
returns public.responses
language plpgsql
security definer
set search_path = public
as $$
declare
  updated_response public.responses;
begin
  update public.responses r
  set status = p_status::response_status
  from public.vacancies v
  join public.projects p on p.project_id = v.project_id
  where r.response_id = p_response_id
    and r.vacancy_id = v.vacancy_id
    and p.owner_id = auth.uid()
    and p_status in ('accepted', 'rejected')
  returning r.* into updated_response;

  if updated_response.response_id is null then
    raise exception 'Response not found or forbidden';
  end if;

  insert into public.notifications(notification_id, user_id, category, event_type, reference_type, reference_id, context_data, is_read, created_at)
  values (gen_random_uuid(), updated_response.user_id, 'response', 'response.status_changed', 'response', updated_response.response_id, jsonb_build_object('status', p_status), false, now());

  return updated_response;
end;
$$;

create or replace function public.cancel_response(p_response_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  delete from public.responses
  where response_id = p_response_id and user_id = auth.uid() and status = 'pending';

  if not found then
    raise exception 'Response not found or cannot be cancelled';
  end if;
end;
$$;

-- 12. RPC: приглашения.
create or replace function public.invite_user(p_project_id uuid, p_user_id uuid, p_role text, p_message text default null)
returns public.invites
language plpgsql
security definer
set search_path = public
as $$
declare
  created_invite public.invites;
begin
  if not exists (select 1 from public.projects where project_id = p_project_id and owner_id = auth.uid()) then
    raise exception 'Only project owner can invite users';
  end if;
  if exists (select 1 from public.invites where project_id = p_project_id and user_id = p_user_id and status = 'pending') then
    raise exception 'Pending invite already exists';
  end if;

  insert into public.invites(invite_id, user_id, project_id, role, status, created_at)
  values (gen_random_uuid(), p_user_id, p_project_id, p_role, 'pending', now())
  returning * into created_invite;

  insert into public.notifications(notification_id, user_id, category, event_type, reference_type, reference_id, context_data, is_read, created_at)
  values (gen_random_uuid(), p_user_id, 'invitation', 'invite.created', 'invitation', created_invite.invite_id, jsonb_build_object('project_id', p_project_id, 'message', p_message), false, now());

  return created_invite;
end;
$$;

create or replace function public.update_invite_status(p_invite_id uuid, p_status text)
returns public.invites
language plpgsql
security definer
set search_path = public
as $$
declare
  updated_invite public.invites;
begin
  update public.invites
  set status = p_status::invite_status
  where invite_id = p_invite_id
    and user_id = auth.uid()
    and p_status in ('accepted', 'declined')
  returning * into updated_invite;

  if updated_invite.invite_id is null then
    raise exception 'Invite not found or forbidden';
  end if;

  if p_status = 'accepted' then
    insert into public.project_members(member_id, project_id, user_id, role, joined_at)
    values (gen_random_uuid(), updated_invite.project_id, updated_invite.user_id, updated_invite.role, now())
    on conflict do nothing;
  end if;

  insert into public.notifications(notification_id, user_id, category, event_type, reference_type, reference_id, context_data, is_read, created_at)
  select gen_random_uuid(), p.owner_id, 'invitation', 'invite.status_changed', 'invitation', updated_invite.invite_id, jsonb_build_object('status', p_status), false, now()
  from public.projects p
  where p.project_id = updated_invite.project_id;

  return updated_invite;
end;
$$;

create or replace function public.cancel_invite(p_invite_id uuid)
returns public.invites
language plpgsql
security definer
set search_path = public
as $$
declare
  updated_invite public.invites;
begin
  update public.invites i
  set status = 'cancelled'
  from public.projects p
  where i.invite_id = p_invite_id
    and i.project_id = p.project_id
    and p.owner_id = auth.uid()
    and i.status = 'pending'
  returning i.* into updated_invite;

  if updated_invite.invite_id is null then
    raise exception 'Invite not found or cannot be cancelled';
  end if;

  insert into public.notifications(notification_id, user_id, category, event_type, reference_type, reference_id, context_data, is_read, created_at)
  values (gen_random_uuid(), updated_invite.user_id, 'invitation', 'invite.cancelled', 'invitation', updated_invite.invite_id, jsonb_build_object('project_id', updated_invite.project_id), false, now());

  return updated_invite;
end;
$$;

-- 13. RPC: оценка проекта.
create or replace function public.rate_project(p_project_id uuid, p_score smallint default 5, p_comment text default null)
returns public.projects
language plpgsql
security definer
set search_path = public
as $$
declare
  updated_project public.projects;
begin
  if exists (select 1 from public.projects where project_id = p_project_id and owner_id = auth.uid()) then
    raise exception 'Project owner cannot rate own project';
  end if;
  if p_score < 1 or p_score > 5 then
    raise exception 'Rating score must be from 1 to 5';
  end if;

  insert into public.project_ratings(project_id, user_id, score, comment)
  values (p_project_id, auth.uid(), p_score, nullif(trim(p_comment), ''));

  update public.projects
  set rating_count = rating_count + 1
  where project_id = p_project_id
  returning * into updated_project;

  insert into public.notifications(notification_id, user_id, category, event_type, reference_type, reference_id, context_data, is_read, created_at)
  values (gen_random_uuid(), updated_project.owner_id, 'project', 'project.rated', 'project', updated_project.project_id, '{}'::jsonb, false, now());

  return updated_project;
end;
$$;

-- 14. RPC: удаления.
create or replace function public.delete_vacancy(p_vacancy_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  delete from public.vacancies v
  using public.projects p
  where v.vacancy_id = p_vacancy_id
    and v.project_id = p.project_id
    and p.owner_id = auth.uid();

  if not found then
    raise exception 'Vacancy not found or forbidden';
  end if;
end;
$$;

create or replace function public.delete_project(p_project_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  delete from public.projects
  where project_id = p_project_id and owner_id = auth.uid();

  if not found then
    raise exception 'Project not found or forbidden';
  end if;
end;
$$;

create or replace function public.delete_my_account()
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  delete from public.users where user_id = auth.uid();
  if not found then
    raise exception 'User not found';
  end if;
end;
$$;

commit;
