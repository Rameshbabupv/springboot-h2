# Multi-Agent Model Usage Guide

This project uses intelligent model selection to optimize cost and performance across three Claude models.

## Quick Reference

| Model | Agent | Use For | Cost | Speed |
|-------|-------|---------|------|-------|
| **Opus** | opus-agent | Architecture, Planning, Design | High | Slower |
| **Sonnet** | sonnet-agent | Implementation, Bug Fixes, Development | Medium | Medium |
| **Haiku** | haiku-agent | Summaries, Boilerplate, Automation | Low | Fast |

## Automatic Model Selection

The project is configured with `opusplan` mode, which automatically:
- Uses **Opus** during planning phases for architectural thinking
- Switches to **Sonnet** during implementation for coding
- Delegates to **Haiku** for mechanical tasks

## When to Use Each Agent

### Use Opus Agent For:
- Planning new features
- Designing data models and GraphQL schemas
- Making architectural decisions
- Evaluating implementation approaches
- Complex system design
- Major refactoring plans
- Performance/scalability architecture

**Example prompts:**
```
Design the architecture for real-time notifications
Plan the data model for the performance review module
Evaluate approaches for implementing audit logging
```

### Use Sonnet Agent For:
- Implementing features from specs
- Writing new code (entities, services, resolvers)
- Fixing bugs
- Refactoring code
- Adding GraphQL queries/mutations
- Writing tests
- General development work

**Example prompts:**
```
Implement the leave approval workflow
Fix the employee search filter bug
Add a GraphQL mutation for updating company details
Refactor the attendance calculation service
```

### Use Haiku Agent For:
- Summarizing log files
- Converting documentation formats
- Generating boilerplate code
- Creating mock test data
- Bulk file operations
- Quick code searches
- Extracting error information

**Example prompts:**
```
Summarize the Maven build errors
Generate 50 mock employee records for testing
Convert these notes to markdown format
List all GraphQL resolver files in the project
Extract stack traces from the application log
```

## Cost Optimization Tips

1. **Let automatic delegation work** - The system will choose the right agent based on task complexity
2. **Be explicit when needed** - Use agent names if you want a specific model
3. **Use Haiku for bulk work** - Logs, summaries, and boilerplate are perfect for Haiku
4. **Reserve Opus for planning** - Don't use Opus for simple implementation tasks
5. **Monitor with /cost** - Check token usage to verify optimal model selection

## Manual Agent Invocation

You can explicitly request a specific agent:

```bash
# Explicit agent selection
> Use opus-agent to design the payroll module architecture

> Use sonnet-agent to implement the attendance GraphQL resolver

> Use haiku-agent to summarize the last 500 lines of application.log
```

## Session Commands

```bash
/model opus      # Switch current session to Opus
/model sonnet    # Switch current session to Sonnet
/model haiku     # Switch current session to Haiku
/cost            # View token usage by model
```

## Configuration Files

- **Settings**: `.claude/settings.local.json` - Model defaults and environment variables
- **Opus Agent**: `.claude/agents/opus-agent.md` - Architecture and planning agent
- **Sonnet Agent**: `.claude/agents/sonnet-agent.md` - Development and implementation agent
- **Haiku Agent**: `.claude/agents/haiku-agent.md` - Automation and efficiency agent

## Expected Cost Savings

Based on typical usage patterns:

| Task Type | Traditional (All Sonnet) | Optimized (Multi-Model) | Savings |
|-----------|-------------------------|-------------------------|---------|
| Architecture Planning | High | High (Opus justified) | 0% |
| Feature Implementation | Medium | Medium (Sonnet) | 0% |
| Log Summarization | Medium | Low (Haiku) | ~70% |
| Boilerplate Generation | Medium | Low (Haiku) | ~70% |
| Bulk Operations | Medium | Low (Haiku) | ~70% |

**Overall estimated savings: 30-40% on typical mixed workloads**

## Troubleshooting

### Agent not being selected automatically?
- Make sure your task description is clear
- Use explicit agent names if automatic selection fails
- Check `.claude/settings.local.json` for correct configuration

### Want to change default model?
Edit `.claude/settings.local.json`:
```json
{
  "model": "sonnet"  // or "opus", "haiku", "opusplan"
}
```

### Agent using wrong tools?
- Each agent has restricted tool access defined in their .md files
- Opus: Read-only tools for analysis
- Sonnet: Full edit/write capabilities
- Haiku: Minimal toolset for efficiency

## Best Practices

1. **Start with automatic delegation** - Let the system choose based on task complexity
2. **Use descriptive prompts** - Clear task descriptions help automatic agent selection
3. **Batch similar tasks** - Group log summaries, boilerplate generation to maximize Haiku usage
4. **Plan before implementing** - Use Opus for design, Sonnet for execution
5. **Monitor costs** - Regular `/cost` checks ensure optimal usage

## Version Information

- **Opus Model**: claude-opus-4-5-20251101
- **Sonnet Model**: claude-sonnet-4-5-20250929-v1:0
- **Haiku Model**: claude-haiku-4-5-20251001
- **Default Mode**: opusplan (auto-switches between Opus planning and Sonnet execution)

---

**This configuration is permanent and applies to all future Claude Code sessions in this project.**

For more information, see:
- Claude Code Documentation: https://code.claude.com/docs
- Model Configuration: https://code.claude.com/docs/en/model-config.md
- Subagents Guide: https://code.claude.com/docs/en/sub-agents.md
