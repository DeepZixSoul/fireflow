CREATE TABLE clients (
  id BIGINT PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL,
  cif VARCHAR(20),
  address TEXT,
  province VARCHAR(100),
  contact_person VARCHAR(255),
  phone VARCHAR(30),
  email VARCHAR(255),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  notes TEXT,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);

CREATE INDEX idx_clients_cif ON clients(cif);
CREATE INDEX idx_clients_name ON clients("name");
CREATE INDEX idx_clients_is_active ON clients(is_active);
