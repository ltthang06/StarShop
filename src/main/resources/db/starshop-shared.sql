USE [master];

GO

IF DB_ID(N'StarShopShared') IS NULL CREATE DATABASE [StarShopShared];

GO

USE [StarShopShared];

GO

SET NOCOUNT ON;

SET XACT_ABORT ON;

SET ANSI_NULLS ON;

SET QUOTED_IDENTIFIER ON;

SET ANSI_PADDING ON;

SET ANSI_WARNINGS ON;

SET CONCAT_NULL_YIELDS_NULL ON;

SET ARITHABORT ON;

SET NUMERIC_ROUNDABORT OFF;

IF EXISTS (SELECT 1 FROM sys.tables WHERE is_ms_shipped=0) THROW 51000, N'Database đã có bảng. Hãy dùng database trống để giữ dữ liệu đang có.', 1;

BEGIN TRY

BEGIN TRANSACTION;

CREATE TABLE [dbo].[addresses] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [address_line] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [default_address] bit NOT NULL,
    [district] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [phone] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [province] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [recipient_name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [ward] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [user_id] bigint NOT NULL
);

CREATE TABLE [dbo].[cart_items] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [quantity] int NOT NULL,
    [cart_id] bigint NOT NULL,
    [product_id] bigint NOT NULL
);

CREATE TABLE [dbo].[carts] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [user_id] bigint NOT NULL
);

CREATE TABLE [dbo].[categories] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [description] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [image] varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
);

CREATE TABLE [dbo].[commissions] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [end_at] datetime2(6) NULL,
    [rate_percent] numeric(5,2) NOT NULL,
    [start_at] datetime2(6) NOT NULL,
    [shop_id] bigint NOT NULL
);

CREATE TABLE [dbo].[notifications] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [content] nvarchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [created_at] datetime2(6) NOT NULL,
    [read_status] bit NOT NULL,
    [target_url] varchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [title] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [user_id] bigint NOT NULL
);

CREATE TABLE [dbo].[order_details] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [quantity] int NOT NULL,
    [subtotal] numeric(15,2) NOT NULL,
    [unit_price] numeric(15,2) NOT NULL,
    [order_id] bigint NOT NULL,
    [product_id] bigint NOT NULL
);

CREATE TABLE [dbo].[orders] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [created_at] datetime2(6) NOT NULL,
    [discount_amount] numeric(15,2) NOT NULL,
    [note] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [payment_method] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [payment_status] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [receiver_name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [receiver_phone] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [shipping_address] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [shipping_fee] numeric(15,2) NOT NULL,
    [status] varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [subtotal] numeric(15,2) NOT NULL,
    [total_amount] numeric(15,2) NOT NULL,
    [shipping_provider_id] bigint NULL,
    [shop_id] bigint NOT NULL,
    [user_id] bigint NOT NULL
);

CREATE TABLE [dbo].[otp_tokens] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [code] varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [created_at] datetime2(6) NOT NULL,
    [expires_at] datetime2(6) NOT NULL,
    [used] bit NOT NULL,
    [user_id] bigint NOT NULL,
    [failed_attempts] int NOT NULL CONSTRAINT [DF__otp_token__faile__3A4CA8FD] DEFAULT ((0))
);

CREATE TABLE [dbo].[product_images] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [image_url] varchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [primary_image] bit NOT NULL,
    [public_id] varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [product_id] bigint NOT NULL
);

CREATE TABLE [dbo].[product_moderations] (
    [product_id] bigint NOT NULL,
    [blocked] bit NOT NULL,
    [moderator_email] varchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [reason] varchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [updated_at] datetime2(6) NOT NULL
);

CREATE TABLE [dbo].[product_views] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [viewed_at] datetime2(6) NOT NULL,
    [product_id] bigint NOT NULL,
    [user_id] bigint NOT NULL
);

CREATE TABLE [dbo].[products] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [created_at] datetime2(6) NOT NULL,
    [description] nvarchar(2000) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [discount_price] numeric(15,2) NULL,
    [name] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [price] numeric(15,2) NOT NULL,
    [quantity] int NOT NULL,
    [rating] float(53) NOT NULL,
    [sold_count] int NOT NULL,
    [status] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [category_id] bigint NOT NULL,
    [shop_id] bigint NOT NULL
);

CREATE TABLE [dbo].[promotions] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [code] varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [description] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [discount_value] numeric(15,2) NOT NULL,
    [end_at] datetime2(6) NOT NULL,
    [max_discount_amount] numeric(15,2) NULL,
    [min_order_amount] numeric(15,2) NULL,
    [name] nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [quantity] int NOT NULL,
    [scope] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [start_at] datetime2(6) NOT NULL,
    [type] varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [shop_id] bigint NULL
);

CREATE TABLE [dbo].[reviews] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [content] nvarchar(2000) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [created_at] datetime2(6) NOT NULL,
    [image_url] varchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [rating] int NOT NULL,
    [video_url] varchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [order_detail_id] bigint NOT NULL,
    [product_id] bigint NOT NULL,
    [user_id] bigint NOT NULL
);

CREATE TABLE [dbo].[roles] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [name] varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
);

CREATE TABLE [dbo].[shipper_assignments] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [assigned_at] datetime2(6) NOT NULL,
    [delivered_at] datetime2(6) NULL,
    [note] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [picked_up_at] datetime2(6) NULL,
    [order_id] bigint NOT NULL,
    [shipper_id] bigint NOT NULL
);

CREATE TABLE [dbo].[shipping_providers] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [base_fee] numeric(15,2) NOT NULL,
    [estimated_days] int NULL,
    [name] varchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [phone] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
);

CREATE TABLE [dbo].[shops] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [address] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [banner] varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [created_at] datetime2(6) NOT NULL,
    [description] nvarchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [email] varchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [logo] varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [name] nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [phone] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [status] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [owner_id] bigint NOT NULL,
    [manager_id] bigint NULL
);

CREATE TABLE [dbo].[user_roles] (
    [user_id] bigint NOT NULL,
    [role_id] bigint NOT NULL
);

CREATE TABLE [dbo].[users] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [avatar] varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [created_at] datetime2(6) NOT NULL,
    [email] varchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [full_name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [password] varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [phone] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [status] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
);

CREATE TABLE [dbo].[vnpay_payment_orders] (
    [payment_reference] varchar(40) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [order_id] bigint NOT NULL
);

CREATE TABLE [dbo].[vnpay_payments] (
    [reference] varchar(40) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [amount] numeric(15,2) NOT NULL,
    [client_ip] varchar(45) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [created_at] datetime2(6) NOT NULL,
    [expires_at] datetime2(6) NOT NULL,
    [gateway_transaction_no] varchar(80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [resolved_at] datetime2(6) NULL,
    [response_code] varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [state] varchar(25) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [transaction_status] varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [user_id] bigint NOT NULL,
    [gateway_started_at] datetime2(6) NULL,
    [last_checked_at] datetime2(6) NULL,
    [merchant_code] varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [reconciliation_note] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [return_received_at] datetime2(6) NULL,
    [return_response_code] varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [return_transaction_status] varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
);

CREATE TABLE [dbo].[wishlists] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [created_at] datetime2(6) NOT NULL,
    [product_id] bigint NOT NULL,
    [user_id] bigint NOT NULL
);

SET IDENTITY_INSERT [dbo].[addresses] ON;

INSERT INTO [dbo].[addresses] ([id],[address_line],[default_address],[district],[phone],[province],[recipient_name],[ward],[user_id]) VALUES (1,N'12 Đường Hoa',1,N'TP. Thủ Đức',N'0901234001',N'TP. Hồ Chí Minh',N'Nguyễn Khánh An',NULL,7);

INSERT INTO [dbo].[addresses] ([id],[address_line],[default_address],[district],[phone],[province],[recipient_name],[ward],[user_id]) VALUES (2,N'25 Đường Số 3',0,N'Bình Thạnh',N'0901234002',N'TP. Hồ Chí Minh',N'Nguyễn Khánh An',NULL,7);

INSERT INTO [dbo].[addresses] ([id],[address_line],[default_address],[district],[phone],[province],[recipient_name],[ward],[user_id]) VALUES (3,N'số 1 võ văn ngân',1,N'thủ đức',N'0998877665',N'hcm',N'Lê Duệ Thắng',N'linh trung',1);

SET IDENTITY_INSERT [dbo].[addresses] OFF;

DBCC CHECKIDENT (N'[dbo].[addresses]', RESEED, 3) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[carts] ON;

INSERT INTO [dbo].[carts] ([id],[user_id]) VALUES (1,7);

INSERT INTO [dbo].[carts] ([id],[user_id]) VALUES (2,1);

SET IDENTITY_INSERT [dbo].[carts] OFF;

DBCC CHECKIDENT (N'[dbo].[carts]', RESEED, 2) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[categories] ON;

INSERT INTO [dbo].[categories] ([id],[active],[description],[image],[name]) VALUES (1,1,NULL,NULL,N'Bó hoa sinh nhật');

INSERT INTO [dbo].[categories] ([id],[active],[description],[image],[name]) VALUES (2,1,NULL,NULL,N'Hộp hoa mica');

INSERT INTO [dbo].[categories] ([id],[active],[description],[image],[name]) VALUES (3,1,NULL,NULL,N'Lẵng hoa khai trương');

INSERT INTO [dbo].[categories] ([id],[active],[description],[image],[name]) VALUES (4,1,NULL,NULL,N'Giỏ hoa chúc mừng');

INSERT INTO [dbo].[categories] ([id],[active],[description],[image],[name]) VALUES (5,1,NULL,NULL,N'Kệ hoa');

SET IDENTITY_INSERT [dbo].[categories] OFF;

DBCC CHECKIDENT (N'[dbo].[categories]', RESEED, 5) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[commissions] ON;

INSERT INTO [dbo].[commissions] ([id],[active],[end_at],[rate_percent],[start_at],[shop_id]) VALUES (1,1,NULL,5.00,N'2026-10-23T23:29:55.4101180',1);

INSERT INTO [dbo].[commissions] ([id],[active],[end_at],[rate_percent],[start_at],[shop_id]) VALUES (2,1,NULL,5.00,N'2026-10-23T23:29:55.4337120',2);

INSERT INTO [dbo].[commissions] ([id],[active],[end_at],[rate_percent],[start_at],[shop_id]) VALUES (3,1,NULL,5.00,N'2026-10-23T23:29:55.4484650',3);

SET IDENTITY_INSERT [dbo].[commissions] OFF;

