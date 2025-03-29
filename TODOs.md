* port to v13
jQuery use:
* render chat log hooks

TODOs:

Testing:
* Test remove event button
* Test handle event button
* Test change stages in cult event activity
* Test @atWar Assassination Attempt during war
* Test dangerous/continues expression context events
* Test modifiers for structures like @hasSewerSystem
* Recheck all event descriptions

Impl:
* Roll events on 2 different tables (official module needs to translate to ids via uuids, otherwise reuse the id returned from the shipped roll table)
* When clicking on Roll Event/Roll Kingdom Event table:
  * Check if the id is either the expected uuid or activity id and render a preview in chat plus an add event button
* On clicking Add Event, show a popup with a list of enabled events and list their details on choosing them
* Hook up resolve event button in chat; that one needs to work on ongoing event index instead of id