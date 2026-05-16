-- ============================================================
-- ProcureGov — Complete Schema & Seed Data
-- Author: Neo Leseme 
-- Database: ProcureGovDB
-- 
-- This single file does everything:
--   1. Drops existing database (if any)
--   2. Creates all tables with correct data types & constraints
--   3. Inserts seed data:
--      - 2 Procurement Officer accounts
--      - 2 Evaluation Committee Member accounts
--      - 3 Supplier accounts
--      - 2 published tenders with 3 bids each
-- 
-- All user passwords: Password123!
-- SHA-256 hash: a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea
-- ============================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
SET time_zone = "+00:00";
SET NAMES utf8mb4;

-- Drop and recreate database
DROP DATABASE IF EXISTS ProcureGovDB;
CREATE DATABASE ProcureGovDB
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE ProcureGovDB;

-- ══════════════════════════════════════════════════════════════
-- TABLE CREATION
-- ══════════════════════════════════════════════════════════════

-- ── users ─────────────────────────────────────────────────────
CREATE TABLE users (
  user_id    INT          NOT NULL AUTO_INCREMENT,
  full_name  VARCHAR(150) NOT NULL,
  email      VARCHAR(200) NOT NULL,
  password_hash VARCHAR(64) NOT NULL,
  role       ENUM('SUPPLIER','PROCUREMENT_OFFICER','EVAL_COMMITTEE') NOT NULL,
  is_locked  TINYINT(1)   NOT NULL DEFAULT 0,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE KEY email (email),
  KEY idx_users_email (email),
  KEY idx_users_role  (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── suppliers ─────────────────────────────────────────────────
CREATE TABLE suppliers (
  supplier_id      INT          NOT NULL AUTO_INCREMENT,
  user_id          INT          NOT NULL,
  company_name     VARCHAR(200) NOT NULL,
  registration_no  VARCHAR(50)  NOT NULL,
  physical_address VARCHAR(300) NOT NULL,
  contact_number   VARCHAR(20)  NOT NULL,
  PRIMARY KEY (supplier_id),
  UNIQUE KEY user_id (user_id),
  UNIQUE KEY registration_no (registration_no),
  CONSTRAINT fk_supplier_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── tender_reference_seq ──────────────────────────────────────
CREATE TABLE tender_reference_seq (
  seq_year  SMALLINT NOT NULL,
  seq_value INT      NOT NULL DEFAULT 0,
  PRIMARY KEY (seq_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── tenders ───────────────────────────────────────────────────
CREATE TABLE tenders (
  tender_id        INT           NOT NULL AUTO_INCREMENT,
  reference_no     VARCHAR(20)   NOT NULL,
  title            VARCHAR(300)  NOT NULL,
  category         ENUM('CONSTRUCTION','ROADS','ELECTRICAL','PLUMBING','GENERAL_SERVICES') NOT NULL,
  description      TEXT          NOT NULL,
  estimated_value  DECIMAL(15,2) NOT NULL,
  closing_datetime DATETIME      NOT NULL,
  status           ENUM('DRAFT','OPEN','CLOSED','UNDER_EVALUATION','EVALUATED','AWARDED') NOT NULL DEFAULT 'DRAFT',
  notice_file_path VARCHAR(500)  DEFAULT NULL,
  created_by       INT           NOT NULL,
  created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (tender_id),
  UNIQUE KEY reference_no (reference_no),
  KEY fk_tender_officer (created_by),
  KEY idx_tender_status   (status),
  KEY idx_tender_category (category),
  KEY idx_tender_closing  (closing_datetime),
  CONSTRAINT fk_tender_officer FOREIGN KEY (created_by) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── bids ──────────────────────────────────────────────────────
CREATE TABLE bids (
  bid_id               INT           NOT NULL AUTO_INCREMENT,
  tender_id            INT           NOT NULL,
  supplier_id          INT           NOT NULL,
  bid_amount           DECIMAL(15,2) NOT NULL,
  compliance_statement TEXT          NOT NULL,
  delivery_days        INT           NOT NULL,
  document_path        VARCHAR(500)  DEFAULT NULL,
  submitted_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status               ENUM('SUBMITTED','UNDER_EVALUATION','AWARDED','NOT_AWARDED') NOT NULL DEFAULT 'SUBMITTED',
  PRIMARY KEY (bid_id),
  UNIQUE KEY uq_bid_per_tender (tender_id, supplier_id),
  KEY idx_bid_tender   (tender_id),
  KEY idx_bid_supplier (supplier_id),
  CONSTRAINT fk_bid_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id),
  CONSTRAINT fk_bid_tender   FOREIGN KEY (tender_id)   REFERENCES tenders  (tender_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── evaluators ────────────────────────────────────────────────
CREATE TABLE evaluators (
  evaluator_id INT      NOT NULL AUTO_INCREMENT,
  tender_id    INT      NOT NULL,
  user_id      INT      NOT NULL,
  assigned_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (evaluator_id),
  UNIQUE KEY uq_evaluator (tender_id, user_id),
  KEY fk_eval_user (user_id),
  CONSTRAINT fk_eval_tender FOREIGN KEY (tender_id) REFERENCES tenders (tender_id) ON DELETE CASCADE,
  CONSTRAINT fk_eval_user   FOREIGN KEY (user_id)   REFERENCES users   (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── evaluation_scores ─────────────────────────────────────────
CREATE TABLE evaluation_scores (
  score_id           INT          NOT NULL AUTO_INCREMENT,
  bid_id             INT          NOT NULL,
  evaluator_user_id  INT          NOT NULL,
  technical_score    DECIMAL(5,2) NOT NULL,
  price_score        DECIMAL(5,2) NOT NULL,
  timeline_score     DECIMAL(5,2) NOT NULL,
  weighted_total     DECIMAL(5,2) NOT NULL,
  scored_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (score_id),
  UNIQUE KEY uq_score_per_evaluator (bid_id, evaluator_user_id),
  KEY fk_score_user (evaluator_user_id),
  KEY idx_score_bid (bid_id),
  CONSTRAINT fk_score_bid  FOREIGN KEY (bid_id)            REFERENCES bids  (bid_id) ON DELETE CASCADE,
  CONSTRAINT fk_score_user FOREIGN KEY (evaluator_user_id) REFERENCES users  (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── award_notices ─────────────────────────────────────────────
CREATE TABLE award_notices (
  award_id       INT           NOT NULL AUTO_INCREMENT,
  tender_id      INT           NOT NULL,
  winning_bid_id INT           NOT NULL,
  awarded_by     INT           NOT NULL,
  awarded_value  DECIMAL(15,2) NOT NULL,
  justification  TEXT          NOT NULL,
  award_date     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (award_id),
  UNIQUE KEY tender_id (tender_id),
  KEY fk_award_bid     (winning_bid_id),
  KEY fk_award_officer (awarded_by),
  CONSTRAINT fk_award_bid     FOREIGN KEY (winning_bid_id) REFERENCES bids   (bid_id),
  CONSTRAINT fk_award_officer FOREIGN KEY (awarded_by)     REFERENCES users   (user_id),
  CONSTRAINT fk_award_tender  FOREIGN KEY (tender_id)      REFERENCES tenders (tender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── login_attempts ────────────────────────────────────────────
CREATE TABLE login_attempts (
  attempt_id    INT      NOT NULL AUTO_INCREMENT,
  user_id       INT      NOT NULL,
  attempt_count TINYINT  NOT NULL DEFAULT 0,
  last_attempt  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (attempt_id),
  UNIQUE KEY uq_attempt_user (user_id),
  CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── password_reset_codes ──────────────────────────────────────
CREATE TABLE password_reset_codes (
  reset_id    INT          NOT NULL AUTO_INCREMENT,
  user_id     INT          NOT NULL,
  code        CHAR(6)      NOT NULL,
  expires_at  DATETIME     NOT NULL,
  used        TINYINT(1)   NOT NULL DEFAULT 0,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (reset_id),
  KEY idx_reset_user (user_id),
  KEY idx_reset_code (code, user_id),
  CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ══════════════════════════════════════════════════════════════
-- SEED DATA
-- ══════════════════════════════════════════════════════════════

-- ── Reference sequence ────────────────────────────────────────
INSERT INTO tender_reference_seq (seq_year, seq_value) VALUES (2026, 0);

-- ── Users ─────────────────────────────────────────────────────
-- Password for all accounts: Password123!
-- SHA-256 hash: a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea

INSERT INTO users (user_id, full_name, email, password_hash, role) VALUES
-- 2 Procurement Officers
(1, 'Thabiso Mokocho',  'thabiso@mpw.ls',   'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', 'PROCUREMENT_OFFICER'),
(2, 'Lineo Ntšekhe',    'lineo@mpw.ls',     'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', 'PROCUREMENT_OFFICER'),
-- 2 Evaluation Committee Members
(3, 'Mpho Lesitsi',     'mpho@mpw.ls',      'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', 'EVAL_COMMITTEE'),
(4, 'Palesa Mohapi',    'palesa@mpw.ls',    'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', 'EVAL_COMMITTEE'),
-- 3 Suppliers
(5, 'Neo Leseme',       'neo@build.ls',     'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', 'SUPPLIER'),
(6, 'Tse-Tse Builders', 'tse@build.ls',     'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', 'SUPPLIER'),
(7, 'Maseru Roads Ltd', 'maseru@roads.ls',  'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', 'SUPPLIER');

-- ── Suppliers ─────────────────────────────────────────────────
INSERT INTO suppliers (supplier_id, user_id, company_name, registration_no, physical_address, contact_number) VALUES
(1, 5, 'Neo Leseme Construction',    'SUP-2026-001', 'Maseru, Lesotho',  '+26622001001'),
(2, 6, 'Tse-Tse Builders Ltd',       'SUP-2026-002', 'Leribe, Lesotho',  '+26622002002'),
(3, 7, 'Maseru Roads & Civils Ltd',  'SUP-2026-003', 'Berea, Lesotho',   '+26622003003');

-- ── Login attempts (clean start) ──────────────────────────────
INSERT INTO login_attempts (user_id, attempt_count) VALUES
(1, 0), (2, 0), (3, 0), (4, 0), (5, 0), (6, 0), (7, 0);

-- ── Tenders (2 published, OPEN status, with future closing dates) ──
INSERT INTO tenders (tender_id, reference_no, title, category, description, estimated_value, closing_datetime, status, created_by) VALUES
(1, 'MPW-2026-0001', 'Maseru-Kingway Road Rehabilitation', 'ROADS',
    'Full rehabilitation of the main arterial road from Maseru city centre through Kingway to the industrial area. Scope includes resurfacing, drainage improvements, new street lighting, and pedestrian walkways. Contractors must have proven experience in large-scale road infrastructure projects.',
    15000000.00, '2027-01-01 12:00:00', 'OPEN', 1),
(2, 'MPW-2026-0002', 'National Hospital Solar Power Installation', 'ELECTRICAL',
    'Design, supply and installation of a 500 kW grid-tied solar photovoltaic system at the National Referral Hospital. The project includes battery storage, inverter system, and integration with existing backup generators. Bidders must be accredited solar installers with at least 5 years experience.',
    8500000.00, '2027-03-15 12:00:00', 'OPEN', 2);

-- ── Bids (3 bids per tender, 6 total) ─────────────────────────
-- Tender 1 bids (suppliers 1, 2, 3)
INSERT INTO bids (bid_id, tender_id, supplier_id, bid_amount, compliance_statement, delivery_days, status) VALUES
(1, 1, 1, 14200000.00, 'We confirm full compliance with the tender specifications including drainage, lighting and pedestrian walkways. Our team has completed 12 similar road projects across Lesotho. We hold valid MOWP registration and ISO 9001 certification. Equipment and materials are ready to deploy within one week of award.', 180, 'SUBMITTED'),
(2, 1, 2, 15500000.00, 'Tse-Tse Builders confirms compliance with all technical requirements stated in the tender. We propose to use locally sourced aggregates and bitumen where possible. Our project management team has extensive experience in road construction throughout Southern Africa. We accept all terms and conditions.', 210, 'SUBMITTED'),
(3, 1, 3, 13800000.00, 'Maseru Roads & Civils Ltd hereby confirms full compliance with the tender documents. We offer a value-engineered solution that maintains all quality standards while optimising material usage. Our fleet of modern road-building equipment is available immediately. We have completed the Maseru-Mafeteng highway project on time and within budget.', 195, 'SUBMITTED');

-- Tender 2 bids (suppliers 1, 2, 3)
INSERT INTO bids (bid_id, tender_id, supplier_id, bid_amount, compliance_statement, delivery_days, status) VALUES
(4, 2, 1, 8100000.00, 'We confirm technical compliance with the solar installation specification. Our team of certified solar engineers will deliver a turnkey solution including all necessary grid interconnection approvals. We have installed over 2 MW of solar capacity across Lesotho and South Africa in the past three years.', 120, 'SUBMITTED'),
(5, 2, 2, 8600000.00, 'Tse-Tse Builders confirms compliance with all electrical and solar specification requirements. We will partner with SunPower Solutions (Pty) Ltd for specialist solar components. Our proposal includes a 5-year maintenance plan at no additional cost. All work will be guaranteed for the full warranty period.', 150, 'SUBMITTED'),
(6, 2, 3, 7900000.00, 'Maseru Roads & Civils confirms full compliance with the tender. We have a dedicated renewable energy division staffed by qualified electrical engineers. Our proposal uses Tier-1 solar panels with 25-year performance warranty. We commit to completing the installation within the specified timeline including all testing and commissioning.', 135, 'SUBMITTED');

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
