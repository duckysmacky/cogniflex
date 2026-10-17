#!/usr/bin/env bash

# Create a new .context/<ISSUE-ID>/ directory from the skill's core templates.

set -euo pipefail

readonly VALID_KINDS="feature | bug | refactor | investigation"

usage() {
  cat <<'EOF'
Usage: new_issue.sh <ISSUE-ID> [options]

Create an issue folder and add it to .context/ISSUES.md.

Options:
  --kind KIND     Issue kind: feature, bug, refactor, or investigation.
                  Defaults to feature.
  --title TEXT    Issue title. Defaults to ISSUE-ID.
  --description TEXT
                  Short issue description for the catalogue. Defaults to the title.
  --root PATH     Project root where .context/ is stored. Defaults to the
                  current directory. PATH must already exist.
  -h, --help      Show this help text.

Examples:
  new_issue.sh FW-1427 --kind bug --title "Intermittent NVMe discovery" \\
    --description "Drives fail to re-enumerate after S3 resume"
  new_issue.sh payment-system --kind feature
  new_issue.sh auth-refactor --root /path/to/project
EOF
}

die() {
  printf 'error: %s\n' "$*" >&2
  exit 1
}

require_value() {
  local option="$1"
  local remaining="$2"
  (( remaining >= 2 )) || die "$option requires a value"
}

