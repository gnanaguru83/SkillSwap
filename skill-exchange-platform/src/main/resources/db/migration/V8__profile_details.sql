-- Headline ("what I can help with") and languages spoken
ALTER TABLE users ADD COLUMN headline VARCHAR(255);
ALTER TABLE users ADD COLUMN languages VARCHAR(500);

-- Optionally link a certification to a skill it backs
ALTER TABLE user_certifications ADD COLUMN skill_id UUID REFERENCES skills(id) ON DELETE SET NULL;

CREATE TABLE user_education (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  institution VARCHAR(255) NOT NULL,
  degree VARCHAR(255),
  field_of_study VARCHAR(255),
  start_year INTEGER,
  end_year INTEGER,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_user_education_user_id ON user_education(user_id);

CREATE TABLE user_experience (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  company VARCHAR(255) NOT NULL,
  title VARCHAR(255),
  start_year INTEGER,
  end_year INTEGER,
  description VARCHAR(1000),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_user_experience_user_id ON user_experience(user_id);
