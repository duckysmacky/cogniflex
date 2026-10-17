# Follow-up prompt

After another conversation with the customer storage team, we learned that the problem is not the total number of buses assigned to a socket. Two shelves, or 48 drives, are required per socket. The likely issue is that too much of the socket's bus-number space is reserved for neighbouring ports that are empty or lightly used, leaving too little for the single stack that hosts the cascaded shelves.

The board review identified unused high-speed links beside the cascade slot: two link groups on one neighbouring stack and one link group on another were unused in the tested configuration.

The initial suggestion was to disable allocation for those links and give the freed buses to the cascade slot. The implementation should prefer redistributing bus-number windows without physically disabling ports, validate the final map, and fall back safely if the requested layout cannot fit. A forced policy is acceptable for initial testing; production behavior should be controlled through firmware setup.

Combine this information with the existing findings and produce a complete solution plan. An earlier experimental change is stashed and can be revisited if its assumptions still hold.
