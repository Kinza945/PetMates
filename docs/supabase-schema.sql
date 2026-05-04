-- PetMates dev Supabase schema.
-- Применять первым файлом перед docs/supabase-release-foundation.sql.
-- В Android service_role key не используется: клиент работает только через anon key + JWT.

begin;

create extension if not exists pgcrypto;

do $$
begin
  create type public.gender as enum ('male', 'female', 'unspecified');
exception when duplicate_object then null;
end $$;

do $$
begin
  create type public.system_role as enum ('admin', 'moderator', 'user');
exception when duplicate_object then null;
end $$;

do $$
begin
  create type public.project_status as enum ('in_progress', 'paused', 'completed');
exception when duplicate_object then null;
end $$;

do $$
begin
  create type public.response_status as enum ('pending', 'accepted', 'rejected');
exception when duplicate_object then null;
end $$;

do $$
begin
  create type public.invite_status as enum ('pending', 'accepted', 'declined', 'cancelled');
exception when duplicate_object then null;
end $$;

do $$
begin
  create type public.notification_category as enum ('response', 'invitation', 'project');
exception when duplicate_object then null;
end $$;

do $$
begin
  create type public.reference_type as enum ('response', 'invitation', 'project');
exception when duplicate_object then null;
end $$;

create table if not exists public.users (
  user_id uuid primary key references auth.users(id) on delete cascade,
  nickname text not null unique,
  avatar_url text,
  real_name text,
  age smallint check (age is null or (age >= 0 and age <= 120)),
  gender public.gender not null default 'unspecified',
  country text,
  city text,
  workplace text,
  profile_role text,
  system_role public.system_role not null default 'user',
  description text,
  hard_skills jsonb not null default '[]'::jsonb,
  soft_skills jsonb not null default '[]'::jsonb,
  contacts jsonb not null default '[]'::jsonb,
  last_online_at timestamptz,
  created_at timestamptz not null default now()
);

create table if not exists public.projects (
  project_id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references public.users(user_id) on delete cascade,
  name text not null,
  short_description text not null,
  full_description text,
  status public.project_status not null default 'in_progress',
  status_changed_at timestamptz not null default now(),
  rating_count integer not null default 0 check (rating_count >= 0),
  created_at timestamptz not null default now()
);

create table if not exists public.project_members (
  member_id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.projects(project_id) on delete cascade,
  user_id uuid not null references public.users(user_id) on delete cascade,
  role text not null,
  joined_at timestamptz not null default now(),
  unique (project_id, user_id)
);

create table if not exists public.vacancies (
  vacancy_id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.projects(project_id) on delete cascade,
  title text not null,
  role text not null,
  description text not null,
  required_tags jsonb not null default '[]'::jsonb,
  is_open boolean not null default true,
  published_at timestamptz not null default now()
);

create table if not exists public.responses (
  response_id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(user_id) on delete cascade,
  vacancy_id uuid not null references public.vacancies(vacancy_id) on delete cascade,
  status public.response_status not null default 'pending',
  created_at timestamptz not null default now()
);

create table if not exists public.project_ratings (
  rating_id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.projects(project_id) on delete cascade,
  user_id uuid not null references public.users(user_id) on delete cascade,
  score smallint not null check (score >= 1 and score <= 5),
  comment text,
  created_at timestamptz not null default now(),
  unique (project_id, user_id)
);

create table if not exists public.invites (
  invite_id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(user_id) on delete cascade,
  project_id uuid not null references public.projects(project_id) on delete cascade,
  role text not null,
  status public.invite_status not null default 'pending',
  created_at timestamptz not null default now()
);

create table if not exists public.notifications (
  notification_id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(user_id) on delete cascade,
  category public.notification_category not null,
  event_type text not null,
  reference_type public.reference_type not null,
  reference_id uuid not null,
  context_data jsonb not null default '{}'::jsonb,
  is_read boolean not null default false,
  created_at timestamptz not null default now()
);

create index if not exists idx_users_nickname on public.users(nickname);
create index if not exists idx_projects_owner_id on public.projects(owner_id);
create index if not exists idx_projects_created_at on public.projects(created_at desc);
create index if not exists idx_project_members_project_id on public.project_members(project_id);
create index if not exists idx_project_members_user_id on public.project_members(user_id);
create index if not exists idx_vacancies_project_id on public.vacancies(project_id);
create index if not exists idx_responses_vacancy_id on public.responses(vacancy_id);
create index if not exists idx_responses_user_id on public.responses(user_id);
create index if not exists idx_invites_project_id on public.invites(project_id);
create index if not exists idx_invites_user_id on public.invites(user_id);
create index if not exists idx_notifications_user_created on public.notifications(user_id, created_at desc);
create index if not exists idx_notifications_user_unread on public.notifications(user_id, is_read);
create index if not exists idx_project_ratings_project_created on public.project_ratings(project_id, created_at desc);

create unique index if not exists uq_responses_pending_once
on public.responses(user_id, vacancy_id)
where status = 'pending';

create unique index if not exists uq_invites_pending_once
on public.invites(project_id, user_id)
where status = 'pending';

create or replace function public.handle_new_auth_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  raw_nickname text;
  normalized_nickname text;
begin
  raw_nickname := coalesce(
    new.raw_user_meta_data ->> 'nickname',
    split_part(new.email, '@', 1),
    'user'
  );
  normalized_nickname := regexp_replace(lower(raw_nickname), '[^a-z0-9_]+', '_', 'g');
  if normalized_nickname = '' then
    normalized_nickname := 'user';
  end if;

  insert into public.users(user_id, nickname, created_at, last_online_at)
  values (
    new.id,
    normalized_nickname,
    now(),
    now()
  )
  on conflict (user_id) do nothing;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_auth_user();

commit;
