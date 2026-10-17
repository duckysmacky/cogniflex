# Customer conversation

**Firmware engineer:** How many shelves are connected to the socket that received the larger bus pool?

**Customer storage engineer:** There should be two.

**Firmware engineer:** Is a third available for testing?

**Customer storage engineer:** A third exists, but it is currently being validated. We connected all three shelves to different buses—two to one socket and the third to the other—and all 72 drives appeared normally.

**Firmware engineer:** Was that without firmware changes? And is the final requirement 96 drives, 48 per socket?

**Customer storage engineer:** Yes to both. We do not currently have all 96 drives available, but if 48 work on one socket, the other should be equivalent.

**Firmware engineer:** Then it sounds as if the total device count may not be the actual problem.

**Customer storage engineer:** Forty-eight already work on one socket in parallel. They do not work when the shelves are cascaded behind one slot. That is the problem.
