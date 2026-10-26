SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @targets TABLE(table_name sysname,column_name sysname);
INSERT INTO @targets VALUES
('users','full_name'),
('addresses','recipient_name'),('addresses','address_line'),('addresses','ward'),('addresses','district'),('addresses','province'),
('orders','receiver_name'),('orders','shipping_address'),('orders','note'),
('reviews','content'),('notifications','title'),('notifications','content'),('shipper_assignments','note');
DECLARE @sql nvarchar(max)=N'';
SELECT @sql=@sql+N'ALTER TABLE dbo.'+QUOTENAME(t.name)+N' ALTER COLUMN '+QUOTENAME(c.name)+N' nvarchar('
    +CASE WHEN c.max_length=-1 THEN N'max' ELSE CONVERT(nvarchar(10),c.max_length) END+N') '
    +CASE WHEN c.is_nullable=1 THEN N'NULL' ELSE N'NOT NULL' END+N';'
FROM @targets target
JOIN sys.tables t ON t.name=target.table_name AND SCHEMA_NAME(t.schema_id)='dbo'
JOIN sys.columns c ON c.object_id=t.object_id AND c.name=target.column_name
JOIN sys.types ty ON ty.user_type_id=c.user_type_id
WHERE ty.name IN ('varchar','char');
IF LEN(@sql)>0 EXEC sys.sp_executesql @sql;
COMMIT;
