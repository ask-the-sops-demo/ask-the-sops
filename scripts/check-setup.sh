#!/usr/bin/env bash
# check-setup.sh: confirms a participant laptop is ready for the workshop.
# Run it inside Ubuntu (WSL), from the root of the demo repo:
#     ./scripts/check-setup.sh
# Add --with-tests to also run the Maven tests (slower the first time).

pass=0; fail=0; warn=0
ok()   { printf '  \033[32mPASS\033[0m  %s\n' "$1"; pass=$((pass+1)); }
bad()  { printf '  \033[31mFAIL\033[0m  %s\n        -> %s\n' "$1" "$2"; fail=$((fail+1)); }
note() { printf '  \033[33mWARN\033[0m  %s\n        -> %s\n' "$1" "$2"; warn=$((warn+1)); }

echo "Workshop setup check"
echo "--------------------"

# 1. WSL 2
if grep -qi "microsoft" /proc/version 2>/dev/null; then
  if grep -qiE "wsl2|microsoft-standard" /proc/version; then ok "Running inside WSL 2"
  else bad "Running inside WSL 1" "In Windows PowerShell: wsl --set-version Ubuntu 2"; fi
else
  note "Not running inside WSL" "Fine in a Codespace; otherwise open Ubuntu and run this again"
fi

# 2. Repo location (the Windows drive is slow for builds)
case "$PWD" in
  /mnt/*) bad "Repo is on the Windows drive ($PWD)" "Clone it again under ~/code (Part 4 of the setup guide)";;
  *)      ok  "Repo is in the Linux filesystem ($PWD)";;
esac

# 3. Java 21 or later
if command -v java >/dev/null 2>&1; then
  jv=$(java -version 2>&1 | head -n1 | sed -E 's/.*version "([0-9]+).*/\1/')
  if [ "${jv:-0}" -ge 21 ] 2>/dev/null; then ok "Java $jv"
  else bad "Java ${jv:-unknown} found; 21 or later is needed" "sudo apt install -y openjdk-21-jdk"; fi
else bad "Java not found" "sudo apt install -y openjdk-21-jdk"; fi

# 4. Maven
if command -v mvn >/dev/null 2>&1; then ok "Maven $(mvn -v 2>/dev/null | head -n1 | awk '{print $3}')"
else bad "Maven not found" "sudo apt install -y maven"; fi

# 5. Git identity
if [ -n "$(git config --global user.email)" ] && [ -n "$(git config --global user.name)" ]; then
  ok "Git identity: $(git config --global user.name) <$(git config --global user.email)>"
else bad "Git name or email not set" "git config --global user.name \"Your Name\"; git config --global user.email you@example.com"; fi

# 6. GitHub CLI signed in
if command -v gh >/dev/null 2>&1; then
  if gh auth status >/dev/null 2>&1; then ok "GitHub CLI signed in"
  else bad "GitHub CLI not signed in" "gh auth login, then gh auth setup-git"; fi
else bad "GitHub CLI (gh) not found" "sudo apt install -y gh"; fi

# 7. Can reach the demo repo on GitHub
if git remote get-url origin >/dev/null 2>&1 && git ls-remote --exit-code origin HEAD >/dev/null 2>&1; then
  ok "Can reach the demo repo on GitHub"
else bad "Cannot reach the demo repo" "Run this from inside the cloned repo; check gh auth status and your organization invitation"; fi

# 8. Claude Code
if command -v claude >/dev/null 2>&1; then ok "Claude Code $(claude --version 2>/dev/null | head -n1)"
else bad "Claude Code not found" "curl -fsSL https://claude.ai/install.sh | bash, then open a new terminal"; fi

# 9. Jira through the Atlassian MCP server
if command -v claude >/dev/null 2>&1; then
  mcp=$(claude mcp list 2>/dev/null)
  if echo "$mcp" | grep -qi "atlassian"; then
    if echo "$mcp" | grep -i "atlassian" | grep -qi "connected"; then ok "Jira (Atlassian MCP) connected"
    else note "Atlassian MCP added but not signed in" "Start claude, type /mcp, choose atlassian, then Authenticate"; fi
  else bad "Atlassian MCP server not added" "claude mcp add --transport http atlassian https://mcp.atlassian.com/v2/mcp"; fi
fi

# 10. Ollama and the embedding models
if command -v ollama >/dev/null 2>&1; then
  if models=$(curl -fsS http://localhost:11434/api/tags 2>/dev/null); then
    for m in all-minilm bge-m3; do
      if echo "$models" | grep -q "\"$m"; then ok "Embedding model $m downloaded"
      else bad "Embedding model $m missing" "ollama pull $m"; fi
    done
  else note "Ollama installed but not running" "Run 'ollama serve' in another terminal, then run this check again"; fi
else bad "Ollama not found" "curl -fsSL https://ollama.com/install.sh | sh"; fi

# 11. Optional: run the tests
if [ "$1" = "--with-tests" ]; then
  if mvn -q -B test >/tmp/check-setup-mvn.log 2>&1; then ok "Maven tests pass"
  else bad "Maven tests failed" "See /tmp/check-setup-mvn.log (certificate errors? see Part 3 of the guide)"; fi
fi

echo "--------------------"
echo "PASS: $pass   WARN: $warn   FAIL: $fail"
if [ "$fail" -eq 0 ]; then echo "Your laptop is ready. Next: the hello pull request (Part 9)."
else echo "Fix the FAIL lines above, then run this again."; fi
exit "$fail"
