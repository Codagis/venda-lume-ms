-- Seções de mesas do restaurante
CREATE TABLE table_section (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_table_section PRIMARY KEY (id),
    CONSTRAINT fk_table_section_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_table_section_tenant ON table_section(tenant_id);
CREATE INDEX idx_table_section_tenant_order ON table_section(tenant_id, display_order);

COMMENT ON TABLE table_section IS 'Seções de mesas (áreas do restaurante)';

-- Mesas do restaurante
CREATE TABLE restaurant_table (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    section_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 2,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_restaurant_table PRIMARY KEY (id),
    CONSTRAINT fk_restaurant_table_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_restaurant_table_section FOREIGN KEY (section_id) REFERENCES table_section(id) ON DELETE CASCADE
);

CREATE INDEX idx_restaurant_table_tenant ON restaurant_table(tenant_id);
CREATE INDEX idx_restaurant_table_section ON restaurant_table(section_id);
CREATE INDEX idx_restaurant_table_tenant_status ON restaurant_table(tenant_id, status);
CREATE INDEX idx_restaurant_table_tenant_active ON restaurant_table(tenant_id, active);

COMMENT ON TABLE restaurant_table IS 'Mesas do restaurante';

-- Reservas de mesas
CREATE TABLE reservation (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    table_id UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20),
    customer_email VARCHAR(255),
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    number_of_guests INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_reservation PRIMARY KEY (id),
    CONSTRAINT fk_reservation_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_table FOREIGN KEY (table_id) REFERENCES restaurant_table(id) ON DELETE CASCADE
);

CREATE INDEX idx_reservation_tenant ON reservation(tenant_id);
CREATE INDEX idx_reservation_table ON reservation(table_id);
CREATE INDEX idx_reservation_tenant_status ON reservation(tenant_id, status);
CREATE INDEX idx_reservation_tenant_scheduled ON reservation(tenant_id, scheduled_at);

COMMENT ON TABLE reservation IS 'Reservas de mesas';

-- Permissões e módulo são criados pelo DataLoader (perfil dev) ou podem ser adicionados manualmente em Configurações > Módulos
