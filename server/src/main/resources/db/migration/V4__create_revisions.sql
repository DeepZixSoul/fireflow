CREATE TABLE revisions (
  id BIGINT PRIMARY KEY,
  group_id BIGINT NOT NULL REFERENCES pressure_groups(id) ON DELETE CASCADE,
  "date" BIGINT NOT NULL,
  technician_name VARCHAR(255),
  notes TEXT,
  checklist_results TEXT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);

CREATE INDEX idx_revisions_group_id ON revisions(group_id);
CREATE INDEX idx_revisions_date ON revisions("date");
