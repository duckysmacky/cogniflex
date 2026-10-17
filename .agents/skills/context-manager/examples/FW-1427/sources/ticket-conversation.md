# Ticket conversation

**Firmware engineer:** It is unclear where the claimed limit of 40 identifiers came from; neither the supplied capture nor the explanation shows that number. It is also unclear which configuration produced the capture. Firmware setup can reserve additional buses for each root port under the socket I/O configuration.

**Platform engineer:** The task says resources are required for four NVMe shelves. At an estimated 30 PCIe BDF identifiers per shelf, the request is about 120, while the current allocation is reported as 40.

**Customer engineer:** Increasing the extra-bus reservation does not solve the problem.

**Firmware engineer:** The setup field appears to have a maximum value of seven. We can make a build with a higher maximum, but first we need to establish where the reported 40-BDF limit originates.

**Platform engineer after customer follow-up:** The captures showing the BDF problem were collected from the shelf by the shelf-integration engineer. The customer storage team confirms that the extra-bus setting did not help.
