CREATE SCHEMA IF NOT EXISTS chat_notification_schema;
CREATE TABLE IF NOT EXISTS chat_notification_schema.conversations (
 id UUID PRIMARY KEY,
 participant_a UUID NOT NULL,
 participant_b UUID NOT NULL,
 project_id UUID,
 created_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS chat_notification_schema.conversation_participants (
 id UUID PRIMARY KEY,
 conversation_id UUID NOT NULL,
 user_id UUID NOT NULL
);
CREATE TABLE IF NOT EXISTS chat_notification_schema.messages (
 id UUID PRIMARY KEY,
 conversation_id UUID NOT NULL,
 sender_id UUID NOT NULL,
 content TEXT NOT NULL,
 sent_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS chat_notification_schema.notifications (
 id UUID PRIMARY KEY,
 recipient_id UUID NOT NULL,
 type VARCHAR(120) NOT NULL,
 title VARCHAR(200) NOT NULL,
 message TEXT NOT NULL,
 resource_type VARCHAR(80),
 resource_id UUID,
 read BOOLEAN NOT NULL,
 created_at TIMESTAMP NOT NULL,
 read_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS chat_notification_schema.user_reports (
 id UUID PRIMARY KEY,
 reporter_id UUID NOT NULL,
 reported_user_id UUID NOT NULL,
 reason VARCHAR(120) NOT NULL,
 description TEXT NOT NULL,
 status VARCHAR(40) NOT NULL,
 created_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS chat_notification_schema.support_tickets (
 id UUID PRIMARY KEY,
 user_id UUID NOT NULL,
 subject VARCHAR(200) NOT NULL,
 description TEXT NOT NULL,
 status VARCHAR(40) NOT NULL,
 created_at TIMESTAMP NOT NULL
);
