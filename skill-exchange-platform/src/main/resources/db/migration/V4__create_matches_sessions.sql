CREATE TYPE match_status AS ENUM ('PENDING','ACCEPTED','REJECTED','CANCELLED');
CREATE TYPE session_status AS ENUM ('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW');
CREATE TABLE match_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  target_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  teach_skill_id UUID NOT NULL REFERENCES skills(id),
  learn_skill_id UUID NOT NULL REFERENCES skills(id),
  status match_status NOT NULL DEFAULT 'PENDING',
  message TEXT,
  compatibility_score INT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  CONSTRAINT chk_match_not_self CHECK (requester_id <> target_id)
);
CREATE TABLE sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  match_id UUID NOT NULL REFERENCES match_requests(id) ON DELETE CASCADE,
  teacher_id UUID NOT NULL REFERENCES users(id),
  learner_id UUID NOT NULL REFERENCES users(id),
  skill_id UUID NOT NULL REFERENCES skills(id),
  scheduled_at TIMESTAMP NOT NULL,
  duration_minutes INT NOT NULL,
  meeting_link VARCHAR(500),
  status session_status NOT NULL DEFAULT 'SCHEDULED',
  notes TEXT,
  reminder_sent BOOLEAN DEFAULT false,
  rating_prompt_sent BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  CONSTRAINT chk_session_duration CHECK (duration_minutes BETWEEN 15 AND 480)
);
CREATE TABLE messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  receiver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  session_id UUID REFERENCES sessions(id) ON DELETE SET NULL,
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT false,
  sent_at TIMESTAMP DEFAULT NOW(),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_match_requests_requester_id ON match_requests(requester_id);
CREATE INDEX idx_match_requests_target_id ON match_requests(target_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX idx_sessions_teacher_id ON sessions(teacher_id);
CREATE INDEX idx_sessions_learner_id ON sessions(learner_id);
CREATE INDEX idx_sessions_status_scheduled_at ON sessions(status, scheduled_at);