resolve_script_dir() {
  local source_path="${BASH_SOURCE[0]}"

  while [[ -L "$source_path" ]]; do
    local source_dir
    source_dir="$(cd -P -- "$(dirname "$source_path")" && pwd)"
    source_path="$(readlink "$source_path")"
    [[ "$source_path" == /* ]] || source_path="$source_dir/$source_path"
  done

  cd -P -- "$(dirname "$source_path")" && pwd
}

issue_id=""
issue_kind="feature"
issue_title=""
issue_description=""
project_root="."

while (( $# > 0 )); do
  case "$1" in
    --kind)
      require_value "$1" "$#"
      issue_kind="$2"
      shift 2
      ;;
    --title)
      require_value "$1" "$#"
      issue_title="$2"
      shift 2
      ;;
    --description)
      require_value "$1" "$#"
      issue_description="$2"
      shift 2
      ;;
    --root)
      require_value "$1" "$#"
      project_root="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    --*)
      die "unknown option: $1"
      ;;
    *)
      [[ -z "$issue_id" ]] || die "unexpected extra argument: $1"
      issue_id="$1"
      shift
      ;;
  esac
done

[[ -n "$issue_id" ]] || {
  usage >&2
  die "missing ISSUE-ID"
}

[[ "$issue_id" =~ ^[A-Za-z0-9][A-Za-z0-9._-]*$ ]] ||
  die "ISSUE-ID must contain only letters, numbers, dots, underscores, and hyphens"

case "$issue_kind" in
  feature|bug|refactor|investigation) ;;
  *) die "--kind must be one of: $VALID_KINDS" ;;
esac

[[ -d "$project_root" ]] || die "project root does not exist: $project_root"
project_root="$(cd -P -- "$project_root" && pwd)"

[[ -n "$issue_title" ]] || issue_title="$issue_id"
[[ "$issue_title" != *$'\n'* && "$issue_title" != *$'\r'* ]] ||
  die "--title must be a single line"
[[ -n "$issue_description" ]] || issue_description="$issue_title"
[[ "$issue_description" != *$'\n'* && "$issue_description" != *$'\r'* ]] ||
  die "--description must be a single line"

readonly script_dir="$(resolve_script_dir)"
readonly templates_dir="$script_dir/../assets/templates"
readonly context_dir="$project_root/.context"
readonly issue_dir="$context_dir/$issue_id"
readonly catalogue_path="$context_dir/ISSUES.md"
readonly core_files=(
  ISSUE.md
  FINDINGS.md
  ATTEMPTS.md
  DECISIONS.md
  CONNECTIONS.md
  HANDOFF.md
)
readonly supporting_directories=(
  notes
  sources
  attachments
)

[[ -d "$templates_dir" ]] || die "templates directory not found: $templates_dir"
[[ -f "$templates_dir/ISSUES.md" ]] ||
  die "missing issue catalogue template: $templates_dir/ISSUES.md"

for name in "${core_files[@]}"; do
  [[ -f "$templates_dir/$name" ]] || die "missing core template: $templates_dir/$name"
done

[[ ! -e "$issue_dir" ]] ||
  die "$issue_dir already exists; resume the existing issue instead"

mkdir -p "$context_dir"

staging_dir="$(mktemp -d "$context_dir/.new-issue.XXXXXX")"
cleanup() {
  if [[ -n "${staging_dir:-}" && -d "$staging_dir" ]]; then
    rm -rf "$staging_dir"
  fi
}
trap cleanup EXIT

mkdir -p "${supporting_directories[@]/#/$staging_dir/}"

sed_escape() {
  printf '%s' "$1" | sed -e 's/[\\&|]/\\&/g'
}

markdown_table_cell() {
  printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e 's/|/\\|/g'
}

readonly today="$(date +%Y-%m-%d)"
readonly now="$(date '+%Y-%m-%d %H:%M')"
escaped_id="$(sed_escape "$issue_id")"
escaped_kind="$(sed_escape "$issue_kind")"
escaped_title="$(sed_escape "$issue_title")"
escaped_date="$(sed_escape "$today")"
escaped_now="$(sed_escape "$now")"

sed \
  -e "s|<ISSUE-ID>|$escaped_id|g" \
  -e 's#status: open | blocked | done#status: open#' \
  -e "s#kind: feature | bug | refactor | investigation#kind: $escaped_kind#" \
  -e "s|<YYYY-MM-DD>|$escaped_date|g" \
  -e "s|# <Issue title>|# $escaped_title|" \
  "$templates_dir/ISSUE.md" > "$staging_dir/ISSUE.md"

for name in FINDINGS.md ATTEMPTS.md DECISIONS.md; do
  sed -n '1p' "$templates_dir/$name" > "$staging_dir/$name"
  printf '\n' >> "$staging_dir/$name"
done

sed -n '1,4p' "$templates_dir/CONNECTIONS.md" > "$staging_dir/CONNECTIONS.md"

sed \
  -e "s|<YYYY-MM-DD HH:MM> by <agent/tool>|$escaped_now by new_issue.sh|" \
  -e 's|<In one to three sentences, state where the issue stands right now.>|Issue context created; implementation has not started.|' \
  -e 's|<Point to the relevant ATTEMPTS.md entry, or restate it in one line.>|Nothing yet.|' \
  -e 's|<Give one concrete action that a cold-start agent can take immediately.>|Populate ISSUE.md, then begin the first concrete investigation or implementation step.|' \
  -e 's|<List gotchas, ruled-out red herrings, or fragile state that should not be rediscovered.>|None recorded.|' \
  "$templates_dir/HANDOFF.md" > "$staging_dir/HANDOFF.md"

mv "$staging_dir" "$issue_dir"
staging_dir=""
trap - EXIT

if [[ ! -e "$catalogue_path" ]]; then
  cp "$templates_dir/ISSUES.md" "$catalogue_path"
fi
[[ -f "$catalogue_path" ]] || die "issue catalogue is not a file: $catalogue_path"
printf '| [%s](%s/ISSUE.md) | %s | open |\n' \
  "$issue_id" "$issue_id" "$(markdown_table_cell "$issue_description")" \
  >> "$catalogue_path"

printf 'Created %s\n' "$issue_dir"
printf 'Added %s to %s\n' "$issue_id" "$catalogue_path"
printf 'Next: populate ISSUE.md summary, scope, acceptance criteria, and links.\n'
printf 'Created supporting directories: notes/, sources/, and attachments/.\n'
