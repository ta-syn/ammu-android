-- Supabase pg_cron schedules for background tasks
-- This script should be run in the Supabase SQL Editor

-- Enable pg_cron extension
CREATE EXTENSION IF NOT EXISTS pg_cron;

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
