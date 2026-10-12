-- SQL Server cho phép nhiều phone NULL bằng filtered unique index.
-- Không thay đổi dữ liệu hay ràng buộc email.
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
BEGIN TRANSACTION;
BEGIN TRY
DECLARE @constraint sysname, @sql nvarchar(max);
SELECT @constraint = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic ON ic.object_id=kc.parent_object_id AND ic.index_id=kc.unique_index_id
JOIN sys.columns c ON c.object_id=ic.object_id AND c.column_id=ic.column_id
WHERE kc.parent_object_id=OBJECT_ID(N'dbo.users') AND kc.type='UQ' AND c.name='phone'
AND (SELECT COUNT(*) FROM sys.index_columns x WHERE x.object_id=kc.parent_object_id AND x.index_id=kc.unique_index_id AND x.key_ordinal>0)=1;
IF @constraint IS NOT NULL
BEGIN
    SET @sql=N'ALTER TABLE dbo.users DROP CONSTRAINT '+QUOTENAME(@constraint);
    EXEC sp_executesql @sql;
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.users') AND name=N'UX_users_phone_present')
    CREATE UNIQUE INDEX UX_users_phone_present ON dbo.users(phone) WHERE phone IS NOT NULL;
COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
