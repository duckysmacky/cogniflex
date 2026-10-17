# Riser and slot observations

The investigation compared one two-slot riser and one three-slot riser.

## Test matrix

| Riser | Physical position | Setup choice | Result |
|---|---|---|---|
| two-slot | upper x16 | primary | 47–48 drives |
| three-slot | upper x8 | primary | PCIe link loss; 0 drives |
| three-slot | middle x8 | secondary | 48 drives |
| three-slot | lower x8 | tertiary | 48 drives |
| any | cascade support disabled | n/a | default allocation; topology can exceed the stack window |

The customer also reported a boot loop for one mismatched physical-slot/setup combination. This supports resolving the UI choice through a board mapping table and documenting that the selected slot must match cabling.
