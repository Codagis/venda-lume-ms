-- Commo - Schema Inicial do Banco de Dados
-- Criação de todas as tabelas do sistema

-- Tabela: users
CREATE TABLE users (
    id UUID NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    cpf VARCHAR(14),
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    phone_verified BOOLEAN NOT NULL DEFAULT false,
    role VARCHAR(30) NOT NULL,
    tenant_id UUID,
    timezone VARCHAR(50) DEFAULT 'America/Sao_Paulo',
    locale VARCHAR(10) DEFAULT 'pt_BR',
    last_login_at TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT false,
    two_factor_secret VARCHAR(255),
    refresh_token_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_cpf ON users(cpf);
CREATE INDEX idx_user_tenant_active ON users(tenant_id, active);
CREATE INDEX idx_user_tenant_username ON users(tenant_id, username);
CREATE INDEX idx_user_active ON users(active);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_last_login ON users(last_login_at);
CREATE INDEX idx_user_locked_until ON users(locked_until);

COMMENT ON TABLE users IS 'Usuários do sistema Commo';
COMMENT ON COLUMN users.id IS 'Identificador único do usuário';
COMMENT ON COLUMN users.username IS 'Nome de usuário para login';
COMMENT ON COLUMN users.password_hash IS 'Senha hasheada com BCrypt';
COMMENT ON COLUMN users.email IS 'Endereço de e-mail';
COMMENT ON COLUMN users.full_name IS 'Nome completo';
COMMENT ON COLUMN users.role IS 'Papel de acesso (ADMIN, USER, etc)';
COMMENT ON COLUMN users.created_at IS 'Data de criação';
COMMENT ON COLUMN users.updated_at IS 'Data da última atualização';
COMMENT ON COLUMN users.version IS 'Controle de concorrência otimista';

-- Tabela: products
CREATE TABLE products (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    sku VARCHAR(50) NOT NULL,
    barcode VARCHAR(20),
    internal_code VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    short_description VARCHAR(500),
    description TEXT,
    unit_price DECIMAL(19, 4) NOT NULL,
    cost_price DECIMAL(19, 4),
    discount_price DECIMAL(19, 4),
    discount_start_at TIMESTAMP,
    discount_end_at TIMESTAMP,
    tax_rate DECIMAL(5, 2),
    unit_of_measure VARCHAR(10) NOT NULL DEFAULT 'UN',
    sell_by_weight BOOLEAN NOT NULL DEFAULT false,
    track_stock BOOLEAN NOT NULL DEFAULT false,
    stock_quantity DECIMAL(19, 4),
    min_stock DECIMAL(19, 4),
    allow_negative_stock BOOLEAN NOT NULL DEFAULT false,
    category_id UUID,
    brand VARCHAR(100),
    ncm VARCHAR(10),
    cest VARCHAR(9),
    weight DECIMAL(10, 4),
    width DECIMAL(10, 2),
    height DECIMAL(10, 2),
    depth DECIMAL(10, 2),
    preparation_time_minutes INTEGER,
    serve_size VARCHAR(50),
    calories INTEGER,
    ingredients TEXT,
    allergens VARCHAR(500),
    nutritional_info TEXT,
    min_order_quantity DECIMAL(10, 4),
    max_order_quantity DECIMAL(10, 4),
    sell_multiple DECIMAL(10, 4),
    active BOOLEAN NOT NULL DEFAULT true,
    available_for_sale BOOLEAN NOT NULL DEFAULT true,
    available_for_delivery BOOLEAN NOT NULL DEFAULT true,
    featured BOOLEAN NOT NULL DEFAULT false,
    is_composite BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER,
    image_url VARCHAR(500),
    image_urls TEXT,
    video_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_product_tenant_sku UNIQUE (tenant_id, sku)
);

CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_barcode ON products(barcode);
CREATE INDEX idx_product_tenant_active ON products(tenant_id, active);
CREATE INDEX idx_product_tenant_sku ON products(tenant_id, sku);
CREATE INDEX idx_product_tenant_name ON products(tenant_id, name);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_active ON products(active);
CREATE INDEX idx_product_available_sale ON products(available_for_sale);
CREATE INDEX idx_product_featured ON products(featured);
CREATE INDEX idx_product_display_order ON products(display_order);
CREATE INDEX idx_product_created_at ON products(created_at);
CREATE INDEX idx_product_brand ON products(brand);

COMMENT ON TABLE products IS 'Produtos do sistema Commo';
COMMENT ON COLUMN products.sku IS 'Código interno do produto';
COMMENT ON COLUMN products.unit_price IS 'Preço unitário de venda';
COMMENT ON COLUMN products.unit_of_measure IS 'Unidade (UN, KG, LT, etc)';

-- Tabela: sales
CREATE TABLE sales (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    sale_number VARCHAR(20) NOT NULL,
    sale_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sale_type VARCHAR(20) NOT NULL,
    customer_id UUID,
    customer_name VARCHAR(150),
    customer_document VARCHAR(18),
    customer_phone VARCHAR(20),
    customer_email VARCHAR(255),
    seller_id UUID NOT NULL,
    register_id VARCHAR(50),
    subtotal DECIMAL(19, 4) NOT NULL,
    discount_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    discount_percent DECIMAL(5, 2),
    tax_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(19, 4),
    total DECIMAL(19, 4) NOT NULL,
    amount_paid DECIMAL(19, 4),
    change_amount DECIMAL(19, 4),
    payment_method VARCHAR(30),
    delivery_address TEXT,
    delivery_complement VARCHAR(255),
    delivery_zip_code VARCHAR(10),
    delivery_neighborhood VARCHAR(100),
    delivery_city VARCHAR(100),
    delivery_state VARCHAR(2),
    expected_delivery_at TIMESTAMP,
    delivered_at TIMESTAMP,
    notes TEXT,
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    cancelled_by UUID,
    invoice_key VARCHAR(50),
    invoice_number VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_sales PRIMARY KEY (id),
    CONSTRAINT uk_sale_tenant_number UNIQUE (tenant_id, sale_number),
    CONSTRAINT fk_sale_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE INDEX idx_sale_tenant_date ON sales(tenant_id, sale_date);
CREATE INDEX idx_sale_tenant_status ON sales(tenant_id, status);
CREATE INDEX idx_sale_tenant_number ON sales(tenant_id, sale_number);
CREATE INDEX idx_sale_seller ON sales(seller_id);
CREATE INDEX idx_sale_customer ON sales(customer_id);
CREATE INDEX idx_sale_created_at ON sales(created_at);

COMMENT ON TABLE sales IS 'Vendas do sistema Commo';
COMMENT ON COLUMN sales.status IS 'DRAFT, OPEN, PAID, COMPLETED, CANCELLED, etc';
COMMENT ON COLUMN sales.sale_type IS 'PDV, DELIVERY, TAKEAWAY, ONLINE, etc';

-- Tabela: sale_items
CREATE TABLE sale_items (
    id UUID NOT NULL,
    sale_id UUID NOT NULL,
    product_id UUID,
    item_order INTEGER NOT NULL DEFAULT 0,
    quantity DECIMAL(19, 4) NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    unit_of_measure VARCHAR(10),
    discount_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    total DECIMAL(19, 4) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(50),
    observations VARCHAR(500),
    CONSTRAINT pk_sale_items PRIMARY KEY (id),
    CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_sale_item_sale ON sale_items(sale_id);
CREATE INDEX idx_sale_item_product ON sale_items(product_id);

COMMENT ON TABLE sale_items IS 'Itens das vendas';
COMMENT ON COLUMN sale_items.product_name IS 'Nome do produto no momento da venda';

-- Tabela: deliveries
CREATE TABLE deliveries (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    delivery_number VARCHAR(20) NOT NULL,
    sale_id UUID NOT NULL,
    delivery_person_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(10) DEFAULT 'NORMAL',
    recipient_name VARCHAR(150) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    complement VARCHAR(255),
    zip_code VARCHAR(10),
    neighborhood VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(2),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    instructions TEXT,
    scheduled_at TIMESTAMP,
    accepted_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    departed_at TIMESTAMP,
    arrived_at TIMESTAMP,
    delivered_at TIMESTAMP,
    estimated_distance_km DECIMAL(8, 2),
    actual_distance_km DECIMAL(8, 2),
    estimated_duration_minutes INTEGER,
    actual_duration_minutes INTEGER,
    delivery_fee DECIMAL(19, 4),
    tip_amount DECIMAL(19, 4),
    proof_of_delivery_url VARCHAR(500),
    received_by VARCHAR(150),
    delivery_notes TEXT,
    failure_reason VARCHAR(500),
    return_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    cancelled_by UUID,
    attempt_count INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_deliveries PRIMARY KEY (id),
    CONSTRAINT uk_delivery_tenant_number UNIQUE (tenant_id, delivery_number),
    CONSTRAINT fk_delivery_sale FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT fk_delivery_person FOREIGN KEY (delivery_person_id) REFERENCES users(id)
);

CREATE INDEX idx_delivery_tenant_date ON deliveries(tenant_id, created_at);
CREATE INDEX idx_delivery_tenant_status ON deliveries(tenant_id, status);
CREATE INDEX idx_delivery_tenant_number ON deliveries(tenant_id, delivery_number);
CREATE INDEX idx_delivery_sale ON deliveries(sale_id);
CREATE INDEX idx_delivery_person ON deliveries(delivery_person_id);
CREATE INDEX idx_delivery_scheduled ON deliveries(scheduled_at);
CREATE INDEX idx_delivery_delivered_at ON deliveries(delivered_at);

COMMENT ON TABLE deliveries IS 'Entregas do sistema Commo';
COMMENT ON COLUMN deliveries.status IS 'PENDING, ASSIGNED, DELIVERED, etc';
