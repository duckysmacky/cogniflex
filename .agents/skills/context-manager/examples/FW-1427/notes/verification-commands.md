# FW-1427 verification procedure

Run the same captures before and after the firmware change with identical shelf cabling. Store results under a stable run ID.

## UEFI Shell

```text
map -r
fs0:
mkdir FW-1427
cd FW-1427
ver > shell-version.txt
devices > devices.txt
drivers > drivers.txt
connect -r
map -r > map-after-connect.txt
pci > pci-all.txt
```

Use the shell's local `help pci` output before issuing targeted configuration reads. Record targeted configuration reads by full segment and BDF.

## Linux

```sh
lspci -Dnn > pci-devices.txt
lspci -Dtv > pci-tree.txt
ls -1 /sys/class/nvme > nvme-controllers.txt
nvme list > nvme-namespaces.txt
journalctl -b -k > kernel-pci.txt
```

For each expected endpoint, verify a valid vendor/device identity, assigned nonzero BAR0, bound NVMe controller, and namespace. Record the stable drive identifier used by the lab inventory.

## Boot matrix

| Configuration | Minimum |
|---|---:|
| Recovery disabled, full cascade, cold boot | 30 |
| Conservative recovery, full cascade, cold boot | 30 |
| Aggressive recovery, full cascade, cold boot | 30 |
| Selected production policy, warm boot | 30 |
| Empty supported slot | 10 |
| Second shelf disconnected | 10 |

Record: endpoint/controller/namespace counts, recovery stage, quarantines, bus-exhaustion warnings, BAR conflicts, watchdogs, and firmware boot time. Stop and preserve the first complete failure capture.
