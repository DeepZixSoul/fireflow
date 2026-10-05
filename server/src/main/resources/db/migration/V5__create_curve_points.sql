CREATE TABLE curve_points (
  id BIGINT PRIMARY KEY,
  revision_id BIGINT NOT NULL REFERENCES revisions(id) ON DELETE CASCADE,
  motor_id BIGINT,
  flow DOUBLE PRECISION,
  pressure DOUBLE PRECISION,
  order_index INTEGER,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);

CREATE INDEX idx_curve_points_revision_id ON curve_points(revision_id);
CREATE INDEX idx_curve_points_motor_id ON curve_points(motor_id);
