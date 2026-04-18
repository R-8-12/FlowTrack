# FlowTrack Documentation Index

Use `README.md` as the canonical start point.

## Core Docs

- `README.md` - Primary setup and configuration guide (all IDEs)
- `SETUP_GUIDE.md` - Extended setup notes
- `TROUBLESHOOTING.md` - Common failures and fixes
- `DATABASE_SCHEMA.md` - Database schema reference
- `SECURITY_RBAC.md` - Role and access model

## Database and Migration

- `MYSQL_SETUP_GUIDE.md` - MySQL setup walkthrough
- `MYSQL_MIGRATION_README.md` - Migration-related notes
- `baseline_schema.sql` - Baseline schema SQL
- `setup-mysql.sql` - DB setup SQL
- `update-to-rupees-and-history.sql` - Feature-specific schema/data update

## Testing and Verification

- `MANUAL_TESTING_GUIDE.md` - Manual validation scenarios
- `TESTING_CHECKLIST.md` - QA checklist
- `VERIFICATION_QUICK_START.md` - Verification module quick start
- `BACKWARD_COMPATIBILITY_MATRIX.md` - Compatibility checks

## Feature-Specific References

- `CHATBOT_SETUP.md`
- `CHATBOT_QUICK_START.md`
- `CHATBOT_INTEGRATION_SUMMARY.md`
- `CHATBOT_QUOTA_ISSUE.md`
- `CHATBOT_VISUAL_GUIDE.md`
- `REPORTS_MODULE.md`
- `RUPEES_AND_HISTORY_GUIDE.md`
- `STAGED_ROLLOUT_GUIDE.md`
- `SPRING_BOOT_VISUAL_GUIDE.md`

## Historical / Implementation Context

These are retained for engineering history and may not be step-by-step setup sources:

- `FLOWTRACK_TRANSFORMATION_BRIEF.md`
- `IMPLEMENTATION_SUMMARY.md`
- `prompts.md`

## Rule of Trust

If two docs conflict, trust values from:

1. `README.md`
2. Runtime config in `src/main/resources/application.properties`
3. Build config in `pom.xml`
