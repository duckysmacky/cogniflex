# Debug boot logging optimization

Full debug firmware emitted multi-megabyte serial captures and altered boot timing enough to obscure the readiness race. The useful build keeps only state transitions and anomalies.

## Keep

- Segment, socket, stack, and BDF for a recovery candidate
- Original and final stack windows
- Link-active state and identity classification
- BAR sizing result
- Recovery stage, elapsed time, and terminal outcome
- One end-of-boot count summary

## Remove

- Per-poll success messages
- Repeated configuration dumps
- Full setup variables, inventory identifiers, network configuration, and management credentials
- Unrelated memory-training and driver traces

Example format:

```text
[CASCADE] map socket=0 stack=1 old=0x40-0x7f new=0x40-0xbf valid=1
[CASCADE] defer endpoint=0001:a4:00.0 reason=identity-absent
[CASCADE] recover endpoint=0001:a4:00.0 stage=leaf-reset elapsed-ms=428 result=ready
[CASCADE] summary endpoints=96 recovered=1 quarantined=0 elapsed-ms=842
```
