CREATE TABLE pressure_groups (
  id BIGINT PRIMARY KEY,
  client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
  brand VARCHAR(100),
  model VARCHAR(100),
  serial_number VARCHAR(100),
  pump_number VARCHAR(50),
  manufacturer VARCHAR(100),
  "power" VARCHAR(50),
  installation_date BIGINT,
  maintenance_date BIGINT,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);

CREATE INDEX idx_pressure_groups_client_id ON pressure_groups(client_id);
CREATE INDEX idx_pressure_groups_serial_number ON pressure_groups(serial_number);
CREATE INDEX idx_pressure_groups_is_active ON pressure_groups(is_active);
