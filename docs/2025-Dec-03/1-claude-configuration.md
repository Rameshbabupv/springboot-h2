# Claude Code Configuration & Documentation Standards

**Last Updated**: 2025-12-03
**Configuration Status**: ACTIVE

---

## 1️⃣ Critical Requirement: Model Disclosure

**MANDATORY INSTRUCTION**: Every response from Claude Code must include which model is being used.

### Rule
- **NO EXCEPTIONS** - Apply to all responses without exception
- Must be clearly stated in every message
- Include the model name and/or model ID

### Current Model Details
- **Active Model**: Claude Haiku 4.5
- **Model ID**: claude-haiku-4-5-20251001
- **Latest Frontier Model Available**: Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Implementation Requirements
This requirement applies to:
1. Every response text sent to the user
2. Every tool output summary
3. Every message and communication
4. All explanations and assistance provided

### Why This Matters
Disclosure of the model being used helps maintain transparency about:
- The capabilities and limitations of the response
- The version of Claude being utilized
- Resource allocation and processing details

---

## 2️⃣ Documentation Organization Standard

**MANDATORY RULE**: All project documentation follows a strict date-based organization system.

### Directory Structure

```
docs/
├── yyyy-mmm-dd/          # Date-based folders (e.g., 2025-Dec-03)
│   ├── 1-feature-name.md
│   ├── 2-another-feature.md
│   └── N-feature-name.md
├── archive/              # Old/superseded documents
├── product/              # User-facing documentation (static)
├── INDEX.md              # Navigation index and timeline
└── README.md             # Documentation guide
```

### Naming Convention

**Format**: `yyyy-mmm-dd/N-descriptive-name.md`

**Examples**:
- `2025-Dec-03/1-documentation-organization.md`
- `2025-Dec-02/3-graphql-logging-implementation.md`
- `2025-Nov-24/1-database-schema.md`

**Rules**:
- Use format `yyyy-mmm-dd` (e.g., 2025-Dec-03, 2025-Nov-24)
- Prefix files with numbers (1-, 2-, 3-, etc.) for ordering
- Use lowercase hyphens for file names
- Keep names descriptive but concise

### File Organization Rules

| Situation | Action |
|-----------|--------|
| Today's work | Create `docs/yyyy-mmm-dd/` folder (one per day) |
| New documentation | Add to today's date folder with next number |
| Incomplete/Old docs | Move to `docs/archive/` when not needed |
| Static product docs | Keep in `docs/product/` unchanged |
| Daily organization | Update `docs/INDEX.md` at end of day |

### Index File Maintenance

Every documentation folder should have:
1. **INDEX.md** - Complete timeline and navigation guide
   - Lists all date folders with brief descriptions
   - Quick navigation section for major features
   - Statistics on total files

2. **README.md** - User guide for the documentation
   - How to use the organization system
   - Quick links and navigation tips
   - Best practices for new documentation

### When to Create New Folders

- **Create** a new date folder: First documentation of the day
- **Use same folder**: All work done on the same day
- **Archive old**: Docs older than current day (if not actively needed)

### Archive Strategy

Move to `docs/archive/` when:
- Document is superseded by newer version
- Document is reference-only (historical)
- Document older than 7-14 days and not frequently accessed
- Explicitly marked as "old" or "legacy"

**Keep in archive**:
- All migration records
- Historical error resolutions
- Superseded implementation approaches
- Reference materials

### Best Practices for New Documentation

1. **Check if folder exists** for today's date
2. **Find next number** in sequence (1-, 2-, 3-, etc.)
3. **Use clear, descriptive names** (hyphenated, lowercase)
4. **Update INDEX.md** with new document entry
5. **Include status badges**: ✅ ⚠️ ❌ for quick scanning
6. **Link to related** documents using relative paths
7. **Include examples** - code samples, outputs, or test results

### Automation Steps

For consistency, each day:
1. Create: `docs/$(date +%Y-%b-%d)/`
2. Create first file: `docs/$(date +%Y-%b-%d)/1-session-start.md`
3. Update: `docs/INDEX.md` with new date entry
4. At session end: Review and archive old docs if needed

---

## 3️⃣ Memory File Updates

All memory files follow this pattern:

**Location**: Internal memory system (mcp__serena__read_memory)

**Current Active Memories**:
- `code_style_conventions` - Java/Spring coding standards
- `communication_protocol` - Backend/Frontend doc locations
- `comprehensive_project_understanding` - Project architecture
- `project_overview` - Project scope and modules
- `tech_stack` - Technologies and versions
- `code_structure` - File organization patterns

**Update Rule**: Update memory when documentation organization changes or new standards established

---

## 📋 Documentation Checklist

When creating new documentation:

- [ ] Check today's date folder exists
- [ ] Use next number in sequence (N-)
- [ ] Follow naming convention (yyyy-mmm-dd/N-name.md)
- [ ] Add status badges (✅ ⚠️ ❌)
- [ ] Include relevant examples
- [ ] Link to related documents
- [ ] Update INDEX.md entry
- [ ] Update memory files if rules change
- [ ] Review for clarity and completeness

---

## 🔄 Current Status

✅ **Documentation Organization**: Active and implemented
✅ **Naming Convention**: Applied to all 38 documents
✅ **Index Files**: INDEX.md and README.md created
✅ **Archive System**: Old documents organized in archive/
✅ **Memory Updates**: Pending (this file)

---

**Configuration Status**: ACTIVE
**Next Review**: 2025-12-10 (weekly)
