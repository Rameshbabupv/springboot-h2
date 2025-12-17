# Claude Code Configuration & Communication Protocol

## 🎯 Communication Style
**Audience**: Enterprise Architects (20+ years experience)

### Requirements
- ✅ Concise & precise - eliminate fluff
- ✅ Skip fundamentals - assume deep expertise
- ✅ Focus on architectural decisions & trade-offs
- ✅ Direct technical language (no explanatory preambles)
- ✅ Decision matrices, not tutorials
- ✅ Skip code unless critical to understanding

### Anti-Patterns (DON'T DO)
- ❌ Explain basic concepts
- ❌ Include unnecessary code examples
- ❌ Verbose preambles
- ❌ Step-by-step walkthroughs
- ❌ Excessive context-setting
- ❌ Emojis (unless explicitly requested)

---

## 🏗️ Project Context
- **Project**: HRMS Multi-Tenant SaaS (Spring Boot 3.2.0, PostgreSQL, GraphQL)
- **Status**: 5 modules complete, attendance/leave fully implemented
- **Current Branch**: `feature/attendance-leave-backend-data`
- **Scale**: 50 entities, 27 resolvers, 6 GraphQL schemas

---

## 🔧 Preferred Tools & Commands
**Always use**:
- Serena symbolic tools (find_symbol, replace_symbol_body, etc.) for precision
- Task tool with specialized agents (opus/sonnet/haiku) for multi-step work
- Memory for context persistence across conversations

**Git Operations**: `git add`, `git commit`, `git push` (with proper safety checks)

**Build**: `/home/sysadmin/tools/apache-maven-3.9.6/bin/mvn`

---

## 📐 Architecture Discussions
- **Pattern Focus**: Integration patterns, scalability trade-offs, failure modes
- **Decision Format**: Options → Pros/Cons → Recommendation (with rationale)
- **Enterprise Patterns**: ETL, CDC, event-driven, CQRS, saga patterns
- **Non-Functional Requirements**: Performance, scalability, observability, reliability

---

## 📝 Output Format
- **Heading Level**: Max H3 (###) - keep hierarchy flat
- **Lists**: Use bullet points, not prose paragraphs
- **Tables**: For comparisons (pattern comparison, failure modes, etc.)
- **Code**: Only if essential; explain via architecture diagrams instead

---

## 🔐 Constitutional Rules (Hard Constraints)
- ✅ **Backend**: Read & write enabled
- ✅ **API Specs**: Read & write enabled
- ❌ **Frontend**: Read-only (NEVER write/edit)
- ✅ **STAGE Protocol**: Mandatory for complex architectural decisions

---

_Last Updated: 2025-Dec-12_
_Maintained by: Claude Code (Haiku 4.5)_
