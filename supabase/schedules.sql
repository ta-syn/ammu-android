-- Supabase pg_cron schedules for background tasks
-- This script should be run in the Supabase SQL Editor
--
-- IMPORTANT WARNING:
-- The HTTP POST calls below contain hardcoded project URLs (https://zbzqyftcmntzskzfyctd.supabase.co)
-- and a hardcoded Anon Key (Authorization: Bearer eyJhbGci...).
-- If you are deploying this project on a new/different Supabase instance, you MUST replace:
-- 1. 'zbzqyftcmntzskzfyctd' with your new Supabase Project ID
-- 2. The Anon Key in the Authorization headers with your new project's Anon Key.
-- Otherwise, pg_cron will trigger Edge Functions on the old/wrong project.


-- Enable pg_cron extension
CREATE EXTENSION IF NOT EXISTS pg_cron;

-- Unschedule existing jobs to prevent conflicts and ensure a fresh registration
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM cron.job WHERE jobname = 'prayer-notifications') THEN
        PERFORM cron.unschedule('prayer-notifications');
    END IF;
    IF EXISTS (SELECT 1 FROM cron.job WHERE jobname = 'daily-content') THEN
        PERFORM cron.unschedule('daily-content');
    END IF;
    IF EXISTS (SELECT 1 FROM cron.job WHERE jobname = 'medicine-reminders') THEN
        PERFORM cron.unschedule('medicine-reminders');
    END IF;
    IF EXISTS (SELECT 1 FROM cron.job WHERE jobname = 'wellness-check') THEN
        PERFORM cron.unschedule('wellness-check');
    END IF;
    IF EXISTS (SELECT 1 FROM cron.job WHERE jobname = 'weekly-report') THEN
        PERFORM cron.unschedule('weekly-report');
    END IF;
END $$;


-- 1. Prayer Notifications (every 5 minutes)
SELECT cron.schedule(
    'prayer-notifications',
    '*/5 * * * *',
    $$
    SELECT net.http_post(
        url:='https://zbzqyftcmntzskzfyctd.supabase.co/functions/v1/prayer-notifications',
        headers:='{"Content-Type": "application/json", "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpienF5ZnRjbW50enNremZ5Y3RkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk3ODMzNDgsImV4cCI6MjA5NTM1OTM0OH0.bKV-PHepNBE2NNyq9AiNjtKQoUbYT2pNpbt2dT0sG8g"}'::jsonb,
        body:='{}'::jsonb
    ) as request_id;
    $$
);

-- 2. Daily Content Generation (daily at 00:00 UTC / 6:00 AM BST)
SELECT cron.schedule(
    'daily-content',
    '0 0 * * *',
    $$
    SELECT net.http_post(
        url:='https://zbzqyftcmntzskzfyctd.supabase.co/functions/v1/daily-content',
        headers:='{"Content-Type": "application/json", "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpienF5ZnRjbW50enNremZ5Y3RkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk3ODMzNDgsImV4cCI6MjA5NTM1OTM0OH0.bKV-PHepNBE2NNyq9AiNjtKQoUbYT2pNpbt2dT0sG8g"}'::jsonb,
        body:='{}'::jsonb
    ) as request_id;
    $$
);

-- 3. Medicine Reminders (every minute)
SELECT cron.schedule(
    'medicine-reminders',
    '* * * * *',
    $$
    SELECT net.http_post(
        url:='https://zbzqyftcmntzskzfyctd.supabase.co/functions/v1/medicine-reminders',
        headers:='{"Content-Type": "application/json", "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpienF5ZnRjbW50enNremZ5Y3RkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk3ODMzNDgsImV4cCI6MjA5NTM1OTM0OH0.bKV-PHepNBE2NNyq9AiNjtKQoUbYT2pNpbt2dT0sG8g"}'::jsonb,
        body:='{}'::jsonb
    ) as request_id;
    $$
);

-- 4. Wellness Check (daily at 4:00 UTC / 10:00 AM BST)
SELECT cron.schedule(
    'wellness-check',
    '0 4 * * *',
    $$
    SELECT net.http_post(
        url:='https://zbzqyftcmntzskzfyctd.supabase.co/functions/v1/wellness-check',
        headers:='{"Content-Type": "application/json", "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpienF5ZnRjbW50enNremZ5Y3RkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk3ODMzNDgsImV4cCI6MjA5NTM1OTM0OH0.bKV-PHepNBE2NNyq9AiNjtKQoUbYT2pNpbt2dT0sG8g"}'::jsonb,
        body:='{}'::jsonb
    ) as request_id;
    $$
);

-- 5. Weekly Report (Sundays at 3:00 UTC / 9:00 AM BST)
SELECT cron.schedule(
    'weekly-report',
    '0 3 * * 0',
    $$
    SELECT net.http_post(
        url:='https://zbzqyftcmntzskzfyctd.supabase.co/functions/v1/weekly-report',
        headers:='{"Content-Type": "application/json", "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpienF5ZnRjbW50enNremZ5Y3RkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk3ODMzNDgsImV4cCI6MjA5NTM1OTM0OH0.bKV-PHepNBE2NNyq9AiNjtKQoUbYT2pNpbt2dT0sG8g"}'::jsonb,
        body:='{}'::jsonb
    ) as request_id;
    $$
);
