-- Database accounts & role-based access control for BookVault
--   db_admin       – database owner (all privileges on schema)
--   app_identity   – application backend account (READ + WRITE + EXECUTE)
--   dev_1..dev_4   – developer accounts (READ only)
--
-- Role created:
--   db_procexecutor – EXECUTE on all functions/procedures; assigned to app_identity

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'db_procexecutor') THEN
        CREATE ROLE db_procexecutor;
    END IF;
END $$;

GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA biblioteka TO db_procexecutor;
GRANT EXECUTE ON ALL PROCEDURES IN SCHEMA biblioteka TO db_procexecutor;
-- Also applies to future functions/procedures
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT EXECUTE ON FUNCTIONS TO db_procexecutor;


DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'db_admin') THEN
        CREATE USER db_admin WITH PASSWORD 'DbAdmin$ecure#2025';
    END IF;
END $$;

-- Owner-level rights on schema
GRANT ALL PRIVILEGES ON SCHEMA biblioteka TO db_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA biblioteka TO db_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA biblioteka TO db_admin;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA biblioteka TO db_admin;
GRANT ALL PRIVILEGES ON ALL PROCEDURES IN SCHEMA biblioteka TO db_admin;

-- Future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT ALL PRIVILEGES ON TABLES TO db_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT ALL PRIVILEGES ON SEQUENCES TO db_admin;


DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'app_BookVault') THEN
        CREATE USER "app_BookVault" WITH PASSWORD 'App1dent1ty#Pass!';
    END IF;
END $$;

GRANT USAGE ON SCHEMA biblioteka TO "app_BookVault";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA biblioteka TO "app_BookVault";
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA biblioteka TO "app_BookVault";
GRANT db_procexecutor TO "app_BookVault";   -- EXECUTE on functions/procedures

-- Future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO "app_BookVault";
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT USAGE, SELECT ON SEQUENCES TO "app_BookVault";

-- search_path so no schema prefix needed
ALTER ROLE "app_BookVault" SET search_path TO biblioteka, public;


DO $$
DECLARE
    dev_names TEXT[] := ARRAY['Cl1ngz-dev', 'Gittokurestwo-dev', 'Bibonluz-dev', 'bakerinho-dev'];
    dev_name  TEXT;
BEGIN
    FOREACH dev_name IN ARRAY dev_names LOOP
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = dev_name) THEN
            EXECUTE format(
                'CREATE USER %I WITH PASSWORD %L',
                dev_name,
                'zaq1@WSX'
            );
        END IF;
        EXECUTE format('GRANT USAGE ON SCHEMA biblioteka TO %I', dev_name);
        EXECUTE format('GRANT SELECT ON ALL TABLES IN SCHEMA biblioteka TO %I', dev_name);
        EXECUTE format('GRANT SELECT ON ALL SEQUENCES IN SCHEMA biblioteka TO %I', dev_name);
        EXECUTE format('ALTER ROLE %I SET search_path TO biblioteka, public', dev_name);
    END LOOP;
END $$;

-- Future tables – SELECT only for developers
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT SELECT ON TABLES TO "Cl1ngz-dev";
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT SELECT ON TABLES TO "Gittokurestwo-dev";
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT SELECT ON TABLES TO "Bibonluz-dev";
ALTER DEFAULT PRIVILEGES IN SCHEMA biblioteka
    GRANT SELECT ON TABLES TO "bakerinho-dev";