DBCC CHECKIDENT (N'[dbo].[commissions]', RESEED, 3) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[notifications] ON;

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (1,N'Đơn #1 · NEW',N'2026-10-23T23:29:55.8374720',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (10,N'Đơn #3 · CONFIRMED',N'2026-10-23T23:29:56.5942580',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (100,N'Đơn #10 · RETURNED',N'2026-10-23T23:30:00.1216160',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (101,N'Đơn #11 · NEW',N'2026-10-23T23:30:00.1801740',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (102,N'Đơn #11 · NEW',N'2026-10-23T23:30:00.1812000',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (103,N'Đơn #11 · CONFIRMED',N'2026-10-23T23:30:00.1921740',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (104,N'Đơn #11 · CONFIRMED',N'2026-10-23T23:30:00.1941760',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (105,N'Đơn #11 · READY_FOR_PICKUP',N'2026-10-23T23:30:00.2021740',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (106,N'Đơn #11 · READY_FOR_PICKUP',N'2026-10-23T23:30:00.2031730',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (107,N'Đơn #11 · ASSIGNED',N'2026-10-23T23:30:00.2174790',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (108,N'Đơn #11 · ASSIGNED',N'2026-10-23T23:30:00.2194830',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (109,N'Đơn #11',N'2026-10-23T23:30:00.2214770',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (11,N'Đơn #3 · READY_FOR_PICKUP',N'2026-10-23T23:29:56.6429680',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (110,N'Đơn #11 · PICKED_UP',N'2026-10-23T23:30:00.2346200',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (111,N'Đơn #11 · PICKED_UP',N'2026-10-23T23:30:00.2366400',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (112,N'Đơn #11 · SHIPPING',N'2026-10-23T23:30:00.2466320',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (113,N'Đơn #11 · SHIPPING',N'2026-10-23T23:30:00.2496190',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (114,N'Đơn #11 · DELIVERED',N'2026-10-23T23:30:00.2610940',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (115,N'Đơn #11 · DELIVERED',N'2026-10-23T23:30:00.2630860',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (116,N'Đơn #11 · RETURN_REQUESTED',N'2026-10-23T23:30:00.2754200',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (117,N'Đơn #11 · RETURN_REQUESTED',N'2026-10-23T23:30:00.2764200',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (118,N'Đơn #11 · RETURNED',N'2026-10-23T23:30:00.2887760',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (119,N'Đơn #11 · RETURNED',N'2026-10-23T23:30:00.2907730',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (12,N'Đơn #3 · READY_FOR_PICKUP',N'2026-10-23T23:29:56.6591790',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (120,N'Đơn #11 · REFUNDED',N'2026-10-23T23:30:00.3007810',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (121,N'Đơn #11 · REFUNDED',N'2026-10-23T23:30:00.3027800',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (122,N'Đơn #12 · NEW',N'2026-10-23T23:30:00.3919520',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (123,N'Đơn #12 · NEW',N'2026-10-23T23:30:00.3929610',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (124,N'Đơn #13 · NEW',N'2026-10-23T23:30:00.3959500',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (125,N'Đơn #13 · NEW',N'2026-10-23T23:30:00.3969490',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (126,N'Đơn #12 · CONFIRMED',N'2026-10-23T23:30:00.4124050',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (127,N'Đơn #12 · CONFIRMED',N'2026-10-23T23:30:00.4144050',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (128,N'Đơn #12 · READY_FOR_PICKUP',N'2026-10-23T23:30:00.4194050',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (129,N'Đơn #12 · READY_FOR_PICKUP',N'2026-10-23T23:30:00.4214060',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (13,N'Đơn #4 · NEW',N'2026-10-23T23:29:56.9443080',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (130,N'Đơn #12 · ASSIGNED',N'2026-10-23T23:30:00.4334460',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (131,N'Đơn #12 · ASSIGNED',N'2026-10-23T23:30:00.4344080',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (132,N'Đơn #12',N'2026-10-23T23:30:00.4354100',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (133,N'Đơn #12 · PICKED_UP',N'2026-10-23T23:30:00.4434090',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (134,N'Đơn #12 · PICKED_UP',N'2026-10-23T23:30:00.4454090',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (135,N'Đơn #12 · SHIPPING',N'2026-10-23T23:30:00.4524080',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (136,N'Đơn #12 · SHIPPING',N'2026-10-23T23:30:00.4544160',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (137,N'Đơn #12 · DELIVERED',N'2026-10-23T23:30:00.4614100',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (138,N'Đơn #12 · DELIVERED',N'2026-10-23T23:30:00.4634130',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (139,N'Đơn #13 · CONFIRMED',N'2026-10-23T23:30:00.4724090',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (14,N'Đơn #4 · NEW',N'2026-10-23T23:29:56.9535430',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (140,N'Đơn #13 · CONFIRMED',N'2026-10-23T23:30:00.4744070',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (141,N'Đơn #13 · READY_FOR_PICKUP',N'2026-10-23T23:30:00.4806590',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (142,N'Đơn #13 · READY_FOR_PICKUP',N'2026-10-23T23:30:00.4816610',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (143,N'Đơn #13 · ASSIGNED',N'2026-10-23T23:30:00.4921030',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (144,N'Đơn #13 · ASSIGNED',N'2026-10-23T23:30:00.4941040',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (145,N'Đơn #13',N'2026-10-23T23:30:00.4961020',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (146,N'Đơn #13 · PICKED_UP',N'2026-10-23T23:30:00.5051030',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (147,N'Đơn #13 · PICKED_UP',N'2026-10-23T23:30:00.5071040',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (148,N'Đơn #13 · SHIPPING',N'2026-10-23T23:30:00.5175860',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (149,N'Đơn #13 · SHIPPING',N'2026-10-23T23:30:00.5195950',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (15,N'Đơn #4 · CONFIRMED',N'2026-10-23T23:29:57.0316860',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (150,N'Đơn #13 · DELIVERED',N'2026-10-23T23:30:00.5277190',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (151,N'Đơn #13 · DELIVERED',N'2026-10-23T23:30:00.5307170',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (152,N'Đơn #13 · RETURN_REQUESTED',N'2026-10-23T23:30:00.5388810',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (153,N'Đơn #13 · RETURN_REQUESTED',N'2026-10-23T23:30:00.5408810',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (154,N'Đơn #13 · RETURNED',N'2026-10-23T23:30:00.5488810',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (155,N'Đơn #13 · RETURNED',N'2026-10-23T23:30:00.5508790',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (156,N'Đơn #13 · REFUNDED',N'2026-10-23T23:30:00.5568790',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (157,N'Đơn #13 · REFUNDED',N'2026-10-23T23:30:00.5578790',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (158,N'Đơn #14 · NEW',N'2026-10-24T13:11:55.4766440',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (159,N'Đơn #14 · NEW',N'2026-10-24T13:11:55.4881630',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (16,N'Đơn #4 · CONFIRMED',N'2026-10-23T23:29:57.0406700',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (160,N'Đơn #15 · NEW',N'2026-10-24T13:20:44.1781950',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (161,N'Đơn #15 · NEW',N'2026-10-24T13:20:44.1961390',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (162,N'Đơn #16 · NEW',N'2026-10-26T14:44:08.2230460',0,N'/orders',N'Cập nhật đơn hàng',1);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (163,N'Đơn #16 · NEW',N'2026-10-26T14:44:08.2350420',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (164,N'Đơn #17 · NEW',N'2026-10-26T14:54:49.4200620',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (165,N'Đơn #17 · NEW',N'2026-10-26T14:54:49.4277040',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (166,N'Đơn #17 · NEW',N'2026-10-26T14:58:19.3267970',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (167,N'Đơn #17 · NEW',N'2026-10-26T14:58:19.3379060',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (168,N'Đơn #17 · CONFIRMED',N'2026-10-26T14:59:44.8358970',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (169,N'Đơn #17 · CONFIRMED',N'2026-10-26T14:59:44.8479850',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (17,N'Đơn #4 · READY_FOR_PICKUP',N'2026-10-23T23:29:57.0636630',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (170,N'Đơn #17 · READY_FOR_PICKUP',N'2026-10-26T14:59:49.8549280',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (171,N'Đơn #17 · READY_FOR_PICKUP',N'2026-10-26T14:59:49.8598370',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (172,N'Đơn #17 · ASSIGNED',N'2026-10-26T15:00:51.1637660',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (173,N'Đơn #17 · ASSIGNED',N'2026-10-26T15:00:51.1725300',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (174,N'Đơn #17',N'2026-10-26T15:00:51.1791300',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (175,N'Đơn #17 · PICKED_UP',N'2026-10-26T15:01:33.6688720',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (176,N'Đơn #17 · PICKED_UP',N'2026-10-26T15:01:33.6815330',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (177,N'Đơn #17 · SHIPPING',N'2026-10-26T15:01:40.6165400',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (178,N'Đơn #17 · SHIPPING',N'2026-10-26T15:01:40.6235550',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (179,N'Đơn #17 · DELIVERED',N'2026-10-26T15:01:46.7275160',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (18,N'Đơn #4 · READY_FOR_PICKUP',N'2026-10-23T23:29:57.0666580',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (180,N'Đơn #17 · DELIVERED',N'2026-10-26T15:01:46.7310610',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (181,N'Đơn #18 · NEW',N'2026-10-26T15:44:54.6308670',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (182,N'Đơn #18 · NEW',N'2026-10-26T15:44:54.6398670',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (183,N'Đơn #18 · NEW',N'2026-10-26T18:09:53.9702330',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (184,N'Đơn #18 · NEW',N'2026-10-26T18:09:54.0230340',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (185,N'Đơn #19 · NEW',N'2026-10-26T18:14:10.1618550',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (186,N'Đơn #19 · NEW',N'2026-10-26T18:14:10.1668420',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (187,N'Đơn #19 · NEW',N'2026-10-26T18:15:35.1390560',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (188,N'Đơn #19 · NEW',N'2026-10-26T18:15:35.1520640',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (19,N'Đơn #4 · ASSIGNED',N'2026-10-23T23:29:57.1246790',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (2,N'Đơn #1 · NEW',N'2026-10-23T23:29:55.8434690',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (20,N'Đơn #4 · ASSIGNED',N'2026-10-23T23:29:57.1399600',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (21,N'Đơn #4',N'2026-10-23T23:29:57.1527540',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (22,N'Đơn #5 · NEW',N'2026-10-23T23:29:57.3792310',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (23,N'Đơn #5 · NEW',N'2026-10-23T23:29:57.3872320',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (24,N'Đơn #5 · CONFIRMED',N'2026-10-23T23:29:57.4581270',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (25,N'Đơn #5 · CONFIRMED',N'2026-10-23T23:29:57.4711220',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (26,N'Đơn #5 · READY_FOR_PICKUP',N'2026-10-23T23:29:57.5237520',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (27,N'Đơn #5 · READY_FOR_PICKUP',N'2026-10-23T23:29:57.5307700',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (28,N'Đơn #5 · ASSIGNED',N'2026-10-23T23:29:57.5797660',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (29,N'Đơn #5 · ASSIGNED',N'2026-10-23T23:29:57.5817550',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (3,N'Đơn #2 · NEW',N'2026-10-23T23:29:56.1574110',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (30,N'Đơn #5',N'2026-10-23T23:29:57.5869400',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (31,N'Đơn #5 · PICKED_UP',N'2026-10-23T23:29:57.6194160',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (32,N'Đơn #5 · PICKED_UP',N'2026-10-23T23:29:57.6302520',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (33,N'Đơn #6 · NEW',N'2026-10-23T23:29:57.8609220',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (34,N'Đơn #6 · NEW',N'2026-10-23T23:29:57.8689210',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (35,N'Đơn #6 · CONFIRMED',N'2026-10-23T23:29:57.9457750',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (36,N'Đơn #6 · CONFIRMED',N'2026-10-23T23:29:57.9576460',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (37,N'Đơn #6 · READY_FOR_PICKUP',N'2026-10-23T23:29:58.0052010',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (38,N'Đơn #6 · READY_FOR_PICKUP',N'2026-10-23T23:29:58.0163630',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (39,N'Đơn #6 · ASSIGNED',N'2026-10-23T23:29:58.0800040',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (4,N'Đơn #2 · NEW',N'2026-10-23T23:29:56.1648750',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (40,N'Đơn #6 · ASSIGNED',N'2026-10-23T23:29:58.0809980',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (41,N'Đơn #6',N'2026-10-23T23:29:58.0834020',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (42,N'Đơn #6 · PICKED_UP',N'2026-10-23T23:29:58.0983950',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (43,N'Đơn #6 · PICKED_UP',N'2026-10-23T23:29:58.1023990',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (44,N'Đơn #6 · SHIPPING',N'2026-10-23T23:29:58.1530920',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (45,N'Đơn #6 · SHIPPING',N'2026-10-23T23:29:58.1752520',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (46,N'Đơn #7 · NEW',N'2026-10-23T23:29:58.4098570',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (47,N'Đơn #7 · NEW',N'2026-10-23T23:29:58.4189600',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (48,N'Đơn #7 · CONFIRMED',N'2026-10-23T23:29:58.5018210',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (49,N'Đơn #7 · CONFIRMED',N'2026-10-23T23:29:58.5148220',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (5,N'Đơn #2 · CONFIRMED',N'2026-10-23T23:29:56.2466780',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (50,N'Đơn #7 · READY_FOR_PICKUP',N'2026-10-23T23:29:58.5478770',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (51,N'Đơn #7 · READY_FOR_PICKUP',N'2026-10-23T23:29:58.5510840',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (52,N'Đơn #7 · ASSIGNED',N'2026-10-23T23:29:58.5831770',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (53,N'Đơn #7 · ASSIGNED',N'2026-10-23T23:29:58.5851820',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (54,N'Đơn #7',N'2026-10-23T23:29:58.5881910',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (55,N'Đơn #7 · PICKED_UP',N'2026-10-23T23:29:58.6017210',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (56,N'Đơn #7 · PICKED_UP',N'2026-10-23T23:29:58.6047090',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (57,N'Đơn #7 · SHIPPING',N'2026-10-23T23:29:58.6451730',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (58,N'Đơn #7 · SHIPPING',N'2026-10-23T23:29:58.6639320',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (59,N'Đơn #7 · DELIVERED',N'2026-10-23T23:29:58.7135590',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (6,N'Đơn #2 · CONFIRMED',N'2026-10-23T23:29:56.2579190',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (60,N'Đơn #7 · DELIVERED',N'2026-10-23T23:29:58.7298320',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (61,N'Đơn #8 · NEW',N'2026-10-23T23:29:59.0488450',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (62,N'Đơn #8 · NEW',N'2026-10-23T23:29:59.0538460',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (63,N'Đơn #8 · CANCELLED',N'2026-10-23T23:29:59.1362670',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (64,N'Đơn #8 · CANCELLED',N'2026-10-23T23:29:59.1489060',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (65,N'Đơn #9 · NEW',N'2026-10-23T23:29:59.3689900',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (66,N'Đơn #9 · NEW',N'2026-10-23T23:29:59.3769960',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (67,N'Đơn #9 · CONFIRMED',N'2026-10-23T23:29:59.4466730',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (68,N'Đơn #9 · CONFIRMED',N'2026-10-23T23:29:59.4620710',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (69,N'Đơn #9 · READY_FOR_PICKUP',N'2026-10-23T23:29:59.5213240',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (7,N'Đơn #3 · NEW',N'2026-10-23T23:29:56.5300870',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (70,N'Đơn #9 · READY_FOR_PICKUP',N'2026-10-23T23:29:59.5353130',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (71,N'Đơn #9 · ASSIGNED',N'2026-10-23T23:29:59.5833130',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (72,N'Đơn #9 · ASSIGNED',N'2026-10-23T23:29:59.5853320',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (73,N'Đơn #9',N'2026-10-23T23:29:59.5873130',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (74,N'Đơn #9 · PICKED_UP',N'2026-10-23T23:29:59.6053170',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (75,N'Đơn #9 · PICKED_UP',N'2026-10-23T23:29:59.6091340',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (76,N'Đơn #9 · SHIPPING',N'2026-10-23T23:29:59.6825340',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (77,N'Đơn #9 · SHIPPING',N'2026-10-23T23:29:59.6975320',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (78,N'Đơn #9 · DELIVERED',N'2026-10-23T23:29:59.7515320',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (79,N'Đơn #9 · DELIVERED',N'2026-10-23T23:29:59.7679890',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (8,N'Đơn #3 · NEW',N'2026-10-23T23:29:56.5360780',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (80,N'Đơn #9 · RETURN_REQUESTED',N'2026-10-23T23:29:59.8149830',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (81,N'Đơn #9 · RETURN_REQUESTED',N'2026-10-23T23:29:59.8169790',0,N'/vendor/shops/2/orders',N'Cập nhật đơn shop',4);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (82,N'Đơn #10 · NEW',N'2026-10-23T23:29:59.9463440',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (83,N'Đơn #10 · NEW',N'2026-10-23T23:29:59.9473390',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (84,N'Đơn #10 · CONFIRMED',N'2026-10-23T23:29:59.9823510',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (85,N'Đơn #10 · CONFIRMED',N'2026-10-23T23:29:59.9843420',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (86,N'Đơn #10 · READY_FOR_PICKUP',N'2026-10-23T23:29:59.9986950',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (87,N'Đơn #10 · READY_FOR_PICKUP',N'2026-10-23T23:30:00.0006940',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (88,N'Đơn #10 · ASSIGNED',N'2026-10-23T23:30:00.0437050',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (89,N'Đơn #10 · ASSIGNED',N'2026-10-23T23:30:00.0457080',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (9,N'Đơn #3 · CONFIRMED',N'2026-10-23T23:29:56.5872610',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (90,N'Đơn #10',N'2026-10-23T23:30:00.0466960',0,N'/shipper',N'Phân công giao hàng',6);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (91,N'Đơn #10 · PICKED_UP',N'2026-10-23T23:30:00.0596980',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (92,N'Đơn #10 · PICKED_UP',N'2026-10-23T23:30:00.0617160',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (93,N'Đơn #10 · SHIPPING',N'2026-10-23T23:30:00.0730840',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (94,N'Đơn #10 · SHIPPING',N'2026-10-23T23:30:00.0750900',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (95,N'Đơn #10 · DELIVERED',N'2026-10-23T23:30:00.0875760',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (96,N'Đơn #10 · DELIVERED',N'2026-10-23T23:30:00.0935750',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (97,N'Đơn #10 · RETURN_REQUESTED',N'2026-10-23T23:30:00.1075690',0,N'/orders',N'Cập nhật đơn hàng',7);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (98,N'Đơn #10 · RETURN_REQUESTED',N'2026-10-23T23:30:00.1085690',0,N'/vendor/shops/3/orders',N'Cập nhật đơn shop',5);

INSERT INTO [dbo].[notifications] ([id],[content],[created_at],[read_status],[target_url],[title],[user_id]) VALUES (99,N'Đơn #10 · RETURNED',N'2026-10-23T23:30:00.1206180',0,N'/orders',N'Cập nhật đơn hàng',7);

SET IDENTITY_INSERT [dbo].[notifications] OFF;

DBCC CHECKIDENT (N'[dbo].[notifications]', RESEED, 188) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[order_details] ON;

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (1,1,1980000.00,1980000.00,1,18);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (10,1,850000.00,850000.00,10,35);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (11,1,1980000.00,1980000.00,11,18);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (12,1,1980000.00,1980000.00,12,18);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (13,1,850000.00,850000.00,13,35);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (14,1,1700000.00,1700000.00,14,50);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (15,1,1700000.00,1700000.00,15,50);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (16,2,4000000.00,2000000.00,16,49);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (17,1,1700000.00,1700000.00,17,50);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (18,1,1700000.00,1700000.00,18,50);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (19,1,1700000.00,1700000.00,19,50);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (2,1,850000.00,850000.00,2,35);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (3,1,1980000.00,1980000.00,3,18);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (4,1,850000.00,850000.00,4,35);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (5,1,1980000.00,1980000.00,5,18);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (6,1,850000.00,850000.00,6,35);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (7,1,1980000.00,1980000.00,7,18);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (8,1,850000.00,850000.00,8,35);

INSERT INTO [dbo].[order_details] ([id],[quantity],[subtotal],[unit_price],[order_id],[product_id]) VALUES (9,1,1980000.00,1980000.00,9,18);

SET IDENTITY_INSERT [dbo].[order_details] OFF;

DBCC CHECKIDENT (N'[dbo].[order_details]', RESEED, 19) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[orders] ON;

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (1,N'2026-10-23T23:29:55.8194700',100000.00,N'Giao hoa sinh nhật buổi sáng',N'COD',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'NEW',1980000.00,1905000.00,1,2,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (10,N'2026-10-23T23:29:59.9343350',25000.00,N'Giao hoa hoàn trả cửa hàng',N'COD',N'PAID',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'RETURNED',850000.00,850000.00,1,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (11,N'2026-10-23T23:30:00.1781850',100000.00,N'Giao hoa hoàn tất hoàn tiền',N'COD',N'REFUNDED',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'REFUNDED',1980000.00,1905000.00,1,2,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (12,N'2026-10-23T23:30:00.3899530',69964.66,N'Giao hoa chúc mừng hai cửa hàng',N'COD',N'PAID',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'DELIVERED',1980000.00,1935035.34,1,2,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (13,N'2026-10-23T23:30:00.3939540',30035.34,N'Giao hoa chúc mừng hai cửa hàng',N'COD',N'REFUNDED',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'REFUNDED',850000.00,844964.66,1,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (14,N'2026-10-24T13:11:55.4269700',0.00,N'',N'VNPAY',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',18000.00,N'NEW',1700000.00,1718000.00,2,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (15,N'2026-10-24T13:20:44.1381740',0.00,N'',N'VNPAY',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',18000.00,N'NEW',1700000.00,1718000.00,2,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (16,N'2026-10-26T14:44:08.1881460',100000.00,N'123456',N'VNPAY',N'PENDING',N'Lê Duệ Thắng',N'0998877665',N'số 1 võ văn ngân, linh trung, thủ đức, hcm',18000.00,N'NEW',4000000.00,3918000.00,2,3,1);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (17,N'2026-10-26T14:54:49.4019550',0.00,N'Kiểm thử thanh toán VNPAY Sandbox',N'VNPAY',N'PAID',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',18000.00,N'DELIVERED',1700000.00,1718000.00,2,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (18,N'2026-10-26T15:44:54.6052670',0.00,N'Kiểm thử Merchant Sandbox mới XZY8U5QF',N'VNPAY',N'PAID',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',18000.00,N'NEW',1700000.00,1718000.00,2,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (19,N'2026-10-26T18:14:10.1418330',18000.00,N'',N'VNPAY',N'PAID',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',18000.00,N'NEW',1700000.00,1700000.00,2,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (2,N'2026-10-23T23:29:56.1361460',25000.00,N'Giao hoa chúc mừng tân gia',N'COD',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'CONFIRMED',850000.00,850000.00,1,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (3,N'2026-10-23T23:29:56.5126090',100000.00,N'Giao hoa lễ khai trương',N'COD',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'READY_FOR_PICKUP',1980000.00,1905000.00,1,2,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (4,N'2026-10-23T23:29:56.9197350',25000.00,N'Giao hoa tặng đồng nghiệp',N'COD',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'ASSIGNED',850000.00,850000.00,1,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (5,N'2026-10-23T23:29:57.3582320',100000.00,N'Giao hoa tặng người thân',N'COD',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'PICKED_UP',1980000.00,1905000.00,1,2,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (6,N'2026-10-23T23:29:57.8439760',25000.00,N'Giao hoa kỷ niệm ngày cưới',N'COD',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'SHIPPING',850000.00,850000.00,1,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (7,N'2026-10-23T23:29:58.3873200',100000.00,N'Giao hoa chúc mừng thăng chức',N'COD',N'PAID',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'DELIVERED',1980000.00,1905000.00,1,2,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (8,N'2026-10-23T23:29:59.0338470',25000.00,N'Giao hoa đổi lịch nhận',N'COD',N'PENDING',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'CANCELLED',850000.00,850000.00,1,3,7);

INSERT INTO [dbo].[orders] ([id],[created_at],[discount_amount],[note],[payment_method],[payment_status],[receiver_name],[receiver_phone],[shipping_address],[shipping_fee],[status],[subtotal],[total_amount],[shipping_provider_id],[shop_id],[user_id]) VALUES (9,N'2026-10-23T23:29:59.3509900',100000.00,N'Giao hoa đổi mẫu thiết kế',N'COD',N'PAID',N'Nguyễn Khánh An',N'0901234001',N'12 Đường Hoa, TP. Thủ Đức, TP. Hồ Chí Minh',25000.00,N'RETURN_REQUESTED',1980000.00,1905000.00,1,2,7);

SET IDENTITY_INSERT [dbo].[orders] OFF;

DBCC CHECKIDENT (N'[dbo].[orders]', RESEED, 19) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[product_images] ON;

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (1,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019188/starshop/products/kclgfz7ewv1qmn9kv1ln.webp',1,N'starshop/products/kclgfz7ewv1qmn9kv1ln',1);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (10,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019206/starshop/products/xjrpeypvqxblprpzcsvm.webp',1,N'starshop/products/xjrpeypvqxblprpzcsvm',10);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (11,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019208/starshop/products/ka7sqqvjb4uhd8mutyya.webp',1,N'starshop/products/ka7sqqvjb4uhd8mutyya',11);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (12,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019210/starshop/products/rpoozxazelmxiuuszqny.webp',1,N'starshop/products/rpoozxazelmxiuuszqny',12);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (13,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019212/starshop/products/ebpnfik2isqkcod85mlc.webp',1,N'starshop/products/ebpnfik2isqkcod85mlc',13);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (14,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019214/starshop/products/zyqtnvddnpye1mmqxqrm.webp',1,N'starshop/products/zyqtnvddnpye1mmqxqrm',14);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (15,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019216/starshop/products/o78uwbj7gc6uvkedwvvv.webp',1,N'starshop/products/o78uwbj7gc6uvkedwvvv',15);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (16,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019219/starshop/products/dufv1z9q77p3cn5dpgpg.webp',1,N'starshop/products/dufv1z9q77p3cn5dpgpg',16);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (17,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019221/starshop/products/e69cu2gdldxh95s48zde.webp',1,N'starshop/products/e69cu2gdldxh95s48zde',17);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (18,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019223/starshop/products/bfab0xymuo962eg8xbiz.webp',1,N'starshop/products/bfab0xymuo962eg8xbiz',18);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (19,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019225/starshop/products/bokkuxaz29dbbpvwjodx.webp',1,N'starshop/products/bokkuxaz29dbbpvwjodx',19);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (2,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019190/starshop/products/rmbbx0bifdtrxktka4ep.webp',1,N'starshop/products/rmbbx0bifdtrxktka4ep',2);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (20,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019227/starshop/products/pw9vadeiz6tzwvrwwsqi.webp',1,N'starshop/products/pw9vadeiz6tzwvrwwsqi',20);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (21,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019230/starshop/products/dvmtcv0fjqhd0oxigizt.webp',1,N'starshop/products/dvmtcv0fjqhd0oxigizt',21);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (22,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019232/starshop/products/pmmypqlu8l3sixcygttl.webp',1,N'starshop/products/pmmypqlu8l3sixcygttl',22);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (23,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019235/starshop/products/zwjgcljtpzww6hmdf4lb.webp',1,N'starshop/products/zwjgcljtpzww6hmdf4lb',23);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (24,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019238/starshop/products/arz4pbkv6jx67rdzimrf.webp',1,N'starshop/products/arz4pbkv6jx67rdzimrf',24);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (25,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019240/starshop/products/hqgo7prndmbmczsaycwk.webp',1,N'starshop/products/hqgo7prndmbmczsaycwk',25);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (26,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019242/starshop/products/yhbwlf9qptlvgbl8olkx.webp',1,N'starshop/products/yhbwlf9qptlvgbl8olkx',26);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (27,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019244/starshop/products/ogn5ouqc2ypf8fy4wben.webp',1,N'starshop/products/ogn5ouqc2ypf8fy4wben',27);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (28,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019246/starshop/products/q93uscffxkjxj0qluwrn.webp',1,N'starshop/products/q93uscffxkjxj0qluwrn',28);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (29,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019248/starshop/products/rannuw7yju8bw9hk2xpq.webp',1,N'starshop/products/rannuw7yju8bw9hk2xpq',29);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (3,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019191/starshop/products/egx5puccrqbae9g0xwxg.webp',1,N'starshop/products/egx5puccrqbae9g0xwxg',3);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (30,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019250/starshop/products/nnmjfwons0wu5fvxshib.webp',1,N'starshop/products/nnmjfwons0wu5fvxshib',30);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (31,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019252/starshop/products/fcmrq9taxdfzklnstbph.webp',1,N'starshop/products/fcmrq9taxdfzklnstbph',31);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (32,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019254/starshop/products/uxcprrlhxvz234spacaw.webp',1,N'starshop/products/uxcprrlhxvz234spacaw',32);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (33,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019256/starshop/products/qusm0zowk8gjvofen1vn.webp',1,N'starshop/products/qusm0zowk8gjvofen1vn',33);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (34,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019258/starshop/products/nlwrdax0rd7b6aancwiz.webp',1,N'starshop/products/nlwrdax0rd7b6aancwiz',34);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (35,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019260/starshop/products/hkjtxr7wxr1qhtc2uumo.webp',1,N'starshop/products/hkjtxr7wxr1qhtc2uumo',35);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (36,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019262/starshop/products/iia9kuxn9t5hwlkuxnlr.webp',1,N'starshop/products/iia9kuxn9t5hwlkuxnlr',36);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (37,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019264/starshop/products/yr2ndqewsoud5fobavwh.webp',1,N'starshop/products/yr2ndqewsoud5fobavwh',37);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (38,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019266/starshop/products/glqdfg1thpbp0y7psf8b.webp',1,N'starshop/products/glqdfg1thpbp0y7psf8b',38);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (39,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019269/starshop/products/ibwwf0wxnabkr2f8illp.webp',1,N'starshop/products/ibwwf0wxnabkr2f8illp',39);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (4,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019193/starshop/products/jujkrm3phoi46svcoevh.webp',1,N'starshop/products/jujkrm3phoi46svcoevh',4);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (40,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019270/starshop/products/ozv3ffjdkdm9qz1cb5y2.webp',1,N'starshop/products/ozv3ffjdkdm9qz1cb5y2',40);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (41,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019272/starshop/products/llqamqb3cbkaoi8x9h0u.webp',1,N'starshop/products/llqamqb3cbkaoi8x9h0u',41);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (42,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019274/starshop/products/krfiwi4p8nhvrp7uhyov.webp',1,N'starshop/products/krfiwi4p8nhvrp7uhyov',42);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (43,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019276/starshop/products/irt4be95q7znvvhzpyjm.webp',1,N'starshop/products/irt4be95q7znvvhzpyjm',43);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (44,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019278/starshop/products/vmav9qqla2h5jwyoptcg.webp',1,N'starshop/products/vmav9qqla2h5jwyoptcg',44);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (45,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019280/starshop/products/tu5zoyg2hjurnybw2mbk.webp',1,N'starshop/products/tu5zoyg2hjurnybw2mbk',45);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (46,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019283/starshop/products/exnxhhv8b906p4v0qnmg.webp',1,N'starshop/products/exnxhhv8b906p4v0qnmg',46);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (47,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019285/starshop/products/ygdfbkjda5c8egmkthpx.webp',1,N'starshop/products/ygdfbkjda5c8egmkthpx',47);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (48,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019287/starshop/products/rimcdmp21zlxeldrknga.webp',1,N'starshop/products/rimcdmp21zlxeldrknga',48);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (49,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019289/starshop/products/yzqjszmxltaag3qr0pc2.webp',1,N'starshop/products/yzqjszmxltaag3qr0pc2',49);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (5,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019196/starshop/products/qwj31sclum9toc9mdwm9.webp',1,N'starshop/products/qwj31sclum9toc9mdwm9',5);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (50,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793018218/starshop/products/kzzovwyab4szlqfsc5sf.webp',1,N'starshop/products/kzzovwyab4szlqfsc5sf',50);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (6,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019198/starshop/products/aolxbfqbfxexfgthu5u3.webp',1,N'starshop/products/aolxbfqbfxexfgthu5u3',6);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (7,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019200/starshop/products/pfnivesbykigz6mwh6te.webp',1,N'starshop/products/pfnivesbykigz6mwh6te',7);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (8,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019202/starshop/products/qu5xai1s1xusi4riw1nq.webp',1,N'starshop/products/qu5xai1s1xusi4riw1nq',8);

INSERT INTO [dbo].[product_images] ([id],[image_url],[primary_image],[public_id],[product_id]) VALUES (9,N'https://res.cloudinary.com/ckaav9vf/image/upload/v1793019204/starshop/products/d9bpki2tu59tevfazwjr.webp',1,N'starshop/products/d9bpki2tu59tevfazwjr',9);

SET IDENTITY_INSERT [dbo].[product_images] OFF;

DBCC CHECKIDENT (N'[dbo].[product_images]', RESEED, 50) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[product_views] ON;

INSERT INTO [dbo].[product_views] ([id],[viewed_at],[product_id],[user_id]) VALUES (1,N'2026-10-23T23:30:00.3117780',18,7);

INSERT INTO [dbo].[product_views] ([id],[viewed_at],[product_id],[user_id]) VALUES (2,N'2026-10-23T23:30:00.1330620',35,7);

INSERT INTO [dbo].[product_views] ([id],[viewed_at],[product_id],[user_id]) VALUES (3,N'2026-10-26T18:17:32.4199520',50,7);

INSERT INTO [dbo].[product_views] ([id],[viewed_at],[product_id],[user_id]) VALUES (4,N'2026-10-26T14:41:56.4564440',49,1);

SET IDENTITY_INSERT [dbo].[product_views] OFF;

DBCC CHECKIDENT (N'[dbo].[product_views]', RESEED, 4) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[products] ON;

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (1,N'2026-10-16T13:38:25.0778330',N'Mây Trắng Bồng Bềnh. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',420000.00,N'Mây Trắng Bồng Bềnh',450000.00,30,0.0,12,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (10,N'2026-10-16T13:38:25.1484130',N'Hydrangea Harmony. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',NULL,N'Hydrangea Harmony',720000.00,30,0.0,21,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (11,N'2026-10-16T13:38:25.1539540',N'Hộp Hoa Tươi Mica Lucent Agencies. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1850000.00,N'Hộp Hoa Tươi Mica Lucent Agencies',1900000.00,30,0.0,22,N'ACTIVE',2,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (12,N'2026-10-16T13:38:25.1569530',N'Hộp Hoa Mica Garden Sunny. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1950000.00,N'Hộp Hoa Mica Garden Sunny',2300000.00,30,0.0,23,N'ACTIVE',2,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (13,N'2026-10-16T13:38:25.1624690',N'Mica FlowerBox Elephant Of Love. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',NULL,N'Mica FlowerBox Elephant Of Love',2200000.00,30,0.0,24,N'ACTIVE',2,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (14,N'2026-10-16T13:38:25.1664760',N'Mica FlowerBox Sheep Of Love. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',NULL,N'Mica FlowerBox Sheep Of Love',1800000.00,30,0.0,25,N'ACTIVE',2,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (15,N'2026-10-16T13:38:25.1694710',N'Sunset And Ladie. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1720000.00,N'Sunset And Ladie',2000000.00,30,0.0,26,N'ACTIVE',2,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (16,N'2026-10-16T13:38:25.1726020',N'Ohara Mix Daisy White. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1250000.00,N'Ohara Mix Daisy White',1550000.00,30,0.0,27,N'ACTIVE',2,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (17,N'2026-10-16T13:38:25.1786160',N'Mica FlowerBox Penguin Of Love. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',NULL,N'Mica FlowerBox Penguin Of Love',2000000.00,30,0.0,28,N'ACTIVE',2,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (18,N'2026-10-16T13:38:25.1831260',N'Hộp Hoa Mica Tulip White Beauty. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1980000.00,N'Hộp Hoa Mica Tulip White Beauty',2350000.00,24,5.0,35,N'ACTIVE',2,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (19,N'2026-10-16T13:38:25.1871200',N'Hộp Hoa Mica Garden Colourfull. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1950000.00,N'Hộp Hoa Mica Garden Colourfull',2100000.00,30,0.0,30,N'ACTIVE',2,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (2,N'2026-10-16T13:38:25.1024600',N'Ohara Pastel Viences. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',NULL,N'Ohara Pastel Viences',880000.00,30,0.0,13,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (20,N'2026-10-16T13:38:25.1906400',N'Hộp Hoa Mica Tulip Darkblue White. Hộp hoa mica dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1850000.00,N'Hộp Hoa Mica Tulip Darkblue White',2050000.00,30,0.0,31,N'ACTIVE',2,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (21,N'2026-10-16T13:38:25.1966460',N'Lẵng Hoa White Moment Love. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1800000.00,N'Lẵng Hoa White Moment Love',2250000.00,30,0.0,32,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (22,N'2026-10-16T13:38:25.1996300',N'Lẵng Hoa Tươi Nature Evil. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1780000.00,N'Lẵng Hoa Tươi Nature Evil',2000000.00,30,0.0,33,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (23,N'2026-10-16T13:38:25.2016880',N'Sắc Đỏ May Mắn. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1850000.00,N'Sắc Đỏ May Mắn',1950000.00,30,0.0,34,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (24,N'2026-10-16T13:38:25.2036890',N'Lẵng Hoa Tươi Capuchino GoL. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1250000.00,N'Lẵng Hoa Tươi Capuchino GoL',1450000.00,30,0.0,35,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (25,N'2026-10-16T13:38:25.2066900',N'Blue Hydra. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2280000.00,N'Blue Hydra',2500000.00,30,0.0,36,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (26,N'2026-10-16T13:38:25.2132010',N'Lẵng Hoa Chúc Mừng Tia Nắng. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1500000.00,N'Lẵng Hoa Chúc Mừng Tia Nắng',1800000.00,30,0.0,37,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (27,N'2026-10-16T13:38:25.2182030',N'Lẵng Hoa Pastel Celestial. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2450000.00,N'Lẵng Hoa Pastel Celestial',2800000.00,30,0.0,38,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (28,N'2026-10-16T13:38:25.2207160',N'Lẵng Hoa Chúc Mừng Ấm Áp Ngày Vui. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2000000.00,N'Lẵng Hoa Chúc Mừng Ấm Áp Ngày Vui',2630000.00,30,0.0,39,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (29,N'2026-10-16T13:38:25.2227200',N'Khoe Sắc. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',NULL,N'Khoe Sắc',1500000.00,30,0.0,40,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (3,N'2026-10-16T13:38:25.1084700',N'Ohara White And Pink Viency. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',880000.00,N'Ohara White And Pink Viency',950000.00,30,0.0,14,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (30,N'2026-10-16T13:38:25.2267430',N'Tinh Tế Tặng Sếp. Lẵng hoa khai trương dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1250000.00,N'Tinh Tế Tặng Sếp',1350000.00,30,0.0,41,N'ACTIVE',3,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (31,N'2026-10-16T13:38:25.2349400',N'Sắc Màu Tinh Tế. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1000000.00,N'Sắc Màu Tinh Tế',1025000.00,30,0.0,42,N'ACTIVE',4,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (32,N'2026-10-16T13:38:25.2379430',N'Azure Dream Garden Basket. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1380000.00,N'Azure Dream Garden Basket',1550000.00,30,0.0,43,N'ACTIVE',4,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (33,N'2026-10-16T13:38:25.2400090',N'Say Đắm Hồng. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',950000.00,N'Say Đắm Hồng',1200000.00,30,0.0,44,N'ACTIVE',4,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (34,N'2026-10-16T13:38:25.2450280',N'Lily Sweety Pink. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',950000.00,N'Lily Sweety Pink',1200000.00,30,0.0,45,N'ACTIVE',4,2);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (35,N'2026-10-16T13:38:25.2495280',N'Sunny Shimmer. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',850000.00,N'Sunny Shimmer',950000.00,27,0.0,49,N'ACTIVE',4,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (36,N'2026-10-16T13:38:25.2515340',N'Giỏ Hoa Tươi Mừng Khai Trương Đồng Tiền Xinh. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',580000.00,N'Giỏ Hoa Tươi Mừng Khai Trương Đồng Tiền Xinh',750000.00,30,0.0,47,N'ACTIVE',4,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (37,N'2026-10-16T13:38:25.2545330',N'Giỏ Hoa Tươi Pastel Angel. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',865000.00,N'Giỏ Hoa Tươi Pastel Angel',950000.00,30,0.0,48,N'ACTIVE',4,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (38,N'2026-10-16T13:38:25.2575360',N'Giỏ Hoa Đẹp Mừng Sinh Nhật Mẹ Ngọt Ngào. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',880000.00,N'Giỏ Hoa Đẹp Mừng Sinh Nhật Mẹ Ngọt Ngào',900000.00,30,0.0,49,N'ACTIVE',4,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (39,N'2026-10-16T13:38:25.2630630',N'Giỏ Hoa Tươi Blissful. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',850000.00,N'Giỏ Hoa Tươi Blissful',950000.00,30,0.0,50,N'ACTIVE',4,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (4,N'2026-10-16T13:38:25.1167090',N'Lily Julibee. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',880000.00,N'Lily Julibee',950000.00,30,0.0,15,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (40,N'2026-10-16T13:38:25.2650530',N'Florentine Grace. Giỏ hoa chúc mừng dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1170000.00,N'Florentine Grace',1250000.00,30,0.0,51,N'ACTIVE',4,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (41,N'2026-10-16T13:38:25.2680550',N'Kệ Hoa Chúc Mừng Ember Corgart. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1620000.00,N'Kệ Hoa Chúc Mừng Ember Corgart',1800000.00,30,0.0,52,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (42,N'2026-10-16T13:38:25.2695820',N'Kệ Hoa Niên Niên Đại Phát. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1420000.00,N'Kệ Hoa Niên Niên Đại Phát',1700000.00,30,0.0,53,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (43,N'2026-10-16T13:38:25.2715940',N'Kệ Hoa Phát Tài Phát Đạt. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1380000.00,N'Kệ Hoa Phát Tài Phát Đạt',1500000.00,30,0.0,54,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (44,N'2026-10-16T13:38:25.2735930',N'Kệ Hoa Khai Trương Hồng Sang Trọng. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2000000.00,N'Kệ Hoa Khai Trương Hồng Sang Trọng',2200000.00,30,0.0,55,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (45,N'2026-10-16T13:38:25.2796100',N'Kệ Hoa Khai Trương Phát Lộc Thịnh Vượng. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2400000.00,N'Kệ Hoa Khai Trương Phát Lộc Thịnh Vượng',2900000.00,30,0.0,56,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (46,N'2026-10-16T13:38:25.2837830',N'Kệ Hoa Hồng Ohara Trắng Hồng Vẹn Tròn. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2500000.00,N'Kệ Hoa Hồng Ohara Trắng Hồng Vẹn Tròn',2650000.00,30,0.0,57,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (47,N'2026-10-16T13:38:25.2867820',N'Viên Mãn Hạnh Phúc. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1500000.00,N'Viên Mãn Hạnh Phúc',1650000.00,30,0.0,58,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (48,N'2026-10-16T13:38:25.2887770',N'Kệ Hoa Chúc Mừng Happy Moment Flus. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2000000.00,N'Kệ Hoa Chúc Mừng Happy Moment Flus',2200000.00,30,0.0,59,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (49,N'2026-10-16T13:38:25.2938520',N'Kệ Hoa Chúc Mừng Garden Style Orange. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',2000000.00,N'Kệ Hoa Chúc Mừng Garden Style Orange',2500000.00,28,0.0,62,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (5,N'2026-10-16T13:38:25.1218590',N'Nắng Mai. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',650000.00,N'Nắng Mai',750000.00,30,0.0,16,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (50,N'2026-10-16T13:38:25.2968500',N'Kệ Hoa Lạc Hồng Thịnh Vượng. Kệ hoa dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',1700000.00,N'Kệ Hoa Lạc Hồng Thịnh Vượng',1800000.00,25,0.0,66,N'ACTIVE',5,3);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (6,N'2026-10-16T13:38:25.1278690',N'Blissful Bouquet – Bó Hoa Hạnh Phúc. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',850000.00,N'Blissful Bouquet – Bó Hoa Hạnh Phúc',950000.00,30,0.0,17,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (7,N'2026-10-16T13:38:25.1344080',N'Sweet Muse. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',780000.00,N'Sweet Muse',850000.00,30,0.0,18,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (8,N'2026-10-16T13:38:25.1373850',N'Hoa Bó Rose Rosa Pink. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',950000.00,N'Hoa Bó Rose Rosa Pink',995000.00,30,0.0,19,N'ACTIVE',1,1);

INSERT INTO [dbo].[products] ([id],[created_at],[description],[discount_price],[name],[price],[quantity],[rating],[sold_count],[status],[category_id],[shop_id]) VALUES (9,N'2026-10-16T13:38:25.1444150',N'Bó Hoa Đẹp Tặng Sinh Nhật Bạn Gái. Bó hoa sinh nhật dành tặng những người thân yêu trong các dịp đặc biệt. Liên hệ cửa hàng để được tư vấn thời gian giao và cách bảo quản hoa.',800000.00,N'Bó Hoa Đẹp Tặng Sinh Nhật Bạn Gái',950000.00,30,0.0,20,N'ACTIVE',1,1);

SET IDENTITY_INSERT [dbo].[products] OFF;

DBCC CHECKIDENT (N'[dbo].[products]', RESEED, 50) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[promotions] ON;

INSERT INTO [dbo].[promotions] ([id],[active],[code],[description],[discount_value],[end_at],[max_discount_amount],[min_order_amount],[name],[quantity],[scope],[start_at],[type],[shop_id]) VALUES (1,1,N'HOATUOI10',NULL,10.00,N'2027-01-23T23:29:55.3363930',100000.00,200000.00,N'Ưu đãi hoa tươi 10%',92,N'SYSTEM',N'2026-10-22T23:29:55.3363930',N'PERCENT',NULL);

INSERT INTO [dbo].[promotions] ([id],[active],[code],[description],[discount_value],[end_at],[max_discount_amount],[min_order_amount],[name],[quantity],[scope],[start_at],[type],[shop_id]) VALUES (2,1,N'FREESHIP',NULL,0.00,N'2027-01-23T23:29:55.3463940',100000.00,200000.00,N'Ưu đãi giao hoa',94,N'SYSTEM',N'2026-10-22T23:29:55.3463940',N'FREE_SHIPPING',NULL);

INSERT INTO [dbo].[promotions] ([id],[active],[code],[description],[discount_value],[end_at],[max_discount_amount],[min_order_amount],[name],[quantity],[scope],[start_at],[type],[shop_id]) VALUES (3,1,N'THUDUC50',NULL,50000.00,N'2027-01-23T23:29:55.3514310',100000.00,200000.00,N'Thủ Đức giảm 50.000đ',100,N'SHOP',N'2026-10-22T23:29:55.3514310',N'FIXED_AMOUNT',2);

SET IDENTITY_INSERT [dbo].[promotions] OFF;

DBCC CHECKIDENT (N'[dbo].[promotions]', RESEED, 3) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[reviews] ON;

INSERT INTO [dbo].[reviews] ([id],[content],[created_at],[image_url],[rating],[video_url],[order_detail_id],[product_id],[user_id]) VALUES (1,N'Hoa tươi, thiết kế đẹp, đóng gói cẩn thận và giao hàng đúng hẹn. Tôi rất hài lòng với dịch vụ của cửa hàng.',N'2026-10-23T23:29:58.8781100',NULL,5,NULL,7,18,7);

SET IDENTITY_INSERT [dbo].[reviews] OFF;

DBCC CHECKIDENT (N'[dbo].[reviews]', RESEED, 2) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[roles] ON;

INSERT INTO [dbo].[roles] ([id],[name]) VALUES (1,N'ADMIN');

INSERT INTO [dbo].[roles] ([id],[name]) VALUES (2,N'MANAGER');

INSERT INTO [dbo].[roles] ([id],[name]) VALUES (3,N'USER');

INSERT INTO [dbo].[roles] ([id],[name]) VALUES (4,N'VENDOR');

INSERT INTO [dbo].[roles] ([id],[name]) VALUES (5,N'SHIPPER');

SET IDENTITY_INSERT [dbo].[roles] OFF;

DBCC CHECKIDENT (N'[dbo].[roles]', RESEED, 5) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[shipper_assignments] ON;

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (1,N'2026-10-23T23:29:57.1124200',NULL,N'',NULL,4,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (10,N'2026-10-26T15:00:51.1541200',N'2026-10-26T15:01:46.7275160',N'Giao đơn thử nghiệm VNPAY #17',N'2026-10-26T15:01:33.6688720',17,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (2,N'2026-10-23T23:29:57.5747520',NULL,N'',N'2026-10-23T23:29:57.6194160',5,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (3,N'2026-10-23T23:29:58.0749960',NULL,N'',N'2026-10-23T23:29:58.0973960',6,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (4,N'2026-10-23T23:29:58.5771910',N'2026-10-23T23:29:58.7135590',N'',N'2026-10-23T23:29:58.6017210',7,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (5,N'2026-10-23T23:29:59.5773280',N'2026-10-23T23:29:59.7515320',N'',N'2026-10-23T23:29:59.6043430',9,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (6,N'2026-10-23T23:30:00.0397080',N'2026-10-23T23:30:00.0875760',N'',N'2026-10-23T23:30:00.0586970',10,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (7,N'2026-10-23T23:30:00.2164780',N'2026-10-23T23:30:00.2610940',N'',N'2026-10-23T23:30:00.2346200',11,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (8,N'2026-10-23T23:30:00.4314080',N'2026-10-23T23:30:00.4614100',N'',N'2026-10-23T23:30:00.4434090',12,6);

INSERT INTO [dbo].[shipper_assignments] ([id],[assigned_at],[delivered_at],[note],[picked_up_at],[order_id],[shipper_id]) VALUES (9,N'2026-10-23T23:30:00.4921030',N'2026-10-23T23:30:00.5277190',N'',N'2026-10-23T23:30:00.5041040',13,6);

SET IDENTITY_INSERT [dbo].[shipper_assignments] OFF;

DBCC CHECKIDENT (N'[dbo].[shipper_assignments]', RESEED, 10) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[shipping_providers] ON;

INSERT INTO [dbo].[shipping_providers] ([id],[active],[base_fee],[estimated_days],[name],[phone]) VALUES (1,1,25000.00,1,N'StarShop Express',NULL);

INSERT INTO [dbo].[shipping_providers] ([id],[active],[base_fee],[estimated_days],[name],[phone]) VALUES (2,1,18000.00,2,N'StarShop Standard',NULL);

SET IDENTITY_INSERT [dbo].[shipping_providers] OFF;

DBCC CHECKIDENT (N'[dbo].[shipping_providers]', RESEED, 2) WITH NO_INFOMSGS;

SET IDENTITY_INSERT [dbo].[shops] ON;

INSERT INTO [dbo].[shops] ([id],[address],[banner],[created_at],[description],[email],[logo],[name],[phone],[status],[owner_id],[manager_id]) VALUES (1,N'Quận 1, TP. Hồ Chí Minh',NULL,N'2026-10-16T13:38:25.0316630',N'Bộ sưu tập hoa dành cho sinh nhật, chúc mừng và những dịp đặc biệt.',NULL,NULL,N'StarShop Central',N'02838220001',N'ACTIVE',1,2);

INSERT INTO [dbo].[shops] ([id],[address],[banner],[created_at],[description],[email],[logo],[name],[phone],[status],[owner_id],[manager_id]) VALUES (2,N'TP. Thủ Đức, TP. Hồ Chí Minh',NULL,N'2026-10-23T23:29:55.2693940',N'Hoa tươi cho sinh nhật, chúc mừng và những dịp đặc biệt. Giao hoa tận nơi tại TP. Hồ Chí Minh.',NULL,NULL,N'StarShop Thủ Đức',N'02838220002',N'ACTIVE',4,2);

INSERT INTO [dbo].[shops] ([id],[address],[banner],[created_at],[description],[email],[logo],[name],[phone],[status],[owner_id],[manager_id]) VALUES (3,N'Bình Thạnh, TP. Hồ Chí Minh',NULL,N'2026-10-23T23:29:55.2794050',N'Hoa tươi cho sinh nhật, chúc mừng và những dịp đặc biệt. Giao hoa tận nơi tại TP. Hồ Chí Minh.',NULL,NULL,N'StarShop Bình Thạnh',N'02838220003',N'ACTIVE',5,3);

SET IDENTITY_INSERT [dbo].[shops] OFF;

DBCC CHECKIDENT (N'[dbo].[shops]', RESEED, 3) WITH NO_INFOMSGS;

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (1,1);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (2,2);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (3,2);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (4,3);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (4,4);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (5,3);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (5,4);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (6,5);

INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES (7,3);

SET IDENTITY_INSERT [dbo].[users] ON;

INSERT INTO [dbo].[users] ([id],[avatar],[created_at],[email],[full_name],[password],[phone],[status]) VALUES (1,NULL,N'2026-10-16T13:38:24.7374480',N'admin@starshop.local',N'StarShop Admin',N'$2a$10$rNHPWBQLFtIH/uZx05PLwueh8DUTpKsqnk43HjAShusDRc9NXucJy',NULL,N'ACTIVE');

INSERT INTO [dbo].[users] ([id],[avatar],[created_at],[email],[full_name],[password],[phone],[status]) VALUES (2,NULL,N'2026-10-23T23:29:54.5341860',N'manager.thuduc@starshop.local',N'Nguyễn Minh Anh',N'$2a$10$rNHPWBQLFtIH/uZx05PLwueh8DUTpKsqnk43HjAShusDRc9NXucJy',NULL,N'ACTIVE');

INSERT INTO [dbo].[users] ([id],[avatar],[created_at],[email],[full_name],[password],[phone],[status]) VALUES (3,NULL,N'2026-10-23T23:29:54.6836360',N'manager.binhthanh@starshop.local',N'Trần Ngọc Linh',N'$2a$10$rNHPWBQLFtIH/uZx05PLwueh8DUTpKsqnk43HjAShusDRc9NXucJy',NULL,N'ACTIVE');

INSERT INTO [dbo].[users] ([id],[avatar],[created_at],[email],[full_name],[password],[phone],[status]) VALUES (4,NULL,N'2026-10-23T23:29:54.7656380',N'vendor.thuduc@starshop.local',N'Lê Thanh Hà',N'$2a$10$rNHPWBQLFtIH/uZx05PLwueh8DUTpKsqnk43HjAShusDRc9NXucJy',NULL,N'ACTIVE');

INSERT INTO [dbo].[users] ([id],[avatar],[created_at],[email],[full_name],[password],[phone],[status]) VALUES (5,NULL,N'2026-10-23T23:29:54.8818930',N'vendor.binhthanh@starshop.local',N'Phạm Hoàng Nam',N'$2a$10$rNHPWBQLFtIH/uZx05PLwueh8DUTpKsqnk43HjAShusDRc9NXucJy',NULL,N'ACTIVE');

INSERT INTO [dbo].[users] ([id],[avatar],[created_at],[email],[full_name],[password],[phone],[status]) VALUES (6,NULL,N'2026-10-23T23:29:55.0036400',N'shipper@starshop.local',N'Võ Thành Đạt',N'$2a$10$rNHPWBQLFtIH/uZx05PLwueh8DUTpKsqnk43HjAShusDRc9NXucJy',NULL,N'ACTIVE');

INSERT INTO [dbo].[users] ([id],[avatar],[created_at],[email],[full_name],[password],[phone],[status]) VALUES (7,NULL,N'2026-10-23T23:29:55.1278630',N'user@starshop.local',N'Nguyễn Khánh An',N'$2a$10$rNHPWBQLFtIH/uZx05PLwueh8DUTpKsqnk43HjAShusDRc9NXucJy',NULL,N'ACTIVE');

SET IDENTITY_INSERT [dbo].[users] OFF;

DBCC CHECKIDENT (N'[dbo].[users]', RESEED, 8) WITH NO_INFOMSGS;

INSERT INTO [dbo].[vnpay_payment_orders] ([payment_reference],[order_id]) VALUES (N'SSSHARED00000001',19);

INSERT INTO [dbo].[vnpay_payment_orders] ([payment_reference],[order_id]) VALUES (N'SSSHARED00000002',15);

INSERT INTO [dbo].[vnpay_payment_orders] ([payment_reference],[order_id]) VALUES (N'SSSHARED00000003',17);

INSERT INTO [dbo].[vnpay_payment_orders] ([payment_reference],[order_id]) VALUES (N'SSSHARED00000004',16);

INSERT INTO [dbo].[vnpay_payment_orders] ([payment_reference],[order_id]) VALUES (N'SSSHARED00000005',18);

INSERT INTO [dbo].[vnpay_payment_orders] ([payment_reference],[order_id]) VALUES (N'SSSHARED00000006',14);

INSERT INTO [dbo].[vnpay_payments] ([reference],[amount],[client_ip],[created_at],[expires_at],[gateway_transaction_no],[resolved_at],[response_code],[state],[transaction_status],[user_id],[gateway_started_at],[last_checked_at],[merchant_code],[reconciliation_note],[return_received_at],[return_response_code],[return_transaction_status]) VALUES (N'SSSHARED00000001',1700000.00,N'127.0.0.1',N'2026-10-26T18:14:10.1829260',N'2026-10-26T18:29:10.1829260',N'SHARED-SSSHARED00000001',N'2026-10-26T18:15:35.1390560',N'00',N'PAID',N'00',7,N'2026-10-26T18:14:14.7569260',NULL,NULL,N'Bản sao dữ liệu chia sẻ; không dùng đối soát giao dịch gốc.',N'2026-10-26T18:15:35.0934630',N'00',N'00');

INSERT INTO [dbo].[vnpay_payments] ([reference],[amount],[client_ip],[created_at],[expires_at],[gateway_transaction_no],[resolved_at],[response_code],[state],[transaction_status],[user_id],[gateway_started_at],[last_checked_at],[merchant_code],[reconciliation_note],[return_received_at],[return_response_code],[return_transaction_status]) VALUES (N'SSSHARED00000002',1718000.00,N'127.0.0.1',N'2026-10-24T13:20:44.2534850',N'2026-10-24T13:35:44.2534850',N'SHARED-SSSHARED00000002',NULL,NULL,N'PENDING',NULL,7,NULL,N'2026-10-27T03:18:03.2691990',NULL,N'Bản sao dữ liệu chia sẻ; không dùng đối soát giao dịch gốc.',NULL,NULL,NULL);

INSERT INTO [dbo].[vnpay_payments] ([reference],[amount],[client_ip],[created_at],[expires_at],[gateway_transaction_no],[resolved_at],[response_code],[state],[transaction_status],[user_id],[gateway_started_at],[last_checked_at],[merchant_code],[reconciliation_note],[return_received_at],[return_response_code],[return_transaction_status]) VALUES (N'SSSHARED00000003',1718000.00,N'127.0.0.1',N'2026-10-26T14:54:49.4416720',N'2026-10-26T15:09:49.4416720',N'SHARED-SSSHARED00000003',N'2026-10-26T14:58:19.3257710',N'00',N'PAID',N'00',7,NULL,NULL,NULL,N'Bản sao dữ liệu chia sẻ; không dùng đối soát giao dịch gốc.',NULL,NULL,NULL);

INSERT INTO [dbo].[vnpay_payments] ([reference],[amount],[client_ip],[created_at],[expires_at],[gateway_transaction_no],[resolved_at],[response_code],[state],[transaction_status],[user_id],[gateway_started_at],[last_checked_at],[merchant_code],[reconciliation_note],[return_received_at],[return_response_code],[return_transaction_status]) VALUES (N'SSSHARED00000004',3918000.00,N'127.0.0.1',N'2026-10-26T14:44:08.2783360',N'2026-10-26T14:59:08.2783360',N'SHARED-SSSHARED00000004',NULL,NULL,N'PENDING',NULL,1,NULL,N'2026-10-27T03:18:03.2752000',NULL,N'Bản sao dữ liệu chia sẻ; không dùng đối soát giao dịch gốc.',NULL,NULL,NULL);

INSERT INTO [dbo].[vnpay_payments] ([reference],[amount],[client_ip],[created_at],[expires_at],[gateway_transaction_no],[resolved_at],[response_code],[state],[transaction_status],[user_id],[gateway_started_at],[last_checked_at],[merchant_code],[reconciliation_note],[return_received_at],[return_response_code],[return_transaction_status]) VALUES (N'SSSHARED00000005',1718000.00,N'127.0.0.1',N'2026-10-26T15:44:54.6588660',N'2026-10-26T15:59:54.6588660',N'SHARED-SSSHARED00000005',N'2026-10-26T18:09:53.9692330',N'00',N'PAID',N'00',7,N'2026-10-26T15:44:54.6588660',N'2026-10-26T16:23:36.2369900',NULL,N'Bản sao dữ liệu chia sẻ; không dùng đối soát giao dịch gốc.',N'2026-10-26T16:08:57.6866950',N'00',N'00');

INSERT INTO [dbo].[vnpay_payments] ([reference],[amount],[client_ip],[created_at],[expires_at],[gateway_transaction_no],[resolved_at],[response_code],[state],[transaction_status],[user_id],[gateway_started_at],[last_checked_at],[merchant_code],[reconciliation_note],[return_received_at],[return_response_code],[return_transaction_status]) VALUES (N'SSSHARED00000006',1718000.00,N'127.0.0.1',N'2026-10-24T13:11:55.5372430',N'2026-10-24T13:26:55.5372430',N'SHARED-SSSHARED00000006',NULL,NULL,N'PENDING',NULL,7,NULL,N'2026-10-27T03:18:03.2477490',NULL,N'Bản sao dữ liệu chia sẻ; không dùng đối soát giao dịch gốc.',NULL,NULL,NULL);

SET IDENTITY_INSERT [dbo].[wishlists] ON;

INSERT INTO [dbo].[wishlists] ([id],[created_at],[product_id],[user_id]) VALUES (1,N'2026-10-23T23:29:55.9930170',18,7);

INSERT INTO [dbo].[wishlists] ([id],[created_at],[product_id],[user_id]) VALUES (2,N'2026-10-23T23:29:56.3469340',35,7);

SET IDENTITY_INSERT [dbo].[wishlists] OFF;

DBCC CHECKIDENT (N'[dbo].[wishlists]', RESEED, 2) WITH NO_INFOMSGS;

ALTER TABLE [dbo].[addresses] ADD CONSTRAINT [PK__addresse__3213E83F8647FD83] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[cart_items] ADD CONSTRAINT [PK__cart_ite__3213E83F92C0AC88] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[cart_items] ADD CONSTRAINT [UK6oue0maw421roerltnxn16a38] UNIQUE NONCLUSTERED ([cart_id] ASC,[product_id] ASC);

ALTER TABLE [dbo].[carts] ADD CONSTRAINT [PK__carts__3213E83F36ADE10D] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[carts] ADD CONSTRAINT [UK_64t7ox312pqal3p7fg9o503c2] UNIQUE NONCLUSTERED ([user_id] ASC);

ALTER TABLE [dbo].[vnpay_payment_orders] ADD CONSTRAINT [PK__vnpay_pa__BDC3E991903EC4AC] PRIMARY KEY CLUSTERED ([payment_reference] ASC,[order_id] ASC);

ALTER TABLE [dbo].[vnpay_payment_orders] ADD CONSTRAINT [UKinmntacun81oe5d97jtgf819s] UNIQUE NONCLUSTERED ([order_id] ASC);

ALTER TABLE [dbo].[categories] ADD CONSTRAINT [PK__categori__3213E83F474B7EA7] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[categories] ADD CONSTRAINT [UK_t8o6pivur7nn124jehx7cygw5] UNIQUE NONCLUSTERED ([name] ASC);

ALTER TABLE [dbo].[vnpay_payments] ADD CONSTRAINT [PK__vnpay_pa__FD90DA98F0E29024] PRIMARY KEY CLUSTERED ([reference] ASC);

ALTER TABLE [dbo].[vnpay_payments] ADD CONSTRAINT [UK_ipgbjkmt0uuky6eofqyelobo2] UNIQUE NONCLUSTERED ([gateway_transaction_no] ASC);

ALTER TABLE [dbo].[commissions] ADD CONSTRAINT [PK__commissi__3213E83FB3A9ED90] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[notifications] ADD CONSTRAINT [PK__notifica__3213E83FE4E712B1] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[order_details] ADD CONSTRAINT [PK__order_de__3213E83FE7F51DE1] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[orders] ADD CONSTRAINT [PK__orders__3213E83F8155FB32] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[otp_tokens] ADD CONSTRAINT [PK__otp_toke__3213E83F1FB5EDCE] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[product_images] ADD CONSTRAINT [PK__product___3213E83F337DDF0D] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[product_moderations] ADD CONSTRAINT [PK__product___47027DF5585E8134] PRIMARY KEY CLUSTERED ([product_id] ASC);

ALTER TABLE [dbo].[product_views] ADD CONSTRAINT [PK__product___3213E83F46983E51] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[product_views] ADD CONSTRAINT [UKaqhq3flku63x1o57tt91651af] UNIQUE NONCLUSTERED ([user_id] ASC,[product_id] ASC);

ALTER TABLE [dbo].[products] ADD CONSTRAINT [PK__products__3213E83FEBA80624] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[promotions] ADD CONSTRAINT [PK__promotio__3213E83FED7ED1F5] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[promotions] ADD CONSTRAINT [UK_jdho73ymbyu46p2hh562dk4kk] UNIQUE NONCLUSTERED ([code] ASC);

ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [PK__reviews__3213E83FC3594A69] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [UK_pdoxdvhk6k612mlg4cdpr6rgn] UNIQUE NONCLUSTERED ([order_detail_id] ASC);

ALTER TABLE [dbo].[roles] ADD CONSTRAINT [PK__roles__3213E83FB92E3272] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[roles] ADD CONSTRAINT [UK_ofx66keruapi6vyqpv6f2or37] UNIQUE NONCLUSTERED ([name] ASC);

ALTER TABLE [dbo].[shipper_assignments] ADD CONSTRAINT [PK__shipper___3213E83F857C690D] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[shipper_assignments] ADD CONSTRAINT [UK_5wp8368gtcrwql6io1ecbs3xa] UNIQUE NONCLUSTERED ([order_id] ASC);

ALTER TABLE [dbo].[shipping_providers] ADD CONSTRAINT [PK__shipping__3213E83FF9CCD041] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[shipping_providers] ADD CONSTRAINT [UK_k1tpitqeebk5lu1x1m5k87855] UNIQUE NONCLUSTERED ([name] ASC);

ALTER TABLE [dbo].[shops] ADD CONSTRAINT [PK__shops__3213E83F6358BE4C] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[user_roles] ADD CONSTRAINT [PK__user_rol__6EDEA15325C6F777] PRIMARY KEY CLUSTERED ([user_id] ASC,[role_id] ASC);

ALTER TABLE [dbo].[users] ADD CONSTRAINT [PK__users__3213E83F87F93A5E] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[users] ADD CONSTRAINT [UK_6dotkott2kjsp8vw4d0m25fb7] UNIQUE NONCLUSTERED ([email] ASC);

CREATE UNIQUE NONCLUSTERED INDEX [UX_users_phone_present] ON [dbo].[users] ([phone] ASC) WHERE ([phone] IS NOT NULL);

ALTER TABLE [dbo].[wishlists] ADD CONSTRAINT [PK__wishlist__3213E83F768F2147] PRIMARY KEY CLUSTERED ([id] ASC);

ALTER TABLE [dbo].[wishlists] ADD CONSTRAINT [UKht6e6158srxsvjciahp1kjywf] UNIQUE NONCLUSTERED ([user_id] ASC,[product_id] ASC);

ALTER TABLE [dbo].[vnpay_payments] WITH CHECK ADD CONSTRAINT [CK__vnpay_pay__state__51300E55] CHECK ([state]='REVIEW_REQUIRED' OR [state]='FAILED' OR [state]='PAID' OR [state]='PENDING');

ALTER TABLE [dbo].[orders] WITH CHECK ADD CONSTRAINT [CK__orders__payment___571DF1D5] CHECK ([payment_method]='VNPAY' OR [payment_method]='COD');

ALTER TABLE [dbo].[orders] WITH CHECK ADD CONSTRAINT [CK__orders__payment___5812160E] CHECK ([payment_status]='REFUNDED' OR [payment_status]='FAILED' OR [payment_status]='PAID' OR [payment_status]='PENDING');

ALTER TABLE [dbo].[orders] WITH CHECK ADD CONSTRAINT [CK__orders__status__59063A47] CHECK ([status]='DELIVERY_FAILED' OR [status]='REFUNDED' OR [status]='RETURNED' OR [status]='RETURN_REQUESTED' OR [status]='CANCELLED' OR [status]='DELIVERED' OR [status]='SHIPPING' OR [status]='PICKED_UP' OR [status]='ASSIGNED' OR [status]='READY_FOR_PICKUP' OR [status]='CONFIRMED' OR [status]='NEW');

ALTER TABLE [dbo].[products] WITH CHECK ADD CONSTRAINT [CK__products__status__6383C8BA] CHECK ([status]='OUT_OF_STOCK' OR [status]='INACTIVE' OR [status]='ACTIVE');

ALTER TABLE [dbo].[promotions] WITH CHECK ADD CONSTRAINT [CK__promotion__scope__66603565] CHECK ([scope]='SYSTEM' OR [scope]='SHOP');

ALTER TABLE [dbo].[promotions] WITH CHECK ADD CONSTRAINT [CK__promotions__type__6754599E] CHECK ([type]='FREE_SHIPPING' OR [type]='FIXED_AMOUNT' OR [type]='PERCENT');

ALTER TABLE [dbo].[roles] WITH CHECK ADD CONSTRAINT [CK__roles__name__6C190EBB] CHECK ([name]='SHIPPER' OR [name]='ADMIN' OR [name]='MANAGER' OR [name]='VENDOR' OR [name]='USER');

ALTER TABLE [dbo].[shops] WITH CHECK ADD CONSTRAINT [CK__shops__status__72C60C4A] CHECK ([status]='REJECTED' OR [status]='BLOCKED' OR [status]='ACTIVE' OR [status]='PENDING');

ALTER TABLE [dbo].[users] WITH CHECK ADD CONSTRAINT [CK__users__status__778AC167] CHECK ([status]='LOCKED' OR [status]='INACTIVE' OR [status]='ACTIVE');

ALTER TABLE [dbo].[addresses] WITH CHECK ADD CONSTRAINT [FK1fa36y2oqhao3wgg2rw1pi459] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[cart_items] WITH CHECK ADD CONSTRAINT [FK1re40cjegsfvw58xrkdp6bac6] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([id]);

ALTER TABLE [dbo].[promotions] WITH CHECK ADD CONSTRAINT [FK1tmgfukvilb2rx2mp4jum295v] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([id]);

ALTER TABLE [dbo].[orders] WITH CHECK ADD CONSTRAINT [FK21gttsw5evi5bbsvleui69d7r] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([id]);

ALTER TABLE [dbo].[orders] WITH CHECK ADD CONSTRAINT [FK32ql8ubntj5uh44ph9659tiih] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[wishlists] WITH CHECK ADD CONSTRAINT [FK330pyw2el06fn5g28ypyljt16] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[orders] WITH CHECK ADD CONSTRAINT [FK39nrpasj4g5t636f5w5oap2rt] FOREIGN KEY ([shipping_provider_id]) REFERENCES [dbo].[shipping_providers] ([id]);

ALTER TABLE [dbo].[order_details] WITH CHECK ADD CONSTRAINT [FK4q98utpd73imf4yhttm3w0eax] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([id]);

ALTER TABLE [dbo].[shipper_assignments] WITH CHECK ADD CONSTRAINT [FK78rv078v3uhxl07frrbwvomhx] FOREIGN KEY ([shipper_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[products] WITH CHECK ADD CONSTRAINT [FK7kp8sbhxboponhx3lxqtmkcoj] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([id]);

ALTER TABLE [dbo].[shops] WITH CHECK ADD CONSTRAINT [FK89htj0ts213s2cu32x4s8a8ol] FOREIGN KEY ([manager_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[product_views] WITH CHECK ADD CONSTRAINT [FK9wal1ra5egnm0t01wworru283] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[notifications] WITH CHECK ADD CONSTRAINT [FK9y21adhxn0ayjhfocscqox7bh] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[reviews] WITH CHECK ADD CONSTRAINT [FKa6fy7npl2qi9nu2fo9igepm9k] FOREIGN KEY ([order_detail_id]) REFERENCES [dbo].[order_details] ([id]);

ALTER TABLE [dbo].[commissions] WITH CHECK ADD CONSTRAINT [FKachi3xm2onm9x8222ttitnc6] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([id]);

ALTER TABLE [dbo].[carts] WITH CHECK ADD CONSTRAINT [FKb5o626f86h46m4s7ms6ginnop] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[vnpay_payment_orders] WITH CHECK ADD CONSTRAINT [FKbbhyy4wv1sh0hy5cwy9k46h39] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([id]);

ALTER TABLE [dbo].[product_views] WITH CHECK ADD CONSTRAINT [FKc93jen7gg3twjaatbhfrwljmk] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([id]);

ALTER TABLE [dbo].[reviews] WITH CHECK ADD CONSTRAINT [FKcgy7qjc1r99dp117y9en6lxye] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[vnpay_payments] WITH CHECK ADD CONSTRAINT [FKckpr9iign2y4uyt6r3vm99an5] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[shipper_assignments] WITH CHECK ADD CONSTRAINT [FKg26bbrjw4s762x8s96n2dfiv4] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([id]);

ALTER TABLE [dbo].[user_roles] WITH CHECK ADD CONSTRAINT [FKh8ciramu9cc9q3qcqiv4ue8a6] FOREIGN KEY ([role_id]) REFERENCES [dbo].[roles] ([id]);

ALTER TABLE [dbo].[user_roles] WITH CHECK ADD CONSTRAINT [FKhfh9dx7w3ubf1co1vdev94g3f] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[otp_tokens] WITH CHECK ADD CONSTRAINT [FKjyk2kyj8ul2bjqjpentlrpyi1] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

ALTER TABLE [dbo].[order_details] WITH CHECK ADD CONSTRAINT [FKjyu2qbqt8gnvno9oe9j2s2ldk] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([id]);

ALTER TABLE [dbo].[wishlists] WITH CHECK ADD CONSTRAINT [FKl7ao98u2bm8nijc1rv4jobcrx] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([id]);

ALTER TABLE [dbo].[vnpay_payment_orders] WITH CHECK ADD CONSTRAINT [FKntsayh6ghln9kmbytw296gkn7] FOREIGN KEY ([payment_reference]) REFERENCES [dbo].[vnpay_payments] ([reference]);

ALTER TABLE [dbo].[products] WITH CHECK ADD CONSTRAINT [FKog2rp4qthbtt2lfyhfo32lsw9] FOREIGN KEY ([category_id]) REFERENCES [dbo].[categories] ([id]);

ALTER TABLE [dbo].[cart_items] WITH CHECK ADD CONSTRAINT [FKpcttvuq4mxppo8sxggjtn5i2c] FOREIGN KEY ([cart_id]) REFERENCES [dbo].[carts] ([id]);

ALTER TABLE [dbo].[reviews] WITH CHECK ADD CONSTRAINT [FKpl51cejpw4gy5swfar8br9ngi] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([id]);

ALTER TABLE [dbo].[product_images] WITH CHECK ADD CONSTRAINT [FKqnq71xsohugpqwf3c9gxmsuy] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([id]);

ALTER TABLE [dbo].[shops] WITH CHECK ADD CONSTRAINT [FKrduswa89ayj0poad3l70nag19] FOREIGN KEY ([owner_id]) REFERENCES [dbo].[users] ([id]);

COMMIT TRANSACTION;

END TRY

BEGIN CATCH

IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;

THROW;

END CATCH;

SELECT N'StarShopShared: nhập dữ liệu thành công' AS result;

GO
